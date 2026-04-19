package com.powergrid.ticketsystem.scheduler;

import com.powergrid.ticketsystem.entity.Ticket;
import com.powergrid.ticketsystem.repository.TicketRepository;
import com.powergrid.ticketsystem.selfservice.FallbackAssignmentService;
import com.powergrid.ticketsystem.notification.NotificationService;
import com.powergrid.ticketsystem.notification.NotificationEmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ============================================================
 * SELF-SERVICE TIMEOUT SCHEDULER
 * ============================================================
 *
 * Monitors tickets in AWAITING_RESPONSE status and handles
 * KB solution timeouts:
 *
 * 1. After 12 hours: Send reminder email to user
 * 2. After 24 hours: Auto-escalate to engineer if no response
 *
 * Runs every 5 minutes by default (configurable).
 */
@Component
public class SelfServiceTimeoutScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SelfServiceTimeoutScheduler.class);

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FallbackAssignmentService fallbackAssignmentService;

    @Autowired
    private NotificationEmailService notificationEmailService;

    @Value("${selfservice.response.timeout.hours:24}")
    private int timeoutHours;

    @Value("${selfservice.response.reminder.hours:12}")
    private int reminderHours;

    @Value("${selfservice.timeout.enabled:true}")
    private boolean timeoutSchedulerEnabled;

    /**
     * Main scheduled task that checks for KB solution timeouts.
     * Runs every 5 minutes (300000 milliseconds).
     */
    @Scheduled(fixedRateString = "${selfservice.timeout.interval:300000}", initialDelayString = "${selfservice.timeout.initial-delay:30000}")
    public void checkTimeouts() {
        if (!timeoutSchedulerEnabled) {
            logger.debug("Self-service timeout scheduler is disabled");
            return;
        }

        try {
            logger.info("⏰ [TIMEOUT SCHEDULER] Checking for KB solution timeouts...");

            // Get all tickets in AWAITING_RESPONSE status
            List<Ticket> awaitingResponseTickets = ticketRepository
                    .findByStatus("AWAITING_RESPONSE");

            if (awaitingResponseTickets == null || awaitingResponseTickets.isEmpty()) {
                logger.debug("No tickets in AWAITING_RESPONSE status");
                return;
            }

            logger.info("Found {} tickets awaiting user response", awaitingResponseTickets.size());

            LocalDateTime now = LocalDateTime.now();

            for (Ticket ticket : awaitingResponseTickets) {
                LocalDateTime createdTime = ticket.getCreatedTime();
                if (createdTime == null)
                    continue;

                long hoursSinceCreation = java.time.temporal.ChronoUnit.HOURS
                        .between(createdTime, now);

                // Check for timeout (24 hours)
                if (hoursSinceCreation >= timeoutHours) {
                    logger.info("⏳ [TIMEOUT ESCALATION] Ticket {} exceeded {} hour timeout",
                            ticket.getTicketId(), timeoutHours);
                    escalateToEngineer(ticket);
                }
                // Check for reminder (12 hours)
                else if (hoursSinceCreation >= reminderHours
                        && !Boolean.TRUE.equals(ticket.getTimeoutReminderSent())) {
                    logger.info("🔔 [REMINDER] Sending reminder for ticket {} ({} hours elapsed)",
                            ticket.getTicketId(), hoursSinceCreation);
                    sendReminderEmail(ticket);
                }
            }

        } catch (Exception e) {
            logger.error("❌ Error in timeout scheduler: {}", e.getMessage(), e);
            // Don't throw - scheduler should continue despite errors
        }
    }

    /**
     * Send reminder email to user after 12 hours of no response.
     */
    private void sendReminderEmail(Ticket ticket) {
        try {
            // Mark reminder as sent
            ticket.setTimeoutReminderSent(true);
            ticket.setTimeoutReminderTime(LocalDateTime.now());
            ticketRepository.save(ticket);

            String recipientEmail = ticket.getSenderEmail();
            String subject = "🔔 Reminder: Your IT Support Ticket " + ticket.getTicketId() + " - Action Needed";
            String body = generateReminderEmailBody(ticket);

            // Send reminder email to employee
            notificationEmailService.sendEmailAsync(recipientEmail, subject, body);

            logger.info("✅ Reminder email sent for ticket {} to {} after 12 hours",
                    ticket.getTicketId(), recipientEmail);

        } catch (Exception e) {
            logger.error("⚠️ Failed to send reminder email for ticket {}: {}",
                    ticket.getTicketId(), e.getMessage());
        }
    }

    /**
     * Auto-escalate ticket to engineer after 24 hours of no response.
     */
    private void escalateToEngineer(Ticket ticket) {
        try {
            logger.info("🚀 [ESCALATION] Auto-escalating ticket {} to engineer (24-hour timeout)",
                    ticket.getTicketId());

            // Assign to engineer using the ticket ID
            fallbackAssignmentService.assignDueToNoSolution(ticket.getTicketId());

            // Update ticket status and mark as escalated
            ticket.setStatus("ASSIGNED");
            ticket.setEscalatedTime(LocalDateTime.now());
            ticketRepository.save(ticket);

            // Notify engineer
            notificationService.notifyTicketAssigned(ticket);

            logger.info("✅ Ticket {} escalated to engineer: {}",
                    ticket.getTicketId(), ticket.getAssignedEngineer());

        } catch (Exception e) {
            logger.error("❌ Failed to escalate ticket {}: {}",
                    ticket.getTicketId(), e.getMessage(), e);
        }
    }

    /**
     * Generate reminder email body.
     */
    private String generateReminderEmailBody(Ticket ticket) {
        String senderName = extractNameFromEmail(ticket.getSenderEmail());
        return "Dear " + senderName + ",\n\n"
                + "This is a friendly reminder about your IT Support ticket:\n\n"
                + "Ticket ID: " + ticket.getTicketId() + "\n"
                + "Issue: " + (ticket.getIssueDescription() != null ? ticket.getIssueDescription() : "N/A") + "\n"
                + "Category: " + (ticket.getCategory() != null ? ticket.getCategory() : "N/A") + "\n\n"
                + "We sent you a solution suggestion earlier, but we haven't received your response yet.\n\n"
                + "Please reply to this email with:\n"
                + "- YES - If the suggested solution worked\n"
                + "- NO - If the suggested solution did not work\n\n"
                + "Your response helps us improve our support process.\n\n"
                + "If you don't respond within 24 hours from when the ticket was created,\n"
                + "it will be automatically escalated to our technical team for investigation.\n\n"
                + "Best regards,\n"
                + "IT Support System\n"
                + "POWERGRID India";
    }

    /**
     * Extract name from email address (simple heuristic).
     */
    private String extractNameFromEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "User";
        }
        // Extract part before @ and capitalize
        String namePart = email.split("@")[0];
        // Replace dots with spaces and capitalize
        String displayName = namePart.replaceAll("[._-]", " ");
        return displayName.substring(0, 1).toUpperCase() + displayName.substring(1).toLowerCase();
    }
}
