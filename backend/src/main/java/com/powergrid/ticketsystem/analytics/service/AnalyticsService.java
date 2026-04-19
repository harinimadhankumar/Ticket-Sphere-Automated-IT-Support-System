package com.powergrid.ticketsystem.analytics.service;

import com.powergrid.ticketsystem.analytics.dto.*;
import com.powergrid.ticketsystem.analytics.repository.AnalyticsRepository;
import com.powergrid.ticketsystem.entity.Engineer;
import com.powergrid.ticketsystem.entity.Ticket;
import com.powergrid.ticketsystem.repository.EngineerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * ============================================================
 * ANALYTICS SERVICE
 * ============================================================
 * 
 * PHASE 9: REPORTS & ANALYTICS
 * 
 * PURPOSE:
 * ─────────
 * Central service for generating all management reports.
 * Aggregates data from repository and transforms into report DTOs.
 * 
 * WHY ANALYTICS IS REQUIRED FOR MANAGEMENT:
 * ─────────────────────────────────────────
 * 1. Data-Driven Decisions: Replace gut feelings with facts
 * 2. Resource Allocation: Know where to invest IT budget
 * 3. Performance Monitoring: Track team efficiency over time
 * 4. Problem Identification: Spot issues before they escalate
 * 5. Accountability: Track SLA compliance for SLAs
 * 
 * WHY THIS PHASE IS OPTIONAL BUT VALUABLE:
 * ────────────────────────────────────────
 * - Core ticket system works without it
 * - But management cannot make informed decisions
 * - Without analytics, improvements are guesswork
 * - Compliance reporting becomes manual effort
 * 
 * DESIGN PRINCIPLES:
 * ──────────────────
 * 1. READ-ONLY: No ticket modifications in this service
 * 2. TIME-FILTERED: All reports support time-based filtering
 * 3. RULE-BASED: No AI/ML, just deterministic calculations
 * 4. CACHED: Results can be cached for performance
 */
