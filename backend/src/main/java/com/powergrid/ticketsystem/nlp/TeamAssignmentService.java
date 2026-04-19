package com.powergrid.ticketsystem.nlp;

import com.powergrid.ticketsystem.constants.Category;
import com.powergrid.ticketsystem.constants.Priority;
import com.powergrid.ticketsystem.constants.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ============================================================
 * TEAM ASSIGNMENT SERVICE
 * ============================================================
 * 
 * Phase 3: Intelligent Team Assignment
 * 
 * PURPOSE:
 * Assigns tickets to the correct team and engineer based on:
 * 1. Ticket category (determines team)
 * 2. Engineer workload (load balancing)
 * 3. Ticket priority (high priority → less loaded engineer)
 * 
 * LOAD BALANCING ALGORITHM:
 * - Track active tickets per engineer
 * - Assign to engineer with lowest workload
 * - Consider priority weight (CRITICAL counts as 3 tickets)
 * 
 * WHY INTELLIGENT ASSIGNMENT:
 * - Even workload distribution
 * - Faster resolution times
 * - No manual admin intervention
 * - Fair work distribution
 */
@Service
public class TeamAssignmentService {

    private static final Logger logger = LoggerFactory.getLogger(TeamAssignmentService.class);

    // ============================================================
    // ASSIGNMENT RESULT DTO
    // ============================================================

    /**
     * Result object for ticket assignment operations.
     */
    public static class AssignmentResult {
        private Team team;
        private String engineer;
        private String reason;

        public AssignmentResult(Team team, String engineer, String reason) {
            this.team = team;
            this.engineer = engineer;
            this.reason = reason;
        }

        public Team getTeam() {
            return team;
        }

        public String getEngineer() {
            return engineer;
        }

        public String getReason() {
            return reason;
        }
    }

    // ============================================================
    // ENGINEER DATA STRUCTURES
    // ============================================================

    // Map: Team → List of Engineers
    private final Map<Team, List<String>> teamEngineers = new HashMap<>();

    // Map: Engineer → Current active ticket count
    private final Map<String, Integer> engineerWorkload = new ConcurrentHashMap<>();

    /**
     * Initialize engineer data on service startup.
     * 
     * NOTE: In production, this would come from a database.
     * For demo purposes, we use hardcoded sample data.
     */
    @PostConstruct
    public void initializeEngineers() {
        logger.info("Initializing team engineers");

        // Network Team Engineers
        teamEngineers.put(Team.NETWORK_TEAM, Arrays.asList(
                "Rahul Sharma (NET)",
                "Priya Singh (NET)",
                "Amit Kumar (NET)"));

        // Application Support Team Engineers
        teamEngineers.put(Team.APPLICATION_SUPPORT, Arrays.asList(
                "Sneha Patel (APP)",
                "Vikram Reddy (APP)",
                "Neha Gupta (APP)",
                "Arjun Mehta (APP)"));

        // Hardware Support Team Engineers
        teamEngineers.put(Team.HARDWARE_SUPPORT, Arrays.asList(
                "Rajesh Verma (HW)",
                "Anita Joshi (HW)"));

        // IT Security Team Engineers
        teamEngineers.put(Team.IT_SECURITY, Arrays.asList(
                "Kiran Rao (SEC)",
                "Deepak Nair (SEC)"));

        // Email Support Team Engineers
        teamEngineers.put(Team.EMAIL_SUPPORT, Arrays.asList(
                "Sunita Sharma (EMAIL)",
                "Manoj Iyer (EMAIL)"));

        // General Support Team Engineers
        teamEngineers.put(Team.GENERAL_SUPPORT, Arrays.asList(
                "Ramesh Kumar (GEN)",
                "Sita Devi (GEN)"));

        // Initialize workload counters
        for (List<String> engineers : teamEngineers.values()) {
            for (String engineer : engineers) {
                engineerWorkload.put(engineer, 0);
            }
        }

        logger.info("Initialized {} teams with engineers", teamEngineers.size());
        logTeamStats();
    }

    /**
     * Assigns an engineer from the specified team.
     * 
     * ALGORITHM:
     * 1. Get all engineers in the team
     * 2. Find engineer with lowest workload
     * 3. Increment their workload counter
     * 4. Return engineer name
     * 
     * @param team     Team to assign from
     * @param priority Ticket priority (affects load calculation)
     * @return Assigned engineer name
     */
    public String assignEngineer(Team team, Priority priority) {
        logger.info("Assigning engineer from team: {}", team.getTeamName());

        List<String> engineers = teamEngineers.get(team);

        if (engineers == null || engineers.isEmpty()) {
            logger.warn("No engineers found for team: {}. Using fallback.", team);
            return "Unassigned - No engineer available";
        }

        // Find engineer with lowest workload
        String selectedEngineer = null;
        int lowestWorkload = Integer.MAX_VALUE;

        for (String engineer : engineers) {
            int workload = engineerWorkload.getOrDefault(engineer, 0);
            logger.debug("  {} has {} active tickets", engineer, workload);

            if (workload < lowestWorkload) {
                lowestWorkload = workload;
                selectedEngineer = engineer;
            }
        }

        // Increment workload based on priority
        int workloadIncrement = getWorkloadWeight(priority);
        int newWorkload = engineerWorkload.getOrDefault(selectedEngineer, 0) + workloadIncrement;
        engineerWorkload.put(selectedEngineer, newWorkload);

        logger.info("Assigned {} (workload: {} → {})",
                selectedEngineer, lowestWorkload, newWorkload);

        return selectedEngineer;
    }

