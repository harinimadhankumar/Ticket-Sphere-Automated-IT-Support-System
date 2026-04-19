package com.powergrid.ticketsystem.controller;

import com.powergrid.ticketsystem.constants.TicketConstants;
import com.powergrid.ticketsystem.dto.ApiResponse;
import com.powergrid.ticketsystem.dto.ChatbotTicketRequest;
import com.powergrid.ticketsystem.dto.TicketResponse;
import com.powergrid.ticketsystem.service.TicketService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Chatbot Ticket Controller - REST API for receiving tickets from AI Chatbot
 */
@RestController
@RequestMapping("/api/tickets")
public class ChatbotTicketController {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotTicketController.class);

    private final TicketService ticketService;

    public ChatbotTicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /** POST /api/tickets/chatbot - Create ticket from chatbot */
    @PostMapping("/chatbot")
    public ResponseEntity<ApiResponse<TicketResponse>> createChatbotTicket(
            @Valid @RequestBody ChatbotTicketRequest request) {

        logger.info("Received chatbot ticket request from employee: {}",
                request.getEmployeeId());
        logger.debug("Request details: {}", request);

        try {
            TicketResponse createdTicket = ticketService.createChatbotTicket(request);
            ApiResponse<TicketResponse> response = ApiResponse.success(
                    TicketConstants.MSG_TICKET_CREATED,
                    createdTicket);

            logger.info("Chatbot ticket created successfully. TicketId: {}",
                    createdTicket.getTicketId());

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid chatbot ticket request: {}", e.getMessage());

            ApiResponse<TicketResponse> errorResponse = ApiResponse.error(
                    TicketConstants.MSG_INVALID_REQUEST + ": " + e.getMessage());

            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            logger.error("Error creating chatbot ticket: {}", e.getMessage(), e);

            ApiResponse<TicketResponse> errorResponse = ApiResponse.error(
                    "Failed to create ticket: " + e.getMessage());

            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /** GET /api/tickets/chatbot/health - Health check */
    @GetMapping("/chatbot/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {

        logger.debug("Chatbot API health check requested");

        ApiResponse<String> response = ApiResponse.success(
                "Chatbot Ticket API is operational",
                "HEALTHY");

        return ResponseEntity.ok(response);
    }
}
