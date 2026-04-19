package com.powergrid.ticketsystem.service;

import com.powergrid.ticketsystem.entity.ManagementUser;
import com.powergrid.ticketsystem.entity.ManagementUser.ManagementRole;
import com.powergrid.ticketsystem.repository.ManagementUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * ============================================================
 * MANAGEMENT AUTHENTICATION SERVICE
 * ============================================================
 * 
 * PHASE 10: ROLE-BASED ACCESS CONTROL (RBAC)
 * 
 * Handles authentication for Management Dashboard users.
 * 
 * RESPONSIBILITIES:
 * 1. User login with credentials validation
 * 2. Session token generation and management
 * 3. Session validation for API requests
 * 4. Logout and session cleanup
 * 5. Role and department extraction for access control
 * 
 * SECURITY FEATURES:
 * - Session token generation (UUID-based)
 * - Session expiry (configurable, default 8 hours)
 * - Role-based access information returned on login
 * - Department restriction for DEPARTMENT_HEAD
 * 
 * @author IT Service Management Team
 * @version 1.0 - RBAC Implementation
 */
@Service
public class ManagementAuthService {

    private static final Logger logger = LoggerFactory.getLogger(ManagementAuthService.class);

    /**
     * Session validity duration in hours.
     */
    private static final int SESSION_HOURS = 8;

    private final ManagementUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ManagementAuthService(ManagementUserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ============================================================
    // LOGIN
    // ============================================================

    /**
     * Authenticate user and create session.
     *
     * BCRYPT PASSWORD VERIFICATION:
     * ──────────────────────────────
     * - Passwords stored in database as bcrypt hashes
     * - User provides plain text password
     * - We use passwordEncoder.matches() for verification
     * - Never stores or logs plain text passwords
     *
     * @param username Username or email
     * @param password Plain text password (automatically verified against bcrypt hash)
     * @return AuthResult with session info, role, and department
     */
    @Transactional
    public AuthResult login(String username, String password) {
        logger.info("════════════════════════════════════════════════════════");
        logger.info("LOGIN ATTEMPT: {}", username);
        logger.info("════════════════════════════════════════════════════════");

        // Validate input
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Login failed: Empty username");
            return AuthResult.failure("Username is required");
        }
        if (password == null || password.isEmpty()) {
            logger.warn("Login failed: Empty password for user {}", username);
            return AuthResult.failure("Password is required");
        }

        // Find user by username
        Optional<ManagementUser> userOpt = userRepository.findByUsername(username.trim());

        // If not found by username, try by email
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(username.trim());
        }

        if (userOpt.isEmpty()) {
            logger.warn("Login failed: User not found {}", username);
            return AuthResult.failure("Invalid username or password");
        }

        ManagementUser user = userOpt.get();

