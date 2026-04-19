package com.powergrid.ticketsystem.service;

import com.powergrid.ticketsystem.entity.Ticket;
import com.powergrid.ticketsystem.entity.TeamLead;
import com.powergrid.ticketsystem.notification.NotificationService;
import com.powergrid.ticketsystem.nlp.TeamAssignmentService;
import com.powergrid.ticketsystem.repository.TeamLeadRepository;
import com.powergrid.ticketsystem.repository.TicketRepository;
import com.powergrid.ticketsystem.verification.AIVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Optional;

/**
 * ============================================================
 * RESOLUTION SERVICE
 * ============================================================
 * 
 * PHASE 6: ENGINEER RESOLUTION WORKFLOW
 * 
 * This service handles the ticket resolution process.
 * 
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║ CRITICAL DESIGN DECISION: RESOLVE vs CLOSE ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║ ║
 * ║ Engineers can RESOLVE but NOT CLOSE tickets. ║
 * ║ ║
 * ║ WHY? ║
 * ║ ───── ║
 * ║ 1. QUALITY CONTROL ║
 * ║ - Resolved tickets go to Phase 7 (AI Verification) ║
 * ║ - AI checks if resolution is complete and accurate ║
 * ║ - Prevents premature closure of unresolved issues ║
 * ║ ║
 * ║ 2. AUDIT TRAIL ║
 * ║ - Clear separation: who resolved vs who closed ║
 * ║ - Accountability for each step ║
 * ║ ║
 * ║ 3. USER CONFIRMATION ║
 * ║ - User may need to confirm resolution worked ║
 * ║ - Only then ticket is closed ║
 * ║ ║
 * ║ WORKFLOW (Updated for Phase 7): ║
 * ║ ─────────────────────────────── ║
 * ║ ASSIGNED → RESOLVED (by Engineer) ║
 * ║ → AI VERIFICATION (Phase 7) ║
 * ║ ├─ VALID → CLOSED (by AI) ║
 * ║ └─ INVALID → ASSIGNED (reopened, back to engineer) ║
 * ║ ║
 * ╚═══════════════════════════════════════════════════════════════╝
 */
@Service
public class ResolutionService {

    private static final Logger logger = LoggerFactory.getLogger(ResolutionService.class);

    private final TicketRepository ticketRepository;
    private final EngineerService engineerService;
    private final TeamAssignmentService teamAssignmentService;
    private final TeamLeadRepository teamLeadRepository;
    private AIVerificationService aiVerificationService;
    private NotificationService notificationService;

    public ResolutionService(TicketRepository ticketRepository,
            EngineerService engineerService,
            TeamAssignmentService teamAssignmentService,
            TeamLeadRepository teamLeadRepository) {
        this.ticketRepository = ticketRepository;
        this.engineerService = engineerService;
        this.teamAssignmentService = teamAssignmentService;
        this.teamLeadRepository = teamLeadRepository;
        logger.info("Resolution Service initialized");
    }

    /**
     * Setter injection to avoid circular dependency with AIVerificationService.
     */
    @Autowired
    @Lazy
    public void setAiVerificationService(AIVerificationService aiVerificationService) {
        this.aiVerificationService = aiVerificationService;
        logger.info("AI Verification Service injected into Resolution Service");
    }

