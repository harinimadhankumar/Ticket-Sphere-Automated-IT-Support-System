package com.powergrid.ticketsystem.sla;

import com.powergrid.ticketsystem.constants.EscalationLevel;
import com.powergrid.ticketsystem.entity.Ticket;
import com.powergrid.ticketsystem.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ============================================================
 * SLA MONITORING SCHEDULER
 * ============================================================
 * 
 * PHASE 5: SLA MONITORING & AUTO ESCALATION
 * 
 * This is the MAIN BACKGROUND SERVICE that runs every 10 minutes
 * to check tickets for SLA breaches and trigger auto-escalation.
 * 
 * ╔═══════════════════════════════════════════════════════════╗
 * ║ SCHEDULER WORKFLOW ║
 * ╠═══════════════════════════════════════════════════════════╣
 * ║ 1. Run every 10 minutes (configurable) ║
 * ║ 2. Find all active tickets (OPEN, IN_PROGRESS, PENDING) ║
 * ║ 3. For each ticket: ║
 * ║ a. Calculate SLA deadline if not set ║
 * ║ b. Check if in warning zone (75%) → send warning ║
 * ║ c. Check if SLA breached → trigger escalation ║
 * ║ d. Check if re-escalation needed → upgrade level ║
 * ║ 4. Log statistics and send notifications ║
 * ╚═══════════════════════════════════════════════════════════╝
 * 
 * IMPORTANT: This scheduler does NOT:
 * - Close tickets automatically
 * - Resolve issues
 * - Change ticket priority
 * - Notify end users
 * 
 * It ONLY monitors SLA and escalates to engineering team.
 */
