package com.powergrid.ticketsystem.selfservice;

import com.powergrid.ticketsystem.entity.KnowledgeBase;
import com.powergrid.ticketsystem.entity.Ticket;
import com.powergrid.ticketsystem.notification.NotificationEmailService;
import com.powergrid.ticketsystem.notification.HtmlEmailTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * ============================================================
 * SOLUTION DELIVERY SERVICE
 * ============================================================
 * 
 * PHASE 4: SELF-SERVICE RESOLUTION
 * 
 * Responsible for delivering solution steps to users via:
 * - Email (for email-originated tickets)
 * - Chatbot API response (for chatbot-originated tickets)
 * 
 * MESSAGE FORMAT:
 * ─────────────────
 * Subject: [Ticket ID] Solution for Your IT Issue
 * 
 * Dear [Employee],
 * 
 * We detected: [Issue Title]
 * 
 * Please try the following steps:
 * 1. [Step 1]
 * 2. [Step 2]
 * 3. [Step 3]
 * 
 * Did this resolve your issue?
 * Reply YES if resolved.
 * Reply NO if you still need help.
 * 
 * Ticket ID: [Ticket ID]
 * IT Support Team - POWERGRID
 */
@Service
public class SolutionDeliveryService {

    private static final Logger logger = LoggerFactory.getLogger(SolutionDeliveryService.class);

    private final NotificationEmailService notificationEmailService;
    private final HtmlEmailTemplateService htmlTemplateService;

    public SolutionDeliveryService(NotificationEmailService notificationEmailService,
            HtmlEmailTemplateService htmlTemplateService) {
        this.notificationEmailService = notificationEmailService;
        this.htmlTemplateService = htmlTemplateService;
    }

    // ============================================================
    // DELIVERY RESULT
    // ============================================================

    public static class DeliveryResult {
        private boolean success;
        private String channel;
        private String message;
        private String formattedSolution;

        public DeliveryResult(boolean success, String channel, String message) {
            this.success = success;
            this.channel = channel;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getChannel() {
            return channel;
        }

        public String getMessage() {
            return message;
        }

        public String getFormattedSolution() {
            return formattedSolution;
        }

        public void setFormattedSolution(String formattedSolution) {
            this.formattedSolution = formattedSolution;
        }
    }

    // ============================================================
    // MAIN DELIVERY METHOD
    // ============================================================

    /**
     * Deliver solution to user based on ticket source.
     * 
     * @param ticket   The ticket being resolved
     * @param solution The knowledge base solution
     * @return DeliveryResult indicating success/failure
     */
    public DeliveryResult deliverSolution(Ticket ticket, KnowledgeBase solution) {
        logger.info("Delivering solution for ticket: {} via {}",
                ticket.getTicketId(), ticket.getSource());

        String formattedMessage = formatSolutionMessage(ticket, solution);

        // Route based on ticket source
        if ("EMAIL".equalsIgnoreCase(ticket.getSource())) {
            return deliverViaEmail(ticket, solution, formattedMessage);
        } else if ("CHATBOT".equalsIgnoreCase(ticket.getSource())) {
            return deliverViaChatbot(ticket, solution, formattedMessage);
        } else {
            logger.warn("Unknown source: {}. Using email delivery.", ticket.getSource());
            return deliverViaEmail(ticket, solution, formattedMessage);
        }
    }

    // ============================================================
    // EMAIL DELIVERY
    // ============================================================

    /**
     * Send solution via email.
     */
    private DeliveryResult deliverViaEmail(Ticket ticket, KnowledgeBase solution,
            String formattedMessage) {
        String recipientEmail = ticket.getSenderEmail();
        if (recipientEmail == null || recipientEmail.isEmpty()) {
            // Try to construct from employee ID
            recipientEmail = ticket.getEmployeeId() + "@powergrid.in";
            logger.info("No sender email, using constructed: {}", recipientEmail);
        }

        try {
            String subject = "[" + ticket.getTicketId() + "] Solution for Your IT Issue";

            // Generate professional HTML email using template service
            String htmlContent = htmlTemplateService.generateNewTicketEmail(ticket);

            // Send as HTML email
            notificationEmailService.sendHtmlEmailAsync(recipientEmail, subject, htmlContent);
            logger.info("✓ Solution email sent to: {}", recipientEmail);

            DeliveryResult result = new DeliveryResult(true, "EMAIL",
                    "Solution sent to " + recipientEmail);
            result.setFormattedSolution(formattedMessage);
            return result;

        } catch (Exception e) {
            logger.error("Failed to send solution email to: {}", recipientEmail, e);
            DeliveryResult result = new DeliveryResult(false, "EMAIL",
                    "Failed to send email: " + e.getMessage());
            result.setFormattedSolution(formattedMessage);
            return result;
        }
    }

    // ============================================================
    // CHATBOT DELIVERY
    // ============================================================

    /**
     * Prepare solution for chatbot response.
     * The actual response is returned via the chatbot API response.
     */
    private DeliveryResult deliverViaChatbot(Ticket ticket, KnowledgeBase solution,
            String formattedMessage) {
        logger.info("Preparing chatbot response for ticket: {}", ticket.getTicketId());

        // For chatbot, we don't actively push - the response is retrieved via API
        // The formatted message is stored and returned when chatbot polls for updates

        DeliveryResult result = new DeliveryResult(true, "CHATBOT",
                "Solution prepared for chatbot response");
        result.setFormattedSolution(formattedMessage);

        logger.info("Chatbot solution prepared. User can retrieve via API.");
        return result;
    }

    // ============================================================
    // MESSAGE FORMATTING
    // ============================================================

