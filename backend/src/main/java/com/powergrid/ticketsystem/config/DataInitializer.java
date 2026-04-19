package com.powergrid.ticketsystem.config;

import com.powergrid.ticketsystem.service.EngineerService;
import com.powergrid.ticketsystem.service.ManagementUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * ============================================================
 * DATA INITIALIZER
 * ============================================================
 * 
 * Initializes sample data for the IT Ticket System on startup.
 * 
 * PHASE 6 & 10: ENGINEER RESOLUTION WORKFLOW & RBAC
 * 
 * This component ensures that:
 * 1. Sample engineers are created for demo purposes
 * 2. Engineers can log in immediately after system starts
 * 3. Management users (IT Coordinators & Dept Heads) are created
 * 
 * SAMPLE ENGINEERS (matching TeamAssignmentService names):
 * ─────────────────────────────────────────────────────────
 * | Name | Email | Team |
 * |---------------------|--------------------------------|-------------------------|
 * | Rahul Sharma (NET) | rahul.sharma@powergrid.com | Network Team |
 * | Priya Singh (NET) | priya.singh@powergrid.com | Network Team |
 * | Sneha Patel (APP) | sneha.patel@powergrid.com | Application Support |
 * | Rajesh Verma (HW) | rajesh.verma@powergrid.com | Hardware Support |
 * | Kiran Rao (SEC) | kiran.rao@powergrid.com | IT Security |
 * | Sunita Sharma (EMAIL)| sunita.sharma@powergrid.com | Email Support |
 * 
 * MANAGEMENT USERS (PHASE 10 RBAC):
 * ─────────────────────────────────────────────────────────
 * IT COORDINATORS (Full Access):
 * admin / password123 → Suresh Kumar (All Departments)
 * coordinator / password123 → Lakshmi Nair (All Departments)
 * 
 * DEPARTMENT HEADS (Restricted to own department):
 * network_head / password123 → Arun Menon (Network only)
 * hardware_head / password123 → Priya Krishnan (Hardware only)
 * software_head / password123 → Ravi Shankar (Software only)
 * email_head / password123 → Meera Bhat (Email only)
 * security_head / password123 → Karthik Iyer (Security only)
 * general_head / password123 → Divya Sharma (General only)
 * 
 * PASSWORD FOR ALL: password123
 */
@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final EngineerService engineerService;
    private final ManagementUserService managementUserService;

    public DataInitializer(EngineerService engineerService,
            ManagementUserService managementUserService) {
        this.engineerService = engineerService;
        this.managementUserService = managementUserService;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("╔════════════════════════════════════════════════════════════╗");
        logger.info("║           DATA INITIALIZER - Starting...                   ║");
        logger.info("╚════════════════════════════════════════════════════════════╝");

        // Initialize sample engineers
        initializeEngineers();

        // Initialize management users (RBAC)
        initializeManagementUsers();

        logger.info("╔════════════════════════════════════════════════════════════╗");
        logger.info("║           DATA INITIALIZER - Complete!                     ║");
        logger.info("╠════════════════════════════════════════════════════════════╣");
        logger.info("║  ENGINEER PORTAL: http://localhost:8080/engineer/login.html║");
        logger.info("║  MANAGEMENT PORTAL: http://localhost:8080/management/login.html║");
        logger.info("╠════════════════════════════════════════════════════════════╣");
        logger.info("║  PASSWORD FOR ALL ACCOUNTS: password123                    ║");
        logger.info("╠════════════════════════════════════════════════════════════╣");
        logger.info("║  ENGINEER LOGINS (by username):                            ║");
        logger.info("║────────────────────────────────────────────────────────────║");
        logger.info("║  Sneha Patel (APP)      - Application Support Team         ║");
        logger.info("║  Vikram Reddy (APP)     - Application Support Team         ║");
        logger.info("║  Neha Gupta (APP)       - Application Support Team         ║");
        logger.info("║  Arjun Mehta (APP)      - Application Support Team         ║");
        logger.info("║  Rahul Sharma (NET)     - Network Team                     ║");
        logger.info("║  Priya Singh (NET)      - Network Team                     ║");
        logger.info("║  Amit Kumar (NET)       - Network Team                     ║");
        logger.info("║  Rajesh Verma (HW)      - Hardware Support Team            ║");
        logger.info("║  Anita Joshi (HW)       - Hardware Support Team            ║");
        logger.info("║  Kiran Rao (SEC)        - IT Security Team                 ║");
        logger.info("║  Deepak Nair (SEC)      - IT Security Team                 ║");
        logger.info("║  Sunita Sharma (EMAIL)  - Email Support Team               ║");
        logger.info("║  Manoj Iyer (EMAIL)     - Email Support Team               ║");
        logger.info("║  Ramesh Kumar (GEN)     - General IT Support               ║");
        logger.info("║  Sita Devi (GEN)        - General IT Support               ║");
        logger.info("╠════════════════════════════════════════════════════════════╣");
        logger.info("║  MANAGEMENT LOGINS (RBAC):                                 ║");
        logger.info("║────────────────────────────────────────────────────────────║");
        logger.info("║  IT COORDINATORS (FULL ACCESS - ALL DEPARTMENTS):          ║");
        logger.info("║    admin         → Suresh Kumar (IT Coordinator)           ║");
        logger.info("║    coordinator   → Lakshmi Nair (IT Coordinator)           ║");
        logger.info("║────────────────────────────────────────────────────────────║");
        logger.info("║  DEPARTMENT HEADS (RESTRICTED TO OWN DEPARTMENT):          ║");
        logger.info("║    network_head  → Arun Menon (Network only)               ║");
        logger.info("║    hardware_head → Priya Krishnan (Hardware only)          ║");
        logger.info("║    software_head → Ravi Shankar (Software only)            ║");
        logger.info("║    email_head    → Meera Bhat (Email only)                 ║");
        logger.info("║    security_head → Karthik Iyer (IT Security only)         ║");
        logger.info("║    general_head  → Divya Sharma (General IT only)          ║");
        logger.info("╚════════════════════════════════════════════════════════════╝");
    }

    private void initializeEngineers() {
        try {
            engineerService.initializeSampleEngineers();
        } catch (Exception e) {
            logger.error("Failed to initialize engineers: {}", e.getMessage());
        }
    }

    private void initializeManagementUsers() {
        try {
            managementUserService.initializeSampleUsers();
        } catch (Exception e) {
            logger.error("Failed to initialize management users: {}", e.getMessage());
        }
    }
}