@Service
public class SlaMonitoringScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SlaMonitoringScheduler.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TicketRepository ticketRepository;
    private final SlaCalculationService slaCalculationService;
    private final EscalationService escalationService;
    private final SlaNotificationService notificationService;

    @Value("${sla.monitoring.enabled:true}")
    private boolean monitoringEnabled;

    @Value("${sla.monitoring.log-details:true}")
    private boolean logDetails;

    // Statistics counters
    private AtomicInteger totalChecks = new AtomicInteger(0);
    private AtomicInteger totalEscalations = new AtomicInteger(0);
    private AtomicInteger totalWarnings = new AtomicInteger(0);

    public SlaMonitoringScheduler(TicketRepository ticketRepository,
            SlaCalculationService slaCalculationService,
            EscalationService escalationService,
            SlaNotificationService notificationService) {
        this.ticketRepository = ticketRepository;
        this.slaCalculationService = slaCalculationService;
        this.escalationService = escalationService;
        this.notificationService = notificationService;
        logger.info("═══════════════════════════════════════════════════════════════");
        logger.info("  SLA Monitoring Scheduler Initialized");
        logger.info("  Interval: Every 10 minutes");
        logger.info("  Status: {}", monitoringEnabled ? "ENABLED" : "DISABLED");
        logger.info("═══════════════════════════════════════════════════════════════");
    }

    /**
     * ┌─────────────────────────────────────────────────────────────┐
     * │ MAIN SCHEDULED TASK │
     * │ Runs every 10 minutes (600,000 ms) │
     * └─────────────────────────────────────────────────────────────┘
     * 
     * This is the heart of the SLA monitoring system.
     * Uses Spring's @Scheduled annotation for automatic execution.
     */
    @Scheduled(fixedRateString = "${sla.monitoring.interval:600000}", initialDelayString = "${sla.monitoring.initial-delay:60000}")
    @Transactional
    public void monitorSlaCompliance() {
        if (!monitoringEnabled) {
            logger.debug("SLA monitoring is disabled, skipping check");
            return;
        }

        LocalDateTime startTime = LocalDateTime.now();
        logger.info("╔═══════════════════════════════════════════════════════════════╗");
        logger.info("║  SLA MONITORING CHECK STARTED                                 ║");
        logger.info("║  Time: {} ║", startTime.format(TIMESTAMP_FORMAT));
        logger.info("╚═══════════════════════════════════════════════════════════════╝");

        int ticketsChecked = 0;
        int slaWarningsSent = 0;
        int ticketsEscalated = 0;
        int ticketsInBreach = 0;

        try {
            // Get all active tickets (not closed/resolved)
            List<Ticket> activeTickets = ticketRepository.findAllActiveTickets();
            logger.info("Found {} active tickets to check", activeTickets.size());

            for (Ticket ticket : activeTickets) {
                ticketsChecked++;

                try {
                    // Step 1: Ensure SLA deadline is set
                    ensureSlaDeadlineSet(ticket);

                    // Step 2: Check warning zone (75% of SLA used)
                    if (checkAndSendWarning(ticket)) {
                        slaWarningsSent++;
                    }

                    // Step 3: Check for SLA breach and escalate
                    EscalationService.EscalationResult result = escalationService.checkAndEscalate(ticket);

                    if (result.isEscalated()) {
                        ticketsEscalated++;
                        ticketsInBreach++;

                        // Send escalation notification
                        EscalationLevel level = EscalationLevel.valueOf(
                                result.getTicket().getEscalationLevel());
                        notificationService.sendEscalationNotification(result.getTicket(), level);

                        if (logDetails) {
                            logEscalation(result.getTicket(), result.getMessage());
                        }
                    } else if (slaCalculationService.isSlaBreached(ticket)) {
                        ticketsInBreach++;
                    }

                    // Update last check time
                    ticket.setLastSlaCheck(LocalDateTime.now());
                    ticketRepository.save(ticket);

                } catch (Exception e) {
                    logger.error("Error processing ticket {}: {}", ticket.getTicketId(), e.getMessage());
                }
            }

            // Update global statistics
            totalChecks.incrementAndGet();
            totalEscalations.addAndGet(ticketsEscalated);
            totalWarnings.addAndGet(slaWarningsSent);

            // Log summary
            logMonitoringSummary(ticketsChecked, slaWarningsSent, ticketsEscalated,
                    ticketsInBreach, startTime);

        } catch (Exception e) {
            logger.error("SLA monitoring check failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Ensure ticket has SLA deadline set.
     * 
     * @param ticket The ticket to check
     */
    private void ensureSlaDeadlineSet(Ticket ticket) {
        if (ticket.getSlaDeadline() == null) {
            LocalDateTime deadline = slaCalculationService.calculateSlaDeadline(ticket);
            ticket.setSlaDeadline(deadline);
            ticketRepository.save(ticket);
            logger.debug("Set SLA deadline for ticket {}: {}", ticket.getTicketId(), deadline);
        }
    }

    /**
     * Check if ticket is in warning zone and send warning if needed.
     * 
     * @param ticket The ticket to check
     * @return true if warning was sent
     */
    private boolean checkAndSendWarning(Ticket ticket) {
        // Skip if already breached or warning already sent
        if (slaCalculationService.isSlaBreached(ticket) ||
                Boolean.TRUE.equals(ticket.getSlaWarningSent())) {
            return false;
        }

        // Check if in warning zone (75-100% of SLA)
        if (slaCalculationService.isInWarningZone(ticket)) {
            notificationService.sendSlaWarningNotification(ticket);
            logger.info("SLA warning sent for ticket: {}", ticket.getTicketId());
            return true;
        }

        return false;
    }

    /**
     * Log escalation details.
     * 
     * @param ticket  Escalated ticket
     * @param message Escalation message
     */
    private void logEscalation(Ticket ticket, String message) {
        logger.warn("┌─────────────────────────────────────────────────────────────┐");
        logger.warn("│ TICKET ESCALATED                                            │");
        logger.warn("├─────────────────────────────────────────────────────────────┤");
        logger.warn("│ Ticket ID: {}", ticket.getTicketId());
        logger.warn("│ Priority: {} | Category: {}", ticket.getPriority(), ticket.getCategory());
        logger.warn("│ Escalation Level: {}", ticket.getEscalationLevel());
        logger.warn("│ Assigned To: {}", ticket.getAssignedEngineer());
        logger.warn("│ Reason: {}", message);
        logger.warn("└─────────────────────────────────────────────────────────────┘");
    }

    /**
     * Log monitoring summary.
     */
    private void logMonitoringSummary(int ticketsChecked, int warningsSent,
            int ticketsEscalated, int ticketsInBreach,
            LocalDateTime startTime) {
        LocalDateTime endTime = LocalDateTime.now();
        long durationMs = java.time.Duration.between(startTime, endTime).toMillis();

        logger.info("╔═══════════════════════════════════════════════════════════════╗");
        logger.info("║  SLA MONITORING CHECK COMPLETED                               ║");
        logger.info("╠═══════════════════════════════════════════════════════════════╣");
        logger.info("║  Duration       : {} ms", String.format("%-42d", durationMs));
        logger.info("║  Tickets Checked: {}", String.format("%-42d", ticketsChecked));
        logger.info("║  Warnings Sent  : {}", String.format("%-42d", warningsSent));
        logger.info("║  Escalations    : {}", String.format("%-42d", ticketsEscalated));
        logger.info("║  In Breach      : {}", String.format("%-42d", ticketsInBreach));
        logger.info("╠═══════════════════════════════════════════════════════════════╣");
        logger.info("║  CUMULATIVE STATISTICS                                        ║");
        logger.info("║  Total Checks   : {}", String.format("%-42d", totalChecks.get()));
        logger.info("║  Total Escalations: {}", String.format("%-40d", totalEscalations.get()));
        logger.info("║  Total Warnings : {}", String.format("%-42d", totalWarnings.get()));
        logger.info("╚═══════════════════════════════════════════════════════════════╝");
    }

    /**
     * ┌─────────────────────────────────────────────────────────────┐
     * │ MANUAL TRIGGER (API) │
     * │ Called from SlaController for manual check │
     * └─────────────────────────────────────────────────────────────┘
     * 
     * @return MonitoringResult with statistics
     */
    public MonitoringResult triggerManualCheck() {
        logger.info("Manual SLA monitoring check triggered");

        LocalDateTime startTime = LocalDateTime.now();
        int ticketsChecked = 0;
        int ticketsEscalated = 0;
        int ticketsInBreach = 0;

        List<Ticket> activeTickets = ticketRepository.findAllActiveTickets();

        for (Ticket ticket : activeTickets) {
            ticketsChecked++;

            try {
                ensureSlaDeadlineSet(ticket);

                EscalationService.EscalationResult result = escalationService.checkAndEscalate(ticket);

                if (result.isEscalated()) {
                    ticketsEscalated++;
                    ticketsInBreach++;

                    EscalationLevel level = EscalationLevel.valueOf(
                            result.getTicket().getEscalationLevel());
                    notificationService.sendEscalationNotification(result.getTicket(), level);
                } else if (slaCalculationService.isSlaBreached(ticket)) {
                    ticketsInBreach++;
                }

            } catch (Exception e) {
                logger.error("Error processing ticket {}: {}", ticket.getTicketId(), e.getMessage());
            }
        }

        return new MonitoringResult(
                ticketsChecked,
                ticketsEscalated,
                ticketsInBreach,
                java.time.Duration.between(startTime, LocalDateTime.now()).toMillis());
    }

    /**
     * ┌─────────────────────────────────────────────────────────────┐
     * │ DAILY SUMMARY REPORT │
     * │ Runs at 8:00 AM every day │
     * └─────────────────────────────────────────────────────────────┘
     */
    @Scheduled(cron = "${sla.monitoring.daily-summary-cron:0 0 8 * * ?}")
    public void sendDailySummaryReport() {
        if (!monitoringEnabled) {
            return;
        }

        logger.info("Generating daily SLA summary report...");

        try {
            java.util.Map<String, Object> stats = escalationService.getEscalationStatistics();
            notificationService.sendDailySummary(stats);
            logger.info("Daily SLA summary report sent successfully");
        } catch (Exception e) {
            logger.error("Failed to send daily summary report: {}", e.getMessage());
        }
    }

    /**
     * Get current monitoring statistics.
     * 
     * @return Statistics map
     */
    public java.util.Map<String, Object> getMonitoringStatistics() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        stats.put("totalChecks", totalChecks.get());
        stats.put("totalEscalations", totalEscalations.get());
        stats.put("totalWarnings", totalWarnings.get());
        stats.put("monitoringEnabled", monitoringEnabled);
        stats.put("lastCheckTime", LocalDateTime.now().format(TIMESTAMP_FORMAT));

        // Add database statistics
        stats.put("activeTickets", ticketRepository.findAllActiveTickets().size());
        stats.put("slaBreachedTickets", ticketRepository.countBySlaBreachedTrue());

        return stats;
    }

    /**
     * Inner class for monitoring result.
     */
    public static class MonitoringResult {
        private final int ticketsChecked;
        private final int ticketsEscalated;
        private final int ticketsInBreach;
        private final long durationMs;

        public MonitoringResult(int ticketsChecked, int ticketsEscalated,
                int ticketsInBreach, long durationMs) {
            this.ticketsChecked = ticketsChecked;
            this.ticketsEscalated = ticketsEscalated;
            this.ticketsInBreach = ticketsInBreach;
            this.durationMs = durationMs;
        }

        public int getTicketsChecked() {
            return ticketsChecked;
        }

        public int getTicketsEscalated() {
            return ticketsEscalated;
        }

        public int getTicketsInBreach() {
            return ticketsInBreach;
        }

        public long getDurationMs() {
            return durationMs;
        }
    }
}
