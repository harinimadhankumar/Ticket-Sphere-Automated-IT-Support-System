package com.powergrid.ticketsystem.notification;

import com.powergrid.ticketsystem.entity.Ticket;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * HTML Email Template Service
 * Loads HTML templates from classpath and replaces ALL placeholders with actual ticket data
 * Templates are located in: src/main/resources/static/email-previews/
 *
 * IMPORTANT: NO CONTENT IS OMITTED - All HTML styling and content is preserved
 */
@Service
public class HtmlEmailTemplateService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");
    private static final String SUPPORT_EMAIL = "support@powergrid.com";
    private static final String ESCALATION_EMAIL = "escalation@powergrid.com";

    private final ResourceLoader resourceLoader;

    public HtmlEmailTemplateService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Generate new ticket HTML email - Sent to ticket creator
     */
    public String generateNewTicketEmail(Ticket ticket) {
        try {
            String template = loadTemplate("ticket-created.html");
            return replaceNewTicketPlaceholders(template, ticket);
        } catch (Exception e) {
            System.err.println("❌ Error loading ticket-created.html template: " + e.getMessage());
            return fallbackProfessionalEmail(ticket);
        }
    }

    /**
     * Generate ticket assigned HTML email - Sent to assigned engineer
     */
    public String generateTicketAssignedEmail(Ticket ticket, String assignedTo) {
        try {
            String template = loadTemplate("ticket-assigned.html");
            return replaceAssignedTicketPlaceholders(template, ticket, assignedTo);
        } catch (Exception e) {
            System.err.println("❌ Error loading ticket-assigned.html template: " + e.getMessage());
            return fallbackProfessionalEmail(ticket);
        }
    }

    /**
     * Generate ticket escalated HTML email - Sent to escalation team
     */
    public String generateTicketEscalatedEmail(Ticket ticket, String escalatedTo) {
        try {
            String template = loadTemplate("ticket-escalated.html");
            return replaceEscalatedTicketPlaceholders(template, ticket, escalatedTo);
        } catch (Exception e) {
            System.err.println("❌ Error loading ticket-escalated.html template: " + e.getMessage());
            return fallbackProfessionalEmail(ticket);
        }
    }

    /**
     * Generate ticket resolved HTML email - Sent to ticket creator
     */
    public String generateTicketResolvedEmail(Ticket ticket) {
        try {
            String template = loadTemplate("ticket-resolved.html");
            return replaceResolvedTicketPlaceholders(template, ticket);
        } catch (Exception e) {
            System.err.println("❌ Error loading ticket-resolved.html template: " + e.getMessage());
            return fallbackProfessionalEmail(ticket);
        }
    }

    /**
     * Load HTML template from classpath
     */
    private String loadTemplate(String templateName) throws IOException {
        String resourcePath = "classpath:static/email-previews/" + templateName;
        Resource resource = resourceLoader.getResource(resourcePath);

        if (!resource.exists()) {
            throw new IOException("Template not found: " + resourcePath);
        }

        return new String(Files.readAllBytes(resource.getFile().toPath()));
    }

    /**
     * ============================================================
     * TICKET CREATED EMAIL - Replace ALL Placeholders
     * ============================================================
     */
    private String replaceNewTicketPlaceholders(String template, Ticket ticket) {
        String ticketId = ticket.getTicketId() != null ? ticket.getTicketId() : "N/A";
        String createdTime = ticket.getCreatedTime() != null
            ? formatDate(ticket.getCreatedTime())
            : "N/A";
        String priority = ticket.getPriority() != null ? ticket.getPriority().toUpperCase() : "MEDIUM";
        String category = ticket.getCategory() != null ? ticket.getCategory() : "General";
        String status = ticket.getStatus() != null ? ticket.getStatus().toUpperCase() : "OPEN";
        String description = ticket.getIssueDescription() != null
            ? ticket.getIssueDescription()
            : "No description provided";
        String responseTime = getResponseTime(priority);

        // Replace ALL placeholders - Exact matching from HTML template
        template = template.replace("TKT-2026-00547", ticketId);
        template = template.replace("31-Mar-2026 02:30 PM", createdTime);
        template = template.replace(">HIGH<", ">" + priority + "<");
        template = template.replace("Network & Connectivity", category);
        template = template.replace(">OPEN<", ">" + status + "<");
        template = template.replace("4 hours", responseTime);
        template = template.replace(
            "Unable to access company VPN from home. Connection keeps dropping after 5 minutes. Error message shows: \"Authentication timeout\". Tried reconnecting multiple times but issue persists. This is blocking access to...",
            description
        );
        template = template.replace("High", priority);

        return template;
    }

    /**
     * ============================================================
     * TICKET ASSIGNED EMAIL - Replace ALL Placeholders
     * ============================================================
     */
    private String replaceAssignedTicketPlaceholders(String template, Ticket ticket, String assignedTo) {
        String engineerName = assignedTo != null && !assignedTo.isEmpty() ? assignedTo : "Engineer";
        String ticketId = ticket.getTicketId() != null ? ticket.getTicketId() : "N/A";
        String createdTime = ticket.getCreatedTime() != null
            ? formatDate(ticket.getCreatedTime())
            : "N/A";
        String priority = ticket.getPriority() != null ? ticket.getPriority().toUpperCase() : "MEDIUM";
        String category = ticket.getCategory() != null ? ticket.getCategory() : "General";
        String description = ticket.getIssueDescription() != null
            ? ticket.getIssueDescription()
            : "No description provided";

        String slaDeadline = ticket.getSlaDeadline() != null
            ? formatDate(ticket.getSlaDeadline())
            : "N/A";
        String slaRemaining = calculateSLARemaining(ticket.getSlaDeadline());

        // Get requester info - these would come from ticket entity
        String requesterEmail = ticket.getSenderEmail() != null ? ticket.getSenderEmail() : "requester@company.com";
        String employeeId = ticket.getId() != null ? "EMP-2024-" + String.format("%04d", ticket.getId()) : "EMP-2024-0000";
        String subCategory = ""; // Can be added to ticket entity if needed

        // Replace ALL placeholders from HTML template
        template = template.replace("Sneha Patel (APP)", engineerName);
        template = template.replace("TKT-2026-00547", ticketId);
        template = template.replace("31-Mar-2026 02:30 PM", createdTime);
        template = template.replace(">HIGH<", ">" + priority + "<");
        template = template.replace("Network & Connectivity", category);
        template = template.replace("VPN Issues", subCategory);
        template = template.replace("EMP-2024-1158", employeeId);
        template = template.replace("rajesh.kumar@company.com", requesterEmail);
        template = template.replace("01-Apr-2026 06:30 PM", slaDeadline);
        template = template.replace("1d 4h 15m remaining", slaRemaining);
        template = template.replace(
            "Unable to access company VPN from home. Connection keeps dropping after 5 minutes. Error message shows: \"Authentication timeout\". Tried reconnecting multiple times but issue persists. This is blocking access to...",
            description
        );

        return template;
    }

    /**
     * ============================================================
     * TICKET ESCALATED EMAIL - Replace ALL Placeholders
     * ============================================================
     */
    private String replaceEscalatedTicketPlaceholders(String template, Ticket ticket, String escalatedTo) {
        String escalationLevel = escalatedTo != null && !escalatedTo.isEmpty() ? escalatedTo : "Team Lead";
        String ticketId = ticket.getTicketId() != null ? ticket.getTicketId() : "N/A";
        String createdTime = ticket.getCreatedTime() != null
            ? formatDate(ticket.getCreatedTime())
            : "N/A";
        LocalDateTime now = LocalDateTime.now();
        String escalatedTime = formatDate(now);
        String priority = ticket.getPriority() != null ? ticket.getPriority().toUpperCase() : "CRITICAL";
        String category = ticket.getCategory() != null ? ticket.getCategory() : "Critical Systems";
        String status = ticket.getStatus() != null ? ticket.getStatus().toUpperCase() : "IN PROGRESS";

        String escalationReason = ticket.getEscalationReason() != null
            ? ticket.getEscalationReason()
            : "SLA Breach - High Priority Ticket";

        String description = ticket.getIssueDescription() != null
            ? ticket.getIssueDescription()
            : "Database connection timeout affecting multiple services. System experiencing intermittent failures. Original engineer unable to identify root cause within 4 hours. Customer reporting revenue impact.";

        String slaDeadline = ticket.getSlaDeadline() != null
            ? formatDate(ticket.getSlaDeadline())
            : "N/A";

        boolean slaBreached = ticket.getSlaDeadline() != null && now.isAfter(ticket.getSlaDeadline());
        String slaStatus = slaBreached ? "SLA BREACHED" : "ON TRACK";

        // Replace ALL placeholders from HTML template
        template = template.replace("Team Lead", escalationLevel);
        template = template.replace("TKT-2026-00539", ticketId);
        template = template.replace("30-Mar-2026 10:15 AM", createdTime);
        template = template.replace("31-Mar-2026 04:45 PM", escalatedTime);
        template = template.replace(">CRITICAL<", ">" + priority + "<");
        template = template.replace("Critical Systems", category);
        template = template.replace(">IN PROGRESS<", ">" + status + "<");
        template = template.replace("SLA Breach - High Priority Ticket", escalationReason);
        template = template.replace(
            "Database connection timeout affecting multiple services. System experiencing intermittent failures. Original engineer unable to identify root cause within 4 hours. Customer reporting revenue impact.",
            description
        );
        template = template.replace("31-Mar-2026 02:15 PM", slaDeadline);
        template = template.replace("SLA BREACHED", slaStatus);
        template = template.replace("escalation@powergrid.com", ESCALATION_EMAIL);

        return template;
    }

    /**
     * ============================================================
     * TICKET RESOLVED EMAIL - Replace ALL Placeholders
     * ============================================================
     */
    private String replaceResolvedTicketPlaceholders(String template, Ticket ticket) {
        String ticketId = ticket.getTicketId() != null ? ticket.getTicketId() : "N/A";
        String category = ticket.getCategory() != null ? ticket.getCategory() : "General";
        String priority = ticket.getPriority() != null ? ticket.getPriority().toUpperCase() : "MEDIUM";
        String status = "RESOLVED";

        String createdTime = ticket.getCreatedTime() != null
            ? formatDate(ticket.getCreatedTime())
            : "N/A";

        LocalDateTime now = LocalDateTime.now();
        String resolvedTime = formatDate(now);

        String resolutionTime = calculateResolutionTime(ticket.getCreatedTime(), now);

        String originalIssue = ticket.getIssueDescription() != null
            ? ticket.getIssueDescription()
            : "Unable to access company VPN from home. Connection keeps dropping after 5 minutes. Error message shows: \"Authentication timeout\".";

        String resolutionDetails = ticket.getResolutionNotes() != null
            ? ticket.getResolutionNotes()
            : "Updated VPN client to latest version (3.2.5). Reset VPN authentication cache. Verified connection stability with 30-minute sustained session test. Issue fully resolved.";

        String resolvedBy = ticket.getAssignedEngineer() != null
            ? ticket.getAssignedEngineer()
            : "Support Team";

        // Replace ALL placeholders from HTML template
        template = template.replace("TKT-2026-00547", ticketId);
        template = template.replace("Network & Connectivity", category);
        template = template.replace(">HIGH<", ">" + priority + "<");
        template = template.replace(">RESOLVED<", ">" + status + "<");
        template = template.replace(
            "Unable to access company VPN from home. Connection keeps dropping after 5 minutes. Error message shows: \"Authentication timeout\".",
            originalIssue
        );
        template = template.replace("Sneha Patel (APP)", resolvedBy);
        template = template.replace(
            "Updated VPN client to latest version (3.2.5). Reset VPN authentication cache. Verified connection stability with 30-minute sustained session test. Issue fully resolved.",
            resolutionDetails
        );
        template = template.replace("31-Mar-2026 02:30 PM", createdTime);
        template = template.replace("31-Mar-2026 05:45 PM", resolvedTime);
        template = template.replace("3h 15m", resolutionTime);

        return template;
    }

    /**
     * Helper: Format LocalDateTime to "dd-MMM-yyyy hh:mm a" format
     */
    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DATE_FORMAT);
    }

    /**
     * Helper: Get response time based on priority
     */
    private String getResponseTime(String priority) {
        if (priority == null) return "4 hours";

        return switch (priority.toUpperCase()) {
            case "CRITICAL" -> "1 hour";
            case "HIGH" -> "4 hours";
            case "MEDIUM" -> "8 hours";
            case "LOW" -> "24 hours";
            default -> "4 hours";
        };
    }

    /**
     * Helper: Calculate SLA remaining time
     */
    private String calculateSLARemaining(LocalDateTime slaDeadline) {
        if (slaDeadline == null) return "N/A";

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(slaDeadline)) {
            return "SLA Breached";
        }

        long minutes = ChronoUnit.MINUTES.between(now, slaDeadline);
        long hours = minutes / 60;
        long days = hours / 24;
        long remainingHours = hours % 24;
        long remainingMinutes = minutes % 60;

        if (days > 0) {
            return String.format("%dd %dh %dm remaining", days, remainingHours, remainingMinutes);
        } else if (hours > 0) {
            return String.format("%dh %dm remaining", hours, remainingMinutes);
        } else {
            return String.format("%dm remaining", minutes);
        }
    }

    /**
     * Helper: Calculate resolution time
     */
    private String calculateResolutionTime(LocalDateTime created, LocalDateTime resolved) {
        if (created == null || resolved == null) return "N/A";

        long minutes = ChronoUnit.MINUTES.between(created, resolved);
        long hours = minutes / 60;
        long days = hours / 24;
        long remainingHours = hours % 24;
        long remainingMinutes = minutes % 60;

        if (days > 0) {
            return String.format("%dd %dh %dm", days, remainingHours, remainingMinutes);
        } else if (hours > 0) {
            return String.format("%dh %dm", hours, remainingMinutes);
        } else {
            return String.format("%dm", minutes);
        }
    }

    /**
     * Fallback to plain text email if HTML template loading fails
     */
    private String fallbackProfessionalEmail(Ticket ticket) {
        String ticketId = ticket.getTicketId() != null ? ticket.getTicketId() : "N/A";
        String sender = ticket.getSenderEmail() != null ? ticket.getSenderEmail() : "Unknown";
        String subject = ticket.getEmailSubject() != null ? ticket.getEmailSubject() : "No Subject";
        String priority = ticket.getPriority() != null ? ticket.getPriority() : "MEDIUM";

        return "====================================================\n" +
                "TICKET NOTIFICATION - PowerGrid IT Support\n" +
                "====================================================\n\n" +
                "Ticket ID: " + ticketId + "\n" +
                "From: " + sender + "\n" +
                "Subject: " + subject + "\n" +
                "Priority: " + priority + "\n\n" +
                "This is an automated notification. Do not reply.\n" +
                "Contact: " + SUPPORT_EMAIL + "\n" +
                "====================================================\n";
    }
}
