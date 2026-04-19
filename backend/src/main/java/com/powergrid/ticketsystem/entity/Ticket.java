package com.powergrid.ticketsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Ticket entity - stores IT support tickets from all channels (Email/Chatbot)
 */
@Entity
@Table(name = "tickets", indexes = {
        @Index(name = "idx_source", columnList = "source"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_employee_id", columnList = "employeeId"),
        @Index(name = "idx_created_time", columnList = "createdTime")
})
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", unique = true, length = 20)
    private String ticketId;

    @Column(name = "source", nullable = false, length = 20)
    private String source;

    @Column(name = "employee_id", nullable = false, length = 50)
    private String employeeId;

    @Column(name = "issue_description", columnDefinition = "TEXT")
    private String issueDescription;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "sender_email", length = 100)
    private String senderEmail;

    @Column(name = "email_subject", length = 500)
    private String emailSubject;

    // Classification fields
    @Column(name = "category", length = 30)
    private String category;

    @Column(name = "sub_category", length = 30)
    private String subCategory;

    @Column(name = "priority", length = 20)
    private String priority;

    @Column(name = "assigned_team", length = 50)
    private String assignedTeam;

    @Column(name = "assigned_engineer", length = 100)
    private String assignedEngineer;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "is_classified")
    private Boolean isClassified = false;

    @Column(name = "classified_time")
    private LocalDateTime classifiedTime;

    // Self-service resolution fields
    @Column(name = "resolution_status", length = 30)
    private String resolutionStatus;

    @Column(name = "knowledge_base_id")
    private Long knowledgeBaseId;

    @Column(name = "solution_sent_time")
    private LocalDateTime solutionSentTime;

    @Column(name = "timeout_reminder_sent")
    private Boolean timeoutReminderSent = false;

    @Column(name = "timeout_reminder_time")
    private LocalDateTime timeoutReminderTime;

    @Column(name = "closed_by", length = 50)
    private String closedBy;

    @Column(name = "closed_time")
    private LocalDateTime closedTime;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "escalation_reason", length = 500)
    private String escalationReason;

    @Column(name = "escalated_time")
    private LocalDateTime escalatedTime;

    // SLA monitoring fields
    @Column(name = "escalation_level", length = 20)
    private String escalationLevel;

    @Column(name = "escalated_to_team_lead", length = 100)
    private String escalatedToTeamLead;

    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;

    @Column(name = "sla_breached")
    private Boolean slaBreached = false;

    @Column(name = "sla_breach_time")
    private LocalDateTime slaBreachTime;

    @Column(name = "escalation_count")
    private Integer escalationCount = 0;

    @Column(name = "previous_engineer", length = 100)
    private String previousEngineer;

    @Column(name = "sla_warning_sent")
    private Boolean slaWarningSent = false;

    @Column(name = "last_sla_check")
    private LocalDateTime lastSlaCheck;

    // AI verification fields
    @Column(name = "verification_status", length = 20)
    private String verificationStatus;

    @Column(name = "verification_score")
    private Integer verificationScore;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    @Column(name = "verified_time")
    private LocalDateTime verifiedTime;

    @Column(name = "verification_attempts")
    private Integer verificationAttempts = 0;

    // Constructors
    public Ticket() {
    }

    public Ticket(String source, String employeeId, String issueDescription,
            LocalDateTime createdTime, String status) {
        this.source = source;
        this.employeeId = employeeId;
        this.issueDescription = issueDescription;
        this.createdTime = createdTime;
        this.status = status;
    }

    @PrePersist
    public void generateTicketId() {
        if (this.ticketId == null) {
            this.ticketId = "TKT-" + System.currentTimeMillis();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getResolutionStatus() {
        return resolutionStatus;
    }

    public void setResolutionStatus(String resolutionStatus) {
        this.resolutionStatus = resolutionStatus;
    }

    public Long getKnowledgeBaseId() {
        return knowledgeBaseId;
    }

    public void setKnowledgeBaseId(Long knowledgeBaseId) {
        this.knowledgeBaseId = knowledgeBaseId;
    }

    public LocalDateTime getSolutionSentTime() {
        return solutionSentTime;
    }

    public void setSolutionSentTime(LocalDateTime solutionSentTime) {
        this.solutionSentTime = solutionSentTime;
    }

    public Boolean getTimeoutReminderSent() {
        return timeoutReminderSent;
    }

    public void setTimeoutReminderSent(Boolean timeoutReminderSent) {
        this.timeoutReminderSent = timeoutReminderSent;
    }

    public LocalDateTime getTimeoutReminderTime() {
        return timeoutReminderTime;
    }

    public void setTimeoutReminderTime(LocalDateTime timeoutReminderTime) {
        this.timeoutReminderTime = timeoutReminderTime;
    }

    public String getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(String closedBy) {
        this.closedBy = closedBy;
    }

    public LocalDateTime getClosedTime() {
        return closedTime;
    }

    public void setClosedTime(LocalDateTime closedTime) {
        this.closedTime = closedTime;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public String getEscalationReason() {
        return escalationReason;
    }

    public void setEscalationReason(String escalationReason) {
        this.escalationReason = escalationReason;
    }

    public LocalDateTime getEscalatedTime() {
        return escalatedTime;
    }

    public void setEscalatedTime(LocalDateTime escalatedTime) {
        this.escalatedTime = escalatedTime;
    }

    public String getEscalationLevel() {
        return escalationLevel;
    }

    public void setEscalationLevel(String escalationLevel) {
        this.escalationLevel = escalationLevel;
    }

    public LocalDateTime getSlaDeadline() {
        return slaDeadline;
    }

    public void setSlaDeadline(LocalDateTime slaDeadline) {
        this.slaDeadline = slaDeadline;
    }

    public Boolean getSlaBreached() {
        return slaBreached;
    }

    public void setSlaBreached(Boolean slaBreached) {
        this.slaBreached = slaBreached;
    }

    public LocalDateTime getSlaBreachTime() {
        return slaBreachTime;
    }

    public void setSlaBreachTime(LocalDateTime slaBreachTime) {
        this.slaBreachTime = slaBreachTime;
    }

    public Integer getEscalationCount() {
        return escalationCount;
    }

    public void setEscalationCount(Integer escalationCount) {
        this.escalationCount = escalationCount;
    }

    public String getPreviousEngineer() {
        return previousEngineer;
    }

    public void setPreviousEngineer(String previousEngineer) {
        this.previousEngineer = previousEngineer;
    }

    public Boolean getSlaWarningSent() {
        return slaWarningSent;
    }

    public void setSlaWarningSent(Boolean slaWarningSent) {
        this.slaWarningSent = slaWarningSent;
    }

    public LocalDateTime getLastSlaCheck() {
        return lastSlaCheck;
    }

    public void setLastSlaCheck(LocalDateTime lastSlaCheck) {
        this.lastSlaCheck = lastSlaCheck;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public Integer getVerificationScore() {
        return verificationScore;
    }

    public void setVerificationScore(Integer verificationScore) {
        this.verificationScore = verificationScore;
    }

    public String getVerificationNotes() {
        return verificationNotes;
    }

    public void setVerificationNotes(String verificationNotes) {
        this.verificationNotes = verificationNotes;
    }

    public LocalDateTime getVerifiedTime() {
        return verifiedTime;
    }

    public void setVerifiedTime(LocalDateTime verifiedTime) {
        this.verifiedTime = verifiedTime;
    }

    public Integer getVerificationAttempts() {
        return verificationAttempts;
    }

    public void setVerificationAttempts(Integer verificationAttempts) {
        this.verificationAttempts = verificationAttempts;
    }

    public String getEscalatedToTeamLead() {
        return escalatedToTeamLead;
    }

    public void setEscalatedToTeamLead(String escalatedToTeamLead) {
        this.escalatedToTeamLead = escalatedToTeamLead;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "ticketId='" + ticketId + '\'' +
                ", source='" + source + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", issueDescription='" + issueDescription + '\'' +
                ", createdTime=" + createdTime +
                ", status='" + status + '\'' +
                '}';
    }
}
