package com.powergrid.ticketsystem.controller;

import com.powergrid.ticketsystem.entity.Engineer;
import com.powergrid.ticketsystem.entity.Ticket;
import com.powergrid.ticketsystem.entity.TeamLead;
import com.powergrid.ticketsystem.service.EngineerService;
import com.powergrid.ticketsystem.service.ResolutionService;
import com.powergrid.ticketsystem.repository.TeamLeadRepository;
import com.powergrid.ticketsystem.selfservice.KnowledgeBaseService;
import com.powergrid.ticketsystem.sla.SlaCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ============================================================
 * ENGINEER CONTROLLER
 * ============================================================
 * 
 * PHASE 6: ENGINEER RESOLUTION WORKFLOW
 * 
 * REST API endpoints for engineer operations.
 * 
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║ AVAILABLE ENDPOINTS ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║ POST /api/engineer/login - Engineer login ║
 * ║ POST /api/engineer/logout - Engineer logout ║
 * ║ GET /api/engineer/dashboard - Get dashboard data ║
 * ║ GET /api/engineer/tickets - Get assigned tickets ║
 * ║ GET /api/engineer/ticket/{id} - Get ticket details ║
 * ║ POST /api/engineer/ticket/{id}/resolve - Resolve ticket ║
 * ║ POST /api/engineer/ticket/{id}/start - Start working ║
 * ║ POST /api/engineer/ticket/{id}/notes - Add progress notes ║
 * ║ GET /api/engineer/stats - Get statistics ║
 * ╚═══════════════════════════════════════════════════════════════╝
 * 
 * FRONTEND INTEGRATION:
 * ─────────────────────
 * The frontend (HTML/JS) communicates with these endpoints using:
 * - fetch() API for AJAX calls
 * - JSON request/response bodies
 * - Session token in header for authentication
 * 
 * Example frontend call:
 * ```javascript
 * fetch('/api/engineer/login', {
 * method: 'POST',
 * headers: { 'Content-Type': 'application/json' },
 * body: JSON.stringify({ email: 'john@example.com', password: 'pass' })
 * })
 * .then(res => res.json())
 * .then(data => console.log(data));
 * ```
 */
@RestController
@RequestMapping("/api/engineer")
@CrossOrigin(origins = "*")
public class EngineerController {

    private static final Logger logger = LoggerFactory.getLogger(EngineerController.class);

    private final EngineerService engineerService;
    private final ResolutionService resolutionService;
    private final TeamLeadRepository teamLeadRepository;
    private final KnowledgeBaseService knowledgeBaseService;
    private final SlaCalculationService slaCalculationService;

    public EngineerController(EngineerService engineerService,
            ResolutionService resolutionService,
            TeamLeadRepository teamLeadRepository,
            KnowledgeBaseService knowledgeBaseService,
            SlaCalculationService slaCalculationService) {
        this.engineerService = engineerService;
        this.resolutionService = resolutionService;
        this.teamLeadRepository = teamLeadRepository;
        this.knowledgeBaseService = knowledgeBaseService;
        this.slaCalculationService = slaCalculationService;
        logger.info("Engineer Controller initialized");
    }

    // ============================================================
    // AUTHENTICATION ENDPOINTS
    // ============================================================

