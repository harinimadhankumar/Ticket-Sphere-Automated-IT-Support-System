package com.powergrid.ticketsystem.controller;

import com.powergrid.ticketsystem.entity.Ticket;
import com.powergrid.ticketsystem.repository.TicketRepository;
import com.powergrid.ticketsystem.verification.AIVerificationService;
import com.powergrid.ticketsystem.verification.VerificationScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ============================================================
 * VERIFICATION CONTROLLER
 * ============================================================
 * 
 * PHASE 7: AI-BASED RESOLUTION VERIFICATION & CLOSURE
 * 
 * REST API endpoints for verification operations.
 * 
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║ AVAILABLE ENDPOINTS ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║ GET /api/verification/stats - Get verification stats ║
 * ║ POST /api/verification/process-all - Process all pending ║
 * ║ POST /api/verification/ticket/{id} - Verify specific ticket ║
 * ║ GET /api/verification/history - Get verification hist ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@RestController
@RequestMapping("/api/verification")
@CrossOrigin(origins = "*")
public class VerificationController {

    private static final Logger logger = LoggerFactory.getLogger(VerificationController.class);

    private final AIVerificationService aiVerificationService;
    private final VerificationScheduler verificationScheduler;
    private final TicketRepository ticketRepository;

    public VerificationController(AIVerificationService aiVerificationService,
            VerificationScheduler verificationScheduler,
            TicketRepository ticketRepository) {
        this.aiVerificationService = aiVerificationService;
        this.verificationScheduler = verificationScheduler;
        this.ticketRepository = ticketRepository;
        logger.info("Verification Controller initialized");
    }

    /**
     * Get verification statistics.
     * 
     * GET /api/verification/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        VerificationScheduler.VerificationStats stats = verificationScheduler.getStats();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("totalTickets", stats.getTotalTickets());
        response.put("resolvedTickets", stats.getResolvedTickets());
        response.put("closedTickets", stats.getClosedTickets());
        response.put("verified", stats.getVerifiedCount());
        response.put("rejected", stats.getRejectedCount());
        response.put("pending", stats.getPendingCount());

        // Calculate verification rate
        long total = stats.getVerifiedCount() + stats.getRejectedCount();
        double successRate = total > 0 ? (stats.getVerifiedCount() * 100.0 / total) : 0;
        response.put("successRate", String.format("%.1f%%", successRate));

        return ResponseEntity.ok(response);
    }

    /**
     * Process all pending verifications manually.
     * 
     * POST /api/verification/process-all
     */
    @PostMapping("/process-all")
    public ResponseEntity<?> processAll() {
        logger.info("Manual verification trigger: Processing all pending tickets");

        List<AIVerificationService.VerificationResult> results = aiVerificationService.verifyAllResolvedTickets();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("processed", results.size());
        response.put("closed", results.stream().filter(r -> "CLOSED".equals(r.getOutcome())).count());
        response.put("reopened", results.stream().filter(r -> "REOPENED".equals(r.getOutcome())).count());

        // Include details of each verification
        List<Map<String, Object>> details = new ArrayList<>();
        for (AIVerificationService.VerificationResult result : results) {
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("ticketId", result.getTicket().getTicketId());
            detail.put("outcome", result.getOutcome());
            detail.put("score", result.getScore());
            detail.put("passed", result.isPassed());
            if (!result.getIssues().isEmpty()) {
                detail.put("issues", result.getIssues());
            }
            details.add(detail);
        }
        response.put("details", details);

        return ResponseEntity.ok(response);
    }

