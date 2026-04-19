package com.powergrid.ticketsystem.analytics.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * CATEGORY VOLUME REPORT DTO
 * ============================================================
 * 
 * PHASE 9: REPORTS & ANALYTICS
 * 
 * PURPOSE:
 * ─────────
 * Shows ticket distribution across different categories.
 * Helps management understand which IT areas need most attention.
 * 
 * WHY THIS REPORT MATTERS:
 * ────────────────────────
 * - Identifies which IT areas have highest demand
 * - Helps in budget allocation for IT infrastructure
 * - Guides training and hiring decisions
 * - Highlights recurring issues that may need systemic fixes
 * 
 * EXAMPLE OUTPUT:
 * ───────────────
 * NETWORK: 45
 * SOFTWARE: 38
 * HARDWARE: 22
 */
public class CategoryVolumeReport {

    // ============================================================
    // CATEGORY DATA
    // ============================================================

    /**
     * List of categories with their ticket counts.
     */
    private List<CategoryCount> categories;

    /**
     * Total tickets across all categories.
     */
    private long totalTickets;

    // ============================================================
    // REPORT METADATA
    // ============================================================

    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private LocalDateTime generatedAt;
    private String periodDescription;

    // ============================================================
    // NESTED CLASS FOR CATEGORY COUNT
    // ============================================================

    /**
     * Represents a single category with its count and percentage.
     */
    public static class CategoryCount {
        private String category;
        private long count;
        private double percentage;
        private List<SubCategoryCount> subCategories;

        public CategoryCount() {
            this.subCategories = new ArrayList<>();
        }

        public CategoryCount(String category, long count) {
            this.category = category;
            this.count = count;
            this.subCategories = new ArrayList<>();
        }

        // Getters and Setters
        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public double getPercentage() {
            return percentage;
        }

        public void setPercentage(double percentage) {
            this.percentage = percentage;
        }

        public List<SubCategoryCount> getSubCategories() {
            return subCategories;
        }

        public void setSubCategories(List<SubCategoryCount> subCategories) {
            this.subCategories = subCategories;
        }
    }

    /**
     * Represents a sub-category with its count.
     */
    public static class SubCategoryCount {
        private String subCategory;
        private long count;
        private double percentage;

        public SubCategoryCount() {
        }

        public SubCategoryCount(String subCategory, long count) {
            this.subCategory = subCategory;
            this.count = count;
        }

        // Getters and Setters
        public String getSubCategory() {
            return subCategory;
        }

        public void setSubCategory(String subCategory) {
            this.subCategory = subCategory;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public double getPercentage() {
            return percentage;
        }

        public void setPercentage(double percentage) {
            this.percentage = percentage;
        }
    }

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public CategoryVolumeReport() {
        this.categories = new ArrayList<>();
        this.generatedAt = LocalDateTime.now();
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Calculate percentages after counts are set.
     */
    public void calculatePercentages() {
        if (totalTickets > 0) {
            for (CategoryCount cat : categories) {
                cat.setPercentage(Math.round(((double) cat.getCount() / totalTickets) * 10000.0) / 100.0);

                // Calculate sub-category percentages
                for (SubCategoryCount subCat : cat.getSubCategories()) {
                    subCat.setPercentage(Math.round(((double) subCat.getCount() / cat.getCount()) * 10000.0) / 100.0);
                }
            }
        }
    }

    /**
     * Add a category to the report.
     */
    public void addCategory(String category, long count) {
        this.categories.add(new CategoryCount(category, count));
        this.totalTickets += count;
    }

    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================

    public List<CategoryCount> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryCount> categories) {
        this.categories = categories;
    }

    public long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(long totalTickets) {
        this.totalTickets = totalTickets;
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
