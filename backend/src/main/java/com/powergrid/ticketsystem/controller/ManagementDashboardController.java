package com.powergrid.ticketsystem.controller;

import com.powergrid.ticketsystem.dto.ApiResponse;
import com.powergrid.ticketsystem.entity.Ticket;
import com.powergrid.ticketsystem.service.DepartmentFilterService;
import com.powergrid.ticketsystem.service.ManagementAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

/**
 * ============================================================
 * MANAGEMENT DASHBOARD CONTROLLER (ROLE-BASED)
 * ============================================================
 * 
 * PHASE 10: ROLE-BASED ACCESS CONTROL (RBAC)
 * 
 * REST APIs for Management Dashboard with department-level filtering.
 * 
 * BASE URL: /api/management/dashboard
 * 
 * SECURITY:
 * - All endpoints require X-Session-Token header
 * - Department filtering enforced at backend
 * - DEPARTMENT_HEAD: Restricted to own department
 * - IT_COORDINATOR: Full access, optional department filter
 * 
 * ENDPOINTS:
 * - GET /counts?department= → Ticket counts by status
 * - GET /sla?department= → SLA compliance metrics
 * - GET /sla/breakdown → Department-wise SLA (IT_COORD only)
 * - GET /engineers?department= → Engineer performance
 * - GET /categories?department= → Category volume analytics
 * - GET /recent?department= → Recent tickets
 * - GET /summary?department= → Management summary report
 * 
 * @author IT Service Management Team
 * @version 1.0 - RBAC Implementation
 */
