package com.powergrid.ticketsystem.analytics.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * TOP SLA BREACHING CATEGORY REPORT DTO
 * ============================================================
 * 
 * PHASE 9: REPORTS & ANALYTICS
 * 
 * PURPOSE:
 * ─────────
 * Identifies which categories and sub-categories have the most
 * SLA breaches. Helps management focus improvement efforts.
 * 
 * WHY THIS REPORT MATTERS:
 * ────────────────────────
 * - Pinpoints problem areas in IT support
 * - Guides investment in specific IT infrastructure
 * - Helps identify if SLA targets for certain categories are unrealistic
 * - Supports data-driven decision making
 * 
 * EXAMPLE OUTPUT:
 * ───────────────
 * Top SLA Breaches:
 * 1. NETWORK – VPN (15 breaches, 45% of category)
 * 2. HARDWARE – LAPTOP (10 breaches, 30% of category)
 */
public class TopSlaBreachingReport {

    // ============================================================
    // BREACH DATA
    // ============================================================

    /**
     * Categories ranked by SLA breach count.
     */
    private List<CategoryBreachInfo> topBreachingCategories;

    /**
     * Total SLA breaches in the period.
     */
    private long totalBreaches;

    /**
     * Total tickets in the period.
     */
    private long totalTickets;

    /**
     * Overall breach percentage.
     */
    private double overallBreachPercentage;

    // ============================================================
    // REPORT METADATA
    // ============================================================

    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private LocalDateTime generatedAt;
    private String periodDescription;

    // ============================================================
    // NESTED CLASS FOR CATEGORY BREACH INFO
    // ============================================================

    /**
     * Breach information for a single category.
     */
    public static class CategoryBreachInfo {
        private String category;
        private long breachCount;
        private long totalInCategory;
        private double breachPercentage;
        private List<SubCategoryBreachInfo> subCategories;
        private int rank;

        public CategoryBreachInfo() {
            this.subCategories = new ArrayList<>();
        }

        public CategoryBreachInfo(String category, long breachCount, long totalInCategory) {
            this.category = category;
            this.breachCount = breachCount;
            this.totalInCategory = totalInCategory;
            this.subCategories = new ArrayList<>();
            if (totalInCategory > 0) {
                this.breachPercentage = Math.round(((double) breachCount / totalInCategory) * 10000.0) / 100.0;
            }
        }

        // Getters and Setters
        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public long getBreachCount() {
            return breachCount;
        }

        public void setBreachCount(long breachCount) {
            this.breachCount = breachCount;
        }

        public long getTotalInCategory() {
            return totalInCategory;
        }

        public void setTotalInCategory(long totalInCategory) {
            this.totalInCategory = totalInCategory;
        }

        public double getBreachPercentage() {
            return breachPercentage;
        }

        public void setBreachPercentage(double breachPercentage) {
            this.breachPercentage = breachPercentage;
        }

        public List<SubCategoryBreachInfo> getSubCategories() {
            return subCategories;
        }

        public void setSubCategories(List<SubCategoryBreachInfo> subCategories) {
            this.subCategories = subCategories;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }
    }

    /**
     * Breach information for a sub-category.
     */
    public static class SubCategoryBreachInfo {
        private String subCategory;
        private long breachCount;
        private double breachPercentage;

        public SubCategoryBreachInfo() {
        }

        public SubCategoryBreachInfo(String subCategory, long breachCount) {
            this.subCategory = subCategory;
            this.breachCount = breachCount;
        }

        // Getters and Setters
        public String getSubCategory() {
            return subCategory;
        }

        public void setSubCategory(String subCategory) {
            this.subCategory = subCategory;
        }

        public long getBreachCount() {
            return breachCount;
        }

        public void setBreachCount(long breachCount) {
            this.breachCount = breachCount;
        }

        public double getBreachPercentage() {
            return breachPercentage;
        }

        public void setBreachPercentage(double breachPercentage) {
            this.breachPercentage = breachPercentage;
        }
    }

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public TopSlaBreachingReport() {
        this.topBreachingCategories = new ArrayList<>();
        this.generatedAt = LocalDateTime.now();
    }

    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================

    public List<CategoryBreachInfo> getTopBreachingCategories() {
        return topBreachingCategories;
    }

    public void setTopBreachingCategories(List<CategoryBreachInfo> topBreachingCategories) {
        this.topBreachingCategories = topBreachingCategories;
    }

    public long getTotalBreaches() {
        return totalBreaches;
    }

    public void setTotalBreaches(long totalBreaches) {
        this.totalBreaches = totalBreaches;
    }

    public long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(long totalTickets) {
        this.totalTickets = totalTickets;
    }

    public double getOverallBreachPercentage() {
        return overallBreachPercentage;
    }

    public void setOverallBreachPercentage(double overallBreachPercentage) {
        this.overallBreachPercentage = overallBreachPercentage;
    }

    public LocalDateTime getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDateTime periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDateTime getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDateTime periodEnd) {
        this.periodEnd = periodEnd;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getPeriodDescription() {
        return periodDescription;
    }

    public void setPeriodDescription(String periodDescription) {
        this.periodDescription = periodDescription;
    }
}
