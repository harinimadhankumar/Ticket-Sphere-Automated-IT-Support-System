package com.powergrid.ticketsystem.controller;

import com.powergrid.ticketsystem.dto.ApiResponse;
import com.powergrid.ticketsystem.dto.TicketResponse;
import com.powergrid.ticketsystem.service.TicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ============================================================
 * DASHBOARD STATISTICS CONTROLLER
 * ============================================================
 * 
 * Provides REST APIs for dashboard statistics and analytics.
 * 
 * BASE URL: /api/dashboard
 * 
 * AVAILABLE ENDPOINTS:
 * - GET /api/dashboard/counts → Get ticket counts by status
 * - GET /api/dashboard/recent → Get recent tickets
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardStatsController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardStatsController.class);

    private final TicketService ticketService;

    public DashboardStatsController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // ============================================================
    // GET TICKET COUNTS BY STATUS
    // ============================================================

    /**
     * Retrieves ticket counts grouped by status.
     * 
     * ENDPOINT: GET /api/dashboard/counts
     * 
     * RESPONSE:
     * {
     * "success": true,
     * "message": "Ticket counts retrieved",
     * "data": {
     * "OPEN": 5,
     * "IN_PROGRESS": 2,
     * "RESOLVED": 1,
     * "CLOSED": 0
     * }
     * }
     * 
     * @return Ticket counts by status
     */
    @GetMapping("/counts")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getTicketCounts() {

        logger.info("API Request: Get ticket counts by status");

        try {
            Map<String, Long> counts = ticketService.getTicketCountsByStatus();

            logger.info("Ticket counts retrieved: {}", counts);
            return ResponseEntity.ok(
                    ApiResponse.success("Ticket counts retrieved successfully", counts));

        } catch (Exception e) {
            logger.error("Error retrieving ticket counts: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ApiResponse.error("Failed to retrieve ticket counts: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ============================================================
    // GET RECENT TICKETS
    // ============================================================

    /**
     * Retrieves the most recent tickets.
     * 
     * ENDPOINT: GET /api/dashboard/recent?limit=10
     * 
     * @param limit Maximum number of tickets to return (default: 10)
     * @return List of recent tickets
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getRecentTickets(
            @RequestParam(defaultValue = "10") int limit) {

        logger.info("API Request: Get {} recent tickets", limit);

        // Validate limit
        if (limit <= 0) {
            limit = 10;
        }
        if (limit > 100) {
            limit = 100; // Cap at 100
        }

        try {
            List<TicketResponse> tickets = ticketService.getRecentTickets(limit);

            if (tickets.isEmpty()) {
                logger.info("No recent tickets found");
                return ResponseEntity.ok(
                        ApiResponse.success("No tickets found", tickets, 0));
            }

            logger.info("Retrieved {} recent tickets", tickets.size());
            return ResponseEntity.ok(
                    ApiResponse.success("Recent tickets retrieved", tickets, tickets.size()));

        } catch (Exception e) {
            logger.error("Error retrieving recent tickets: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ApiResponse.error("Failed to retrieve recent tickets: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ============================================================
    // DASHBOARD HEALTH CHECK
    // ============================================================

    /**
     * Health check endpoint for Dashboard Stats API.
     * 
     * ENDPOINT: GET /api/dashboard/health
     * 
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        logger.debug("Dashboard Stats API health check requested");
        return ResponseEntity.ok(
                ApiResponse.success("Dashboard Stats API is operational", "HEALTHY"));
    }
}
