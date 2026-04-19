package com.powergrid.ticketsystem.constants;

/**
 * ============================================================
 * PRIORITY ENUM - Ticket Priority Levels
 * ============================================================
 * 
 * Phase 3: AI-Based Priority Setting
 * 
 * Priority is automatically determined based on:
 * 1. Category of the issue
 * 2. Keywords indicating urgency
 * 3. Impact scope (individual vs. multiple users)
 * 
 * WHY RULE-BASED PRIORITY:
 * - Consistent prioritization
 * - No human bias
 * - Faster processing
 * - Auditable decisions
 */
public enum Priority {

    /**
     * CRITICAL - System-wide outages, security breaches
     * SLA: Respond within 15 minutes
     * Examples: Network down, Security incident, Server crash
     */
    CRITICAL(1, "Critical", 15, "Immediate action required"),

    /**
     * HIGH - Major functionality blocked for user/team
     * SLA: Respond within 1 hour
     * Examples: VPN not working, Cannot login, Email down
     */
    HIGH(2, "High", 60, "Urgent attention needed"),

    /**
     * MEDIUM - Important but workaround exists
     * SLA: Respond within 4 hours
     * Examples: Printer issue, Software slow, Minor bugs
     */
    MEDIUM(3, "Medium", 240, "Normal priority"),

    /**
     * LOW - Minor issues, requests, enhancements
     * SLA: Respond within 24 hours
     * Examples: Password reset, New software request
     */
    LOW(4, "Low", 1440, "Can be scheduled");

    private final int level;
    private final String displayName;
    private final int slaMinutes;
    private final String description;

    Priority(int level, String displayName, int slaMinutes, String description) {
        this.level = level;
        this.displayName = displayName;
        this.slaMinutes = slaMinutes;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getSlaMinutes() {
        return slaMinutes;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Compare priorities (lower level = higher priority)
     */
    public boolean isHigherThan(Priority other) {
        return this.level < other.level;
    }
}
