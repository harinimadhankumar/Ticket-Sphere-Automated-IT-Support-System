package com.powergrid.ticketsystem.sla;

import com.powergrid.ticketsystem.constants.EscalationLevel;
import com.powergrid.ticketsystem.constants.SlaConfiguration;
import com.powergrid.ticketsystem.entity.Ticket;
import com.powergrid.ticketsystem.notification.NotificationService;
import com.powergrid.ticketsystem.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================
 * ESCALATION SERVICE
 * ============================================================
 * 
 * PHASE 5: SLA MONITORING & AUTO ESCALATION
 * 
 * This service handles the escalation of tickets when SLA is breached.
 * 
 * ESCALATION PROCESS:
 * ───────────────────
 * 1. Mark ticket as SLA breached
 * 2. Update ticket status to ESCALATED
 * 3. Set appropriate escalation level
 * 4. Reassign to senior engineer or escalation team
 * 5. Record escalation reason and timestamp
 * 
 * WHY AUTO-ESCALATION IS IMPORTANT:
 * ─────────────────────────────────
 * 1. Accountability: Ensures someone senior is aware
 * 2. Visibility: Management sees delayed tickets
 * 3. Resource Allocation: Enables faster resolution
 * 4. SLA Compliance: Helps meet organizational targets
 * 5. No Manual Intervention: Works 24/7 automatically
 * 
 * ESCALATION HIERARCHY:
 * ─────────────────────
 * LEVEL_1 → Senior Engineer (first breach)
 * LEVEL_2 → Team Lead (50% overtime)
 * LEVEL_3 → Department Head (100% overtime)
 */
@Service
public class EscalationService {

    private static final Logger logger = LoggerFactory.getLogger(EscalationService.class);

    private final TicketRepository ticketRepository;
    private final SlaCalculationService slaCalculationService;
    private NotificationService notificationService;

    // Senior engineers by team (for escalation assignment)
    private final Map<String, String> seniorEngineers = new HashMap<>();

    public EscalationService(TicketRepository ticketRepository,
            SlaCalculationService slaCalculationService) {
        this.ticketRepository = ticketRepository;
        this.slaCalculationService = slaCalculationService;
        initializeSeniorEngineers();
    }

