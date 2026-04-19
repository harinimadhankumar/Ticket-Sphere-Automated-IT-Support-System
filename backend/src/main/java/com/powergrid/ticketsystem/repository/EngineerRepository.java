package com.powergrid.ticketsystem.repository;

import com.powergrid.ticketsystem.entity.Engineer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * ENGINEER REPOSITORY
 * ============================================================
 * 
 * PHASE 6: ENGINEER RESOLUTION WORKFLOW
 * 
 * Repository for Engineer entity operations.
 * Provides methods for authentication, lookup, and workload management.
 */
@Repository
public interface EngineerRepository extends JpaRepository<Engineer, Long> {

        // ============================================================
        // AUTHENTICATION QUERIES
        // ============================================================

        /**
         * Find engineer by email (for login).
         * 
         * @param email Engineer's email
         * @return Optional engineer
         */
        Optional<Engineer> findByEmail(String email);

        /**
         * Find engineer by email and password (simple authentication).
         * NOTE: In production, password should be hashed and verified separately.
         * Uses findFirst to handle multiple engineers with same email (for testing).
         * 
         * @param email    Engineer's email
         * @param password Engineer's password
         * @return Optional engineer (first match)
         */
        Optional<Engineer> findFirstByEmailAndPassword(String email, String password);

        /**
         * Find engineer by name and password (username-based login).
         * This allows each engineer to login with their unique name.
         * 
         * @param name     Engineer's name (username)
         * @param password Engineer's password
         * @return Optional engineer
         */
        Optional<Engineer> findByNameAndPassword(String name, String password);

        /**
         * Find active engineer by email.
         * 
         * @param email Engineer's email
         * @return Optional engineer
         */
        @Query("SELECT e FROM Engineer e WHERE e.email = :email AND e.isActive = true")
        Optional<Engineer> findActiveByEmail(@Param("email") String email);

        // ============================================================
        // LOOKUP QUERIES
        // ============================================================

        /**
         * Find engineer by engineerId.
         * 
         * @param engineerId Engineer's unique ID
         * @return Optional engineer
         */
        Optional<Engineer> findByEngineerId(String engineerId);

        /**
         * Find engineer by name (exact match).
         * 
         * @param name Engineer's name
         * @return Optional engineer
         */
        Optional<Engineer> findByName(String name);

        /**
         * Find engineers by team.
         * 
         * @param team Team name
         * @return List of engineers in the team
         */
        List<Engineer> findByTeam(String team);

        /**
         * Find engineers by status.
         * 
         * @param status Engineer status
         * @return List of engineers with that status
         */
        List<Engineer> findByStatus(String status);

        /**
         * Find all active engineers.
         * 
         * @return List of active engineers
         */
        List<Engineer> findByIsActiveTrue();

        /**
         * Find available engineers in a team.
         * 
         * @param team   Team name
         * @param status Status (AVAILABLE)
         * @return List of available engineers
         */
        List<Engineer> findByTeamAndStatus(String team, String status);

        // ============================================================
        // WORKLOAD QUERIES
        // ============================================================

        /**
         * Find engineers with available capacity.
         * 
         * @param team Team name
         * @return List of engineers with workload less than max
         */
        @Query("SELECT e FROM Engineer e WHERE e.team = :team " +
                        "AND e.currentWorkload < e.maxWorkload " +
                        "AND e.status = 'AVAILABLE' " +
                        "AND e.isActive = true " +
                        "ORDER BY e.currentWorkload ASC")
        List<Engineer> findAvailableEngineersInTeam(@Param("team") String team);

        /**
         * Find engineer with lowest workload in team.
         * 
         * @param team Team name
         * @return Engineer with lowest workload
         */
        @Query("SELECT e FROM Engineer e WHERE e.team = :team " +
                        "AND e.isActive = true " +
                        "ORDER BY e.currentWorkload ASC")
        List<Engineer> findByTeamOrderByCurrentWorkloadAsc(@Param("team") String team);

        /**
         * Find engineers by skill.
         * 
         * @param skill Skill to search for
         * @return List of engineers with that skill
         */
        @Query("SELECT e FROM Engineer e WHERE e.skills LIKE %:skill% AND e.isActive = true")
        List<Engineer> findBySkillContaining(@Param("skill") String skill);

        // ============================================================
        // STATISTICS QUERIES
        // ============================================================

        /**
         * Count engineers by team.
         * 
         * @param team Team name
         * @return Count of engineers
         */
        long countByTeam(String team);

        /**
         * Count active engineers.
         * 
         * @return Count of active engineers
         */
        long countByIsActiveTrue();

        /**
         * Count engineers by status.
         * 
         * @param status Engineer status
         * @return Count
         */
        long countByStatus(String status);

        /**
         * Get total tickets resolved by all engineers.
         * 
         * @return Sum of tickets resolved
         */
        @Query("SELECT COALESCE(SUM(e.ticketsResolved), 0) FROM Engineer e")
        long getTotalTicketsResolved();

        /**
         * Get average performance score.
         * 
         * @return Average performance score
         */
        @Query("SELECT COALESCE(AVG(e.performanceScore), 0) FROM Engineer e WHERE e.isActive = true")
        double getAveragePerformanceScore();
}