    /**
     * Assigns a ticket to the appropriate team and engineer based on category and
     * priority.
     * This is the main entry point for Phase 4 Self-Service escalation.
     * 
     * @param category Category of the ticket (determines team)
     * @param priority Priority string (e.g., "HIGH", "MEDIUM")
     * @return AssignmentResult with team and engineer assignment
     */
    public AssignmentResult assignTicket(String category, String priority) {
        logger.info("Assigning ticket - Category: {}, Priority: {}", category, priority);

        // Determine team from category
        Team team = getTeamForCategory(category);

        // Parse priority
        Priority priorityEnum = Priority.MEDIUM; // default
        if (priority != null) {
            try {
                priorityEnum = Priority.valueOf(priority.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Unknown priority: {}. Using MEDIUM.", priority);
            }
        }

        // Assign engineer
        String engineer = assignEngineer(team, priorityEnum);

        return new AssignmentResult(team, engineer,
                "Assigned to " + team.getTeamName() + " based on category: " + category);
    }

    /**
     * Get the appropriate team for a ticket category.
     */
    private Team getTeamForCategory(String category) {
        if (category == null) {
            return Team.GENERAL_SUPPORT;
        }

        try {
            Category cat = Category.valueOf(category.toUpperCase());
            switch (cat) {
                case NETWORK:
                    return Team.NETWORK_TEAM;
                case SOFTWARE:
                    return Team.APPLICATION_SUPPORT;
                case HARDWARE:
                    return Team.HARDWARE_SUPPORT;
                case ACCESS:
                    return Team.IT_SECURITY;
                case EMAIL:
                    return Team.EMAIL_SUPPORT;
                default:
                    return Team.GENERAL_SUPPORT;
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Unknown category: {}. Using GENERAL_SUPPORT.", category);
            return Team.GENERAL_SUPPORT;
        }
    }

    /**
     * Gets workload weight based on priority.
     * 
     * Higher priority tickets count as more workload because
     * they require more attention and faster resolution.
     */
    private int getWorkloadWeight(Priority priority) {
        switch (priority) {
            case CRITICAL:
                return 3; // Critical tickets count as 3 normal tickets
            case HIGH:
                return 2; // High priority counts as 2
            case MEDIUM:
            case LOW:
            default:
                return 1; // Normal weight
        }
    }

    /**
     * Releases workload when a ticket is resolved.
     * Should be called when ticket status changes to RESOLVED/CLOSED.
     * 
     * @param engineerName Name of the engineer
     * @param priority     Priority of the resolved ticket
     */
    public void releaseWorkload(String engineerName, Priority priority) {
        if (engineerName == null || !engineerWorkload.containsKey(engineerName)) {
            logger.warn("Cannot release workload - engineer not found: {}", engineerName);
            return;
        }

        int weight = getWorkloadWeight(priority);
        int currentWorkload = engineerWorkload.get(engineerName);
        int newWorkload = Math.max(0, currentWorkload - weight);

        engineerWorkload.put(engineerName, newWorkload);

        logger.info("Released workload for {}: {} → {}",
                engineerName, currentWorkload, newWorkload);
    }

    /**
     * Gets current workload for an engineer.
     */
    public int getEngineerWorkload(String engineerName) {
        return engineerWorkload.getOrDefault(engineerName, 0);
    }

    /**
     * Gets all engineers for a team.
     */
    public List<String> getTeamEngineers(Team team) {
        return teamEngineers.getOrDefault(team, Collections.emptyList());
    }

    /**
     * Gets workload summary for all engineers.
     */
    public Map<String, Integer> getAllWorkloads() {
        return new HashMap<>(engineerWorkload);
    }

    /**
     * Gets workload summary for a specific team.
     */
    public Map<String, Integer> getTeamWorkload(Team team) {
        Map<String, Integer> teamWorkload = new HashMap<>();
        List<String> engineers = teamEngineers.get(team);

        if (engineers != null) {
            for (String engineer : engineers) {
                teamWorkload.put(engineer, engineerWorkload.getOrDefault(engineer, 0));
            }
        }

        return teamWorkload;
    }

    /**
     * Release workload from an engineer.
     * Called when a ticket is closed, reassigned, or self-resolved.
     * 
     * @param engineerName Name of the engineer
     */
    public void releaseEngineerWorkload(String engineerName) {
        if (engineerName == null || !engineerWorkload.containsKey(engineerName)) {
            return;
        }

        int currentWorkload = engineerWorkload.get(engineerName);
        if (currentWorkload > 0) {
            engineerWorkload.put(engineerName, currentWorkload - 1);
            logger.debug("Released workload for {}. New count: {}",
                    engineerName, engineerWorkload.get(engineerName));
        }
    }

    /**
     * Reset workload for an engineer (for testing/admin purposes).
     * 
     * @param engineerName Name of the engineer
     */
    public void resetEngineerWorkload(String engineerName) {
        if (engineerWorkload.containsKey(engineerName)) {
            engineerWorkload.put(engineerName, 0);
            logger.info("Reset workload for {}", engineerName);
        }
    }

    /**
     * Logs team statistics.
     */
    private void logTeamStats() {
        logger.info("Team Statistics:");
        for (Map.Entry<Team, List<String>> entry : teamEngineers.entrySet()) {
            logger.info("  {} ({} engineers): {}",
                    entry.getKey().getTeamName(),
                    entry.getValue().size(),
                    entry.getValue());
        }
    }
}
