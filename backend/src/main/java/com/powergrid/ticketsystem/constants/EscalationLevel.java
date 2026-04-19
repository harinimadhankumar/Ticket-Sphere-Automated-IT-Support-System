package com.powergrid.ticketsystem.constants;

/**
 * ============================================================
 * ESCALATION LEVEL ENUM
 * ============================================================
 * 
 * PHASE 5: SLA MONITORING & AUTO ESCALATION
 * 
 * Defines the escalation hierarchy for tickets that breach SLA.
 * Each level represents a higher authority in the support chain.
 * 
 * ESCALATION HIERARCHY:
 * ─────────────────────
 * LEVEL_1 → Senior Engineer (First escalation)
 * LEVEL_2 → Team Lead / Manager (Second escalation)
 * LEVEL_3 → Department Head / Director (Critical escalation)
 * 
 * WHY ESCALATION LEVELS ARE NEEDED:
 * ─────────────────────────────────
 * 1. Accountability: Ensures someone senior takes ownership
 * 2. Visibility: Management becomes aware of delayed tickets
 * 3. Resolution: Higher authority can allocate more resources
 * 4. SLA Compliance: Helps meet organizational SLA targets
 * 
 * ESCALATION TRIGGERS:
 * ────────────────────
 * - LEVEL_1: SLA breached (0-25% overtime)
 * - LEVEL_2: SLA breached by 50% or more
 * - LEVEL_3: SLA breached by 100% or more (double the SLA time)
 */
public enum EscalationLevel {

    /**
     * No escalation - ticket is within SLA.
     */
    NONE("No Escalation", "Ticket is within SLA limits", 0),

    /**
     * Level 1 Escalation - Senior Engineer.
     * Triggered when SLA is first breached.
     */
    LEVEL_1("Level 1 - Senior Engineer", "SLA breached, escalated to senior engineer", 1),

    /**
     * Level 2 Escalation - Team Lead/Manager.
     * Triggered when SLA is breached by 50% or more.
     */
    LEVEL_2("Level 2 - Team Lead", "SLA significantly breached, escalated to team lead", 2),

    /**
     * Level 3 Escalation - Department Head.
     * Triggered when SLA is breached by 100% (double the time).
     */
    LEVEL_3("Level 3 - Department Head", "Critical SLA breach, escalated to department head", 3);

    private final String displayName;
    private final String description;
    private final int severity;

    EscalationLevel(String displayName, String description, int severity) {
        this.displayName = displayName;
        this.description = description;
        this.severity = severity;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getSeverity() {
        return severity;
    }

    /**
     * Get the next escalation level.
     * Used when re-escalating a ticket that's still breaching SLA.
     * 
     * @return Next higher escalation level, or LEVEL_3 if already at max
     */
    public EscalationLevel getNextLevel() {
        switch (this) {
            case NONE:
                return LEVEL_1;
            case LEVEL_1:
                return LEVEL_2;
            case LEVEL_2:
            case LEVEL_3:
                return LEVEL_3; // Max level
            default:
                return LEVEL_1;
        }
    }

    /**
     * Check if this is a critical escalation level.
     * 
     * @return true if LEVEL_2 or LEVEL_3
     */
    public boolean isCritical() {
        return this == LEVEL_2 || this == LEVEL_3;
    }

    /**
     * Check if this level requires management notification.
     * 
     * @return true if LEVEL_2 or LEVEL_3
     */
    public boolean requiresManagementNotification() {
        return this == LEVEL_2 || this == LEVEL_3;
    }
}
