package com.powergrid.ticketsystem.service;

import com.powergrid.ticketsystem.entity.Engineer;
import com.powergrid.ticketsystem.entity.ManagementUser;
import com.powergrid.ticketsystem.repository.EngineerRepository;
import com.powergrid.ticketsystem.repository.ManagementUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ============================================================
 * PASSWORD MIGRATION SERVICE
 * ============================================================
 *
 * Automatically migrates all plain text passwords to bcrypt hashes.
 * Runs on application startup.
 *
 * WHAT IT DOES:
 * 1. Checks all existing engineers for plain text passwords
 * 2. Checks all existing management users for plain text passwords
 * 3. Hashes any plain text passwords (checks for $2a$ or $2b$ prefix)
 * 4. Saves hashed versions back to database
 * 5. Logs migration progress
 *
 * HOW IT DETECTS PLAIN TEXT:
 * - Bcrypt hashes always start with $2a$, $2b$, $2x$, or $2y$
 * - Plain text passwords don't have this format
 * - If password doesn't start with $2 → it's plain text → hash it
 *
 * SAFETY:
 * - Only hashes passwords once (checks for hash format first)
 * - Won't double-hash already hashed passwords
 * - Logs all changes for audit trail
 * - Runs automatically on every startup
 *
 * @author IT Service Management Team
 * @version 1.0 - Password Migration to BCrypt
 */
@Service
public class PasswordMigrationService implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(PasswordMigrationService.class);

    private final EngineerRepository engineerRepository;
    private final ManagementUserRepository managementUserRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordMigrationService(EngineerRepository engineerRepository,
            ManagementUserRepository managementUserRepository,
            PasswordEncoder passwordEncoder) {
        this.engineerRepository = engineerRepository;
        this.managementUserRepository = managementUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Runs automatically when application starts.
     * Spring Boot calls this after all beans are initialized.
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("════════════════════════════════════════════════════════");
        logger.info("PASSWORD MIGRATION SERVICE STARTING");
        logger.info("════════════════════════════════════════════════════════");

        migrateEngineerPasswords();
        migrateManagementUserPasswords();

        logger.info("════════════════════════════════════════════════════════");
        logger.info("PASSWORD MIGRATION SERVICE COMPLETED");
        logger.info("════════════════════════════════════════════════════════");
    }

    /**
     * Migrate all engineer passwords from plain text to bcrypt.
     */
    @Transactional
    private void migrateEngineerPasswords() {
        logger.info("Checking engineer passwords for migration...");

        List<Engineer> engineers = engineerRepository.findAll();
        int migratedCount = 0;
        int alreadyHashedCount = 0;

        for (Engineer engineer : engineers) {
            String password = engineer.getPassword();

            // Check if password is already hashed
            if (isAlreadyHashed(password)) {
                alreadyHashedCount++;
                logger.debug("✓ Engineer {} already has hashed password", engineer.getName());
                continue;
            }

            // Hash the plain text password
            try {
                String hashedPassword = passwordEncoder.encode(password);
                engineer.setPassword(hashedPassword);
                engineerRepository.save(engineer);
                migratedCount++;
                logger.info("✓ Migrated engineer: {} | Old: {} | New: {}***",
                        engineer.getName(),
                        password.substring(0, Math.min(5, password.length())),
                        hashedPassword.substring(0, 10));
            } catch (Exception e) {
                logger.error("✗ Failed to migrate engineer {}: {}", engineer.getName(), e.getMessage());
            }
        }

        logger.info("Engineer password migration: {} migrated, {} already hashed",
                migratedCount, alreadyHashedCount);
    }

    /**
     * Migrate all management user passwords from plain text to bcrypt.
     */
    @Transactional
    private void migrateManagementUserPasswords() {
        logger.info("Checking management user passwords for migration...");

        List<ManagementUser> users = managementUserRepository.findAll();
        int migratedCount = 0;
        int alreadyHashedCount = 0;

        for (ManagementUser user : users) {
            String password = user.getPassword();

            // Check if password is already hashed
            if (isAlreadyHashed(password)) {
                alreadyHashedCount++;
                logger.debug("✓ Management user {} already has hashed password", user.getUsername());
                continue;
            }

            // Hash the plain text password
            try {
                String hashedPassword = passwordEncoder.encode(password);
                user.setPassword(hashedPassword);
                managementUserRepository.save(user);
                migratedCount++;
                logger.info("✓ Migrated management user: {} | Old: {} | New: {}***",
                        user.getUsername(),
                        password.substring(0, Math.min(5, password.length())),
                        hashedPassword.substring(0, 10));
            } catch (Exception e) {
                logger.error("✗ Failed to migrate management user {}: {}", user.getUsername(), e.getMessage());
            }
        }

        logger.info("Management user password migration: {} migrated, {} already hashed",
                migratedCount, alreadyHashedCount);
    }

    /**
     * Check if a password is already hashed in bcrypt format.
     *
     * Bcrypt hashes always start with:
     * - $2a$ (original)
     * - $2b$ (fixed PHP issue)
     * - $2x$ (invalid)
     * - $2y$ (PHP specific)
     *
     * @param password Password to check
     * @return true if password is already hashed, false if plain text
     */
    private boolean isAlreadyHashed(String password) {
        if (password == null || password.length() < 4) {
            return false;
        }
        String prefix = password.substring(0, 4);
        return prefix.equals("$2a$") || prefix.equals("$2b$") ||
               prefix.equals("$2x$") || prefix.equals("$2y$");
    }
}
