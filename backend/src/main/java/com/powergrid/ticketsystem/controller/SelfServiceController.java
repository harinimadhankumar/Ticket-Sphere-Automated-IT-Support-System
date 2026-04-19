package com.powergrid.ticketsystem.controller;

import com.powergrid.ticketsystem.entity.Ticket;
import com.powergrid.ticketsystem.repository.TicketRepository;
import com.powergrid.ticketsystem.selfservice.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ============================================================
 * SELF-SERVICE CONTROLLER
 * ============================================================
 * 
 * PHASE 4: SELF-SERVICE RESOLUTION
 * 
 * REST API endpoints for self-service resolution operations.
 * 
 * ENDPOINTS:
 * ──────────
 * 
 * Self-Service Processing:
 * POST /api/self-service/process/{ticketId} - Process ticket for self-service
 * POST /api/self-service/process-all - Process all pending tickets
 * GET /api/self-service/check/{ticketId} - Check eligibility without processing
 * 
 * User Response Handling:
 * POST /api/self-service/response - Handle user YES/NO response
 * POST /api/self-service/response/chatbot - Handle chatbot response
 * POST /api/self-service/response/email - Handle email response
 * 
 * Ticket Management:
 * POST /api/self-service/close/{ticketId} - Manually close ticket
 * POST /api/self-service/escalate/{ticketId} - Manually escalate ticket
 * POST /api/self-service/reopen/{ticketId} - Reopen closed ticket
 * 
 * Statistics:
 * GET /api/self-service/stats - Get self-service statistics
 * GET /api/self-service/config - Get configuration
 */
@RestController
@RequestMapping("/api/self-service")
public class SelfServiceController {

    private static final Logger logger = LoggerFactory.getLogger(SelfServiceController.class);

    private final SelfServiceOrchestrator selfServiceOrchestrator;
    private final SelfServiceEngine selfServiceEngine;
    private final UserResponseHandler userResponseHandler;
    private final TicketClosureService ticketClosureService;
    private final FallbackAssignmentService fallbackAssignmentService;
    private final TicketRepository ticketRepository;

    public SelfServiceController(SelfServiceOrchestrator selfServiceOrchestrator,
            SelfServiceEngine selfServiceEngine,
            UserResponseHandler userResponseHandler,
            TicketClosureService ticketClosureService,
            FallbackAssignmentService fallbackAssignmentService,
            TicketRepository ticketRepository) {
        this.selfServiceOrchestrator = selfServiceOrchestrator;
        this.selfServiceEngine = selfServiceEngine;
        this.userResponseHandler = userResponseHandler;
        this.ticketClosureService = ticketClosureService;
        this.fallbackAssignmentService = fallbackAssignmentService;
        this.ticketRepository = ticketRepository;
    }

    // ============================================================
    // SELF-SERVICE PROCESSING
    // ============================================================

