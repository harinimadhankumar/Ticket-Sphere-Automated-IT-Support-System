package com.powergrid.ticketsystem.service;

import com.powergrid.ticketsystem.constants.Department;
import com.powergrid.ticketsystem.entity.ManagementUser;
import com.powergrid.ticketsystem.entity.Ticket;
import com.powergrid.ticketsystem.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ============================================================
 * DEPARTMENT FILTER SERVICE
 * ============================================================
 * 
 * PHASE 10: ROLE-BASED ACCESS CONTROL (RBAC)
 * 
 * Service responsible for enforcing department-level filtering
 * on all ticket and analytics queries.
 * 
 * SECURITY PRINCIPLE:
 * All data filtering MUST happen at the backend.
 * Frontend filtering is supplementary, not primary security.
 * 
 * RESPONSIBILITIES:
 * 1. Filter tickets by department
 * 2. Filter analytics by department
 * 3. Enforce DEPARTMENT_HEAD restrictions
 * 4. Provide department-wise breakdowns for IT_COORDINATOR
 * 
 * DEPARTMENT MAPPING:
 * - NETWORK → Ticket categories: NETWORK
 * - HARDWARE → Ticket categories: HARDWARE
 * - SOFTWARE → Ticket categories: SOFTWARE
 * - EMAIL → Ticket categories: EMAIL
 * - ACCESS → Ticket categories: ACCESS
 * - GENERAL → Ticket categories: UNKNOWN, GENERAL
 * 
 * @author IT Service Management Team
 * @version 1.0 - RBAC Implementation
 */
@Service
public class DepartmentFilterService {

        private static final Logger logger = LoggerFactory.getLogger(DepartmentFilterService.class);

        private final TicketRepository ticketRepository;
        private final ManagementAuthService authService;

        public DepartmentFilterService(TicketRepository ticketRepository,
                        ManagementAuthService authService) {
                this.ticketRepository = ticketRepository;
                this.authService = authService;
        }

        // ============================================================
        // TICKET FILTERING
        // ============================================================

        /**
         * Get tickets filtered by department based on user's role.
         * 
         * @param sessionToken        User's session token
         * @param requestedDepartment Requested department filter (IT_COORDINATOR only)
         * @return Filtered list of tickets
         */
        public List<Ticket> getFilteredTickets(String sessionToken, String requestedDepartment) {
                String effectiveDepartment = authService.getEffectiveDepartment(sessionToken, requestedDepartment);

                logger.debug("Filtering tickets - Requested: {}, Effective: {}",
                                requestedDepartment, effectiveDepartment);

                List<Ticket> allTickets = ticketRepository.findAll();

                if (effectiveDepartment == null) {
                        // No filter - return all (IT_COORDINATOR viewing ALL)
                        return allTickets;
                }

                // Filter by department's categories
                Department dept = Department.fromCode(effectiveDepartment);
                return allTickets.stream()
                                .filter(ticket -> dept.handlesCategory(ticket.getCategory()))
                                .collect(Collectors.toList());
        }

        /**
         * Get ticket count by status, filtered by department.
         */
        public Map<String, Long> getFilteredTicketCountsByStatus(String sessionToken, String requestedDepartment) {
                List<Ticket> tickets = getFilteredTickets(sessionToken, requestedDepartment);

                return tickets.stream()
                                .collect(Collectors.groupingBy(
                                                ticket -> ticket.getStatus() != null ? ticket.getStatus() : "UNKNOWN",
                                                Collectors.counting()));
        }

        /**
         * Get recent tickets filtered by department.
         */
        public List<Ticket> getFilteredRecentTickets(String sessionToken, String requestedDepartment, int limit) {
                List<Ticket> tickets = getFilteredTickets(sessionToken, requestedDepartment);

                return tickets.stream()
                                .sorted(Comparator.comparing(Ticket::getCreatedTime).reversed())
                                .limit(limit)
                                .collect(Collectors.toList());
        }

        // ============================================================
        // SLA ANALYTICS
        // ============================================================

