package com.powergrid.ticketsystem.sla;

import com.powergrid.ticketsystem.constants.EscalationLevel;
import com.powergrid.ticketsystem.entity.Ticket;
import com.powergrid.ticketsystem.notification.NotificationEmailService;
import com.powergrid.ticketsystem.notification.HtmlEmailTemplateService;
import com.powergrid.ticketsystem.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * SLA NOTIFICATION SERVICE
 * ============================================================
 *
 * PHASE 5: SLA MONITORING & AUTO ESCALATION
 *
 * This service handles sending notifications for SLA breaches,
 * warnings, and escalations.
 *
 * NOTIFICATION TYPES:
 * ───────────────────
 * 1. SLA Warning - When ticket reaches 75% of SLA time
 * 2. SLA Breach - When SLA time is exceeded
 * 3. Escalation Notice - When ticket is escalated
 * 4. Critical Alert - For CRITICAL priority breaches
 *
 * RECIPIENTS:
 * ───────────
 * - Assigned Engineer: SLA warnings
 * - Team Lead: Level 1 escalations
 * - Department Head: Level 2+ escalations
 * - Management: Critical priority breaches
 */
@Service
public class SlaNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(SlaNotificationService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TicketRepository ticketRepository;
    private final NotificationEmailService notificationEmailService;
    private final HtmlEmailTemplateService htmlTemplateService;

    @Value("${sla.notification.enabled:true}")
    private boolean notificationsEnabled;

    @Value("${sla.notification.admin:harinipriya3108@gmail.com}")
    private String adminEmail;

    @Value("${sla.notification.teamlead:harinim23cse@srishakthi.ac.in}")
    private String teamLeadEmail;

    @Value("${sla.notification.manager:harinim23cse@srishakthi.ac.in}")
    private String managerEmail;

    public SlaNotificationService(TicketRepository ticketRepository,
            NotificationEmailService notificationEmailService,
            HtmlEmailTemplateService htmlTemplateService) {
        this.ticketRepository = ticketRepository;
        this.notificationEmailService = notificationEmailService;
        this.htmlTemplateService = htmlTemplateService;
        logger.info("SLA Notification Service initialized");
    }


    /**
     * Send SLA warning notification.
     * Called when ticket reaches 75% of SLA time.
     * 
     * @param ticket The ticket approaching SLA breach
     */
    @Transactional
    public void sendSlaWarningNotification(Ticket ticket) {
        if (!notificationsEnabled) {
            logger.debug("Notifications disabled, skipping SLA warning for ticket {}", ticket.getTicketId());
            return;
        }

        logger.info("Sending SLA warning for ticket: {}", ticket.getTicketId());

        try {
            String subject = String.format("[⏰ SLA WARNING] Ticket %s approaching deadline", ticket.getTicketId());
            String body = "Ticket: " + ticket.getTicketId() + "\n" +
                         "Status: " + ticket.getStatus() + "\n" +
                         "SLA Deadline: " + (ticket.getSlaDeadline() != null ? ticket.getSlaDeadline() : "N/A") + "\n\n" +
                         "This ticket is approaching its SLA deadline. Please take immediate action.";

            // Send to assigned engineer
            notificationEmailService.sendEmailAsync(getEngineerEmail(ticket.getAssignedEngineer()), subject, body);

            // Mark warning as sent
            ticket.setSlaWarningSent(true);
            ticket.setLastSlaCheck(LocalDateTime.now());
            ticketRepository.save(ticket);

            logger.info("✓ SLA warning sent successfully for ticket: {}", ticket.getTicketId());

        } catch (Exception e) {
            logger.error("Failed to send SLA warning for ticket {}: {}", ticket.getTicketId(), e.getMessage());
        }
    }

    /**
     * Send SLA breach notification.
     * Called when SLA time is exceeded.
     * 
     * @param ticket The ticket with breached SLA
     */
    public void sendSlaBreachNotification(Ticket ticket) {
        if (!notificationsEnabled) {
            logger.debug("Notifications disabled, skipping SLA breach notification for ticket {}",
                    ticket.getTicketId());
            return;
        }

        logger.info("Sending SLA breach notification for ticket: {}", ticket.getTicketId());

        try {
            String subject = String.format("[🚨 SLA BREACH] Ticket %s has exceeded SLA", ticket.getTicketId());
            String htmlContent = htmlTemplateService.generateTicketEscalatedEmail(ticket, "Team Lead");

            // Send to assigned engineer
            notificationEmailService.sendHtmlEmailAsync(getEngineerEmail(ticket.getAssignedEngineer()), subject, htmlContent);

            // Send to team lead
            notificationEmailService.sendHtmlEmailAsync(teamLeadEmail, subject, htmlContent);

            // For CRITICAL tickets, notify management
            if ("CRITICAL".equalsIgnoreCase(ticket.getPriority())) {
                notificationEmailService.sendHtmlEmailAsync(managerEmail, "[🔴 CRITICAL] " + subject, htmlContent);
            }

            logger.info("✓ SLA breach notification sent for ticket: {}", ticket.getTicketId());

        } catch (Exception e) {
            logger.error("Failed to send SLA breach notification for ticket {}: {}", ticket.getTicketId(),
                    e.getMessage());
        }
    }

    /**
     * Send escalation notification.
     * Called when ticket is escalated to a higher level.
     * 
     * @param ticket The escalated ticket
     * @param level  Escalation level
     */
    public void sendEscalationNotification(Ticket ticket, EscalationLevel level) {
        if (!notificationsEnabled) {
            logger.debug("Notifications disabled, skipping escalation notification for ticket {}",
                    ticket.getTicketId());
            return;
        }

        logger.info("Sending escalation notification for ticket: {}, level: {}", ticket.getTicketId(), level);

        try {
            String subject = String.format("[🔺 ESCALATION - %s] Ticket %s requires attention",
                    level.getDisplayName(), ticket.getTicketId());
            String htmlContent = htmlTemplateService.generateTicketEscalatedEmail(ticket, level.getDisplayName());

            // Get recipients based on escalation level
            List<String> recipients = getEscalationRecipients(ticket, level);

            for (String recipient : recipients) {
                notificationEmailService.sendHtmlEmailAsync(recipient, subject, htmlContent);
            }

            logger.info("✓ Escalation notification sent for ticket: {}", ticket.getTicketId());

        } catch (Exception e) {
            logger.error("Failed to send escalation notification for ticket {}: {}", ticket.getTicketId(),
                    e.getMessage());
        }
    }

    /**
     * Get recipients for escalation notification.
     * 
     * @param ticket The ticket
     * @param level  Escalation level
     * @return List of email addresses
     */
    private List<String> getEscalationRecipients(Ticket ticket, EscalationLevel level) {
        List<String> recipients = new ArrayList<>();

        // Always notify assigned engineer and previous engineer
        recipients.add(getEngineerEmail(ticket.getAssignedEngineer()));
        if (ticket.getPreviousEngineer() != null) {
            recipients.add(getEngineerEmail(ticket.getPreviousEngineer()));
        }

        // Add escalation contacts based on level
        switch (level) {
            case LEVEL_1:
                recipients.add(teamLeadEmail);
                break;
            case LEVEL_2:
                recipients.add(teamLeadEmail);
                recipients.add(managerEmail);
                break;
            case LEVEL_3:
                recipients.add(teamLeadEmail);
                recipients.add(managerEmail);
                recipients.add(adminEmail);
                break;
            default:
                break;
        }

        return recipients;
    }

    /**
     * Send HTML email using NotificationEmailService.
     *
     * @param to      Recipient email
     * @param subject Email subject
     * @param htmlBody HTML email body
     */
    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            notificationEmailService.sendHtmlEmail(to, subject, htmlBody);
            logger.debug("HTML email sent to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Get email address for engineer.
     * In real implementation, this would query user database.
     * 
     * @param engineerName Engineer name
     * @return Email address
     */
    private String getEngineerEmail(String engineerName) {
        // In production, query user database for email
        // For now, generate based on name
        if (engineerName == null) {
            return adminEmail;
        }
        // Use test email for all engineers during testing
        return "harinim23cse@srishakthi.ac.in";
    }

    /**
     * Send daily SLA summary to management.
     *
     * @param stats SLA statistics
     */
    public void sendDailySummary(java.util.Map<String, Object> stats) {
        if (!notificationsEnabled) {
            return;
        }

        try {
            String subject = "[📊 DAILY SLA SUMMARY] IT Service Desk Report";
            String body = buildDailySummaryBody(stats);
            // Wrap plain text in simple HTML for consistency
            String htmlContent = "<pre style=\"background: #f5f5f5; padding: 20px; font-family: monospace;\">" +
                                 body.replace("<", "&lt;").replace(">", "&gt;") +
                                 "</pre>";

            sendHtmlEmail(managerEmail, subject, htmlContent);
            sendHtmlEmail(adminEmail, subject, htmlContent);

            logger.info("✓ Daily SLA summary sent");

        } catch (Exception e) {
            logger.error("Failed to send daily summary: {}", e.getMessage());
        }
    }

    /**
     * Build daily summary email body.
     */
    private String buildDailySummaryBody(java.util.Map<String, Object> stats) {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════════════════════════════╗\n");
        sb.append("║              DAILY SLA SUMMARY REPORT                        ║\n");
        sb.append("╚══════════════════════════════════════════════════════════════╝\n\n");

        sb.append(String.format("Report Date: %s\n\n", LocalDateTime.now().format(DATE_FORMATTER)));

        sb.append("SLA BREACH STATISTICS:\n");
        sb.append("───────────────────────\n");
        sb.append(String.format("Total SLA Breaches: %s\n", stats.get("totalSlaBreaches")));
        sb.append(String.format("Level 1 Escalations: %s\n", stats.get("level1Escalations")));
        sb.append(String.format("Level 2 Escalations: %s\n", stats.get("level2Escalations")));
        sb.append(String.format("Level 3 Escalations: %s\n", stats.get("level3Escalations")));
        sb.append("\n");

        @SuppressWarnings("unchecked")
        java.util.Map<String, Long> breachesByPriority = (java.util.Map<String, Long>) stats.get("breachesByPriority");

        if (breachesByPriority != null) {
            sb.append("BREACHES BY PRIORITY:\n");
            sb.append("──────────────────────\n");
            breachesByPriority.forEach((priority, count) -> sb.append(String.format("  %s: %d\n", priority, count)));
        }

        sb.append("\n---\n");
        sb.append("IT Service Desk - Automated SLA Monitoring System\n");

        return sb.toString();
    }
}
