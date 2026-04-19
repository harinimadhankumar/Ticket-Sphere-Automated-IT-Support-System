package com.powergrid.ticketsystem.notification;

import com.powergrid.ticketsystem.entity.Engineer;
import com.powergrid.ticketsystem.entity.Ticket;
import com.powergrid.ticketsystem.entity.TeamLead;
import com.powergrid.ticketsystem.repository.EngineerRepository;
import com.powergrid.ticketsystem.repository.TeamLeadRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * ============================================================
 * NOTIFICATION SERVICE - Core Orchestrator
 * ============================================================
 * 
 * PHASE 8: NOTIFICATIONS & ALERTS
 * 
 * Central notification service that orchestrates all ticket
 * lifecycle email notifications. This service acts as the
 * single entry point for triggering notifications from
 * other services (TicketService, ResolutionService, etc.)
 * 
 * RESPONSIBILITIES:
 * 1. Receive notification requests from other services
 * 2. Determine appropriate recipients based on event type
 * 3. Generate email content using templates
 * 4. Delegate to NotificationEmailService for delivery
 * 5. Log all notification activities for audit
 * 
 * INTEGRATION POINTS:
 * - TicketService → Ticket Created/Assigned
 * - ResolutionService → Ticket Resolved/Reopened
 * - SlaMonitoringScheduler → SLA Warning/Escalation
 * - AIVerificationService → Ticket Closed/Reopened
 * 
 * DESIGN PRINCIPLES:
 * - Single Responsibility: Only handles notifications
 * - Event-driven: Methods triggered on state changes
 * - Non-blocking: Uses async email delivery
 * - Fail-safe: Notifications don't break main flow
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationEmailService emailService;
    private final HtmlEmailTemplateService htmlTemplateService;
    private final EngineerRepository engineerRepository;
    private final TeamLeadRepository teamLeadRepository;

    // Escalation chain email addresses
    @Value("${notification.escalation.level1:harinim23cse@srishakthi.ac.in}")
    private String level1Email; // Senior Engineer / Team Lead

    @Value("${notification.escalation.level2:harinim23cse@srishakthi.ac.in}")
    private String level2Email; // Manager

    @Value("${notification.escalation.level3:harinipriya3108@gmail.com}")
    private String level3Email; // Director / Department Head

    @Value("${notification.engineer.email:harinipriya3108@gmail.com}")
    private String engineerNotificationEmail; // Shared inbox for all engineer assignments

    @Value("${notification.enabled:true}")
    private boolean notificationsEnabled;

    @Value("${notification.log-only:false}")
    private boolean logOnlyMode; // For testing - log but don't send

    public NotificationService(NotificationEmailService emailService,
            HtmlEmailTemplateService htmlTemplateService,
            EngineerRepository engineerRepository,
            TeamLeadRepository teamLeadRepository) {
        this.emailService = emailService;
        this.htmlTemplateService = htmlTemplateService;
        this.engineerRepository = engineerRepository;
        this.teamLeadRepository = teamLeadRepository;
    }

    @PostConstruct
    public void init() {
        logger.info("╔══════════════════════════════════════════════════════════╗");
        logger.info("║   NOTIFICATION SERVICE INITIALIZED                       ║");
        logger.info("║   Status: {}                                   ║",
                notificationsEnabled ? "ENABLED " : "DISABLED");
        logger.info("║   Mode: {}                                     ║",
                logOnlyMode ? "LOG ONLY" : "LIVE    ");
        logger.info("╚══════════════════════════════════════════════════════════╝");
    }

    // ============================================================
    // EVENT 1: TICKET CREATED
    // ============================================================

    /**
     * Send notification when a new ticket is created.
     * Triggered by: TicketService (Phase 1)
     * Recipient: Employee (ticket owner)
     * 
     * @param ticket The newly created ticket
     */
    public void notifyTicketCreated(Ticket ticket) {
        if (!shouldSendNotification())
            return;

        logger.info("📧 [NOTIFICATION] TICKET_CREATED - {}", ticket.getTicketId());

        String recipientEmail = ticket.getSenderEmail();
        if (recipientEmail == null || recipientEmail.isEmpty()) {
            logger.warn("Cannot send TICKET_CREATED notification - no sender email for ticket {}",
                    ticket.getTicketId());
            return;
        }

        String subject = "IT Support Ticket Created: " + ticket.getTicketId();
        String htmlBody = htmlTemplateService.generateNewTicketEmail(ticket);

        emailService.sendHtmlEmailAsync(recipientEmail, subject, htmlBody);
        logger.info("✅ HTML email queued for: {}", recipientEmail);
    }

    // ============================================================
    // EVENT 2: TICKET ASSIGNED
    // ============================================================

    /**
     * Send notification when a ticket is assigned to an engineer.
     * Triggered by: TeamAssignmentService (Phase 3)
     * Recipient: Assigned Engineer (shared inbox)
     *
     * @param ticket The assigned ticket
     */
    public void notifyTicketAssigned(Ticket ticket) {
        if (!shouldSendNotification())
            return;

        logger.info("📧 [NOTIFICATION] TICKET_ASSIGNED - {} to {}",
                ticket.getTicketId(), ticket.getAssignedEngineer());

        String subject = "New Ticket Assigned: " + ticket.getTicketId() + " [" + ticket.getPriority() + "]";
        String htmlBody = htmlTemplateService.generateTicketAssignedEmail(ticket, ticket.getAssignedEngineer());

        Set<String> recipients = new HashSet<>();

        // Send ONLY to engineerNotificationEmail (shared inbox for all engineers)
        // This ensures all engineer assignments go to: harinipriya3108@gmail.com
        if (engineerNotificationEmail != null && !engineerNotificationEmail.isBlank()) {
            recipients.add(engineerNotificationEmail);
            logger.debug("Engineer notification recipient: {}", engineerNotificationEmail);
        } else {
            logger.warn("Engineer notification email is not configured for ticket {}", ticket.getTicketId());
        }

        if (recipients.isEmpty()) {
            logger.warn("Cannot send TICKET_ASSIGNED notification - no recipients for ticket {}",
                    ticket.getTicketId());
            return;
        }

        for (String recipient : recipients) {
            emailService.sendHtmlEmailAsync(recipient, subject, htmlBody);
            logger.info("✅ HTML email queued for: {}", recipient);
        }
    }

    // ============================================================
    // EVENT 3: TICKET ESCALATED
    // ============================================================

    /**
     * Send notification when a ticket is escalated.
     * Triggered by: EscalationService (Phase 5)
     * Recipients: Based on escalation level
     * - LEVEL_1: Senior Engineer / Team Lead
     * - LEVEL_2: Manager
     * - LEVEL_3: Director / Department Head
     * 
     * @param ticket          The escalated ticket
     * @param escalationLevel The current escalation level
     */
    public void notifyTicketEscalated(Ticket ticket, String escalationLevel) {
        if (!shouldSendNotification())
            return;

        logger.info("🚨 [NOTIFICATION] TICKET_ESCALATED - {} Level: {}",
                ticket.getTicketId(), escalationLevel);

        List<String> recipients = getEscalationRecipients(escalationLevel, ticket);
        if (recipients.isEmpty()) {
            logger.warn("No escalation recipients found for level: {}", escalationLevel);
            return;
        }

        String subject = "🚨 URGENT: Ticket Escalation [" + escalationLevel + "] - " + ticket.getTicketId();
        String htmlBody = htmlTemplateService.generateTicketEscalatedEmail(ticket, escalationLevel);

        for (String recipient : recipients) {
            emailService.sendHtmlEmailAsync(recipient, subject, htmlBody);
            logger.info("✅ HTML email queued for: {}", recipient);
        }
    }

    // ============================================================
    // EVENT 4: TICKET RESOLVED
    // ============================================================

    /**
     * Send notification when a ticket is resolved by an engineer.
     * Triggered by: ResolutionService (Phase 6)
     * Recipient: Employee (ticket owner)
     * 
     * @param ticket The resolved ticket
     */
    public void notifyTicketResolved(Ticket ticket) {
        if (!shouldSendNotification())
            return;

        logger.info("✅ [NOTIFICATION] TICKET_RESOLVED - {}", ticket.getTicketId());

        String recipientEmail = ticket.getSenderEmail();
        if (recipientEmail == null || recipientEmail.isEmpty()) {
            logger.warn("Cannot send TICKET_RESOLVED notification - no sender email for ticket {}",
                    ticket.getTicketId());
            return;
        }

        String subject = "IT Support Ticket Resolved: " + ticket.getTicketId();
        String htmlBody = htmlTemplateService.generateTicketResolvedEmail(ticket);

        emailService.sendHtmlEmailAsync(recipientEmail, subject, htmlBody);
        logger.info("✅ HTML email queued for: {}", recipientEmail);
    }

    // ============================================================
    // EVENT 5: TICKET CLOSED
    // ============================================================

    /**
     * Send notification when a ticket is verified and closed.
     * Triggered by: AIVerificationService (Phase 7)
     * Recipients: Employee + Engineer (optional)
     *
     * @param ticket         The closed ticket
     * @param notifyEngineer Whether to also notify the engineer
     */
    public void notifyTicketClosed(Ticket ticket, boolean notifyEngineer) {
        if (!shouldSendNotification())
            return;

        logger.info("🎉 [NOTIFICATION] TICKET_CLOSED - {}", ticket.getTicketId());

        // Notify Employee with HTML
        String employeeEmail = ticket.getSenderEmail();
        if (employeeEmail != null && !employeeEmail.isEmpty()) {
            String subject = "IT Support Ticket Closed: " + ticket.getTicketId();
            String htmlBody = htmlTemplateService.generateTicketResolvedEmail(ticket);
            emailService.sendHtmlEmailAsync(employeeEmail, subject, htmlBody);
            logger.info("✅ HTML email queued for: {}", employeeEmail);
        }

        // Optionally notify Engineer via shared inbox
        if (notifyEngineer && engineerNotificationEmail != null) {
            String subject = "Ticket Closure Confirmed: " + ticket.getTicketId();
            String body = "Your resolution for ticket " + ticket.getTicketId() +
                    " has been verified and the ticket is now CLOSED.\n\n" +
                    "Verification Score: " + ticket.getVerificationScore() + "/100\n" +
                    "Thank you for your excellent support!";
            emailService.sendEmailAsync(engineerNotificationEmail, subject, body);
            logger.info("✅ Email queued for: {}", engineerNotificationEmail);
        }
    }

    /**
     * Overloaded method - notify only employee by default.
     */
    public void notifyTicketClosed(Ticket ticket) {
        notifyTicketClosed(ticket, false);
    }

    // ============================================================
    // EVENT 6: SLA WARNING
    // ============================================================

    /**
     * Send notification when a ticket approaches SLA deadline.
     * Triggered by: SlaMonitoringScheduler (Phase 5)
     * Recipient: Assigned Engineer (shared inbox)
     *
     * @param ticket The ticket approaching SLA deadline
     */
    public void notifySlaWarning(Ticket ticket) {
        if (!shouldSendNotification())
            return;

        logger.info("⚠️ [NOTIFICATION] SLA_WARNING - {}", ticket.getTicketId());

        if (engineerNotificationEmail == null || engineerNotificationEmail.isBlank()) {
            logger.warn("Cannot send SLA_WARNING notification - engineer email not configured");
            return;
        }

        String subject = "⚠️ SLA Warning: Ticket " + ticket.getTicketId() + " - Action Required";
        String body = "Ticket: " + ticket.getTicketId() + "\n" +
                      "Status: " + ticket.getStatus() + "\n" +
                      "SLA Deadline: " + (ticket.getSlaDeadline() != null ? ticket.getSlaDeadline() : "N/A") + "\n\n" +
                      "This ticket is approaching its SLA deadline. Please take immediate action.";

        emailService.sendEmailAsync(engineerNotificationEmail, subject, body);
        logger.info("✅ SLA Warning email queued for: {}", engineerNotificationEmail);
    }

    // ============================================================
    // EVENT 7: TICKET REOPENED
    // ============================================================

    /**
     * Send notification when a ticket is reopened after failed verification.
     * Triggered by: AIVerificationService (Phase 7)
     * Recipient: Assigned Engineer (shared inbox)
     *
     * @param ticket The reopened ticket
     */
    public void notifyTicketReopened(Ticket ticket) {
        if (!shouldSendNotification())
            return;

        logger.info("🔄 [NOTIFICATION] TICKET_REOPENED - {}", ticket.getTicketId());

        if (engineerNotificationEmail == null || engineerNotificationEmail.isBlank()) {
            logger.warn("Cannot send TICKET_REOPENED notification - engineer email not configured");
            return;
        }

        String subject = "🔄 Ticket Reopened: " + ticket.getTicketId() + " - Verification Failed";
        String body = "Ticket: " + ticket.getTicketId() + "\n" +
                      "Status: " + ticket.getStatus() + "\n\n" +
                      "Your resolution for this ticket did not meet quality standards and has been reopened.\n" +
                      "Please review the verification feedback and resubmit for closure.";

        emailService.sendEmailAsync(engineerNotificationEmail, subject, body);
        logger.info("✅ Ticket Reopened email queued for: {}", engineerNotificationEmail);
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Send notification email with logging.
     */
    @SuppressWarnings("unused")
    private void sendNotificationEmail(String to, String subject, String body,
            NotificationEvent event) {
        if (logOnlyMode) {
            logger.info("📧 [LOG-ONLY] Would send {} email to: {}", event.getEventName(), to);
            logger.debug("Subject: {}", subject);
            return;
        }

        try {
            // Check if body is HTML (starts with <!DOCTYPE or <html)
            boolean isHtml = body != null && (body.trim().startsWith("<!DOCTYPE") || body.trim().startsWith("<html"));

            if (isHtml) {
                // Send as HTML email asynchronously
                emailService.sendHtmlEmailAsync(to, subject, body);
            } else {
                // Send as plain text email asynchronously
                emailService.sendEmailAsync(to, subject, body);
            }

            logger.info("📧 [QUEUED] {} notification to {} ({})", event.getEventName(), to, isHtml ? "HTML" : "TEXT");
        } catch (Exception e) {
            // Log but don't throw - notifications should not break main flow
            logger.error("📧 [ERROR] Failed to send {} notification to {}: {}",
                    event.getEventName(), to, e.getMessage());
        }
    }

    // ============================================================
    // EVENT 8: TICKET ESCALATED (BY ENGINEER)
    // ============================================================

    /**
     * Notify team lead when engineer manually escalates a ticket
     *
     * @param ticket           The escalated ticket
     * @param escalationReason Reason for escalation
     */
    public void notifyTeamLeadOfEscalation(Ticket ticket, String escalationReason) {
        if (!shouldSendNotification())
            return;

        logger.info("🚨 [NOTIFICATION] TEAM_LEAD_ESCALATION - {} to {}",
                ticket.getTicketId(), ticket.getEscalatedToTeamLead());

        String teamLeadName = ticket.getEscalatedToTeamLead();
        String subject = "🚨 Ticket Escalated - " + ticket.getTicketId();

        if (teamLeadName != null && !teamLeadName.isBlank()) {
            String teamLeadEmail = getTeamLeadEmail(teamLeadName);
            if (teamLeadEmail != null) {
                String htmlBody = htmlTemplateService.generateTicketEscalatedEmail(ticket, teamLeadName);
                emailService.sendHtmlEmailAsync(teamLeadEmail, subject, htmlBody);
                logger.info("✅ HTML escalation email queued for: {}", teamLeadEmail);
                return;
            }

            logger.warn("Team lead email not found for '{}'. Falling back to admin escalation recipients.",
                    teamLeadName);
        }

        Set<String> adminRecipients = new HashSet<>();
        if (level2Email != null && !level2Email.isBlank()) {
            adminRecipients.add(level2Email);
        }
        if (level3Email != null && !level3Email.isBlank()) {
            adminRecipients.add(level3Email);
        }

        if (adminRecipients.isEmpty()) {
            logger.warn("Cannot send escalation notification - no admin escalation recipients configured");
            return;
        }

        String adminName = (teamLeadName != null && !teamLeadName.isBlank()) ? teamLeadName : "Admin Team";
        String htmlBody = htmlTemplateService.generateTicketEscalatedEmail(ticket, adminName);

        for (String recipient : adminRecipients) {
            emailService.sendHtmlEmailAsync(recipient, subject, htmlBody);
            logger.info("✅ HTML escalation email queued for: {}", recipient);
        }
    }

    /**
     * Notify employee when their ticket is escalated
     *
     * @param ticket The escalated ticket
     */
    public void notifyEmployeeOfEscalation(Ticket ticket) {
        if (!shouldSendNotification())
            return;

        logger.info("📧 [NOTIFICATION] EMPLOYEE_ESCALATION - {}", ticket.getTicketId());

        String employeeEmail = ticket.getSenderEmail();
        if (employeeEmail == null || employeeEmail.isEmpty()) {
            logger.warn("Cannot send employee escalation notification - no sender email");
            return;
        }

        String subject = "Your Ticket Has Been Escalated - " + ticket.getTicketId();
        String htmlBody = htmlTemplateService.generateTicketEscalatedEmail(ticket, "Management Team");

        emailService.sendHtmlEmailAsync(employeeEmail, subject, htmlBody);
        logger.info("✅ HTML escalation email queued for: {}", employeeEmail);
    }

    /**
     * Send confirmation to engineer when they successfully escalate a ticket
     *
     * @param ticket       The escalated ticket
     * @param engineerName Name of engineer who escalated
     */
    public void notifyEngineerOfEscalationConfirmation(Ticket ticket, String engineerName) {
        if (!shouldSendNotification())
            return;

        logger.info("✅ [NOTIFICATION] ENGINEER_ESCALATION_CONFIRMATION - {}", ticket.getTicketId());

        String engineerEmail = getEngineerEmail(engineerName);
        if (engineerEmail == null) {
            logger.warn("Cannot send escalation confirmation - engineer email not found");
            return;
        }

        String subject = "Escalation Confirmation - " + ticket.getTicketId();
        String htmlContent = htmlTemplateService.generateTicketEscalatedEmail(ticket, "Team Lead");

        // Send HTML email notification
        emailService.sendHtmlEmailAsync(engineerEmail, subject, htmlContent);
    }

    // ============================================================
    // HELPER METHODS - EMAIL SENDING
    // ============================================================

    /**
     * Get engineer's email from repository.
     */
    private String getEngineerEmail(String engineerName) {
        if (engineerName == null || engineerName.isEmpty()) {
            return null;
        }

        Optional<Engineer> engineer = engineerRepository.findByName(engineerName);
        return engineer.map(Engineer::getEmail).orElse(null);
    }

    /**
     * Get team lead's email from repository.
     */
    private String getTeamLeadEmail(String teamLeadName) {
        if (teamLeadName == null || teamLeadName.isEmpty()) {
            return null;
        }

        Optional<TeamLead> teamLead = teamLeadRepository.findByName(teamLeadName);
        return teamLead.map(TeamLead::getEmail).orElse(null);
    }

    /**
     * Get escalation recipients based on level.
     */
    private List<String> getEscalationRecipients(String escalationLevel, Ticket ticket) {
        List<String> recipients = new ArrayList<>();

        // Always include current engineer (if assigned)
        String engineerEmail = getEngineerEmail(ticket.getAssignedEngineer());
        if (engineerEmail != null) {
            recipients.add(engineerEmail);
        }

        // Add escalation chain based on level
        switch (escalationLevel) {
            case "LEVEL_1":
                recipients.add(level1Email);
                break;
            case "LEVEL_2":
                recipients.add(level1Email);
                recipients.add(level2Email);
                break;
            case "LEVEL_3":
                recipients.add(level1Email);
                recipients.add(level2Email);
                recipients.add(level3Email);
                break;
            default:
                recipients.add(level1Email);
        }

        return recipients;
    }

    /**
     * Check if notifications should be sent.
     */
    private boolean shouldSendNotification() {
        if (!notificationsEnabled) {
            logger.debug("Notifications are disabled");
            return false;
        }
        return true;
    }

    // ============================================================
    // UTILITY METHODS FOR TESTING
    // ============================================================

    /**
     * Check if notification service is enabled.
     */
    public boolean isEnabled() {
        return notificationsEnabled;
    }

    /**
     * Check if in log-only mode.
     */
    public boolean isLogOnlyMode() {
        return logOnlyMode;
    }

    /**
     * Get service status for health checks.
     */
    public String getServiceStatus() {
        if (!notificationsEnabled) {
            return "DISABLED";
        }
        if (logOnlyMode) {
            return "LOG_ONLY";
        }
        return "ACTIVE";
    }
}
