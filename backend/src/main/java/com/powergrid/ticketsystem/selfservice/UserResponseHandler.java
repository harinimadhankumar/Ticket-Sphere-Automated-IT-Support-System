package com.powergrid.ticketsystem.selfservice;

import com.powergrid.ticketsystem.constants.ResolutionStatus;
import com.powergrid.ticketsystem.entity.Ticket;
import com.powergrid.ticketsystem.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * ============================================================
 * USER RESPONSE HANDLER
 * ============================================================
 * 
 * PHASE 4: SELF-SERVICE RESOLUTION
 * 
 * Handles user responses (YES/NO) after solution delivery.
 * Captures responses from:
 * - Email replies (parsed by email ingestion)
 * - Chatbot messages (via API)
 * 
 * RESPONSE FLOW:
 * ─────────────────
 * 
 * User receives solution message:
 * "Did this resolve your issue? Reply YES or NO."
 * 
 * ┌─────────────────────────────────────────────────┐
 * │ User Response: YES │
 * │ ───────────────── │
 * │ → Ticket auto-closed │
 * │ → status = CLOSED │
 * │ → closed_by = SYSTEM │
 * │ → resolution_status = SELF_RESOLVED │
 * │ → Knowledge base success count incremented │
 * └─────────────────────────────────────────────────┘
 * 
 * ┌─────────────────────────────────────────────────┐
 * │ User Response: NO │
 * │ ───────────────── │
 * │ → Ticket escalated to engineer │
 * │ → status = ASSIGNED │
 * │ → resolution_status = ESCALATED │
 * │ → Engineer assigned via load balancing │
 * └─────────────────────────────────────────────────┘
 * 
 * RESPONSE MATCHING:
 * - YES: "yes", "y", "yup", "yeah", "resolved", "fixed", "done", "working"
 * - NO: "no", "n", "nope", "not working", "still", "issue persists", "help"
 */
@Service
public class UserResponseHandler {

    private static final Logger logger = LoggerFactory.getLogger(UserResponseHandler.class);

    private final TicketRepository ticketRepository;
    private final TicketClosureService ticketClosureService;
    private final FallbackAssignmentService fallbackAssignmentService;

    public UserResponseHandler(TicketRepository ticketRepository,
            TicketClosureService ticketClosureService,
            FallbackAssignmentService fallbackAssignmentService) {
        this.ticketRepository = ticketRepository;
        this.ticketClosureService = ticketClosureService;
        this.fallbackAssignmentService = fallbackAssignmentService;
    }

    // ============================================================
    // RESPONSE TYPE ENUM
    // ============================================================

    public enum ResponseType {
        YES, // User confirmed solution worked
        NO, // User said solution didn't work
        UNCLEAR // Response couldn't be understood
    }

    // ============================================================
    // RESPONSE RESULT DTO
    // ============================================================

    public static class ResponseResult {
        private boolean processed;
        private ResponseType responseType;
        private String action;
        private String message;
        private Ticket updatedTicket;

        public ResponseResult(boolean processed, ResponseType responseType,
                String action, String message) {
            this.processed = processed;
            this.responseType = responseType;
            this.action = action;
            this.message = message;
        }

        public boolean isProcessed() {
            return processed;
        }

        public ResponseType getResponseType() {
            return responseType;
        }

        public String getAction() {
            return action;
        }

        public String getMessage() {
            return message;
        }

        public Ticket getUpdatedTicket() {
            return updatedTicket;
        }

        public void setUpdatedTicket(Ticket ticket) {
            this.updatedTicket = ticket;
        }
    }

    // ============================================================
    // MAIN RESPONSE PROCESSING
    // ============================================================

    /**
     * Process user response to self-service solution.
     * Main entry point for both email and chatbot responses.
     * 
     * @param ticketId     The ticket ID
     * @param userResponse The raw user response text
     * @param source       The response source (EMAIL/CHATBOT)
     * @return ResponseResult with action taken
     */
    @Transactional
    public ResponseResult processResponse(String ticketId, String userResponse, String source) {
        logger.info("Processing user response for ticket: {} from {} - Response: {}",
                ticketId, source, userResponse);

        // 1. Validate ticket exists
        Optional<Ticket> ticketOpt = ticketRepository.findByTicketId(ticketId);
        if (ticketOpt.isEmpty()) {
            logger.warn("Ticket not found: {}", ticketId);
            return new ResponseResult(false, null, "ERROR",
                    "Ticket not found: " + ticketId);
        }

        Ticket ticket = ticketOpt.get();

        // 2. Validate ticket is awaiting response
        if (!isAwaitingResponse(ticket)) {
            logger.warn("Ticket {} not awaiting response. Status: {}, Resolution: {}",
                    ticketId, ticket.getStatus(), ticket.getResolutionStatus());
            return new ResponseResult(false, null, "INVALID_STATE",
                    "Ticket is not awaiting a response. Current status: " + ticket.getStatus());
        }

        // 3. Parse user response
        ResponseType responseType = parseResponse(userResponse);
        logger.info("Parsed response type: {} for ticket: {}", responseType, ticketId);

        // 4. Handle based on response type
        return handleResponse(ticket, responseType);
    }