        /**
         * Get SLA compliance metrics filtered by department.
         */
        public Map<String, Object> getFilteredSlaMetrics(String sessionToken, String requestedDepartment, String period) {
                List<Ticket> tickets = getFilteredTickets(sessionToken, requestedDepartment);

                // Apply period filtering
                tickets = filterTicketsByPeriod(tickets, period);

                Map<String, Object> metrics = new HashMap<>();

                long totalTickets = tickets.size();
                long slaBreached = tickets.stream()
                                .filter(t -> Boolean.TRUE.equals(t.getSlaBreached()))
                                .count();
                long slaMet = totalTickets - slaBreached;

                double complianceRate = totalTickets > 0
                                ? (slaMet * 100.0 / totalTickets)
                                : 100.0;

                metrics.put("totalTickets", totalTickets);
                metrics.put("slaMet", slaMet);
                metrics.put("slaBreached", slaBreached);
                metrics.put("complianceRate", Math.round(complianceRate * 100.0) / 100.0);
                metrics.put("department", requestedDepartment);
                metrics.put("timestamp", LocalDateTime.now());

                // Breakdown by priority
                Map<String, Long> byPriority = tickets.stream()
                                .filter(t -> t.getPriority() != null)
                                .collect(Collectors.groupingBy(Ticket::getPriority, Collectors.counting()));
                metrics.put("byPriority", byPriority);

                // Breach breakdown
                Map<String, Long> breachByPriority = tickets.stream()
                                .filter(t -> Boolean.TRUE.equals(t.getSlaBreached()) && t.getPriority() != null)
                                .collect(Collectors.groupingBy(Ticket::getPriority, Collectors.counting()));
                metrics.put("breachByPriority", breachByPriority);

                return metrics;
        }

        /**
         * Get department-wise SLA breakdown (for IT_COORDINATOR).
         */
        public List<Map<String, Object>> getDepartmentWiseSlaBreakdown(String sessionToken) {
                // Verify IT Coordinator access
                ManagementUser user = authService.getCurrentUser(sessionToken);
                if (!user.hasFullAccess()) {
                        throw new SecurityException("Department-wise breakdown requires IT Coordinator role");
                }

                List<Ticket> allTickets = ticketRepository.findAll();
                List<Map<String, Object>> breakdown = new ArrayList<>();

                for (Department dept : Department.values()) {
                        List<Ticket> deptTickets = allTickets.stream()
                                        .filter(t -> dept.handlesCategory(t.getCategory()))
                                        .collect(Collectors.toList());

                        Map<String, Object> deptMetrics = new HashMap<>();
                        deptMetrics.put("department", dept.getCode());
                        deptMetrics.put("departmentName", dept.getTeamName());

                        long total = deptTickets.size();
                        long breached = deptTickets.stream()
                                        .filter(t -> Boolean.TRUE.equals(t.getSlaBreached()))
                                        .count();
                        double compliance = total > 0 ? ((total - breached) * 100.0 / total) : 100.0;

                        deptMetrics.put("totalTickets", total);
                        deptMetrics.put("slaBreached", breached);
                        deptMetrics.put("complianceRate", Math.round(compliance * 100.0) / 100.0);

                        // Health status
                        String health;
                        if (compliance >= 95)
                                health = "EXCELLENT";
                        else if (compliance >= 85)
                                health = "GOOD";
                        else if (compliance >= 70)
                                health = "FAIR";
                        else
                                health = "POOR";
                        deptMetrics.put("health", health);

                        breakdown.add(deptMetrics);
                }

                return breakdown;
        }

        // ============================================================
        // ENGINEER ANALYTICS
        // ============================================================

