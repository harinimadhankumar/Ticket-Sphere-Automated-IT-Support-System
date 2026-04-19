package com.powergrid.ticketsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Engineer entity - IT support staff who resolve tickets */
@Entity
@Table(name = "engineers", indexes = {
        @Index(name = "idx_engineer_email", columnList = "email"),
        @Index(name = "idx_engineer_team", columnList = "team"),
        @Index(name = "idx_engineer_status", columnList = "status")
})
public class Engineer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "engineer_id", unique = true, length = 20)
    private String engineerId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "team", nullable = false, length = 50)
    private String team;

    @Column(name = "skills", length = 500)
    private String skills;

    @Column(name = "status", length = 20)
    private String status = "AVAILABLE";

    @Column(name = "current_workload")
    private Integer currentWorkload = 0;

    @Column(name = "max_workload")
    private Integer maxWorkload = 10;

    @Column(name = "tickets_resolved")
    private Integer ticketsResolved = 0;

    @Column(name = "avg_resolution_time")
    private Double avgResolutionTime = 0.0;

    @Column(name = "performance_score")
    private Double performanceScore = 100.0;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "role", length = 50)
    private String role = "IT Engineer";

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Engineer() {
    }

    public Engineer(String name, String email, String password, String team) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.team = team;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Auto-generate engineerId before insert.
     */
    @PrePersist
    public void generateEngineerId() {
        if (this.engineerId == null) {
            this.engineerId = "ENG-" + String.format("%04d", System.currentTimeMillis() % 10000);
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEngineerId() {
        return engineerId;
    }

    public void setEngineerId(String engineerId) {
        this.engineerId = engineerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCurrentWorkload() {
        return currentWorkload;
    }

    public void setCurrentWorkload(Integer currentWorkload) {
        this.currentWorkload = currentWorkload;
    }

    public Integer getMaxWorkload() {
        return maxWorkload;
    }

    public void setMaxWorkload(Integer maxWorkload) {
        this.maxWorkload = maxWorkload;
    }

    public Integer getTicketsResolved() {
        return ticketsResolved;
    }

    public void setTicketsResolved(Integer ticketsResolved) {
        this.ticketsResolved = ticketsResolved;
    }

    public Double getAvgResolutionTime() {
        return avgResolutionTime;
    }

    public void setAvgResolutionTime(Double avgResolutionTime) {
        this.avgResolutionTime = avgResolutionTime;
    }

    public Double getPerformanceScore() {
        return performanceScore;
    }

    public void setPerformanceScore(Double performanceScore) {
        this.performanceScore = performanceScore;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Engineer{" +
                "engineerId='" + engineerId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", team='" + team + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
