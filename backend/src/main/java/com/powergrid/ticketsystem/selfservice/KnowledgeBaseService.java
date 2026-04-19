package com.powergrid.ticketsystem.selfservice;

import com.powergrid.ticketsystem.entity.KnowledgeBase;
import com.powergrid.ticketsystem.repository.KnowledgeBaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ============================================================
 * KNOWLEDGE BASE SERVICE
 * ============================================================
 * 
 * PHASE 4: SELF-SERVICE RESOLUTION
 * 
 * Business logic layer for knowledge base operations.
 * 
 * RESPONSIBILITIES:
 * - CRUD operations on knowledge base
 * - Solution lookup for tickets
 * - Success/usage tracking
 * - Statistics and analytics
 */
@Service
public class KnowledgeBaseService {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseService.class);

    private final KnowledgeBaseRepository knowledgeBaseRepository;

    public KnowledgeBaseService(KnowledgeBaseRepository knowledgeBaseRepository) {
        this.knowledgeBaseRepository = knowledgeBaseRepository;
    }

    // ============================================================
    // CRUD OPERATIONS
    // ============================================================

    /**
     * Create a new knowledge base entry.
     */
    public KnowledgeBase create(KnowledgeBase knowledgeBase) {
        logger.info("Creating knowledge base entry for issue type: {}", knowledgeBase.getIssueType());
        return knowledgeBaseRepository.save(knowledgeBase);
    }

    /**
     * Update an existing knowledge base entry.
     */
    public KnowledgeBase update(Long id, KnowledgeBase updated) {
        Optional<KnowledgeBase> existing = knowledgeBaseRepository.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Knowledge base entry not found: " + id);
        }

        KnowledgeBase kb = existing.get();
        kb.setIssueType(updated.getIssueType());
        kb.setIssueTitle(updated.getIssueTitle());
        kb.setSolutionSteps(updated.getSolutionSteps());
        kb.setAutoClosable(updated.getAutoClosable());
        kb.setKeywords(updated.getKeywords());
        kb.setCategory(updated.getCategory());
        kb.setPriorityRank(updated.getPriorityRank());
        kb.setIsActive(updated.getIsActive());

        logger.info("Updated knowledge base entry: {}", id);
        return knowledgeBaseRepository.save(kb);
    }

    /**
     * Delete (soft-delete by deactivating) a knowledge base entry.
     */
    public void delete(Long id) {
        Optional<KnowledgeBase> existing = knowledgeBaseRepository.findById(id);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Knowledge base entry not found: " + id);
        }

        KnowledgeBase kb = existing.get();
        kb.setIsActive(false);
        knowledgeBaseRepository.save(kb);
        logger.info("Deactivated knowledge base entry: {}", id);
    }

    /**
     * Get all active knowledge base entries.
     */
    public List<KnowledgeBase> getAllActive() {
        return knowledgeBaseRepository.findByIsActiveTrue();
    }

    /**
     * Get all knowledge base entries.
     */
    public List<KnowledgeBase> getAll() {
        return knowledgeBaseRepository.findAll();
    }

    /**
     * Get knowledge base entry by ID.
     */
    public Optional<KnowledgeBase> getById(Long id) {
        return knowledgeBaseRepository.findById(id);
    }

    // ============================================================
    // SOLUTION LOOKUP
    // ============================================================

    /**
     * Find solution for a given issue type (SubCategory).
     * Primary lookup method used by SelfServiceEngine.
     * 
     * @param issueType The issue type (e.g., VPN, PASSWORD, WIFI)
     * @return Optional containing the best matching solution
     */
    public Optional<KnowledgeBase> findSolutionByIssueType(String issueType) {
        logger.debug("Looking up solution for issue type: {}", issueType);

        List<KnowledgeBase> solutions = knowledgeBaseRepository.findActiveByIssueType(issueType);

        if (solutions.isEmpty()) {
            logger.debug("No solution found for issue type: {}", issueType);
            return Optional.empty();
        }

        // Return the highest priority solution
        return Optional.of(solutions.get(0));
    }

    /**
     * Find auto-closable solution for a given issue type.
     * Used to determine if automatic ticket closure is possible.
     * 
     * @param issueType The issue type
     * @return Optional containing auto-closable solution
     */
    public Optional<KnowledgeBase> findAutoClosableSolution(String issueType) {
        logger.debug("Looking up auto-closable solution for: {}", issueType);

        List<KnowledgeBase> solutions = knowledgeBaseRepository.findAutoClosableByIssueType(issueType);

        if (solutions.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(solutions.get(0));
    }

    /**
     * Search knowledge base by keyword.
     */
    public List<KnowledgeBase> searchByKeyword(String keyword) {
        return knowledgeBaseRepository.searchByKeyword(keyword);
    }

    /**
     * NEW METHOD: Find KB solution by matching keywords in issue description.
     * Used BEFORE classification to check if self-service is applicable.
     * 
     * @param issueDescription The raw issue description text
     * @return KnowledgeBase entry if match found, null otherwise
     */
    public KnowledgeBase findByKeywordMatch(String issueDescription) {
        if (issueDescription == null || issueDescription.trim().isEmpty()) {
            return null;
        }

        String lowerText = issueDescription.toLowerCase();
        logger.info("Searching KB by keyword match for: {}", lowerText);

        // Get all active KB entries
        List<KnowledgeBase> activeEntries = knowledgeBaseRepository.findByIsActiveTrue();

        KnowledgeBase bestMatch = null;
        int bestScore = 0;
        int bestKeywordMatches = 0;
        int bestTitleMatches = 0;
        boolean bestIssueTypeMatched = false;

        for (KnowledgeBase kb : activeEntries) {
            int score = 0;
            int keywordMatches = 0;
            int titleMatches = 0;
            boolean issueTypeMatched = false;

            // Check issue type match
            if (kb.getIssueType() != null && lowerText.contains(kb.getIssueType().toLowerCase())) {
                score += 10;
                issueTypeMatched = true;
            }

            // Check keywords match
            String keywords = kb.getKeywords();
            if (keywords != null && !keywords.isEmpty()) {
                String[] keywordArray = keywords.toLowerCase().split(",");
                for (String keyword : keywordArray) {
                    keyword = keyword.trim();
                    if (!keyword.isEmpty() && lowerText.contains(keyword)) {
                        score += 5;
                        keywordMatches++;
                    }
                }
            }

            // Check title match
            if (kb.getIssueTitle() != null) {
                String[] titleWords = kb.getIssueTitle().toLowerCase().split("\\s+");
                for (String word : titleWords) {
                    if (word.length() > 3 && lowerText.contains(word)) {
                        score += 2;
                        titleMatches++;
                    }
                }
            }

            logger.debug("KB {} ({}): score={}", kb.getId(), kb.getIssueType(), score);

            if (score > bestScore) {
                bestScore = score;
                bestMatch = kb;
                bestKeywordMatches = keywordMatches;
                bestTitleMatches = titleMatches;
                bestIssueTypeMatched = issueTypeMatched;
            }
        }

        // Minimum threshold for a match - SET TO 10
        // This allows direct issue type match (e.g., "VPN" → VPN KB) with minimum score
        // of 10
        // OR keyword-based matches with score >= 10
        if (bestScore >= 10) {
            logger.info("✓ KB match found: {} (score: {})", bestMatch.getIssueType(), bestScore);
            logger.info("  - Issue Type Matched: {}", bestIssueTypeMatched);
            logger.info("  - Keyword Matches: {}", bestKeywordMatches);
            logger.info("  - Title Matches: {}", bestTitleMatches);
            return bestMatch;
        }

        logger.info("✗ No KB match found (best score: {}, threshold: 10)", bestScore);
        return null;
    }

    /**
     * Full text search across knowledge base.
     */
    public List<KnowledgeBase> search(String searchTerm) {
        return knowledgeBaseRepository.fullTextSearch(searchTerm);
    }

    /**
     * Get solutions by category.
     */
    public List<KnowledgeBase> getByCategory(String category) {
        return knowledgeBaseRepository.findActiveByCategoryOrderedBySuccess(category);
    }

    /**
     * Check if solution exists for an issue type.
     */
    public boolean hasSolution(String issueType) {
        return knowledgeBaseRepository.existsActiveByIssueType(issueType);
    }

    // ============================================================
    // USAGE TRACKING
    // ============================================================

    /**
     * Record that a solution was used (delivered to user).
     */
    public void recordUsage(Long knowledgeBaseId) {
        Optional<KnowledgeBase> kb = knowledgeBaseRepository.findById(knowledgeBaseId);
        if (kb.isPresent()) {
            kb.get().incrementUsageCount();
            knowledgeBaseRepository.save(kb.get());
            logger.debug("Recorded usage for KB entry: {}", knowledgeBaseId);
        }
    }

    /**
     * Record that a solution was successful (user confirmed).
     */
    public void recordSuccess(Long knowledgeBaseId) {
        Optional<KnowledgeBase> kb = knowledgeBaseRepository.findById(knowledgeBaseId);
        if (kb.isPresent()) {
            kb.get().incrementSuccessCount();
            knowledgeBaseRepository.save(kb.get());
            logger.info("Recorded success for KB entry: {}", knowledgeBaseId);
        }
    }

    // ============================================================
    // STATISTICS
    // ============================================================

    /**
     * Get knowledge base statistics.
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalEntries", knowledgeBaseRepository.count());
        stats.put("activeEntries", knowledgeBaseRepository.countActiveEntries());
        stats.put("autoClosableEntries", knowledgeBaseRepository.countAutoClosableEntries());
        stats.put("topPerforming", knowledgeBaseRepository.findTopPerformingSolutions().stream()
                .limit(5).toList());
        stats.put("mostUsed", knowledgeBaseRepository.findMostUsedSolutions().stream()
                .limit(5).toList());

        return stats;
    }

    /**
     * Get all auto-closable solutions.
     */
    public List<KnowledgeBase> getAutoClosableSolutions() {
        return knowledgeBaseRepository.findActiveAutoClosable();
    }
}
