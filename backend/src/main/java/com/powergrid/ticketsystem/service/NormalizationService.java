package com.powergrid.ticketsystem.service;

import com.powergrid.ticketsystem.constants.TicketConstants;
import com.powergrid.ticketsystem.dto.ChatbotTicketRequest;
import com.powergrid.ticketsystem.entity.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * ============================================================
 * NORMALIZATION SERVICE (CRITICAL LAYER)
 * ============================================================
 * 
 * This service is the heart of the Unified Ticket Ingestion system.
 * 
 * PURPOSE:
 * Converts tickets from different sources (Email, Chatbot) into a
 * COMMON TICKET FORMAT before storing in MongoDB.
 * 
 * WHY NORMALIZATION IS CRITICAL:
 * 1. Ensures consistency - All tickets follow the same structure
 * 2. AI-readiness - Standardized data is easier for AI to process
 * 3. Unified storage - Single source of truth in MongoDB
 * 4. Easy querying - Consistent fields for reporting and dashboards
 * 5. Prevents data fragmentation - No scattered/duplicate data
 * 
 * NORMALIZED TICKET STRUCTURE:
 * - ticketId: Auto-generated unique ID
 * - source: EMAIL / CHATBOT
 * - employeeId: Extracted/provided identifier
 * - issueDescription: Problem description
 * - createdTime: Timestamp
 * - status: OPEN (default)
 */
@Service
public class NormalizationService {

    private static final Logger logger = LoggerFactory.getLogger(NormalizationService.class);

    // ============================================================
    // NORMALIZE EMAIL TICKET
    // Converts raw email data into standard ticket format
    // ============================================================

    /**
     * Normalizes email data into a standard Ticket entity.
     * 
     * EXTRACTION LOGIC:
     * - employeeId: Derived from sender email (username part before @)
     * - issueDescription: Email body content
     * - createdTime: Email received timestamp
     * - source: Set to EMAIL
     * - status: Set to OPEN (default)
     * 
     * @param senderEmail  Full email address of the sender
     * @param emailSubject Subject line of the email
     * @param emailBody    Body content of the email (issue description)
     * @param receivedTime Timestamp when email was received
     * @return Normalized Ticket entity ready for storage
     */
    public Ticket normalizeEmailTicket(String senderEmail, String emailSubject,
            String emailBody, LocalDateTime receivedTime) {

        logger.info("Normalizing email ticket from: {}", senderEmail);

        // Create new ticket entity
        Ticket ticket = new Ticket();

        // Set source as EMAIL
        ticket.setSource(TicketConstants.SOURCE_EMAIL);

        // Extract employee ID from email address
        // Example: "john.doe@powergrid.in" -> "john.doe"
        String employeeId = extractEmployeeIdFromEmail(senderEmail);
        ticket.setEmployeeId(employeeId);

        // Set issue description from email body
        // Clean and sanitize the content
        String cleanedDescription = sanitizeDescription(emailBody);
        ticket.setIssueDescription(cleanedDescription);

        // Set creation time from email received time
        ticket.setCreatedTime(receivedTime);

        // Set default status as OPEN
        ticket.setStatus(TicketConstants.STATUS_OPEN);

        // Store original email details for reference
        ticket.setSenderEmail(senderEmail);
        ticket.setEmailSubject(emailSubject);

        logger.info("Email ticket normalized successfully. EmployeeId: {}", employeeId);

        return ticket;
    }

    // ============================================================
    // NORMALIZE CHATBOT TICKET
    // Converts chatbot API payload into standard ticket format
    // ============================================================

    /**
     * Normalizes chatbot request into a standard Ticket entity.
     * 
     * The chatbot already provides structured data, so normalization
     * is simpler - mainly adding metadata and defaults.
     * 
     * @param request ChatbotTicketRequest from API
     * @return Normalized Ticket entity ready for storage
     */
    public Ticket normalizeChatbotTicket(ChatbotTicketRequest request) {

        logger.info("Normalizing chatbot ticket for employee: {}", request.getEmployeeId());

        // Create new ticket entity
        Ticket ticket = new Ticket();

        // Set source as CHATBOT
        ticket.setSource(TicketConstants.SOURCE_CHATBOT);

        // Employee ID provided directly in request
        ticket.setEmployeeId(request.getEmployeeId().trim().toUpperCase());

        // Set issue description from request
        String cleanedDescription = sanitizeDescription(request.getIssueDescription());
        ticket.setIssueDescription(cleanedDescription);

        // Set creation time as current time
        ticket.setCreatedTime(LocalDateTime.now());

        // Set default status as OPEN
        ticket.setStatus(TicketConstants.STATUS_OPEN);

        // Email fields are null for chatbot tickets
        ticket.setSenderEmail(null);
        ticket.setEmailSubject(null);

        logger.info("Chatbot ticket normalized successfully. EmployeeId: {}",
                request.getEmployeeId());

        return ticket;
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Extracts employee ID from email address.
     * 
     * Logic:
     * 1. Takes the part before @ symbol
     * 2. Converts to uppercase for consistency
     * 3. Replaces dots with underscores if needed
     * 
     * Examples:
     * - "john.doe@powergrid.in" -> "JOHN.DOE"
     * - "emp001@powergrid.in" -> "EMP001"
     * 
     * @param email Full email address
     * @return Extracted and formatted employee ID
     */
    private String extractEmployeeIdFromEmail(String email) {
        if (email == null || email.isEmpty()) {
            logger.warn("Empty email received, using UNKNOWN as employee ID");
            return "UNKNOWN";
        }

        // Extract part before @
        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            String username = email.substring(0, atIndex);
            return username.trim().toUpperCase();
        }

        // If no @ found, return cleaned email as ID
        return email.trim().toUpperCase();
    }

    /**
     * Sanitizes and cleans issue description text.
     * 
     * Operations:
     * 1. Removes excess whitespace
     * 2. Trims leading/trailing spaces
     * 3. Removes potentially harmful characters
     * 4. Limits length if too long
     * 
     * @param description Raw description text
     * @return Cleaned description
     */
    private String sanitizeDescription(String description) {
        if (description == null || description.isEmpty()) {
            return "No description provided";
        }

        // Remove excess whitespace (multiple spaces, tabs, newlines)
        String cleaned = description.replaceAll("\\s+", " ").trim();

        // Remove any HTML tags that might be present
        cleaned = cleaned.replaceAll("<[^>]*>", "");

        // Limit length to 2000 characters
        if (cleaned.length() > 2000) {
            cleaned = cleaned.substring(0, 2000) + "... [truncated]";
            logger.warn("Issue description was truncated to 2000 characters");
        }

        return cleaned;
    }

    /**
     * Validates if the ticket data is complete and valid.
     * 
     * @param ticket Ticket to validate
     * @return true if valid, false otherwise
     */
    public boolean validateTicket(Ticket ticket) {
        if (ticket == null) {
            logger.error("Ticket is null");
            return false;
        }

        if (ticket.getEmployeeId() == null || ticket.getEmployeeId().isEmpty()) {
            logger.error("Employee ID is missing");
            return false;
        }

        if (ticket.getIssueDescription() == null || ticket.getIssueDescription().isEmpty()) {
            logger.error("Issue description is missing");
            return false;
        }

        if (ticket.getSource() == null || ticket.getSource().isEmpty()) {
            logger.error("Source is missing");
            return false;
        }

        return true;
    }
}
