package com.powergrid.ticketsystem.selfservice;

import com.powergrid.ticketsystem.constants.ResolutionStatus;
import com.powergrid.ticketsystem.entity.Ticket;
import com.powergrid.ticketsystem.nlp.ClassificationService;
import com.powergrid.ticketsystem.nlp.ClassificationService.ClassificationResult;
import com.powergrid.ticketsystem.nlp.TeamAssignmentService;
import com.powergrid.ticketsystem.notification.NotificationService;
import com.powergrid.ticketsystem.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * ============================================================
 * FALLBACK ASSIGNMENT SERVICE
 * ============================================================
 * 
 * PHASE 4: SELF-SERVICE RESOLUTION
 * 
 * Handles escalation when self-service fails.
 * NOW ALSO handles classification when user says NO.
 * 
 * Assigns ticket to an engineer when:
 * 
 * 1. User responds "NO" - solution didn't work
 * 2. User response timeout - no response within time limit
 * 3. No self-service solution available
 * 4. Ticket priority too high for self-service
 * 
 * NEW ESCALATION FLOW (when user says NO):
 * 1. Classify ticket (if not already classified)
 * 2. Set priority
 * 3. Assign team and engineer (load-balanced)
 * 4. Update ticket status to ASSIGNED
 * 5. Notify engineer
 */
@Service
public class FallbackAssignmentService {

    private static final Logger logger = LoggerFactory.getLogger(FallbackAssignmentService.class);

    private final TicketRepository ticketRepository;
    private final TeamAssignmentService teamAssignmentService;

    private ClassificationService classificationService;
    private NotificationService notificationService;

    public FallbackAssignmentService(TicketRepository ticketRepository,
            TeamAssignmentService teamAssignmentService) {
        this.ticketRepository = ticketRepository;
        this.teamAssignmentService = teamAssignmentService;
    }

    @Autowired
    @Lazy
    public void setClassificationService(ClassificationService classificationService) {
        this.classificationService = classificationService;
        logger.info("ClassificationService injected into FallbackAssignmentService");
    }