        /**
         * Get engineer performance filtered by department.
         */
        public List<Map<String, Object>> getFilteredEngineerPerformance(String sessionToken,
                        String requestedDepartment, String period) {
                List<Ticket> tickets = getFilteredTickets(sessionToken, requestedDepartment);

                // Apply period filtering
                tickets = filterTicketsByPeriod(tickets, period);

                // Group tickets by assigned engineer
                Map<String, List<Ticket>> byEngineer = tickets.stream()
                                .filter(t -> t.getAssignedEngineer() != null)
                                .collect(Collectors.groupingBy(Ticket::getAssignedEngineer));

                List<Map<String, Object>> performance = new ArrayList<>();

                for (Map.Entry<String, List<Ticket>> entry : byEngineer.entrySet()) {
                        Map<String, Object> engPerf = new HashMap<>();
                        String engineerName = entry.getKey();
                        List<Ticket> engTickets = entry.getValue();

                        engPerf.put("engineer", engineerName);
                        engPerf.put("totalTickets", (long) engTickets.size());

                        long resolved = engTickets.stream()
                                        .filter(t -> "CLOSED".equals(t.getStatus()) || "RESOLVED".equals(t.getStatus()))
                                        .count();
                        engPerf.put("resolved", resolved);

                        long inProgress = engTickets.stream()
                                        .filter(t -> "IN_PROGRESS".equals(t.getStatus())
                                                        || "ASSIGNED".equals(t.getStatus()))
                                        .count();
                        engPerf.put("inProgress", inProgress);

                        long slaBreached = engTickets.stream()
                                        .filter(t -> Boolean.TRUE.equals(t.getSlaBreached()))
                                        .count();
                        engPerf.put("slaBreached", slaBreached);

                        // Calculate SLA Compliance percentage for this engineer
                        double slaCompliance = engTickets.size() > 0
                                        ? ((engTickets.size() - slaBreached) * 100.0 / engTickets.size())
                                        : 100.0;
                        engPerf.put("slaCompliance", Math.round(slaCompliance * 100.0) / 100.0);

                        // Calculate average resolution time from resolved tickets
                        List<Ticket> resolvedTickets = engTickets.stream()
                                        .filter(t -> ("CLOSED".equals(t.getStatus()) || "RESOLVED".equals(t.getStatus()))
                                                        && t.getClosedTime() != null)
                                        .collect(Collectors.toList());

                        double avgResolutionTime = 0.0;
                        if (!resolvedTickets.isEmpty()) {
                                long totalMinutes = resolvedTickets.stream()
                                        .mapToLong(t -> ChronoUnit.MINUTES.between(
                                                t.getCreatedTime(), t.getClosedTime()))
                                        .sum();
                                avgResolutionTime = totalMinutes / (double) resolvedTickets.size();
                        }
                        engPerf.put("avgResolutionTime", Math.round(avgResolutionTime * 100.0) / 100.0);

                        // Calculate average resolution time (mock - would need actual time tracking)
                        double resolutionRate = engTickets.size() > 0
                                        ? (resolved * 100.0 / engTickets.size())
                                        : 0.0;
                        engPerf.put("resolutionRate", Math.round(resolutionRate * 100.0) / 100.0);

                        // Extract team from ticket
                        String team = engTickets.stream()
                                        .filter(t -> t.getAssignedTeam() != null)
                                        .map(Ticket::getAssignedTeam)
                                        .findFirst()
                                        .orElse("Unknown");
                        engPerf.put("team", team);

                        performance.add(engPerf);
                }

                // Sort by total tickets (descending)
                performance.sort((a, b) -> ((Long) b.get("totalTickets")).compareTo((Long) a.get("totalTickets")));

                return performance;
        }

        // ============================================================
        // CATEGORY ANALYTICS
        // ============================================================

