package com.powergrid.ticketsystem.nlp;

import com.powergrid.ticketsystem.constants.Category;
import com.powergrid.ticketsystem.constants.Priority;
import com.powergrid.ticketsystem.constants.SubCategory;
import com.powergrid.ticketsystem.constants.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Classification Service - Orchestrates ticket classification pipeline */
@Service
public class ClassificationService {

    private static final Logger logger = LoggerFactory.getLogger(ClassificationService.class);

    private final IntentDetectionService intentService;
    private final PriorityEngine priorityEngine;
    private final TeamAssignmentService teamAssignmentService;

    public ClassificationService(IntentDetectionService intentService,
            PriorityEngine priorityEngine,
            TeamAssignmentService teamAssignmentService) {
        this.intentService = intentService;
        this.priorityEngine = priorityEngine;
        this.teamAssignmentService = teamAssignmentService;
    }

    /**
     * Classifies a ticket: detects category, priority, assigns team and engineer
     */
    public ClassificationResult classifyTicket(String rawText) {
        logger.info("========================================");
        logger.info("Starting ticket classification pipeline");
        logger.info("========================================");
        logger.info("Input text: {}", truncateText(rawText, 100));

        // Detect Intent
        logger.info("Step 1: Intent Detection");
        IntentDetectionService.IntentResult intent = intentService.detectFullIntent(rawText);
        Category category = intent.getCategory();
        SubCategory subCategory = intent.getSubCategory();
        logger.info("  → Category: {}", category);
        logger.info("  → SubCategory: {}", subCategory);

        // Step 2: Determine Priority
        logger.info("Step 2: Priority Calculation");
        Priority priority = priorityEngine.determinePriority(rawText, category, subCategory);
        logger.info("  → Priority: {}", priority);

        // Step 3: Assign Team
        logger.info("Step 3: Team Assignment");
        Team team = Team.getTeamForCategory(category);
        logger.info("  → Team: {}", team.getTeamName());

        // Step 4: Assign Engineer (with load balancing)
        logger.info("Step 4: Engineer Assignment");
        String engineer = teamAssignmentService.assignEngineer(team, priority);
        logger.info("  → Engineer: {}", engineer);

        // Build result
        ClassificationResult result = new ClassificationResult();
        result.setCategory(category);
        result.setSubCategory(subCategory);
        result.setPriority(priority);
        result.setTeam(team);
        result.setAssignedEngineer(engineer);
        result.setConfidenceScore(calculateConfidence(rawText, category));

        logger.info("========================================");
        logger.info("Classification Complete: {}", result);
        logger.info("========================================");

        return result;
    }

    /** Calculates confidence score based on text length and category */
    private double calculateConfidence(String rawText, Category category) {
        if (category == Category.UNKNOWN) {
            return 0.3; // Low confidence for unknown
        }

        // Simple confidence calculation
        // In production, this would be more sophisticated
        int textLength = rawText.length();

        if (textLength > 100) {
            return 0.9; // Long, detailed description
        } else if (textLength > 50) {
            return 0.8; // Medium description
        } else if (textLength > 20) {
            return 0.7; // Short description
        } else {
            return 0.5; // Very short
        }
    }

    private String truncateText(String text, int maxLength) {
        if (text == null)
            return "";
        if (text.length() <= maxLength)
            return text;
        return text.substring(0, maxLength) + "...";
    }

    /** Classification result holder */
    public static class ClassificationResult {
        private Category category;
        private SubCategory subCategory;
        private Priority priority;
        private Team team;
        private String assignedEngineer;
        private double confidenceScore;

        // Getters and Setters
        public Category getCategory() {
            return category;
        }

        public void setCategory(Category category) {
            this.category = category;
        }

        public SubCategory getSubCategory() {
            return subCategory;
        }

        public void setSubCategory(SubCategory subCategory) {
            this.subCategory = subCategory;
        }

        public Priority getPriority() {
            return priority;
        }

        public void setPriority(Priority priority) {
            this.priority = priority;
        }

        public Team getTeam() {
            return team;
        }

        public void setTeam(Team team) {
            this.team = team;
        }

        public String getAssignedEngineer() {
            return assignedEngineer;
        }

        public void setAssignedEngineer(String assignedEngineer) {
            this.assignedEngineer = assignedEngineer;
        }

        public double getConfidenceScore() {
            return confidenceScore;
        }

        public void setConfidenceScore(double confidenceScore) {
            this.confidenceScore = confidenceScore;
        }

        @Override
        public String toString() {
            return "ClassificationResult{" +
                    "category=" + category +
                    ", subCategory=" + subCategory +
                    ", priority=" + priority +
                    ", team=" + (team != null ? team.getTeamName() : "null") +
                    ", engineer='" + assignedEngineer + '\'' +
                    ", confidence=" + String.format("%.2f", confidenceScore) +
                    '}';
        }
    }
}
