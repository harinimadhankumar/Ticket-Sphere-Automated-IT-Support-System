package com.powergrid.ticketsystem.service;

import com.powergrid.ticketsystem.entity.Engineer;
import com.powergrid.ticketsystem.entity.Ticket;
import com.powergrid.ticketsystem.repository.EngineerRepository;
import com.powergrid.ticketsystem.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ============================================================
 * ENGINEER SERVICE
 * ============================================================
 * 
 * PHASE 6: ENGINEER RESOLUTION WORKFLOW
 * 
 * This service handles:
 * 1. Engineer authentication (login/logout)
 * 2. Fetching assigned tickets for an engineer
 * 3. Engineer workload management
 * 4. Engineer statistics
 * 
 * DESIGN PHILOSOPHY:
 * ──────────────────
 * Engineers can RESOLVE tickets but NOT CLOSE them.
 * This ensures:
 * - Quality control through Phase 7 (AI Verification)
 * - Separation of concerns
 * - Audit trail for resolution activities
 */
@Service
public class EngineerService {

    private static final Logger logger = LoggerFactory.getLogger(EngineerService.class);

    private final EngineerRepository engineerRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;

    // Simple session storage (in production, use proper session management)
    private final Map<String, Engineer> activeSessions = new HashMap<>();

    public EngineerService(EngineerRepository engineerRepository,
            TicketRepository ticketRepository,
            PasswordEncoder passwordEncoder) {
        this.engineerRepository = engineerRepository;
        this.ticketRepository = ticketRepository;
        this.passwordEncoder = passwordEncoder;
        logger.info("Engineer Service initialized with BCrypt password encoding");
    }

    // ============================================================
    // AUTHENTICATION METHODS
    // ============================================================

    /**
     * Authenticate an engineer with email and password.
     *
     * BCRYPT PASSWORD HASHING:
     * ────────────────────────
     * - Passwords are stored as bcrypt hashes in database
     * - Never stored in plain text
     * - Uses salt internally (random data mixed with password)
     * - Same password produces different hash each time
     * - Verifies using encoder.matches(plaintext, hash)
     *
     * @param username Engineer's name (username)
     * @param password Engineer's plain text password
     * @return LoginResult with success status and session token
     */
    @Transactional
    public LoginResult login(String username, String password) {
        logger.info("Login attempt for username: {}", username);

        // Validate input
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return new LoginResult(false, null, null, "Username and password are required");
        }

        // Find engineer by name only (password verification happens below)
        Optional<Engineer> engineerOpt = engineerRepository.findByName(username);

        if (engineerOpt.isEmpty()) {
            logger.warn("Login failed for username: {} (user not found)", username);
            return new LoginResult(false, null, null, "Invalid username or password");
        }

        Engineer engineer = engineerOpt.get();

        // Verify password using bcrypt comparison
        if (!passwordEncoder.matches(password, engineer.getPassword())) {
            logger.warn("Login failed for username: {} (password mismatch)", username);
            return new LoginResult(false, null, null, "Invalid username or password");
        }

        // Check if engineer is active
        if (!Boolean.TRUE.equals(engineer.getIsActive())) {
            logger.warn("Login attempt for inactive engineer: {}", username);
            return new LoginResult(false, null, null, "Account is deactivated");
        }

        // Generate simple session token
        String sessionToken = UUID.randomUUID().toString();
        activeSessions.put(sessionToken, engineer);

        // Update last login time
        engineer.setLastLogin(LocalDateTime.now());
        engineerRepository.save(engineer);