    /**
     * Process a ticket for self-service resolution.
     * 
     * POST /api/self-service/process/{ticketId}
     * 
     * This is the main entry point for triggering self-service on a ticket.
     * It evaluates eligibility, finds solutions, and delivers them to the user.
     */
    @PostMapping("/process/{ticketId}")
    public ResponseEntity<Map<String, Object>> processTicket(@PathVariable String ticketId) {
        logger.info("Processing ticket for self-service: {}", ticketId);

        Map<String, Object> response = new HashMap<>();

        try {
            SelfServiceOrchestrator.OrchestrationResult result = selfServiceOrchestrator.evaluateAndProcess(ticketId);

            response.put("success", true);
            response.put("ticketId", ticketId);
            response.put("selfServiceTriggered", result.isSelfServiceTriggered());
            response.put("solutionDelivered", result.isSolutionDelivered());
            response.put("escalatedToEngineer", result.isEscalatedToEngineer());
            response.put("status", result.getStatus());
            response.put("message", result.getMessage());

            if (result.getTicket() != null) {
                response.put("ticketStatus", result.getTicket().getStatus());
                response.put("resolutionStatus", result.getTicket().getResolutionStatus());
                response.put("assignedEngineer", result.getTicket().getAssignedEngineer());
            }

            if (result.getChatbotResponse() != null) {
                response.put("chatbotResponse", result.getChatbotResponse());
            }

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            logger.error("Error processing ticket for self-service", e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Process all pending tickets for self-service.
     * 
     * POST /api/self-service/process-all
     */
    @PostMapping("/process-all")
    public ResponseEntity<Map<String, Object>> processAllPending() {
        logger.info("Processing all pending tickets for self-service");

        try {
            Map<String, Object> summary = selfServiceOrchestrator.processAllPendingTickets();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Bulk processing completed");
            response.put("summary", summary);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in bulk processing", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Check eligibility for self-service without processing.
     * 
     * GET /api/self-service/check/{ticketId}
     */
    @GetMapping("/check/{ticketId}")
    public ResponseEntity<Map<String, Object>> checkEligibility(@PathVariable String ticketId) {
        logger.info("Checking self-service eligibility for: {}", ticketId);

        Map<String, Object> response = new HashMap<>();

        Optional<Ticket> ticketOpt = ticketRepository.findByTicketId(ticketId);
        if (ticketOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Ticket not found: " + ticketId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        SelfServiceEngine.EligibilityResult eligibility = selfServiceEngine.evaluateEligibility(ticketOpt.get());

        response.put("success", true);
        response.put("ticketId", ticketId);
        response.put("eligible", eligibility.isEligible());
        response.put("reason", eligibility.getReason());
        response.put("autoClosable", eligibility.isAutoClosable());
        response.put("recommendedStatus", eligibility.getRecommendedStatus().name());

        if (eligibility.getSolution() != null) {
            Map<String, Object> solutionInfo = new HashMap<>();
            solutionInfo.put("id", eligibility.getSolution().getId());
            solutionInfo.put("issueType", eligibility.getSolution().getIssueType());
            solutionInfo.put("issueTitle", eligibility.getSolution().getIssueTitle());
            solutionInfo.put("autoClosable", eligibility.getSolution().getAutoClosable());
            response.put("solution", solutionInfo);
        }

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // USER RESPONSE HANDLING
    // ============================================================

    /**
     * Handle user response (YES/NO) to self-service solution.
     * 
     * POST /api/self-service/response
     * 
     * Request Body:
     * {
     * "ticketId": "TKT-1234567890",
     * "response": "YES",
     * "source": "CHATBOT"
     * }
     */
    @PostMapping("/response")
    public ResponseEntity<Map<String, Object>> handleResponse(@RequestBody UserResponseRequest request) {
        logger.info("Handling user response for ticket: {} - Response: {}",
                request.getTicketId(), request.getResponse());

        Map<String, Object> response = new HashMap<>();

        try {
            UserResponseHandler.ResponseResult result = userResponseHandler.processResponse(
                    request.getTicketId(),
                    request.getResponse(),
                    request.getSource());

            response.put("success", result.isProcessed());
            response.put("responseType", result.getResponseType() != null ? result.getResponseType().name() : null);
            response.put("action", result.getAction());
            response.put("message", result.getMessage());

            if (result.getUpdatedTicket() != null) {
                response.put("ticketStatus", result.getUpdatedTicket().getStatus());
                response.put("resolutionStatus", result.getUpdatedTicket().getResolutionStatus());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error handling user response", e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Handle chatbot confirmation response.
     * 
     * POST /api/self-service/response/chatbot
     */
    @PostMapping("/response/chatbot")
    public ResponseEntity<Map<String, Object>> handleChatbotResponse(
            @RequestParam String ticketId,
            @RequestParam String response) {

        UserResponseHandler.ResponseResult result = userResponseHandler.processChatbotResponse(ticketId, response);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", result.isProcessed());
        responseMap.put("action", result.getAction());
        responseMap.put("message", result.getMessage());

        if (result.getUpdatedTicket() != null) {
            responseMap.put("ticketStatus", result.getUpdatedTicket().getStatus());
        }

        return ResponseEntity.ok(responseMap);
    }

    /**
     * Handle email response (for integration with email ingestion).
     * 
     * POST /api/self-service/response/email
     */
    @PostMapping("/response/email")
    public ResponseEntity<Map<String, Object>> handleEmailResponse(
            @RequestParam String senderEmail,
            @RequestParam String subject,
            @RequestParam String body) {

        UserResponseHandler.ResponseResult result = userResponseHandler.processEmailResponse(senderEmail, subject,
                body);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isProcessed());
        response.put("action", result.getAction());
        response.put("message", result.getMessage());

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // TICKET MANAGEMENT
    // ============================================================

    /**
     * Manually close a ticket.
     * 
     * POST /api/self-service/close/{ticketId}
     */
    @PostMapping("/close/{ticketId}")
    public ResponseEntity<Map<String, Object>> closeTicket(
            @PathVariable String ticketId,
            @RequestParam(required = false) String closedBy,
            @RequestParam(required = false) String notes) {

        logger.info("Manually closing ticket: {}", ticketId);

        Map<String, Object> response = new HashMap<>();

        try {
            Ticket closedTicket;
            if (closedBy != null && !closedBy.isEmpty()) {
                closedTicket = ticketClosureService.closeByEngineer(ticketId, closedBy, notes);
            } else {
                closedTicket = ticketClosureService.autoCloseTicket(ticketId);
            }

            response.put("success", true);
            response.put("message", "Ticket closed successfully");
            response.put("ticketId", closedTicket.getTicketId());
            response.put("status", closedTicket.getStatus());
            response.put("closedBy", closedTicket.getClosedBy());
            response.put("closedTime", closedTicket.getClosedTime());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            logger.error("Error closing ticket", e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Manually escalate a ticket to engineer.
     * 
     * POST /api/self-service/escalate/{ticketId}
     */
    @PostMapping("/escalate/{ticketId}")
    public ResponseEntity<Map<String, Object>> escalateTicket(
            @PathVariable String ticketId,
            @RequestParam(defaultValue = "Manual escalation requested") String reason) {

        logger.info("Manually escalating ticket: {} - Reason: {}", ticketId, reason);

        Map<String, Object> response = new HashMap<>();

        try {
            Ticket escalatedTicket = fallbackAssignmentService.escalateToEngineer(ticketId, reason);

            response.put("success", true);
            response.put("message", "Ticket escalated successfully");
            response.put("ticketId", escalatedTicket.getTicketId());
            response.put("status", escalatedTicket.getStatus());
            response.put("assignedTeam", escalatedTicket.getAssignedTeam());
            response.put("assignedEngineer", escalatedTicket.getAssignedEngineer());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            logger.error("Error escalating ticket", e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Reopen a closed ticket.
     * 
     * POST /api/self-service/reopen/{ticketId}
     */
    @PostMapping("/reopen/{ticketId}")
    public ResponseEntity<Map<String, Object>> reopenTicket(
            @PathVariable String ticketId,
            @RequestParam(defaultValue = "Ticket reopened by user request") String reason) {

        logger.info("Reopening ticket: {} - Reason: {}", ticketId, reason);

        Map<String, Object> response = new HashMap<>();

        try {
            Ticket reopenedTicket = ticketClosureService.reopenTicket(ticketId, reason);

            response.put("success", true);
            response.put("message", "Ticket reopened successfully");
            response.put("ticketId", reopenedTicket.getTicketId());
            response.put("status", reopenedTicket.getStatus());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            logger.error("Error reopening ticket", e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ============================================================
    // STATISTICS & CONFIGURATION
    // ============================================================

    /**
     * Get self-service statistics.
     * 
     * GET /api/self-service/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = selfServiceOrchestrator.getStatistics();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", stats);

        return ResponseEntity.ok(response);
    }

    /**
     * Get self-service configuration.
     * 
     * GET /api/self-service/config
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("enabled", selfServiceEngine.isSelfServiceEnabled());
        config.put("confidenceThreshold", selfServiceEngine.getConfidenceThreshold());
        config.put("skipCriticalPriority", selfServiceEngine.isSkipCriticalPriority());
        config.put("skipHighPriority", selfServiceEngine.isSkipHighPriority());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("config", config);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // REQUEST DTOs
    // ============================================================

    public static class UserResponseRequest {
        private String ticketId;
        private String response;
        private String source;

        public String getTicketId() {
            return ticketId;
        }

        public void setTicketId(String ticketId) {
            this.ticketId = ticketId;
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }
    }
}
