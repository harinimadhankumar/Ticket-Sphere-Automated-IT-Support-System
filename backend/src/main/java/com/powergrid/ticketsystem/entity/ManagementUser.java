package com.powergrid.ticketsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ============================================================
 * MANAGEMENT USER ENTITY
 * ============================================================
 * 
 * PHASE 10: ROLE-BASED ACCESS CONTROL (RBAC)
 * 
 * Represents administrative users who access the Management Dashboard.
 * Supports two roles:
 * 
 * 1. DEPARTMENT_HEAD - Can only view their own department's data
 * 2. IT_COORDINATOR - Can view all departments (organization-wide)
 * 
 * SECURITY PRINCIPLE:
 * - Department-level access is enforced at the BACKEND level
 * - Frontend filtering is supplementary, not primary security
 * - All API calls verify user's role and department before returning data
 * 
 * DATABASE TABLE: management_users
 * 
 * @author IT Service Management Team
 * @version 1.0 - RBAC Implementation
 */
@Entity
@Table(name = "management_users", indexes = {
        @Index(name = "idx_mgmt_user_email", columnList = "email"),
        @Index(name = "idx_mgmt_user_username", columnList = "username"),
        @Index(name = "idx_mgmt_user_role", columnList = "role"),
        @Index(name = "idx_mgmt_user_department", columnList = "department")
})
public class ManagementUser {

    // ============================================================
    // PRIMARY KEY
    // ============================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ============================================================
    // USER IDENTIFICATION
    // ============================================================

   
    @Column(name = "user_id", unique = true, nullable = false, length = 50)
    private String userId;

    /**
     * Username for login (unique).
     */
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    /**
     * Full name of the management user.
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Email address (unique).
     */
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    /**
     * Hashed password for authentication.
     * In production, use BCrypt or similar.
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    // ============================================================
    // ROLE & DEPARTMENT (RBAC)
    // ============================================================

    /**
     * User's role in the system.
     * 
     * Allowed values:
     * - DEPARTMENT_HEAD: Limited to own department
     * - IT_COORDINATOR: Full access to all departments
     */
    @Column(name = "role", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private ManagementRole role;

    /**
     * Department the user belongs to.
     * 
     * For IT_COORDINATOR: This is their primary department but they can view all.
     * For DEPARTMENT_HEAD: This strictly limits their data access.
     * 
     * Values: NETWORK, HARDWARE, SOFTWARE, EMAIL, ACCESS, GENERAL
     */
    @Column(name = "department", nullable = false, length = 30)
    private String department;

    /**
     * Display name of the department.
     */
    @Column(name = "department_display_name", length = 100)
    private String departmentDisplayName;

    // ============================================================
    // STATUS & METADATA
    // ============================================================

    /**
     * Whether the account is active.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Last successful login timestamp.
     */
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    /**
     * Account creation timestamp.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Session token for active sessions.
     */
    @Column(name = "session_token", length = 255)
    private String sessionToken;

    /**
     * Session expiry time.
     */
    @Column(name = "session_expiry")
    private LocalDateTime sessionExpiry;

    // ============================================================
    // ROLE ENUM (INNER)
    // ============================================================

    /**
     * Management user roles.
     */
    public enum ManagementRole {
        /**
         * Department Head - Limited to own department data only.
         * Cannot view other departments.
         */
        DEPARTMENT_HEAD("Department Head", false),

        /**
         * IT Coordinator - Full access to all departments.
         * Can view organization-wide metrics and drill down by department.
         */
        IT_COORDINATOR("IT Coordinator", true);

        private final String displayName;
        private final boolean hasFullAccess;

        ManagementRole(String displayName, boolean hasFullAccess) {
            this.displayName = displayName;
            this.hasFullAccess = hasFullAccess;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean hasFullAccess() {
            return hasFullAccess;
        }
    }

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public ManagementUser() {
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }

    public ManagementUser(String userId, String username, String name, String email,
            String password, ManagementRole role, String department) {
        this();
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.department = department;
        setDepartmentDisplayNameFromCode(department);
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Check if this user can access a specific department's data.
     * 
     * @param requestedDepartment The department to check access for
     * @return true if user can access, false otherwise
     */
    public boolean canAccessDepartment(String requestedDepartment) {
        // IT Coordinator can access everything
        if (role == ManagementRole.IT_COORDINATOR) {
            return true;
        }

        // Department Head can only access their own department
        if (requestedDepartment == null || requestedDepartment.equalsIgnoreCase("ALL")) {
            return false; // Department heads cannot see "ALL"
        }

        return this.department.equalsIgnoreCase(requestedDepartment);
    }

    /**
     * Check if user has full organizational access.
     */
    public boolean hasFullAccess() {
        return role != null && role.hasFullAccess();
    }

    /**
     * Set department display name based on code.
     */
    public void setDepartmentDisplayNameFromCode(String departmentCode) {
        if (departmentCode == null) {
            this.departmentDisplayName = "Unknown";
            return;
        }

        switch (departmentCode.toUpperCase()) {
            case "NETWORK":
                this.departmentDisplayName = "Network Team";
                break;
            case "HARDWARE":
                this.departmentDisplayName = "Hardware Support Team";
                break;
            case "SOFTWARE":
                this.departmentDisplayName = "Application Support Team";
                break;
            case "EMAIL":
                this.departmentDisplayName = "Email Support Team";
                break;
            case "ACCESS":
                this.departmentDisplayName = "IT Security Team";
                break;
            case "GENERAL":
                this.departmentDisplayName = "General IT Support";
                break;
            default:
                this.departmentDisplayName = departmentCode + " Team";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public ManagementRole getRole() {
        return role;
    }

    public void setRole(ManagementRole role) {
        this.role = role;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
        setDepartmentDisplayNameFromCode(department);
    }

    public String getDepartmentDisplayName() {
        return departmentDisplayName;
    }

    public void setDepartmentDisplayName(String departmentDisplayName) {
        this.departmentDisplayName = departmentDisplayName;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public LocalDateTime getSessionExpiry() {
        return sessionExpiry;
    }

    public void setSessionExpiry(LocalDateTime sessionExpiry) {
        this.sessionExpiry = sessionExpiry;
    }

    @Override
    public String toString() {
        return "ManagementUser{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", role=" + role +
                ", department='" + department + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