        // Verify password using bcrypt comparison
        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("Login failed: Password mismatch for {}", username);
            return AuthResult.failure("Invalid username or password");
        }

        // Check if account is active
        if (!user.getIsActive()) {
            logger.warn("Login failed: Account disabled for {}", username);
            return AuthResult.failure("Account is disabled. Contact administrator.");
        }

        // Generate session token
        String sessionToken = generateSessionToken();
        LocalDateTime sessionExpiry = LocalDateTime.now().plusHours(SESSION_HOURS);
        LocalDateTime loginTime = LocalDateTime.now();

        // Update session in database
        userRepository.updateSession(user.getId(), sessionToken, sessionExpiry, loginTime);

        logger.info("════════════════════════════════════════════════════════");
        logger.info("LOGIN SUCCESSFUL");
        logger.info("  User: {} ({})", user.getName(), user.getUsername());
        logger.info("  Role: {}", user.getRole().getDisplayName());
        logger.info("  Department: {}", user.getDepartmentDisplayName());
        logger.info("  Full Access: {}", user.hasFullAccess());
        logger.info("  Session Expiry: {}", sessionExpiry);
        logger.info("════════════════════════════════════════════════════════");

        return AuthResult.success(user, sessionToken, sessionExpiry);
    }

    // ============================================================
    // SESSION VALIDATION
    // ============================================================

    /**
     * Validate session token and return user info.
     * Used by APIs to verify authentication and get role/department.
     * 
     * @param sessionToken Session token from header/cookie
     * @return Optional of ManagementUser if valid session
     */
    public Optional<ManagementUser> validateSession(String sessionToken) {
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return Optional.empty();
        }

        return userRepository.findByValidSessionToken(
                sessionToken.trim(), LocalDateTime.now());
    }

    /**
     * Get current user from session token.
     * Throws exception if invalid.
     */
    public ManagementUser getCurrentUser(String sessionToken) {
        return validateSession(sessionToken)
                .orElseThrow(() -> new SecurityException("Invalid or expired session"));
    }

    /**
     * Check if session is valid.
     */
    public boolean isSessionValid(String sessionToken) {
        return validateSession(sessionToken).isPresent();
    }

    // ============================================================
    // LOGOUT
    // ============================================================

    /**
     * Logout user and clear session.
     * 
     * @param sessionToken Session token to invalidate
     * @return true if logout successful
     */
    @Transactional
    public boolean logout(String sessionToken) {
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return false;
        }

        int cleared = userRepository.clearSessionByToken(sessionToken.trim());

        if (cleared > 0) {
            logger.info("User logged out successfully");
            return true;
        }

        return false;
    }

    /**
     * Logout user by userId.
     */
    @Transactional
    public boolean logoutByUserId(String userId) {
        Optional<ManagementUser> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isPresent()) {
            userRepository.clearSession(userOpt.get().getId());
            logger.info("User {} logged out", userId);
            return true;
        }
        return false;
    }

    // ============================================================
    // ACCESS CONTROL HELPERS
    // ============================================================

    /**
     * Check if user can access a specific department's data.
     * 
     * @param sessionToken        User's session token
     * @param requestedDepartment Department to access (null or "ALL" for all)
     * @return true if access allowed
     */
    public boolean canAccessDepartment(String sessionToken, String requestedDepartment) {
        Optional<ManagementUser> userOpt = validateSession(sessionToken);
        if (userOpt.isEmpty()) {
            return false;
        }

        ManagementUser user = userOpt.get();
        return user.canAccessDepartment(requestedDepartment);
    }

    /**
     * Get the effective department filter for a user.
     * 
     * For IT_COORDINATOR: Returns the requested department (or null for all)
     * For DEPARTMENT_HEAD: Always returns their own department (ignores request)
     * 
     * @param sessionToken        User's session
     * @param requestedDepartment Requested department filter
     * @return Effective department to filter by
     */
    public String getEffectiveDepartment(String sessionToken, String requestedDepartment) {
        Optional<ManagementUser> userOpt = validateSession(sessionToken);
        if (userOpt.isEmpty()) {
            throw new SecurityException("Invalid session");
        }

        ManagementUser user = userOpt.get();

        // IT Coordinator can choose any department
        if (user.getRole() == ManagementRole.IT_COORDINATOR) {
            // null or "ALL" means all departments
            if (requestedDepartment == null ||
                    requestedDepartment.trim().isEmpty() ||
                    requestedDepartment.equalsIgnoreCase("ALL")) {
                return null; // No filter = all departments
            }
            return requestedDepartment.toUpperCase();
        }

        // Department Head is always restricted to their department
        logger.debug("Department Head {} restricted to department: {}",
                user.getUsername(), user.getDepartment());
        return user.getDepartment();
    }

    // ============================================================
    // USER INFO
    // ============================================================

    /**
     * Get user info for session (without sensitive data).
     */
    public Map<String, Object> getUserInfo(String sessionToken) {
        Optional<ManagementUser> userOpt = validateSession(sessionToken);
        if (userOpt.isEmpty()) {
            return null;
        }

        ManagementUser user = userOpt.get();
        Map<String, Object> info = new HashMap<>();
        info.put("userId", user.getUserId());
        info.put("username", user.getUsername());
        info.put("name", user.getName());
        info.put("email", user.getEmail());
        info.put("role", user.getRole().name());
        info.put("roleDisplayName", user.getRole().getDisplayName());
        info.put("department", user.getDepartment());
        info.put("departmentDisplayName", user.getDepartmentDisplayName());
        info.put("hasFullAccess", user.hasFullAccess());
        info.put("lastLogin", user.getLastLogin());

        return info;
    }

    // ============================================================
    // HELPERS
    // ============================================================

    /**
     * Generate unique session token.
     */
    private String generateSessionToken() {
        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }

    // ============================================================
    // AUTH RESULT CLASS
    // ============================================================

    /**
     * Authentication result wrapper.
     */
    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final String sessionToken;
        private final LocalDateTime sessionExpiry;
        private final Map<String, Object> userInfo;

        private AuthResult(boolean success, String message, String sessionToken,
                LocalDateTime sessionExpiry, Map<String, Object> userInfo) {
            this.success = success;
            this.message = message;
            this.sessionToken = sessionToken;
            this.sessionExpiry = sessionExpiry;
            this.userInfo = userInfo;
        }

        public static AuthResult success(ManagementUser user, String token, LocalDateTime expiry) {
            Map<String, Object> info = new HashMap<>();
            info.put("userId", user.getUserId());
            info.put("username", user.getUsername());
            info.put("name", user.getName());
            info.put("email", user.getEmail());
            info.put("role", user.getRole().name());
            info.put("roleDisplayName", user.getRole().getDisplayName());
            info.put("department", user.getDepartment());
            info.put("departmentDisplayName", user.getDepartmentDisplayName());
            info.put("hasFullAccess", user.hasFullAccess());

            return new AuthResult(true, "Login successful", token, expiry, info);
        }

        public static AuthResult failure(String message) {
            return new AuthResult(false, message, null, null, null);
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getSessionToken() {
            return sessionToken;
        }

        public LocalDateTime getSessionExpiry() {
            return sessionExpiry;
        }

        public Map<String, Object> getUserInfo() {
            return userInfo;
        }
    }
}
