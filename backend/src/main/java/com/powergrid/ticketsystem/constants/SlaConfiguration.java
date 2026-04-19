package com.powergrid.ticketsystem.constants;

/**
 * ============================================================
 * SLA CONFIGURATION - Service Level Agreement Constants
 * ============================================================
 * 
 * PHASE 5: SLA MONITORING & AUTO ESCALATION
 * 
 * Defines the SLA time limits for each ticket priority level.
 * These are enterprise-standard SLA thresholds for IT support.
 * 
 * SLA TIME RULES (FIXED):
 * ───────────────────────
 * | Priority | SLA Time | Description |
 * |-----------|----------|--------------------------------|
 * | CRITICAL | 2 hours | System down, security breach |
 * | HIGH | 4 hours | Major impact, VIP users |
 * | MEDIUM | 8 hours | Normal business impact |
 * | LOW | 24 hours | Minor issues, general queries |
 * 
 * WHY THESE SLA TIMES:
 * ────────────────────
 * - CRITICAL: Business-critical systems must be restored immediately
 * - HIGH: Important issues affecting productivity
 * - MEDIUM: Standard issues with reasonable resolution time
 * - LOW: Non-urgent issues that can be addressed in a day
 * 
 * WHY RULE-BASED SLA (NOT ML):
 * ────────────────────────────
 * 1. Predictable: Same priority always gets same SLA
 * 2. Auditable: Easy to explain and verify compliance
 * 3. Simple: No training data or model maintenance needed
 * 4. Enterprise Standard: Industry-standard approach for IT SLAs
 */
public final class SlaConfiguration {

    // ============================================================
    // SLA TIME LIMITS (in hours)
    // ============================================================

    /**
     * SLA time for CRITICAL priority tickets.
     * Example: Server down, security breach, data loss
     */
    public static final int CRITICAL_SLA_HOURS = 2;

    /**
     * SLA time for HIGH priority tickets.
     * Example: VPN not working, email server issues
     */
    public static final int HIGH_SLA_HOURS = 4;

    /**
     * SLA time for MEDIUM priority tickets.
     * Example: Slow application, printer not working
     */
    public static final int MEDIUM_SLA_HOURS = 8;

    /**
     * SLA time for LOW priority tickets.
     * Example: General queries, software installation requests
     */
    public static final int LOW_SLA_HOURS = 24;

    /**
     * Default SLA for unknown priority (fallback).
     */
    public static final int DEFAULT_SLA_HOURS = 24;

    // ============================================================
    // ESCALATION THRESHOLDS (percentage of SLA time)
    // ============================================================

    /**
     * Percentage of SLA time after which LEVEL_1 escalation triggers.
     * 100% = SLA time exceeded
     */
    public static final double LEVEL_1_THRESHOLD_PERCENT = 100.0;

    /**
     * Percentage of SLA time after which LEVEL_2 escalation triggers.
     * 150% = 1.5x the SLA time
     */
    public static final double LEVEL_2_THRESHOLD_PERCENT = 150.0;

    /**
     * Percentage of SLA time after which LEVEL_3 escalation triggers.
     * 200% = 2x the SLA time (double)
     */
    public static final double LEVEL_3_THRESHOLD_PERCENT = 200.0;

    // ============================================================
    // WARNING THRESHOLDS (for proactive alerts)
    // ============================================================

    /**
     * Percentage of SLA time to send warning notification.
     * 75% = Alert before SLA breach
     */
    public static final double WARNING_THRESHOLD_PERCENT = 75.0;

    // ============================================================
    // SCHEDULER CONFIGURATION
    // ============================================================

    /**
     * How often the SLA monitoring scheduler runs (in minutes).
     */
    public static final int SCHEDULER_INTERVAL_MINUTES = 10;

    /**
     * Cron expression for SLA monitoring.
     * Runs every 10 minutes.
     */
    public static final String SLA_MONITORING_CRON = "0 */10 * * * *";

    // ============================================================
    // ESCALATION CONTACTS
    // ============================================================

    /**
     * Default senior engineer for Level 1 escalation.
     */
    public static final String LEVEL_1_ESCALATION_CONTACT = "Senior Engineer";

    /**
     * Default team lead for Level 2 escalation.
     */
    public static final String LEVEL_2_ESCALATION_CONTACT = "Team Lead";

    /**
     * Default department head for Level 3 escalation.
     */
    public static final String LEVEL_3_ESCALATION_CONTACT = "Department Head";

    // ============================================================
    // UTILITY METHODS
    // ============================================================

    /**
     * Get SLA hours for a given priority.
     * 
     * @param priority Ticket priority (CRITICAL, HIGH, MEDIUM, LOW)
     * @return SLA time in hours
     */
    public static int getSlaHoursForPriority(String priority) {
        if (priority == null) {
            return DEFAULT_SLA_HOURS;
        }

        switch (priority.toUpperCase()) {
            case "CRITICAL":
                return CRITICAL_SLA_HOURS;
            case "HIGH":
                return HIGH_SLA_HOURS;
            case "MEDIUM":
                return MEDIUM_SLA_HOURS;
            case "LOW":
                return LOW_SLA_HOURS;
            default:
                return DEFAULT_SLA_HOURS;
        }
    }

    /**
     * Get SLA hours in minutes for a given priority.
     * 
     * @param priority Ticket priority
     * @return SLA time in minutes
     */
    public static int getSlaMinutesForPriority(String priority) {
        return getSlaHoursForPriority(priority) * 60;
    }

    /**
     * Get escalation contact for a given level.
     * 
     * @param level Escalation level
     * @return Contact name/role
     */
    public static String getEscalationContact(EscalationLevel level) {
        switch (level) {
            case LEVEL_1:
                return LEVEL_1_ESCALATION_CONTACT;
            case LEVEL_2:
                return LEVEL_2_ESCALATION_CONTACT;
            case LEVEL_3:
                return LEVEL_3_ESCALATION_CONTACT;
            default:
                return LEVEL_1_ESCALATION_CONTACT;
        }
    }

    // Private constructor to prevent instantiation
    private SlaConfiguration() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
