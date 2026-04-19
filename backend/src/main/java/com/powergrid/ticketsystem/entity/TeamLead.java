package com.powergrid.ticketsystem.entity;

import jakarta.persistence.*;

/**
 * Team Lead entity - IT team leads and managers
 * Used for ticket escalation routing
 */
@Entity
@Table(name = "team_leads", indexes = {
        @Index(name = "idx_team_lead_name", columnList = "name", unique = true),
        @Index(name = "idx_team_lead_email", columnList = "email"),
        @Index(name = "idx_team_lead_status", columnList = "status")
})
public class TeamLead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String name;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "status", length = 20)
    private String status = "ACTIVE";

    // Constructors
    public TeamLead() {
    }

    public TeamLead(String name, String email, String department) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.status = "ACTIVE";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TeamLead{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", department='" + department + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
