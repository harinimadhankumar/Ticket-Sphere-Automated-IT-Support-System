package com.powergrid.ticketsystem.selfservice;

import com.powergrid.ticketsystem.constants.Priority;
import com.powergrid.ticketsystem.constants.ResolutionStatus;
import com.powergrid.ticketsystem.entity.KnowledgeBase;
import com.powergrid.ticketsystem.entity.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * ============================================================
 * SELF-SERVICE ENGINE
 * ============================================================
 * 
 * PHASE 4: SELF-SERVICE RESOLUTION
 * 
 * Core decision engine that determines whether a ticket qualifies
 * for automatic self-service resolution.
 * 
 * RULE-BASED LOGIC (NO MACHINE LEARNING):
 * This engine uses deterministic, explainable rules to decide
 * if a ticket can be auto-resolved. Rules are based on:
 * 
 * 1. Issue Type Matching
 * - Ticket's subCategory must match a knowledge base entry
 * - Knowledge base entry must be active
 * 
 * 2. Auto-Closable Check
 * - Only auto-closable issues qualify for full automation
 * - Non-auto-closable issues still get solutions but need engineer
 * 
 * 3. Priority Filter
 * - CRITICAL tickets always go to engineers (safety)
 * - HIGH priority may be filtered (configurable)
 * - MEDIUM and LOW priorities qualify for self-service
 * 
 * 4. Repeat Issue Check
 * - If same user reported same issue recently that wasn't resolved,
 * escalate to engineer (prevent frustration)
 * 
 * 5. Confidence Threshold
 * - If NLP classification confidence < threshold, skip self-service
 * 
 * WHY RULE-BASED AI (NOT ML):
 * ─────────────────────────────
 * 1. EXPLAINABILITY: Every decision can be traced to a specific rule
 * 2. PREDICTABILITY: Same inputs always produce same outputs
 * 3. AUDITABILITY: Clear audit trail for compliance
 * 4. NO TRAINING DATA: Works immediately without historical data
 * 5. EASY MAINTENANCE: Rules can be updated without retraining
 * 6. TRANSPARENCY: Users/admins understand why decisions were made
 * 7. CONTROL: IT team has full control over resolution criteria
 * 8. SAFETY: No risk of ML model making unexpected decisions
 * 
 * For an IT helpdesk serving critical infrastructure (POWERGRID),
 * predictability and control are more important than adaptive learning.
 */
@Service
public class SelfServiceEngine {

    private static final Logger logger = LoggerFactory.getLogger(SelfServiceEngine.class);

    private final KnowledgeBaseService knowledgeBaseService;

    // ============================================================
    // CONFIGURABLE THRESHOLDS
    // ============================================================

    @Value("${selfservice.confidence.threshold:0.6}")
    private double confidenceThreshold;

    @Value("${selfservice.skip.critical:true}")
    private boolean skipCriticalPriority;

    @Value("${selfservice.skip.high:false}")
    private boolean skipHighPriority;

    @Value("${selfservice.enabled:true}")
    private boolean selfServiceEnabled;