    /**
     * Verify a specific ticket.
     * 
     * POST /api/verification/ticket/{ticketId}
     */
    @PostMapping("/ticket/{ticketId}")
    public ResponseEntity<?> verifyTicket(@PathVariable String ticketId) {
        logger.info("Manual verification for ticket: {}", ticketId);

        Optional<Ticket> ticketOpt = ticketRepository.findByTicketId(ticketId);
        if (ticketOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ticket not found: " + ticketId));
        }

        Ticket ticket = ticketOpt.get();

        // Check if ticket is in a state that can be verified
        String status = ticket.getStatus();
        if (!"RESOLVED".equalsIgnoreCase(status)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ticket is not in RESOLVED status. Current status: " + status));
        }

        AIVerificationService.VerificationResult result = aiVerificationService.verifyAndProcess(ticket);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("ticketId", ticketId);
        response.put("passed", result.isPassed());
        response.put("outcome", result.getOutcome());
        response.put("score", result.getScore());
        response.put("message", result.getMessage());

        if (!result.getIssues().isEmpty()) {
            response.put("issues", result.getIssues());
        }
        if (!result.getWarnings().isEmpty()) {
            response.put("warnings", result.getWarnings());
        }

        // Include updated ticket
        Ticket updatedTicket = ticketRepository.findByTicketId(ticketId).orElse(ticket);
        Map<String, Object> ticketInfo = new LinkedHashMap<>();
        ticketInfo.put("status", updatedTicket.getStatus());
        ticketInfo.put("verificationStatus", updatedTicket.getVerificationStatus());
        ticketInfo.put("verificationScore", updatedTicket.getVerificationScore());
        ticketInfo.put("closedBy", updatedTicket.getClosedBy());
        response.put("ticket", ticketInfo);

        return ResponseEntity.ok(response);
    }

    /**
     * Get verification history for all tickets.
     * 
     * GET /api/verification/history
     */
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(
            @RequestParam(required = false, defaultValue = "50") int limit) {

        List<Ticket> allTickets = ticketRepository.findAll();

        // Filter to only verified tickets and sort by verification time
        List<Map<String, Object>> history = allTickets.stream()
                .filter(t -> t.getVerificationStatus() != null)
                .sorted((a, b) -> {
                    LocalDateTime timeA = a.getVerifiedTime();
                    LocalDateTime timeB = b.getVerifiedTime();
                    if (timeA == null)
                        return 1;
                    if (timeB == null)
                        return -1;
                    return timeB.compareTo(timeA); // Descending order
                })
                .limit(limit)
                .map(ticket -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("ticketId", ticket.getTicketId());
                    item.put("verificationStatus", ticket.getVerificationStatus());
                    item.put("verificationScore", ticket.getVerificationScore());
                    item.put("verifiedTime", ticket.getVerifiedTime());
                    item.put("currentStatus", ticket.getStatus());
                    item.put("closedBy", ticket.getClosedBy());
                    item.put("category", ticket.getCategory());
                    item.put("priority", ticket.getPriority());
                    item.put("attempts", ticket.getVerificationAttempts());
                    return item;
                })
                .toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("count", history.size());
        response.put("history", history);

        return ResponseEntity.ok(response);
    }

    /**
     * Get AI verification configuration.
     * 
     * GET /api/verification/config
     */
    @GetMapping("/config")
    public ResponseEntity<?> getConfig() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("phase", "Phase 7: AI-Based Resolution Verification & Closure");
        response.put("description", "Automatic verification of ticket resolutions before closure");

        Map<String, Object> checks = new LinkedHashMap<>();
        checks.put("resolutionTextExists", "Confirms engineer has provided solution notes (min 20 chars, 5 words)");
        checks.put("categoryMatch", "Ensures resolution keywords match ticket category");
        checks.put("slaCompliance", "Verifies resolution was within SLA deadline");
        checks.put("noEscalation", "Checks no Level 3 escalations are pending");
        response.put("verificationChecks", checks);

        Map<String, String> outcomes = new LinkedHashMap<>();
        outcomes.put("valid", "status = CLOSED, closed_by = AI");
        outcomes.put("invalid", "status = ASSIGNED (reopened), sent back to engineer");
        response.put("outcomes", outcomes);

        Map<String, Object> scoring = new LinkedHashMap<>();
        scoring.put("maxScore", 100);
        scoring.put("resolutionTextPenalty", 30);
        scoring.put("categoryMatchPenalty", 25);
        scoring.put("escalationPenalty", 20);
        scoring.put("slaBreachPenalty", 10);
        scoring.put("warningPenalty", 5);
        response.put("scoring", scoring);

        return ResponseEntity.ok(response);
    }
}
