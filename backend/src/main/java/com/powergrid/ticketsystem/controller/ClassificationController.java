package com.powergrid.ticketsystem.controller;

import com.powergrid.ticketsystem.dto.ApiResponse;
import com.powergrid.ticketsystem.dto.TicketResponse;
import com.powergrid.ticketsystem.entity.Ticket;
import com.powergrid.ticketsystem.nlp.ClassificationService;
import com.powergrid.ticketsystem.nlp.ClassificationService.ClassificationResult;
import com.powergrid.ticketsystem.notification.NotificationService;
import com.powergrid.ticketsystem.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ============================================================
 * CLASSIFICATION CONTROLLER - NLP Classification REST APIs
 * ============================================================
 * 
 * Phase 2 & 3: NLP-Based Classification and Assignment
 * 
 * Provides REST APIs to:
 * 1. Classify a single ticket
 * 2. Classify all unclassified tickets
 * 3. Re-classify a ticket
 * 4. Get classification statistics
 * 
 * BASE URL: /api/classification
 * 
 * ENDPOINTS:
 * - POST /api/classification/classify/{ticketId} → Classify single ticket
 * - POST /api/classification/classify-all → Classify all pending
 * - POST /api/classification/reclassify/{ticketId} → Re-classify ticket
 * - GET /api/classification/stats → Classification stats
 * - POST /api/classification/test → Test classification
 */
@RestController
@RequestMapping("/api/classification")
public class ClassificationController {

    private static final Logger logger = LoggerFactory.getLogger(ClassificationController.class);

    private final ClassificationService classificationService;
    private final TicketRepository ticketRepository;
    private NotificationService notificationService;

    public ClassificationController(ClassificationService classificationService,
            TicketRepository ticketRepository) {
        this.classificationService = classificationService;
        this.ticketRepository = ticketRepository;
    }