    @Autowired
    @Lazy
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
        logger.info("NotificationService injected into FallbackAssignmentService");
    }
    // ============================================================
    // MAIN ESCALATION METHOD
    // ============================================================

    /**
     * Escalate ticket to engineer after self-service failure.
     * 
     * @param ticketId The ticket ID
     * @param reason   Reason for escalation
     * @return Updated ticket with engineer assignment
     */
    @Transactional
    public Ticket escalateToEngineer(String ticketId, String reason) {
        logger.info("Escalating ticket to engineer: {} - Reason: {}", ticketId, reason);

        Optional<Ticket> ticketOpt = ticketRepository.findByTicketId(ticketId);
        if (ticketOpt.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found: " + ticketId);
        }

        Ticket ticket = ticketOpt.get();

        // Check if already assigned
        if ("ASSIGNED".equalsIgnoreCase(ticket.getStatus()) &&
                ticket.getAssignedEngineer() != null) {
            logger.warn("Ticket already assigned to: {}", ticket.getAssignedEngineer());
            return ticket;
        }

        // Get team and engineer assignment
        TeamAssignmentService.AssignmentResult assignment = teamAssignmentService.assignTicket(
                ticket.getCategory(),
                ticket.getPriority());

        // Update ticket with assignment
        ticket.setStatus("ASSIGNED");
        ticket.setAssignedTeam(assignment.getTeam().getTeamName());
        ticket.setAssignedEngineer(assignment.getEngineer());
        ticket.setResolutionStatus(ResolutionStatus.ESCALATED.name());
        ticket.setEscalationReason(reason);
        ticket.setEscalatedTime(LocalDateTime.now());

        // Add notes
        String existingNotes = ticket.getResolutionNotes();
        String newNote = "Escalated to engineer: " + reason;
        ticket.setResolutionNotes(existingNotes != null ? existingNotes + "\n" + newNote : newNote);

        Ticket savedTicket = ticketRepository.save(ticket);

        logger.info("Ticket {} escalated to {} ({})",
                ticketId, assignment.getEngineer(), assignment.getTeam().getTeamName());

        return savedTicket;
    }

    // ============================================================
    // ESCALATION DUE TO USER RESPONSE "NO"
    // ============================================================

    /**
     * Escalate when user says solution didn't work.
     * NOW performs classification FIRST (since ticket wasn't classified before
     * self-service).
     */
    @Transactional
    public Ticket escalateDueToFailedResolution(String ticketId) {
        logger.info("════════════════════════════════════════════════════════");
        logger.info("USER SAID NO - PROCEEDING WITH CLASSIFICATION & ASSIGNMENT");
        logger.info("Ticket: {}", ticketId);
        logger.info("════════════════════════════════════════════════════════");

        Optional<Ticket> ticketOpt = ticketRepository.findByTicketId(ticketId);
        if (ticketOpt.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found: " + ticketId);
        }

        Ticket ticket = ticketOpt.get();

        // STEP 1: Classify the ticket if not already classified
        if (ticket.getCategory() == null || !Boolean.TRUE.equals(ticket.getIsClassified())) {
            logger.info("Step 1: Classifying ticket (was not classified before)...");

            if (classificationService != null) {
                ClassificationResult result = classificationService.classifyTicket(
                        ticket.getIssueDescription());

                ticket.setCategory(result.getCategory().name());
                ticket.setSubCategory(result.getSubCategory().name());
                ticket.setPriority(result.getPriority().name());
                ticket.setConfidenceScore(result.getConfidenceScore());
                ticket.setIsClassified(true);
                ticket.setClassifiedTime(LocalDateTime.now());

                logger.info("  ✓ Category: {}", result.getCategory());
                logger.info("  ✓ SubCategory: {}", result.getSubCategory());
                logger.info("  ✓ Priority: {}", result.getPriority());
            } else {
                // Fallback defaults if classification service not available
                ticket.setCategory("GENERAL");
                ticket.setPriority("MEDIUM");
                logger.warn("  ✗ ClassificationService not available, using defaults");
            }
        } else {
            logger.info("Step 1: Ticket already classified - Category: {}", ticket.getCategory());
        }

        // STEP 2: Assign team and engineer
        logger.info("Step 2: Assigning team and engineer...");
        TeamAssignmentService.AssignmentResult assignment = teamAssignmentService.assignTicket(
                ticket.getCategory(),
                ticket.getPriority());

        ticket.setAssignedTeam(assignment.getTeam().getTeamName());
        ticket.setAssignedEngineer(assignment.getEngineer());

        logger.info("  ✓ Team: {}", assignment.getTeam().getTeamName());
        logger.info("  ✓ Engineer: {}", assignment.getEngineer());

        // STEP 3: Update ticket status
        ticket.setStatus("ASSIGNED");
        ticket.setResolutionStatus(ResolutionStatus.ESCALATED.name());
        ticket.setEscalationReason("User indicated self-service solution did not resolve the issue");
        ticket.setEscalatedTime(LocalDateTime.now());

        String existingNotes = ticket.getResolutionNotes();
        String newNote = "Self-service solution unsuccessful. Escalated to engineer.";
        ticket.setResolutionNotes(existingNotes != null ? existingNotes + "\n" + newNote : newNote);

        Ticket savedTicket = ticketRepository.save(ticket);

        // STEP 4: Send engineer assignment notification
        logger.info("Step 3: Sending engineer notification...");
        try {
            if (notificationService != null) {
                notificationService.notifyTicketAssigned(savedTicket);
                logger.info("  ✓ Engineer notification sent");
            }
        } catch (Exception e) {
            logger.warn("  ✗ Failed to send notification: {}", e.getMessage());
        }

        logger.info("════════════════════════════════════════════════════════");
        logger.info("ESCALATION COMPLETE");
        logger.info("  Ticket: {} → Status: ASSIGNED", ticketId);
        logger.info("  Engineer: {}", assignment.getEngineer());
        logger.info("════════════════════════════════════════════════════════");

        return savedTicket;
    }

    // ============================================================
    // ESCALATION DUE TO TIMEOUT
    // ============================================================

    /**
     * Escalate when user doesn't respond within timeout.
     */
    @Transactional
    public Ticket escalateDueToTimeout(String ticketId) {
        logger.info("Escalating ticket due to timeout: {}", ticketId);

        Optional<Ticket> ticketOpt = ticketRepository.findByTicketId(ticketId);
        if (ticketOpt.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found: " + ticketId);
        }

        Ticket ticket = ticketOpt.get();

        // Update resolution status to TIMED_OUT
        ticket.setResolutionStatus(ResolutionStatus.TIMED_OUT.name());

        ticketRepository.save(ticket);

        // Then escalate
        return escalateToEngineer(ticketId,
                "User did not respond to self-service solution within timeout period");
    }

    // ============================================================
    // ESCALATION DUE TO NO SOLUTION AVAILABLE
    // ============================================================

    /**
     * Assign to engineer when no self-service solution exists.
     */
    @Transactional
    public Ticket assignDueToNoSolution(String ticketId) {
        logger.info("Assigning ticket due to no self-service solution: {}", ticketId);

        Optional<Ticket> ticketOpt = ticketRepository.findByTicketId(ticketId);
        if (ticketOpt.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found: " + ticketId);
        }

        Ticket ticket = ticketOpt.get();
        ticket.setResolutionStatus(ResolutionStatus.NOT_APPLICABLE.name());
        ticketRepository.save(ticket);

        return escalateToEngineer(ticketId,
                "No self-service solution available for this issue type");
    }

    // ============================================================
    // ESCALATION DUE TO PRIORITY
    // ============================================================

    /**
     * Assign to engineer due to high priority.
     */
    @Transactional
    public Ticket assignDueToPriority(String ticketId) {
        logger.info("Assigning ticket due to priority: {}", ticketId);

        Optional<Ticket> ticketOpt = ticketRepository.findByTicketId(ticketId);
        if (ticketOpt.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found: " + ticketId);
        }

        Ticket ticket = ticketOpt.get();
        ticket.setResolutionStatus(ResolutionStatus.SKIPPED.name());
        ticketRepository.save(ticket);

        return escalateToEngineer(ticketId,
                "High/Critical priority - self-service skipped per policy");
    }

    // ============================================================
    // REASSIGNMENT
    // ============================================================

    /**
     * Reassign ticket to a different engineer.
     */
    @Transactional
    public Ticket reassignTicket(String ticketId, String newEngineer, String reason) {
        logger.info("Reassigning ticket {} to {}: {}", ticketId, newEngineer, reason);

        Optional<Ticket> ticketOpt = ticketRepository.findByTicketId(ticketId);
        if (ticketOpt.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found: " + ticketId);
        }

        Ticket ticket = ticketOpt.get();

        String previousEngineer = ticket.getAssignedEngineer();
        ticket.setAssignedEngineer(newEngineer);

        // Update notes
        String note = String.format("Reassigned from %s to %s: %s",
                previousEngineer, newEngineer, reason);
        String existingNotes = ticket.getResolutionNotes();
        ticket.setResolutionNotes(existingNotes != null ? existingNotes + "\n" + note : note);

        // Release previous engineer's workload if applicable
        if (previousEngineer != null) {
            teamAssignmentService.releaseEngineerWorkload(previousEngineer);
        }

        return ticketRepository.save(ticket);
    }
}
