package com.powergrid.ticketsystem.selfservice;

import com.powergrid.ticketsystem.constants.ResolutionStatus;
import com.powergrid.ticketsystem.entity.KnowledgeBase;
import com.powergrid.ticketsystem.entity.Ticket;
import com.powergrid.ticketsystem.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ============================================================
 * SELF-SERVICE ORCHESTRATOR
 * ============================================================
 * 
 * PHASE 4: SELF-SERVICE RESOLUTION
 * 
 * Main orchestrator that coordinates the entire self-service
 * resolution workflow. This is the primary entry point for
 * triggering self-service on new tickets.
 * 
 * WORKFLOW ORCHESTRATION:
 * ──────────────────────────
 * 
 * 1. Ticket Created (Email/Chatbot)
 * │
 * ▼
 * 2. Ticket Classified (NLP)
 * │
 * ▼
 * ┌────────────────────────────────────┐
 * │ 3. SELF-SERVICE ORCHESTRATOR │
 * │ └─ evaluateAndProcess(ticket) │
 * │ │
 * │ ┌──────────────────────────┐ │
 * │ │ SelfServiceEngine │ │
 * │ │ - Check eligibility │ │
 * │ │ - Priority filter │ │
 * │ │ - Confidence threshold │ │
 * │ │ - Knowledge base lookup │ │
 * │ └──────────────────────────┘ │
 * │ │ │
 * │ ┌───────┴───────┐ │
 * │ ▼ ▼ │
 * │ ELIGIBLE NOT ELIGIBLE │
 * │ │ │ │
 * │ ▼ ▼ │
 * │ ┌────────┐ ┌─────────────┐ │
 * │ │Delivery│ │Fallback │ │
 * │ │Service │ │Assignment │ │
 * │ └────────┘ └─────────────┘ │
 * └────────────────────────────────────┘
 * │
 * ▼
 * 4. Await User Response (YES/NO)
 * │
 * ┌────┴────┐
 * ▼ ▼
 * YES NO
 * │ │
 * ▼ ▼
 * AUTO- ESCALATE
 * CLOSE TO ENGINEER
 */
