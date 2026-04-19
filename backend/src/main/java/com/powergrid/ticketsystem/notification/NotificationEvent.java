package com.powergrid.ticketsystem.notification;

/**
 * ============================================================
 * NOTIFICATION EVENT ENUM
 * ============================================================
 * 
 * PHASE 8: NOTIFICATIONS & ALERTS
 * 
 * Defines all ticket lifecycle events that trigger email notifications.
 * Each event has associated metadata for email subject and recipient type.
 * 
 * EVENTS:
 * - TICKET_CREATED: New ticket created (notify employee)
 * - TICKET_ASSIGNED: Ticket assigned to engineer (notify engineer)
 * - TICKET_ESCALATED: SLA breach escalation (notify senior staff)
 * - TICKET_RESOLVED: Engineer resolved ticket (notify employee)
 * - TICKET_CLOSED: Ticket verified and closed (notify employee + engineer)
 * - SLA_WARNING: Approaching SLA deadline (notify engineer)
 */
public enum NotificationEvent {

    /**
     * Triggered when a new ticket is created via Email or Chatbot.
     * Recipient: Employee (ticket owner)
     */
    TICKET_CREATED(
            "Ticket Created",
            "Your IT Support Ticket Has Been Created",
            RecipientType.EMPLOYEE),

    /**
     * Triggered when ticket is assigned to an engineer.
     * Recipient: Assigned Engineer
     */
    TICKET_ASSIGNED(
            "Ticket Assigned",
            "New Ticket Assigned to You",
            RecipientType.ENGINEER),

    /**
     * Triggered during SLA breach - ticket escalated.
     * Recipients: Senior Engineer, Team Lead, Manager (based on level)
     */
    TICKET_ESCALATED(
            "Ticket Escalated",
            "URGENT: Ticket Escalation Alert",
            RecipientType.ESCALATION_CHAIN),

    /**
     * Triggered when engineer marks ticket as RESOLVED.
     * Recipient: Employee (ticket owner)
     */
    TICKET_RESOLVED(
            "Ticket Resolved",
            "Your IT Support Ticket Has Been Resolved",
            RecipientType.EMPLOYEE),

    /**
     * Triggered when ticket passes AI verification and is CLOSED.
     * Recipients: Employee, Engineer (optional)
     */
    TICKET_CLOSED(
            "Ticket Closed",
            "Your IT Support Ticket Has Been Closed",
            RecipientType.EMPLOYEE_AND_ENGINEER),

    /**
     * Triggered when ticket approaches SLA deadline (75% time elapsed).
     * Recipient: Assigned Engineer
     */
    SLA_WARNING(
            "SLA Warning",
            "SLA Warning: Ticket Requires Immediate Attention",
            RecipientType.ENGINEER),

    /**
     * Triggered when ticket is reopened after failed verification.
     * Recipient: Assigned Engineer
     */
    TICKET_REOPENED(
            "Ticket Reopened",
            "Ticket Reopened: Additional Action Required",
            RecipientType.ENGINEER);

    // ============================================================
    // ENUM PROPERTIES
    // ============================================================

    private final String eventName;
    private final String emailSubject;
    private final RecipientType recipientType;

    NotificationEvent(String eventName, String emailSubject, RecipientType recipientType) {
        this.eventName = eventName;
        this.emailSubject = emailSubject;
        this.recipientType = recipientType;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public RecipientType getRecipientType() {
        return recipientType;
    }

    /**
     * Recipient type for notification routing.
     */
    public enum RecipientType {
        EMPLOYEE, // Ticket owner/requester
        ENGINEER, // Assigned engineer
        ESCALATION_CHAIN, // Senior Engineer → Team Lead → Manager
        EMPLOYEE_AND_ENGINEER // Both employee and engineer
    }
}
