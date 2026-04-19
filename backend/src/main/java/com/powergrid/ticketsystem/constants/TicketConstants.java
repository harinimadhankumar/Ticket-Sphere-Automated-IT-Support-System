package com.powergrid.ticketsystem.constants;

/**
 * ============================================================
 * TICKET CONSTANTS
 * ============================================================
 * 
 * Centralized constants used throughout the ticket management system.
 * Using constants instead of hardcoded strings ensures:
 * - Consistency across the application
 * - Easy maintenance and updates
 * - Prevention of typo-related bugs
 */
public final class TicketConstants {

    // ============================================================
    // TICKET SOURCE CHANNELS
    // Indicates the origin of the ticket
    // ============================================================

    /**
     * Ticket received via email to it-support@powergrid.in
     */
    public static final String SOURCE_EMAIL = "EMAIL";

    /**
     * Ticket received via AI Chatbot REST API
     */
    public static final String SOURCE_CHATBOT = "CHATBOT";

    // ============================================================
    // TICKET STATUS VALUES
    // Tracks the lifecycle of a ticket
    // ============================================================

    /**
     * Default status when ticket is first created.
     * Indicates ticket is waiting to be assigned/processed.
     */
    public static final String STATUS_OPEN = "OPEN";

    /**
     * Ticket is being actively worked on by IT support.
     */
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";

    /**
     * Issue has been resolved but awaiting confirmation.
     */
    public static final String STATUS_RESOLVED = "RESOLVED";

    /**
     * Ticket is fully closed after resolution confirmation.
     */
    public static final String STATUS_CLOSED = "CLOSED";

    // ============================================================
    // API RESPONSE MESSAGES
    // Standard messages for API responses
    // ============================================================

    public static final String MSG_TICKET_CREATED = "Ticket created successfully";
    public static final String MSG_TICKET_NOT_FOUND = "Ticket not found";
    public static final String MSG_INVALID_REQUEST = "Invalid request data";
    public static final String MSG_TICKETS_RETRIEVED = "Tickets retrieved successfully";
    public static final String MSG_NO_TICKETS_FOUND = "No tickets found";

    // ============================================================
    // PRIVATE CONSTRUCTOR
    // Prevents instantiation of this utility class
    // ============================================================

    private TicketConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }
}
