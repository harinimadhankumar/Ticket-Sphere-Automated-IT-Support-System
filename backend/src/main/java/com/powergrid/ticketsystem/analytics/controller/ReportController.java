package com.powergrid.ticketsystem.analytics.controller;

import com.powergrid.ticketsystem.analytics.dto.*;
import com.powergrid.ticketsystem.analytics.service.AnalyticsService;
import com.powergrid.ticketsystem.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ============================================================
 * REPORT CONTROLLER
 * ============================================================
 * 
 * PHASE 9: REPORTS & ANALYTICS API
 * 
 * PURPOSE:
 * ─────────
 * REST API endpoints for management reports.
 * All endpoints are READ-ONLY and intended for HOD/Management.
 * 
 * ACCESS CONTROL:
 * ────────────────
 * - All endpoints require HOD/Admin role
 * - Role validation via roleId header
 * - Returns 403 Forbidden for unauthorized access
 * 
 * ROLE DEFINITIONS:
 * - roleId: HOD -> Full access to all reports
 * - roleId: ADMIN -> Full access to all reports
 * - roleId: ENGINEER-> Denied access (should use engineer dashboard)
 * - roleId: USER -> Denied access
 * 
 * API ENDPOINTS:
 * ─────────────────────────────────────────────────────
 * GET /api/reports/sla-compliance - SLA metrics
 * GET /api/reports/engineer-performance - All engineers
 * GET /api/reports/engineer/{id} - Single engineer
 * GET /api/reports/category-volume - Category breakdown
 * GET /api/reports/sla-breaches - Breach analysis
 * GET /api/reports/peak-hours - Hourly distribution
 * GET /api/reports/management-summary - Executive summary
 * 
 * QUERY PARAMETERS:
 * ─────────────────────────────────────────────────────
 * period: today, week, month, custom (default: week)
 * startDate: ISO datetime (for custom period)
 * endDate: ISO datetime (for custom period)
 * 
 * RESPONSE FORMAT:
 * ─────────────────
 * All responses wrapped in ApiResponse for consistency.
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    private final AnalyticsService analyticsService;

    // ==============================
    // Role-based access constants
    // ==============================
    private static final String ROLE_HOD = "HOD";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ACCESS_DENIED_MESSAGE = "Access denied. Only HOD and Admin can access reports.";

    public ReportController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
        logger.info("╔══════════════════════════════════════════════════════════╗");
        logger.info("║         REPORT CONTROLLER INITIALIZED (Phase 9)         ║");
        logger.info("║         Management Reports API Ready                    ║");
        logger.info("╚══════════════════════════════════════════════════════════╝");
    }

    // ============================================================
    // ACCESS CONTROL HELPER
    // ============================================================

    /**
     * Check if the provided role has access to reports.
     * 
     * @param roleId The role identifier from request header
     * @return true if authorized (HOD or ADMIN), false otherwise
     */
    private boolean isAuthorized(String roleId) {
        if (roleId == null || roleId.isEmpty()) {
            return false;
        }
        String normalizedRole = roleId.toUpperCase().trim();
        return ROLE_HOD.equals(normalizedRole) || ROLE_ADMIN.equals(normalizedRole);
    }

    // ============================================================
    // TIME PERIOD HELPER
    // ============================================================

    /**
     * Parse time period from request parameters.
     * 
     * @param period    Period name: today, week, month, custom
     * @param startDate Custom start date (ISO format)
     * @param endDate   Custom end date (ISO format)
     * @return Array with [startDateTime, endDateTime]
     */
    private LocalDateTime[] parsePeriod(String period, String startDate, String endDate) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start;

        if (period == null) {
            period = "week";
        }

        switch (period.toLowerCase()) {
            case "today":
                start = analyticsService.getStartOfToday();
                break;
            case "month":
                start = analyticsService.getStartOfLast30Days();
                break;
            case "custom":
                if (startDate != null && endDate != null) {
                    start = LocalDateTime.parse(startDate);
                    end = LocalDateTime.parse(endDate);
                } else {
                    start = analyticsService.getStartOfLast7Days();
                }
                break;
            case "week":
            default:
                start = analyticsService.getStartOfLast7Days();
                break;
        }

        return new LocalDateTime[] { start, end };
    }

    // ============================================================
    // ENDPOINT 1: SLA COMPLIANCE REPORT
    // ============================================================

    /**
     * Get SLA Compliance Report.
     * 
     * Shows:
     * - Total tickets in period
     * - SLA met vs breached count
     * - SLA compliance percentage
     * - Tickets at risk
     * 
     * @param roleId    Required header for role-based access
     * @param period    Time period: today, week, month, custom
     * @param startDate Start date for custom period
     * @param endDate   End date for custom period
     * @return SlaComplianceReport
     */
    @GetMapping("/sla-compliance")
    public ResponseEntity<ApiResponse<SlaComplianceReport>> getSlaComplianceReport(
            @RequestHeader(value = "roleId", required = false) String roleId,
            @RequestParam(required = false, defaultValue = "week") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        logger.info("SLA Compliance Report requested by role: {}", roleId);

        if (!isAuthorized(roleId)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error(ACCESS_DENIED_MESSAGE));
        }

        try {
            LocalDateTime[] dates = parsePeriod(period, startDate, endDate);
            SlaComplianceReport report = analyticsService.generateSlaComplianceReport(dates[0], dates[1]);
            return ResponseEntity.ok(ApiResponse.success("SLA Compliance Report generated successfully", report));
        } catch (Exception e) {
            logger.error("Error generating SLA Compliance Report", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to generate report: " + e.getMessage()));
        }
    }

    // ============================================================
    // ENDPOINT 2: ENGINEER PERFORMANCE REPORTS
    // ============================================================

    /**
     * Get performance reports for all engineers.
     * 
     * Shows (per engineer):
     * - Tickets assigned/resolved
     * - SLA compliance rate
     * - Average resolution time
     * - Current workload
     * 
     * @param roleId    Required header for role-based access
     * @param period    Time period
     * @param startDate Start date for custom period
     * @param endDate   End date for custom period
     * @return List of EngineerPerformanceReport
     */
    @GetMapping("/engineer-performance")
    public ResponseEntity<ApiResponse<List<EngineerPerformanceReport>>> getEngineerPerformanceReports(
            @RequestHeader(value = "roleId", required = false) String roleId,
            @RequestParam(required = false, defaultValue = "week") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        logger.info("Engineer Performance Reports requested by role: {}", roleId);

        if (!isAuthorized(roleId)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error(ACCESS_DENIED_MESSAGE));
        }

        try {
            LocalDateTime[] dates = parsePeriod(period, startDate, endDate);
            List<EngineerPerformanceReport> reports = analyticsService.generateEngineerPerformanceReports(
                    dates[0], dates[1]);
            return ResponseEntity.ok(ApiResponse.success(
                    "Engineer Performance Reports generated successfully", reports));
        } catch (Exception e) {
            logger.error("Error generating Engineer Performance Reports", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to generate report: " + e.getMessage()));
        }
    }

    /**
     * Get performance report for a specific engineer.
     * 
     * @param engineerId Engineer ID to query
     * @param roleId     Required header for role-based access
     * @param period     Time period
     * @param startDate  Start date for custom period
     * @param endDate    End date for custom period
     * @return EngineerPerformanceReport for specific engineer
     */
    @GetMapping("/engineer/{engineerId}")
    public ResponseEntity<ApiResponse<EngineerPerformanceReport>> getEngineerPerformanceReport(
            @PathVariable String engineerId,
            @RequestHeader(value = "roleId", required = false) String roleId,
            @RequestParam(required = false, defaultValue = "week") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        logger.info("Engineer Performance Report requested for: {} by role: {}", engineerId, roleId);

        if (!isAuthorized(roleId)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error(ACCESS_DENIED_MESSAGE));
        }

        try {
            LocalDateTime[] dates = parsePeriod(period, startDate, endDate);
            EngineerPerformanceReport report = analyticsService.generateEngineerPerformanceReport(
                    engineerId, dates[0], dates[1]);

            if (report == null) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("Engineer not found: " + engineerId));
            }

            return ResponseEntity.ok(ApiResponse.success(
                    "Engineer Performance Report generated successfully", report));
        } catch (Exception e) {
            logger.error("Error generating Engineer Performance Report for: {}", engineerId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to generate report: " + e.getMessage()));
        }
    }

    // ============================================================
    // ENDPOINT 3: CATEGORY VOLUME REPORT
    // ============================================================

    /**
     * Get Category Volume Report.
     * 
     * Shows:
     * - Ticket count per category
     * - Percentage breakdown
     * - Sub-category details
     * 
     * @param roleId    Required header for role-based access
     * @param period    Time period
     * @param startDate Start date for custom period
     * @param endDate   End date for custom period
     * @return CategoryVolumeReport
     */
    @GetMapping("/category-volume")
    public ResponseEntity<ApiResponse<CategoryVolumeReport>> getCategoryVolumeReport(
            @RequestHeader(value = "roleId", required = false) String roleId,
            @RequestParam(required = false, defaultValue = "week") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        logger.info("Category Volume Report requested by role: {}", roleId);

        if (!isAuthorized(roleId)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error(ACCESS_DENIED_MESSAGE));
        }

        try {
            LocalDateTime[] dates = parsePeriod(period, startDate, endDate);
            CategoryVolumeReport report = analyticsService.generateCategoryVolumeReport(dates[0], dates[1]);
            return ResponseEntity.ok(ApiResponse.success(
                    "Category Volume Report generated successfully", report));
        } catch (Exception e) {
            logger.error("Error generating Category Volume Report", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to generate report: " + e.getMessage()));
        }
    }

    // ============================================================
    // ENDPOINT 4: TOP SLA BREACHING CATEGORIES
    // ============================================================

    /**
     * Get Top SLA Breaching Categories Report.
     * 
     * Shows:
     * - Categories ranked by breach count
     * - Breach percentage per category
     * - Sub-category breakdown
     * 
     * @param roleId    Required header for role-based access
     * @param period    Time period
     * @param startDate Start date for custom period
     * @param endDate   End date for custom period
     * @return TopSlaBreachingReport
     */
    @GetMapping("/sla-breaches")
    public ResponseEntity<ApiResponse<TopSlaBreachingReport>> getTopSlaBreachingReport(
            @RequestHeader(value = "roleId", required = false) String roleId,
            @RequestParam(required = false, defaultValue = "week") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        logger.info("Top SLA Breaching Report requested by role: {}", roleId);

        if (!isAuthorized(roleId)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error(ACCESS_DENIED_MESSAGE));
        }

        try {
            LocalDateTime[] dates = parsePeriod(period, startDate, endDate);
            TopSlaBreachingReport report = analyticsService.generateTopSlaBreachingReport(dates[0], dates[1]);
            return ResponseEntity.ok(ApiResponse.success(
                    "Top SLA Breaching Report generated successfully", report));
        } catch (Exception e) {
            logger.error("Error generating Top SLA Breaching Report", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to generate report: " + e.getMessage()));
        }
    }

    // ============================================================
    // ENDPOINT 5: PEAK TICKET HOURS
    // ============================================================

    /**
     * Get Peak Ticket Hours Report.
     * 
     * Shows:
     * - Hourly ticket distribution (0-23 hours)
     * - Peak hours identification
     * - Quiet hours identification
     * 
     * Purpose: Staffing decisions and shift planning
     * 
     * @param roleId    Required header for role-based access
     * @param period    Time period
     * @param startDate Start date for custom period
     * @param endDate   End date for custom period
     * @return PeakHoursReport
     */
    @GetMapping("/peak-hours")
    public ResponseEntity<ApiResponse<PeakHoursReport>> getPeakHoursReport(
            @RequestHeader(value = "roleId", required = false) String roleId,
            @RequestParam(required = false, defaultValue = "week") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        logger.info("Peak Hours Report requested by role: {}", roleId);

        if (!isAuthorized(roleId)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error(ACCESS_DENIED_MESSAGE));
        }

        try {
            LocalDateTime[] dates = parsePeriod(period, startDate, endDate);
            PeakHoursReport report = analyticsService.generatePeakHoursReport(dates[0], dates[1]);
            return ResponseEntity.ok(ApiResponse.success(
                    "Peak Hours Report generated successfully", report));
        } catch (Exception e) {
            logger.error("Error generating Peak Hours Report", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to generate report: " + e.getMessage()));
        }
    }

    // ============================================================
    // ENDPOINT 6: MANAGEMENT SUMMARY (EXECUTIVE VIEW)
    // ============================================================

    /**
     * Get Management Summary (Executive Dashboard).
     * 
     * RULE-BASED SUMMARY (No AI/ML):
     * - SLA compliance status with alerts
     * - Key findings and highlights
     * - Actionable recommendations
     * - Top performer identification
     * - Problem category identification
     * - Health score (0-100)
     * 
     * @param roleId    Required header for role-based access
     * @param period    Time period
     * @param startDate Start date for custom period
     * @param endDate   End date for custom period
     * @return ManagementSummary
     */
    @GetMapping("/management-summary")
    public ResponseEntity<ApiResponse<ManagementSummary>> getManagementSummary(
            @RequestHeader(value = "roleId", required = false) String roleId,
            @RequestParam(required = false, defaultValue = "week") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        logger.info("Management Summary requested by role: {}", roleId);

        if (!isAuthorized(roleId)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error(ACCESS_DENIED_MESSAGE));
        }

        try {
            LocalDateTime[] dates = parsePeriod(period, startDate, endDate);
            ManagementSummary summary = analyticsService.generateManagementSummary(dates[0], dates[1]);
            return ResponseEntity.ok(ApiResponse.success(
                    "Management Summary generated successfully", summary));
        } catch (Exception e) {
            logger.error("Error generating Management Summary", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to generate summary: " + e.getMessage()));
        }
    }

    // ============================================================
    // ENDPOINT 7: QUICK STATS (FOR DASHBOARD WIDGET)
    // ============================================================

    /**
     * Get quick statistics for dashboard widget.
     * 
     * Lightweight endpoint returning just key metrics.
     * 
     * @param roleId Required header for role-based access
     * @return SlaComplianceReport (contains key metrics)
     */
    @GetMapping("/quick-stats")
    public ResponseEntity<ApiResponse<SlaComplianceReport>> getQuickStats(
            @RequestHeader(value = "roleId", required = false) String roleId) {

        logger.info("Quick Stats requested by role: {}", roleId);

        if (!isAuthorized(roleId)) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error(ACCESS_DENIED_MESSAGE));
        }

        try {
            // Get today's stats for quick view
            LocalDateTime start = analyticsService.getStartOfToday();
            LocalDateTime end = LocalDateTime.now();

            SlaComplianceReport report = analyticsService.generateSlaComplianceReport(start, end);
            return ResponseEntity.ok(ApiResponse.success("Quick stats retrieved successfully", report));
        } catch (Exception e) {
            logger.error("Error generating Quick Stats", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get quick stats: " + e.getMessage()));
        }
    }
}
