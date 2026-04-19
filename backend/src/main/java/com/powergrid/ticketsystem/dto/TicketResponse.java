package com.powergrid.ticketsystem.dto;

import java.time.LocalDateTime;

/**
 * ============================================================
 * TICKET RESPONSE DTO
 * ============================================================
 * 
 * Data Transfer Object for sending ticket data in API responses.
 * 
 * This DTO is used to present ticket information to API consumers
 * (Admin dashboard, AI classification engine, etc.)
 * 
 * Contains all relevant ticket fields formatted for external consumption.
 */
public class TicketResponse {

    /**
     * Unique identifier of the ticket.
     */
    private String ticketId;

    /**
     * Source channel of the ticket (EMAIL/CHATBOT).
     */
    private String source;

    /**
     * Employee ID who raised the ticket.
     */
    private String employeeId;

    /**
     * Description of the IT issue.
     */
    private String issueDescription;

    /**
     * Timestamp when ticket was created.
     */
    private LocalDateTime createdTime;

    /**
     * Current status of the ticket.
     */
    private String status;

    /**
     * Original sender email (for EMAIL source).
     */
    private String senderEmail;

    /**
     * Email subject line (for EMAIL source).
     */
    private String emailSubject;

    // ============================================================
    // PHASE 2 & 3: CLASSIFICATION FIELDS
    // ============================================================

    /**
     * Ticket category (NETWORK, SOFTWARE, HARDWARE, etc.)
     */
    private String category;

    /**
     * Ticket sub-category (VPN, WIFI, LOGIN, etc.)
     */
    private String subCategory;

    /**
     * Ticket priority (CRITICAL, HIGH, MEDIUM, LOW)
     */
    private String priority;

    /**
     * Team assigned to handle the ticket.
     */
    private String assignedTeam;

    /**
     * Engineer assigned to resolve the ticket.
     */
    private String assignedEngineer;

    /**
     * Classification confidence score (0.0 to 1.0)
     */
    private Double confidenceScore;

    /**
     * Whether the ticket has been classified.
     */
    private Boolean isClassified;

    /**
     * Timestamp when classification was performed.
     */
    private LocalDateTime classifiedTime;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    /**
     * Default constructor.
     */
    public TicketResponse() {
    }

    /**
     * Full constructor with all fields.
     */
    public TicketResponse(String ticketId, String source, String employeeId,
            String issueDescription, LocalDateTime createdTime,
            String status, String senderEmail, String emailSubject) {
        this.ticketId = ticketId;
        this.source = source;
        this.employeeId = employeeId;
        this.issueDescription = issueDescription;
        this.createdTime = createdTime;
        this.status = status;
        this.senderEmail = senderEmail;
        this.emailSubject = emailSubject;
    }

    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

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

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    // ----- Phase 2 & 3: Classification Getters/Setters -----

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getAssignedTeam() {
        return assignedTeam;
    }

    public void setAssignedTeam(String assignedTeam) {
        this.assignedTeam = assignedTeam;
    }

    public String getAssignedEngineer() {
        return assignedEngineer;
    }

    public void setAssignedEngineer(String assignedEngineer) {
        this.assignedEngineer = assignedEngineer;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public Boolean getIsClassified() {
        return isClassified;
    }

    public void setIsClassified(Boolean isClassified) {
        this.isClassified = isClassified;
    }

    public LocalDateTime getClassifiedTime() {
        return classifiedTime;
    }

    public void setClassifiedTime(LocalDateTime classifiedTime) {
        this.classifiedTime = classifiedTime;
    }
}