@Service
public class SelfServiceOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(SelfServiceOrchestrator.class);

    private final SelfServiceEngine selfServiceEngine;
    private final SolutionDeliveryService solutionDeliveryService;
    private final FallbackAssignmentService fallbackAssignmentService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final TicketRepository ticketRepository;

    public SelfServiceOrchestrator(SelfServiceEngine selfServiceEngine,
            SolutionDeliveryService solutionDeliveryService,
            FallbackAssignmentService fallbackAssignmentService,
            KnowledgeBaseService knowledgeBaseService,
            TicketRepository ticketRepository) {
        this.selfServiceEngine = selfServiceEngine;
        this.solutionDeliveryService = solutionDeliveryService;
        this.fallbackAssignmentService = fallbackAssignmentService;
        this.knowledgeBaseService = knowledgeBaseService;
        this.ticketRepository = ticketRepository;
    }

    // ============================================================
    // ORCHESTRATION RESULT DTO
    // ============================================================

    public static class OrchestrationResult {
        private boolean selfServiceTriggered;
        private boolean solutionDelivered;
        private boolean escalatedToEngineer;
        private String status;
        private String message;
        private Ticket ticket;
        private SolutionDeliveryService.ChatbotSolutionResponse chatbotResponse;

        public boolean isSelfServiceTriggered() {
            return selfServiceTriggered;
        }

        public void setSelfServiceTriggered(boolean value) {
            this.selfServiceTriggered = value;
        }

        public boolean isSolutionDelivered() {
            return solutionDelivered;
        }

        public void setSolutionDelivered(boolean value) {
            this.solutionDelivered = value;
        }

        public boolean isEscalatedToEngineer() {
            return escalatedToEngineer;
        }

        public void setEscalatedToEngineer(boolean value) {
            this.escalatedToEngineer = value;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Ticket getTicket() {
            return ticket;
        }

        public void setTicket(Ticket ticket) {
            this.ticket = ticket;
        }

        public SolutionDeliveryService.ChatbotSolutionResponse getChatbotResponse() {
            return chatbotResponse;
        }

        public void setChatbotResponse(SolutionDeliveryService.ChatbotSolutionResponse response) {
            this.chatbotResponse = response;
        }
    }

    // ============================================================
    // MAIN ORCHESTRATION METHOD
    // ============================================================

    /**
     * Evaluate and process a ticket for self-service resolution.
     * This is the main entry point called after ticket classification.
     * 
     * @param ticketId The ticket ID to process
     * @return OrchestrationResult with outcome details
     */
    @Transactional
    public OrchestrationResult evaluateAndProcess(String ticketId) {
        logger.info("═══════════════════════════════════════════════════════");
        logger.info("SELF-SERVICE ORCHESTRATION STARTED");
        logger.info("Ticket: {}", ticketId);
        logger.info("═══════════════════════════════════════════════════════");

        OrchestrationResult result = new OrchestrationResult();

        // 1. Get ticket
        Optional<Ticket> ticketOpt = ticketRepository.findByTicketId(ticketId);
        if (ticketOpt.isEmpty()) {
            result.setStatus("ERROR");
            result.setMessage("Ticket not found: " + ticketId);
            return result;
        }

        Ticket ticket = ticketOpt.get();
        result.setTicket(ticket);

        // 2. Evaluate eligibility
        SelfServiceEngine.EligibilityResult eligibility = selfServiceEngine.evaluateEligibility(ticket);

        logger.info("Eligibility: {} - {}", eligibility.isEligible(), eligibility.getReason());

        if (!eligibility.isEligible()) {
            // Not eligible for self-service
            return handleNotEligible(ticket, eligibility, result);
        }

        // 3. Eligible - Deliver solution
        return deliverSolution(ticket, eligibility, result);
    }

    /**
     * Process a ticket entity directly.
     */
    @Transactional
    public OrchestrationResult evaluateAndProcess(Ticket ticket) {
        return evaluateAndProcess(ticket.getTicketId());
    }

    // ============================================================
    // NOT ELIGIBLE HANDLING
    // ============================================================

    private OrchestrationResult handleNotEligible(Ticket ticket,
            SelfServiceEngine.EligibilityResult eligibility,
            OrchestrationResult result) {
        logger.info("Ticket not eligible for self-service: {}", ticket.getTicketId());

        result.setSelfServiceTriggered(false);
        result.setSolutionDelivered(false);

        // Update ticket resolution status
        ticket.setResolutionStatus(eligibility.getRecommendedStatus().name());
        ticketRepository.save(ticket);

        // Check if we should escalate
        ResolutionStatus status = eligibility.getRecommendedStatus();
        if (status.requiresEngineer()) {
            // Assign to engineer
            Ticket escalated;
            switch (status) {
                case SKIPPED:
                    escalated = fallbackAssignmentService.assignDueToPriority(ticket.getTicketId());
                    break;
                case NOT_APPLICABLE:
                    escalated = fallbackAssignmentService.assignDueToNoSolution(ticket.getTicketId());
                    break;
                default:
                    escalated = fallbackAssignmentService.escalateToEngineer(
                            ticket.getTicketId(), eligibility.getReason());
            }

            result.setEscalatedToEngineer(true);
            result.setTicket(escalated);
            result.setStatus("ESCALATED");
            result.setMessage("Ticket assigned to engineer: " + escalated.getAssignedEngineer());
        } else {
            result.setEscalatedToEngineer(false);
            result.setStatus("NOT_ELIGIBLE");
            result.setMessage(eligibility.getReason());
        }

        return result;
    }

    // ============================================================
    // SOLUTION DELIVERY
    // ============================================================

    private OrchestrationResult deliverSolution(Ticket ticket,
            SelfServiceEngine.EligibilityResult eligibility,
            OrchestrationResult result) {
        logger.info("Delivering solution for ticket: {}", ticket.getTicketId());

        result.setSelfServiceTriggered(true);
        KnowledgeBase solution = eligibility.getSolution();

        // Deliver solution based on ticket source
        SolutionDeliveryService.DeliveryResult delivery = solutionDeliveryService.deliverSolution(ticket, solution);

        if (delivery.isSuccess()) {
            // Update ticket with solution delivery info
            ticket.setResolutionStatus(ResolutionStatus.SOLUTION_SENT.name());
            ticket.setKnowledgeBaseId(solution.getId());
            ticket.setSolutionSentTime(LocalDateTime.now());
            ticket.setStatus("IN_PROGRESS");
            ticketRepository.save(ticket);

            // Record KB usage
            knowledgeBaseService.recordUsage(solution.getId());

            result.setSolutionDelivered(true);
            result.setEscalatedToEngineer(false);
            result.setStatus("SOLUTION_SENT");
            result.setMessage("Solution delivered via " + delivery.getChannel() +
                    ". Awaiting user confirmation.");
            result.setTicket(ticket);

            // For chatbot, include formatted response
            if ("CHATBOT".equalsIgnoreCase(ticket.getSource())) {
                SolutionDeliveryService.ChatbotSolutionResponse chatbotResponse = solutionDeliveryService
                        .formatForChatbot(ticket, solution);
                result.setChatbotResponse(chatbotResponse);
            }

            logger.info("Solution delivered successfully for ticket: {}", ticket.getTicketId());

        } else {
            // Delivery failed - escalate
            logger.error("Solution delivery failed for ticket: {}", ticket.getTicketId());

            Ticket escalated = fallbackAssignmentService.escalateToEngineer(
                    ticket.getTicketId(), "Solution delivery failed: " + delivery.getMessage());

            result.setSolutionDelivered(false);
            result.setEscalatedToEngineer(true);
            result.setStatus("DELIVERY_FAILED");
            result.setMessage("Solution delivery failed. Escalated to: " +
                    escalated.getAssignedEngineer());
            result.setTicket(escalated);
        }

        return result;
    }

    // ============================================================
    // BULK PROCESSING
    // ============================================================

    /**
     * NEW METHOD: Check KB and deliver solution BEFORE classification.
     * This is called immediately after ticket creation, before any classification.
     * 
     * WORKFLOW:
     * 1. Search KB using keywords from issue description
     * 2. If match found → Deliver solution, set status to AWAITING_RESPONSE
     * 3. If no match → Return false (caller should proceed with classification)
     * 
     * @param ticket The newly created ticket (not classified yet)
     * @return OrchestrationResult indicating if solution was sent
     */
    @Transactional
    public OrchestrationResult checkAndDeliverSolution(Ticket ticket) {
        logger.info("════════════════════════════════════════════════════════");
        logger.info("KNOWLEDGE BASE CHECK (PRE-CLASSIFICATION)");
        logger.info("Ticket: {} - Status: {}", ticket.getTicketId(), ticket.getStatus());
        logger.info("════════════════════════════════════════════════════════");

        OrchestrationResult result = new OrchestrationResult();
        result.setTicket(ticket);

        // Search KB using issue description keywords
        String issueText = ticket.getIssueDescription();
        if (issueText == null || issueText.trim().isEmpty()) {
            issueText = ticket.getEmailSubject();
        }

        logger.info("Searching KB for keywords in: {}", issueText);

        // Try to find KB solution by keyword matching
        KnowledgeBase solution = knowledgeBaseService.findByKeywordMatch(issueText);

        if (solution == null) {
            // No KB match found
            logger.info("✗ No Knowledge Base match found for: {}", issueText);
            result.setSelfServiceTriggered(false);
            result.setSolutionDelivered(false);
            result.setStatus("NO_KB_MATCH");
            result.setMessage("No Knowledge Base solution found. Proceeding with classification.");
            return result;
        }

        // KB match found! Deliver solution WITHOUT classification
        logger.info("✓ KB Solution Found!");
        logger.info("  KB ID: {}", solution.getId());
        logger.info("  Title: {}", solution.getIssueTitle());
        logger.info("  Issue Type: {}", solution.getIssueType());

        result.setSelfServiceTriggered(true);

        // Deliver solution to user
        SolutionDeliveryService.DeliveryResult delivery = solutionDeliveryService.deliverSolution(ticket, solution);

        if (delivery.isSuccess()) {
            // Update ticket - NO CLASSIFICATION, NO ASSIGNMENT
            ticket.setResolutionStatus(ResolutionStatus.SOLUTION_SENT.name());
            ticket.setKnowledgeBaseId(solution.getId());
            ticket.setSolutionSentTime(LocalDateTime.now());
            ticket.setStatus("AWAITING_RESPONSE");
            // These remain null - NO engineer assignment
            // ticket.setCategory(null);
            // ticket.setAssignedTeam(null);
            // ticket.setAssignedEngineer(null);
            ticketRepository.save(ticket);

            // Record KB usage
            knowledgeBaseService.recordUsage(solution.getId());

            result.setSolutionDelivered(true);
            result.setEscalatedToEngineer(false);
            result.setStatus("SOLUTION_SENT");
            result.setMessage("Self-service solution sent. Awaiting user YES/NO response.");
            result.setTicket(ticket);

            logger.info("════════════════════════════════════════════════════════");
            logger.info("✓ SELF-SERVICE SOLUTION DELIVERED");
            logger.info("  Ticket: {} → Status: AWAITING_RESPONSE", ticket.getTicketId());
            logger.info("  NO classification performed");
            logger.info("  NO engineer assigned");
            logger.info("  Waiting for user's YES/NO reply");
            logger.info("════════════════════════════════════════════════════════");

        } else {
            // Delivery failed
            logger.error("Solution delivery failed for ticket: {}", ticket.getTicketId());
            result.setSolutionDelivered(false);
            result.setStatus("DELIVERY_FAILED");
            result.setMessage("Solution delivery failed: " + delivery.getMessage());
        }

        return result;
    }

    /**
     * Process all unprocessed tickets for self-service.
     * Called by scheduler or manually.
     */
    @Transactional
    public Map<String, Object> processAllPendingTickets() {
        logger.info("Processing all pending tickets for self-service...");

        List<Ticket> pendingTickets = ticketRepository.findByResolutionStatusOrResolutionStatusIsNull(
                ResolutionStatus.PENDING.name());

        Map<String, Object> summary = new HashMap<>();
        int processed = 0;
        int solutionsSent = 0;
        int escalated = 0;
        int errors = 0;

        for (Ticket ticket : pendingTickets) {
            try {
                // Skip if not classified yet
                if (ticket.getSubCategory() == null) {
                    continue;
                }

                OrchestrationResult result = evaluateAndProcess(ticket);
                processed++;

                if (result.isSolutionDelivered()) {
                    solutionsSent++;
                } else if (result.isEscalatedToEngineer()) {
                    escalated++;
                }
            } catch (Exception e) {
                logger.error("Error processing ticket: {}", ticket.getTicketId(), e);
                errors++;
            }
        }

        summary.put("totalPending", pendingTickets.size());
        summary.put("processed", processed);
        summary.put("solutionsSent", solutionsSent);
        summary.put("escalated", escalated);
        summary.put("errors", errors);

        logger.info("Bulk processing complete: {}", summary);
        return summary;
    }

    // ============================================================
    // STATISTICS
    // ============================================================

    /**
     * Get self-service resolution statistics.
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Resolution status counts
        stats.put("pending", ticketRepository.countByResolutionStatus(ResolutionStatus.PENDING.name()));
        stats.put("solutionsSent", ticketRepository.countByResolutionStatus(ResolutionStatus.SOLUTION_SENT.name()));
        stats.put("awaitingConfirmation",
                ticketRepository.countByResolutionStatus(ResolutionStatus.AWAITING_CONFIRMATION.name()));
        stats.put("selfResolved", ticketRepository.countByResolutionStatus(ResolutionStatus.SELF_RESOLVED.name()));
        stats.put("escalated", ticketRepository.countByResolutionStatus(ResolutionStatus.ESCALATED.name()));
        stats.put("notApplicable", ticketRepository.countByResolutionStatus(ResolutionStatus.NOT_APPLICABLE.name()));
        stats.put("timedOut", ticketRepository.countByResolutionStatus(ResolutionStatus.TIMED_OUT.name()));

        // Calculate success rate
        long selfResolved = (long) stats.get("selfResolved");
        long escalated = (long) stats.get("escalated");
        long total = selfResolved + escalated;
        double successRate = total > 0 ? (double) selfResolved / total * 100 : 0;
        stats.put("selfServiceSuccessRate", String.format("%.1f%%", successRate));

        // Knowledge base stats
        stats.put("knowledgeBase", knowledgeBaseService.getStatistics());

        return stats;
    }
}
