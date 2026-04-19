package com.powergrid.ticketsystem.nlp;

import com.powergrid.ticketsystem.constants.Category;
import com.powergrid.ticketsystem.constants.SubCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * ============================================================
 * INTENT DETECTION SERVICE
 * ============================================================
 * 
 * Phase 2: NLP-Based Issue Understanding
 * 
 * PURPOSE:
 * Detects the intent (category) of a ticket based on keywords.
 * 
 * HOW IT WORKS:
 * 1. Receives preprocessed tokens from TextPreprocessingService
 * 2. Matches tokens against keyword dictionaries
 * 3. Counts matches for each category
 * 4. Returns category with highest match count
 * 
 * EXAMPLE:
 * Input: "VPN not connecting from home"
 * Tokens: ["vpn", "connecting", "home"]
 * Matches: NETWORK=2 (vpn, connecting), SOFTWARE=0, HARDWARE=0
 * Result: Intent = NETWORK
 * 
 * WHY RULE-BASED INTENT DETECTION:
 * ─────────────────────────────────────────────────────────────
 * Traditional ML Approach:
 * - Requires thousands of labeled examples
 * - Needs model training and evaluation
 * - Black-box decisions (hard to explain)
 * - Requires GPU/compute resources
 * 
 * Our Rule-Based Approach:
 * - Uses domain-specific keyword dictionaries
 * - Transparent matching logic
 * - Easy to explain in viva/audit
 * - Instant processing
 * - No training data needed
 * - Easy to update and maintain
 * ─────────────────────────────────────────────────────────────
 */
@Service
public class IntentDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(IntentDetectionService.class);

    private final TextPreprocessingService preprocessingService;
    private final KeywordDictionaryService dictionaryService;

    public IntentDetectionService(TextPreprocessingService preprocessingService,
            KeywordDictionaryService dictionaryService) {
        this.preprocessingService = preprocessingService;
        this.dictionaryService = dictionaryService;
    }

    /**
     * Detects the intent (category) from raw ticket text.
     * 
     * ALGORITHM:
     * 1. Preprocess text (lowercase, remove punctuation)
     * 2. Tokenize into words
     * 3. Remove stop words
     * 4. Match tokens against category keywords
     * 5. Return category with most matches
     * 
     * @param rawText Raw ticket description
     * @return Detected category (intent)
     */
    public Category detectIntent(String rawText) {
        logger.info("Detecting intent for text: {}",
                rawText.length() > 50 ? rawText.substring(0, 50) + "..." : rawText);

        // Step 1-3: Preprocess and tokenize
        List<String> tokens = preprocessingService.processForNLP(rawText);

        if (tokens.isEmpty()) {
            logger.warn("No tokens found after preprocessing. Returning UNKNOWN.");
            return Category.UNKNOWN;
        }

        // Step 4: Find matching categories
        Map<Category, Integer> matchCounts = dictionaryService.findMatchingCategories(tokens);

        if (matchCounts.isEmpty()) {
            logger.info("No category matches found. Returning UNKNOWN.");
            return Category.UNKNOWN;
        }

        // Step 5: Find category with highest match count
        Category bestMatch = Category.UNKNOWN;
        int maxCount = 0;

        for (Map.Entry<Category, Integer> entry : matchCounts.entrySet()) {
            logger.debug("Category {} has {} matches", entry.getKey(), entry.getValue());
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                bestMatch = entry.getKey();
            }
        }

        logger.info("Intent detected: {} (with {} keyword matches)", bestMatch, maxCount);

        return bestMatch;
    }

    /**
     * Detects sub-category based on category and text.
     * 
     * @param rawText  Raw ticket description
     * @param category Already detected category
     * @return Detected sub-category
     */
    public SubCategory detectSubCategory(String rawText, Category category) {
        logger.info("Detecting sub-category for category: {}", category);

        List<String> tokens = preprocessingService.processForNLP(rawText);

        if (tokens.isEmpty()) {
            return getDefaultSubCategory(category);
        }

        // Find matching sub-categories within the category
        Map<SubCategory, Integer> matchCounts = dictionaryService.findMatchingSubCategories(tokens, category);

        if (matchCounts.isEmpty()) {
            logger.info("No sub-category matches. Using default for {}", category);
            return getDefaultSubCategory(category);
        }

        // Find sub-category with highest match count
        SubCategory bestMatch = getDefaultSubCategory(category);
        int maxCount = 0;

        for (Map.Entry<SubCategory, Integer> entry : matchCounts.entrySet()) {
            logger.debug("SubCategory {} has {} matches", entry.getKey(), entry.getValue());
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                bestMatch = entry.getKey();
            }
        }

        logger.info("Sub-category detected: {} (with {} keyword matches)", bestMatch, maxCount);

        return bestMatch;
    }

    /**
     * Complete intent detection: Category + SubCategory.
     * 
     * @param rawText Raw ticket description
     * @return IntentResult containing category and sub-category
     */
    public IntentResult detectFullIntent(String rawText) {
        logger.info("Starting full intent detection");

        Category category = detectIntent(rawText);
        SubCategory subCategory = detectSubCategory(rawText, category);

        IntentResult result = new IntentResult(category, subCategory);

        logger.info("Full intent: Category={}, SubCategory={}",
                result.getCategory(), result.getSubCategory());

        return result;
    }

    /**
     * Returns the default sub-category for a category.
     */
    private SubCategory getDefaultSubCategory(Category category) {
        switch (category) {
            case NETWORK:
                return SubCategory.INTERNET;
            case SOFTWARE:
                return SubCategory.PERFORMANCE;
            case HARDWARE:
                return SubCategory.LAPTOP;
            case ACCESS:
                return SubCategory.PASSWORD;
            case EMAIL:
                return SubCategory.OUTLOOK;
            default:
                return SubCategory.GENERAL;
        }
    }

    // ============================================================
    // INTENT RESULT CLASS
    // ============================================================

    /**
     * Holds the result of intent detection.
     */
    public static class IntentResult {
        private final Category category;
        private final SubCategory subCategory;

        public IntentResult(Category category, SubCategory subCategory) {
            this.category = category;
            this.subCategory = subCategory;
        }

        public Category getCategory() {
            return category;
        }

        public SubCategory getSubCategory() {
            return subCategory;
        }

        @Override
        public String toString() {
            return "IntentResult{category=" + category + ", subCategory=" + subCategory + "}";
        }
    }
}