@Service
public class AnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);

    private final AnalyticsRepository analyticsRepository;
    private final EngineerRepository engineerRepository;

    public AnalyticsService(AnalyticsRepository analyticsRepository,
            EngineerRepository engineerRepository) {
        this.analyticsRepository = analyticsRepository;
        this.engineerRepository = engineerRepository;
        logger.info("╔══════════════════════════════════════════════════════════╗");
        logger.info("║       ANALYTICS SERVICE INITIALIZED (Phase 9)           ║");
        logger.info("║       Reports & Analytics for Management                ║");
        logger.info("╚══════════════════════════════════════════════════════════╝");
    }

    // ============================================================
    // TIME PERIOD HELPERS
    // ============================================================

    /**
     * Get start date for "Today" filter.
     */
    public LocalDateTime getStartOfToday() {
        return LocalDateTime.now().toLocalDate().atStartOfDay();
    }

    /**
     * Get start date for "Last 7 Days" filter.
     */
    public LocalDateTime getStartOfLast7Days() {
        return LocalDateTime.now().minusDays(7).toLocalDate().atStartOfDay();
    }

    /**
     * Get start date for "Last 30 Days" filter.
     */
    public LocalDateTime getStartOfLast30Days() {
        return LocalDateTime.now().minusDays(30).toLocalDate().atStartOfDay();
    }

    /**
     * Get period description based on start date.
     */
    public String getPeriodDescription(LocalDateTime startDate, LocalDateTime endDate) {
        long days = Duration.between(startDate, endDate).toDays();
        if (days <= 1) {
            return "Today";
        } else if (days <= 7) {
            return "Last 7 Days";
        } else if (days <= 30) {
            return "Last 30 Days";
        } else {
            return "Custom Period";
        }
    }

    // ============================================================
    // REPORT 1: SLA COMPLIANCE REPORT
    // ============================================================

    /**
     * Generate SLA Compliance Report.
     * 
     * WHAT IT SHOWS:
     * - Total tickets in period
     * - Tickets that met SLA
     * - Tickets that breached SLA
     * - SLA compliance percentage
     * 
     * @param startDate Start of reporting period
     * @param endDate   End of reporting period
     * @return SlaComplianceReport with all metrics
     */
    public SlaComplianceReport generateSlaComplianceReport(LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Generating SLA Compliance Report: {} to {}", startDate, endDate);

        SlaComplianceReport report = new SlaComplianceReport();
        report.setPeriodStart(startDate);
        report.setPeriodEnd(endDate);
        report.setPeriodDescription(getPeriodDescription(startDate, endDate));

        // Get counts from repository
        long totalTickets = analyticsRepository.countTicketsInPeriod(startDate, endDate);
        long slaBreached = analyticsRepository.countSlaBreachedTickets(startDate, endDate);
        long slaMet = analyticsRepository.countSlaMetTickets(startDate, endDate);

        report.setTotalTickets(totalTickets);
        report.setSlaBreached(slaBreached);
        report.setSlaMet(slaMet);
        report.calculateCompliance();

        // Calculate tickets at risk (SLA deadline within next 2 hours)
        LocalDateTime warningTime = LocalDateTime.now().plusHours(2);
        long atRisk = analyticsRepository.countTicketsAtRisk(startDate, endDate, warningTime);
        report.setTicketsAtRisk(atRisk);

        // Calculate average resolution time
        List<Ticket> resolvedTickets = analyticsRepository.findResolvedTicketsWithTime(startDate, endDate);
        if (!resolvedTickets.isEmpty()) {
            double totalHours = 0;
            int count = 0;
            for (Ticket t : resolvedTickets) {
                if (t.getClosedTime() != null && t.getCreatedTime() != null) {
                    long minutes = Duration.between(t.getCreatedTime(), t.getClosedTime()).toMinutes();
                    totalHours += minutes / 60.0;
                    count++;
                }
            }
            if (count > 0) {
                report.setAverageResolutionTimeHours(Math.round((totalHours / count) * 100.0) / 100.0);
            }
        }

        logger.info("SLA Compliance Report generated: {}% compliance", report.getSlaCompliancePercentage());
        return report;
    }

    // ============================================================
    // REPORT 2: ENGINEER PERFORMANCE REPORT
    // ============================================================

    /**
     * Generate performance reports for all engineers.
     * 
     * WHAT IT SHOWS (per engineer):
     * - Tickets assigned
     * - Tickets resolved
     * - Average resolution time
     * - SLA compliance rate
     * 
     * @param startDate Start of reporting period
     * @param endDate   End of reporting period
     * @return List of EngineerPerformanceReport for each engineer
     */
    public List<EngineerPerformanceReport> generateEngineerPerformanceReports(
            LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Generating Engineer Performance Reports: {} to {}", startDate, endDate);

        List<EngineerPerformanceReport> reports = new ArrayList<>();

        // Get all engineers
        List<Engineer> engineers = engineerRepository.findAll();

        // Get aggregated data from repository
        Map<String, Long> assignedCounts = toMap(analyticsRepository.countTicketsByEngineer(startDate, endDate));
        Map<String, Long> resolvedCounts = toMap(
                analyticsRepository.countResolvedTicketsByEngineer(startDate, endDate));
        Map<String, Long> breachCounts = toMap(analyticsRepository.countSlaBreachesByEngineer(startDate, endDate));
        Map<String, Long> openCounts = toMap(analyticsRepository.countOpenTicketsByEngineer());
        Map<String, Double> avgScores = toDoubleMap(
                analyticsRepository.getAverageVerificationScoreByEngineer(startDate, endDate));
        Map<String, Long> firstAttempts = toMap(
                analyticsRepository.countFirstAttemptSuccessByEngineer(startDate, endDate));

        for (Engineer engineer : engineers) {
            String name = engineer.getName();

            EngineerPerformanceReport report = new EngineerPerformanceReport();
            report.setEngineerId(engineer.getEngineerId());
            report.setEngineerName(name);
            report.setTeam(engineer.getTeam());
            report.setCurrentStatus(engineer.getStatus());
            report.setPeriodStart(startDate);
            report.setPeriodEnd(endDate);

            // Set counts
            report.setTicketsAssigned(assignedCounts.getOrDefault(name, 0L));
            report.setTicketsResolved(resolvedCounts.getOrDefault(name, 0L));
            report.setSlaBreachedCount(breachCounts.getOrDefault(name, 0L));
            report.setSlaMetCount(report.getTicketsResolved() - report.getSlaBreachedCount());
            report.setCurrentOpenTickets(openCounts.getOrDefault(name, 0L));
            report.setAverageVerificationScore(avgScores.getOrDefault(name, 0.0));
            report.setFirstAttemptSuccessCount(firstAttempts.getOrDefault(name, 0L));

            // Calculate average resolution time for this engineer
            List<Ticket> engineerTickets = analyticsRepository.findTicketsByEngineer(name, startDate, endDate);
            double avgResolutionTime = calculateAverageResolutionTime(engineerTickets);
            report.setAverageResolutionTimeHours(avgResolutionTime);

            // Calculate derived metrics
            report.calculateDerivedMetrics();

            reports.add(report);
        }

        // Sort by tickets resolved (descending)
        reports.sort((a, b) -> Long.compare(b.getTicketsResolved(), a.getTicketsResolved()));

        logger.info("Generated {} engineer performance reports", reports.size());
        return reports;
    }

    /**
     * Generate performance report for a single engineer.
     */
    public EngineerPerformanceReport generateEngineerPerformanceReport(
            String engineerId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Generating Performance Report for Engineer: {}", engineerId);

        Optional<Engineer> engineerOpt = engineerRepository.findByEngineerId(engineerId);
        if (engineerOpt.isEmpty()) {
            logger.warn("Engineer not found: {}", engineerId);
            return null;
        }

        Engineer engineer = engineerOpt.get();
        String name = engineer.getName();

        EngineerPerformanceReport report = new EngineerPerformanceReport();
        report.setEngineerId(engineer.getEngineerId());
        report.setEngineerName(name);
        report.setTeam(engineer.getTeam());
        report.setCurrentStatus(engineer.getStatus());
        report.setPeriodStart(startDate);
        report.setPeriodEnd(endDate);

        // Get ticket data
        List<Ticket> tickets = analyticsRepository.findTicketsByEngineer(name, startDate, endDate);

        long assigned = tickets.size();
        long resolved = tickets.stream()
                .filter(t -> "RESOLVED".equals(t.getStatus()) || "CLOSED".equals(t.getStatus()))
                .count();
        long breached = tickets.stream()
                .filter(t -> Boolean.TRUE.equals(t.getSlaBreached()))
                .count();

        report.setTicketsAssigned(assigned);
        report.setTicketsResolved(resolved);
        report.setSlaBreachedCount(breached);
        report.setSlaMetCount(resolved - breached);
        report.setAverageResolutionTimeHours(calculateAverageResolutionTime(tickets));
        report.calculateDerivedMetrics();

        return report;
    }

    // ============================================================
    // REPORT 3: CATEGORY VOLUME REPORT
    // ============================================================

    /**
     * Generate Category Volume Report.
     * 
     * WHAT IT SHOWS:
     * - Number of tickets per category
     * - Percentage breakdown
     * - Sub-category details
     * 
     * @param startDate Start of reporting period
     * @param endDate   End of reporting period
     * @return CategoryVolumeReport with all category data
     */
    public CategoryVolumeReport generateCategoryVolumeReport(LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Generating Category Volume Report: {} to {}", startDate, endDate);

        CategoryVolumeReport report = new CategoryVolumeReport();
        report.setPeriodStart(startDate);
        report.setPeriodEnd(endDate);
        report.setPeriodDescription(getPeriodDescription(startDate, endDate));

        // Get category counts
        List<Object[]> categoryCounts = analyticsRepository.countTicketsByCategory(startDate, endDate);

        long totalTickets = 0;
        for (Object[] row : categoryCounts) {
            String category = (String) row[0];
            Long count = (Long) row[1];

            CategoryVolumeReport.CategoryCount catCount = new CategoryVolumeReport.CategoryCount(category, count);

            // Get sub-category breakdown
            List<Object[]> subCounts = analyticsRepository.countTicketsBySubCategory(category, startDate, endDate);
            for (Object[] subRow : subCounts) {
                String subCategory = (String) subRow[0];
                Long subCount = (Long) subRow[1];
                catCount.getSubCategories().add(new CategoryVolumeReport.SubCategoryCount(subCategory, subCount));
            }

            report.getCategories().add(catCount);
            totalTickets += count;
        }

        report.setTotalTickets(totalTickets);
        report.calculatePercentages();

        logger.info("Category Volume Report generated: {} categories, {} total tickets",
                report.getCategories().size(), totalTickets);
        return report;
    }

    // ============================================================
    // EXTRA 2: TOP SLA BREACHING CATEGORY REPORT
    // ============================================================

    /**
     * Generate Top SLA Breaching Category Report.
     * 
     * WHAT IT SHOWS:
     * - Categories ranked by SLA breach count
     * - Sub-category breakdown for each
     * - Breach percentages
     * 
     * @param startDate Start of reporting period
     * @param endDate   End of reporting period
     * @return TopSlaBreachingReport with breach analysis
     */
    public TopSlaBreachingReport generateTopSlaBreachingReport(LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Generating Top SLA Breaching Report: {} to {}", startDate, endDate);

        TopSlaBreachingReport report = new TopSlaBreachingReport();
        report.setPeriodStart(startDate);
        report.setPeriodEnd(endDate);
        report.setPeriodDescription(getPeriodDescription(startDate, endDate));

        // Get breach counts by category
        List<Object[]> breachData = analyticsRepository.countSlaBreachesByCategory(startDate, endDate);

        long totalBreaches = 0;
        long totalTickets = 0;
        int rank = 1;

        for (Object[] row : breachData) {
            String category = (String) row[0];
            Long breachCount = (Long) row[1];
            Long categoryTotal = (Long) row[2];

            TopSlaBreachingReport.CategoryBreachInfo info = new TopSlaBreachingReport.CategoryBreachInfo(
                    category, breachCount, categoryTotal);
            info.setRank(rank++);

            // Get sub-category breaches
            List<Object[]> subBreaches = analyticsRepository.countSlaBreachesBySubCategory(
                    category, startDate, endDate);
            for (Object[] subRow : subBreaches) {
                String subCategory = (String) subRow[0];
                Long subBreachCount = (Long) subRow[1];
                info.getSubCategories().add(
                        new TopSlaBreachingReport.SubCategoryBreachInfo(subCategory, subBreachCount));
            }

            report.getTopBreachingCategories().add(info);
            totalBreaches += breachCount;
            totalTickets += categoryTotal;
        }

        report.setTotalBreaches(totalBreaches);
        report.setTotalTickets(totalTickets);
        if (totalTickets > 0) {
            report.setOverallBreachPercentage(Math.round(((double) totalBreaches / totalTickets) * 10000.0) / 100.0);
        }

        logger.info("Top SLA Breaching Report generated: {} breaches across {} categories",
                totalBreaches, report.getTopBreachingCategories().size());
        return report;
    }

    // ============================================================
    // EXTRA 3: PEAK TICKET HOURS REPORT
    // ============================================================

    /**
     * Generate Peak Ticket Hours Report.
     * 
     * WHAT IT SHOWS:
     * - Hourly distribution of ticket creation
     * - Peak hour identification
     * - Quiet hour identification
     * 
     * PURPOSE:
     * - Staffing decisions
     * - Shift planning
     * - Resource allocation by time of day
     * 
     * @param startDate Start of reporting period
     * @param endDate   End of reporting period
     * @return PeakHoursReport with hourly distribution
     */
    public PeakHoursReport generatePeakHoursReport(LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Generating Peak Hours Report: {} to {}", startDate, endDate);

        PeakHoursReport report = new PeakHoursReport();
        report.setPeriodStart(startDate);
        report.setPeriodEnd(endDate);
        report.setPeriodDescription(getPeriodDescription(startDate, endDate));

        // Get hourly counts
        List<Object[]> hourlyData = analyticsRepository.countTicketsByHour(startDate, endDate);

        // Initialize all 24 hours with 0
        Map<Integer, Long> hourMap = new HashMap<>();
        for (int i = 0; i < 24; i++) {
            hourMap.put(i, 0L);
        }

        // Fill in actual counts
        for (Object[] row : hourlyData) {
            Integer hour = (Integer) row[0];
            Long count = (Long) row[1];
            hourMap.put(hour, count);
        }

        // Convert to hourly count list
        for (int i = 0; i < 24; i++) {
            report.getHourlyDistribution().add(new PeakHoursReport.HourlyCount(i, hourMap.get(i)));
        }

        // Calculate peaks
        report.calculatePeakHours();

        logger.info("Peak Hours Report generated: Peak at {} with {} tickets",
                report.getPeakTimeWindow(), report.getPeakHourCount());
        return report;
    }

    // ============================================================
    // EXTRA 4: MANAGEMENT SUMMARY (RULE-BASED)
    // ============================================================

    /**
     * Generate Auto Management Summary.
     * 
     * IMPORTANT: This uses RULE-BASED logic, NOT AI/ML.
     * 
     * WHY RULE-BASED IS ENOUGH:
     * 1. Deterministic: Same data = same summary
     * 2. Explainable: Every insight can be traced to a rule
     * 3. No training data required
     * 4. No external dependencies
     * 5. Fast and reliable
     * 
     * RULES IMPLEMENTED:
     * - If SLA compliance < 80%, alert
     * - If category increased by >20%, highlight
     * - If engineer workload imbalanced, recommend
     * - Identify top performer
     * - Identify problem category
     * 
     * @param startDate Start of reporting period
     * @param endDate   End of reporting period
     * @return ManagementSummary with findings and recommendations
     */
    public ManagementSummary generateManagementSummary(LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Generating Management Summary: {} to {}", startDate, endDate);

        ManagementSummary summary = new ManagementSummary();
        summary.setPeriodStart(startDate);
        summary.setPeriodEnd(endDate);
        summary.setPeriodDescription(getPeriodDescription(startDate, endDate));

        // ==============================
        // Gather all data needed
        // ==============================
        SlaComplianceReport slaReport = generateSlaComplianceReport(startDate, endDate);
        CategoryVolumeReport categoryReport = generateCategoryVolumeReport(startDate, endDate);
        TopSlaBreachingReport breachReport = generateTopSlaBreachingReport(startDate, endDate);
        List<EngineerPerformanceReport> engineerReports = generateEngineerPerformanceReports(startDate, endDate);

        summary.setSlaCompliance(slaReport.getSlaCompliancePercentage());

        // ==============================
        // RULE 1: SLA Compliance Alerts
        // ==============================
        if (slaReport.getSlaCompliancePercentage() < 70) {
            summary.addAlert("CRITICAL: SLA compliance is severely low at " +
                    slaReport.getSlaCompliancePercentage() + "%");
            summary.addRecommendation("Immediately review escalation procedures and add additional staff");
        } else if (slaReport.getSlaCompliancePercentage() < 80) {
            summary.addAlert("WARNING: SLA compliance below target at " +
                    slaReport.getSlaCompliancePercentage() + "%");
            summary.addRecommendation("Review ticket assignment process and engineer workloads");
        } else if (slaReport.getSlaCompliancePercentage() >= 95) {
            summary.addHighlight("Excellent SLA compliance: " + slaReport.getSlaCompliancePercentage() + "%");
        }

        // ==============================
        // RULE 2: Tickets at Risk Alert
        // ==============================
        if (slaReport.getTicketsAtRisk() > 0) {
            summary.addAlert(slaReport.getTicketsAtRisk() + " tickets are at risk of SLA breach");
        }

        // ==============================
        // RULE 3: Top Breaching Category
        // ==============================
        if (!breachReport.getTopBreachingCategories().isEmpty()) {
            TopSlaBreachingReport.CategoryBreachInfo topBreach = breachReport.getTopBreachingCategories().get(0);
            if (topBreach.getBreachCount() > 0) {
                summary.setProblemCategory(topBreach.getCategory());
                summary.addFinding(topBreach.getCategory() + " category has highest SLA breaches: " +
                        topBreach.getBreachCount() + " (" + topBreach.getBreachPercentage() + "% of category)");

                // Check for specific sub-category issues
                if (!topBreach.getSubCategories().isEmpty()) {
                    TopSlaBreachingReport.SubCategoryBreachInfo topSub = topBreach.getSubCategories().get(0);
                    summary.addFinding("Within " + topBreach.getCategory() + ", " +
                            topSub.getSubCategory() + " causes most breaches: " + topSub.getBreachCount());
                    summary.addRecommendation("Increase " + topBreach.getCategory() +
                            " support capacity, especially for " + topSub.getSubCategory() + " issues");
                }
            }
        }

        // ==============================
        // RULE 4: Top Category Finding
        // ==============================
        if (!categoryReport.getCategories().isEmpty()) {
            CategoryVolumeReport.CategoryCount topCategory = categoryReport.getCategories().get(0);
            summary.addFinding("Most tickets are in " + topCategory.getCategory() +
                    " category: " + topCategory.getCount() + " (" + topCategory.getPercentage() + "%)");
        }

        // ==============================
        // RULE 5: Engineer Performance
        // ==============================
        if (!engineerReports.isEmpty()) {
            // Find top performer (highest resolution rate with > 5 tickets)
            EngineerPerformanceReport topPerformer = engineerReports.stream()
                    .filter(e -> e.getTicketsResolved() >= 5)
                    .max(Comparator.comparingDouble(EngineerPerformanceReport::getResolutionRate))
                    .orElse(null);

            if (topPerformer != null) {
                summary.setTopPerformer(topPerformer.getEngineerName());
                summary.addHighlight("Top performer: " + topPerformer.getEngineerName() +
                        " with " + topPerformer.getTicketsResolved() + " tickets resolved" +
                        " (" + topPerformer.getResolutionRate() + "% resolution rate)");
            }

            // Find engineers with high workload
            for (EngineerPerformanceReport eng : engineerReports) {
                if (eng.getCurrentOpenTickets() > 8) {
                    summary.addAlert("Engineer " + eng.getEngineerName() + " has high workload: " +
                            eng.getCurrentOpenTickets() + " open tickets");
                    summary.addRecommendation("Consider redistributing tickets from " + eng.getEngineerName());
                }
            }

            // Check for engineers with low SLA compliance
            for (EngineerPerformanceReport eng : engineerReports) {
                if (eng.getTicketsResolved() >= 5 && eng.getSlaComplianceRate() < 70) {
                    summary.addFinding("Engineer " + eng.getEngineerName() +
                            " has low SLA compliance: " + eng.getSlaComplianceRate() + "%");
                    summary.addRecommendation("Provide additional training or support to " + eng.getEngineerName());
                }
            }
        }

        // ==============================
        // RULE 6: Generate Headline
        // ==============================
        if (slaReport.getSlaCompliancePercentage() < 80) {
            summary.setHeadline("SLA Compliance Needs Attention - " +
                    slaReport.getSlaCompliancePercentage() + "% compliance rate");
        } else if (slaReport.getTotalTickets() > 100) {
            summary.setHeadline("High Volume Period - " + slaReport.getTotalTickets() +
                    " tickets with " + slaReport.getSlaCompliancePercentage() + "% SLA compliance");
        } else {
            summary.setHeadline("IT Support Summary - " + slaReport.getTotalTickets() +
                    " tickets, " + slaReport.getSlaCompliancePercentage() + "% SLA compliance");
        }

        // ==============================
        // Calculate Health Score
        // ==============================
        summary.calculateHealthScore();

        // ==============================
        // Determine Trends
        // ==============================
        // (Simplified: Would compare with previous period in full implementation)
        if (slaReport.getSlaCompliancePercentage() >= 90) {
            summary.setSlaTrend("STABLE");
        } else {
            summary.setSlaTrend("NEEDS_IMPROVEMENT");
        }

        logger.info("Management Summary generated: Health Score = {}, Status = {}",
                summary.getHealthScore(), summary.getHealthStatus());
        return summary;
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Convert List of Object[] to Map<String, Long>.
     */
    private Map<String, Long> toMap(List<Object[]> data) {
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : data) {
            String key = (String) row[0];
            Long value = (Long) row[1];
            if (key != null) {
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * Convert List of Object[] to Map<String, Double>.
     */
    private Map<String, Double> toDoubleMap(List<Object[]> data) {
        Map<String, Double> map = new HashMap<>();
        for (Object[] row : data) {
            String key = (String) row[0];
            Double value = (Double) row[1];
            if (key != null) {
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * Calculate average resolution time from ticket list.
     */
    private double calculateAverageResolutionTime(List<Ticket> tickets) {
        double totalHours = 0;
        int count = 0;

        for (Ticket t : tickets) {
            if (t.getClosedTime() != null && t.getCreatedTime() != null) {
                long minutes = Duration.between(t.getCreatedTime(), t.getClosedTime()).toMinutes();
                totalHours += minutes / 60.0;
                count++;
            }
        }

        if (count > 0) {
            return Math.round((totalHours / count) * 100.0) / 100.0;
        }
        return 0.0;
    }
}
