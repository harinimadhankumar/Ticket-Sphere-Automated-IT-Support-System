package com.powergrid.ticketsystem.analytics.repository;

import com.powergrid.ticketsystem.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ============================================================
 * ANALYTICS REPOSITORY
 * ============================================================
 * 
 * PHASE 9: REPORTS & ANALYTICS
 * 
 * PURPOSE:
 * ─────────
 * Specialized repository for analytics queries.
 * Contains custom JPQL queries optimized for reporting.
 * 
 * WHY SEPARATE REPOSITORY?
 * ────────────────────────
 * 1. Separation of concerns: Analytics queries are different from CRUD
 * 2. Complex queries: Analytics need aggregations, groupings
 * 3. Read-only focus: This repository only reads, never writes
 * 4. Performance: Analytics queries can be optimized separately
 * 
 * DESIGN PRINCIPLES:
 * ──────────────────
 * - Use native queries for complex aggregations
 * - Use projections for efficient data retrieval
 * - Time-based filtering is mandatory for all queries
 * - All queries are read-only
 */
@Repository
public interface AnalyticsRepository extends JpaRepository<Ticket, Long> {

    // ============================================================
    // SLA COMPLIANCE QUERIES
    // ============================================================

    /**
     * Count total tickets in a time period.
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.createdTime BETWEEN :startDate AND :endDate")
    long countTicketsInPeriod(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count tickets with SLA breached.
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.slaBreached = true AND t.createdTime BETWEEN :startDate AND :endDate")
    long countSlaBreachedTickets(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count tickets with SLA met (resolved and not breached).
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.slaBreached = false AND t.status IN ('RESOLVED', 'CLOSED') AND t.createdTime BETWEEN :startDate AND :endDate")
    long countSlaMetTickets(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count tickets at risk (open and approaching SLA deadline).
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status NOT IN ('RESOLVED', 'CLOSED') AND t.slaDeadline < :warningTime AND t.createdTime BETWEEN :startDate AND :endDate")
    long countTicketsAtRisk(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("warningTime") LocalDateTime warningTime);

    // ============================================================
    // CATEGORY VOLUME QUERIES
    // ============================================================

    /**
     * Get ticket count by category.
     * Returns Object[] with [category, count].
     */
    @Query("SELECT t.category, COUNT(t) FROM Ticket t WHERE t.createdTime BETWEEN :startDate AND :endDate AND t.category IS NOT NULL GROUP BY t.category ORDER BY COUNT(t) DESC")
    List<Object[]> countTicketsByCategory(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get ticket count by sub-category for a specific category.
     */
    @Query("SELECT t.subCategory, COUNT(t) FROM Ticket t WHERE t.category = :category AND t.createdTime BETWEEN :startDate AND :endDate AND t.subCategory IS NOT NULL GROUP BY t.subCategory ORDER BY COUNT(t) DESC")
    List<Object[]> countTicketsBySubCategory(@Param("category") String category,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // ============================================================
    // SLA BREACH ANALYSIS QUERIES
    // ============================================================

    /**
     * Get SLA breaches by category.
     * Returns Object[] with [category, breachCount, totalCount].
     */
    @Query("SELECT t.category, " +
            "SUM(CASE WHEN t.slaBreached = true THEN 1 ELSE 0 END), " +
            "COUNT(t) " +
            "FROM Ticket t " +
            "WHERE t.createdTime BETWEEN :startDate AND :endDate AND t.category IS NOT NULL " +
            "GROUP BY t.category " +
            "ORDER BY SUM(CASE WHEN t.slaBreached = true THEN 1 ELSE 0 END) DESC")
    List<Object[]> countSlaBreachesByCategory(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get SLA breaches by sub-category for a specific category.
     */
    @Query("SELECT t.subCategory, COUNT(t) FROM Ticket t WHERE t.category = :category AND t.slaBreached = true AND t.createdTime BETWEEN :startDate AND :endDate AND t.subCategory IS NOT NULL GROUP BY t.subCategory ORDER BY COUNT(t) DESC")
    List<Object[]> countSlaBreachesBySubCategory(@Param("category") String category,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // ============================================================
    // ENGINEER PERFORMANCE QUERIES
    // ============================================================

    /**
     * Get tickets assigned to each engineer.
     * Returns Object[] with [assignedEngineer, assignedCount].
     */
    @Query("SELECT t.assignedEngineer, COUNT(t) FROM Ticket t WHERE t.assignedEngineer IS NOT NULL AND t.createdTime BETWEEN :startDate AND :endDate GROUP BY t.assignedEngineer")
    List<Object[]> countTicketsByEngineer(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get resolved tickets by engineer.
     */
    @Query("SELECT t.assignedEngineer, COUNT(t) FROM Ticket t WHERE t.assignedEngineer IS NOT NULL AND t.status IN ('RESOLVED', 'CLOSED') AND t.createdTime BETWEEN :startDate AND :endDate GROUP BY t.assignedEngineer")
    List<Object[]> countResolvedTicketsByEngineer(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get SLA breaches by engineer.
     */
    @Query("SELECT t.assignedEngineer, COUNT(t) FROM Ticket t WHERE t.assignedEngineer IS NOT NULL AND t.slaBreached = true AND t.createdTime BETWEEN :startDate AND :endDate GROUP BY t.assignedEngineer")
    List<Object[]> countSlaBreachesByEngineer(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get tickets for a specific engineer.
     */
    @Query("SELECT t FROM Ticket t WHERE t.assignedEngineer = :engineerName AND t.createdTime BETWEEN :startDate AND :endDate")
    List<Ticket> findTicketsByEngineer(@Param("engineerName") String engineerName,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get current open tickets by engineer.
     */
    @Query("SELECT t.assignedEngineer, COUNT(t) FROM Ticket t WHERE t.assignedEngineer IS NOT NULL AND t.status NOT IN ('RESOLVED', 'CLOSED') GROUP BY t.assignedEngineer")
    List<Object[]> countOpenTicketsByEngineer();

    // ============================================================
    // PEAK HOURS QUERIES
    // ============================================================

    /**
     * Get ticket count by hour of day.
     * Returns Object[] with [hour, count].
     */
    @Query("SELECT FUNCTION('HOUR', t.createdTime), COUNT(t) FROM Ticket t WHERE t.createdTime BETWEEN :startDate AND :endDate GROUP BY FUNCTION('HOUR', t.createdTime) ORDER BY FUNCTION('HOUR', t.createdTime)")
    List<Object[]> countTicketsByHour(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get ticket count by day of week.
     * Returns Object[] with [dayOfWeek, count].
     */
    @Query("SELECT FUNCTION('DAYOFWEEK', t.createdTime), COUNT(t) FROM Ticket t WHERE t.createdTime BETWEEN :startDate AND :endDate GROUP BY FUNCTION('DAYOFWEEK', t.createdTime) ORDER BY FUNCTION('DAYOFWEEK', t.createdTime)")
    List<Object[]> countTicketsByDayOfWeek(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // ============================================================
    // TREND ANALYSIS QUERIES
    // ============================================================

    /**
     * Get ticket count by date.
     * Returns Object[] with [date, count].
     */
    @Query("SELECT FUNCTION('DATE', t.createdTime), COUNT(t) FROM Ticket t WHERE t.createdTime BETWEEN :startDate AND :endDate GROUP BY FUNCTION('DATE', t.createdTime) ORDER BY FUNCTION('DATE', t.createdTime)")
    List<Object[]> countTicketsByDate(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Get resolved tickets with resolution time.
     */
    @Query("SELECT t FROM Ticket t WHERE t.closedTime IS NOT NULL AND t.createdTime BETWEEN :startDate AND :endDate")
    List<Ticket> findResolvedTicketsWithTime(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // ============================================================
    // VERIFICATION QUALITY QUERIES
    // ============================================================

    /**
     * Get average verification score by engineer.
     */
    @Query("SELECT t.assignedEngineer, AVG(t.verificationScore) FROM Ticket t WHERE t.assignedEngineer IS NOT NULL AND t.verificationScore IS NOT NULL AND t.createdTime BETWEEN :startDate AND :endDate GROUP BY t.assignedEngineer")
    List<Object[]> getAverageVerificationScoreByEngineer(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count first-attempt verifications by engineer.
     */
    @Query("SELECT t.assignedEngineer, COUNT(t) FROM Ticket t WHERE t.assignedEngineer IS NOT NULL AND t.verificationAttempts = 1 AND t.verificationStatus = 'VERIFIED' AND t.createdTime BETWEEN :startDate AND :endDate GROUP BY t.assignedEngineer")
    List<Object[]> countFirstAttemptSuccessByEngineer(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // ============================================================
    // SOURCE ANALYSIS QUERIES
    // ============================================================

    /**
     * Get ticket count by source (EMAIL/CHATBOT).
     */
    @Query("SELECT t.source, COUNT(t) FROM Ticket t WHERE t.createdTime BETWEEN :startDate AND :endDate GROUP BY t.source")
    List<Object[]> countTicketsBySource(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // ============================================================
    // PRIORITY DISTRIBUTION QUERIES
    // ============================================================

    /**
     * Get ticket count by priority.
     */
    @Query("SELECT t.priority, COUNT(t) FROM Ticket t WHERE t.createdTime BETWEEN :startDate AND :endDate AND t.priority IS NOT NULL GROUP BY t.priority ORDER BY COUNT(t) DESC")
    List<Object[]> countTicketsByPriority(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
