package com.powergrid.ticketsystem.analytics.dto;

import java.time.LocalDateTime;

/**
 * ============================================================
 * SLA COMPLIANCE REPORT DTO
 * ============================================================
 * 
 * PHASE 9: REPORTS & ANALYTICS
 * 
 * PURPOSE:
 * ─────────
 * This DTO provides SLA compliance metrics for management.
 * It shows how well the IT team is meeting service level agreements.
 * 
 * WHY THIS REPORT MATTERS:
 * ────────────────────────
 * - SLA compliance is a key performance indicator (KPI) for IT departments
 * - Low compliance rates indicate understaffing or process issues
 * - Management uses this to make resource allocation decisions
 * - Helps identify if SLA targets are realistic
 * 
 * EXAMPLE OUTPUT:
 * ───────────────
 * Total Tickets: 120
 * SLA Met: 98
 * SLA Breached: 22
 * SLA Compliance: 81.6%
 */
public class SlaComplianceReport {

    // ============================================================
    // CORE METRICS
    // ============================================================

    /**
     * Total number of tickets in the reporting period.
     */
    private long totalTickets;

    /**
     * Tickets that were resolved within SLA deadline.
     */
    private long slaMet;

    /**
     * Tickets that breached SLA deadline.
     */
    private long slaBreached;

    /**
     * SLA compliance percentage (0-100).
     * Formula: (slaMet / totalTickets) * 100
     */
    private double slaCompliancePercentage;

    // ============================================================
    // ADDITIONAL INSIGHTS
    // ============================================================

    /**
     * Average time to resolution in hours.
     */
    private double averageResolutionTimeHours;

    /**
     * Average SLA buffer (time before deadline) in hours.
     * Negative value indicates breach.
     */
    private double averageSlaBufferHours;

    /**
     * Number of tickets still open (potential future breaches).
     */
    private long ticketsAtRisk;

    // ============================================================
    // REPORT METADATA
    // ============================================================

    /**
     * Start of reporting period.
     */
    private LocalDateTime periodStart;

    /**
     * End of reporting period.
     */
    private LocalDateTime periodEnd;

    /**
     * Report generation timestamp.
     */
    private LocalDateTime generatedAt;

    /**
     * Time period description (e.g., "Last 7 Days").
     */
    private String periodDescription;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public SlaComplianceReport() {
        this.generatedAt = LocalDateTime.now();
    }

    // ============================================================
    // CALCULATED METHODS
    // ============================================================

    /**
     * Calculate SLA compliance percentage.
     * Called after setting totalTickets, slaMet, slaBreached.
     */
    public void calculateCompliance() {
        if (totalTickets > 0) {
            this.slaCompliancePercentage = ((double) slaMet / totalTickets) * 100;
            // Round to 2 decimal places
            this.slaCompliancePercentage = Math.round(slaCompliancePercentage * 100.0) / 100.0;
        } else {
            this.slaCompliancePercentage = 0.0;
        }
    }

    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================

    public long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(long totalTickets) {
        this.totalTickets = totalTickets;
    }

    public long getSlaMet() {
        return slaMet;
    }

    public void setSlaMet(long slaMet) {
        this.slaMet = slaMet;
    }

    public long getSlaBreached() {
        return slaBreached;
    }

    public void setSlaBreached(long slaBreached) {
        this.slaBreached = slaBreached;
    }

    public double getSlaCompliancePercentage() {
        return slaCompliancePercentage;
    }

    public void setSlaCompliancePercentage(double slaCompliancePercentage) {
        this.slaCompliancePercentage = slaCompliancePercentage;
    }

    public double getAverageResolutionTimeHours() {
        return averageResolutionTimeHours;
    }

    public void setAverageResolutionTimeHours(double averageResolutionTimeHours) {
        this.averageResolutionTimeHours = averageResolutionTimeHours;
    }

    public double getAverageSlaBufferHours() {
        return averageSlaBufferHours;
    }

    public void setAverageSlaBufferHours(double averageSlaBufferHours) {
        this.averageSlaBufferHours = averageSlaBufferHours;
    }

    public long getTicketsAtRisk() {
        return ticketsAtRisk;
    }

    public void setTicketsAtRisk(long ticketsAtRisk) {
        this.ticketsAtRisk = ticketsAtRisk;
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
