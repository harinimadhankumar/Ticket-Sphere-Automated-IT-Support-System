package com.powergrid.ticketsystem.analytics.dto;

import java.time.LocalDateTime;

/**
 * ============================================================
 * ENGINEER PERFORMANCE REPORT DTO
 * ============================================================
 * 
 * PHASE 9: REPORTS & ANALYTICS
 * 
 * PURPOSE:
 * ─────────
 * This DTO provides performance metrics for individual engineers.
 * Used by management to evaluate engineer productivity and efficiency.
 * 
 * WHY THIS REPORT MATTERS:
 * ────────────────────────
 * - Identifies top performers for recognition/promotion
 * - Highlights engineers who may need additional training
 * - Helps in workload balancing decisions
 * - Provides data for performance reviews
 * 
 * WHY ENGINEERS SHOULD NOT SEE THIS:
 * ──────────────────────────────────
 * - Prevents unhealthy competition among team members
 * - Avoids demotivation from public comparisons
 * - Keeps performance discussions confidential
 * - Management uses this for coaching, not public shaming
 * 
 * EXAMPLE OUTPUT:
 * ───────────────
 * Engineer: Mike Johnson
 * Tickets Assigned: 35
 * Tickets Resolved: 33
 * Avg Resolution Time: 2.4 hours
 */
public class EngineerPerformanceReport {

    // ============================================================
    // ENGINEER IDENTIFICATION
    // ============================================================

    /**
     * Engineer's unique ID (e.g., ENG-0001).
     */
    private String engineerId;

    /**
     * Engineer's full name.
     */
    private String engineerName;

    /**
     * Team the engineer belongs to.
     */
    private String team;

    // ============================================================
    // CORE METRICS
    // ============================================================

    /**
     * Total tickets assigned to this engineer.
     */
    private long ticketsAssigned;

    /**
     * Total tickets resolved by this engineer.
     */
    private long ticketsResolved;

    /**
     * Resolution rate (resolved/assigned * 100).
     */
    private double resolutionRate;

    /**
     * Average resolution time in hours.
     */
    private double averageResolutionTimeHours;

    // ============================================================
    // SLA PERFORMANCE
    // ============================================================

    /**
     * Tickets resolved within SLA.
     */
    private long slaMetCount;

    /**
     * Tickets that breached SLA.
     */
    private long slaBreachedCount;

    /**
     * SLA compliance percentage for this engineer.
     */
    private double slaComplianceRate;

    // ============================================================
    // QUALITY METRICS
    // ============================================================

    /**
     * Average verification score from AI verification (0-100).
     */
    private double averageVerificationScore;

    /**
     * Number of tickets that passed AI verification on first attempt.
     */
    private long firstAttemptSuccessCount;

    /**
     * Tickets that were reopened after this engineer's resolution.
     */
    private long reopenedCount;

    // ============================================================
    // CURRENT STATUS
    // ============================================================

    /**
     * Current number of open tickets.
     */
    private long currentOpenTickets;

    /**
     * Engineer's current status (AVAILABLE, BUSY, OFFLINE).
     */
    private String currentStatus;

    // ============================================================
    // REPORT METADATA
    // ============================================================

    /**
     * Report period start.
     */
    private LocalDateTime periodStart;

    /**
     * Report period end.
     */
    private LocalDateTime periodEnd;

    /**
     * When report was generated.
     */
    private LocalDateTime generatedAt;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public EngineerPerformanceReport() {
        this.generatedAt = LocalDateTime.now();
    }

    public EngineerPerformanceReport(String engineerId, String engineerName, String team) {
        this.engineerId = engineerId;
        this.engineerName = engineerName;
        this.team = team;
        this.generatedAt = LocalDateTime.now();
    }

    // ============================================================
    // CALCULATED METHODS
    // ============================================================

    /**
     * Calculate derived metrics after core metrics are set.
     */
    public void calculateDerivedMetrics() {
        // Resolution rate
        if (ticketsAssigned > 0) {
            this.resolutionRate = ((double) ticketsResolved / ticketsAssigned) * 100;
            this.resolutionRate = Math.round(resolutionRate * 100.0) / 100.0;
        }

        // SLA compliance rate
        if (ticketsResolved > 0) {
            this.slaComplianceRate = ((double) slaMetCount / ticketsResolved) * 100;
            this.slaComplianceRate = Math.round(slaComplianceRate * 100.0) / 100.0;
        }
    }

    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================

    public String getEngineerId() {
        return engineerId;
    }

    public void setEngineerId(String engineerId) {
        this.engineerId = engineerId;
    }

    public String getEngineerName() {
        return engineerName;
    }

    public void setEngineerName(String engineerName) {
        this.engineerName = engineerName;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public long getTicketsAssigned() {
        return ticketsAssigned;
    }

    public void setTicketsAssigned(long ticketsAssigned) {
        this.ticketsAssigned = ticketsAssigned;
    }

    public long getTicketsResolved() {
        return ticketsResolved;
    }

    public void setTicketsResolved(long ticketsResolved) {
        this.ticketsResolved = ticketsResolved;
    }

    public double getResolutionRate() {
        return resolutionRate;
    }

    public void setResolutionRate(double resolutionRate) {
        this.resolutionRate = resolutionRate;
    }

    public double getAverageResolutionTimeHours() {
        return averageResolutionTimeHours;
    }

    public void setAverageResolutionTimeHours(double averageResolutionTimeHours) {
        this.averageResolutionTimeHours = averageResolutionTimeHours;
    }

    public long getSlaMetCount() {
        return slaMetCount;
    }

    public void setSlaMetCount(long slaMetCount) {
        this.slaMetCount = slaMetCount;
    }

    public long getSlaBreachedCount() {
        return slaBreachedCount;
    }

    public void setSlaBreachedCount(long slaBreachedCount) {
        this.slaBreachedCount = slaBreachedCount;
    }

    public double getSlaComplianceRate() {
        return slaComplianceRate;
    }

    public void setSlaComplianceRate(double slaComplianceRate) {
        this.slaComplianceRate = slaComplianceRate;
    }

    public double getAverageVerificationScore() {
        return averageVerificationScore;
    }

    public void setAverageVerificationScore(double averageVerificationScore) {
        this.averageVerificationScore = averageVerificationScore;
    }

    public long getFirstAttemptSuccessCount() {
        return firstAttemptSuccessCount;
    }

    public void setFirstAttemptSuccessCount(long firstAttemptSuccessCount) {
        this.firstAttemptSuccessCount = firstAttemptSuccessCount;
    }

    public long getReopenedCount() {
        return reopenedCount;
    }

    public void setReopenedCount(long reopenedCount) {
        this.reopenedCount = reopenedCount;
    }

    public long getCurrentOpenTickets() {
        return currentOpenTickets;
    }

    public void setCurrentOpenTickets(long currentOpenTickets) {
        this.currentOpenTickets = currentOpenTickets;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
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
}
