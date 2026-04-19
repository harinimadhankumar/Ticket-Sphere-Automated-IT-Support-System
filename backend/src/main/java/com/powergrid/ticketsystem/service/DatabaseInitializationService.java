package com.powergrid.ticketsystem.service;

import com.powergrid.ticketsystem.entity.DepartmentEntity;
import com.powergrid.ticketsystem.repository.DepartmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Database initialization service
 * Populates departments table on application startup if empty
 */
@Component
public class DatabaseInitializationService implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializationService.class);
    private final DepartmentRepository departmentRepository;

    public DatabaseInitializationService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initializeDepartments();
    }

    private void initializeDepartments() {
        try {
            // Check if departments already exist
            if (departmentRepository.count() > 0) {
                logger.info("Departments already initialized in database");
                return;
            }

            logger.info("Initializing departments in database...");

            // Create the 6 standard departments
            departmentRepository.save(new DepartmentEntity(
                "NETWORK", "NETWORK", "Network Team", "Network Support"
            ));
            departmentRepository.save(new DepartmentEntity(
                "HARDWARE", "HARDWARE", "Hardware Support Team", "Hardware Support"
            ));
            departmentRepository.save(new DepartmentEntity(
                "SOFTWARE", "SOFTWARE", "Application Support Team", "Software Support"
            ));
            departmentRepository.save(new DepartmentEntity(
                "EMAIL", "EMAIL", "Email Support Team", "Email Support"
            ));
            departmentRepository.save(new DepartmentEntity(
                "ACCESS", "ACCESS", "IT Security Team", "Access Control & Security"
            ));
            departmentRepository.save(new DepartmentEntity(
                "GENERAL", "GENERAL", "General IT Support", "General Support"
            ));

            logger.info("Successfully initialized 6 departments in database");

        } catch (Exception e) {
            logger.error("Error initializing departments: {}", e.getMessage(), e);
        }
    }
}
