package com.powergrid.ticketsystem.nlp;

import com.powergrid.ticketsystem.constants.Category;
import com.powergrid.ticketsystem.constants.Priority;
import com.powergrid.ticketsystem.constants.SubCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ============================================================
 * PRIORITY ENGINE SERVICE
 * ============================================================
 * 
 * Phase 3: AI-Based Priority Setting
 * 
 * PURPOSE:
 * Automatically determines ticket priority based on:
 * 1. Category of the issue
 * 2. Sub-category specifics
 * 3. Urgency keywords in description
 * 4. Impact scope indicators
 * 
 * PRIORITY RULES:
 * ─────────────────────────────────────────────────────────────
 * | Condition | Priority |
 * ─────────────────────────────────────────────────────────────
 * | Critical keywords (outage, breach) | CRITICAL |
 * | VPN/Network issues | HIGH |
 * | Login/Authentication issues | HIGH |
 * | Urgent keywords present | HIGH |
 * | Hardware failures | MEDIUM |
 * | Software crashes | MEDIUM |
 * | Password reset | LOW |
 * | General requests | LOW |
 * ─────────────────────────────────────────────────────────────
 * 
 * WHY RULE-BASED PRIORITY (NOT ML):
 * - Priority rules are business-defined
 * - Rules are transparent and auditable
 * - Easy to modify based on organizational policy
 * - No training data required
 * - Consistent and predictable results
 */
@Service
public class PriorityEngine {

    private static final Logger logger = LoggerFactory.getLogger(PriorityEngine.class);

    private final TextPreprocessingService preprocessingService;
    private final KeywordDictionaryService dictionaryService;

    public PriorityEngine(TextPreprocessingService preprocessingService,
            KeywordDictionaryService dictionaryService) {
        this.preprocessingService = preprocessingService;
        this.dictionaryService = dictionaryService;
    }

    /**
     * Determines ticket priority based on category, sub-category, and text.
     * 
     * ALGORITHM:
     * 1. Check for critical keywords → CRITICAL
     * 2. Check for urgent keywords → bump up priority
     * 3. Apply category-based rules
     * 4. Apply sub-category modifiers
     * 5. Return final priority
     * 
     * @param rawText     Raw ticket description
     * @param category    Detected category
     * @param subCategory Detected sub-category
     * @return Calculated priority
     */
    public Priority determinePriority(String rawText, Category category, SubCategory subCategory) {
        logger.info("Determining priority for Category={}, SubCategory={}", category, subCategory);

        // Preprocess text for keyword matching
        List<String> tokens = preprocessingService.processForNLP(rawText);

        // Rule 1: Check for CRITICAL keywords
        if (dictionaryService.containsCriticalKeywords(tokens)) {
            logger.info("Critical keywords detected. Priority = CRITICAL");
            return Priority.CRITICAL;
        }

        // Rule 2: Check for urgent keywords (affects final priority)
        boolean hasUrgentKeywords = dictionaryService.containsUrgentKeywords(tokens);
        if (hasUrgentKeywords) {
            logger.debug("Urgent keywords detected - will increase priority");
        }

        // Rule 3: Apply category-based priority rules
        Priority basePriority = getCategoryBasedPriority(category, subCategory);

        // Rule 4: Bump up priority if urgent keywords present
        Priority finalPriority = basePriority;
        if (hasUrgentKeywords && basePriority != Priority.CRITICAL) {
            finalPriority = increasePriority(basePriority);
            logger.debug("Priority bumped from {} to {} due to urgent keywords",
                    basePriority, finalPriority);
        }

        logger.info("Final priority: {}", finalPriority);
        return finalPriority;
    }

