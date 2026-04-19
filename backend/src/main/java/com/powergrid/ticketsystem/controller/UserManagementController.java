package com.powergrid.ticketsystem.controller;

import com.powergrid.ticketsystem.dto.ApiResponse;
import com.powergrid.ticketsystem.entity.Engineer;
import com.powergrid.ticketsystem.entity.ManagementUser;
import com.powergrid.ticketsystem.repository.EngineerRepository;
import com.powergrid.ticketsystem.repository.ManagementUserRepository;
import com.powergrid.ticketsystem.service.ManagementAuthService;
import com.powergrid.ticketsystem.service.ManagementUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ============================================================
 * USER MANAGEMENT CONTROLLER
 * ============================================================
 *
 * Manages engineers and management users with role-based access control.
 *
 * PERMISSIONS:
 * - IT_COORDINATOR: Can manage all management users (department heads)
 * - DEPARTMENT_HEAD: Can manage engineers in their department only
 * - ENGINEER: No access to user management
 *
 * BASE URL: /api/management/users
 *
 * @author IT Service Management Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/management/users")
@CrossOrigin(origins = "*")
public class UserManagementController {

    private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);
    private static final String SESSION_HEADER = "X-Session-Token";

    private final EngineerRepository engineerRepository;
    private final ManagementUserRepository managementUserRepository;
    private final ManagementAuthService authService;
    private final ManagementUserService managementUserService;

    public UserManagementController(EngineerRepository engineerRepository,
                                   ManagementUserRepository managementUserRepository,
                                   ManagementAuthService authService,
                                   ManagementUserService managementUserService) {
        this.engineerRepository = engineerRepository;
        this.managementUserRepository = managementUserRepository;
        this.authService = authService;
        this.managementUserService = managementUserService;
    }

    // ============================================================
    // ENGINEER MANAGEMENT (DEPARTMENT_HEAD only - own department)
    // ============================================================

    /**
     * Get all engineers in user's department
     * DEPT_HEAD can only see own department engineers
     */
    @GetMapping("/engineers")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getEngineersList(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken) {

        logger.info("API Request: Get engineers list");

        try {
            ManagementUser user = authService.getCurrentUser(sessionToken);

            // Only DEPARTMENT_HEAD can access this
            if (!user.getRole().equals(ManagementUser.ManagementRole.DEPARTMENT_HEAD)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Only Department Heads can manage engineers"));
            }

            // Get department from user
            String userDept = user.getDepartment();

            // Fetch engineers by team (matching department)
            List<Engineer> engineers = engineerRepository.findAll().stream()
                .filter(e -> e.getTeam() != null &&
                          getDepartmentFromTeam(e.getTeam()).equals(userDept))
                .collect(Collectors.toList());

            List<Map<String, Object>> engineersList = engineers.stream()
                .map(this::buildEngineerMap)
                .collect(Collectors.toList());

            logger.info("Retrieved {} engineers for department {}", engineersList.size(), userDept);
            return ResponseEntity.ok(ApiResponse.success("Engineers retrieved", engineersList));

        } catch (Exception e) {
            logger.error("Error getting engineers list: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to retrieve engineers"));
        }
    }

    /**
     * Add new engineer to department
     * Only accessible by DEPARTMENT_HEAD for their own department
     */
    @PostMapping("/engineers")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addEngineer(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken,
            @RequestBody Map<String, Object> engineerData) {

        logger.info("API Request: Add new engineer");

        try {
            ManagementUser user = authService.getCurrentUser(sessionToken);

            // Only DEPARTMENT_HEAD can add engineers
            if (!user.getRole().equals(ManagementUser.ManagementRole.DEPARTMENT_HEAD)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Only Department Heads can add engineers"));
            }

            // Validate required fields
            String name = (String) engineerData.get("name");
            String email = (String) engineerData.get("email");
            String password = (String) engineerData.get("password");
            String team = (String) engineerData.get("team");

            if (name == null || email == null || password == null || team == null) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error("Missing required fields: name, email, password, team"));
            }

            // Verify team belongs to user's department
            if (!getDepartmentFromTeam(team).equals(user.getDepartment())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Cannot add engineer to team outside your department"));
            }

            // Create new engineer
            Engineer engineer = new Engineer();
            engineer.setName(name);
            engineer.setEmail(email);
            engineer.setPassword(password);
            engineer.setTeam(team);
            engineer.setIsActive(true);

            Engineer saved = engineerRepository.save(engineer);

            logger.info("Created new engineer: {} in team: {}", saved.getName(), team);

            Map<String, Object> response = buildEngineerMap(saved);
            return ResponseEntity.ok(ApiResponse.success("Engineer added successfully", response));

        } catch (Exception e) {
            logger.error("Error adding engineer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to add engineer"));
        }
    }

    /**
     * Update engineer information
     * Only accessible by DEPARTMENT_HEAD for engineers in their department
     */
    @PutMapping("/engineers/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateEngineer(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken,
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {

        logger.info("API Request: Update engineer {}", id);

        try {
            ManagementUser user = authService.getCurrentUser(sessionToken);

            // Only DEPARTMENT_HEAD can update engineers
            if (!user.getRole().equals(ManagementUser.ManagementRole.DEPARTMENT_HEAD)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Only Department Heads can update engineers"));
            }

            Optional<Engineer> engineerOpt = engineerRepository.findById(id);
            if (engineerOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Engineer engineer = engineerOpt.get();

            // Verify engineer belongs to user's department
            if (!getDepartmentFromTeam(engineer.getTeam()).equals(user.getDepartment())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Cannot update engineer outside your department"));
            }

            // Update fields
            if (updates.containsKey("name")) engineer.setName((String) updates.get("name"));
            if (updates.containsKey("email")) engineer.setEmail((String) updates.get("email"));
            if (updates.containsKey("team")) engineer.setTeam((String) updates.get("team"));
            if (updates.containsKey("isActive")) engineer.setIsActive((Boolean) updates.get("isActive"));

            Engineer updated = engineerRepository.save(engineer);

            logger.info("Updated engineer: {}", updated.getName());

            Map<String, Object> response = buildEngineerMap(updated);
            return ResponseEntity.ok(ApiResponse.success("Engineer updated successfully", response));

        } catch (Exception e) {
            logger.error("Error updating engineer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to update engineer"));
        }
    }

    /**
     * Delete engineer
     * Only accessible by DEPARTMENT_HEAD for engineers in their department
     */
    @DeleteMapping("/engineers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEngineer(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken,
            @PathVariable Long id) {

        logger.info("API Request: Delete engineer {}", id);

        try {
            ManagementUser user = authService.getCurrentUser(sessionToken);

            // Only DEPARTMENT_HEAD can delete engineers
            if (!user.getRole().equals(ManagementUser.ManagementRole.DEPARTMENT_HEAD)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Only Department Heads can delete engineers"));
            }

            Optional<Engineer> engineerOpt = engineerRepository.findById(id);
            if (engineerOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Engineer engineer = engineerOpt.get();

            // Verify engineer belongs to user's department
            if (!getDepartmentFromTeam(engineer.getTeam()).equals(user.getDepartment())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Cannot delete engineer outside your department"));
            }

            engineerRepository.deleteById(id);

            logger.info("Deleted engineer: {}", engineer.getName());
            return ResponseEntity.ok(ApiResponse.success("Engineer deleted successfully", null));

        } catch (Exception e) {
            logger.error("Error deleting engineer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to delete engineer"));
        }
    }

    // ============================================================
    // MANAGEMENT USER MANAGEMENT (IT_COORDINATOR only)
    // ============================================================

    /**
     * Get all management users (department heads)
     * Only IT_COORDINATOR can access
     */
    @GetMapping("/managers")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getManagersList(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken) {

        logger.info("API Request: Get managers list");

        try {
            ManagementUser user = authService.getCurrentUser(sessionToken);

            // Only IT_COORDINATOR can access
            if (!user.hasFullAccess()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Only IT Coordinators can manage department heads"));
            }

            List<ManagementUser> managers = managementUserRepository.findAll().stream()
                .filter(m -> m.getRole().equals(ManagementUser.ManagementRole.DEPARTMENT_HEAD))
                .collect(Collectors.toList());

            List<Map<String, Object>> managersList = managers.stream()
                .map(this::buildManagerMap)
                .collect(Collectors.toList());

            logger.info("Retrieved {} department heads", managersList.size());
            return ResponseEntity.ok(ApiResponse.success("Managers retrieved", managersList));

        } catch (Exception e) {
            logger.error("Error getting managers list: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to retrieve managers"));
        }
    }

    /**
     * Add new management user (department head)
     * Only IT_COORDINATOR can add
     */
    @PostMapping("/managers")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addManager(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken,
            @RequestBody Map<String, Object> managerData) {

        logger.info("API Request: Add new manager");

        try {
            ManagementUser user = authService.getCurrentUser(sessionToken);

            // Only IT_COORDINATOR can add managers
            if (!user.hasFullAccess()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Only IT Coordinators can add department heads"));
            }

            // Validate required fields
            String name = (String) managerData.get("name");
            String username = (String) managerData.get("username");
            String password = (String) managerData.get("password");
            String department = (String) managerData.get("department");
            String email = (String) managerData.get("email");

            if (name == null || username == null || password == null ||
                department == null || email == null) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error("Missing required fields"));
            }

            // Create new manager
            ManagementUser manager = new ManagementUser();
            manager.setName(name);
            manager.setUsername(username);
            manager.setPassword(password);
            manager.setDepartment(department);
            manager.setEmail(email);
            manager.setRole(ManagementUser.ManagementRole.DEPARTMENT_HEAD);
            // Generate user ID
            String userId = "MGMT-" + department.substring(0, Math.min(3, department.length())).toUpperCase() + "-" +
                           System.nanoTime() % 10000;
            manager.setUserId(userId);
            manager.setIsActive(true);

            ManagementUser saved = managementUserRepository.save(manager);

            logger.info("Created new department head: {} for department: {}", saved.getName(), department);

            Map<String, Object> response = buildManagerMap(saved);
            return ResponseEntity.ok(ApiResponse.success("Manager added successfully", response));

        } catch (DataIntegrityViolationException e) {
            logger.warn("Duplicate username or email: {}", e.getMessage());
            if (e.getMessage().contains("username")) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error("Username already exists. Please choose a different username."));
            } else if (e.getMessage().contains("email")) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error("Email already exists. Please choose a different email."));
            }
            return ResponseEntity.badRequest().body(
                ApiResponse.error("This record already exists. Please use different values."));
        } catch (Exception e) {
            logger.error("Error adding manager: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to add manager"));
        }
    }

    /**
     * Update management user information
     * Only IT_COORDINATOR can update
     */
    @PutMapping("/managers/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateManager(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken,
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {

        logger.info("API Request: Update manager {}", id);

        try {
            ManagementUser user = authService.getCurrentUser(sessionToken);

            // Only IT_COORDINATOR can update managers
            if (!user.hasFullAccess()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Only IT Coordinators can update managers"));
            }

            Optional<ManagementUser> managerOpt = managementUserRepository.findById(id);
            if (managerOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            ManagementUser manager = managerOpt.get();

            // Update fields
            if (updates.containsKey("name")) manager.setName((String) updates.get("name"));
            if (updates.containsKey("email")) manager.setEmail((String) updates.get("email"));
            if (updates.containsKey("username")) manager.setUsername((String) updates.get("username"));
            if (updates.containsKey("department")) manager.setDepartment((String) updates.get("department"));

            ManagementUser updated = managementUserRepository.save(manager);

            logger.info("Updated manager: {}", updated.getName());

            Map<String, Object> response = buildManagerMap(updated);
            return ResponseEntity.ok(ApiResponse.success("Manager updated successfully", response));

        } catch (Exception e) {
            logger.error("Error updating manager: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to update manager"));
        }
    }

    /**
     * Delete management user
     * Only IT_COORDINATOR can delete
     */
    @DeleteMapping("/managers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteManager(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken,
            @PathVariable Long id) {

        logger.info("API Request: Delete manager {}", id);

        try {
            ManagementUser user = authService.getCurrentUser(sessionToken);

            // Only IT_COORDINATOR can delete managers
            if (!user.hasFullAccess()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Only IT Coordinators can delete managers"));
            }

            Optional<ManagementUser> managerOpt = managementUserRepository.findById(id);
            if (managerOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            ManagementUser manager = managerOpt.get();
            managementUserRepository.deleteById(id);

            logger.info("Deleted manager: {}", manager.getName());
            return ResponseEntity.ok(ApiResponse.success("Manager deleted successfully", null));

        } catch (Exception e) {
            logger.error("Error deleting manager: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to delete manager"));
        }
    }

    /**
     * Update current user's own profile
     * Any logged-in management user can update their own profile
     *
     * PUT /api/management/profile
     * Body: {
     * "name": "John Doe",
     * "email": "john@example.com"
     * }
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestBody Map<String, String> body,
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken) {

        logger.info("API Request: Update management user profile");

        try {
            ManagementUser currentUser = authService.getCurrentUser(sessionToken);

            if (currentUser.getId() == null) {
                logger.error("Management user from session has no ID");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "User ID is missing"));
            }

            logger.info("Updating profile for management user ID: {} with data: {}", currentUser.getId(), body);

            // Call service to update profile and save to database
            ManagementUser updated = managementUserService.updateProfile(
                    currentUser.getId(),
                    body.get("name"),
                    body.get("email")
            );

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            response.put("management", buildManagerMap(updated));
            response.put("timestamp", java.time.LocalDateTime.now());

            logger.info("Management user {} profile updated successfully", updated.getId());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Management user not found during profile update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "User not found: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating management user profile: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to update profile: " + e.getMessage(),
                             "details", e.getClass().getSimpleName()));
        }
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private Map<String, Object> buildEngineerMap(Engineer eng) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", eng.getId());
        map.put("name", eng.getName());
        map.put("email", eng.getEmail());
        map.put("engineerId", eng.getEngineerId());
        map.put("team", eng.getTeam());
        map.put("isActive", eng.getIsActive());
        map.put("createdAt", eng.getCreatedAt());
        return map;
    }

    private Map<String, Object> buildManagerMap(ManagementUser mgr) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", mgr.getId());
        map.put("name", mgr.getName());
        map.put("username", mgr.getUsername());
        map.put("email", mgr.getEmail());
        map.put("department", mgr.getDepartment());
        map.put("role", mgr.getRole().toString());
        map.put("createdAt", mgr.getCreatedAt());
        return map;
    }

    private String getDepartmentFromTeam(String team) {
        if (team == null) return null;
        if (team.contains("Network")) return "NETWORK";
        if (team.contains("Hardware")) return "HARDWARE";
        if (team.contains("Application")) return "SOFTWARE";
        if (team.contains("Email")) return "EMAIL";
        if (team.contains("Security")) return "ACCESS";
        if (team.contains("General")) return "GENERAL";
        return "GENERAL";
    }
}
