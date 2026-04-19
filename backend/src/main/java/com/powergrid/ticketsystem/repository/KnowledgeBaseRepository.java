package com.powergrid.ticketsystem.repository;

import com.powergrid.ticketsystem.entity.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * KNOWLEDGE BASE REPOSITORY - Data Access Layer
 * ============================================================
 * 
 * PHASE 4: SELF-SERVICE RESOLUTION
 * 
 * Spring Data JPA Repository for KnowledgeBase entity.
 * Provides methods to query the knowledge base for solutions.
 * 
 * KEY METHODS:
 * - findByIssueType: Primary lookup for matching solutions
 * - findByCategory: Filter by issue category
 * - findActiveByIssueType: Only active solutions
 * - searchByKeywords: Flexible keyword matching
 */
@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {

    // ============================================================
    // FIND BY ISSUE TYPE (Primary Lookup)
    // ============================================================

    /**
     * Find knowledge base entry by exact issue type match.
     * This is the primary lookup method for self-service.
     * 
     * @param issueType The issue type (matches SubCategory enum values)
     * @return Optional containing the knowledge base entry if found
     */
    Optional<KnowledgeBase> findByIssueTypeIgnoreCase(String issueType);

    /**
     * Find all entries for an issue type (may have multiple solutions).
     * 
     * @param issueType The issue type
     * @return List of matching entries ordered by priority
     */
    List<KnowledgeBase> findByIssueTypeIgnoreCaseOrderByPriorityRankDesc(String issueType);

    /**
     * Find active entry by issue type.
     * Only returns entries where isActive = true.
     * 
     * @param issueType The issue type
     * @return Optional containing the active knowledge base entry
     */
    @Query("SELECT k FROM KnowledgeBase k WHERE UPPER(k.issueType) = UPPER(:issueType) " +
            "AND k.isActive = true ORDER BY k.priorityRank DESC")
    List<KnowledgeBase> findActiveByIssueType(@Param("issueType") String issueType);

    /**
     * Find active and auto-closable entry by issue type.
     * Used to check if automatic resolution is possible.
     * 
     * @param issueType The issue type
     * @return Optional containing the matching entry
     */
    @Query("SELECT k FROM KnowledgeBase k WHERE UPPER(k.issueType) = UPPER(:issueType) " +
            "AND k.isActive = true AND k.autoClosable = true ORDER BY k.priorityRank DESC")
    List<KnowledgeBase> findAutoClosableByIssueType(@Param("issueType") String issueType);

    // ============================================================
    // FIND BY CATEGORY
    // ============================================================

    /**
     * Find all knowledge base entries for a category.
     * 
     * @param category The category (NETWORK, SOFTWARE, etc.)
     * @return List of entries in that category
     */
    List<KnowledgeBase> findByCategoryIgnoreCase(String category);

    /**
     * Find active entries by category.
     * 
     * @param category The category
     * @return List of active entries
     */
    @Query("SELECT k FROM KnowledgeBase k WHERE UPPER(k.category) = UPPER(:category) " +
            "AND k.isActive = true ORDER BY k.priorityRank DESC, k.successRate DESC")
    List<KnowledgeBase> findActiveByCategoryOrderedBySuccess(@Param("category") String category);

    // ============================================================
    // KEYWORD SEARCH
    // ============================================================

    /**
     * Search knowledge base by keyword in title or keywords field.
     * Case-insensitive search.
     * 
     * @param keyword The search keyword
     * @return List of matching entries
     */
    @Query("SELECT k FROM KnowledgeBase k WHERE " +
            "(LOWER(k.issueTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(k.keywords) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND k.isActive = true ORDER BY k.priorityRank DESC")
    List<KnowledgeBase> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Full text search across multiple fields.
     * 
     * @param searchTerm The search term
     * @return List of matching entries
     */
    @Query("SELECT k FROM KnowledgeBase k WHERE " +
            "(LOWER(k.issueTitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(k.issueType) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(k.keywords) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(k.solutionSteps) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND k.isActive = true ORDER BY k.priorityRank DESC, k.successRate DESC")
    List<KnowledgeBase> fullTextSearch(@Param("searchTerm") String searchTerm);

    // ============================================================
    // AUTO-CLOSABLE QUERIES
    // ============================================================

    /**
     * Find all auto-closable entries.
     * 
     * @return List of entries that can trigger automatic closure
     */
    List<KnowledgeBase> findByAutoClosableTrue();

    /**
     * Find all active auto-closable entries.
     * 
     * @return List of active auto-closable entries
     */
    @Query("SELECT k FROM KnowledgeBase k WHERE k.autoClosable = true AND k.isActive = true")
    List<KnowledgeBase> findActiveAutoClosable();

    // ============================================================
    // STATISTICS AND ANALYTICS
    // ============================================================

    /**
     * Find top performing solutions by success rate.
     * 
     * @return List of entries ordered by success rate
     */
    @Query("SELECT k FROM KnowledgeBase k WHERE k.isActive = true AND k.usageCount > 0 " +
            "ORDER BY k.successRate DESC")
    List<KnowledgeBase> findTopPerformingSolutions();

    /**
     * Find most used solutions.
     * 
     * @return List of entries ordered by usage count
     */
    @Query("SELECT k FROM KnowledgeBase k WHERE k.isActive = true " +
            "ORDER BY k.usageCount DESC")
    List<KnowledgeBase> findMostUsedSolutions();

    /**
     * Count active entries.
     * 
     * @return Number of active knowledge base entries
     */
    @Query("SELECT COUNT(k) FROM KnowledgeBase k WHERE k.isActive = true")
    Long countActiveEntries();

    /**
     * Count auto-closable entries.
     * 
     * @return Number of auto-closable entries
     */
    @Query("SELECT COUNT(k) FROM KnowledgeBase k WHERE k.autoClosable = true AND k.isActive = true")
    Long countAutoClosableEntries();

    // ============================================================
    // FIND ALL ACTIVE
    // ============================================================

    /**
     * Find all active knowledge base entries.
     * 
     * @return List of all active entries
     */
    List<KnowledgeBase> findByIsActiveTrue();

    /**
     * Check if solution exists for an issue type.
     * 
     * @param issueType The issue type to check
     * @return true if solution exists
     */
    @Query("SELECT CASE WHEN COUNT(k) > 0 THEN true ELSE false END FROM KnowledgeBase k " +
            "WHERE UPPER(k.issueType) = UPPER(:issueType) AND k.isActive = true")
    boolean existsActiveByIssueType(@Param("issueType") String issueType);
}