        logger.info("Login successful for engineer: {}", engineer.getName());
        return new LoginResult(true, sessionToken, engineer, "Login successful");
    }

    /**
     * Logout an engineer.
     * 
     * @param sessionToken The session token to invalidate
     * @return true if logout successful
     */
    public boolean logout(String sessionToken) {
        if (sessionToken != null && activeSessions.containsKey(sessionToken)) {
            Engineer engineer = activeSessions.remove(sessionToken);
            logger.info("Logout successful for engineer: {}", engineer.getName());
            return true;
        }
        return false;
    }

    /**
     * Validate a session token.
     * 
     * @param sessionToken The session token to validate
     * @return Optional engineer if session is valid
     */
    public Optional<Engineer> validateSession(String sessionToken) {
        if (sessionToken == null || sessionToken.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(activeSessions.get(sessionToken));
    }

    /**
     * Get engineer from session token.
     * 
     * @param sessionToken Session token
     * @return Engineer or null
     */
    public Engineer getEngineerFromSession(String sessionToken) {
        return activeSessions.get(sessionToken);
    }

    // ============================================================
    // TICKET ACCESS METHODS
    // ============================================================

    /**
     * Get all tickets assigned to an engineer.
     * 
     * Engineers can only see their own assigned tickets.
     * This ensures data privacy and focused workload.
     * 
     * @param engineerName Name of the engineer
     * @return List of assigned tickets
     */
    public List<Ticket> getAssignedTickets(String engineerName) {
        logger.info("Fetching tickets for engineer: {}", engineerName);
        return ticketRepository.findByAssignedEngineer(engineerName);
    }

    /**
     * Get open/active tickets assigned to an engineer.
     * Excludes RESOLVED and CLOSED tickets.
     * 
     * @param engineerName Name of the engineer
     * @return List of active assigned tickets
     */
    public List<Ticket> getActiveAssignedTickets(String engineerName) {
        logger.info("Fetching active tickets for engineer: {}", engineerName);

        List<Ticket> allTickets = ticketRepository.findByAssignedEngineer(engineerName);

        // Filter out resolved and closed tickets
        return allTickets.stream()
                .filter(t -> !"RESOLVED".equalsIgnoreCase(t.getStatus())
                        && !"CLOSED".equalsIgnoreCase(t.getStatus()))
                .toList();
    }

    /**
     * Get tickets filtered by priority.
     * 
     * @param engineerName Name of the engineer
     * @param priority     Priority filter
     * @return Filtered list of tickets
     */
    public List<Ticket> getTicketsByPriority(String engineerName, String priority) {
        return getActiveAssignedTickets(engineerName).stream()
                .filter(t -> priority.equalsIgnoreCase(t.getPriority()))
                .toList();
    }

    /**
     * Get tickets approaching SLA deadline.
     * Useful for engineers to prioritize urgent work.
     * 
     * @param engineerName Name of the engineer
     * @return List of tickets nearing SLA breach
     */
    public List<Ticket> getTicketsNearingSla(String engineerName) {
        LocalDateTime threshold = LocalDateTime.now().plusMinutes(60); // Within 1 hour

        return getActiveAssignedTickets(engineerName).stream()
                .filter(t -> t.getSlaDeadline() != null
                        && t.getSlaDeadline().isBefore(threshold)
                        && !Boolean.TRUE.equals(t.getSlaBreached()))
                .toList();
    }

    /**
     * Get ticket statistics for an engineer.
     * 
     * @param engineerName Name of the engineer
     * @return Map of statistics
     */
    public Map<String, Object> getEngineerStats(String engineerName) {
        Map<String, Object> stats = new LinkedHashMap<>();

        List<Ticket> allTickets = getAssignedTickets(engineerName);
        List<Ticket> activeTickets = getActiveAssignedTickets(engineerName);

        stats.put("totalAssigned", allTickets.size());
        stats.put("activeTickets", activeTickets.size());
        stats.put("resolvedTickets", allTickets.stream()
                .filter(t -> "RESOLVED".equalsIgnoreCase(t.getStatus()))
                .count());

        // Count by priority
        Map<String, Long> byPriority = new LinkedHashMap<>();
        byPriority.put("CRITICAL", activeTickets.stream()
                .filter(t -> "CRITICAL".equalsIgnoreCase(t.getPriority())).count());
        byPriority.put("HIGH", activeTickets.stream()
                .filter(t -> "HIGH".equalsIgnoreCase(t.getPriority())).count());
        byPriority.put("MEDIUM", activeTickets.stream()
                .filter(t -> "MEDIUM".equalsIgnoreCase(t.getPriority())).count());
        byPriority.put("LOW", activeTickets.stream()
                .filter(t -> "LOW".equalsIgnoreCase(t.getPriority())).count());
        stats.put("byPriority", byPriority);

        // SLA nearing breach
        stats.put("slaNearingBreach", getTicketsNearingSla(engineerName).size());

        return stats;
    }

    // ============================================================
    // ENGINEER MANAGEMENT
    // ============================================================

    /**
     * Get engineer by ID.
     * 
     * @param id Engineer ID
     * @return Optional engineer
     */
    public Optional<Engineer> getEngineerById(Long id) {
        return engineerRepository.findById(id);
    }

    /**
     * Get engineer by name.
     * 
     * @param name Engineer name
     * @return Optional engineer
     */
    public Optional<Engineer> getEngineerByName(String name) {
        return engineerRepository.findByName(name);
    }

    /**
     * Get all engineers.
     * 
     * @return List of all engineers
     */
    public List<Engineer> getAllEngineers() {
        return engineerRepository.findAll();
    }

    /**
     * Create a new engineer.
     * Password is automatically hashed using bcrypt before storing.
     *
     * @param engineer Engineer to create (with plain text password)
     * @return Created engineer
     */
    @Transactional
    public Engineer createEngineer(Engineer engineer) {
        logger.info("Creating new engineer: {}", engineer.getName());

        // Hash the password before storing
        if (engineer.getPassword() != null && !engineer.getPassword().isEmpty()) {
            String hashedPassword = passwordEncoder.encode(engineer.getPassword());
            engineer.setPassword(hashedPassword);
            logger.debug("Password hashed using bcrypt for: {}", engineer.getName());
        }

        return engineerRepository.save(engineer);
    }

    /**
     * Update engineer profile information.
     *
     * @param engineerId  Engineer ID
     * @param name        Updated name (optional)
     * @param email       Updated email (optional)
     * @param phone       Updated phone (optional)
     * @param team        Updated team (optional)
     * @return Updated engineer
     */
    @Transactional
    public Engineer updateProfile(Long engineerId, String name, String email, String phone, String team) {
        logger.info("Starting profile update for engineer ID: {}", engineerId);

        if (engineerId == null) {
            logger.error("Engineer ID is null");
            throw new IllegalArgumentException("Engineer ID cannot be null");
        }

        Optional<Engineer> engineerOpt = engineerRepository.findById(engineerId);
        if (engineerOpt.isEmpty()) {
            logger.error("Engineer not found with ID: {}", engineerId);
            throw new IllegalArgumentException("Engineer not found with ID: " + engineerId);
        }

        Engineer engineer = engineerOpt.get();
        logger.debug("Found engineer: {} ({})", engineer.getName(), engineer.getId());

        // Update each field if provided
        if (name != null && !name.trim().isEmpty()) {
            logger.debug("Updating name from {} to {}", engineer.getName(), name);
            engineer.setName(name);
        }

        if (email != null && !email.trim().isEmpty()) {
            logger.debug("Updating email from {} to {}", engineer.getEmail(), email);
            engineer.setEmail(email);
        }

        if (phone != null) {
            logger.debug("Updating phone from {} to {}", engineer.getPhone(), phone);
            engineer.setPhone(phone);
        }

        if (team != null && !team.trim().isEmpty()) {
            logger.debug("Updating team from {} to {}", engineer.getTeam(), team);
            engineer.setTeam(team);
        }

        try {
            // SAVES TO DATABASE
            Engineer updated = engineerRepository.save(engineer);
            logger.info("Successfully updated profile for engineer ID: {}", engineerId);
            return updated;
        } catch (Exception e) {
            logger.error("Error saving engineer to database: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save engineer profile: " + e.getMessage(), e);
        }
    }

    /**
     * Update engineer workload.
     * 
     * @param engineerName Engineer name
     * @param delta        Change in workload (+1 or -1)
     */
    @Transactional
    public void updateWorkload(String engineerName, int delta) {
        engineerRepository.findByName(engineerName).ifPresent(engineer -> {
            int newWorkload = Math.max(0, engineer.getCurrentWorkload() + delta);
            engineer.setCurrentWorkload(newWorkload);
            engineerRepository.save(engineer);
            logger.debug("Updated workload for {}: {}", engineerName, newWorkload);
        });
    }

    /**
     * Increment resolved ticket count.
     * 
     * @param engineerName Engineer name
     */
    @Transactional
    public void incrementResolvedCount(String engineerName) {
        engineerRepository.findByName(engineerName).ifPresent(engineer -> {
            engineer.setTicketsResolved(engineer.getTicketsResolved() + 1);
            engineerRepository.save(engineer);
        });
    }

    // ============================================================
    // INITIALIZATION
    // ============================================================

    /**
     * Initialize sample engineers for demo.
     * Called on application startup. All engineers use same email for
     * notifications.
     * Login is by USERNAME (engineer name), not email.
     *
     * IMPORTANT: Passwords are automatically hashed using bcrypt
     * before storing in database.
     */
    @Transactional
    public void initializeSampleEngineers() {
        // All engineers receive notifications at this email
        String notificationEmail = "harinipriya3108@gmail.com";

        if (engineerRepository.count() > 0) {
            // Update existing engineers to use same notification email
            logger.info("Updating all engineers to use notification email: {}", notificationEmail);
            engineerRepository.findAll().forEach(engineer -> {
                if (!notificationEmail.equals(engineer.getEmail())) {
                    engineer.setEmail(notificationEmail);
                    // Hash password if it's not already hashed (check if it starts with $2a$ or $2b$)
                    if (!engineer.getPassword().startsWith("$2a$") && !engineer.getPassword().startsWith("$2b$")) {
                        String hashedPassword = passwordEncoder.encode(engineer.getPassword());
                        engineer.setPassword(hashedPassword);
                        logger.debug("Updated password to bcrypt hash for: {}", engineer.getName());
                    }
                    engineerRepository.save(engineer);
                    logger.info("Updated {} -> email: {}", engineer.getName(), notificationEmail);
                }
            });
            logger.info("All engineer emails set to: {}", notificationEmail);
            return;
        }

        logger.info("Initializing sample engineers (login by username, all notifications to {})...", notificationEmail);

        // IMPORTANT: These names MUST match the engineer names in TeamAssignmentService
        // so that tickets assigned by the NLP system show up in engineer dashboards
        // Login: Use engineer NAME as username, password: password123
        // All notification emails go to: harinipriya3108@gmail.com

        List<Engineer> engineers = Arrays.asList(
                // Network Team Engineers
                createEngineerWithDetails("Rahul Sharma (NET)", notificationEmail, "password123",
                        "Network Team", "VPN,WIFI,Network,Firewall,Router"),
                createEngineerWithDetails("Priya Singh (NET)", notificationEmail, "password123",
                        "Network Team", "VPN,WIFI,Network,Connectivity"),
                createEngineerWithDetails("Amit Kumar (NET)", notificationEmail, "password123",
                        "Network Team", "VPN,Network,Internet,DNS"),

                // Application Support Team Engineers
                createEngineerWithDetails("Sneha Patel (APP)", notificationEmail, "password123",
                        "Application Support Team", "Software,Installation,Crash,Update"),
                createEngineerWithDetails("Vikram Reddy (APP)", notificationEmail, "password123",
                        "Application Support Team", "Software,License,Error,Update"),
                createEngineerWithDetails("Neha Gupta (APP)", notificationEmail, "password123",
                        "Application Support Team", "Application,Crash,Performance"),
                createEngineerWithDetails("Arjun Mehta (APP)", notificationEmail, "password123",
                        "Application Support Team", "Software,Configuration,Update"),

                // Hardware Support Team Engineers
                createEngineerWithDetails("Rajesh Verma (HW)", notificationEmail, "password123",
                        "Hardware Support Team", "Laptop,Desktop,Printer,Monitor"),
                createEngineerWithDetails("Anita Joshi (HW)", notificationEmail, "password123",
                        "Hardware Support Team", "Hardware,Mouse,Keyboard,Repair"),

                // IT Security Team Engineers
                createEngineerWithDetails("Kiran Rao (SEC)", notificationEmail, "password123",
                        "IT Security Team", "Access,Password,Security,Permissions"),
                createEngineerWithDetails("Deepak Nair (SEC)", notificationEmail, "password123",
                        "IT Security Team", "Security,Firewall,Access,MFA"),

                // Email Support Team Engineers
                createEngineerWithDetails("Sunita Sharma (EMAIL)", notificationEmail, "password123",
                        "Email Support Team", "Email,Outlook,Calendar,Teams"),
                createEngineerWithDetails("Manoj Iyer (EMAIL)", notificationEmail, "password123",
                        "Email Support Team", "Email,Sync,Mailbox,Exchange"),

                // General Support Team Engineers
                createEngineerWithDetails("Ramesh Kumar (GEN)", notificationEmail, "password123",
                        "General IT Support", "General,Other,Support"),
                createEngineerWithDetails("Sita Devi (GEN)", notificationEmail, "password123",
                        "General IT Support", "General,HelpDesk,Support"));

        // Hash all passwords before saving
        engineers.forEach(engineer -> {
            String hashedPassword = passwordEncoder.encode(engineer.getPassword());
            engineer.setPassword(hashedPassword);
        });

        engineerRepository.saveAll(engineers);
        logger.info("Created {} sample engineers with bcrypt hashed passwords (login by username)", engineers.size());
    }

    private Engineer createEngineerWithDetails(String name, String email, String password,
            String team, String skills) {
        Engineer engineer = new Engineer(name, email, password, team);
        engineer.setSkills(skills);
        engineer.setRole("IT Engineer");
        engineer.setStatus("AVAILABLE");
        return engineer;
    }

    // ============================================================
    // INNER CLASSES
    // ============================================================

    /**
     * Result object for login operation.
     */
    public static class LoginResult {
        private final boolean success;
        private final String sessionToken;
        private final Engineer engineer;
        private final String message;

        public LoginResult(boolean success, String sessionToken, Engineer engineer, String message) {
            this.success = success;
            this.sessionToken = sessionToken;
            this.engineer = engineer;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getSessionToken() {
            return sessionToken;
        }

        public Engineer getEngineer() {
            return engineer;
        }

        public String getMessage() {
            return message;
        }
    }
}
