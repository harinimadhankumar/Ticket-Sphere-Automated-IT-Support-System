package com.powergrid.ticketsystem.repository;

import com.powergrid.ticketsystem.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * TICKET REPOSITORY - Data Access Layer (JPA/MySQL)
 * ============================================================
 * 
 * JPA Repository for Ticket entity.
 * Provides CRUD operations and custom queries for ticket management.
 * 
 * Uses Spring Data JPA which automatically implements:
 * - save(), findById(), findAll(), delete(), etc.
 * 
 * Custom query methods follow Spring Data naming conventions
 * for automatic query generation.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

        // ============================================================
        // FIND BY TICKET ID (String)
        // ============================================================

        /**
         * Find ticket by the generated ticket ID (e.g., TKT-123456789).
         * 
         * @param ticketId The ticket ID string
         * @return Optional containing the ticket if found
         */
        Optional<Ticket> findByTicketId(String ticketId);

        // ============================================================
        // QUERY BY SOURCE
        // Used to filter tickets by origin channel
        // ============================================================

        /**
         * Find all tickets from a specific source channel.
         * 
         * @param source Source channel (EMAIL/CHATBOT)
         * @return List of tickets from the specified source
         * 
         *         Example: findBySource("EMAIL") returns all email tickets
         */
        List<Ticket> findBySource(String source);

        // ============================================================
        // QUERY BY STATUS
        // Used to filter tickets by current status
        // ============================================================

        /**
         * Find all tickets with a specific status.
         * 
         * @param status Ticket status (OPEN/IN_PROGRESS/RESOLVED/CLOSED)
         * @return List of tickets with the specified status
         * 
         *         Example: findByStatus("OPEN") returns all open tickets
         */
        List<Ticket> findByStatus(String status);

        /**
         * Find all tickets that are NOT in a specific status.
         * Useful for finding all active (non-closed) tickets.
         * 
         * @param status Status to exclude
         * @return List of tickets not in the specified status
         */
        List<Ticket> findByStatusNot(String status);

        // ============================================================
        // QUERY BY EMPLOYEE
        // Used to find all tickets raised by a specific employee
        // ============================================================

        /**
         * Find all tickets raised by a specific employee.
         * 
         * @param employeeId Employee identifier
         * @return List of tickets from the employee
         */
        List<Ticket> findByEmployeeId(String employeeId);

        /**
         * Find tickets by employee ID and status.
         * Useful for finding open tickets for a specific employee.
         * 
         * @param employeeId Employee identifier
         * @param status     Ticket status
         * @return List of matching tickets
         */
        List<Ticket> findByEmployeeIdAndStatus(String employeeId, String status);

        // ============================================================
        // COMBINED QUERIES
        // ============================================================

        /**
         * Find tickets by source and status.
         * Example: All OPEN tickets from EMAIL source.
         * 
         * @param source Source channel
         * @param status Ticket status
         * @return List of matching tickets
         */
        List<Ticket> findBySourceAndStatus(String source, String status);

        // ============================================================
        // DATE-BASED QUERIES
        // Useful for reporting and analytics
        // ============================================================

        /**
         * Find tickets created after a specific date/time.
         * 
         * @param dateTime Start date/time
         * @return List of tickets created after the specified time
         */
        List<Ticket> findByCreatedTimeAfter(LocalDateTime dateTime);

        /**
         * Find tickets created between two dates.
         * 
         * @param startTime Start of date range
         * @param endTime   End of date range
         * @return List of tickets within the date range
         */
        List<Ticket> findByCreatedTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

        // ============================================================
        // CUSTOM JPQL QUERIES
        // For more complex query requirements
        // ============================================================

        /**
         * Find tickets with issue description containing specific text.
         * Case-insensitive search.
         * 
         * @param keyword Search keyword
         * @return List of tickets containing the keyword
         */
        @Query("SELECT t FROM Ticket t WHERE LOWER(t.issueDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        List<Ticket> searchByIssueDescription(@Param("keyword") String keyword);

        /**
         * Count tickets by source channel.
         * Useful for dashboard statistics.
         * 
         * @param source Source channel
         * @return Count of tickets from the source
         */
        long countBySource(String source);

        /**
         * Count tickets by status.
         * Useful for dashboard statistics.
         * 
         * @param status Ticket status
         * @return Count of tickets with the status
         */
        long countByStatus(String status);

        /**
         * Check if a ticket exists with the given sender email and subject.
         * Used to prevent duplicate email ticket creation.
         * 
         * @param senderEmail  Sender's email address
         * @param emailSubject Email subject line
         * @return true if ticket exists, false otherwise
         */
        boolean existsBySenderEmailAndEmailSubject(String senderEmail, String emailSubject);

        // ============================================================
        // SORTED QUERIES
        // ============================================================

        /**
         * Find all tickets ordered by creation time (newest first).
         * 
         * @return List of all tickets sorted by creation time descending
         */
        List<Ticket> findAllByOrderByCreatedTimeDesc();

        /**
         * Find tickets by status ordered by creation time (newest first).
         * 
         * @param status Ticket status
         * @return Sorted list of tickets
         */
        List<Ticket> findByStatusOrderByCreatedTimeDesc(String status);

        /**
         * Find top N most recent tickets.
         * Used for dashboard recent tickets display.
         * 
         * @param pageable Pagination info with limit
         * @return List of most recent tickets
         */
        List<Ticket> findAllByOrderByCreatedTimeDesc(org.springframework.data.domain.Pageable pageable);

        // ============================================================
        // PHASE 2 & 3: CLASSIFICATION QUERIES
        // ============================================================

        /**
         * Find all tickets that have not been classified yet.
         * Used by the classification service to find pending tickets.
         * 
         * @return List of unclassified tickets
         */
        List<Ticket> findByIsClassifiedFalseOrIsClassifiedIsNull();

        /**
         * Find tickets by category.
         * 
         * @param category Ticket category
         * @return List of tickets in the category
         */
        List<Ticket> findByCategory(String category);

        /**
         * Find tickets by priority.
         * 
         * @param priority Ticket priority
         * @return List of tickets with the priority
         */
        List<Ticket> findByPriority(String priority);

        /**
         * Find tickets assigned to a specific team.
         * 
         * @param assignedTeam Team name
         * @return List of tickets assigned to the team
         */
        List<Ticket> findByAssignedTeam(String assignedTeam);

        /**
         * Find tickets assigned to a specific engineer.
         * 
         * @param assignedEngineer Engineer name
         * @return List of tickets assigned to the engineer
         */
        List<Ticket> findByAssignedEngineer(String assignedEngineer);

        /**
         * Count tickets by category.
         * 
         * @param category Ticket category
         * @return Count of tickets
         */
        long countByCategory(String category);

        /**
         * Count tickets by priority.
         * 
         * @param priority Ticket priority
         * @return Count of tickets
         */
        long countByPriority(String priority);

        /**
         * Count tickets assigned to an engineer (for workload tracking).
         * Only counts non-closed tickets.
         * 
         * @param assignedEngineer Engineer name
         * @param status           Status to exclude (e.g., CLOSED)
         * @return Count of active tickets
         */
        long countByAssignedEngineerAndStatusNot(String assignedEngineer, String status);

        // ============================================================
        // PHASE 4: SELF-SERVICE RESOLUTION QUERIES
        // ============================================================

        /**
         * Find tickets by resolution status.
         * 
         * @param resolutionStatus The resolution status
         * @return List of tickets with the specified resolution status
         */
        List<Ticket> findByResolutionStatus(String resolutionStatus);

        /**
         * Find tickets where resolution status is null or matches.
         * Used to find pending tickets for self-service processing.
         * 
         * @param resolutionStatus The resolution status to match
         * @return List of matching tickets
         */
        @Query("SELECT t FROM Ticket t WHERE t.resolutionStatus = :status OR t.resolutionStatus IS NULL")
        List<Ticket> findByResolutionStatusOrResolutionStatusIsNull(@Param("status") String resolutionStatus);

        /**
         * Count tickets by resolution status.
         * 
         * @param resolutionStatus The resolution status
         * @return Count of tickets
         */
        long countByResolutionStatus(String resolutionStatus);

        /**
         * Find tickets closed by SYSTEM (auto-closed).
         * 
         * @return List of auto-closed tickets
         */
        List<Ticket> findByClosedBy(String closedBy);

        /**
         * Count tickets closed by a specific entity.
         * 
         * @param closedBy Who closed the ticket (SYSTEM, ENGINEER, USER)
         * @return Count of tickets
         */
        long countByClosedBy(String closedBy);

        /**
         * Find tickets that used a specific knowledge base entry.
         * 
         * @param knowledgeBaseId The knowledge base entry ID
         * @return List of tickets
         */
        List<Ticket> findByKnowledgeBaseId(Long knowledgeBaseId);

        /**
         * Find tickets awaiting user response (solution sent but not confirmed).
         * Tickets where solution was sent before cutoff time and still awaiting.
         * 
         * @param statuses   Resolution statuses to include
         * @param cutoffTime Time before which solution was sent
         * @return List of tickets needing follow-up
         */
        @Query("SELECT t FROM Ticket t WHERE t.resolutionStatus IN :statuses " +
                        "AND t.solutionSentTime < :cutoffTime")
        List<Ticket> findAwaitingResponseBefore(
                        @Param("statuses") List<String> statuses,
                        @Param("cutoffTime") LocalDateTime cutoffTime);

        /**
         * Find self-resolved tickets within a date range.
         * Used for reporting self-service success.
         * 
         * @param resolutionStatus Status to match (SELF_RESOLVED)
         * @param startTime        Start of date range
         * @param endTime          End of date range
         * @return List of tickets
         */
        List<Ticket> findByResolutionStatusAndClosedTimeBetween(
                        String resolutionStatus,
                        LocalDateTime startTime,
                        LocalDateTime endTime);

        /**
         * Find escalated tickets within a date range.
         * 
         * @param startTime Start of date range
         * @param endTime   End of date range
         * @return List of escalated tickets
         */
        @Query("SELECT t FROM Ticket t WHERE t.resolutionStatus = 'ESCALATED' " +
                        "AND t.escalatedTime BETWEEN :startTime AND :endTime")
        List<Ticket> findEscalatedBetween(
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        /**
         * Get self-service statistics for a category.
         * 
         * @param category The ticket category
         * @return Count of self-resolved tickets
         */
        @Query("SELECT COUNT(t) FROM Ticket t WHERE t.category = :category " +
                        "AND t.resolutionStatus = 'SELF_RESOLVED'")
        long countSelfResolvedByCategory(@Param("category") String category);

        // ============================================================
        // PHASE 5: SLA MONITORING & ESCALATION QUERIES
        // ============================================================

        /**
         * Find all active tickets for SLA monitoring.
         * Active tickets are those not yet CLOSED or RESOLVED.
         * 
         * @param excludedStatuses Statuses to exclude (CLOSED, RESOLVED)
         * @return List of active tickets
         */
        @Query("SELECT t FROM Ticket t WHERE t.status NOT IN :excludedStatuses")
        List<Ticket> findActiveTickets(@Param("excludedStatuses") List<String> excludedStatuses);

        /**
         * Find tickets where SLA deadline has passed.
         * Used to identify SLA breaches.
         * 
         * @param currentTime      Current time
         * @param excludedStatuses Statuses to exclude
         * @return List of SLA-breached tickets
         */
        @Query("SELECT t FROM Ticket t WHERE t.slaDeadline < :currentTime " +
                        "AND t.status NOT IN :excludedStatuses")
        List<Ticket> findSlaBreachedTickets(
                        @Param("currentTime") LocalDateTime currentTime,
                        @Param("excludedStatuses") List<String> excludedStatuses);

        /**
         * Find tickets approaching SLA deadline (warning zone).
         * Used for proactive notifications.
         * 
         * @param warningTime      Time threshold for warning
         * @param currentTime      Current time
         * @param excludedStatuses Statuses to exclude
         * @return List of tickets nearing SLA breach
         */
        @Query("SELECT t FROM Ticket t WHERE t.slaDeadline BETWEEN :currentTime AND :warningTime " +
                        "AND t.status NOT IN :excludedStatuses " +
                        "AND (t.slaWarningSent = false OR t.slaWarningSent IS NULL)")
        List<Ticket> findTicketsApproachingSla(
                        @Param("currentTime") LocalDateTime currentTime,
                        @Param("warningTime") LocalDateTime warningTime,
                        @Param("excludedStatuses") List<String> excludedStatuses);

        /**
         * Find tickets by escalation level.
         * 
         * @param escalationLevel The escalation level
         * @return List of tickets at that level
         */
        List<Ticket> findByEscalationLevel(String escalationLevel);

        /**
         * Find tickets where SLA has been breached.
         * 
         * @return List of SLA-breached tickets
         */
        List<Ticket> findBySlaBreachedTrue();

        /**
         * Count tickets by escalation level.
         * 
         * @param escalationLevel The escalation level
         * @return Count of tickets
         */
        long countByEscalationLevel(String escalationLevel);

        /**
         * Count SLA-breached tickets.
         * 
         * @return Count of breached tickets
         */
        long countBySlaBreachedTrue();

        /**
         * Find tickets breached within a date range.
         * 
         * @param startTime Start of date range
         * @param endTime   End of date range
         * @return List of tickets
         */
        List<Ticket> findBySlaBreachTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

        /**
         * Find tickets by priority that have breached SLA.
         * 
         * @param priority Ticket priority
         * @return List of breached tickets for that priority
         */
        List<Ticket> findByPriorityAndSlaBreachedTrue(String priority);

        /**
         * Count SLA breaches by priority.
         * 
         * @param priority Ticket priority
         * @return Count of breached tickets
         */
        long countByPriorityAndSlaBreachedTrue(String priority);

        /**
         * Find tickets that need escalation level upgrade.
         * Tickets already breached but may need higher escalation.
         * 
         * @param excludedStatuses Statuses to exclude
         * @return List of tickets for re-escalation check
         */
        @Query("SELECT t FROM Ticket t WHERE t.slaBreached = true " +
                        "AND t.status NOT IN :excludedStatuses " +
                        "AND t.escalationLevel IS NOT NULL")
        List<Ticket> findTicketsForReEscalation(@Param("excludedStatuses") List<String> excludedStatuses);

        /**
         * Find tickets by team that have breached SLA.
         * 
         * @param assignedTeam Team name
         * @return List of breached tickets for that team
         */
        List<Ticket> findByAssignedTeamAndSlaBreachedTrue(String assignedTeam);

        // ============================================================
        // PHASE 5: SIMPLIFIED SLA QUERIES (NO PARAMETERS)
        // For easier use in services
        // ============================================================

        /**
         * Find all active tickets for SLA monitoring.
         * Active tickets are those not CLOSED or RESOLVED.
         * 
         * @return List of active tickets
         */
        @Query("SELECT t FROM Ticket t WHERE t.status NOT IN ('CLOSED', 'RESOLVED')")
        List<Ticket> findAllActiveTickets();

        /**
         * Find tickets approaching SLA deadline (simple version).
         * Uses current time and provided deadline threshold.
         * 
         * @param deadline The deadline threshold
         * @return List of tickets nearing SLA breach
         */
        @Query("SELECT t FROM Ticket t WHERE t.slaDeadline <= :deadline " +
                        "AND t.slaDeadline >= CURRENT_TIMESTAMP " +
                        "AND t.status NOT IN ('CLOSED', 'RESOLVED') " +
                        "AND (t.slaWarningSent = false OR t.slaWarningSent IS NULL)")
        List<Ticket> findTicketsApproachingSlaSimple(@Param("deadline") LocalDateTime deadline);
}
