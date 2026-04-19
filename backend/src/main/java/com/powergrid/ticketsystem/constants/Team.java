package com.powergrid.ticketsystem.constants;

/**
 * ============================================================
 * TEAM ENUM - IT Support Teams
 * ============================================================
 * 
 * Phase 3: Intelligent Team Assignment
 * 
 * Each team handles specific categories of issues.
 * Teams are mapped to categories for automatic assignment.
 * 
 * WHY TEAM ENUM:
 * - Clear team definitions
 * - Easy category-to-team mapping
 * - Prevents invalid team assignments
 */
public enum Team {

    NETWORK_TEAM("Network Team", "NET", Category.NETWORK),
    APPLICATION_SUPPORT("Application Support Team", "APP", Category.SOFTWARE),
    HARDWARE_SUPPORT("Hardware Support Team", "HW", Category.HARDWARE),
    IT_SECURITY("IT Security Team", "SEC", Category.ACCESS),
    EMAIL_SUPPORT("Email Support Team", "EMAIL", Category.EMAIL),
    GENERAL_SUPPORT("General IT Support", "GEN", Category.UNKNOWN);

    private final String teamName;
    private final String teamCode;
    private final Category handlesCategory;

    Team(String teamName, String teamCode, Category handlesCategory) {
        this.teamName = teamName;
        this.teamCode = teamCode;
        this.handlesCategory = handlesCategory;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getTeamCode() {
        return teamCode;
    }

    public Category getHandlesCategory() {
        return handlesCategory;
    }

    /**
     * Get the team that handles a specific category.
     */
    public static Team getTeamForCategory(Category category) {
        for (Team team : values()) {
            if (team.getHandlesCategory() == category) {
                return team;
            }
        }
        return GENERAL_SUPPORT;
    }
}