    // ============================================================
    // RESPONSE PARSING
    // ============================================================

    /**
     * Parse user response text to determine YES/NO/UNCLEAR.
     * Uses keyword matching for flexible interpretation.
     */
    public ResponseType parseResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return ResponseType.UNCLEAR;
        }

        // Clean the response: remove email quote markers (>, >>) and extra whitespace
        String cleaned = response
                .replaceAll("(?m)^[>\\s]+", "") // Remove leading > and whitespace on each line
                .replaceAll("[>]+", " ") // Replace remaining > with space
                .replaceAll("\\s+", " ") // Normalize whitespace
                .toLowerCase()
                .trim();

        logger.debug("Cleaned response for parsing: '{}' -> '{}'", response, cleaned);

        String normalized = cleaned;

        // Check for YES patterns
        if (isYesResponse(normalized)) {
            return ResponseType.YES;
        }

        // Check for NO patterns
        if (isNoResponse(normalized)) {
            return ResponseType.NO;
        }

        // Couldn't determine
        return ResponseType.UNCLEAR;
    }

    /**
     * Check if response indicates YES (resolved).
     */
    private boolean isYesResponse(String response) {
        // Exact matches
        String[] exactYes = { "yes", "y", "yep", "yup", "yeah", "ok", "okay",
                "resolved", "fixed", "done", "working", "works",
                "thank you", "thanks", "solved" };

        for (String pattern : exactYes) {
            if (response.equals(pattern) || response.startsWith(pattern + " ") ||
                    response.endsWith(" " + pattern) || response.contains(" " + pattern + " ")) {
                return true;
            }
        }

        // Phrase patterns
        String[] yesPatterns = { "it works", "it's working", "problem solved",
                "issue resolved", "issue fixed", "all good",
                "that worked", "this worked", "it worked",
                "thank you it", "thanks it", "yes thank" };

        for (String pattern : yesPatterns) {
            if (response.contains(pattern)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if response indicates NO (not resolved).
     */
    private boolean isNoResponse(String response) {
        // Exact matches or starts with NO patterns
        String[] exactNo = { "no", "n", "nope", "nah", "negative" };

        for (String pattern : exactNo) {
            // Check exact match OR starts with pattern OR contains pattern as word
            if (response.equals(pattern) || response.startsWith(pattern + " ") ||
                    response.endsWith(" " + pattern) || response.contains(" " + pattern + " ")) {
                return true;
            }
        }

        // Phrase patterns indicating NOT resolved
        String[] noPatterns = { "not working", "still not", "doesn't work",
                "didn't work", "does not work", "did not work",
                "still having", "still facing", "issue persists",
                "problem persists", "same issue", "same problem",
                "not resolved", "not fixed", "help me", "need help",
                "not helpful", "didn't help", "did not help",
                "please help", "escalate", "talk to someone",
                "still broken", "still down" };

        for (String pattern : noPatterns) {
            if (response.contains(pattern)) {
                return true;
            }
        }

        return false;
    }

    // ============================================================
    // RESPONSE HANDLING
    // ============================================================

    /**
     * Handle parsed response and take appropriate action.
     */
    private ResponseResult handleResponse(Ticket ticket, ResponseType responseType) {
        switch (responseType) {
            case YES:
                return handleYesResponse(ticket);
            case NO:
                return handleNoResponse(ticket);
            case UNCLEAR:
            default:
                return handleUnclearResponse(ticket);
        }
    }

    /**
     * Handle YES response - auto-close ticket and send confirmation email.
     */
    private ResponseResult handleYesResponse(Ticket ticket) {
        logger.info("Handling YES response for ticket: {}", ticket.getTicketId());

        try {
            Ticket closedTicket = ticketClosureService.autoCloseTicket(
                    ticket.getTicketId(), ticket.getKnowledgeBaseId());

            // Send confirmation email to user
            ticketClosureService.sendClosureConfirmationEmail(closedTicket);

            ResponseResult result = new ResponseResult(true, ResponseType.YES,
                    "AUTO_CLOSED",
                    "Ticket automatically closed. Thank you for your feedback!");
            result.setUpdatedTicket(closedTicket);
            return result;

        } catch (Exception e) {
            logger.error("Failed to auto-close ticket: {}", ticket.getTicketId(), e);
            return new ResponseResult(false, ResponseType.YES, "ERROR",
                    "Failed to close ticket: " + e.getMessage());
        }
    }

    /**
     * Handle NO response - escalate to engineer and send notification email.
     */
    private ResponseResult handleNoResponse(Ticket ticket) {
        logger.info("Handling NO response for ticket: {}", ticket.getTicketId());

        try {
            Ticket escalatedTicket = fallbackAssignmentService.escalateDueToFailedResolution(
                    ticket.getTicketId());

            // Send escalation notification email to user
            ticketClosureService.sendEscalationEmail(escalatedTicket, escalatedTicket.getAssignedEngineer());

            String message = String.format(
                    "We're sorry the solution didn't work. Your ticket has been assigned to %s. " +
                            "They will contact you shortly.",
                    escalatedTicket.getAssignedEngineer());

            ResponseResult result = new ResponseResult(true, ResponseType.NO,
                    "ESCALATED", message);
            result.setUpdatedTicket(escalatedTicket);
            return result;

        } catch (Exception e) {
            logger.error("Failed to escalate ticket: {}", ticket.getTicketId(), e);
            return new ResponseResult(false, ResponseType.NO, "ERROR",
                    "Failed to escalate ticket: " + e.getMessage());
        }
    }

    /**
     * Handle unclear response - prompt for clarification.
     */
    private ResponseResult handleUnclearResponse(Ticket ticket) {
        logger.info("Unclear response for ticket: {}", ticket.getTicketId());

        String message = "We couldn't understand your response. " +
                "Please reply with 'YES' if your issue is resolved, " +
                "or 'NO' if you still need help.";

        return new ResponseResult(true, ResponseType.UNCLEAR,
                "CLARIFICATION_NEEDED", message);
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Check if ticket is in a state that accepts user responses.
     */
    private boolean isAwaitingResponse(Ticket ticket) {
        String status = ticket.getStatus();
        String resolutionStatus = ticket.getResolutionStatus();

        // Ticket must be OPEN or IN_PROGRESS
        if ("CLOSED".equalsIgnoreCase(status) || "ASSIGNED".equalsIgnoreCase(status)) {
            return false;
        }

        // Resolution status should indicate solution was sent
        if (resolutionStatus == null) {
            // If no resolution status, check if solution was sent
            return ticket.getSolutionSentTime() != null;
        }

        return ResolutionStatus.SOLUTION_SENT.name().equals(resolutionStatus) ||
                ResolutionStatus.AWAITING_CONFIRMATION.name().equals(resolutionStatus);
    }

    /**
     * Process response from email reply.
     * Called by EmailIngestionService when detecting a reply to solution email.
     */
    @Transactional
    public ResponseResult processEmailResponse(String senderEmail, String subject, String body) {
        logger.info("Processing email response from: {}", senderEmail);

        // Extract ticket ID from subject
        // Expected format: "Re: [TKT-XXXXXXXX] Solution for Your IT Issue"
        String ticketId = extractTicketIdFromSubject(subject);
        if (ticketId == null) {
            logger.warn("Could not extract ticket ID from subject: {}", subject);
            return new ResponseResult(false, null, "ERROR",
                    "Could not identify ticket from email subject");
        }

        return processResponse(ticketId, body, "EMAIL");
    }

    /**
     * Extract ticket ID from email subject.
     */
    private String extractTicketIdFromSubject(String subject) {
        if (subject == null)
            return null;

        // Look for pattern [TKT-XXXXXXXX]
        int start = subject.indexOf("[TKT-");
        if (start == -1) {
            // Try without brackets
            start = subject.indexOf("TKT-");
            if (start == -1)
                return null;

            int end = start + 4; // "TKT-"
            while (end < subject.length() && Character.isDigit(subject.charAt(end))) {
                end++;
            }
            return subject.substring(start, end);
        }

        int end = subject.indexOf("]", start);
        if (end == -1)
            return null;

        return subject.substring(start + 1, end); // Remove brackets
    }

    /**
     * Process response from chatbot.
     * Called by ChatbotController when receiving confirmation response.
     */
    @Transactional
    public ResponseResult processChatbotResponse(String ticketId, String response) {
        return processResponse(ticketId, response, "CHATBOT");
    }
}