    /**
     * Gets base priority based on category and sub-category.
     * 
     * BUSINESS RULES:
     * - Network issues (especially VPN) = HIGH (blocks work)
     * - Login issues = HIGH (user cannot work)
     * - Hardware failures = MEDIUM
     * - Software issues = MEDIUM
     * - Password resets = LOW (common, quick fix)
     * - General requests = LOW
     */
    private Priority getCategoryBasedPriority(Category category, SubCategory subCategory) {

        // ----- NETWORK CATEGORY -----
        if (category == Category.NETWORK) {
            switch (subCategory) {
                case VPN:
                    // VPN issues block remote work
                    return Priority.HIGH;
                case INTERNET:
                case LAN:
                    // Connectivity issues are urgent
                    return Priority.HIGH;
                case WIFI:
                    // WiFi might have alternatives (LAN)
                    return Priority.MEDIUM;
                case FIREWALL:
                    // Firewall issues can be security-related
                    return Priority.HIGH;
                default:
                    return Priority.MEDIUM;
            }
        }

        // ----- SOFTWARE CATEGORY -----
        if (category == Category.SOFTWARE) {
            switch (subCategory) {
                case LOGIN:
                    // Cannot login = cannot work
                    return Priority.HIGH;
                case CRASH:
                    // Application crash affects productivity
                    return Priority.MEDIUM;
                case INSTALLATION:
                    // Can usually wait
                    return Priority.LOW;
                case UPDATE:
                    // Updates can be scheduled
                    return Priority.LOW;
                case LICENSE:
                    // License issues can block work
                    return Priority.MEDIUM;
                case PERFORMANCE:
                    // Slow but still working
                    return Priority.MEDIUM;
                default:
                    return Priority.MEDIUM;
            }
        }

        // ----- HARDWARE CATEGORY -----
        if (category == Category.HARDWARE) {
            switch (subCategory) {
                case LAPTOP:
                case DESKTOP:
                    // Main device issues are serious
                    return Priority.HIGH;
                case MONITOR:
                    // Cannot see = cannot work
                    return Priority.HIGH;
                case KEYBOARD:
                case MOUSE:
                    // Input device issues
                    return Priority.MEDIUM;
                case PRINTER:
                    // Printing can often wait
                    return Priority.LOW;
                case PERIPHERAL:
                    // Usually non-critical
                    return Priority.LOW;
                default:
                    return Priority.MEDIUM;
            }
        }

        // ----- ACCESS CATEGORY -----
        if (category == Category.ACCESS) {
            switch (subCategory) {
                case PASSWORD:
                    // Quick fix, common request
                    return Priority.LOW;
                case ACCOUNT_UNLOCK:
                    // User locked out
                    return Priority.MEDIUM;
                case PERMISSION:
                    // May block specific task
                    return Priority.MEDIUM;
                case NEW_ACCOUNT:
                    // Can be planned
                    return Priority.LOW;
                default:
                    return Priority.LOW;
            }
        }

        // ----- EMAIL CATEGORY -----
        if (category == Category.EMAIL) {
            switch (subCategory) {
                case OUTLOOK:
                    // Main email client
                    return Priority.MEDIUM;
                case EMAIL_SEND:
                case EMAIL_RECEIVE:
                    // Email flow issues
                    return Priority.MEDIUM;
                case CALENDAR:
                    // Calendar/meeting issues
                    return Priority.MEDIUM;
                default:
                    return Priority.MEDIUM;
            }
        }

        // ----- UNKNOWN CATEGORY -----
        // Default to MEDIUM for manual review
        return Priority.MEDIUM;
    }

    /**
     * Increases priority by one level.
     */
    private Priority increasePriority(Priority current) {
        switch (current) {
            case LOW:
                return Priority.MEDIUM;
            case MEDIUM:
                return Priority.HIGH;
            case HIGH:
                return Priority.CRITICAL;
            case CRITICAL:
                return Priority.CRITICAL;
            default:
                return current;
        }
    }

    /**
     * Simple priority determination with just raw text.
     * Combines intent detection and priority calculation.
     */
    public Priority determinePriority(String rawText, IntentDetectionService intentService) {
        IntentDetectionService.IntentResult intent = intentService.detectFullIntent(rawText);
        return determinePriority(rawText, intent.getCategory(), intent.getSubCategory());
    }
}