    /**
     * Setter injection for NotificationService (Phase 8).
     */
    @Autowired
    @Lazy
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
        logger.info("NotificationService injected into Resolution Service");
    }

    /**
     * Resolve a ticket with resolution notes.
     * 
     * This method:
     * 1. Validates the ticket exists and is assigned to the engineer
     * 2. Updates the ticket status to RESOLVED
     * 3. Saves resolution notes and timestamp
     * 4. Updates engineer statistics
     * 5. Triggers AI Verification (Phase 7)
     * 
     * IMPORTANT: Status changes to RESOLVED first, then AI verification
     * decides whether to CLOSE or REOPEN the ticket.
     * 
     * @param ticketId        The ticket to resolve
     * @param resolutionNotes Notes describing the resolution
     * @param engineerName    The engineer resolving the ticket
     * @return ResolutionResult with status and details
     */
    @Transactional
    public ResolutionResult resolveTicket(String ticketId, String resolutionNotes, String engineerName) {
        logger.info("Resolving ticket {} by engineer {}", ticketId, engineerName);

        // Validate inputs
        if (ticketId == null || ticketId.isEmpty()) {
            return new ResolutionResult(false, null, "Ticket ID is required", null);
        }
        if (resolutionNotes == null || resolutionNotes.trim().isEmpty()) {
            return new ResolutionResult(false, null, "Resolution notes are required", null);
        }
        if (engineerName == null || engineerName.isEmpty()) {
            return new ResolutionResult(false, null, "Engineer name is required", null);
        }

        // Find the ticket
        Optional<Ticket> ticketOpt = ticketRepository.findByTicketId(ticketId);
        if (ticketOpt.isEmpty()) {
            logger.warn("Ticket not found: {}", ticketId);
            return new ResolutionResult(false, null, "Ticket not found: " + ticketId, null);
        }

        Ticket ticket = ticketOpt.get();

        // Validate engineer is assigned to this ticket
        if (!engineerName.equals(ticket.getAssignedEngineer())) {
            logger.warn("Engineer {} not authorized for ticket {}", engineerName, ticketId);
            return new ResolutionResult(false, null,
                    "You are not authorized to resolve this ticket. Assigned to: " + ticket.getAssignedEngineer(),
                    null);
        }

        // Check if ticket is already resolved or closed
        String currentStatus = ticket.getStatus();
        if ("RESOLVED".equalsIgnoreCase(currentStatus)) {
            return new ResolutionResult(false, ticket, "Ticket is already resolved", null);
        }
        if ("CLOSED".equalsIgnoreCase(currentStatus)) {
            return new ResolutionResult(false, ticket, "Ticket is already closed", null);
        }

        // Update ticket with resolution details
        ticket.setStatus("RESOLVED");
        ticket.setResolutionNotes(resolutionNotes.trim());
        ticket.setClosedTime(LocalDateTime.now()); // This is actually resolvedTime
        ticket.setClosedBy(engineerName);
        ticket.setResolutionStatus("ENGINEER_RESOLVED");

        // Increment verification attempts counter
        Integer attempts = ticket.getVerificationAttempts();
        ticket.setVerificationAttempts(attempts != null ? attempts + 1 : 1);

        // Calculate resolution time
        Duration resolutionDuration = Duration.between(ticket.getCreatedTime(), LocalDateTime.now());

        // Save the updated ticket
        Ticket savedTicket = ticketRepository.save(ticket);

        // Update engineer statistics
        engineerService.updateWorkload(engineerName, -1);
        engineerService.incrementResolvedCount(engineerName);

        logger.info("Ticket {} resolved by {} in {} minutes. Starting AI verification...",
                ticketId, engineerName, resolutionDuration.toMinutes());

        // ================================================================
        // PHASE 8: SEND TICKET_RESOLVED NOTIFICATION
        // ================================================================
        try {
            if (notificationService != null) {
                notificationService.notifyTicketResolved(savedTicket);
            }
        } catch (Exception e) {
            logger.warn("Failed to send TICKET_RESOLVED notification for {}: {}",
                    ticketId, e.getMessage());
        }

        // ================================================================
        // PHASE 7: TRIGGER AI VERIFICATION
        // ================================================================
        AIVerificationService.VerificationResult verificationResult = null;
        String finalMessage;

        if (aiVerificationService != null) {
            try {
                verificationResult = aiVerificationService.verifyAndProcess(savedTicket);

                if (verificationResult.isPassed()) {
                    finalMessage = "✅ Ticket resolved and VERIFIED by AI. Status: CLOSED. " +
                            "Quality Score: " + verificationResult.getScore() + "/100";
                    logger.info("Ticket {} passed AI verification and is now CLOSED", ticketId);
                } else {
                    finalMessage = "⚠️ Ticket resolution needs improvement. Status: REOPENED. " +
                            "Issues: " + String.join(", ", verificationResult.getIssues());
                    logger.warn("Ticket {} failed AI verification and was REOPENED", ticketId);

                    // Restore engineer workload since ticket was reopened
                    engineerService.updateWorkload(engineerName, 1);
                }
            } catch (Exception e) {
                logger.error("Error during AI verification for ticket {}: {}", ticketId, e.getMessage());
                finalMessage = "Ticket resolved. AI verification encountered an error - manual review needed.";
            }
        } else {
            finalMessage = "Ticket resolved successfully. AI verification service not available.";
        }

        return new ResolutionResult(true,
                ticketRepository.findByTicketId(ticketId).orElse(savedTicket),
                finalMessage,
                verificationResult);
    }

    /**
     * Get ticket for viewing (includes escalated tickets).
     * Allows engineers to view tickets they escalated or are currently assigned to.
     *
     * @param ticketId     Ticket ID
     * @param engineerName Engineer name
     * @return Ticket if authorized to view
     */
    public Optional<Ticket> getTicketForViewing(String ticketId, String engineerName) {
        Optional<Ticket> ticketOpt = ticketRepository.findByTicketId(ticketId);

        if (ticketOpt.isEmpty()) {
            return Optional.empty();
        }

        Ticket ticket = ticketOpt.get();

        // Allow viewing if:
        // 1. Currently assigned to this engineer, OR
        // 2. Was previously assigned (escalated ticket)
        if (engineerName.equals(ticket.getAssignedEngineer()) ||
            engineerName.equals(ticket.getPreviousEngineer())) {
            return ticketOpt;
        }

        logger.warn("Engineer {} not authorized to view ticket {}", engineerName, ticketId);
        return Optional.empty();
    }

    /**
     * Get ticket details for resolution.
     *
     * @param ticketId     Ticket ID
     * @param engineerName Engineer requesting the ticket
     * @return Optional ticket if accessible
     */
    public Optional<Ticket> getTicketForResolution(String ticketId, String engineerName) {
        Optional<Ticket> ticketOpt = ticketRepository.findByTicketId(ticketId);

        if (ticketOpt.isEmpty()) {
            return Optional.empty();
        }

        Ticket ticket = ticketOpt.get();

        // Validate engineer is assigned to this ticket
        if (!engineerName.equals(ticket.getAssignedEngineer())) {
            logger.warn("Engineer {} not authorized to edit ticket {}", engineerName, ticketId);
            return Optional.empty();
        }

        return ticketOpt;
    }

    /**
     * Add notes to a ticket without resolving it.
     * Useful for progress updates.
     * 
     * @param ticketId     Ticket ID
     * @param notes        Notes to add
     * @param engineerName Engineer adding notes
     * @return Updated ticket
     */
    @Transactional
    public Optional<Ticket> addProgressNotes(String ticketId, String notes, String engineerName) {
        Optional<Ticket> ticketOpt = getTicketForResolution(ticketId, engineerName);

        if (ticketOpt.isEmpty()) {
            return Optional.empty();
        }

        Ticket ticket = ticketOpt.get();

        // Append notes with timestamp
        String existingNotes = ticket.getResolutionNotes() != null ? ticket.getResolutionNotes() : "";
        String newNote = String.format("\n[%s - %s]: %s",
                LocalDateTime.now().toString(), engineerName, notes);

        ticket.setResolutionNotes(existingNotes + newNote);

        // Update status to IN_PROGRESS if still ASSIGNED
        if ("ASSIGNED".equalsIgnoreCase(ticket.getStatus()) || "OPEN".equalsIgnoreCase(ticket.getStatus())) {
            ticket.setStatus("IN_PROGRESS");
        }

        return Optional.of(ticketRepository.save(ticket));
    }

    /**
     * Start working on a ticket.
     * Changes status from ASSIGNED to IN_PROGRESS.
     * 
     * @param ticketId     Ticket ID
     * @param engineerName Engineer starting work
     * @return Updated ticket
     */
    @Transactional
    public Optional<Ticket> startWorking(String ticketId, String engineerName) {
        Optional<Ticket> ticketOpt = getTicketForResolution(ticketId, engineerName);

        if (ticketOpt.isEmpty()) {
            return Optional.empty();
        }

        Ticket ticket = ticketOpt.get();

        if ("ASSIGNED".equalsIgnoreCase(ticket.getStatus()) || "OPEN".equalsIgnoreCase(ticket.getStatus())) {
            ticket.setStatus("IN_PROGRESS");
            logger.info("Engineer {} started working on ticket {}", engineerName, ticketId);
            return Optional.of(ticketRepository.save(ticket));
        }

        return Optional.of(ticket);
    }

    /**
     * Escalate a ticket to management.
     * Saves escalation reason, target team lead, and updates status.
     *
     * @param ticketId         Ticket ID
     * @param escalationReason Reason for escalation
     * @param engineerName     Engineer escalating
     * @param targetTeamLead   Team lead to escalate to (optional)
     * @return Updated ticket
     */
    @Transactional
    public Optional<Ticket> escalateTicket(String ticketId, String escalationReason, String engineerName) {
        return escalateTicket(ticketId, escalationReason, engineerName, null);
    }

    /**
     * Escalate a ticket to a specific team lead.
     *
     * @param ticketId         Ticket ID
     * @param escalationReason Reason for escalation
     * @param engineerName     Engineer escalating
     * @param targetTeamLead   Specific team lead to route to
     * @return Updated ticket
     */
    @Transactional
    public Optional<Ticket> escalateTicket(String ticketId, String escalationReason, String engineerName,
            String targetTeamLead) {
        Optional<Ticket> ticketOpt = getTicketForResolution(ticketId, engineerName);

        if (ticketOpt.isEmpty()) {
            return Optional.empty();
        }

        Ticket ticket = ticketOpt.get();
        ticket.setEscalationReason(escalationReason);
        ticket.setEscalatedTime(LocalDateTime.now());
        ticket.setEscalationLevel("LEVEL_1");
        ticket.setStatus("ESCALATED");
        ticket.setResolutionStatus("ESCALATED_TO_TEAM_LEAD");
        ticket.setLastSlaCheck(LocalDateTime.now());

        // Store previous engineer before reassignment
        String previousEngineer = ticket.getAssignedEngineer();
        ticket.setPreviousEngineer(previousEngineer);

        // Set the target team lead if provided
        if (targetTeamLead != null && !targetTeamLead.isEmpty()) {
            ticket.setEscalatedToTeamLead(targetTeamLead);
            // Assign the ticket to the team lead so they can resolve it
            ticket.setAssignedEngineer(targetTeamLead);
            logger.info("Escalating ticket {} to specific team lead: {}", ticketId, targetTeamLead);

            String escalatedTeam = resolveEscalationTeamName(targetTeamLead, ticket.getAssignedTeam());
            ticket.setAssignedTeam(escalatedTeam);
        } else {
            // When no explicit team lead is chosen, route to admin/management chain.
            ticket.setAssignedEngineer(null);
            ticket.setEscalatedToTeamLead("Admin Team");
        }

        // Also set priority to HIGH/CRITICAL if not already
        if (!"CRITICAL".equalsIgnoreCase(ticket.getPriority())) {
            ticket.setPriority("HIGH");
        }

        logger.info("Engineer {} escalated ticket {} with reason: {}", engineerName, ticketId, escalationReason);

        Ticket savedTicket = ticketRepository.save(ticket);

        // NOTIFY: Send assignment email to newly assigned engineer (if different from previous)
        if (targetTeamLead != null && !targetTeamLead.isEmpty() &&
                savedTicket.getAssignedEngineer() != null &&
                !savedTicket.getAssignedEngineer().equals(previousEngineer)) {
            try {
                if (notificationService != null) {
                    logger.info("📧 Notifying newly assigned team lead: {}", savedTicket.getAssignedEngineer());
                    notificationService.notifyTicketAssigned(savedTicket);
                }
            } catch (Exception e) {
                logger.warn("Failed to notify newly assigned team lead {}: {}",
                        savedTicket.getAssignedEngineer(), e.getMessage());
            }
        }

        // Notify all stakeholders
        if (notificationService != null) {
            notificationService.notifyTeamLeadOfEscalation(savedTicket, escalationReason);
            notificationService.notifyEmployeeOfEscalation(savedTicket);
            notificationService.notifyEngineerOfEscalationConfirmation(savedTicket, engineerName);
        }

        return Optional.of(savedTicket);
    }

    /**
     * Calculate time remaining until SLA breach.
     * 
     * @param ticket The ticket
     * @return Remaining time in minutes (negative if breached)
     */
    public long calculateSlaRemainingMinutes(Ticket ticket) {
        if (ticket.getSlaDeadline() == null) {
            return Long.MAX_VALUE; // No deadline set
        }

        Duration remaining = Duration.between(LocalDateTime.now(), ticket.getSlaDeadline());
        return remaining.toMinutes();
    }

    /**
     * Check if resolution notes are adequate.
     * Simple validation for minimum quality.
     *
     * @param notes Resolution notes
     * @return true if notes are adequate
     */
    public boolean validateResolutionNotes(String notes) {
        if (notes == null || notes.trim().isEmpty()) {
            return false;
        }

        // Minimum 20 characters
        if (notes.trim().length() < 20) {
            return false;
        }

        // Should contain at least 3 words
        String[] words = notes.trim().split("\\s+");
        return words.length >= 3;
    }

    /**
     * AUTO-ASSIGN ticket to an engineer in the same team.
     * Called when a ticket is escalated to a team lead.
     * Immediately assigns the ticket to the least loaded engineer in that team.
     *
     * @param ticket   The escalated ticket
     * @param teamName The team name to assign from
     */
    @SuppressWarnings("unused")
    private void autoAssignToTeamEngineer(Ticket ticket, String teamName) {
        try {
            if (teamName == null || teamName.isEmpty()) {
                logger.warn("Cannot auto-assign - no team specified for ticket {}", ticket.getTicketId());
                return;
            }

            // Convert team name string to Team enum
            com.powergrid.ticketsystem.constants.Team team = null;

            // Map common team names to Team enum
            if (teamName.contains("Network") || teamName.contains("NET")) {
                team = com.powergrid.ticketsystem.constants.Team.NETWORK_TEAM;
            } else if (teamName.contains("Hardware") || teamName.contains("HW")) {
                team = com.powergrid.ticketsystem.constants.Team.HARDWARE_SUPPORT;
            } else if (teamName.contains("Application") || teamName.contains("APP")) {
                team = com.powergrid.ticketsystem.constants.Team.APPLICATION_SUPPORT;
            } else if (teamName.contains("Security") || teamName.contains("SEC")) {
                team = com.powergrid.ticketsystem.constants.Team.IT_SECURITY;
            } else if (teamName.contains("Email") || teamName.contains("MAIL")) {
                team = com.powergrid.ticketsystem.constants.Team.EMAIL_SUPPORT;
            } else {
                team = com.powergrid.ticketsystem.constants.Team.NETWORK_TEAM; // default
            }

            // Get least loaded engineer from team
            String assignedEngineer = teamAssignmentService.assignEngineer(team,
                    com.powergrid.ticketsystem.constants.Priority.valueOf(ticket.getPriority()));

            if (assignedEngineer != null && !assignedEngineer.isEmpty()) {
                ticket.setAssignedEngineer(assignedEngineer);
                logger.info("✅ AUTO-ASSIGNED ticket {} to engineer {} in team {}",
                        ticket.getTicketId(), assignedEngineer, teamName);
            }
        } catch (Exception e) {
            logger.warn("Failed to auto-assign ticket {} to team {}: {}",
                    ticket.getTicketId(), teamName, e.getMessage());
        }
    }

    /**
     * Resolve destination team from the selected team lead, then fall back to
     * keyword
     * inference and finally current team.
     */
    private String resolveEscalationTeamName(String targetTeamLead, String fallbackTeam) {
        if (targetTeamLead == null || targetTeamLead.isBlank()) {
            return fallbackTeam;
        }

        Optional<TeamLead> teamLead = findTeamLeadByNameFlexible(targetTeamLead);
        if (teamLead.isPresent()) {
            String department = teamLead.get().getDepartment();
            String normalizedDepartmentTeam = normalizeTeamName(department);
            if (normalizedDepartmentTeam != null) {
                return normalizedDepartmentTeam;
            }
        }

        String inferredFromTarget = normalizeTeamName(targetTeamLead);
        if (inferredFromTarget != null) {
            return inferredFromTarget;
        }

        return fallbackTeam;
    }

    private Optional<TeamLead> findTeamLeadByNameFlexible(String rawInput) {
        if (rawInput == null || rawInput.isBlank()) {
            return Optional.empty();
        }

        String cleanedInput = rawInput.replaceFirst("(?i)^\\s*hello\\s+", "")
                .replace(",", " ")
                .trim();

        Optional<TeamLead> exact = teamLeadRepository.findByName(cleanedInput);
        if (exact.isPresent()) {
            return exact;
        }

        String canonicalInput = canonicalizeLabel(cleanedInput);
        for (TeamLead lead : teamLeadRepository.findAllActive()) {
            String canonicalLeadName = canonicalizeLabel(lead.getName());
            if (canonicalInput.equals(canonicalLeadName)
                    || canonicalInput.contains(canonicalLeadName)
                    || canonicalLeadName.contains(canonicalInput)) {
                return Optional.of(lead);
            }
        }

        return Optional.empty();
    }

    private String canonicalizeLabel(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase()
                .replaceAll("\\(.*?\\)", " ")
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Normalize user-facing department/team strings to internal team names.
     */
    private String normalizeTeamName(String teamLikeText) {
        if (teamLikeText == null || teamLikeText.isBlank()) {
            return null;
        }

        String normalized = teamLikeText.toLowerCase();
        if (normalized.contains("hardware") || normalized.contains("hw")) {
            return "Hardware Support Team";
        }
        if (normalized.contains("application") || normalized.contains("app")) {
            return "Application Support Team";
        }
        if (normalized.contains("network") || normalized.contains("net")) {
            return "Network Team";
        }
        if (normalized.contains("security") || normalized.contains("sec")) {
            return "IT Security Team";
        }
        if (normalized.contains("email") || normalized.contains("mail")) {
            return "Email Support Team";
        }
        if (normalized.contains("general") || normalized.contains("it support")) {
            return "General IT Support";
        }

        return null;
    }

    // ============================================================
    // INNER CLASSES
    // ============================================================

    /**
     * Result object for resolution operation.
     * Updated for Phase 7 to include AI verification result.
     */
    public static class ResolutionResult {
        private final boolean success;
        private final Ticket ticket;
        private final String message;
        private final AIVerificationService.VerificationResult verificationResult;

        public ResolutionResult(boolean success, Ticket ticket, String message,
                AIVerificationService.VerificationResult verificationResult) {
            this.success = success;
            this.ticket = ticket;
            this.message = message;
            this.verificationResult = verificationResult;
        }

        public boolean isSuccess() {
            return success;
        }

        public Ticket getTicket() {
            return ticket;
        }

        public String getMessage() {
            return message;
        }

        public AIVerificationService.VerificationResult getVerificationResult() {
            return verificationResult;
        }

        /**
         * Check if AI verification passed.
         */
        public boolean isVerificationPassed() {
            return verificationResult != null && verificationResult.isPassed();
        }

        /**
         * Get the final ticket status after verification.
         */
        public String getFinalStatus() {
            if (ticket != null) {
                return ticket.getStatus();
            }
            return null;
        }
    }
}