    /**
     * Engineer login endpoint.
     * 
     * REQUEST:
     * POST /api/engineer/login
     * Body: { "email": "john@example.com", "password": "password123" }
     * 
     * RESPONSE:
     * {
     * "success": true,
     * "sessionToken": "uuid-token",
     * "engineer": { ... },
     * "message": "Login successful"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        logger.info("Login request for username: {}", username);

        EngineerService.LoginResult result = engineerService.login(username, password);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", result.isSuccess());
        response.put("message", result.getMessage());

        if (result.isSuccess()) {
            response.put("sessionToken", result.getSessionToken());
            response.put("engineer", buildEngineerResponse(result.getEngineer()));
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * Engineer logout endpoint.
     * 
     * REQUEST:
     * POST /api/engineer/logout
     * Header: X-Session-Token: uuid-token
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {
        boolean success = engineerService.logout(sessionToken);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", success);
        response.put("message", success ? "Logout successful" : "Invalid session");

        return ResponseEntity.ok(response);
    }

    /**
     * Validate session endpoint.
     * Frontend uses this to check if session is still valid.
     */
    @GetMapping("/validate-session")
    public ResponseEntity<?> validateSession(
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {

        Optional<Engineer> engineerOpt = engineerService.validateSession(sessionToken);

        Map<String, Object> response = new LinkedHashMap<>();

        if (engineerOpt.isPresent()) {
            response.put("valid", true);
            response.put("engineer", buildEngineerResponse(engineerOpt.get()));
            return ResponseEntity.ok(response);
        } else {
            response.put("valid", false);
            response.put("message", "Invalid or expired session");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // ============================================================
    // DASHBOARD ENDPOINTS
    // ============================================================

    /**
     * Get engineer dashboard data.
     * 
     * Returns:
     * - Engineer info
     * - Assigned tickets
     * - Statistics
     * - SLA alerts
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {

        // Validate session
        Optional<Engineer> engineerOpt = engineerService.validateSession(sessionToken);
        if (engineerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid session. Please login again."));
        }

        Engineer engineer = engineerOpt.get();

        Map<String, Object> dashboard = new LinkedHashMap<>();

        // Engineer info
        dashboard.put("engineer", buildEngineerResponse(engineer));

        // Assigned tickets
        List<Ticket> activeTickets = engineerService.getActiveAssignedTickets(engineer.getName());
        dashboard.put("tickets", buildTicketListResponse(activeTickets));

        // Statistics
        dashboard.put("stats", engineerService.getEngineerStats(engineer.getName()));

        // SLA alerts (tickets nearing breach)
        List<Ticket> slaAlerts = engineerService.getTicketsNearingSla(engineer.getName());
        dashboard.put("slaAlerts", buildTicketListResponse(slaAlerts));

        dashboard.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(dashboard);
    }

    // ============================================================
    // TICKET ENDPOINTS
    // ============================================================

    /**
     * Get all tickets assigned to the engineer.
     * 
     * Optional filters:
     * - priority: CRITICAL, HIGH, MEDIUM, LOW
     * - status: ASSIGNED, IN_PROGRESS, RESOLVED
     */
    @GetMapping("/tickets")
    public ResponseEntity<?> getTickets(
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {

        // Validate session
        Optional<Engineer> engineerOpt = engineerService.validateSession(sessionToken);
        if (engineerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid session"));
        }

        Engineer engineer = engineerOpt.get();
        List<Ticket> tickets;

        if (activeOnly) {
            tickets = engineerService.getActiveAssignedTickets(engineer.getName());
        } else {
            tickets = engineerService.getAssignedTickets(engineer.getName());
        }

        // Apply filters
        if (priority != null && !priority.isEmpty()) {
            tickets = tickets.stream()
                    .filter(t -> priority.equalsIgnoreCase(t.getPriority()))
                    .toList();
        }

        if (status != null && !status.isEmpty()) {
            tickets = tickets.stream()
                    .filter(t -> status.equalsIgnoreCase(t.getStatus()))
                    .toList();
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", tickets.size());
        response.put("tickets", buildTicketListResponse(tickets));
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * Get details of a specific ticket.
     */
    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<?> getTicketDetails(
            @PathVariable String ticketId,
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {

        // Validate session
        Optional<Engineer> engineerOpt = engineerService.validateSession(sessionToken);
        if (engineerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid session"));
        }

        Engineer engineer = engineerOpt.get();

        Optional<Ticket> ticketOpt = resolutionService.getTicketForViewing(ticketId, engineer.getName());

        if (ticketOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Ticket not found or not authorized"));
        }

        return ResponseEntity.ok(buildTicketDetailResponse(ticketOpt.get()));
    }

    /**
     * Resolve a ticket.
     *
     * REQUEST:
     * POST /api/engineer/ticket/{ticketId}/resolve
     * Body: { "resolutionNotes": "Fixed by restarting the VPN service..." }
     *
     * RESPONSE:
     * {
     * "success": true,
     * "message": "Ticket resolved successfully",
     * "ticket": { ... },
     * "verification": {
     * "passed": true/false,
     * "outcome": "CLOSED" or "REOPENED",
     * "score": 85,
     * "issues": [],
     * "warnings": []
     * }
     * }
     * 
     * NOTE: This changes status to RESOLVED, then AI Verification (Phase 7)
     * automatically verifies and either CLOSES or REOPENS the ticket.
     */
    @PostMapping("/ticket/{ticketId}/resolve")
    public ResponseEntity<?> resolveTicket(
            @PathVariable String ticketId,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {

        // Validate session
        Optional<Engineer> engineerOpt = engineerService.validateSession(sessionToken);
        if (engineerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid session"));
        }

        Engineer engineer = engineerOpt.get();
        String resolutionNotes = body.get("resolutionNotes");

        // Validate resolution notes
        if (!resolutionService.validateResolutionNotes(resolutionNotes)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Resolution notes must be at least 20 characters with 3+ words"));
        }

        ResolutionService.ResolutionResult result = resolutionService.resolveTicket(ticketId, resolutionNotes,
                engineer.getName());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", result.isSuccess());
        response.put("message", result.getMessage());

        if (result.isSuccess() && result.getTicket() != null) {
            response.put("ticket", buildTicketDetailResponse(result.getTicket()));
            response.put("finalStatus", result.getFinalStatus());

            // Include verification details (Phase 7)
            if (result.getVerificationResult() != null) {
                Map<String, Object> verification = new LinkedHashMap<>();
                verification.put("passed", result.isVerificationPassed());
                verification.put("outcome", result.getVerificationResult().getOutcome());
                verification.put("score", result.getVerificationResult().getScore());
                verification.put("issues", result.getVerificationResult().getIssues());
                verification.put("warnings", result.getVerificationResult().getWarnings());
                response.put("verification", verification);
            }
        }

        if (result.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Start working on a ticket.
     * Changes status from ASSIGNED to IN_PROGRESS.
     */
    @PostMapping("/ticket/{ticketId}/start")
    public ResponseEntity<?> startWorking(
            @PathVariable String ticketId,
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {

        Optional<Engineer> engineerOpt = engineerService.validateSession(sessionToken);
        if (engineerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid session"));
        }

        Engineer engineer = engineerOpt.get();

        Optional<Ticket> ticketOpt = resolutionService.startWorking(ticketId, engineer.getName());

        if (ticketOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Ticket not found or not assigned to you"));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Started working on ticket");
        response.put("ticket", buildTicketDetailResponse(ticketOpt.get()));

        return ResponseEntity.ok(response);
    }

    /**
     * Add progress notes to a ticket.
     */
    @PostMapping("/ticket/{ticketId}/notes")
    public ResponseEntity<?> addNotes(
            @PathVariable String ticketId,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {

        Optional<Engineer> engineerOpt = engineerService.validateSession(sessionToken);
        if (engineerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid session"));
        }

        Engineer engineer = engineerOpt.get();
        String notes = body.get("notes");

        if (notes == null || notes.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Notes are required"));
        }

        Optional<Ticket> ticketOpt = resolutionService.addProgressNotes(ticketId, notes, engineer.getName());

        if (ticketOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Ticket not found or not assigned to you"));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Notes added successfully");
        response.put("ticket", buildTicketDetailResponse(ticketOpt.get()));

        return ResponseEntity.ok(response);
    }

    /**
     * Escalate a ticket to management or specific team lead.
     *
     * REQUEST:
     * POST /api/engineer/ticket/{ticketId}/escalate
     * Body: {
     * "escalationReason": "Issue too complex for my level",
     * "targetTeamLead": "John Smith" (optional)
     * }
     */
    @PostMapping("/ticket/{ticketId}/escalate")
    public ResponseEntity<?> escalateTicket(
            @PathVariable String ticketId,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {

        Optional<Engineer> engineerOpt = engineerService.validateSession(sessionToken);
        if (engineerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid session"));
        }

        Engineer engineer = engineerOpt.get();
        String escalationReason = body.get("escalationReason");
        String targetTeamLead = body.get("targetTeamLead");

        if (escalationReason == null || escalationReason.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Escalation reason is required"));
        }

        Optional<Ticket> ticketOpt = resolutionService.escalateTicket(ticketId, escalationReason,
                engineer.getName(), targetTeamLead);

        if (ticketOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Ticket not found or not assigned to you"));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message",
                targetTeamLead != null ? "Ticket escalated to " + targetTeamLead : "Ticket escalated successfully");
        response.put("ticket", buildTicketDetailResponse(ticketOpt.get()));

        return ResponseEntity.ok(response);
    }

    /**
     * Get available team leads for escalation.
     * Returns a list of team leads that tickets can be escalated to.
     */
    @GetMapping("/team-leads")
    public ResponseEntity<?> getAvailableTeamLeads(
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {

        // Validate session and get current engineer
        Optional<Engineer> engineerOpt = engineerService.validateSession(sessionToken);
        String currentEngineersTeam = null;

        if (engineerOpt.isPresent()) {
            currentEngineersTeam = engineerOpt.get().getTeam();
        }

        // Get all team leads and filter out same department
        List<TeamLead> allTeamLeads = teamLeadRepository.findAll();
        final String finalCurrentTeam = currentEngineersTeam;

        List<Map<String, String>> teamLeads = allTeamLeads.stream()
                // Exclude team leads from the same department
                .filter(tl -> finalCurrentTeam == null || !tl.getDepartment().equalsIgnoreCase(finalCurrentTeam))
                .filter(tl -> "ACTIVE".equalsIgnoreCase(tl.getStatus()))
                .map(tl -> Map.of(
                        "name", tl.getName(),
                        "email", tl.getEmail(),
                        "department", tl.getDepartment() != null ? tl.getDepartment() : ""))
                .collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("teamLeads", teamLeads);
        response.put("count", teamLeads.size());
        response.put("currentTeam", finalCurrentTeam);

        logger.debug("Returning {} team leads (excluding team: {})", teamLeads.size(), finalCurrentTeam);
        return ResponseEntity.ok(response);
    }

    /**
     * Get engineer statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats(
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {

        Optional<Engineer> engineerOpt = engineerService.validateSession(sessionToken);
        if (engineerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid session"));
        }

        Engineer engineer = engineerOpt.get();
        Map<String, Object> stats = engineerService.getEngineerStats(engineer.getName());
        stats.put("engineer", buildEngineerResponse(engineer));

        return ResponseEntity.ok(stats);
    }

    // ============================================================
    // KNOWLEDGE BASE & SLA ENDPOINTS
    // ============================================================

    /**
     * Get all knowledge base entries for engineers.
     *
     * GET /api/engineer/knowledge-base
     */
    @GetMapping("/knowledge-base")
    public ResponseEntity<?> getKnowledgeBase(
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {

        Optional<Engineer> engineerOpt = engineerService.validateSession(sessionToken);
        if (engineerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid session"));
        }

        try {
            var entries = knowledgeBaseService.getAllActive();
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("count", entries.size());
            response.put("data", entries);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching knowledge base", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch knowledge base: " + e.getMessage()));
        }
    }

    /**
     * Get SLA status for engineer's tickets.
     *
     * GET /api/engineer/sla-status
     */
    @GetMapping("/sla-status")
    public ResponseEntity<?> getSlaStatus(
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {

        Optional<Engineer> engineerOpt = engineerService.validateSession(sessionToken);
        if (engineerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid session"));
        }

        try {
            Engineer engineer = engineerOpt.get();
            List<Ticket> activeTickets = engineerService.getActiveAssignedTickets(engineer.getName());

            Map<String, Object> dashboard = new LinkedHashMap<>();

            // Summary cards
            long totalActive = activeTickets.size();
            long breached = activeTickets.stream()
                    .filter(t -> Boolean.TRUE.equals(t.getSlaBreached()))
                    .count();
            long inWarning = activeTickets.stream()
                    .filter(t -> slaCalculationService.isInWarningZone(t))
                    .count();
            long onTrack = totalActive - breached - inWarning;

            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("totalActive", totalActive);
            summary.put("breached", breached);
            summary.put("inWarning", inWarning);
            summary.put("onTrack", Math.max(0, onTrack));
            dashboard.put("summary", summary);

            // Detailed SLA status for each ticket
            List<Map<String, Object>> tickets = new ArrayList<>();
            for (Ticket ticket : activeTickets) {
                Map<String, Object> ticketData = new LinkedHashMap<>();
                ticketData.put("ticketId", ticket.getTicketId());
                ticketData.put("subject", ticket.getEmailSubject());
                ticketData.put("priority", ticket.getPriority());
                ticketData.put("status", ticket.getStatus());
                ticketData.put("createdTime", ticket.getCreatedTime());
                ticketData.put("slaDeadline", ticket.getSlaDeadline());
                ticketData.put("slaBreached", ticket.getSlaBreached());

                if (ticket.getSlaDeadline() != null) {
                    long remainingMinutes = Duration.between(LocalDateTime.now(), ticket.getSlaDeadline())
                            .toMinutes();
                    ticketData.put("slaPercentage",
                            String.format("%.1f%%", slaCalculationService.calculateSlaPercentage(ticket)));
                    ticketData.put("slaStatus",
                            remainingMinutes < 0 ? "BREACHED"
                                    : remainingMinutes < 30 ? "CRITICAL"
                                            : remainingMinutes < 60 ? "WARNING" : "ON_TRACK");
                }

                tickets.add(ticketData);
            }
            dashboard.put("tickets", tickets);
            dashboard.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            logger.error("Error fetching SLA status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch SLA status: " + e.getMessage()));
        }
    }

    /**
     * Update engineer profile information.
     *
     * PUT /api/engineer/profile
     * Body: {
     * "name": "John Doe",
     * "email": "john@example.com",
     * "phone": "+1-234-567-8900",
     * "team": "Support Team"
     * }
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {

        Optional<Engineer> engineerOpt = engineerService.validateSession(sessionToken);
        if (engineerOpt.isEmpty()) {
            logger.warn("Update profile attempt with invalid session token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid session"));
        }

        Engineer engineer = engineerOpt.get();

        // Validate engineer ID
        if (engineer.getId() == null) {
            logger.error("Engineer from session has no ID");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Engineer ID is missing"));
        }

        logger.info("Updating profile for engineer ID: {} with data: {}", engineer.getId(), body);

        try {
            // Call service to update profile and save to database
            Engineer updated = engineerService.updateProfile(
                    engineer.getId(),
                    body.get("name"),
                    body.get("email"),
                    body.get("phone"),
                    body.get("team")
            );

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            response.put("engineer", buildEngineerResponse(updated));
            response.put("timestamp", LocalDateTime.now());

            logger.info("Engineer {} profile updated successfully", updated.getId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Engineer not found during profile update: {} - Message: {}", engineer.getId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Engineer not found: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating engineer profile for ID: {}", engineer.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update profile: " + e.getMessage(),
                                 "details", e.getClass().getSimpleName()));
        }
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Build engineer response object.
     */
    private Map<String, Object> buildEngineerResponse(Engineer engineer) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", engineer.getId());
        response.put("engineerId", engineer.getEngineerId());
        response.put("name", engineer.getName());
        response.put("email", engineer.getEmail());
        response.put("phone", engineer.getPhone());
        response.put("team", engineer.getTeam());
        response.put("role", engineer.getRole());
        response.put("status", engineer.getStatus());
        response.put("currentWorkload", engineer.getCurrentWorkload());
        response.put("ticketsResolved", engineer.getTicketsResolved());
        response.put("performanceScore", engineer.getPerformanceScore());
        return response;
    }

    /**
     * Build ticket list response.
     */
    private List<Map<String, Object>> buildTicketListResponse(List<Ticket> tickets) {
        List<Map<String, Object>> response = new ArrayList<>();

        for (Ticket ticket : tickets) {
            Map<String, Object> t = new LinkedHashMap<>();
            t.put("ticketId", ticket.getTicketId());
            t.put("subject", ticket.getEmailSubject() != null ? ticket.getEmailSubject() : "No Subject");
            t.put("priority", ticket.getPriority());
            t.put("category", ticket.getCategory());
            t.put("status", ticket.getStatus());
            t.put("createdTime", ticket.getCreatedTime());
            t.put("slaDeadline", ticket.getSlaDeadline());

            // Calculate SLA remaining
            if (ticket.getSlaDeadline() != null) {
                long remainingMinutes = Duration.between(LocalDateTime.now(), ticket.getSlaDeadline()).toMinutes();
                t.put("slaRemainingMinutes", remainingMinutes);
                t.put("slaBreached", remainingMinutes < 0);
                t.put("slaStatus", getSlaStatusLabel(remainingMinutes, ticket.getPriority()));
            } else {
                t.put("slaRemainingMinutes", null);
                t.put("slaBreached", false);
                t.put("slaStatus", "UNKNOWN");
            }

            response.add(t);
        }

        // Sort by priority and SLA
        response.sort((a, b) -> {
            int priorityCompare = getPriorityWeight((String) b.get("priority"))
                    - getPriorityWeight((String) a.get("priority"));
            if (priorityCompare != 0)
                return priorityCompare;

            Long slaA = (Long) a.get("slaRemainingMinutes");
            Long slaB = (Long) b.get("slaRemainingMinutes");
            if (slaA == null)
                return 1;
            if (slaB == null)
                return -1;
            return slaA.compareTo(slaB);
        });

        return response;
    }

    /**
     * Build detailed ticket response.
     */
    private Map<String, Object> buildTicketDetailResponse(Ticket ticket) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("ticketId", ticket.getTicketId());
        response.put("source", ticket.getSource());
        response.put("employeeId", ticket.getEmployeeId());
        response.put("senderEmail", ticket.getSenderEmail());
        response.put("subject", ticket.getEmailSubject());
        response.put("issueDescription", ticket.getIssueDescription());
        response.put("category", ticket.getCategory());
        response.put("subCategory", ticket.getSubCategory());
        response.put("priority", ticket.getPriority());
        response.put("status", ticket.getStatus());
        response.put("assignedTeam", ticket.getAssignedTeam());
        response.put("assignedEngineer", ticket.getAssignedEngineer());
        response.put("createdTime", ticket.getCreatedTime());
        response.put("slaDeadline", ticket.getSlaDeadline());
        response.put("slaBreached", ticket.getSlaBreached());
        response.put("escalationLevel", ticket.getEscalationLevel());
        response.put("escalatedToTeamLead", ticket.getEscalatedToTeamLead());
        response.put("resolutionNotes", ticket.getResolutionNotes());
        response.put("resolutionStatus", ticket.getResolutionStatus());

        // Phase 7: AI Verification fields
        response.put("verificationStatus", ticket.getVerificationStatus());
        response.put("verificationScore", ticket.getVerificationScore());
        response.put("verificationNotes", ticket.getVerificationNotes());
        response.put("verifiedTime", ticket.getVerifiedTime());
        response.put("verificationAttempts", ticket.getVerificationAttempts());

        // SLA calculations
        if (ticket.getSlaDeadline() != null) {
            long remainingMinutes = Duration.between(LocalDateTime.now(), ticket.getSlaDeadline()).toMinutes();
            response.put("slaRemainingMinutes", remainingMinutes);
            response.put("slaRemainingFormatted", formatDuration(remainingMinutes));
            response.put("slaStatus", getSlaStatusLabel(remainingMinutes, ticket.getPriority()));
        }

        return response;
    }

    /**
     * Get priority weight for sorting.
     */
    private int getPriorityWeight(String priority) {
        if (priority == null)
            return 0;
        return switch (priority.toUpperCase()) {
            case "CRITICAL" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }

    /**
     * Get SLA status label.
     */
    private String getSlaStatusLabel(long remainingMinutes, String priority) {
        if (remainingMinutes < 0)
            return "BREACHED";
        if (remainingMinutes < 30)
            return "CRITICAL";
        if (remainingMinutes < 60)
            return "WARNING";
        return "ON_TRACK";
    }

    /**
     * Format duration in human-readable form.
     */
    private String formatDuration(long minutes) {
        if (minutes < 0) {
            return Math.abs(minutes) + " min overdue";
        }
        if (minutes < 60) {
            return minutes + " min";
        }
        long hours = minutes / 60;
        long mins = minutes % 60;
        return hours + "h " + mins + "m";
    }
}
