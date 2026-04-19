package com.powergrid.ticketsystem.controller;

import com.powergrid.ticketsystem.dto.ApiResponse;
import com.powergrid.ticketsystem.service.ManagementAuthService;
import com.powergrid.ticketsystem.service.ManagementAuthService.AuthResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================
 * MANAGEMENT AUTHENTICATION CONTROLLER
 * ============================================================
 * 
 * PHASE 10: ROLE-BASED ACCESS CONTROL (RBAC)
 * 
 * REST API for Management Dashboard authentication.
 * 
 * BASE URL: /api/management/auth
 * 
 * ENDPOINTS:
 * - POST /api/management/auth/login → Login with credentials
 * - POST /api/management/auth/logout → Logout and clear session
 * - GET /api/management/auth/validate → Validate session token
 * - GET /api/management/auth/user → Get current user info
 * 
 * LOGIN RESPONSE INCLUDES:
 * - sessionToken: For subsequent API calls
 * - role: DEPARTMENT_HEAD or IT_COORDINATOR
 * - department: User's department code
 * - hasFullAccess: Whether user can see all departments
 * 
 * SECURITY:
 * - Session token must be sent in X-Session-Token header
 * - Department-level access enforced at backend
 * 
 * @author IT Service Management Team
 * @version 1.0 - RBAC Implementation
 */
@RestController
@RequestMapping("/api/management/auth")
@CrossOrigin(origins = "*")
public class ManagementAuthController {

    private static final Logger logger = LoggerFactory.getLogger(ManagementAuthController.class);

    private static final String SESSION_HEADER = "X-Session-Token";

    private final ManagementAuthService authService;

    public ManagementAuthController(ManagementAuthService authService) {
        this.authService = authService;
    }

    // ============================================================
    // LOGIN
    // ============================================================

