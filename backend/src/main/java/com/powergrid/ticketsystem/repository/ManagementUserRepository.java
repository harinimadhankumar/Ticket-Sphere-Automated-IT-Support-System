package com.powergrid.ticketsystem.repository;

import com.powergrid.ticketsystem.entity.ManagementUser;
import com.powergrid.ticketsystem.entity.ManagementUser.ManagementRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * MANAGEMENT USER REPOSITORY
 * ============================================================
 * 
 * PHASE 10: ROLE-BASED ACCESS CONTROL (RBAC)
 * 
 * Data access layer for ManagementUser entity.
 * Provides methods for:
 * - Authentication (login/logout)
 * - Session management
 * - User lookup by various criteria
 * - Role-based queries
 * 
 * @author IT Service Management Team
 * @version 1.0 - RBAC Implementation
 */
@Repository
public interface ManagementUserRepository extends JpaRepository<ManagementUser, Long> {

    // ============================================================
    // AUTHENTICATION QUERIES
    // ============================================================

    /**
     * Find user by username for login.
     */
    Optional<ManagementUser> findByUsername(String username);

    /**
     * Find user by email for login.
     */
    Optional<ManagementUser> findByEmail(String email);

    /**
     * Find user by userId.
     */
    Optional<ManagementUser> findByUserId(String userId);

    /**
     * Find active user by username (for authentication).
     */
    @Query("SELECT u FROM ManagementUser u WHERE u.username = :username AND u.isActive = true")
    Optional<ManagementUser> findActiveByUsername(@Param("username") String username);

    /**
     * Find user by username and password (simple auth - for demo).
     * In production, use BCrypt password verification in service layer.
     */
    @Query("SELECT u FROM ManagementUser u WHERE u.username = :username AND u.password = :password AND u.isActive = true")
    Optional<ManagementUser> findByUsernameAndPassword(
            @Param("username") String username,
            @Param("password") String password);

    // ============================================================
    // SESSION MANAGEMENT
    // ============================================================

    /**
     * Find user by session token.
     */
    @Query("SELECT u FROM ManagementUser u WHERE u.sessionToken = :token AND u.sessionExpiry > :now AND u.isActive = true")
    Optional<ManagementUser> findByValidSessionToken(
            @Param("token") String token,
            @Param("now") LocalDateTime now);

    /**
     * Update session token for user.
     */
    @Modifying
    @Transactional
    @Query("UPDATE ManagementUser u SET u.sessionToken = :token, u.sessionExpiry = :expiry, u.lastLogin = :loginTime WHERE u.id = :userId")
    int updateSession(
            @Param("userId") Long userId,
            @Param("token") String token,
            @Param("expiry") LocalDateTime expiry,
            @Param("loginTime") LocalDateTime loginTime);

    /**
     * Clear session (logout).
     */
    @Modifying
    @Transactional
    @Query("UPDATE ManagementUser u SET u.sessionToken = null, u.sessionExpiry = null WHERE u.id = :userId")
    int clearSession(@Param("userId") Long userId);

    /**
     * Clear session by token.
     */
    @Modifying
    @Transactional
    @Query("UPDATE ManagementUser u SET u.sessionToken = null, u.sessionExpiry = null WHERE u.sessionToken = :token")
    int clearSessionByToken(@Param("token") String token);

    // ============================================================
    // ROLE-BASED QUERIES
    // ============================================================

    /**
     * Find all users by role.
     */
    List<ManagementUser> findByRole(ManagementRole role);

    /**
     * Find all users by department.
     */
    List<ManagementUser> findByDepartment(String department);

    /**
     * Find all active users by department.
     */
    @Query("SELECT u FROM ManagementUser u WHERE u.department = :department AND u.isActive = true")
    List<ManagementUser> findActiveByDepartment(@Param("department") String department);

    /**
     * Find department heads for a specific department.
     */
    @Query("SELECT u FROM ManagementUser u WHERE u.role = 'DEPARTMENT_HEAD' AND u.department = :department AND u.isActive = true")
    List<ManagementUser> findDepartmentHeads(@Param("department") String department);

    /**
     * Find all IT coordinators.
     */
    @Query("SELECT u FROM ManagementUser u WHERE u.role = 'IT_COORDINATOR' AND u.isActive = true")
    List<ManagementUser> findAllITCoordinators();

    /**
     * Count users by role.
     */
    long countByRole(ManagementRole role);

    /**
     * Count users by department.
     */
    long countByDepartment(String department);

    // ============================================================
    // EXISTENCE CHECKS
    // ============================================================

    /**
     * Check if username exists.
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists.
     */
    boolean existsByEmail(String email);

    /**
     * Check if userId exists.
     */
    boolean existsByUserId(String userId);

    // ============================================================
    // ADMIN OPERATIONS
    // ============================================================

    /**
     * Find all active users.
     */
    @Query("SELECT u FROM ManagementUser u WHERE u.isActive = true ORDER BY u.role, u.department, u.name")
    List<ManagementUser> findAllActive();

    /**
     * Deactivate user by userId.
     */
    @Modifying
    @Transactional
    @Query("UPDATE ManagementUser u SET u.isActive = false WHERE u.userId = :userId")
    int deactivateUser(@Param("userId") String userId);

    /**
     * Activate user by userId.
     */
    @Modifying
    @Transactional
    @Query("UPDATE ManagementUser u SET u.isActive = true WHERE u.userId = :userId")
    int activateUser(@Param("userId") String userId);

    /**
     * Update user's department.
     */
    @Modifying
    @Transactional
    @Query("UPDATE ManagementUser u SET u.department = :department WHERE u.userId = :userId")
    int updateUserDepartment(
            @Param("userId") String userId,
            @Param("department") String department);

    /**
     * Update user's role.
     */
    @Modifying
    @Transactional
    @Query("UPDATE ManagementUser u SET u.role = :role WHERE u.userId = :userId")
    int updateUserRole(
            @Param("userId") String userId,
            @Param("role") ManagementRole role);
}
