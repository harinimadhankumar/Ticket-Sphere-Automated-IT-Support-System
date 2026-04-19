package com.powergrid.ticketsystem.controller;

import com.powergrid.ticketsystem.constants.SlaConfiguration;
import com.powergrid.ticketsystem.entity.Ticket;
import com.powergrid.ticketsystem.repository.TicketRepository;
import com.powergrid.ticketsystem.sla.EscalationService;
import com.powergrid.ticketsystem.sla.SlaCalculationService;
import com.powergrid.ticketsystem.sla.SlaMonitoringScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ============================================================
 * SLA CONTROLLER
 * ============================================================
 * 
 * PHASE 5: SLA MONITORING & AUTO ESCALATION
 * 
 * REST API endpoints for SLA monitoring, manual checks, and statistics.
 * 
 * ╔═══════════════════════════════════════════════════════════╗
 * ║ AVAILABLE ENDPOINTS ║
 * ╠═══════════════════════════════════════════════════════════╣
 * ║ GET /api/sla/status/{ticketId} - Get SLA status ║
 * ║ GET /api/sla/breached - List breached ║
 * ║ GET /api/sla/approaching - Approaching breach ║
 * ║ GET /api/sla/statistics - SLA statistics ║
 * ║ GET /api/sla/escalation-stats - Escalation stats ║
 * ║ POST /api/sla/check - Manual check ║
 * ║ POST /api/sla/check/{ticketId} - Check single ║
 * ║ GET /api/sla/configuration - View SLA config ║
 * ║ GET /api/sla/dashboard - Dashboard data ║
 * ╚═══════════════════════════════════════════════════════════╝
 */
@RestController
@RequestMapping("/api/sla")
@CrossOrigin(origins = "*")
public class SlaController {

    private static final Logger logger = LoggerFactory.getLogger(SlaController.class);

    private final TicketRepository ticketRepository;
    private final SlaCalculationService slaCalculationService;
    private final EscalationService escalationService;
    private final SlaMonitoringScheduler monitoringScheduler;

    public SlaController(TicketRepository ticketRepository,
            SlaCalculationService slaCalculationService,
            EscalationService escalationService,
            SlaMonitoringScheduler monitoringScheduler) {
        this.ticketRepository = ticketRepository;
        this.slaCalculationService = slaCalculationService;
        this.escalationService = escalationService;
        this.monitoringScheduler = monitoringScheduler;
        logger.info("SLA Controller initialized");
    }

