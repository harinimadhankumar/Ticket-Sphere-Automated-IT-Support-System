package com.powergrid.ticketsystem.analytics.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * MANAGEMENT SUMMARY REPORT DTO
 * ============================================================
 * 
 * PHASE 9: REPORTS & ANALYTICS
 * 
 * PURPOSE:
 * ─────────
 * Auto-generated text summary for management using RULE-BASED logic.
 * Provides actionable insights without requiring AI/ML.
 * 
 * WHY RULE-BASED SUMMARIES ARE ENOUGH:
 * ────────────────────────────────────
 * 1. Deterministic: Same data = same summary (predictable)
 * 2. Explainable: Rules can be audited and understood
 * 3. No training data required
 * 4. No external dependencies or cloud services
 * 5. Fast and reliable
 * 
 * RULE EXAMPLES:
 * ──────────────
 * - If category X increased by >20%, flag it
 * - If SLA compliance < 80%, recommend action
 * - If engineer workload imbalanced, suggest redistribution
 * 
 * EXAMPLE OUTPUT:
 * ───────────────
 * Summary:
 * - Network-related tickets increased by 20% this month.
 * - VPN issues caused maximum SLA breaches.
 * - Recommendation: Increase network support capacity.
 */
public class ManagementSummary {

    // ============================================================
    // SUMMARY CONTENT
    // ============================================================

    /**
     * Main headline summary.
     */
    private String headline;

    /**
     * List of key findings.
     */
    private List<String> keyFindings;

    /**
     * List of recommendations.
     */
    private List<String> recommendations;

    /**
     * Alerts/Warnings that need attention.
     */
    private List<String> alerts;

    /**
     * Positive highlights.
     */
    private List<String> highlights;

    // ============================================================
    // SUMMARY STATISTICS
    // ============================================================

    /**
     * Overall SLA compliance percentage.
     */
    private double slaCompliance;

    /**
     * Change in ticket volume from previous period.
     */
    private double ticketVolumeChangePercent;

    /**
     * Top performing engineer name.
     */
    private String topPerformer;

    /**
     * Category with most issues.
     */
    private String problemCategory;

    /**
     * Overall system health score (0-100).
     */
    private int healthScore;

    /**
     * Health status (HEALTHY, NEEDS_ATTENTION, CRITICAL).
     */
    private String healthStatus;

    // ============================================================
    // TREND INDICATORS
    // ============================================================

    /**
     * SLA trend (IMPROVING, STABLE, DECLINING).
     */
    private String slaTrend;

    /**
     * Ticket volume trend.
     */
    private String volumeTrend;

    /**
     * Resolution time trend.
     */
    private String resolutionTimeTrend;