    /**
     * Setter injection for NotificationService (Phase 8).
     */
    @Autowired
    @Lazy
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
        logger.info("NotificationService injected into Escalation Service");
    }

    /**
     * Initialize senior engineers for each team.
     * These are the Level 1 escalation contacts.
     */
    private void initializeSeniorEngineers() {
        seniorEngineers.put("Network Team", "Senior Network Engineer");
        seniorEngineers.put("Hardware Support Team", "Senior Hardware Engineer");
        seniorEngineers.put("Application Support Team", "Senior Application Engineer");
        seniorEngineers.put("IT Security Team", "Senior Security Engineer");
        seniorEngineers.put("Email Support Team", "Senior Email Administrator");
        seniorEngineers.put("General IT Support", "Senior IT Support Engineer");
        seniorEngineers.put("default", "Senior IT Engineer");

        logger.info("Escalation Service initialized with {} team mappings", seniorEngineers.size());
    }

    /**
     * Escalate a ticket due to SLA breach.
     * 
     * @param ticket The ticket to escalate
     * @param level  The escalation level
     * @return The escalated ticket
     */
    @Transactional
    public Ticket escalateTicket(Ticket ticket, EscalationLevel level) {
        logger.info("Escalating ticket {} to level: {}", ticket.getTicketId(), level);

        // Store previous engineer for tracking
        if (ticket.getAssignedEngineer() != null) {
            ticket.setPreviousEngineer(ticket.getAssignedEngineer());
        }

        // Update escalation fields
        ticket.setEscalationLevel(level.name());
        ticket.setEscalatedTime(LocalDateTime.now());
        ticket.setEscalationCount(
                ticket.getEscalationCount() != null ? ticket.getEscalationCount() + 1 : 1);

        // Set SLA breach flag if not already set
        if (Boolean.FALSE.equals(ticket.getSlaBreached()) || ticket.getSlaBreached() == null) {
            ticket.setSlaBreached(true);
            ticket.setSlaBreachTime(LocalDateTime.now());
        }

        // Build escalation reason
        String reason = buildEscalationReason(ticket, level);
        ticket.setEscalationReason(reason);

        // Reassign based on escalation level
        String newAssignee = getEscalationAssignee(ticket, level);
        ticket.setAssignedEngineer(newAssignee);

        // Update ticket status
        ticket.setStatus("ESCALATED");
        ticket.setLastSlaCheck(LocalDateTime.now());

        // Save and return
        Ticket savedTicket = ticketRepository.save(ticket);

        logger.info("Ticket {} escalated successfully: Level={}, NewAssignee={}, Reason={}",
                ticket.getTicketId(), level, newAssignee, reason);

        // ================================================================
        // PHASE 8: SEND TICKET_ESCALATED NOTIFICATION
        // ================================================================
        try {
            if (notificationService != null) {
                notificationService.notifyTicketEscalated(savedTicket, level.name());
            }
        } catch (Exception e) {
            logger.warn("Failed to send TICKET_ESCALATED notification for {}: {}",
                    ticket.getTicketId(), e.getMessage());
        }

        return savedTicket;
    }

    /**
     * Build escalation reason message.
     * 
     * @param ticket The ticket
     * @param level  Escalation level
     * @return Reason message
     */
    private String buildEscalationReason(Ticket ticket, EscalationLevel level) {
        double overtimeHours = slaCalculationService.calculateOvertimeHours(ticket);
        int slaHours = SlaConfiguration.getSlaHoursForPriority(ticket.getPriority());

        return String.format(
                "SLA Breach - %s escalation. Priority: %s, SLA: %d hours, Overtime: %.1f hours (%.0f%% of SLA)",
                level.getDisplayName(),
                ticket.getPriority(),
                slaHours,
                overtimeHours,
                slaCalculationService.calculateSlaPercentage(ticket));
    }

    /**
     * Get the appropriate assignee for escalation level.
     * 
     * @param ticket The ticket
     * @param level  Escalation level
     * @return Assignee name/role
     */
    private String getEscalationAssignee(Ticket ticket, EscalationLevel level) {
        switch (level) {
            case LEVEL_1:
                return getSeniorEngineerForTeam(ticket.getAssignedTeam());
            case LEVEL_2:
                return SlaConfiguration.LEVEL_2_ESCALATION_CONTACT + " - " + ticket.getAssignedTeam();
            case LEVEL_3:
                return SlaConfiguration.LEVEL_3_ESCALATION_CONTACT + " - IT Department";
            default:
                return ticket.getAssignedEngineer();
        }
    }

    /**
     * Get senior engineer for a team.
     * 
     * @param team Team name
     * @return Senior engineer name
     */
    public String getSeniorEngineerForTeam(String team) {
        if (team == null) {
            return seniorEngineers.get("default");
        }
        return seniorEngineers.getOrDefault(team, seniorEngineers.get("default"));
    }

    /**
     * Check and escalate ticket if SLA is breached.
     * This is the main method called by the scheduler.
     * 
     * @param ticket The ticket to check
     * @return EscalationResult with details
     */
    @Transactional
    public EscalationResult checkAndEscalate(Ticket ticket) {
        // Skip closed/resolved tickets
        if (isTicketClosed(ticket)) {
            return new EscalationResult(ticket, false, "Ticket is already closed/resolved");
        }

        // Calculate SLA deadline if not set
        if (ticket.getSlaDeadline() == null) {
            ticket.setSlaDeadline(slaCalculationService.calculateSlaDeadline(ticket));
        }

        // Check if SLA is breached
        if (!slaCalculationService.isSlaBreached(ticket)) {
            // Check for warning zone
            if (slaCalculationService.isInWarningZone(ticket)
                    && Boolean.FALSE.equals(ticket.getSlaWarningSent())) {
                return new EscalationResult(ticket, false, "In warning zone - notification pending");
            }
            return new EscalationResult(ticket, false, "Within SLA limits");
        }

        // Determine required escalation level
        EscalationLevel requiredLevel = slaCalculationService.determineEscalationLevel(ticket);
        EscalationLevel currentLevel = getCurrentEscalationLevel(ticket);

        // Check if escalation upgrade is needed
        if (requiredLevel.getSeverity() > currentLevel.getSeverity()) {
            Ticket escalatedTicket = escalateTicket(ticket, requiredLevel);
            return new EscalationResult(
                    escalatedTicket,
                    true,
                    String.format("Escalated from %s to %s", currentLevel, requiredLevel));
        }

        return new EscalationResult(ticket, false,
                String.format("Already at %s escalation level", currentLevel));
    }

    /**
     * Get current escalation level of ticket.
     * 
     * @param ticket The ticket
     * @return Current escalation level
     */
    private EscalationLevel getCurrentEscalationLevel(Ticket ticket) {
        if (ticket.getEscalationLevel() == null) {
            return EscalationLevel.NONE;
        }
        try {
            return EscalationLevel.valueOf(ticket.getEscalationLevel());
        } catch (IllegalArgumentException e) {
            return EscalationLevel.NONE;
        }
    }

    /**
     * Check if ticket is in closed/resolved state.
     * 
     * @param ticket The ticket
     * @return true if closed/resolved
     */
    private boolean isTicketClosed(Ticket ticket) {
        String status = ticket.getStatus();
        return status != null &&
                (status.equalsIgnoreCase("CLOSED") ||
                        status.equalsIgnoreCase("RESOLVED"));
    }

    /**
     * Get escalation statistics.
     * 
     * @return Map of statistics
     */
    public Map<String, Object> getEscalationStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalSlaBreaches", ticketRepository.countBySlaBreachedTrue());
        stats.put("level1Escalations", ticketRepository.countByEscalationLevel("LEVEL_1"));
        stats.put("level2Escalations", ticketRepository.countByEscalationLevel("LEVEL_2"));
        stats.put("level3Escalations", ticketRepository.countByEscalationLevel("LEVEL_3"));

        // Breaches by priority
        Map<String, Long> breachesByPriority = new HashMap<>();
        breachesByPriority.put("CRITICAL", ticketRepository.countByPriorityAndSlaBreachedTrue("CRITICAL"));
        breachesByPriority.put("HIGH", ticketRepository.countByPriorityAndSlaBreachedTrue("HIGH"));
        breachesByPriority.put("MEDIUM", ticketRepository.countByPriorityAndSlaBreachedTrue("MEDIUM"));
        breachesByPriority.put("LOW", ticketRepository.countByPriorityAndSlaBreachedTrue("LOW"));
        stats.put("breachesByPriority", breachesByPriority);

        return stats;
    }

    /**
     * Inner class representing escalation result.
     */
    public static class EscalationResult {
        private final Ticket ticket;
        private final boolean escalated;
        private final String message;

        public EscalationResult(Ticket ticket, boolean escalated, String message) {
            this.ticket = ticket;
            this.escalated = escalated;
            this.message = message;
        }

        public Ticket getTicket() {
            return ticket;
        }

        public boolean isEscalated() {
            return escalated;
        }

        public String getMessage() {
            return message;
        }
    }
}