    /**
     * ┌─────────────────────────────────────────────────────────────┐
     * │ GET /api/sla/status/{ticketId} │
     * │ Get detailed SLA status for a specific ticket │
     * └─────────────────────────────────────────────────────────────┘
     */
    @GetMapping("/status/{ticketId}")
    public ResponseEntity<?> getSlaStatus(@PathVariable String ticketId) {
        logger.info("Getting SLA status for ticket: {}", ticketId);

        Optional<Ticket> ticketOpt = ticketRepository.findByTicketId(ticketId);
        if (ticketOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Ticket ticket = ticketOpt.get();
        SlaCalculationService.SlaStatus slaStatus = slaCalculationService.getSlaStatus(ticket);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("ticketId", ticket.getTicketId());
        response.put("priority", ticket.getPriority());
        response.put("status", ticket.getStatus());
        response.put("createdTime", ticket.getCreatedTime());
        response.put("slaDeadline", slaStatus.getSlaDeadline());
        response.put("slaHours", SlaConfiguration.getSlaHoursForPriority(ticket.getPriority()));
        response.put("elapsedHours", slaStatus.getElapsedHours());
        response.put("remainingMinutes",
                slaStatus.getRemainingTime() != null ? slaStatus.getRemainingTime().toMinutes() : -1);
        response.put("slaPercentage", String.format("%.1f%%", slaStatus.getSlaPercentage()));
        response.put("isBreached", slaStatus.isBreached());
        response.put("overtimeHours", slaStatus.getOvertimeHours());
        response.put("escalationLevel", slaStatus.getEscalationLevel().name());
        response.put("escalationRequired", slaStatus.isEscalationRequired());
        response.put("inWarningZone", slaStatus.isInWarningZone());
        response.put("formattedStatus", slaStatus.getFormattedStatus());

        return ResponseEntity.ok(response);
    }

    /**
     * ┌─────────────────────────────────────────────────────────────┐
     * │ GET /api/sla/breached │
     * │ Get all tickets with breached SLA │
     * └─────────────────────────────────────────────────────────────┘
     */
    @GetMapping("/breached")
    public ResponseEntity<?> getBreachedTickets(
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String team) {

        logger.info("Getting breached tickets - Priority: {}, Team: {}", priority, team);

        List<Ticket> breachedTickets;

        if (priority != null && !priority.isEmpty()) {
            breachedTickets = ticketRepository.findByPriorityAndSlaBreachedTrue(priority);
        } else if (team != null && !team.isEmpty()) {
            breachedTickets = ticketRepository.findByAssignedTeamAndSlaBreachedTrue(team);
        } else {
            breachedTickets = ticketRepository.findBySlaBreachedTrue();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Ticket ticket : breachedTickets) {
            Map<String, Object> ticketData = buildTicketSummary(ticket);
            result.add(ticketData);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", result.size());
        response.put("breachedTickets", result);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * ┌─────────────────────────────────────────────────────────────┐
     * │ GET /api/sla/approaching │
     * │ Get tickets approaching SLA breach (warning zone) │
     * └─────────────────────────────────────────────────────────────┘
     */
    @GetMapping("/approaching")
    public ResponseEntity<?> getApproachingDeadline(
            @RequestParam(defaultValue = "30") int thresholdMinutes) {

        logger.info("Getting tickets approaching SLA (within {} minutes)", thresholdMinutes);

        LocalDateTime threshold = LocalDateTime.now().plusMinutes(thresholdMinutes);
        List<Ticket> approachingTickets = ticketRepository.findTicketsApproachingSlaSimple(threshold);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Ticket ticket : approachingTickets) {
            Map<String, Object> ticketData = buildTicketSummary(ticket);
            long remainingMinutes = slaCalculationService.calculateRemainingTime(ticket).toMinutes();
            ticketData.put("remainingMinutes", remainingMinutes);
            ticketData.put("urgency", getUrgencyLevel(remainingMinutes));
            result.add(ticketData);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("thresholdMinutes", thresholdMinutes);
        response.put("count", result.size());
        response.put("tickets", result);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * ┌─────────────────────────────────────────────────────────────┐
     * │ GET /api/sla/statistics │
     * │ Get overall SLA statistics │
     * └─────────────────────────────────────────────────────────────┘
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getSlaStatistics() {
        logger.info("Getting SLA statistics");

        Map<String, Object> stats = new LinkedHashMap<>();

        // Overall counts
        List<Ticket> activeTickets = ticketRepository.findAllActiveTickets();
        long totalActive = activeTickets.size();
        long totalBreached = ticketRepository.countBySlaBreachedTrue();

        // Calculate SLA compliance rate
        double complianceRate = totalActive > 0 ? ((double) (totalActive - totalBreached) / totalActive) * 100 : 100;

        stats.put("totalActiveTickets", totalActive);
        stats.put("totalBreached", totalBreached);
        stats.put("slaComplianceRate", String.format("%.1f%%", complianceRate));

        // Escalation counts
        Map<String, Long> escalationCounts = new LinkedHashMap<>();
        escalationCounts.put("LEVEL_1", ticketRepository.countByEscalationLevel("LEVEL_1"));
        escalationCounts.put("LEVEL_2", ticketRepository.countByEscalationLevel("LEVEL_2"));
        escalationCounts.put("LEVEL_3", ticketRepository.countByEscalationLevel("LEVEL_3"));
        stats.put("escalationCounts", escalationCounts);

        // Breaches by priority
        Map<String, Long> breachesByPriority = new LinkedHashMap<>();
        breachesByPriority.put("CRITICAL", ticketRepository.countByPriorityAndSlaBreachedTrue("CRITICAL"));
        breachesByPriority.put("HIGH", ticketRepository.countByPriorityAndSlaBreachedTrue("HIGH"));
        breachesByPriority.put("MEDIUM", ticketRepository.countByPriorityAndSlaBreachedTrue("MEDIUM"));
        breachesByPriority.put("LOW", ticketRepository.countByPriorityAndSlaBreachedTrue("LOW"));
        stats.put("breachesByPriority", breachesByPriority);

        // Average SLA percentage for active tickets
        double avgSlaPercentage = calculateAverageSlaPercentage(activeTickets);
        stats.put("averageSlaUsage", String.format("%.1f%%", avgSlaPercentage));

        // Warning zone count
        long inWarningZone = activeTickets.stream()
                .filter(t -> slaCalculationService.isInWarningZone(t))
                .count();
        stats.put("ticketsInWarningZone", inWarningZone);

        stats.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(stats);
    }

    /**
     * ┌─────────────────────────────────────────────────────────────┐
     * │ GET /api/sla/escalation-stats │
     * │ Get detailed escalation statistics │
     * └─────────────────────────────────────────────────────────────┘
     */
    @GetMapping("/escalation-stats")
    public ResponseEntity<?> getEscalationStatistics() {
        logger.info("Getting escalation statistics");

        Map<String, Object> stats = escalationService.getEscalationStatistics();
        stats.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(stats);
    }

    /**
     * ┌─────────────────────────────────────────────────────────────┐
     * │ POST /api/sla/check │
     * │ Trigger manual SLA check for all active tickets │
     * └─────────────────────────────────────────────────────────────┘
     */
    @PostMapping("/check")
    public ResponseEntity<?> triggerManualCheck() {
        logger.info("Manual SLA check triggered");

        SlaMonitoringScheduler.MonitoringResult result = monitoringScheduler.triggerManualCheck();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Manual SLA check completed");
        response.put("ticketsChecked", result.getTicketsChecked());
        response.put("ticketsEscalated", result.getTicketsEscalated());
        response.put("ticketsInBreach", result.getTicketsInBreach());
        response.put("durationMs", result.getDurationMs());
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * ┌─────────────────────────────────────────────────────────────┐
     * │ POST /api/sla/check/{ticketId} │
     * │ Check and escalate a single ticket │
     * └─────────────────────────────────────────────────────────────┘
     */
    @PostMapping("/check/{ticketId}")
    public ResponseEntity<?> checkSingleTicket(@PathVariable String ticketId) {
        logger.info("Checking SLA for single ticket: {}", ticketId);

        Optional<Ticket> ticketOpt = ticketRepository.findByTicketId(ticketId);
        if (ticketOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Ticket ticket = ticketOpt.get();
        EscalationService.EscalationResult result = escalationService.checkAndEscalate(ticket);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("ticketId", ticketId);
        response.put("escalated", result.isEscalated());
        response.put("message", result.getMessage());
        response.put("currentStatus", result.getTicket().getStatus());
        response.put("escalationLevel", result.getTicket().getEscalationLevel());
        response.put("assignedEngineer", result.getTicket().getAssignedEngineer());
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * ┌─────────────────────────────────────────────────────────────┐
     * │ GET /api/sla/configuration │
     * │ Get current SLA configuration │
     * └─────────────────────────────────────────────────────────────┘
     */
    @GetMapping("/configuration")
    public ResponseEntity<?> getSlaConfiguration() {
        logger.info("Getting SLA configuration");

        Map<String, Object> config = new LinkedHashMap<>();

        // SLA times by priority
        Map<String, Object> slaTimes = new LinkedHashMap<>();
        slaTimes.put("CRITICAL", SlaConfiguration.CRITICAL_SLA_HOURS + " hours");
        slaTimes.put("HIGH", SlaConfiguration.HIGH_SLA_HOURS + " hours");
        slaTimes.put("MEDIUM", SlaConfiguration.MEDIUM_SLA_HOURS + " hours");
        slaTimes.put("LOW", SlaConfiguration.LOW_SLA_HOURS + " hours");
        config.put("slaTimes", slaTimes);

        // Escalation thresholds
        Map<String, Object> thresholds = new LinkedHashMap<>();
        thresholds.put("warningThreshold", (SlaConfiguration.WARNING_THRESHOLD_PERCENT * 100) + "%");
        thresholds.put("level1Threshold", (SlaConfiguration.LEVEL_1_THRESHOLD_PERCENT * 100) + "%");
        thresholds.put("level2Threshold", (SlaConfiguration.LEVEL_2_THRESHOLD_PERCENT * 100) + "%");
        thresholds.put("level3Threshold", (SlaConfiguration.LEVEL_3_THRESHOLD_PERCENT * 100) + "%");
        config.put("escalationThresholds", thresholds);

        // Escalation contacts
        Map<String, Object> contacts = new LinkedHashMap<>();
        contacts.put("level1", SlaConfiguration.LEVEL_1_ESCALATION_CONTACT);
        contacts.put("level2", SlaConfiguration.LEVEL_2_ESCALATION_CONTACT);
        contacts.put("level3", SlaConfiguration.LEVEL_3_ESCALATION_CONTACT);
        config.put("escalationContacts", contacts);

        // Monitoring settings
        Map<String, Object> monitoring = monitoringScheduler.getMonitoringStatistics();
        config.put("monitoring", monitoring);

        return ResponseEntity.ok(config);
    }

    /**
     * ┌─────────────────────────────────────────────────────────────┐
     * │ GET /api/sla/dashboard │
     * │ Get dashboard data for SLA monitoring │
     * └─────────────────────────────────────────────────────────────┘
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardData() {
        logger.info("Getting SLA dashboard data");

        Map<String, Object> dashboard = new LinkedHashMap<>();

        // Summary cards
        List<Ticket> activeTickets = ticketRepository.findAllActiveTickets();
        long totalActive = activeTickets.size();
        long breached = ticketRepository.countBySlaBreachedTrue();
        long inWarning = activeTickets.stream()
                .filter(t -> slaCalculationService.isInWarningZone(t))
                .count();
        long onTrack = totalActive - breached - inWarning;

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalActive", totalActive);
        summary.put("breached", breached);
        summary.put("inWarning", inWarning);
        summary.put("onTrack", Math.max(0, onTrack));
        dashboard.put("summary", summary);

        // Critical tickets requiring attention
        List<Map<String, Object>> criticalTickets = new ArrayList<>();
        List<Ticket> criticalBreached = ticketRepository.findByPriorityAndSlaBreachedTrue("CRITICAL");
        for (Ticket t : criticalBreached) {
            criticalTickets.add(buildTicketSummary(t));
        }
        dashboard.put("criticalBreachedTickets", criticalTickets);

        // Escalation breakdown
        Map<String, Long> escalationBreakdown = new LinkedHashMap<>();
        escalationBreakdown.put("none", ticketRepository.countByEscalationLevel("NONE"));
        escalationBreakdown.put("level1", ticketRepository.countByEscalationLevel("LEVEL_1"));
        escalationBreakdown.put("level2", ticketRepository.countByEscalationLevel("LEVEL_2"));
        escalationBreakdown.put("level3", ticketRepository.countByEscalationLevel("LEVEL_3"));
        dashboard.put("escalationBreakdown", escalationBreakdown);

        // Recent escalations (last 24 hours)
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        List<Ticket> recentBreaches = ticketRepository.findBySlaBreachTimeBetween(oneDayAgo, LocalDateTime.now());
        List<Map<String, Object>> recentEscalations = new ArrayList<>();
        for (Ticket t : recentBreaches) {
            recentEscalations.add(buildTicketSummary(t));
        }
        dashboard.put("recentEscalations", recentEscalations);

        // SLA compliance by priority
        Map<String, Object> complianceByPriority = new LinkedHashMap<>();
        for (String priority : Arrays.asList("CRITICAL", "HIGH", "MEDIUM", "LOW")) {
            long total = activeTickets.stream()
                    .filter(t -> priority.equals(t.getPriority()))
                    .count();
            long priorityBreached = ticketRepository.countByPriorityAndSlaBreachedTrue(priority);
            double compliance = total > 0 ? ((double) (total - priorityBreached) / total) * 100 : 100;
            complianceByPriority.put(priority, String.format("%.1f%%", compliance));
        }
        dashboard.put("complianceByPriority", complianceByPriority);

        dashboard.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(dashboard);
    }

    /**
     * ┌─────────────────────────────────────────────────────────────┐
     * │ GET /api/sla/by-escalation-level/{level} │
     * │ Get tickets by escalation level │
     * └─────────────────────────────────────────────────────────────┘
     */
    @GetMapping("/by-escalation-level/{level}")
    public ResponseEntity<?> getByEscalationLevel(@PathVariable String level) {
        logger.info("Getting tickets by escalation level: {}", level);

        List<Ticket> tickets = ticketRepository.findByEscalationLevel(level.toUpperCase());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Ticket ticket : tickets) {
            result.add(buildTicketSummary(ticket));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("escalationLevel", level.toUpperCase());
        response.put("count", result.size());
        response.put("tickets", result);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * ┌─────────────────────────────────────────────────────────────┐
     * │ GET /api/sla/breaches/range │
     * │ Get SLA breaches within a date range │
     * └─────────────────────────────────────────────────────────────┘
     */
    @GetMapping("/breaches/range")
    public ResponseEntity<?> getBreachesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        logger.info("Getting SLA breaches between {} and {}", startDate, endDate);

        List<Ticket> breaches = ticketRepository.findBySlaBreachTimeBetween(startDate, endDate);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Ticket ticket : breaches) {
            result.add(buildTicketSummary(ticket));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("startDate", startDate);
        response.put("endDate", endDate);
        response.put("count", result.size());
        response.put("breaches", result);

        return ResponseEntity.ok(response);
    }

    // ================= HELPER METHODS =================

    /**
     * Build ticket summary for API response.
     */
    private Map<String, Object> buildTicketSummary(Ticket ticket) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("ticketId", ticket.getTicketId());
        summary.put("subject", ticket.getEmailSubject());
        summary.put("priority", ticket.getPriority());
        summary.put("category", ticket.getCategory());
        summary.put("status", ticket.getStatus());
        summary.put("assignedTeam", ticket.getAssignedTeam());
        summary.put("assignedEngineer", ticket.getAssignedEngineer());
        summary.put("createdTime", ticket.getCreatedTime());
        summary.put("slaDeadline", ticket.getSlaDeadline());
        summary.put("slaBreached", ticket.getSlaBreached());
        summary.put("slaBreachTime", ticket.getSlaBreachTime());
        summary.put("escalationLevel", ticket.getEscalationLevel());
        summary.put("escalationCount", ticket.getEscalationCount());
        summary.put("slaPercentage", String.format("%.1f%%",
                slaCalculationService.calculateSlaPercentage(ticket)));
        return summary;
    }

    /**
     * Calculate average SLA percentage across tickets.
     */
    private double calculateAverageSlaPercentage(List<Ticket> tickets) {
        if (tickets.isEmpty()) {
            return 0.0;
        }
        double total = tickets.stream()
                .mapToDouble(t -> slaCalculationService.calculateSlaPercentage(t))
                .sum();
        return total / tickets.size();
    }

    /**
     * Get urgency level based on remaining minutes.
     */
    private String getUrgencyLevel(long remainingMinutes) {
        if (remainingMinutes <= 15) {
            return "CRITICAL";
        } else if (remainingMinutes <= 30) {
            return "HIGH";
        } else if (remainingMinutes <= 60) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
}