    public SelfServiceEngine(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    // ============================================================
    // ELIGIBILITY CHECK RESULT
    // ============================================================

    /**
     * Result object containing eligibility decision and details.
     */
    public static class EligibilityResult {
        private boolean eligible;
        private String reason;
        private KnowledgeBase solution;
        private boolean autoClosable;
        private ResolutionStatus recommendedStatus;

        public EligibilityResult(boolean eligible, String reason) {
            this.eligible = eligible;
            this.reason = reason;
            this.recommendedStatus = eligible ? ResolutionStatus.PENDING : ResolutionStatus.NOT_APPLICABLE;
        }

        public boolean isEligible() {
            return eligible;
        }

        public String getReason() {
            return reason;
        }

        public KnowledgeBase getSolution() {
            return solution;
        }

        public void setSolution(KnowledgeBase solution) {
            this.solution = solution;
            this.autoClosable = solution != null && Boolean.TRUE.equals(solution.getAutoClosable());
        }

        public boolean isAutoClosable() {
            return autoClosable;
        }

        public ResolutionStatus getRecommendedStatus() {
            return recommendedStatus;
        }

        public void setRecommendedStatus(ResolutionStatus status) {
            this.recommendedStatus = status;
        }

        @Override
        public String toString() {
            return "EligibilityResult{" +
                    "eligible=" + eligible +
                    ", reason='" + reason + '\'' +
                    ", autoClosable=" + autoClosable +
                    ", recommendedStatus=" + recommendedStatus +
                    '}';
        }
    }

    // ============================================================
    // MAIN ELIGIBILITY CHECK METHOD
    // ============================================================

    /**
     * Evaluate whether a ticket is eligible for self-service resolution.
     * 
     * This is the main entry point for the self-service engine.
     * It applies all rules in sequence and returns a decision.
     * 
     * @param ticket The ticket to evaluate
     * @return EligibilityResult containing decision and details
     */
    public EligibilityResult evaluateEligibility(Ticket ticket) {
        logger.info("Evaluating self-service eligibility for ticket: {}", ticket.getTicketId());

        // ─────────────────────────────────────────────────────────
        // RULE 0: Check if self-service is globally enabled
        // ─────────────────────────────────────────────────────────
        if (!selfServiceEnabled) {
            logger.debug("Self-service is disabled globally");
            EligibilityResult result = new EligibilityResult(false, "Self-service resolution is disabled");
            result.setRecommendedStatus(ResolutionStatus.SKIPPED);
            return result;
        }

        // ─────────────────────────────────────────────────────────
        // RULE 1: Ticket must be classified
        // ─────────────────────────────────────────────────────────
        if (ticket.getSubCategory() == null || ticket.getSubCategory().isEmpty()) {
            logger.debug("Ticket not classified - skipping self-service");
            EligibilityResult result = new EligibilityResult(false, "Ticket not yet classified");
            result.setRecommendedStatus(ResolutionStatus.NOT_APPLICABLE);
            return result;
        }

        // ─────────────────────────────────────────────────────────
        // RULE 2: Check priority filter (CRITICAL always to engineer)
        // ─────────────────────────────────────────────────────────
        EligibilityResult priorityResult = checkPriorityRule(ticket);
        if (priorityResult != null) {
            return priorityResult;
        }

        // ─────────────────────────────────────────────────────────
        // RULE 3: Check confidence threshold
        // ─────────────────────────────────────────────────────────
        EligibilityResult confidenceResult = checkConfidenceRule(ticket);
        if (confidenceResult != null) {
            return confidenceResult;
        }

        // ─────────────────────────────────────────────────────────
        // RULE 4: Look up solution in knowledge base
        // ─────────────────────────────────────────────────────────
        Optional<KnowledgeBase> solution = knowledgeBaseService.findSolutionByIssueType(
                ticket.getSubCategory());

        if (solution.isEmpty()) {
            logger.info("No knowledge base entry found for: {}", ticket.getSubCategory());
            EligibilityResult result = new EligibilityResult(false,
                    "No self-service solution available for issue type: " + ticket.getSubCategory());
            result.setRecommendedStatus(ResolutionStatus.NOT_APPLICABLE);
            return result;
        }

        // ─────────────────────────────────────────────────────────
        // ELIGIBLE - Solution found!
        // ─────────────────────────────────────────────────────────
        KnowledgeBase kb = solution.get();
        logger.info("Solution found! KB ID: {}, Title: {}, Auto-closable: {}",
                kb.getId(), kb.getIssueTitle(), kb.getAutoClosable());

        EligibilityResult result = new EligibilityResult(true,
                "Solution available: " + kb.getIssueTitle());
        result.setSolution(kb);
        result.setRecommendedStatus(ResolutionStatus.SOLUTION_SENT);

        return result;
    }

    // ============================================================
    // INDIVIDUAL RULE CHECKS
    // ============================================================

    /**
     * RULE 2: Priority Filter
     * Critical tickets always go to engineers for safety.
     */
    private EligibilityResult checkPriorityRule(Ticket ticket) {
        String priority = ticket.getPriority();

        if (priority == null) {
            // No priority set - allow self-service
            return null;
        }

        // CRITICAL tickets always skip self-service
        if (skipCriticalPriority && Priority.CRITICAL.name().equalsIgnoreCase(priority)) {
            logger.info("CRITICAL priority ticket - skipping self-service");
            EligibilityResult result = new EligibilityResult(false,
                    "Critical priority tickets require immediate engineer attention");
            result.setRecommendedStatus(ResolutionStatus.SKIPPED);
            return result;
        }

        // HIGH priority check (configurable)
        if (skipHighPriority && Priority.HIGH.name().equalsIgnoreCase(priority)) {
            logger.info("HIGH priority ticket - skipping self-service (configured)");
            EligibilityResult result = new EligibilityResult(false,
                    "High priority tickets are configured to skip self-service");
            result.setRecommendedStatus(ResolutionStatus.SKIPPED);
            return result;
        }

        return null; // Priority check passed
    }

    /**
     * RULE 3: Confidence Threshold
     * Low-confidence classifications may lead to wrong solutions.
     */
    private EligibilityResult checkConfidenceRule(Ticket ticket) {
        Double confidence = ticket.getConfidenceScore();

        if (confidence == null) {
            // No confidence score - assume acceptable
            return null;
        }

        if (confidence < confidenceThreshold) {
            logger.info("Low confidence ({}) below threshold ({}) - skipping self-service",
                    confidence, confidenceThreshold);
            EligibilityResult result = new EligibilityResult(false,
                    String.format("Classification confidence (%.2f) below threshold (%.2f)",
                            confidence, confidenceThreshold));
            result.setRecommendedStatus(ResolutionStatus.SKIPPED);
            return result;
        }

        return null; // Confidence check passed
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Check if a specific issue type has a self-service solution.
     */
    public boolean hasSolutionFor(String issueType) {
        return knowledgeBaseService.hasSolution(issueType);
    }

    /**
     * Get solution for an issue type without full eligibility check.
     */
    public Optional<KnowledgeBase> getSolutionFor(String issueType) {
        return knowledgeBaseService.findSolutionByIssueType(issueType);
    }

    // ============================================================
    // CONFIGURATION GETTERS
    // ============================================================

    public boolean isSelfServiceEnabled() {
        return selfServiceEnabled;
    }

    public double getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public boolean isSkipCriticalPriority() {
        return skipCriticalPriority;
    }

    public boolean isSkipHighPriority() {
        return skipHighPriority;
    }
}
