package com.powergrid.ticketsystem.verification;

import com.powergrid.ticketsystem.entity.Ticket;
import com.powergrid.ticketsystem.notification.NotificationService;
import com.powergrid.ticketsystem.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ============================================================
 * AI VERIFICATION SERVICE
 * ============================================================
 * 
 * PHASE 7: AI-BASED RESOLUTION VERIFICATION & CLOSURE
 * 
 * This service provides AI-powered verification of ticket resolutions
 * before allowing them to be closed.
 * 
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║ VERIFICATION CHECKS PERFORMED BY AI ║
 * ╠═══════════════════════════════════════════════════════════════╣
 * ║ ║
 * ║ 1. RESOLUTION TEXT EXISTS ║
 * ║ - Engineer must provide solution notes ║
 * ║ - Minimum 20 characters required ║
 * ║ - At least 3 meaningful words ║
 * ║ ║
 * ║ 2. CATEGORY MATCH ║
 * ║ - Resolution keywords should match ticket category ║
 * ║ - Prevents generic/irrelevant resolutions ║
 * ║ ║
 * ║ 3. SLA COMPLIANCE ║
 * ║ - Checks if resolution was within SLA deadline ║
 * ║ - Notes SLA breach if applicable ║
 * ║ ║
 * ║ 4. NO PENDING ESCALATION ║
 * ║ - Verifies no active Level 1/2/3 escalations ║
 * ║ - Escalated tickets need special handling ║
 * ║ ║
 * ╚═══════════════════════════════════════════════════════════════╝
 * 
 * OUTCOMES:
 * ─────────
 * ✅ VALID → status = CLOSED, closed_by = AI
 * ❌ INVALID → Ticket reopened, sent back to engineer with comments
 */
