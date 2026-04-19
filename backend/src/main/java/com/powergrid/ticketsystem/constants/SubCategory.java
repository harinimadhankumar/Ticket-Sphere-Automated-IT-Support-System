package com.powergrid.ticketsystem.constants;

/**
 * ============================================================
 * SUB-CATEGORY ENUM - Detailed Ticket Classification
 * ============================================================
 * 
 * Phase 2: NLP-Based Classification
 * 
 * Each SubCategory belongs to a parent Category.
 * This allows for more granular classification and
 * better routing to specialized engineers.
 * 
 * WHY SUB-CATEGORIES:
 * - More precise issue identification
 * - Better team assignment
 * - Improved reporting and analytics
 * - Faster resolution by specialists
 */
public enum SubCategory {

    // ========== NETWORK SUB-CATEGORIES ==========
    VPN(Category.NETWORK, "VPN Connectivity"),
    WIFI(Category.NETWORK, "WiFi/Wireless"),
    LAN(Category.NETWORK, "LAN/Ethernet"),
    INTERNET(Category.NETWORK, "Internet Access"),
    FIREWALL(Category.NETWORK, "Firewall Issues"),

    // ========== SOFTWARE SUB-CATEGORIES ==========
    LOGIN(Category.SOFTWARE, "Login/Authentication"),
    INSTALLATION(Category.SOFTWARE, "Software Installation"),
    UPDATE(Category.SOFTWARE, "Software Updates"),
    CRASH(Category.SOFTWARE, "Application Crash"),
    LICENSE(Category.SOFTWARE, "License Issues"),
    PERFORMANCE(Category.SOFTWARE, "Performance Issues"),

    // ========== HARDWARE SUB-CATEGORIES ==========
    LAPTOP(Category.HARDWARE, "Laptop Issues"),
    DESKTOP(Category.HARDWARE, "Desktop Issues"),
    KEYBOARD(Category.HARDWARE, "Keyboard Issues"),
    MOUSE(Category.HARDWARE, "Mouse Issues"),
    MONITOR(Category.HARDWARE, "Monitor/Display"),
    PRINTER(Category.HARDWARE, "Printer Issues"),
    PERIPHERAL(Category.HARDWARE, "Other Peripherals"),

    // ========== ACCESS SUB-CATEGORIES ==========
    PASSWORD(Category.ACCESS, "Password Reset"),
    ACCOUNT_UNLOCK(Category.ACCESS, "Account Unlock"),
    PERMISSION(Category.ACCESS, "Permission Request"),
    NEW_ACCOUNT(Category.ACCESS, "New Account Request"),

    // ========== EMAIL SUB-CATEGORIES ==========
    OUTLOOK(Category.EMAIL, "Outlook Issues"),
    EMAIL_SEND(Category.EMAIL, "Email Sending"),
    EMAIL_RECEIVE(Category.EMAIL, "Email Receiving"),
    CALENDAR(Category.EMAIL, "Calendar Issues"),

    // ========== UNKNOWN ==========
    GENERAL(Category.UNKNOWN, "General Issue");

    private final Category parentCategory;
    private final String displayName;

    SubCategory(Category parentCategory, String displayName) {
        this.parentCategory = parentCategory;
        this.displayName = displayName;
    }

    public Category getParentCategory() {
        return parentCategory;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get all sub-categories for a given category.
     */
    public static java.util.List<SubCategory> getByCategory(Category category) {
        java.util.List<SubCategory> result = new java.util.ArrayList<>();
        for (SubCategory sub : values()) {
            if (sub.getParentCategory() == category) {
                result.add(sub);
            }
        }
        return result;
    }
}
