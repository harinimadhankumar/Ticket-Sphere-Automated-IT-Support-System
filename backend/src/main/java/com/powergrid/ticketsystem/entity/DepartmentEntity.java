package com.powergrid.ticketsystem.entity;

import jakarta.persistence.*;

/**
 * Department entity - Stores IT departments managed by IT_COORDINATORs
 * Maps to Department enum at startup
 */
@Entity
@Table(name = "departments", indexes = {
        @Index(name = "idx_dept_code", columnList = "code", unique = true)
})
public class DepartmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "team_name", nullable = false, length = 150)
    private String teamName;

    @Column(name = "short_name", nullable = false, length = 100)
    private String shortName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    // Constructors
    public DepartmentEntity() {
    }

    public DepartmentEntity(String code, String name, String teamName, String shortName) {
        this.code = code;
        this.name = name;
        this.teamName = teamName;
        this.shortName = shortName;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = System.currentTimeMillis();
        }
        updatedAt = System.currentTimeMillis();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "DepartmentEntity{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", teamName='" + teamName + '\'' +
                ", shortName='" + shortName + '\'' +
                '}';
    }
}