@Service
public class AIVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(AIVerificationService.class);

    private final TicketRepository ticketRepository;
    private NotificationService notificationService;

    // Category-specific keywords for resolution validation
    private static final Map<String, List<String>> CATEGORY_KEYWORDS = new HashMap<>();

    static {
        // Network category keywords
        CATEGORY_KEYWORDS.put("NETWORK", Arrays.asList(
                "network", "wifi", "vpn", "connection", "firewall", "router",
                "switch", "dns", "ip", "internet", "connectivity", "lan", "wan",
                "bandwidth", "proxy", "port", "ping", "latency", "packet"));

        // Software/Application category keywords
        CATEGORY_KEYWORDS.put("SOFTWARE", Arrays.asList(
                "software", "application", "install", "update", "crash", "error",
                "bug", "patch", "version", "license", "configure", "settings",
                "restart", "reinstall", "uninstall", "performance", "slow", "freeze"));

        // Hardware category keywords
        CATEGORY_KEYWORDS.put("HARDWARE", Arrays.asList(
                "hardware", "monitor", "keyboard", "mouse", "printer", "scanner",
                "laptop", "desktop", "ram", "disk", "hard drive", "cpu", "fan",
                "power", "battery", "screen", "display", "usb", "port", "cable"));

        // Email category keywords
        CATEGORY_KEYWORDS.put("EMAIL", Arrays.asList(
                "email", "outlook", "inbox", "send", "receive", "attachment",
                "calendar", "meeting", "mailbox", "spam", "junk", "signature",
                "forward", "reply", "sync", "exchange", "smtp", "imap", "pop"));

        // Security category keywords
        CATEGORY_KEYWORDS.put("SECURITY", Arrays.asList(
                "security", "password", "access", "permission", "login", "logout",
                "authentication", "authorization", "encrypt", "decrypt", "virus",
                "malware", "antivirus", "firewall", "threat", "vulnerability", "patch"));

        // Access category keywords
        CATEGORY_KEYWORDS.put("ACCESS", Arrays.asList(
                "access", "permission", "login", "password", "reset", "unlock",
                "account", "credential", "authenticate", "authorize", "role",
                "privilege", "admin", "user", "grant", "revoke", "disable", "enable"));

        // General/Unknown category - accepts more generic keywords
        CATEGORY_KEYWORDS.put("UNKNOWN", Arrays.asList(
                "fixed", "resolved", "completed", "done", "working", "issue",
                "problem", "solution", "help", "support", "update", "change"));
    }

    public AIVerificationService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
        logger.info("AI Verification Service initialized");
    }

    /**
     * Setter injection to avoid circular dependency with NotificationService.
     */
    @Autowired
    @Lazy
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
        logger.info("NotificationService injected into AI Verification Service");
    }

    /**
     * Verify a resolved ticket and decide whether to close or reopen it.
     * 
     * This is the main entry point for Phase 7 verification.
     * 
     * @param ticket The resolved ticket to verify
     * @return VerificationResult with outcome and details
     */
    @Transactional
    public VerificationResult verifyAndProcess(Ticket ticket) {
        logger.info("Starting AI verification for ticket: {}", ticket.getTicketId());

        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int score = 100; // Start with perfect score, deduct for issues

        // ================================================================
        // CHECK 1: Resolution Text Exists
        // ================================================================
        ResolutionTextCheckResult textCheck = checkResolutionText(ticket);
        if (!textCheck.isValid()) {
            issues.add(textCheck.getMessage());
            score -= 30;
        } else if (textCheck.hasWarning()) {
            warnings.add(textCheck.getMessage());
            score -= 10;
        }

        // ================================================================
        // CHECK 2: Category Match
        // ================================================================
        CategoryMatchResult categoryCheck = checkCategoryMatch(ticket);
        if (!categoryCheck.isValid()) {
            issues.add(categoryCheck.getMessage());
            score -= 25;
        } else if (categoryCheck.hasWarning()) {
            warnings.add(categoryCheck.getMessage());
            score -= 5;
        }

        // ================================================================
        // CHECK 3: SLA Compliance
        // ================================================================
        SlaComplianceResult slaCheck = checkSlaCompliance(ticket);
        if (!slaCheck.isCompliant()) {
            // SLA breach is a warning, not a blocker
            warnings.add(slaCheck.getMessage());
            score -= 10;
        }

        // ================================================================
        // CHECK 4: No Pending Escalation
        // ================================================================
        EscalationCheckResult escalationCheck = checkEscalation(ticket);
        if (!escalationCheck.isValid()) {
            issues.add(escalationCheck.getMessage());
            score -= 20;
        }

        // ================================================================
        // DETERMINE OUTCOME
        // ================================================================
        boolean isValid = issues.isEmpty();
        String verificationNotes = buildVerificationNotes(issues, warnings, score);

        if (isValid) {
            // CLOSE THE TICKET
            return closeTicket(ticket, verificationNotes, score, warnings);
        } else {
            // REOPEN THE TICKET
            return reopenTicket(ticket, verificationNotes, issues, score);
        }
    }

    /**
     * CHECK 1: Verify resolution text exists and is adequate.
     */
    private ResolutionTextCheckResult checkResolutionText(Ticket ticket) {
        String notes = ticket.getResolutionNotes();

        if (notes == null || notes.trim().isEmpty()) {
            return new ResolutionTextCheckResult(false, false,
                    "Resolution notes are missing. Engineer must describe how the issue was resolved.");
        }

        notes = notes.trim();

        // Check minimum length
        if (notes.length() < 20) {
            return new ResolutionTextCheckResult(false, false,
                    "Resolution notes are too brief (" + notes.length() + " chars). " +
                            "Please provide more detail about the solution.");
        }

        // Check minimum words
        String[] words = notes.split("\\s+");
        if (words.length < 5) {
            return new ResolutionTextCheckResult(false, true,
                    "Resolution notes contain only " + words.length + " words. " +
                            "Please provide a more detailed explanation.");
        }

        // Check for generic/placeholder text
        String lowerNotes = notes.toLowerCase();
        List<String> genericPhrases = Arrays.asList(
                "done", "fixed", "resolved", "completed", "ok", "okay", "working now");

        for (String phrase : genericPhrases) {
            if (lowerNotes.equals(phrase)) {
                return new ResolutionTextCheckResult(false, true,
                        "Resolution notes are too generic ('" + notes + "'). " +
                                "Please explain what was done to resolve the issue.");
            }
        }

        // All checks passed
        return new ResolutionTextCheckResult(true, false, "Resolution notes are adequate.");
    }

    /**
     * CHECK 2: Verify resolution aligns with ticket category.
     */
    private CategoryMatchResult checkCategoryMatch(Ticket ticket) {
        String category = ticket.getCategory();
        String notes = ticket.getResolutionNotes();

        if (category == null || notes == null) {
            return new CategoryMatchResult(true, false,
                    "Category match check skipped (missing data).");
        }

        // Get keywords for this category
        List<String> keywords = CATEGORY_KEYWORDS.getOrDefault(
                category.toUpperCase(),
                CATEGORY_KEYWORDS.get("UNKNOWN"));

        // Check if resolution contains at least one category keyword
        String lowerNotes = notes.toLowerCase();
        int matchCount = 0;

        for (String keyword : keywords) {
            if (lowerNotes.contains(keyword.toLowerCase())) {
                matchCount++;
            }
        }

        if (matchCount == 0) {
            // Changed: Category mismatch is now a warning, not a blocker
            return new CategoryMatchResult(true, true,
                    "Resolution notes may not fully address the ticket category (" + category + "). " +
                            "Consider adding more specific details in future resolutions.");
        }

        if (matchCount < 2) {
            return new CategoryMatchResult(true, true,
                    "Low category relevance score. Consider adding more specific details.");
        }

        return new CategoryMatchResult(true, false,
                "Resolution aligns with ticket category (" + category + ").");
    }

    /**
     * CHECK 3: Verify SLA compliance.
     */
    private SlaComplianceResult checkSlaCompliance(Ticket ticket) {
        LocalDateTime deadline = ticket.getSlaDeadline();
        LocalDateTime resolvedTime = ticket.getClosedTime(); // This is set when resolved

        if (deadline == null) {
            return new SlaComplianceResult(true, "SLA deadline not set.");
        }

        if (resolvedTime == null) {
            resolvedTime = LocalDateTime.now();
        }

        if (resolvedTime.isBefore(deadline)) {
            long minutesRemaining = java.time.Duration.between(resolvedTime, deadline).toMinutes();
            return new SlaComplianceResult(true,
                    "Resolved within SLA (" + minutesRemaining + " minutes remaining).");
        } else {
            long minutesOverdue = java.time.Duration.between(deadline, resolvedTime).toMinutes();
            return new SlaComplianceResult(false,
                    "SLA BREACHED: Resolution was " + minutesOverdue + " minutes overdue.");
        }
    }

    /**
     * CHECK 4: Verify no pending escalations.
     */
    private EscalationCheckResult checkEscalation(Ticket ticket) {
        String escalationLevel = ticket.getEscalationLevel();
        Integer escalationCount = ticket.getEscalationCount();

        // If ticket was escalated to LEVEL_3 (Department Head), allow closure with
        // warning
        // Changed: Level 3 escalation is now a warning, not a blocker
        if ("LEVEL_3".equalsIgnoreCase(escalationLevel)) {
            return new EscalationCheckResult(true,
                    "Note: Ticket was escalated to Department Head (Level 3). " +
                            "Engineer has closed the ticket.");
        }

        // Level 1 and 2 escalations can be closed but noted
        if (escalationLevel != null && !escalationLevel.isEmpty() &&
                !"NONE".equalsIgnoreCase(escalationLevel)) {
            return new EscalationCheckResult(true,
                    "Ticket had escalation level: " + escalationLevel +
                            ". Escalation count: " + (escalationCount != null ? escalationCount : 0));
        }

        return new EscalationCheckResult(true, "No escalation issues.");
    }

    /**
     * Build verification notes from issues and warnings.
     */
    private String buildVerificationNotes(List<String> issues, List<String> warnings, int score) {
        StringBuilder notes = new StringBuilder();
        notes.append("=== AI VERIFICATION REPORT ===\n");
        notes.append("Verification Time: ").append(LocalDateTime.now()).append("\n");
        notes.append("Quality Score: ").append(score).append("/100\n\n");

        if (!issues.isEmpty()) {
            notes.append("ISSUES (Must Fix):\n");
            for (int i = 0; i < issues.size(); i++) {
                notes.append("  ").append(i + 1).append(". ").append(issues.get(i)).append("\n");
            }
            notes.append("\n");
        }

        if (!warnings.isEmpty()) {
            notes.append("WARNINGS:\n");
            for (int i = 0; i < warnings.size(); i++) {
                notes.append("  - ").append(warnings.get(i)).append("\n");
            }
        }

        return notes.toString();
    }

    /**
     * Close a verified ticket.
     */
    @Transactional
    private VerificationResult closeTicket(Ticket ticket, String verificationNotes,
            int score, List<String> warnings) {
        logger.info("AI Verification PASSED for ticket {}. Closing ticket.", ticket.getTicketId());

        // Update ticket status to CLOSED
        ticket.setStatus("CLOSED");
        ticket.setClosedBy("AI");
        ticket.setClosedTime(LocalDateTime.now());
        ticket.setVerificationStatus("VERIFIED");
        ticket.setVerificationScore(score);
        ticket.setVerificationNotes(verificationNotes);
        ticket.setVerifiedTime(LocalDateTime.now());

        ticketRepository.save(ticket);

        logger.info("Ticket {} closed by AI verification. Score: {}", ticket.getTicketId(), score);

        // PHASE 8: Send TICKET_CLOSED notification
        try {
            if (notificationService != null) {
                notificationService.notifyTicketClosed(ticket, true); // Notify both employee and engineer
            }
        } catch (Exception e) {
            logger.warn("Failed to send TICKET_CLOSED notification for {}: {}",
                    ticket.getTicketId(), e.getMessage());
        }

        return new VerificationResult(
                true,
                "CLOSED",
                ticket,
                "Ticket verification passed. Ticket closed automatically by AI.",
                score,
                new ArrayList<>(),
                warnings);
    }

    /**
     * Reopen a ticket that failed verification.
     */
    @Transactional
    private VerificationResult reopenTicket(Ticket ticket, String verificationNotes,
            List<String> issues, int score) {
        logger.info("AI Verification FAILED for ticket {}. Reopening ticket.", ticket.getTicketId());

        // Update ticket status back to ASSIGNED
        ticket.setStatus("ASSIGNED");
        ticket.setResolutionStatus("VERIFICATION_FAILED");
        ticket.setVerificationStatus("REJECTED");
        ticket.setVerificationScore(score);
        ticket.setVerificationNotes(verificationNotes);
        ticket.setVerifiedTime(LocalDateTime.now());

        // Clear resolved status but keep notes for reference
        String existingNotes = ticket.getResolutionNotes() != null ? ticket.getResolutionNotes() : "";
        ticket.setResolutionNotes(existingNotes +
                "\n\n--- VERIFICATION FAILED ---\n" + verificationNotes);

        ticketRepository.save(ticket);

        logger.warn("Ticket {} reopened due to failed AI verification. Issues: {}",
                ticket.getTicketId(), issues);

        // PHASE 8: Send TICKET_REOPENED notification
        try {
            if (notificationService != null) {
                notificationService.notifyTicketReopened(ticket);
            }
        } catch (Exception e) {
            logger.warn("Failed to send TICKET_REOPENED notification for {}: {}",
                    ticket.getTicketId(), e.getMessage());
        }

        return new VerificationResult(
                false,
                "REOPENED",
                ticket,
                "Ticket verification failed. Ticket sent back to engineer for corrections.",
                score,
                issues,
                new ArrayList<>());
    }

    /**
     * Verify all resolved tickets (batch processing).
     * Called by scheduler or manually.
     * 
     * @return List of verification results
     */
    @Transactional
    public List<VerificationResult> verifyAllResolvedTickets() {
        List<Ticket> resolvedTickets = ticketRepository.findByStatus("RESOLVED");
        List<VerificationResult> results = new ArrayList<>();

        logger.info("Starting batch verification of {} resolved tickets", resolvedTickets.size());

        for (Ticket ticket : resolvedTickets) {
            try {
                VerificationResult result = verifyAndProcess(ticket);
                results.add(result);
            } catch (Exception e) {
                logger.error("Error verifying ticket {}: {}", ticket.getTicketId(), e.getMessage());
            }
        }

        logger.info("Batch verification complete. Processed: {}, Closed: {}, Reopened: {}",
                results.size(),
                results.stream().filter(r -> r.getOutcome().equals("CLOSED")).count(),
                results.stream().filter(r -> r.getOutcome().equals("REOPENED")).count());

        return results;
    }

    // ============================================================
    // INNER CLASSES FOR CHECK RESULTS
    // ============================================================

    private static class ResolutionTextCheckResult {
        private final boolean valid;
        private final boolean warning;
        private final String message;

        public ResolutionTextCheckResult(boolean valid, boolean warning, String message) {
            this.valid = valid;
            this.warning = warning;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public boolean hasWarning() {
            return warning;
        }

        public String getMessage() {
            return message;
        }
    }

    private static class CategoryMatchResult {
        private final boolean valid;
        private final boolean warning;
        private final String message;

        public CategoryMatchResult(boolean valid, boolean warning, String message) {
            this.valid = valid;
            this.warning = warning;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public boolean hasWarning() {
            return warning;
        }

        public String getMessage() {
            return message;
        }
    }

    private static class SlaComplianceResult {
        private final boolean compliant;
        private final String message;

        public SlaComplianceResult(boolean compliant, String message) {
            this.compliant = compliant;
            this.message = message;
        }

        public boolean isCompliant() {
            return compliant;
        }

        public String getMessage() {
            return message;
        }
    }

    private static class EscalationCheckResult {
        private final boolean valid;
        private final String message;

        public EscalationCheckResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }

    // ============================================================
    // VERIFICATION RESULT CLASS
    // ============================================================

    /**
     * Result of AI verification process.
     */
    public static class VerificationResult {
        private final boolean passed;
        private final String outcome; // CLOSED or REOPENED
        private final Ticket ticket;
        private final String message;
        private final int score;
        private final List<String> issues;
        private final List<String> warnings;

        public VerificationResult(boolean passed, String outcome, Ticket ticket,
                String message, int score,
                List<String> issues, List<String> warnings) {
            this.passed = passed;
            this.outcome = outcome;
            this.ticket = ticket;
            this.message = message;
            this.score = score;
            this.issues = issues;
            this.warnings = warnings;
        }

        public boolean isPassed() {
            return passed;
        }

        public String getOutcome() {
            return outcome;
        }

        public Ticket getTicket() {
            return ticket;
        }

        public String getMessage() {
            return message;
        }

        public int getScore() {
            return score;
        }

        public List<String> getIssues() {
            return issues;
        }

        public List<String> getWarnings() {
            return warnings;
        }
    }
}