    /**
     * Login to Management Dashboard.
     * 
     * ENDPOINT: POST /api/management/auth/login
     * 
     * REQUEST BODY:
     * {
     * "username": "network_head",
     * "password": "password123"
     * }
     * 
     * SUCCESS RESPONSE:
     * {
     * "success": true,
     * "message": "Login successful",
     * "data": {
     * "sessionToken": "uuid-timestamp",
     * "sessionExpiry": "2026-02-07T20:00:00",
     * "user": {
     * "userId": "MGMT-001",
     * "username": "network_head",
     * "name": "Network Department Head",
     * "role": "DEPARTMENT_HEAD",
     * "roleDisplayName": "Department Head",
     * "department": "NETWORK",
     * "departmentDisplayName": "Network Team",
     * "hasFullAccess": false
     * }
     * }
     * }
     * 
     * FAILURE RESPONSE:
     * {
     * "success": false,
     * "message": "Invalid username or password"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
            @RequestBody Map<String, String> credentials) {

        logger.info("API Request: Management Login");

        String username = credentials.get("username");
        String password = credentials.get("password");

        // Validate request
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Username is required"));
        }
        if (password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Password is required"));
        }

        try {
            AuthResult result = authService.login(username.trim(), password);

            if (result.isSuccess()) {
                Map<String, Object> data = new HashMap<>();
                data.put("sessionToken", result.getSessionToken());
                data.put("sessionExpiry", result.getSessionExpiry());
                data.put("user", result.getUserInfo());

                return ResponseEntity.ok(
                        ApiResponse.success(result.getMessage(), data));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        ApiResponse.error(result.getMessage()));
            }

        } catch (Exception e) {
            logger.error("Login error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }

    // ============================================================
    // LOGOUT
    // ============================================================

    /**
     * Logout from Management Dashboard.
     * 
     * ENDPOINT: POST /api/management/auth/logout
     * 
     * HEADERS:
     * - X-Session-Token: <session_token>
     * 
     * RESPONSE:
     * {
     * "success": true,
     * "message": "Logout successful"
     * }
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken) {

        logger.info("API Request: Management Logout");

        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return ResponseEntity.ok(
                    ApiResponse.success("Already logged out", null));
        }

        try {
            boolean loggedOut = authService.logout(sessionToken);

            if (loggedOut) {
                return ResponseEntity.ok(
                        ApiResponse.success("Logout successful", null));
            } else {
                return ResponseEntity.ok(
                        ApiResponse.success("Session already expired or invalid", null));
            }

        } catch (Exception e) {
            logger.error("Logout error: {}", e.getMessage(), e);
            return ResponseEntity.ok(
                    ApiResponse.success("Logout completed", null));
        }
    }

    // ============================================================
    // VALIDATE SESSION
    // ============================================================

    /**
     * Validate session token.
     * 
     * ENDPOINT: GET /api/management/auth/validate
     * 
     * HEADERS:
     * - X-Session-Token: <session_token>
     * 
     * RESPONSE:
     * {
     * "success": true,
     * "data": {
     * "valid": true,
     * "role": "DEPARTMENT_HEAD",
     * "department": "NETWORK",
     * "hasFullAccess": false
     * }
     * }
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateSession(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken) {

        logger.debug("API Request: Validate Session");

        Map<String, Object> data = new HashMap<>();

        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            data.put("valid", false);
            data.put("message", "No session token provided");
            return ResponseEntity.ok(ApiResponse.success("Session validation result", data));
        }

        try {
            Map<String, Object> userInfo = authService.getUserInfo(sessionToken);

            if (userInfo != null) {
                data.put("valid", true);
                data.put("role", userInfo.get("role"));
                data.put("department", userInfo.get("department"));
                data.put("departmentDisplayName", userInfo.get("departmentDisplayName"));
                data.put("hasFullAccess", userInfo.get("hasFullAccess"));
                data.put("name", userInfo.get("name"));
            } else {
                data.put("valid", false);
                data.put("message", "Session expired or invalid");
            }

            return ResponseEntity.ok(ApiResponse.success("Session validation result", data));

        } catch (Exception e) {
            logger.error("Session validation error: {}", e.getMessage());
            data.put("valid", false);
            data.put("message", "Validation error");
            return ResponseEntity.ok(ApiResponse.success("Session validation result", data));
        }
    }

    // ============================================================
    // GET CURRENT USER
    // ============================================================

    /**
     * Get current authenticated user info.
     * 
     * ENDPOINT: GET /api/management/auth/user
     * 
     * HEADERS:
     * - X-Session-Token: <session_token>
     * 
     * RESPONSE:
     * {
     * "success": true,
     * "data": {
     * "userId": "MGMT-001",
     * "username": "network_head",
     * "name": "Network Department Head",
     * "email": "network.head@powergrid.com",
     * "role": "DEPARTMENT_HEAD",
     * "roleDisplayName": "Department Head",
     * "department": "NETWORK",
     * "departmentDisplayName": "Network Team",
     * "hasFullAccess": false,
     * "lastLogin": "2026-02-07T12:00:00"
     * }
     * }
     */
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken) {

        logger.debug("API Request: Get Current User");

        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Authentication required"));
        }

        try {
            Map<String, Object> userInfo = authService.getUserInfo(sessionToken);

            if (userInfo != null) {
                return ResponseEntity.ok(
                        ApiResponse.success("User info retrieved", userInfo));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        ApiResponse.error("Session expired. Please login again."));
            }

        } catch (Exception e) {
            logger.error("Get user error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Failed to get user info"));
        }
    }

    // ============================================================
    // CHECK DEPARTMENT ACCESS
    // ============================================================

    /**
     * Check if user can access a specific department's data.
     * 
     * ENDPOINT: GET /api/management/auth/access?department=NETWORK
     * 
     * HEADERS:
     * - X-Session-Token: <session_token>
     * 
     * RESPONSE:
     * {
     * "success": true,
     * "data": {
     * "canAccess": true,
     * "effectiveDepartment": "NETWORK"
     * }
     * }
     */
    @GetMapping("/access")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkAccess(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken,
            @RequestParam(required = false) String department) {

        logger.debug("API Request: Check Access for department: {}", department);

        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Authentication required"));
        }

        try {
            Map<String, Object> data = new HashMap<>();

            boolean canAccess = authService.canAccessDepartment(sessionToken, department);
            String effectiveDept = authService.getEffectiveDepartment(sessionToken, department);

            data.put("canAccess", canAccess);
            data.put("requestedDepartment", department);
            data.put("effectiveDepartment", effectiveDept);

            return ResponseEntity.ok(ApiResponse.success("Access check result", data));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Access check error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Access check failed"));
        }
    }
}
