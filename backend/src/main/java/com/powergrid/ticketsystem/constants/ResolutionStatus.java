package com.powergrid.ticketsystem.constants;

/**
 * ============================================================
 * RESOLUTION STATUS ENUM
 * ============================================================
 * 
 * PHASE 4: SELF-SERVICE RESOLUTION
 * 
 * Tracks the self-service resolution lifecycle of a ticket.
 * 
 * RESOLUTION FLOW:
 * 1. PENDING - Ticket created, awaiting self-service check
 * 2. SOLUTION_SENT - Solution delivered to user, awaiting response
 * 3. AWAITING_CONFIRMATION - User prompted to confirm resolution
 * 4. SELF_RESOLVED - User confirmed solution worked (YES)
 * 5. ESCALATED - User said NO, ticket assigned to engineer
 * 6. NOT_APPLICABLE - No self-service solution available
 * 7. TIMED_OUT - User didn't respond within timeout period
 * 
 * WHY TRACK RESOLUTION STATUS:
 * - Clear visibility into resolution pipeline
 * - Metrics and reporting on self-service effectiveness
 * - Audit trail for ticket lifecycle
 * - Enables timeout handling for unresponsive users
 */
public enum ResolutionStatus {

    /**
     * Initial state - ticket just created, not yet checked for self-service.
     */
    PENDING("Pending", "Awaiting self-service eligibility check"),

    /**
     * Solution found and delivered to user.
     */
    SOLUTION_SENT("Solution Sent", "Solution steps sent to user"),

    /**
     * Awaiting user's YES/NO confirmation.
     */
    AWAITING_CONFIRMATION("Awaiting Confirmation", "Waiting for user to confirm resolution"),

    /**
     * User confirmed the solution worked - ticket will be auto-closed.
     */
    SELF_RESOLVED("Self-Resolved", "User confirmed solution worked"),

    /**
     * User indicated solution didn't work - ticket escalated to engineer.
     */
    ESCALATED("Escalated", "Escalated to engineer due to failed self-resolution"),

    /**
     * No self-service solution available for this issue type.
     */
    NOT_APPLICABLE("Not Applicable", "No self-service solution available"),

    /**
     * User didn't respond within the timeout period.
     * Ticket will be escalated to engineer.
     */
    TIMED_OUT("Timed Out", "User response timeout - escalated to engineer"),

    /**
     * Self-service was skipped due to priority or other rules.
     */
    SKIPPED("Skipped", "Self-service skipped due to business rules");

    private final String displayName;
    private final String description;

    ResolutionStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if this status indicates the ticket is still in self-service flow.
     */
    public boolean isInProgress() {
        return this == PENDING || this == SOLUTION_SENT || this == AWAITING_CONFIRMATION;
    }

    /**
     * Check if this status indicates self-service was attempted.
     */
    public boolean wasAttempted() {
        return this == SOLUTION_SENT || this == AWAITING_CONFIRMATION ||
                this == SELF_RESOLVED || this == ESCALATED || this == TIMED_OUT;
    }

    /**
     * Check if this status indicates successful self-resolution.
     */
    public boolean isSuccessful() {
        return this == SELF_RESOLVED;
    }

    /**
     * Check if this status requires engineer intervention.
     */
    public boolean requiresEngineer() {
        return this == ESCALATED || this == NOT_APPLICABLE || this == TIMED_OUT || this == SKIPPED;
    }
}
