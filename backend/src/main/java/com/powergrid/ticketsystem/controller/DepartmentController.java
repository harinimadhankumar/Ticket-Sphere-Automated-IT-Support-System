package com.powergrid.ticketsystem.controller;

import com.powergrid.ticketsystem.dto.ApiResponse;
import com.powergrid.ticketsystem.entity.DepartmentEntity;
import com.powergrid.ticketsystem.entity.ManagementUser;
import com.powergrid.ticketsystem.repository.DepartmentRepository;
import com.powergrid.ticketsystem.service.ManagementAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * ============================================================
 * DEPARTMENT CONTROLLER
 * ============================================================
 *
 * Manages department CRUD operations in database.
 *
 * PERMISSIONS:
 * - IT_COORDINATOR: Can create, read, update, delete departments
 * - DEPARTMENT_HEAD: Can view all departments
 * - ENGINEER: Cannot access
 *
 * @author IT Service Management Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/management/departments")
@CrossOrigin(origins = "*")
public class DepartmentController {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentController.class);
    private static final String SESSION_HEADER = "X-Session-Token";

    private final ManagementAuthService authService;
    private final DepartmentRepository departmentRepository;

    public DepartmentController(ManagementAuthService authService, DepartmentRepository departmentRepository) {
        this.authService = authService;
        this.departmentRepository = departmentRepository;
    }

    /**
     * Get all departments from database
     * Public endpoint - no authentication required for reads
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDepartments() {

        logger.info("API Request: Get all departments");

        try {
            List<DepartmentEntity> departments = departmentRepository.findAllByOrderByCodeAsc();
            List<Map<String, Object>> result = new ArrayList<>();

            for (DepartmentEntity dept : departments) {
                Map<String, Object> deptMap = new LinkedHashMap<>();
                deptMap.put("code", dept.getCode());
                deptMap.put("name", dept.getName());
                deptMap.put("teamName", dept.getTeamName());
                deptMap.put("shortName", dept.getShortName());
                result.add(deptMap);
            }

            logger.info("Retrieved {} departments", result.size());
            return ResponseEntity.ok(ApiResponse.success("Departments retrieved", result));

        } catch (Exception e) {
            logger.error("Error getting departments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to retrieve departments"));
        }
    }

    /**
     * Get department by code
     */
    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDepartmentByCode(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken,
            @PathVariable String code) {

        logger.info("API Request: Get department {}", code);

        try {
            ManagementUser user = authService.getCurrentUser(sessionToken);

            if (!user.hasFullAccess() && !user.getRole().equals(ManagementUser.ManagementRole.DEPARTMENT_HEAD)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Unauthorized to view departments"));
            }

            Optional<DepartmentEntity> optDept = departmentRepository.findByCode(code);
            if (!optDept.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            DepartmentEntity dept = optDept.get();
            Map<String, Object> deptMap = new LinkedHashMap<>();
            deptMap.put("code", dept.getCode());
            deptMap.put("name", dept.getName());
            deptMap.put("teamName", dept.getTeamName());
            deptMap.put("shortName", dept.getShortName());

            logger.info("Retrieved department: {}", dept.getTeamName());
            return ResponseEntity.ok(ApiResponse.success("Department retrieved", deptMap));

        } catch (Exception e) {
            logger.error("Error getting department: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to retrieve department"));
        }
    }

    /**
     * Create new department (IT_COORDINATOR only)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createDepartment(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken,
            @RequestBody Map<String, String> deptData) {

        logger.info("API Request: Create new department");

        try {
            ManagementUser user = authService.getCurrentUser(sessionToken);

            // Only IT_COORDINATOR can create departments
            if (!user.hasFullAccess()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Only IT Coordinators can create departments"));
            }

            String code = deptData.get("code");
            String teamName = deptData.get("teamName");
            String shortName = deptData.get("shortName");

            if (code == null || code.isEmpty() || teamName == null || teamName.isEmpty() || shortName == null || shortName.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error("All fields are required"));
            }

            if (departmentRepository.existsByCode(code)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ApiResponse.error("Department code already exists"));
            }

            DepartmentEntity newDept = new DepartmentEntity(code.toUpperCase(), code.toUpperCase(), teamName, shortName);
            DepartmentEntity saved = departmentRepository.save(newDept);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("code", saved.getCode());
            result.put("name", saved.getName());
            result.put("teamName", saved.getTeamName());
            result.put("shortName", saved.getShortName());

            logger.info("Created new department: {}", code);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Department created successfully", result));

        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiResponse.error("Department code already exists. Please choose a different code."));
        } catch (Exception e) {
            logger.error("Error creating department: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to create department"));
        }
    }

    /**
     * Update department (IT_COORDINATOR only)
     */
    @PutMapping("/{code}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateDepartment(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken,
            @PathVariable String code,
            @RequestBody Map<String, String> deptData) {

        logger.info("API Request: Update department {}", code);

        try {
            ManagementUser user = authService.getCurrentUser(sessionToken);

            // Only IT_COORDINATOR can update departments
            if (!user.hasFullAccess()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Only IT Coordinators can update departments"));
            }

            Optional<DepartmentEntity> optDept = departmentRepository.findByCode(code);
            if (!optDept.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            String teamName = deptData.get("teamName");
            String shortName = deptData.get("shortName");

            if (teamName == null || teamName.isEmpty() || shortName == null || shortName.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error("Team name and short name are required"));
            }

            DepartmentEntity dept = optDept.get();
            dept.setTeamName(teamName);
            dept.setShortName(shortName);
            DepartmentEntity updated = departmentRepository.save(dept);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("code", updated.getCode());
            result.put("name", updated.getName());
            result.put("teamName", updated.getTeamName());
            result.put("shortName", updated.getShortName());

            logger.info("Updated department: {}", code);
            return ResponseEntity.ok(ApiResponse.success("Department updated successfully", result));

        } catch (Exception e) {
            logger.error("Error updating department: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to update department"));
        }
    }

    /**
     * Delete department (IT_COORDINATOR only)
     */
    @DeleteMapping("/{code}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken,
            @PathVariable String code) {

        logger.info("API Request: Delete department {}", code);

        try {
            ManagementUser user = authService.getCurrentUser(sessionToken);

            // Only IT_COORDINATOR can delete departments
            if (!user.hasFullAccess()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Only IT Coordinators can delete departments"));
            }

            Optional<DepartmentEntity> optDept = departmentRepository.findByCode(code);
            if (!optDept.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            departmentRepository.deleteById(optDept.get().getId());

            logger.info("Deleted department: {}", code);
            return ResponseEntity.ok(ApiResponse.success("Department deleted successfully", null));

        } catch (Exception e) {
            logger.error("Error deleting department: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to delete department"));
        }
    }
}
