package com.powergrid.ticketsystem.constants;

/**
 * ============================================================
 * DEPARTMENT CONSTANTS
 * ============================================================
 * 
 * PHASE 10: ROLE-BASED ACCESS CONTROL (RBAC)
 * 
 * Defines all IT departments in the organization.
 * Used for:
 * - Ticket categorization
 * - Team assignment
 * - Access control filtering
 * - Dashboard segmentation
 * 
 * MAPPING TO TICKET CATEGORIES:
 * - NETWORK → Network issues (WiFi, VPN, LAN, Internet)
 * - HARDWARE → Hardware issues (Printer, Monitor, Keyboard, Mouse, Laptop)
 * - SOFTWARE → Software issues (Crash, Installation, Update, Performance)
 * - EMAIL → Email issues (Outlook, Send, Receive, Calendar)
 * - ACCESS → Access issues (Password, Account Unlock, Permission, Login)
 * - GENERAL → Unknown/Other issues
 * 
 * @author IT Service Management Team
 * @version 1.0 - RBAC Implementation
 */
public enum Department {

    /**
     * Network Team - Handles all network-related issues.
     * Categories: NETWORK
     * SubCategories: WIFI, VPN, LAN, INTERNET
     */
    NETWORK("NETWORK", "Network Team", "Network Support",
            new String[] { "NETWORK" },
            new String[] { "WIFI", "VPN", "LAN", "INTERNET" }),

    /**
     * Hardware Support Team - Handles all hardware-related issues.
     * Categories: HARDWARE
     * SubCategories: PRINTER, MONITOR, KEYBOARD, MOUSE, LAPTOP
     */
    HARDWARE("HARDWARE", "Hardware Support Team", "Hardware Support",
            new String[] { "HARDWARE" },
            new String[] { "PRINTER", "MONITOR", "KEYBOARD", "MOUSE", "LAPTOP" }),

    /**
     * Application Support Team - Handles all software-related issues.
     * Categories: SOFTWARE
     * SubCategories: CRASH, INSTALLATION, UPDATE, PERFORMANCE
     */
    SOFTWARE("SOFTWARE", "Application Support Team", "Software Support",
            new String[] { "SOFTWARE" },
            new String[] { "CRASH", "INSTALLATION", "UPDATE", "PERFORMANCE" }),

    /**
     * Email Support Team - Handles all email-related issues.
     * Categories: EMAIL
     * SubCategories: OUTLOOK, EMAIL_SEND, EMAIL_RECEIVE, CALENDAR
     */
    EMAIL("EMAIL", "Email Support Team", "Email Support",
            new String[] { "EMAIL" },
            new String[] { "OUTLOOK", "EMAIL_SEND", "EMAIL_RECEIVE", "CALENDAR" }),

    /**
     * IT Security Team - Handles all access/security-related issues.
     * Categories: ACCESS
     * SubCategories: PASSWORD, ACCOUNT_UNLOCK, PERMISSION, LOGIN
     */
    ACCESS("ACCESS", "IT Security Team", "Access Control & Security",
            new String[] { "ACCESS" },
            new String[] { "PASSWORD", "ACCOUNT_UNLOCK", "PERMISSION", "LOGIN" }),

    /**
     * General IT Support - Handles unknown/miscellaneous issues.
     * Categories: UNKNOWN, GENERAL
     * SubCategories: GENERAL
     */
    GENERAL("GENERAL", "General IT Support", "General Support",
            new String[] { "UNKNOWN", "GENERAL" },
            new String[] { "GENERAL" });

    // ============================================================
    // FIELDS
    // ============================================================

    private final String code;
    private final String teamName;
    private final String shortName;
    private final String[] categories;
    private final String[] subCategories;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    Department(String code, String teamName, String shortName,
            String[] categories, String[] subCategories) {
        this.code = code;
        this.teamName = teamName;
        this.shortName = shortName;
        this.categories = categories;
        this.subCategories = subCategories;
    }

    // ============================================================
    // GETTERS
    // ============================================================

    public String getCode() {
        return code;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getShortName() {
        return shortName;
    }

    public String[] getCategories() {
        return categories;
    }

    public String[] getSubCategories() {
        return subCategories;
    }

    // ============================================================
    // LOOKUP METHODS
    // ============================================================

    /**
     * Find department by code.
     */
    public static Department fromCode(String code) {
        if (code == null)
            return GENERAL;

        for (Department dept : values()) {
            if (dept.code.equalsIgnoreCase(code)) {
                return dept;
            }
        }
        return GENERAL;
    }

    /**
     * Find department by ticket category.
     */
    public static Department fromCategory(String category) {
        if (category == null)
            return GENERAL;

        String upperCategory = category.toUpperCase();

        for (Department dept : values()) {
            for (String cat : dept.categories) {
                if (cat.equals(upperCategory)) {
                    return dept;
                }
            }
        }
        return GENERAL;
    }

    /**
     * Find department by team name.
     */
    public static Department fromTeamName(String teamName) {
        if (teamName == null)
            return GENERAL;

        for (Department dept : values()) {
            if (dept.teamName.equalsIgnoreCase(teamName) ||
                    teamName.toLowerCase().contains(dept.shortName.toLowerCase())) {
                return dept;
            }
        }
        return GENERAL;
    }

    /**
     * Check if this department handles a specific category.
     */
    public boolean handlesCategory(String category) {
        if (category == null)
            return false;

        String upperCategory = category.toUpperCase();
        for (String cat : categories) {
            if (cat.equals(upperCategory)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get all department codes as array.
     */
    public static String[] getAllCodes() {
        String[] codes = new String[values().length];
        int i = 0;
        for (Department dept : values()) {
            codes[i++] = dept.code;
        }
        return codes;
    }

    /**
     * Get all department display names.
     */
    public static String[] getAllTeamNames() {
        String[] names = new String[values().length];
        int i = 0;
        for (Department dept : values()) {
            names[i++] = dept.teamName;
        }
        return names;
    }
}