        /**
         * Get category volume filtered by department.
         */
        public Map<String, Object> getFilteredCategoryVolume(String sessionToken, String requestedDepartment, String period) {
                List<Ticket> tickets = getFilteredTickets(sessionToken, requestedDepartment);

                // Apply period filtering
                tickets = filterTicketsByPeriod(tickets, period);

                Map<String, Object> result = new HashMap<>();

                // Group by category
                Map<String, Long> byCategory = tickets.stream()
                                .filter(t -> t.getCategory() != null)
                                .collect(Collectors.groupingBy(Ticket::getCategory, Collectors.counting()));
                result.put("byCategory", byCategory);

                // Calculate time-based metrics per category
                Map<String, List<Ticket>> ticketsByCategory = tickets.stream()
                                .filter(t -> t.getCategory() != null)
                                .collect(Collectors.groupingBy(Ticket::getCategory));

                Map<String, Map<String, Object>> categoryMetrics = new HashMap<>();

                for (Map.Entry<String, List<Ticket>> entry : ticketsByCategory.entrySet()) {
                        String category = entry.getKey();
                        List<Ticket> categoryTickets = entry.getValue();

                        Map<String, Object> metrics = new HashMap<>();

                        // Calculate average first response time (using createdTime as proxy)
                        long totalResponseTime = 0;
                        long responseCount = 0;
                        long fastestResponse = Long.MAX_VALUE;
                        long slowestResponse = 0;

                        for (Ticket ticket : categoryTickets) {
                                if (ticket.getCreatedTime() != null && ticket.getClosedTime() != null) {
                                        long responseTime = ChronoUnit.MINUTES.between(
                                                ticket.getCreatedTime(), ticket.getClosedTime());
                                        totalResponseTime += responseTime;
                                        responseCount++;

                                        fastestResponse = Math.min(fastestResponse, responseTime);
                                        slowestResponse = Math.max(slowestResponse, responseTime);
                                }
                        }

                        long avgFirstResponse = responseCount > 0 ? totalResponseTime / responseCount : 0;
                        long avgResolution = avgFirstResponse; // Same calculation as first response
                        fastestResponse = fastestResponse == Long.MAX_VALUE ? 0 : fastestResponse;

                        metrics.put("avgFirstResponse", avgFirstResponse);
                        metrics.put("avgResolution", avgResolution);
                        metrics.put("fastestResponse", fastestResponse);
                        metrics.put("slowestResponse", slowestResponse);
                        metrics.put("ticketCount", categoryTickets.size());

                        categoryMetrics.put(category, metrics);
                }

                result.put("categoryMetrics", categoryMetrics);

                // Group by subcategory
                Map<String, Long> bySubCategory = tickets.stream()
                                .filter(t -> t.getSubCategory() != null)
                                .collect(Collectors.groupingBy(Ticket::getSubCategory, Collectors.counting()));
                result.put("bySubCategory", bySubCategory);

                // Group by status
                Map<String, Long> byStatus = tickets.stream()
                                .filter(t -> t.getStatus() != null)
                                .collect(Collectors.groupingBy(Ticket::getStatus, Collectors.counting()));
                result.put("byStatus", byStatus);

                result.put("totalTickets", tickets.size());
                result.put("department", requestedDepartment);

                return result;
        }

        // ============================================================
        // SUMMARY REPORT
        // ============================================================

        /**
         * Get management summary filtered by department.
         */
        public Map<String, Object> getFilteredManagementSummary(String sessionToken, String requestedDepartment, String period) {
                List<Ticket> tickets = getFilteredTickets(sessionToken, requestedDepartment);

                // Apply period filtering
                tickets = filterTicketsByPeriod(tickets, period);

                String effectiveDept = authService.getEffectiveDepartment(sessionToken, requestedDepartment);

                Map<String, Object> summary = new HashMap<>();

                // Basic counts
                summary.put("totalTickets", tickets.size());

                long openTickets = tickets.stream()
                                .filter(t -> "OPEN".equals(t.getStatus()) || "NEW".equals(t.getStatus()))
                                .count();
                summary.put("openTickets", openTickets);

                long inProgress = tickets.stream()
                                .filter(t -> "IN_PROGRESS".equals(t.getStatus()) || "ASSIGNED".equals(t.getStatus()))
                                .count();
                summary.put("inProgressTickets", inProgress);

                long resolved = tickets.stream()
                                .filter(t -> "CLOSED".equals(t.getStatus()) || "RESOLVED".equals(t.getStatus()))
                                .count();
                summary.put("resolvedTickets", resolved);

                // SLA metrics
                long slaBreached = tickets.stream()
                                .filter(t -> Boolean.TRUE.equals(t.getSlaBreached()))
                                .count();
                summary.put("slaBreached", slaBreached);

                double slaCompliance = tickets.size() > 0
                                ? ((tickets.size() - slaBreached) * 100.0 / tickets.size())
                                : 100.0;
                summary.put("slaComplianceRate", Math.round(slaCompliance * 100.0) / 100.0);

                // Priority breakdown
                Map<String, Long> byPriority = tickets.stream()
                                .filter(t -> t.getPriority() != null)
                                .collect(Collectors.groupingBy(Ticket::getPriority, Collectors.counting()));
                summary.put("byPriority", byPriority);

                // Department info
                if (effectiveDept != null) {
                        Department dept = Department.fromCode(effectiveDept);
                        summary.put("department", dept.getCode());
                        summary.put("departmentName", dept.getTeamName());
                } else {
                        summary.put("department", "ALL");
                        summary.put("departmentName", "All Departments");
                }

                summary.put("generatedAt", LocalDateTime.now());

                // Add 7-day trend data
                Map<String, Object> trends = generateTrendData(tickets);
                summary.put("trends", trends);

                return summary;
        }

