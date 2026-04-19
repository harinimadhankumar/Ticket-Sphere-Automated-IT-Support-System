package com.powergrid.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ============================================================
 * CHATBOT TICKET REQUEST DTO
 * ============================================================
 * 
 * Data Transfer Object for receiving ticket data from AI Chatbot.
 * 
 * This DTO represents the payload sent by the Chatbot when an
 * employee raises a ticket through the conversational interface.
 * 
 * PAYLOAD STRUCTURE:
 * {
 * "employeeId": "EMP001",
 * "issueDescription": "VPN not working from home"
 * }
 * 
 * Validation annotations ensure data integrity before processing.
 */
public class ChatbotTicketRequest {

    /**
     * Employee ID of the user raising the ticket.
     * Must be provided and cannot be blank.
     * Example: "EMP001", "EMP12345"
     */
    @NotBlank(message = "Employee ID is required")
    @Size(min = 3, max = 20, message = "Employee ID must be between 3 and 20 characters")
    private String employeeId;

    /**
     * Description of the IT issue reported by the employee.
     * Must be provided and should be descriptive enough for IT support.
     * Example: "VPN not connecting from home", "Outlook not syncing emails"
     */
    @NotBlank(message = "Issue description is required")
    @Size(min = 10, max = 2000, message = "Issue description must be between 10 and 2000 characters")
    private String issueDescription;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    /**
     * Default constructor required for JSON deserialization.
     */
    public ChatbotTicketRequest() {
    }

    /**
     * Parameterized constructor for creating request with data.
     * 
     * @param employeeId       Employee identifier
     * @param issueDescription Description of the issue
     */
    public ChatbotTicketRequest(String employeeId, String issueDescription) {
        this.employeeId = employeeId;
        this.issueDescription = issueDescription;
    }

    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
    }

    @Override
    public String toString() {
        return "ChatbotTicketRequest{" +
                "employeeId='" + employeeId + '\'' +
                ", issueDescription='" + issueDescription + '\'' +
                '}';
    }
}
