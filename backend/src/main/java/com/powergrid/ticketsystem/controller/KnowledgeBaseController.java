package com.powergrid.ticketsystem.controller;

import com.powergrid.ticketsystem.entity.KnowledgeBase;
import com.powergrid.ticketsystem.selfservice.KnowledgeBaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ============================================================
 * KNOWLEDGE BASE CONTROLLER
 * ============================================================
 * 
 * PHASE 4: SELF-SERVICE RESOLUTION
 * 
 * REST API endpoints for managing the knowledge base.
 * 
 * ENDPOINTS:
 * ──────────
 * 
 * CRUD Operations:
 * POST /api/knowledge-base - Create new entry
 * GET /api/knowledge-base - Get all entries
 * GET /api/knowledge-base/{id} - Get entry by ID
 * PUT /api/knowledge-base/{id} - Update entry
 * DELETE /api/knowledge-base/{id} - Delete (deactivate) entry
 * 
 * Query Operations:
 * GET /api/knowledge-base/search - Search by keyword
 * GET /api/knowledge-base/category/{cat} - Get by category
 * GET /api/knowledge-base/issue/{type} - Get by issue type
 * GET /api/knowledge-base/auto-closable - Get auto-closable entries
 * 
 * Statistics:
 * GET /api/knowledge-base/stats - Get statistics
 */
@RestController
@RequestMapping("/api/knowledge-base")
public class KnowledgeBaseController {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseController.class);

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    // ============================================================
    // CREATE
    // ============================================================

    /**
     * Create a new knowledge base entry.
     * 
     * POST /api/knowledge-base
     * 
     * Request Body:
     * {
     * "issueType": "VPN",
     * "issueTitle": "VPN Connection Issues",
     * "solutionSteps": "[\"Step 1: ...\", \"Step 2: ...\"]",
     * "autoClosable": true,
     * "category": "NETWORK",
     * "keywords": "vpn,connection,cisco,anyconnect"
     * }
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody KnowledgeBase knowledgeBase) {
        logger.info("Creating knowledge base entry for issue type: {}", knowledgeBase.getIssueType());

        try {
            KnowledgeBase created = knowledgeBaseService.create(knowledgeBase);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Knowledge base entry created successfully");
            response.put("data", created);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error creating knowledge base entry", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ============================================================
    // READ
    // ============================================================

    /**
     * Get all knowledge base entries.
     * 
     * GET /api/knowledge-base
     * GET /api/knowledge-base?active=true (only active entries)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "true") boolean active) {

        List<KnowledgeBase> entries = active ? knowledgeBaseService.getAllActive() : knowledgeBaseService.getAll();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", entries.size());
        response.put("data", entries);

        return ResponseEntity.ok(response);
    }

    /**
     * Get knowledge base entry by ID.
     * 
     * GET /api/knowledge-base/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        Optional<KnowledgeBase> entry = knowledgeBaseService.getById(id);

        Map<String, Object> response = new HashMap<>();

        if (entry.isPresent()) {
            response.put("success", true);
            response.put("data", entry.get());
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Knowledge base entry not found: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // ============================================================
    // UPDATE
    // ============================================================

    /**
     * Update a knowledge base entry.
     * 
     * PUT /api/knowledge-base/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @RequestBody KnowledgeBase knowledgeBase) {

        logger.info("Updating knowledge base entry: {}", id);

        Map<String, Object> response = new HashMap<>();

        try {
            KnowledgeBase updated = knowledgeBaseService.update(id, knowledgeBase);
            response.put("success", true);
            response.put("message", "Knowledge base entry updated");
            response.put("data", updated);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            logger.error("Error updating knowledge base entry", e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ============================================================
    // DELETE
    // ============================================================

    /**
     * Delete (deactivate) a knowledge base entry.
     * 
     * DELETE /api/knowledge-base/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        logger.info("Deleting knowledge base entry: {}", id);

        Map<String, Object> response = new HashMap<>();

        try {
            knowledgeBaseService.delete(id);
            response.put("success", true);
            response.put("message", "Knowledge base entry deactivated");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            logger.error("Error deleting knowledge base entry", e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ============================================================
    // SEARCH / QUERY
    // ============================================================

    /**
     * Search knowledge base by keyword.
     * 
     * GET /api/knowledge-base/search?q=vpn
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam String q) {
        logger.info("Searching knowledge base for: {}", q);

        List<KnowledgeBase> results = knowledgeBaseService.search(q);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("query", q);
        response.put("count", results.size());
        response.put("data", results);

        return ResponseEntity.ok(response);
    }

    /**
     * Get knowledge base entries by category.
     * 
     * GET /api/knowledge-base/category/NETWORK
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Map<String, Object>> getByCategory(@PathVariable String category) {
        List<KnowledgeBase> entries = knowledgeBaseService.getByCategory(category);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("category", category);
        response.put("count", entries.size());
        response.put("data", entries);

        return ResponseEntity.ok(response);
    }

    /**
     * Get solution by issue type (SubCategory).
     * 
     * GET /api/knowledge-base/issue/VPN
     */
    @GetMapping("/issue/{issueType}")
    public ResponseEntity<Map<String, Object>> getByIssueType(@PathVariable String issueType) {
        Optional<KnowledgeBase> solution = knowledgeBaseService.findSolutionByIssueType(issueType);

        Map<String, Object> response = new HashMap<>();

        if (solution.isPresent()) {
            response.put("success", true);
            response.put("issueType", issueType);
            response.put("solutionAvailable", true);
            response.put("data", solution.get());
        } else {
            response.put("success", true);
            response.put("issueType", issueType);
            response.put("solutionAvailable", false);
            response.put("message", "No solution found for issue type: " + issueType);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get all auto-closable solutions.
     * 
     * GET /api/knowledge-base/auto-closable
     */
    @GetMapping("/auto-closable")
    public ResponseEntity<Map<String, Object>> getAutoClosable() {
        List<KnowledgeBase> entries = knowledgeBaseService.getAutoClosableSolutions();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", entries.size());
        response.put("data", entries);

        return ResponseEntity.ok(response);
    }

    /**
     * Check if solution exists for an issue type.
     * 
     * GET /api/knowledge-base/exists/VPN
     */
    @GetMapping("/exists/{issueType}")
    public ResponseEntity<Map<String, Object>> checkExists(@PathVariable String issueType) {
        boolean exists = knowledgeBaseService.hasSolution(issueType);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("issueType", issueType);
        response.put("solutionExists", exists);

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // STATISTICS
    // ============================================================

    /**
     * Get knowledge base statistics.
     * 
     * GET /api/knowledge-base/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = knowledgeBaseService.getStatistics();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", stats);

        return ResponseEntity.ok(response);
    }
}
