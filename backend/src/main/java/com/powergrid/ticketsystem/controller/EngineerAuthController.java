package com.powergrid.ticketsystem.controller;

import com.powergrid.ticketsystem.dto.ApiResponse;
import com.powergrid.ticketsystem.entity.Engineer;
import com.powergrid.ticketsystem.service.EngineerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ============================================================
 * ENGINEER AUTHENTICATION CONTROLLER
 * ============================================================
 * 
 * REST API for Engineer Dashboard authentication.
 * 
 * BASE URL: /api/engineer/auth
 * 
 * ENDPOINTS:
 * - POST /api/engineer/auth/login → Login with credentials
 * - POST /api/engineer/auth/logout → Logout and clear session
 * - GET /api/engineer/auth/validate → Validate session token
 * 
 * LOGIN RESPONSE INCLUDES:
 * - sessionToken: For subsequent API calls
 * - user: Engineer user information
 * 
 * SECURITY:
 * - Session token must be sent in X-Session-Token header
 * - Passwords are validated against backend
 * 
 * @author IT Service Management Team
 * @version 1.0 - Engineer Authentication
 */
@RestController
@RequestMapping("/api/engineer/auth")
@CrossOrigin(origins = "*")
public class EngineerAuthController {

    private static final Logger logger = LoggerFactory.getLogger(EngineerAuthController.class);
    private static final String SESSION_HEADER = "X-Session-Token";

    private final EngineerService engineerService;

    public EngineerAuthController(EngineerService engineerService) {
        this.engineerService = engineerService;
    }

    // ============================================================
    // LOGIN
    // ============================================================

    /**
     * Login to Engineer Dashboard.
     * 
     * ENDPOINT: POST /api/engineer/auth/login
     * 
     * REQUEST BODY:
     * {
     * "username": "john_engineer",
     * "password": "password123"
     * }
     * 
     * SUCCESS RESPONSE:
     * {
     * "success": true,
     * "message": "Login successful",
     * "data": {
     * "sessionToken": "uuid-timestamp",
     * "user": {
     * "userId": "ENG-001",
     * "name": "John Developer",
     * "role": "ENGINEER",
     * "roleDisplayName": "Support Engineer",
     * "department": "POWER",
     * "departmentDisplayName": "Power Systems Team"
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

        logger.info("API Request: Engineer Login");

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
            EngineerService.LoginResult result = engineerService.login(username.trim(), password);

            if (result.isSuccess()) {
                Map<String, Object> data = new HashMap<>();
                data.put("sessionToken", result.getSessionToken());
                data.put("user", buildEngineerResponse(result.getEngineer()));

                logger.info("Engineer login successful for: {}", username);
                return ResponseEntity.ok(
                        ApiResponse.success(result.getMessage(), data));
            } else {
                logger.warn("Engineer login failed for: {}", username);
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
     * Logout from Engineer Dashboard.
     * 
     * ENDPOINT: POST /api/engineer/auth/logout
     * HEADER: X-Session-Token: session-token-value
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken) {

        logger.info("API Request: Engineer Logout");

        boolean success = engineerService.logout(sessionToken);

        if (success) {
            logger.info("Engineer logout successful");
            return ResponseEntity.ok(
                    ApiResponse.success("Logout successful", null));
        } else {
            logger.warn("Engineer logout failed - invalid session");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Invalid session token"));
        }
    }

    // ============================================================
    // VALIDATE SESSION
    // ============================================================

    /**
     * Validate session token.
     * Frontend uses this to check if session is still valid.
     * 
     * ENDPOINT: GET /api/engineer/auth/validate
     * HEADER: X-Session-Token: session-token-value
     * 
     * SUCCESS RESPONSE:
     * {
     * "success": true,
     * "data": {
     * "valid": true,
     * "user": { ... engineer info ... }
     * }
     * }
     * 
     * FAILURE RESPONSE:
     * {
     * "success": false,
     * "message": "Session expired or invalid"
     * }
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateSession(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken) {

        logger.info("API Request: Engineer Session Validation");

        Optional<Engineer> engineerOpt = engineerService.validateSession(sessionToken);

        if (engineerOpt.isPresent()) {
            Map<String, Object> data = new HashMap<>();
            data.put("valid", true);
            data.put("user", buildEngineerResponse(engineerOpt.get()));

            logger.info("Engineer session validation: SUCCESS");
            return ResponseEntity.ok(
                    ApiResponse.success("Session valid", data));
        } else {
            logger.warn("Engineer session validation: FAILED - Invalid or expired token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Invalid or expired session token"));
        }
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Build engineer response object with necessary fields.
     */
    private Map<String, Object> buildEngineerResponse(Engineer engineer) {
        Map<String, Object> response = new HashMap<>();

        response.put("userId", engineer.getId());
        response.put("name", engineer.getName());
        response.put("email", engineer.getEmail());
        response.put("role", "ENGINEER");
        response.put("roleDisplayName", "Support Engineer");
        response.put("department", engineer.getTeam() != null ? engineer.getTeam() : "UNASSIGNED");
        response.put("departmentDisplayName", getDepartmentDisplayName(engineer.getTeam()));
        response.put("status", engineer.getStatus());

        return response;
    }

    /**
     * Get display name for department/team.
     */
    private String getDepartmentDisplayName(String team) {
        if (team == null)
            return "Unassigned";
        return switch (team.toUpperCase()) {
            case "POWER" -> "Power Systems Team";
            case "NETWORK" -> "Network Team";
            case "HARDWARE" -> "Hardware Team";
            case "SOFTWARE" -> "Software Team";
            default -> team;
        };
    }
}
