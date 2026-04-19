package com.powergrid.ticketsystem.controller;

import com.powergrid.ticketsystem.constants.Category;
import com.powergrid.ticketsystem.constants.Priority;
import com.powergrid.ticketsystem.constants.SubCategory;
import com.powergrid.ticketsystem.constants.Team;
import com.powergrid.ticketsystem.dto.ApiResponse;
import com.powergrid.ticketsystem.nlp.TeamAssignmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * ============================================================
 * TEAM CONTROLLER - Team & Engineer Management REST APIs
 * ============================================================
 * 
 * Phase 3: Intelligent Team Assignment
 * 
 * Provides REST APIs to:
 * 1. View all teams
 * 2. View engineers in a team
 * 3. View engineer workloads
 * 4. View categories, sub-categories, priorities
 * 
 * BASE URL: /api/teams
 */
@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private static final Logger logger = LoggerFactory.getLogger(TeamController.class);

    private final TeamAssignmentService teamAssignmentService;

    public TeamController(TeamAssignmentService teamAssignmentService) {
        this.teamAssignmentService = teamAssignmentService;
    }

    // ============================================================
    // GET ALL TEAMS
    // ============================================================

    /**
     * Gets all available teams.
     * 
     * ENDPOINT: GET /api/teams
     * 
     * @return List of all teams
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllTeams() {

        logger.info("API Request: Get all teams");

        List<Map<String, Object>> teams = new ArrayList<>();

        for (Team team : Team.values()) {
            Map<String, Object> teamInfo = new HashMap<>();
            teamInfo.put("code", team.getTeamCode());
            teamInfo.put("name", team.getTeamName());
            teamInfo.put("handlesCategory", team.getHandlesCategory().name());
            teamInfo.put("engineers", teamAssignmentService.getTeamEngineers(team));
            teams.add(teamInfo);
        }

        return ResponseEntity.ok(
                ApiResponse.success("Teams retrieved successfully", teams, teams.size()));
    }

    // ============================================================
    // GET TEAM BY CODE
    // ============================================================

    /**
     * Gets a specific team by its code.
     * 
     * ENDPOINT: GET /api/teams/{teamCode}
     * 
     * @param teamCode Team code (NET, APP, HW, SEC, EMAIL, GEN)
     * @return Team details
     */
    @GetMapping("/{teamCode}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTeamByCode(
            @PathVariable String teamCode) {

        logger.info("API Request: Get team by code: {}", teamCode);

        for (Team team : Team.values()) {
            if (team.getTeamCode().equalsIgnoreCase(teamCode)) {
                Map<String, Object> teamInfo = new HashMap<>();
                teamInfo.put("code", team.getTeamCode());
                teamInfo.put("name", team.getTeamName());
                teamInfo.put("handlesCategory", team.getHandlesCategory().name());
                teamInfo.put("engineers", teamAssignmentService.getTeamEngineers(team));
                teamInfo.put("workloads", teamAssignmentService.getTeamWorkload(team));

                return ResponseEntity.ok(
                        ApiResponse.success("Team found", teamInfo));
            }
        }

        return ResponseEntity.ok(
                ApiResponse.error("Team not found: " + teamCode));
    }

    // ============================================================
    // GET ENGINEER WORKLOADS
    // ============================================================

    /**
     * Gets current workload for all engineers.
     * 
     * ENDPOINT: GET /api/teams/workloads
     * 
     * @return Engineer workloads
     */
    @GetMapping("/workloads")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getAllWorkloads() {

        logger.info("API Request: Get all engineer workloads");

        Map<String, Integer> workloads = teamAssignmentService.getAllWorkloads();

        return ResponseEntity.ok(
                ApiResponse.success("Workloads retrieved", workloads));
    }

    // ============================================================
    // GET ALL CATEGORIES
    // ============================================================

    /**
     * Gets all ticket categories.
     * 
     * ENDPOINT: GET /api/teams/categories
     * 
     * @return List of categories
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllCategories() {

        logger.info("API Request: Get all categories");

        List<Map<String, Object>> categories = new ArrayList<>();

        for (Category category : Category.values()) {
            Map<String, Object> catInfo = new HashMap<>();
            catInfo.put("name", category.name());
            catInfo.put("displayName", category.getDisplayName());
            catInfo.put("defaultTeam", category.getDefaultTeam());

            // Get sub-categories for this category
            List<Map<String, String>> subCats = new ArrayList<>();
            for (SubCategory sub : SubCategory.getByCategory(category)) {
                Map<String, String> subInfo = new HashMap<>();
                subInfo.put("name", sub.name());
                subInfo.put("displayName", sub.getDisplayName());
                subCats.add(subInfo);
            }
            catInfo.put("subCategories", subCats);

            categories.add(catInfo);
        }

        return ResponseEntity.ok(
                ApiResponse.success("Categories retrieved", categories, categories.size()));
    }

    // ============================================================
    // GET ALL PRIORITIES
    // ============================================================

    /**
     * Gets all priority levels.
     * 
     * ENDPOINT: GET /api/teams/priorities
     * 
     * @return List of priorities
     */
    @GetMapping("/priorities")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllPriorities() {

        logger.info("API Request: Get all priorities");

        List<Map<String, Object>> priorities = new ArrayList<>();

        for (Priority priority : Priority.values()) {
            Map<String, Object> prioInfo = new HashMap<>();
            prioInfo.put("name", priority.name());
            prioInfo.put("displayName", priority.getDisplayName());
            prioInfo.put("level", priority.getLevel());
            prioInfo.put("slaMinutes", priority.getSlaMinutes());
            prioInfo.put("description", priority.getDescription());
            priorities.add(prioInfo);
        }

        return ResponseEntity.ok(
                ApiResponse.success("Priorities retrieved", priorities, priorities.size()));
    }
}
