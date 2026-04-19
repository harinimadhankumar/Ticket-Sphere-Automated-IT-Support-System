package com.powergrid.ticketsystem.controller;

import com.powergrid.ticketsystem.constants.TicketConstants;
import com.powergrid.ticketsystem.dto.ApiResponse;
import com.powergrid.ticketsystem.dto.TicketResponse;
import com.powergrid.ticketsystem.service.TicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Dashboard Controller - REST APIs for ticket management and retrieval */
@RestController
@RequestMapping("/api/tickets")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final TicketService ticketService;

    public DashboardController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /** GET /api/tickets - Retrieve all tickets */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getAllTickets() {

        logger.info("API Request: Get all tickets");

        try {
            List<TicketResponse> tickets = ticketService.getAllTickets();

            if (tickets.isEmpty()) {
                logger.info("No tickets found in database");
                return ResponseEntity.ok(
                        ApiResponse.success(TicketConstants.MSG_NO_TICKETS_FOUND, tickets, 0));
            }

            logger.info("Retrieved {} tickets", tickets.size());
            return ResponseEntity.ok(
                    ApiResponse.success(TicketConstants.MSG_TICKETS_RETRIEVED, tickets, tickets.size()));

        } catch (Exception e) {
            logger.error("Error retrieving all tickets: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ApiResponse.error("Failed to retrieve tickets: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ============================================================
    // GET TICKET BY ID
    // ============================================================

    /**
     * Retrieves a single ticket by its ID.
     * 
     * ENDPOINT: GET /api/tickets/{ticketId}
     * 
     * @param ticketId Unique ticket identifier
     * @return Ticket if found, 404 if not found
     */
    @GetMapping("/{ticketId}")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketById(
            @PathVariable String ticketId) {

        logger.info("API Request: Get ticket by ID: {}", ticketId);

        try {
            TicketResponse ticket = ticketService.getTicketById(ticketId);

            if (ticket == null) {
                logger.warn("Ticket not found: {}", ticketId);
                return new ResponseEntity<>(
                        ApiResponse.error(TicketConstants.MSG_TICKET_NOT_FOUND),
                        HttpStatus.NOT_FOUND);
            }

            logger.info("Ticket found: {}", ticketId);
            return ResponseEntity.ok(
                    ApiResponse.success("Ticket found", ticket));

        } catch (Exception e) {
            logger.error("Error retrieving ticket {}: {}", ticketId, e.getMessage(), e);
            return new ResponseEntity<>(
                    ApiResponse.error("Failed to retrieve ticket: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ============================================================
    // GET TICKETS BY SOURCE
    // ============================================================

    /** GET /api/tickets/source/{source} - Get tickets by source (EMAIL/CHATBOT) */
    @GetMapping("/source/{source}")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTicketsBySource(
            @PathVariable String source) {

        logger.info("API Request: Get tickets by source: {}", source);

        try {
            List<TicketResponse> tickets = ticketService.getTicketsBySource(source);

            logger.info("Retrieved {} tickets from source: {}", tickets.size(), source);
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Tickets from " + source.toUpperCase() + " retrieved",
                            tickets,
                            tickets.size()));

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid source parameter: {}", source);
            return new ResponseEntity<>(
                    ApiResponse.error(e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error retrieving tickets by source: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ApiResponse.error("Failed to retrieve tickets: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** GET /api/tickets/status/open - Get only OPEN tickets */
    @GetMapping("/status/open")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getOpenTickets() {

        logger.info("API Request: Get OPEN tickets");

        try {
            List<TicketResponse> tickets = ticketService.getOpenTickets();

            logger.info("Retrieved {} OPEN tickets", tickets.size());
            return ResponseEntity.ok(
                    ApiResponse.success("Open tickets retrieved", tickets, tickets.size()));

        } catch (Exception e) {
            logger.error("Error retrieving open tickets: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ApiResponse.error("Failed to retrieve open tickets: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** GET /api/tickets/status/{status} - Get tickets by status */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTicketsByStatus(
            @PathVariable String status) {

        logger.info("API Request: Get tickets by status: {}", status);

        try {
            List<TicketResponse> tickets = ticketService.getTicketsByStatus(status);

            logger.info("Retrieved {} tickets with status: {}", tickets.size(), status);
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Tickets with status " + status.toUpperCase() + " retrieved",
                            tickets,
                            tickets.size()));

        } catch (Exception e) {
            logger.error("Error retrieving tickets by status: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ApiResponse.error("Failed to retrieve tickets: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** GET /api/tickets/employee/{employeeId} - Get tickets by employee */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTicketsByEmployee(
            @PathVariable String employeeId) {

        logger.info("API Request: Get tickets for employee: {}", employeeId);

        try {
            List<TicketResponse> tickets = ticketService.getTicketsByEmployee(employeeId);

            logger.info("Retrieved {} tickets for employee: {}", tickets.size(), employeeId);
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Tickets for employee " + employeeId + " retrieved",
                            tickets,
                            tickets.size()));

        } catch (Exception e) {
            logger.error("Error retrieving tickets for employee: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ApiResponse.error("Failed to retrieve tickets: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ============================================================
    // SEARCH TICKETS
    // ============================================================

    /**
     * Searches tickets by keyword in issue description.
     * 
     * ENDPOINT: GET /api/tickets/search?keyword=vpn
     * 
     * @param keyword Search keyword
     * @return List of matching tickets
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> searchTickets(
            @RequestParam String keyword) {

        logger.info("API Request: Search tickets with keyword: {}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            return new ResponseEntity<>(
                    ApiResponse.error("Search keyword cannot be empty"),
                    HttpStatus.BAD_REQUEST);
        }

        try {
            List<TicketResponse> tickets = ticketService.searchTickets(keyword.trim());

            logger.info("Found {} tickets matching keyword: {}", tickets.size(), keyword);
            return ResponseEntity.ok(
                    ApiResponse.success(
                            "Search results for '" + keyword + "'",
                            tickets,
                            tickets.size()));

        } catch (Exception e) {
            logger.error("Error searching tickets: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ApiResponse.error("Search failed: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** GET /api/tickets/stats - Get ticket statistics */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getTicketStatistics() {

        logger.info("API Request: Get ticket statistics");

        try {
            Map<String, Long> stats = new HashMap<>();

            // Count by source
            long emailCount = ticketService.getTicketCountBySource(TicketConstants.SOURCE_EMAIL);
            long chatbotCount = ticketService.getTicketCountBySource(TicketConstants.SOURCE_CHATBOT);

            // Count by status
            long openCount = ticketService.getTicketCountByStatus(TicketConstants.STATUS_OPEN);
            long inProgressCount = ticketService.getTicketCountByStatus(TicketConstants.STATUS_IN_PROGRESS);
            long resolvedCount = ticketService.getTicketCountByStatus(TicketConstants.STATUS_RESOLVED);
            long closedCount = ticketService.getTicketCountByStatus(TicketConstants.STATUS_CLOSED);

            // Build statistics map
            stats.put("totalTickets", emailCount + chatbotCount);
            stats.put("emailTickets", emailCount);
            stats.put("chatbotTickets", chatbotCount);
            stats.put("openTickets", openCount);
            stats.put("inProgressTickets", inProgressCount);
            stats.put("resolvedTickets", resolvedCount);
            stats.put("closedTickets", closedCount);

            logger.info("Statistics retrieved. Total tickets: {}", emailCount + chatbotCount);
            return ResponseEntity.ok(
                    ApiResponse.success("Statistics retrieved", stats));

        } catch (Exception e) {
            logger.error("Error retrieving statistics: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ApiResponse.error("Failed to retrieve statistics: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** PUT /api/tickets/{ticketId}/status - Update ticket status */
    @PutMapping("/{ticketId}/status")
    public ResponseEntity<ApiResponse<TicketResponse>> updateTicketStatus(
            @PathVariable String ticketId,
            @RequestParam String status) {

        logger.info("API Request: Update ticket {} status to {}", ticketId, status);

        if (status == null || status.trim().isEmpty()) {
            return new ResponseEntity<>(
                    ApiResponse.error("Status cannot be empty"),
                    HttpStatus.BAD_REQUEST);
        }

        try {
            TicketResponse updatedTicket = ticketService.updateTicketStatus(
                    ticketId, status.trim());

            if (updatedTicket == null) {
                return new ResponseEntity<>(
                        ApiResponse.error(TicketConstants.MSG_TICKET_NOT_FOUND),
                        HttpStatus.NOT_FOUND);
            }

            logger.info("Ticket {} status updated to {}", ticketId, status);
            return ResponseEntity.ok(
                    ApiResponse.success("Ticket status updated", updatedTicket));

        } catch (Exception e) {
            logger.error("Error updating ticket status: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ApiResponse.error("Failed to update status: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** GET /api/tickets/health - Health check */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {

        logger.debug("Dashboard API health check requested");

        return ResponseEntity.ok(
                ApiResponse.success("Dashboard API is operational", "HEALTHY"));
    }
}
