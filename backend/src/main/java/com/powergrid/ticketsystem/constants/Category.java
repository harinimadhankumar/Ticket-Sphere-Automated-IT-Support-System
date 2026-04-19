package com.powergrid.ticketsystem.constants;

/**
 * ============================================================
 * CATEGORY ENUM - Ticket Category Classification
 * ============================================================
 * 
 * Phase 2: NLP-Based Classification
 * 
 * WHY ENUM:
 * - Ensures consistency across the system
 * - Prevents typos and invalid values
 * - Easy to maintain and extend
 * - Type-safe at compile time
 * 
 * Categories are determined by rule-based NLP
 * matching keywords in ticket descriptions.
 */
public enum Category {

    /**
     * NETWORK category for all network-related issues.
     * Examples: VPN, WiFi, LAN, Internet connectivity
     */
    NETWORK("Network Issues", "Network Team"),

    /**
     * SOFTWARE category for application/software issues.
     * Examples: Login problems, Installation, Updates, Crashes
     */
    SOFTWARE("Software Issues", "Application Support Team"),

    /**
     * HARDWARE category for physical device issues.
     * Examples: Laptop, Keyboard, Mouse, Monitor, Printer
     */
    HARDWARE("Hardware Issues", "Hardware Support Team"),

    /**
     * ACCESS category for access/permission related issues.
     * Examples: Password reset, Account unlock, Permission requests
     */
    ACCESS("Access & Permissions", "IT Security Team"),

    /**
     * EMAIL category for email related issues.
     * Examples: Outlook, Email not sending, Calendar issues
     */
    EMAIL("Email Issues", "Email Support Team"),

    /**
     * UNKNOWN category when NLP cannot determine category.
     * These tickets require manual review.
     */
    UNKNOWN("Uncategorized", "General IT Support");

    private final String displayName;
    private final String defaultTeam;

    Category(String displayName, String defaultTeam) {
        this.displayName = displayName;
        this.defaultTeam = defaultTeam;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultTeam() {
        return defaultTeam;
    }
}