    // ============================================================
    // REPORT METADATA
    // ============================================================

    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private LocalDateTime generatedAt;
    private String periodDescription;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public ManagementSummary() {
        this.keyFindings = new ArrayList<>();
        this.recommendations = new ArrayList<>();
        this.alerts = new ArrayList<>();
        this.highlights = new ArrayList<>();
        this.generatedAt = LocalDateTime.now();
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Add a key finding to the summary.
     */
    public void addFinding(String finding) {
        this.keyFindings.add(finding);
    }

    /**
     * Add a recommendation.
     */
    public void addRecommendation(String recommendation) {
        this.recommendations.add(recommendation);
    }

    /**
     * Add an alert.
     */
    public void addAlert(String alert) {
        this.alerts.add(alert);
    }

    /**
     * Add a highlight.
     */
    public void addHighlight(String highlight) {
        this.highlights.add(highlight);
    }

    /**
     * Calculate health score based on metrics.
     * Uses rule-based logic.
     */
    public void calculateHealthScore() {
        int score = 100;

        // Deduct points for low SLA compliance
        if (slaCompliance < 80) {
            score -= 30;
        } else if (slaCompliance < 90) {
            score -= 15;
        } else if (slaCompliance < 95) {
            score -= 5;
        }

        // Deduct points for volume increase
        if (ticketVolumeChangePercent > 30) {
            score -= 20;
        } else if (ticketVolumeChangePercent > 15) {
            score -= 10;
        }

        // Ensure score is within bounds
        this.healthScore = Math.max(0, Math.min(100, score));

        // Determine health status
        if (healthScore >= 80) {
            this.healthStatus = "HEALTHY";
        } else if (healthScore >= 50) {
            this.healthStatus = "NEEDS_ATTENTION";
        } else {
            this.healthStatus = "CRITICAL";
        }
    }

    /**
     * Generate the summary as formatted text.
     */
    public String getFormattedSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════\n");
        sb.append("  MANAGEMENT SUMMARY - ").append(periodDescription).append("\n");
        sb.append("═══════════════════════════════════════════════════════════\n\n");

        if (headline != null) {
            sb.append("📊 ").append(headline).append("\n\n");
        }

        if (!alerts.isEmpty()) {
            sb.append("⚠️ ALERTS:\n");
            for (String alert : alerts) {
                sb.append("   • ").append(alert).append("\n");
            }
            sb.append("\n");
        }

        if (!keyFindings.isEmpty()) {
            sb.append("📋 KEY FINDINGS:\n");
            for (String finding : keyFindings) {
                sb.append("   • ").append(finding).append("\n");
            }
            sb.append("\n");
        }

        if (!highlights.isEmpty()) {
            sb.append("✅ HIGHLIGHTS:\n");
            for (String highlight : highlights) {
                sb.append("   • ").append(highlight).append("\n");
            }
            sb.append("\n");
        }

        if (!recommendations.isEmpty()) {
            sb.append("💡 RECOMMENDATIONS:\n");
            for (String rec : recommendations) {
                sb.append("   • ").append(rec).append("\n");
            }
            sb.append("\n");
        }

        sb.append("───────────────────────────────────────────────────────────\n");
        sb.append("System Health Score: ").append(healthScore).append("/100 (").append(healthStatus).append(")\n");
        sb.append("Generated: ").append(generatedAt).append("\n");

        return sb.toString();
    }

    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public List<String> getKeyFindings() {
        return keyFindings;
    }

    public void setKeyFindings(List<String> keyFindings) {
        this.keyFindings = keyFindings;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }

    public List<String> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<String> alerts) {
        this.alerts = alerts;
    }

    public List<String> getHighlights() {
        return highlights;
    }

    public void setHighlights(List<String> highlights) {
        this.highlights = highlights;
    }

    public double getSlaCompliance() {
        return slaCompliance;
    }

    public void setSlaCompliance(double slaCompliance) {
        this.slaCompliance = slaCompliance;
    }

    public double getTicketVolumeChangePercent() {
        return ticketVolumeChangePercent;
    }

    public void setTicketVolumeChangePercent(double ticketVolumeChangePercent) {
        this.ticketVolumeChangePercent = ticketVolumeChangePercent;
    }

    public String getTopPerformer() {
        return topPerformer;
    }

    public void setTopPerformer(String topPerformer) {
        this.topPerformer = topPerformer;
    }

    public String getProblemCategory() {
        return problemCategory;
    }

    public void setProblemCategory(String problemCategory) {
        this.problemCategory = problemCategory;
    }

    public int getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(int healthScore) {
        this.healthScore = healthScore;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public String getSlaTrend() {
        return slaTrend;
    }

    public void setSlaTrend(String slaTrend) {
        this.slaTrend = slaTrend;
    }

    public String getVolumeTrend() {
        return volumeTrend;
    }

    public void setVolumeTrend(String volumeTrend) {
        this.volumeTrend = volumeTrend;
    }

    public String getResolutionTimeTrend() {
        return resolutionTimeTrend;
    }

    public void setResolutionTimeTrend(String resolutionTimeTrend) {
        this.resolutionTimeTrend = resolutionTimeTrend;
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