    /**
     * Format the solution message for delivery.
     * Creates a user-friendly message with solution steps.
     */
    public String formatSolutionMessage(Ticket ticket, KnowledgeBase solution) {
        StringBuilder sb = new StringBuilder();

        // Greeting
        sb.append("Dear ").append(ticket.getEmployeeId()).append(",\n\n");

        // Issue detection
        sb.append("We detected: ").append(solution.getIssueTitle()).append("\n\n");

        // Solution steps
        sb.append("Please try the following steps:\n");
        sb.append(formatSteps(solution.getSolutionSteps())).append("\n");

        // Confirmation request
        sb.append("─────────────────────────────────────────\n");
        sb.append("Did this resolve your issue?\n\n");
        sb.append("  ✓ Reply YES if your issue is resolved.\n");
        sb.append("  ✗ Reply NO if you still need help.\n\n");

        // Ticket reference
        sb.append("─────────────────────────────────────────\n");
        sb.append("Ticket ID: ").append(ticket.getTicketId()).append("\n");
        sb.append("Category: ").append(ticket.getCategory()).append("\n");
        sb.append("Issue Type: ").append(ticket.getSubCategory()).append("\n\n");

        // Footer
        sb.append("Thank you,\n");
        sb.append("IT Support Team\n");
        sb.append("POWERGRID India\n");

        return sb.toString();
    }

    /**
     * Format solution steps from JSON or plain text.
     */
    private String formatSteps(String solutionSteps) {
        if (solutionSteps == null || solutionSteps.isEmpty()) {
            return "1. Please contact IT support for assistance.";
        }

        // Check if it's JSON array format
        if (solutionSteps.trim().startsWith("[")) {
            return formatJsonSteps(solutionSteps);
        }

        // Check if it's already numbered
        if (solutionSteps.contains("1.") || solutionSteps.contains("Step 1")) {
            return solutionSteps;
        }

        // Plain text with newlines
        String[] lines = solutionSteps.split("\n");
        StringBuilder formatted = new StringBuilder();
        int stepNum = 1;
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                formatted.append(stepNum++).append(". ").append(line.trim()).append("\n");
            }
        }

        return formatted.toString();
    }

    /**
     * Format JSON array of steps.
     * Input: ["Step 1 text", "Step 2 text", ...]
     */
    private String formatJsonSteps(String jsonSteps) {
        try {
            // Simple JSON array parsing without external library
            String content = jsonSteps.trim();
            content = content.substring(1, content.length() - 1); // Remove [ ]

            // Split by comma, handling quoted strings
            List<String> steps = parseJsonArray(content);

            StringBuilder formatted = new StringBuilder();
            int stepNum = 1;
            for (String step : steps) {
                String cleanStep = step.trim();
                // Remove quotes
                if (cleanStep.startsWith("\"") && cleanStep.endsWith("\"")) {
                    cleanStep = cleanStep.substring(1, cleanStep.length() - 1);
                }
                if (!cleanStep.isEmpty()) {
                    formatted.append(stepNum++).append(". ").append(cleanStep).append("\n");
                }
            }

            return formatted.toString();
        } catch (Exception e) {
            logger.warn("Failed to parse JSON steps, using raw format", e);
            return jsonSteps;
        }
    }

    /**
     * Simple JSON array parser for string arrays.
     */
    private List<String> parseJsonArray(String content) {
        // Handle escaped quotes and split properly
        return Arrays.asList(content.split("\",\\s*\""));
    }

    // ============================================================
    // CHATBOT RESPONSE FORMAT
    // ============================================================

    /**
     * Format solution for chatbot JSON response.
     */
    public ChatbotSolutionResponse formatForChatbot(Ticket ticket, KnowledgeBase solution) {
        ChatbotSolutionResponse response = new ChatbotSolutionResponse();
        response.setTicketId(ticket.getTicketId());
        response.setIssueType(solution.getIssueType());
        response.setIssueTitle(solution.getIssueTitle());
        response.setSolutionSteps(solution.getSolutionSteps());
        response.setAutoClosable(solution.getAutoClosable());
        response.setMessage(formatSolutionMessage(ticket, solution));
        response.setConfirmationRequired(true);
        response.setConfirmationPrompt("Did this resolve your issue? Reply YES or NO.");
        return response;
    }

    /**
     * DTO for chatbot solution response.
     */
    public static class ChatbotSolutionResponse {
        private String ticketId;
        private String issueType;
        private String issueTitle;
        private String solutionSteps;
        private Boolean autoClosable;
        private String message;
        private boolean confirmationRequired;
        private String confirmationPrompt;

        // Getters and Setters
        public String getTicketId() {
            return ticketId;
        }

        public void setTicketId(String ticketId) {
            this.ticketId = ticketId;
        }

        public String getIssueType() {
            return issueType;
        }

        public void setIssueType(String issueType) {
            this.issueType = issueType;
        }

        public String getIssueTitle() {
            return issueTitle;
        }

        public void setIssueTitle(String issueTitle) {
            this.issueTitle = issueTitle;
        }

        public String getSolutionSteps() {
            return solutionSteps;
        }

        public void setSolutionSteps(String solutionSteps) {
            this.solutionSteps = solutionSteps;
        }

        public Boolean getAutoClosable() {
            return autoClosable;
        }

        public void setAutoClosable(Boolean autoClosable) {
            this.autoClosable = autoClosable;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public boolean isConfirmationRequired() {
            return confirmationRequired;
        }

        public void setConfirmationRequired(boolean confirmationRequired) {
            this.confirmationRequired = confirmationRequired;
        }

        public String getConfirmationPrompt() {
            return confirmationPrompt;
        }

        public void setConfirmationPrompt(String confirmationPrompt) {
            this.confirmationPrompt = confirmationPrompt;
        }
    }
}