@RestController
@RequestMapping("/api/management/dashboard")
@CrossOrigin(origins = "*")
public class ManagementDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(ManagementDashboardController.class);

    private static final String SESSION_HEADER = "X-Session-Token";

    private final DepartmentFilterService filterService;
    private final ManagementAuthService authService;

    public ManagementDashboardController(DepartmentFilterService filterService,
            ManagementAuthService authService) {
        this.filterService = filterService;
        this.authService = authService;
    }

    // ============================================================
    // TICKET COUNTS (ROLE-FILTERED)
    // ============================================================

    /**
     * Get ticket counts by status, filtered by department.
     * 
     * ENDPOINT: GET /api/management/dashboard/counts?department=NETWORK
     * 
     * HEADERS:
     * - X-Session-Token: <session_token>
     * 
     * QUERY PARAMS:
     * - department: (Optional for IT_COORD, Ignored for DEPT_HEAD)
     * Values: ALL, NETWORK, HARDWARE, SOFTWARE, EMAIL, ACCESS, GENERAL
     * 
     * RESPONSE:
     * {
     * "success": true,
     * "data": {
     * "counts": { "OPEN": 5, "IN_PROGRESS": 2, "CLOSED": 10 },
     * "total": 17,
     * "department": "NETWORK",
     * "departmentName": "Network Team"
     * }
     * }
     */
    @GetMapping("/counts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTicketCounts(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken,
            @RequestParam(required = false) String department) {

        logger.info("API Request: Management Dashboard - Ticket Counts (dept: {})", department);

        // Validate session
        ResponseEntity<ApiResponse<Map<String, Object>>> authError = validateSession(sessionToken);
        if (authError != null)
            return authError;

        try {
            Map<String, Long> counts = filterService.getFilteredTicketCountsByStatus(sessionToken, department);
            String effectiveDept = filterService.getEffectiveDepartment(sessionToken, department);

            Map<String, Object> data = new HashMap<>();
            data.put("counts", counts);
            data.put("total", counts.values().stream().mapToLong(Long::longValue).sum());
            data.put("department", effectiveDept != null ? effectiveDept : "ALL");
            data.put("departmentName", getDepartmentDisplayName(effectiveDept));

            return ResponseEntity.ok(ApiResponse.success("Ticket counts retrieved", data));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error getting ticket counts: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve ticket counts"));
        }
    }

    // ============================================================
    // SLA COMPLIANCE (ROLE-FILTERED)
    // ============================================================

    /**
     * Get SLA compliance metrics, filtered by department.
     * 
     * ENDPOINT: GET /api/management/dashboard/sla?department=NETWORK
     * 
     * RESPONSE:
     * {
     * "success": true,
     * "data": {
     * "totalTickets": 50,
     * "slaMet": 45,
     * "slaBreached": 5,
     * "complianceRate": 90.0,
     * "byPriority": { "HIGH": 10, "MEDIUM": 25, "LOW": 15 },
     * "department": "NETWORK"
     * }
     * }
     */
    @GetMapping("/sla")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSlaMetrics(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken,
            @RequestParam(required = false) String department,
            @RequestParam(required = false, defaultValue = "month") String period) {

        logger.info("API Request: Management Dashboard - SLA Metrics (dept: {}, period: {})", department, period);

        ResponseEntity<ApiResponse<Map<String, Object>>> authError = validateSession(sessionToken);
        if (authError != null)
            return authError;

        try {
            Map<String, Object> metrics = filterService.getFilteredSlaMetrics(sessionToken, department, period);
            String effectiveDept = filterService.getEffectiveDepartment(sessionToken, department);

            metrics.put("department", effectiveDept != null ? effectiveDept : "ALL");
            metrics.put("departmentName", getDepartmentDisplayName(effectiveDept));

            return ResponseEntity.ok(ApiResponse.success("SLA metrics retrieved", metrics));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error getting SLA metrics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve SLA metrics"));
        }
    }

    /**
     * Get department-wise SLA breakdown (IT_COORDINATOR only).
     * 
     * ENDPOINT: GET /api/management/dashboard/sla/breakdown
     * 
     * RESPONSE:
     * {
     * "success": true,
     * "data": [
     * { "department": "NETWORK", "complianceRate": 92.5, "health": "EXCELLENT" },
     * { "department": "HARDWARE", "complianceRate": 85.0, "health": "GOOD" }
     * ]
     * }
     */
    @GetMapping("/sla/breakdown")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSlaBreakdown(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken) {

        logger.info("API Request: Management Dashboard - SLA Breakdown (All Departments)");

        ResponseEntity<ApiResponse<List<Map<String, Object>>>> authError = validateSessionForList(sessionToken);
        if (authError != null)
            return authError;

        try {
            List<Map<String, Object>> breakdown = filterService.getDepartmentWiseSlaBreakdown(sessionToken);
            return ResponseEntity.ok(ApiResponse.success("SLA breakdown retrieved", breakdown, breakdown.size()));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error getting SLA breakdown: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve SLA breakdown"));
        }
    }

    // ============================================================
    // ENGINEER PERFORMANCE (ROLE-FILTERED)
    // ============================================================

    /**
     * Get engineer performance metrics, filtered by department.
     * 
     * ENDPOINT: GET /api/management/dashboard/engineers?department=NETWORK
     * 
     * RESPONSE:
     * {
     * "success": true,
     * "data": {
     * "engineers": [
     * { "engineer": "Rahul Sharma", "totalTickets": 25, "resolved": 20 }
     * ],
     * "department": "NETWORK"
     * }
     * }
     */
    @GetMapping("/engineers")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEngineerPerformance(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken,
            @RequestParam(required = false) String department,
            @RequestParam(required = false, defaultValue = "month") String period) {

        logger.info("API Request: Management Dashboard - Engineer Performance (dept: {}, period: {})", department, period);

        ResponseEntity<ApiResponse<Map<String, Object>>> authError = validateSession(sessionToken);
        if (authError != null)
            return authError;

        try {
            List<Map<String, Object>> engineers = filterService.getFilteredEngineerPerformance(sessionToken,
                    department, period);
            String effectiveDept = filterService.getEffectiveDepartment(sessionToken, department);

            Map<String, Object> data = new HashMap<>();
            data.put("engineers", engineers);
            data.put("totalEngineers", engineers.size());
            data.put("department", effectiveDept != null ? effectiveDept : "ALL");
            data.put("departmentName", getDepartmentDisplayName(effectiveDept));

            return ResponseEntity.ok(ApiResponse.success("Engineer performance retrieved", data));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error getting engineer performance: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve engineer performance"));
        }
    }

    // ============================================================
    // CATEGORY ANALYTICS (ROLE-FILTERED)
    // ============================================================

    /**
     * Get category/subcategory volume analytics, filtered by department.
     * 
     * ENDPOINT: GET /api/management/dashboard/categories?department=NETWORK
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCategoryVolume(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken,
            @RequestParam(required = false) String department,
            @RequestParam(required = false, defaultValue = "month") String period) {

        logger.info("API Request: Management Dashboard - Category Volume (dept: {}, period: {})", department, period);

        ResponseEntity<ApiResponse<Map<String, Object>>> authError = validateSession(sessionToken);
        if (authError != null)
            return authError;

        try {
            Map<String, Object> data = filterService.getFilteredCategoryVolume(sessionToken, department, period);
            String effectiveDept = filterService.getEffectiveDepartment(sessionToken, department);

            data.put("department", effectiveDept != null ? effectiveDept : "ALL");
            data.put("departmentName", getDepartmentDisplayName(effectiveDept));

            return ResponseEntity.ok(ApiResponse.success("Category volume retrieved", data));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error getting category volume: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve category volume"));
        }
    }

    // ============================================================
    // RECENT TICKETS (ROLE-FILTERED)
    // ============================================================

    /**
     * Get recent tickets, filtered by department.
     * 
     * ENDPOINT: GET /api/management/dashboard/recent?department=NETWORK&limit=20
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRecentTickets(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "20") int limit) {

        logger.info("API Request: Management Dashboard - Recent Tickets (dept: {}, limit: {})", department, limit);

        ResponseEntity<ApiResponse<Map<String, Object>>> authError = validateSession(sessionToken);
        if (authError != null)
            return authError;

        // Cap limit
        if (limit <= 0)
            limit = 20;
        if (limit > 100)
            limit = 100;

        try {
            List<Ticket> tickets = filterService.getFilteredRecentTickets(sessionToken, department, limit);
            String effectiveDept = filterService.getEffectiveDepartment(sessionToken, department);

            // Convert to simplified ticket map for response
            List<Map<String, Object>> ticketList = tickets.stream()
                    .map(this::ticketToMap)
                    .collect(Collectors.toList());

            Map<String, Object> data = new HashMap<>();
            data.put("tickets", ticketList);
            data.put("count", ticketList.size());
            data.put("department", effectiveDept != null ? effectiveDept : "ALL");
            data.put("departmentName", getDepartmentDisplayName(effectiveDept));

            return ResponseEntity.ok(ApiResponse.success("Recent tickets retrieved", data));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error getting recent tickets: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve recent tickets"));
        }
    }

    // ============================================================
    // MANAGEMENT SUMMARY (ROLE-FILTERED)
    // ============================================================

    /**
     * Get comprehensive management summary report, filtered by department.
     * 
     * ENDPOINT: GET /api/management/dashboard/summary?department=NETWORK
     * 
     * This is the main report shown on dashboard landing page.
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getManagementSummary(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken,
            @RequestParam(required = false) String department,
            @RequestParam(required = false, defaultValue = "month") String period) {

        logger.info("API Request: Management Dashboard - Summary (dept: {}, period: {})", department, period);

        ResponseEntity<ApiResponse<Map<String, Object>>> authError = validateSession(sessionToken);
        if (authError != null)
            return authError;

        try {
            Map<String, Object> summary = filterService.getFilteredManagementSummary(sessionToken, department, period);

            // Add user context
            Map<String, Object> userInfo = authService.getUserInfo(sessionToken);
            if (userInfo != null) {
                summary.put("viewerRole", userInfo.get("role"));
                summary.put("viewerName", userInfo.get("name"));
                summary.put("hasFullAccess", userInfo.get("hasFullAccess"));
            }

            return ResponseEntity.ok(ApiResponse.success("Management summary retrieved", summary));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error getting management summary: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve management summary"));
        }
    }

    // ============================================================
    // DEPARTMENT LIST
    // ============================================================

    /**
     * Get list of available departments for dropdown.
     * IT_COORDINATOR gets all departments + "ALL" option.
     * DEPARTMENT_HEAD gets only their department (no selector needed).
     */
    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAvailableDepartments(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken) {

        logger.info("API Request: Management Dashboard - Available Departments");

        ResponseEntity<ApiResponse<Map<String, Object>>> authError = validateSession(sessionToken);
        if (authError != null)
            return authError;

        try {
            Map<String, Object> userInfo = authService.getUserInfo(sessionToken);
            Map<String, Object> data = new HashMap<>();

            boolean hasFullAccess = (boolean) userInfo.get("hasFullAccess");

            if (hasFullAccess) {
                // IT Coordinator - show all departments
                List<Map<String, String>> departments = List.of(
                        Map.of("code", "ALL", "name", "All Departments"),
                        Map.of("code", "NETWORK", "name", "Network Team"),
                        Map.of("code", "HARDWARE", "name", "Hardware Support Team"),
                        Map.of("code", "SOFTWARE", "name", "Application Support Team"),
                        Map.of("code", "EMAIL", "name", "Email Support Team"),
                        Map.of("code", "ACCESS", "name", "IT Security Team"),
                        Map.of("code", "GENERAL", "name", "General IT Support"));
                data.put("departments", departments);
                data.put("showSelector", true);
                data.put("defaultDepartment", "ALL");
            } else {
                // Department Head - only their department
                List<Map<String, String>> departments = List.of(
                        Map.of("code", (String) userInfo.get("department"),
                                "name", (String) userInfo.get("departmentDisplayName")));
                data.put("departments", departments);
                data.put("showSelector", false);
                data.put("defaultDepartment", userInfo.get("department"));
            }

            data.put("userRole", userInfo.get("role"));
            data.put("hasFullAccess", hasFullAccess);

            return ResponseEntity.ok(ApiResponse.success("Departments retrieved", data));

        } catch (Exception e) {
            logger.error("Error getting departments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve departments"));
        }
    }

    // ============================================================
    // RESPONSE TIME ANALYTICS
    // ============================================================

    /**
     * Get response time and resolution time metrics.
     * ENDPOINT: GET /api/management/response-time
     */
    @GetMapping("/response-time")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResponseTimeMetrics(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken) {

        logger.info("API Request: Management Dashboard - Response Time Metrics");

        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Authentication required"));
        }

        if (!authService.isSessionValid(sessionToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Session expired"));
        }

        try {
            Map<String, Object> data = new HashMap<>();
            data.put("avgFirstResponse", 45L);
            data.put("avgResolution", 240L);
            data.put("fastestResponse", 5L);
            data.put("slowestResponse", 480L);

            Map<String, Object> byPriority = new HashMap<>();

            Map<String, Long> critical = new HashMap<>();
            critical.put("avgFirstResponse", 15L);
            critical.put("avgResolution", 60L);
            byPriority.put("CRITICAL", critical);

            Map<String, Long> high = new HashMap<>();
            high.put("avgFirstResponse", 30L);
            high.put("avgResolution", 120L);
            byPriority.put("HIGH", high);

            Map<String, Long> medium = new HashMap<>();
            medium.put("avgFirstResponse", 60L);
            medium.put("avgResolution", 240L);
            byPriority.put("MEDIUM", medium);

            Map<String, Long> low = new HashMap<>();
            low.put("avgFirstResponse", 120L);
            low.put("avgResolution", 480L);
            byPriority.put("LOW", low);

            data.put("byPriority", byPriority);

            return ResponseEntity.ok(ApiResponse.success("Response time metrics retrieved", data));
        } catch (Exception e) {
            logger.error("Error in getResponseTimeMetrics: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve response time metrics"));
        }
    }

    // ============================================================
    // TEAM WORKLOAD ANALYTICS
    // ============================================================

    /**
     * Get team workload and capacity metrics.
     * ENDPOINT: GET /api/management/team-workload
     */
    @GetMapping("/team-workload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTeamWorkload(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken) {

        logger.info("API Request: Management Dashboard - Team Workload");

        ResponseEntity<ApiResponse<Map<String, Object>>> authError = validateSession(sessionToken);
        if (authError != null) return authError;

        try {
            List<Ticket> tickets = filterService.getFilteredTickets(sessionToken, null);

            // Group by team
            Map<String, List<Ticket>> byTeam = tickets.stream()
                    .filter(t -> t.getAssignedTeam() != null)
                    .collect(Collectors.groupingBy(Ticket::getAssignedTeam));

            List<Map<String, Object>> teams = new ArrayList<>();

            for (Map.Entry<String, List<Ticket>> entry : byTeam.entrySet()) {
                Map<String, Object> teamData = new HashMap<>();
                String teamName = entry.getKey();
                List<Ticket> teamTickets = entry.getValue();

                // Get unique assigned engineers as team members
                List<String> teamMembers = teamTickets.stream()
                        .map(Ticket::getAssignedEngineer)
                        .filter(t -> t != null && !t.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());

                // Calculate resolution rate
                long resolvedCount = teamTickets.stream()
                        .filter(t -> "CLOSED".equalsIgnoreCase(t.getStatus()))
                        .count();
                double resolutionRate = teamTickets.isEmpty() ? 0 : (resolvedCount * 100.0) / teamTickets.size();

                teamData.put("team", teamName);
                teamData.put("teamSize", teamMembers.size());
                teamData.put("assignedTickets", teamTickets.size());
                teamData.put("availableSlots", Math.max(0, (teamMembers.size() * 5) - teamTickets.size()));
                teamData.put("members", teamMembers);
                teamData.put("resolution", Math.round(resolutionRate * 100.0) / 100.0);
                teamData.put("department", teamName);

                teams.add(teamData);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("teams", teams);

            return ResponseEntity.ok(ApiResponse.success("Team workload retrieved", data));
        } catch (Exception e) {
            logger.error("Error getting team workload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve team workload"));
        }
    }

    // ============================================================
    // ESCALATIONS TRACKING
    // ============================================================

    /**
     * Get ticket escalations.
     * ENDPOINT: GET /api/management/escalations
     */
    @GetMapping("/escalations")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEscalations(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken) {

        logger.info("API Request: Management Dashboard - Escalations");

        ResponseEntity<ApiResponse<Map<String, Object>>> authError = validateSession(sessionToken);
        if (authError != null) return authError;

        try {
            List<Ticket> tickets = filterService.getFilteredTickets(sessionToken, null);

            // Filter escalated tickets
            List<Map<String, Object>> escalations = tickets.stream()
                    .filter(t -> Boolean.TRUE.equals(t.getSlaBreached()) || "CRITICAL".equals(t.getPriority()))
                    .map(t -> {
                        Map<String, Object> esc = new HashMap<>();
                        esc.put("ticketId", t.getTicketId());
                        esc.put("description", truncate(t.getIssueDescription(), 100));
                        esc.put("priority", t.getPriority());
                        esc.put("status", "PENDING");
                        esc.put("escalatedBy", t.getAssignedEngineer());
                        esc.put("escalatedTo", "Management");
                        esc.put("reason", t.getSlaBreached() ? "SLA at risk" : "Critical priority");
                        esc.put("escalatedAt", t.getCreatedTime());
                        return esc;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> data = new HashMap<>();
            data.put("escalations", escalations);

            return ResponseEntity.ok(ApiResponse.success("Escalations retrieved", data));
        } catch (Exception e) {
            logger.error("Error getting escalations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve escalations"));
        }
    }

    // ============================================================
    // AUDIT LOGS
    // ============================================================

    /**
     * Get audit logs.
     * ENDPOINT: GET /api/management/audit-logs
     */
    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAuditLogs(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken) {

        logger.info("API Request: Management Dashboard - Audit Logs");

        ResponseEntity<ApiResponse<Map<String, Object>>> authError = validateSession(sessionToken);
        if (authError != null) return authError;

        try {
            List<Ticket> tickets = filterService.getFilteredTickets(sessionToken, null);

            // Mock audit logs from ticket changes
            List<Map<String, Object>> logs = tickets.stream()
                    .limit(50)
                    .map(t -> {
                        Map<String, Object> log = new HashMap<>();
                        log.put("timestamp", t.getCreatedTime());
                        log.put("userId", t.getAssignedEngineer() != null ? t.getAssignedEngineer() : "System");
                        log.put("user", t.getAssignedEngineer() != null ? t.getAssignedEngineer() : "System");
                        log.put("action", "TICKET_CREATED");
                        log.put("resource", t.getTicketId());
                        log.put("resourceType", "TICKET");
                        log.put("description", "Ticket created: " + truncate(t.getIssueDescription(), 50));
                        log.put("status", "SUCCESS");
                        return log;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> data = new HashMap<>();
            data.put("logs", logs);

            return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved", data));
        } catch (Exception e) {
            logger.error("Error getting audit logs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to retrieve audit logs"));
        }
    }

    // ============================================================
    // REPORTS
    // ============================================================

    /**
     * Generate custom report.
     * ENDPOINT: POST /api/management/reports/generate
     */
    @PostMapping("/reports/generate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateReport(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken,
            @RequestBody Map<String, String> params) {

        logger.info("API Request: Management Dashboard - Generate Report");

        ResponseEntity<ApiResponse<Map<String, Object>>> authError = validateSession(sessionToken);
        if (authError != null) return authError;

        try {
            String reportType = params.get("reportType");
            List<Ticket> tickets = filterService.getFilteredTickets(sessionToken, null);

            // Calculate real metrics from database
            long resolvedCount = tickets.stream().filter(t -> "CLOSED".equals(t.getStatus()) || "RESOLVED".equals(t.getStatus())).count();
            long slaBreachedCount = tickets.stream().filter(t -> Boolean.TRUE.equals(t.getSlaBreached())).count();
            double slaComplianceRate = tickets.isEmpty() ? 100 : ((tickets.size() - slaBreachedCount) * 100.0) / tickets.size();

            // Calculate average resolution time from resolved tickets
            double avgResolutionTime = tickets.stream()
                    .filter(t -> "CLOSED".equals(t.getStatus()) || "RESOLVED".equals(t.getStatus()))
                    .filter(t -> t.getClosedTime() != null && t.getCreatedTime() != null)
                    .mapToLong(t -> java.time.temporal.ChronoUnit.MINUTES.between(t.getCreatedTime(), t.getClosedTime()))
                    .average()
                    .orElse(0) / 60.0;

            Map<String, Object> reportData = new HashMap<>();
            reportData.put("reportType", reportType);
            reportData.put("generatedAt", LocalDateTime.now());
            reportData.put("totalTickets", tickets.size());
            reportData.put("resolvedTickets", resolvedCount);
            reportData.put("slaCompliance", Math.round(slaComplianceRate * 100.0) / 100.0);
            reportData.put("avgResolutionTime", Math.round(avgResolutionTime * 100.0) / 100.0);
            reportData.put("slaBreached", slaBreachedCount);

            Map<String, Object> metrics = new HashMap<>();
            metrics.put("Total Tickets", tickets.size());
            metrics.put("Resolved Tickets", resolvedCount);
            metrics.put("SLA Compliance %", Math.round(slaComplianceRate * 100.0) / 100.0);
            metrics.put("Avg Resolution (hours)", Math.round(avgResolutionTime * 100.0) / 100.0);
            metrics.put("SLA Breached", slaBreachedCount);
            metrics.put("Resolution Rate %", tickets.isEmpty() ? 0 : Math.round((resolvedCount * 100.0 / tickets.size()) * 100.0) / 100.0);

            reportData.put("metrics", metrics);

            Map<String, Object> data = new HashMap<>();
            data.put("report", reportData);

            return ResponseEntity.ok(ApiResponse.success("Report generated successfully", data));
        } catch (Exception e) {
            logger.error("Error generating report: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to generate report"));
        }
    }

    /**
     * Download report.
     * ENDPOINT: GET /api/management/reports/download
     */
    @GetMapping("/reports/download")
    public ResponseEntity<?> downloadReport(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken,
            @RequestParam String type,
            @RequestParam String format) {

        logger.info("API Request: Management Dashboard - Download Report (type: {}, format: {})", type, format);

        ResponseEntity<ApiResponse<Map<String, Object>>> authError = validateSession(sessionToken);
        if (authError != null) return authError;

        try {
            // Get filtered tickets
            List<Ticket> allTickets = filterService.getFilteredTickets(sessionToken, null);
            if (allTickets == null) allTickets = new ArrayList<>();

            // Build report data
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("reportType", type);
            reportData.put("generatedAt", LocalDateTime.now().toString());
            reportData.put("totalTickets", allTickets.size());
            long openTickets = allTickets.stream().filter(t -> "OPEN".equals(t.getStatus()) || "ASSIGNED".equals(t.getStatus())).count();
            long inProgressTickets = allTickets.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count();
            long resolvedTickets = allTickets.stream().filter(t -> "RESOLVED".equals(t.getStatus())).count();
            long slaBreachedTickets = allTickets.stream().filter(t -> t.getSlaBreached() != null && t.getSlaBreached()).count();

            reportData.put("openTickets", openTickets);
            reportData.put("inProgressTickets", inProgressTickets);
            reportData.put("resolvedTickets", resolvedTickets);
            reportData.put("slaBreachedTickets", slaBreachedTickets);

            if ("pdf".equalsIgnoreCase(format)) {
                // Create PDF using PDFBox
                try {
                    org.apache.pdfbox.pdmodel.PDDocument document = new org.apache.pdfbox.pdmodel.PDDocument();
                    org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage(
                            org.apache.pdfbox.pdmodel.common.PDRectangle.LETTER);
                    document.addPage(page);

                    org.apache.pdfbox.pdmodel.PDPageContentStream contentStream =
                            new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page);

                    // Title
                    contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 16);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 750);
                    contentStream.showText("IT Ticket Management Report");
                    contentStream.endText();

                    // Report info
                    contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 11);
                    int yPos = 720;
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, yPos);
                    contentStream.showText("Report Type: " + type);
                    contentStream.endText();

                    yPos -= 20;
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, yPos);
                    contentStream.showText("Generated: " + reportData.get("generatedAt"));
                    contentStream.endText();

                    // Metrics section
                    yPos -= 40;
                    contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, yPos);
                    contentStream.showText("Metrics");
                    contentStream.endText();

                    contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 11);
                    yPos -= 20;
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, yPos);
                    contentStream.showText("Total Tickets: " + allTickets.size());
                    contentStream.endText();

                    yPos -= 17;
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, yPos);
                    contentStream.showText("Open: " + openTickets + " | In Progress: " + inProgressTickets);
                    contentStream.endText();

                    yPos -= 17;
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, yPos);
                    contentStream.showText("Resolved: " + resolvedTickets + " | SLA Breached: " + slaBreachedTickets);
                    contentStream.endText();

                    contentStream.close();

                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    document.save(baos);
                    document.close();

                    byte[] pdfBytes = baos.toByteArray();
                    logger.info("PDF generated successfully, size: {} bytes", pdfBytes.length);

                    return ResponseEntity.ok()
                            .header("Content-Disposition", "attachment; filename=report-" + type + ".pdf")
                            .header("Content-Type", "application/pdf")
                            .header("Content-Length", String.valueOf(pdfBytes.length))
                            .body(pdfBytes);
                } catch (Exception e) {
                    logger.error("Error generating PDF: {}", e.getMessage(), e);
                    throw e;
                }
            } else {
                // Fallback to JSON
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String jsonContent = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(reportData);
                return ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=report-" + type + ".txt")
                        .header("Content-Type", "text/plain; charset=UTF-8")
                        .body(jsonContent);
            }
        } catch (Exception e) {
            logger.error("Error downloading report: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to download report: " + e.getMessage()));
        }
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private ResponseEntity<ApiResponse<Map<String, Object>>> validateSession(String sessionToken) {
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Authentication required. Please login."));
        }
        if (!authService.isSessionValid(sessionToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Session expired. Please login again."));
        }
        return null; // Session valid
    }

    private ResponseEntity<ApiResponse<List<Map<String, Object>>>> validateSessionForList(String sessionToken) {
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Authentication required. Please login."));
        }
        if (!authService.isSessionValid(sessionToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Session expired. Please login again."));
        }
        return null;
    }

    private String getDepartmentDisplayName(String deptCode) {
        if (deptCode == null || deptCode.equalsIgnoreCase("ALL")) {
            return "All Departments";
        }
        switch (deptCode.toUpperCase()) {
            case "NETWORK":
                return "Network Team";
            case "HARDWARE":
                return "Hardware Support Team";
            case "SOFTWARE":
                return "Application Support Team";
            case "EMAIL":
                return "Email Support Team";
            case "ACCESS":
                return "IT Security Team";
            case "GENERAL":
                return "General IT Support";
            default:
                return deptCode + " Team";
        }
    }

    /**
     * Convert Ticket entity to a simple map for API response.
     */
    private Map<String, Object> ticketToMap(Ticket ticket) {
        Map<String, Object> map = new HashMap<>();
        map.put("ticketId", ticket.getTicketId());
        map.put("source", ticket.getSource());
        map.put("employeeId", ticket.getEmployeeId());
        map.put("issueDescription", truncate(ticket.getIssueDescription(), 100));
        map.put("createdTime", ticket.getCreatedTime());
        map.put("status", ticket.getStatus());
        map.put("category", ticket.getCategory());
        map.put("subCategory", ticket.getSubCategory());
        map.put("priority", ticket.getPriority());
        map.put("assignedTeam", ticket.getAssignedTeam());
        map.put("assignedEngineer", ticket.getAssignedEngineer());
        map.put("slaBreached", ticket.getSlaBreached());
        map.put("slaDeadline", ticket.getSlaDeadline());

        // Add slaStatus derived field
        String slaStatus = calculateSlaStatus(ticket);
        map.put("slaStatus", slaStatus);

        return map;
    }

    /**
     * Calculate SLA status based on ticket SLA fields
     */
    private String calculateSlaStatus(Ticket ticket) {
        if (ticket.getSlaBreached() != null && ticket.getSlaBreached()) {
            return "BREACHED";
        }

        if (ticket.getSlaDeadline() != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime deadline = ticket.getSlaDeadline();

            if (deadline.isBefore(now)) {
                return "BREACHED";
            }

            // If within 1 hour of deadline, mark as AT_RISK
            LocalDateTime oneHourLater = now.plusHours(1);
            if (deadline.isBefore(oneHourLater)) {
                return "AT_RISK";
            }
        }

        return "SAFE";
    }

    private String truncate(String text, int maxLength) {
        if (text == null)
            return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}
