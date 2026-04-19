package com.powergrid.ticketsystem.verification;

import com.powergrid.ticketsystem.entity.Ticket;
import com.powergrid.ticketsystem.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ============================================================
 * AI VERIFICATION SCHEDULER
 * ============================================================
 * 
 * PHASE 7: AI-BASED RESOLUTION VERIFICATION & CLOSURE
 * 
 * This scheduler periodically processes any tickets that are in
 * RESOLVED status but haven't been verified yet.
 * 
 * This serves as a backup mechanism to ensure all resolved tickets
 * get verified, even if the real-time verification failed or was
 * skipped for some reason.
 * 
 * SCHEDULE:
 * - Runs every 5 minutes by default
 * - Processes all RESOLVED tickets without verification status
 * 
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║ VERIFICATION WORKFLOW ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║ ║
 * ║ 1. Find all tickets with status = RESOLVED ║
 * ║ and verification_status is NULL or PENDING ║
 * ║ ║
 * ║ 2. For each ticket, run AI verification ║
 * ║ ║
 * ║ 3. Update ticket: ║
 * ║ ├─ If VALID → status = CLOSED, closed_by = AI ║
 * ║ └─ If INVALID → status = ASSIGNED, reopened ║
 * ║ ║
 * ║ 4. Log results for monitoring ║
 * ║ ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Component
public class VerificationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(VerificationScheduler.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AIVerificationService aiVerificationService;
    private final TicketRepository ticketRepository;

    @Value("${verification.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    public VerificationScheduler(AIVerificationService aiVerificationService,
            TicketRepository ticketRepository) {
        this.aiVerificationService = aiVerificationService;
        this.ticketRepository = ticketRepository;

        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("  AI Verification Scheduler Initialized");
        logger.info("  Interval: Every 5 minutes");
        logger.info("  Status: {}", schedulerEnabled ? "ENABLED" : "DISABLED");
        logger.info("═══════════════════════════════════════════════════════════");
    }

    /**
     * Scheduled job to verify resolved tickets.
     * Runs every 5 minutes.
     */
    @Scheduled(fixedRateString = "${verification.scheduler.interval:300000}", initialDelayString = "${verification.scheduler.initial-delay:120000}")
    public void processResolvedTickets() {
        if (!schedulerEnabled) {
            logger.debug("Verification scheduler is disabled");
            return;
        }

        logger.info("════════════════════════════════════════════════════════════");
        logger.info("  AI VERIFICATION SCHEDULER - Starting at {}",
                LocalDateTime.now().format(formatter));
        logger.info("════════════════════════════════════════════════════════════");

        try {
            // Find all RESOLVED tickets that haven't been verified
            List<Ticket> unreviewedTickets = ticketRepository.findByStatus("RESOLVED");

            // Filter to only those without verification status
            List<Ticket> pendingVerification = unreviewedTickets.stream()
                    .filter(t -> t.getVerificationStatus() == null ||
                            "PENDING".equals(t.getVerificationStatus()))
                    .toList();

            if (pendingVerification.isEmpty()) {
                logger.info("  No tickets pending verification");
                return;
            }

            logger.info("  Found {} tickets pending verification", pendingVerification.size());

            int closed = 0;
            int reopened = 0;
            int errors = 0;

            for (Ticket ticket : pendingVerification) {
                try {
                    logger.info("  Processing: {} (Category: {}, Priority: {})",
                            ticket.getTicketId(), ticket.getCategory(), ticket.getPriority());

                    AIVerificationService.VerificationResult result = aiVerificationService.verifyAndProcess(ticket);

                    if (result.isPassed()) {
                        closed++;
                        logger.info("    ✓ CLOSED - Score: {}/100", result.getScore());
                    } else {
                        reopened++;
                        logger.warn("    ✗ REOPENED - Issues: {}", result.getIssues().size());
                    }
                } catch (Exception e) {
                    errors++;
                    logger.error("    ✗ ERROR verifying ticket {}: {}",
                            ticket.getTicketId(), e.getMessage());
                }
            }

            // Log summary
            logger.info("════════════════════════════════════════════════════════════");
            logger.info("  VERIFICATION SUMMARY");
            logger.info("  Total Processed: {}", pendingVerification.size());
            logger.info("  Closed by AI:    {}", closed);
            logger.info("  Reopened:        {}", reopened);
            logger.info("  Errors:          {}", errors);
            logger.info("════════════════════════════════════════════════════════════");

        } catch (Exception e) {
            logger.error("Error in verification scheduler: {}", e.getMessage(), e);
        }
    }

    /**
     * Get verification statistics.
     * Can be called from a controller to get current stats.
     */
    public VerificationStats getStats() {
        long totalTickets = ticketRepository.count();
        long resolvedTickets = ticketRepository.findByStatus("RESOLVED").size();
        long closedTickets = ticketRepository.findByStatus("CLOSED").size();

        List<Ticket> allTickets = ticketRepository.findAll();
        long verifiedCount = allTickets.stream()
                .filter(t -> "VERIFIED".equals(t.getVerificationStatus()))
                .count();
        long rejectedCount = allTickets.stream()
                .filter(t -> "REJECTED".equals(t.getVerificationStatus()))
                .count();
        long pendingCount = resolvedTickets; // RESOLVED tickets are pending verification

        return new VerificationStats(
                totalTickets,
                resolvedTickets,
                closedTickets,
                verifiedCount,
                rejectedCount,
                pendingCount);
    }

    /**
     * Statistics about verification status.
     */
    public static class VerificationStats {
        private final long totalTickets;
        private final long resolvedTickets;
        private final long closedTickets;
        private final long verifiedCount;
        private final long rejectedCount;
        private final long pendingCount;

        public VerificationStats(long totalTickets, long resolvedTickets, long closedTickets,
                long verifiedCount, long rejectedCount, long pendingCount) {
            this.totalTickets = totalTickets;
            this.resolvedTickets = resolvedTickets;
            this.closedTickets = closedTickets;
            this.verifiedCount = verifiedCount;
            this.rejectedCount = rejectedCount;
            this.pendingCount = pendingCount;
        }

        public long getTotalTickets() {
            return totalTickets;
        }

        public long getResolvedTickets() {
            return resolvedTickets;
        }

        public long getClosedTickets() {
            return closedTickets;
        }

        public long getVerifiedCount() {
            return verifiedCount;
        }

        public long getRejectedCount() {
            return rejectedCount;
        }

        public long getPendingCount() {
            return pendingCount;
        }
    }
}