        /**
         * Generate 7-day trend data for ticket creation and resolution
         */
        private Map<String, Object> generateTrendData(List<Ticket> tickets) {
                Map<String, Object> trends = new HashMap<>();

                // Labels for last 7 days
                List<String> labels = new ArrayList<>();
                List<Long> createdData = new ArrayList<>();
                List<Long> resolvedData = new ArrayList<>();

                LocalDateTime now = LocalDateTime.now();

                for (int i = 6; i >= 0; i--) {
                        LocalDateTime dayStart = now.minusDays(i).withHour(0).withMinute(0).withSecond(0);
                        LocalDateTime dayEnd = dayStart.withHour(23).withMinute(59).withSecond(59);

                        String dayLabel = dayStart.toLocalDate().toString().substring(5); // MM-dd format

                        long createdCount = tickets.stream()
                                        .filter(t -> t.getCreatedTime() != null &&
                                                t.getCreatedTime().isAfter(dayStart) &&
                                                t.getCreatedTime().isBefore(dayEnd))
                                        .count();

                        long resolvedCount = tickets.stream()
                                        .filter(t -> t.getClosedTime() != null &&
                                                t.getClosedTime().isAfter(dayStart) &&
                                                t.getClosedTime().isBefore(dayEnd))
                                        .count();

                        labels.add(dayLabel);
                        createdData.add(createdCount);
                        resolvedData.add(resolvedCount);
                }

                trends.put("labels", labels);
                trends.put("created", createdData);
                trends.put("resolved", resolvedData);

                return trends;
        }

        // ============================================================
        // PERIOD-BASED FILTERING
        // ============================================================

        /**
         * Filter tickets based on period (today, week, month)
         *
         * @param tickets  List of tickets to filter
         * @param period   Period filter: "today", "week", "month"
         * @return Filtered list of tickets
         */
        public List<Ticket> filterTicketsByPeriod(List<Ticket> tickets, String period) {
                if (tickets == null || period == null) {
                        return tickets;
                }

                LocalDateTime now = LocalDateTime.now();
                LocalDateTime periodStart;

                switch (period.toLowerCase()) {
                        case "today":
                                periodStart = now.withHour(0).withMinute(0).withSecond(0);
                                break;
                        case "week":
                                periodStart = now.minusDays(7).withHour(0).withMinute(0).withSecond(0);
                                break;
                        case "month":
                                periodStart = now.minusDays(30).withHour(0).withMinute(0).withSecond(0);
                                break;
                        default:
                                return tickets;
                }

                return tickets.stream()
                        .filter(t -> t.getCreatedTime() != null && t.getCreatedTime().isAfter(periodStart))
                        .collect(Collectors.toList());
        }

        // ============================================================
        // ACCESS VALIDATION
        // ============================================================

        /**
         * Validate that user can access requested department.
         * Throws SecurityException if access denied.
         */
        public void validateAccess(String sessionToken, String requestedDepartment) {
                if (!authService.canAccessDepartment(sessionToken, requestedDepartment)) {
                        ManagementUser user = authService.getCurrentUser(sessionToken);
                        throw new SecurityException(
                                        "Access denied. " + user.getRole().getDisplayName() +
                                                        " can only access " + user.getDepartmentDisplayName()
                                                        + " data.");
                }
        }

        /**
         * Get user's effective department (for response metadata).
         */
        public String getEffectiveDepartment(String sessionToken, String requestedDepartment) {
                return authService.getEffectiveDepartment(sessionToken, requestedDepartment);
        }
}