    /**
     * Setter injection for NotificationService (Phase 8).
     */
    @Autowired
    @Lazy
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
        logger.info("NotificationService injected into Classification Controller");
    }

    // ============================================================
    // CLASSIFY SINGLE TICKET
    // ============================================================

    /**
     * Classifies a single ticket by its ID.
     * 
     * ENDPOINT: POST /api/classification/classify/{ticketId}
     * 
     * FLOW:
     * 1. Fetch ticket from database
     * 2. Run through classification pipeline
     * 3. Update ticket with classification results
     * 4. Save to database
     * 5. Return classified ticket
     * 
     * @param ticketId Ticket ID to classify
     * @return Classified ticket
     */
    @PostMapping("/classify/{ticketId}")
    public ResponseEntity<ApiResponse<TicketResponse>> classifyTicket(
            @PathVariable String ticketId) {

        logger.info("API Request: Classify ticket {}", ticketId);

        try {
            // Fetch ticket
            Optional<Ticket> optionalTicket = ticketRepository.findByTicketId(ticketId);

            if (optionalTicket.isEmpty()) {
                logger.warn("Ticket not found: {}", ticketId);
                return new ResponseEntity<>(
                        ApiResponse.error("Ticket not found: " + ticketId),
                        HttpStatus.NOT_FOUND);
            }

            Ticket ticket = optionalTicket.get();

            // Check if already classified
            if (Boolean.TRUE.equals(ticket.getIsClassified())) {
                logger.info("Ticket {} is already classified. Use reclassify endpoint to force.", ticketId);
                return new ResponseEntity<>(
                        ApiResponse.error("Ticket already classified. Use /reclassify endpoint to re-classify."),
                        HttpStatus.BAD_REQUEST);
            }

            // Classify ticket
            TicketResponse classifiedTicket = classifyAndSaveTicket(ticket);

            logger.info("Ticket {} classified successfully", ticketId);
            return ResponseEntity.ok(
                    ApiResponse.success("Ticket classified successfully", classifiedTicket));

        } catch (Exception e) {
            logger.error("Error classifying ticket {}: {}", ticketId, e.getMessage(), e);
            return new ResponseEntity<>(
                    ApiResponse.error("Classification failed: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ============================================================
    // CLASSIFY ALL UNCLASSIFIED TICKETS
    // ============================================================

    /**
     * Classifies all tickets that haven't been classified yet.
     * 
     * ENDPOINT: POST /api/classification/classify-all
     * 
     * @return Summary of classification results
     */
    @PostMapping("/classify-all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> classifyAllPending() {

        logger.info("API Request: Classify all unclassified tickets");

        try {
            // Find unclassified tickets
            List<Ticket> unclassifiedTickets = ticketRepository.findByIsClassifiedFalseOrIsClassifiedIsNull();

            if (unclassifiedTickets.isEmpty()) {
                logger.info("No unclassified tickets found");
                return ResponseEntity.ok(
                        ApiResponse.success("No tickets to classify", createEmptyStats()));
            }

            logger.info("Found {} unclassified tickets", unclassifiedTickets.size());

            // Classify each ticket
            int successCount = 0;
            int failCount = 0;
            List<String> classifiedIds = new ArrayList<>();
            List<String> failedIds = new ArrayList<>();

            for (Ticket ticket : unclassifiedTickets) {
                try {
                    classifyAndSaveTicket(ticket);
                    successCount++;
                    classifiedIds.add(ticket.getTicketId());
                } catch (Exception e) {
                    logger.error("Failed to classify ticket {}: {}",
                            ticket.getTicketId(), e.getMessage());
                    failCount++;
                    failedIds.add(ticket.getTicketId());
                }
            }

            // Build result summary
            Map<String, Object> result = new HashMap<>();
            result.put("totalProcessed", unclassifiedTickets.size());
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("classifiedTicketIds", classifiedIds);
            result.put("failedTicketIds", failedIds);

            logger.info("Classification complete. Success: {}, Failed: {}", successCount, failCount);
            return ResponseEntity.ok(
                    ApiResponse.success("Classification completed", result));

        } catch (Exception e) {
            logger.error("Error in bulk classification: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ApiResponse.error("Bulk classification failed: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ============================================================
    // RE-CLASSIFY TICKET
    // ============================================================

    /**
     * Re-classifies a ticket (even if already classified).
     * 
     * ENDPOINT: POST /api/classification/reclassify/{ticketId}
     * 
     * @param ticketId Ticket ID to re-classify
     * @return Re-classified ticket
     */
    @PostMapping("/reclassify/{ticketId}")
    public ResponseEntity<ApiResponse<TicketResponse>> reclassifyTicket(
            @PathVariable String ticketId) {

        logger.info("API Request: Re-classify ticket {}", ticketId);

        try {
            Optional<Ticket> optionalTicket = ticketRepository.findByTicketId(ticketId);

            if (optionalTicket.isEmpty()) {
                return new ResponseEntity<>(
                        ApiResponse.error("Ticket not found: " + ticketId),
                        HttpStatus.NOT_FOUND);
            }

            Ticket ticket = optionalTicket.get();

            // Force re-classification
            TicketResponse classifiedTicket = classifyAndSaveTicket(ticket);

            logger.info("Ticket {} re-classified successfully", ticketId);
            return ResponseEntity.ok(
                    ApiResponse.success("Ticket re-classified successfully", classifiedTicket));

        } catch (Exception e) {
            logger.error("Error re-classifying ticket {}: {}", ticketId, e.getMessage(), e);
            return new ResponseEntity<>(
                    ApiResponse.error("Re-classification failed: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ============================================================
    // CLASSIFICATION STATISTICS
    // ============================================================

    /**
     * Gets classification statistics.
     * 
     * ENDPOINT: GET /api/classification/stats
     * 
     * @return Classification statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getClassificationStats() {

        logger.info("API Request: Get classification statistics");

        try {
            Map<String, Object> stats = new HashMap<>();

            // Total tickets
            long totalTickets = ticketRepository.count();
            stats.put("totalTickets", totalTickets);

            // Classified vs unclassified
            List<Ticket> unclassified = ticketRepository.findByIsClassifiedFalseOrIsClassifiedIsNull();
            long classifiedCount = totalTickets - unclassified.size();
            stats.put("classifiedCount", classifiedCount);
            stats.put("unclassifiedCount", unclassified.size());
            stats.put("classificationRate", totalTickets > 0 ? (double) classifiedCount / totalTickets * 100 : 0);

            // Counts by category
            Map<String, Long> categoryStats = new HashMap<>();
            for (Ticket ticket : ticketRepository.findAll()) {
                if (ticket.getCategory() != null) {
                    categoryStats.merge(ticket.getCategory(), 1L, Long::sum);
                }
            }
            stats.put("byCategory", categoryStats);

            // Counts by priority
            Map<String, Long> priorityStats = new HashMap<>();
            for (Ticket ticket : ticketRepository.findAll()) {
                if (ticket.getPriority() != null) {
                    priorityStats.merge(ticket.getPriority(), 1L, Long::sum);
                }
            }
            stats.put("byPriority", priorityStats);

            return ResponseEntity.ok(ApiResponse.success("Statistics retrieved", stats));

        } catch (Exception e) {
            logger.error("Error getting classification stats: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ApiResponse.error("Failed to get statistics: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ============================================================
    // TEST CLASSIFICATION (without saving)
    // ============================================================

    /**
     * Tests classification without saving to database.
     * Useful for testing the NLP pipeline.
     * 
     * ENDPOINT: POST /api/classification/test
     * 
     * Body: { "text": "VPN not connecting from home" }
     * 
     * @param request Request containing text to classify
     * @return Classification result
     */
    @PostMapping("/test")
    public ResponseEntity<ApiResponse<ClassificationResult>> testClassification(
            @RequestBody Map<String, String> request) {

        String text = request.get("text");

        if (text == null || text.trim().isEmpty()) {
            return new ResponseEntity<>(
                    ApiResponse.error("Text is required for classification test"),
                    HttpStatus.BAD_REQUEST);
        }

        logger.info("API Request: Test classification for text: {}",
                text.length() > 50 ? text.substring(0, 50) + "..." : text);

        try {
            ClassificationResult result = classificationService.classifyTicket(text);

            return ResponseEntity.ok(
                    ApiResponse.success("Classification test completed", result));

        } catch (Exception e) {
            logger.error("Error in classification test: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ApiResponse.error("Classification test failed: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Classifies a ticket and saves the results to database.
     */
    private TicketResponse classifyAndSaveTicket(Ticket ticket) {
        // Run classification
        ClassificationResult result = classificationService.classifyTicket(
                ticket.getIssueDescription());

        // Update ticket with classification results
        ticket.setCategory(result.getCategory().name());
        ticket.setSubCategory(result.getSubCategory().name());
        ticket.setPriority(result.getPriority().name());
        ticket.setAssignedTeam(result.getTeam().getTeamName());
        ticket.setAssignedEngineer(result.getAssignedEngineer());
        ticket.setConfidenceScore(result.getConfidenceScore());
        ticket.setIsClassified(true);
        ticket.setClassifiedTime(LocalDateTime.now());

        // Update status to ASSIGNED
        ticket.setStatus("ASSIGNED");

        // Save to database
        Ticket savedTicket = ticketRepository.save(ticket);

        // ================================================================
        // PHASE 8: SEND TICKET_ASSIGNED NOTIFICATION
        // ================================================================
        try {
            if (notificationService != null && savedTicket.getAssignedEngineer() != null) {
                notificationService.notifyTicketAssigned(savedTicket);
            }
        } catch (Exception e) {
            logger.warn("Failed to send TICKET_ASSIGNED notification for {}: {}",
                    savedTicket.getTicketId(), e.getMessage());
        }

        // Convert to response
        return convertToResponse(savedTicket);
    }

    /**
     * Converts Ticket entity to TicketResponse DTO.
     */
    private TicketResponse convertToResponse(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setTicketId(ticket.getTicketId());
        response.setSource(ticket.getSource());
        response.setEmployeeId(ticket.getEmployeeId());
        response.setIssueDescription(ticket.getIssueDescription());
        response.setCreatedTime(ticket.getCreatedTime());
        response.setStatus(ticket.getStatus());
        response.setSenderEmail(ticket.getSenderEmail());
        response.setEmailSubject(ticket.getEmailSubject());
        response.setCategory(ticket.getCategory());
        response.setSubCategory(ticket.getSubCategory());
        response.setPriority(ticket.getPriority());
        response.setAssignedTeam(ticket.getAssignedTeam());
        response.setAssignedEngineer(ticket.getAssignedEngineer());
        response.setConfidenceScore(ticket.getConfidenceScore());
        response.setIsClassified(ticket.getIsClassified());
        response.setClassifiedTime(ticket.getClassifiedTime());
        return response;
    }

    /**
     * Creates empty stats map.
     */
    private Map<String, Object> createEmptyStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProcessed", 0);
        stats.put("successCount", 0);
        stats.put("failCount", 0);
        return stats;
    }
}
