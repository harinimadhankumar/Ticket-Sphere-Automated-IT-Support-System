package com.powergrid.ticketsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Knowledge Base entity - stores IT issue solutions for self-service resolution
 */
@Entity
@Table(name = "knowledge_base", indexes = {
        @Index(name = "idx_issue_type", columnList = "issue_type"),
        @Index(name = "idx_auto_closable", columnList = "auto_closable")
})
public class KnowledgeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "issue_type", nullable = false, length = 50)
    private String issueType;

    @Column(name = "issue_title", nullable = false, length = 200)
    private String issueTitle;

    @Column(name = "solution_steps", columnDefinition = "TEXT", nullable = false)
    private String solutionSteps;

    @Column(name = "auto_closable", nullable = false)
    private Boolean autoClosable = false;

    @Column(name = "keywords", length = 500)
    private String keywords;

    @Column(name = "category", length = 30)
    private String category;

    @Column(name = "success_rate")
    private Double successRate = 0.0;

    @Column(name = "usage_count")
    private Integer usageCount = 0;

    @Column(name = "success_count")
    private Integer successCount = 0;

    @Column(name = "priority_rank")
    private Integer priorityRank = 1;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    // Constructors
    public KnowledgeBase() {
    }

    public KnowledgeBase(String issueType, String issueTitle, String solutionSteps,
            Boolean autoClosable, String category) {
        this.issueType = issueType;
        this.issueTitle = issueTitle;
        this.solutionSteps = solutionSteps;
        this.autoClosable = autoClosable;
        this.category = category;
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedTime = LocalDateTime.now();
    }

    // Helper methods
    public void incrementUsageCount() {
        this.usageCount = (this.usageCount == null ? 0 : this.usageCount) + 1;
    }

    public void incrementSuccessCount() {
        this.successCount = (this.successCount == null ? 0 : this.successCount) + 1;
        updateSuccessRate();
    }

    public void updateSuccessRate() {
        if (this.usageCount != null && this.usageCount > 0) {
            this.successRate = (double) this.successCount / this.usageCount;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getIssueTitle() {
        return issueTitle;
    }

    public void setIssueTitle(String issueTitle) {
        this.issueTitle = issueTitle;
    }

    public String getSolutionSteps() {
        return solutionSteps;
    }

    public void setSolutionSteps(String solutionSteps) {
        this.solutionSteps = solutionSteps;
    }

    public Boolean getAutoClosable() {
        return autoClosable;
    }

    public void setAutoClosable(Boolean autoClosable) {
        this.autoClosable = autoClosable;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getPriorityRank() {
        return priorityRank;
    }

    public void setPriorityRank(Integer priorityRank) {
        this.priorityRank = priorityRank;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public String toString() {
        return "KnowledgeBase{" +
                "id=" + id +
                ", issueType='" + issueType + '\'' +
                ", issueTitle='" + issueTitle + '\'' +
                ", autoClosable=" + autoClosable +
                ", successRate=" + successRate +
                ", usageCount=" + usageCount +
                '}';
    }
}
