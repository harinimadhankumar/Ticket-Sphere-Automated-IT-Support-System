package com.powergrid.ticketsystem.service;

import com.powergrid.ticketsystem.entity.ManagementUser;
import com.powergrid.ticketsystem.entity.ManagementUser.ManagementRole;
import com.powergrid.ticketsystem.repository.ManagementUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ============================================================
 * MANAGEMENT USER SERVICE
 * ============================================================
 * 
 * PHASE 10: ROLE-BASED ACCESS CONTROL (RBAC)
 * 
 * Service for managing ManagementUser entities.
 * Handles user creation, lookup, and initialization.
 * 
 * @author IT Service Management Team
 * @version 1.0 - RBAC Implementation
 */
@Service
public class ManagementUserService {

        private static final Logger logger = LoggerFactory.getLogger(ManagementUserService.class);

        private final ManagementUserRepository repository;
        private final PasswordEncoder passwordEncoder;

        public ManagementUserService(ManagementUserRepository repository,
                PasswordEncoder passwordEncoder) {
                this.repository = repository;
                this.passwordEncoder = passwordEncoder;
        }

        /**
         * Initialize sample management users for demo.
         * Creates:
         * - 1 IT Coordinator (admin - full access)
         * - 6 Department Heads (one for each department)
         */
        @Transactional
        public void initializeSampleUsers() {
                logger.info("════════════════════════════════════════════════════════");
                logger.info("INITIALIZING MANAGEMENT USERS");
                logger.info("════════════════════════════════════════════════════════");

                // Default password for all demo users
                String defaultPassword = "password123";

                // ════════════════════════════════════════════════════════
                // IT COORDINATOR (FULL ACCESS)
                // ════════════════════════════════════════════════════════
                createIfNotExists(
                                "MGMT-IT-COORD-001",
                                "admin",
                                "Suresh Kumar (IT Coordinator)",
                                "harinipriya3108@gmail.com",
                                defaultPassword,
                                ManagementRole.IT_COORDINATOR,
                                "SOFTWARE" // Primary department (but can see all)
                );

                // Additional IT Coordinator
                createIfNotExists(
                                "MGMT-IT-COORD-002",
                                "coordinator",
                                "Lakshmi Nair (IT Coordinator)",
                                "harinipriya3108@gmail.com",
                                defaultPassword,
                                ManagementRole.IT_COORDINATOR,
                                "GENERAL");

                // ════════════════════════════════════════════════════════
                // DEPARTMENT HEADS (RESTRICTED TO OWN DEPARTMENT)
                // ════════════════════════════════════════════════════════

                // Network Team Head
                createIfNotExists(
                                "MGMT-DEPT-NET-001",
                                "network_head",
                                "Arun Menon (Network Head)",
                                "harinim23cse@srishakthi.ac.in",
                                defaultPassword,
                                ManagementRole.DEPARTMENT_HEAD,
                                "NETWORK");

                // Hardware Support Team Head
                createIfNotExists(
                                "MGMT-DEPT-HW-001",
                                "hardware_head",
                                "Priya Krishnan (Hardware Head)",
                                "harinim23cse@srishakthi.ac.in",
                                defaultPassword,
                                ManagementRole.DEPARTMENT_HEAD,
                                "HARDWARE");

                // Software/Application Support Team Head
                createIfNotExists(
                                "MGMT-DEPT-SW-001",
                                "software_head",
                                "Ravi Shankar (Software Head)",
                                "harinim23cse@srishakthi.ac.in",
                                defaultPassword,
                                ManagementRole.DEPARTMENT_HEAD,
                                "SOFTWARE");

                // Email Support Team Head
                createIfNotExists(
                                "MGMT-DEPT-EMAIL-001",
                                "email_head",
                                "Meera Bhat (Email Head)",
                                "harinim23cse@srishakthi.ac.in",
                                defaultPassword,
                                ManagementRole.DEPARTMENT_HEAD,
                                "EMAIL");

                // IT Security/Access Team Head
                createIfNotExists(
                                "MGMT-DEPT-SEC-001",
                                "security_head",
                                "Karthik Iyer (Security Head)",
                                "harinim23cse@srishakthi.ac.in",
                                defaultPassword,
                                ManagementRole.DEPARTMENT_HEAD,
                                "ACCESS");

                // General IT Support Team Head
                createIfNotExists(
                                "MGMT-DEPT-GEN-001",
                                "general_head",
                                "Divya Sharma (General IT Head)",
                                "harinim23cse@srishakthi.ac.in",
                                defaultPassword,
                                ManagementRole.DEPARTMENT_HEAD,
                                "GENERAL");

                // Log summary
                long totalUsers = repository.count();
                long coordinators = repository.countByRole(ManagementRole.IT_COORDINATOR);
                long deptHeads = repository.countByRole(ManagementRole.DEPARTMENT_HEAD);

                logger.info("════════════════════════════════════════════════════════");
                logger.info("MANAGEMENT USERS INITIALIZED");
                logger.info("  Total Users: {}", totalUsers);
                logger.info("  IT Coordinators: {} (Full Access)", coordinators);
                logger.info("  Department Heads: {} (Restricted)", deptHeads);
                logger.info("════════════════════════════════════════════════════════");
        }

        /**
         * Create a management user if not exists.
         * Password is automatically hashed using bcrypt before storing.
         */
        private void createIfNotExists(String userId, String username, String name,
                        String email, String password, ManagementRole role, String department) {

                if (repository.existsByUsername(username)) {
                        logger.debug("User {} already exists, skipping", username);
                        return;
                }

                // Hash the password before storing
                String hashedPassword = passwordEncoder.encode(password);

                ManagementUser user = new ManagementUser(
                                userId, username, name, email, hashedPassword, role, department);

                repository.save(user);

                logger.info("  ✓ Created: {} | {} | {} | {}",
                                username,
                                role == ManagementRole.IT_COORDINATOR ? "IT_COORDINATOR" : "DEPT_HEAD",
                                department,
                                name);
        }

        /**
         * Get all management users (for admin view).
         */
        public List<ManagementUser> getAllUsers() {
                return repository.findAll();
        }

        /**
         * Get all active management users.
         */
        public List<ManagementUser> getActiveUsers() {
                return repository.findAllActive();
        }

        /**
         * Get users by role.
         */
        public List<ManagementUser> getUsersByRole(ManagementRole role) {
                return repository.findByRole(role);
        }

        /**
         * Get users by department.
         */
        public List<ManagementUser> getUsersByDepartment(String department) {
                return repository.findByDepartment(department);
        }

        /**
         * Update management user profile.
         * Updates name and email fields.
         */
        @Transactional
        public ManagementUser updateProfile(Long userId, String name, String email) {
                logger.info("Updating profile for management user ID: {}", userId);

                ManagementUser user = repository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

                if (name != null && !name.isEmpty()) {
                        user.setName(name);
                }
                if (email != null && !email.isEmpty()) {
                        user.setEmail(email);
                }

                ManagementUser updated = repository.save(user);
                logger.info("Profile updated for user: {}", updated.getName());

                return updated;
        }
}
