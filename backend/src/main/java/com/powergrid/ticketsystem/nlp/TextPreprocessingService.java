package com.powergrid.ticketsystem.nlp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * ============================================================
 * TEXT PRE-PROCESSING SERVICE
 * ============================================================
 * 
 * Phase 2: NLP-Based Issue Understanding
 * 
 * PURPOSE:
 * Prepares raw ticket text for keyword extraction and intent detection.
 * 
 * PRE-PROCESSING STEPS:
 * 1. Convert to lowercase
 * 2. Remove special characters and punctuation
 * 3. Remove extra whitespaces
 * 4. Tokenize into words
 * 5. Remove stop words (optional)
 * 
 * WHY RULE-BASED (NOT ML):
 * ─────────────────────────────────────────────────────────────
 * | Aspect | Rule-Based | Machine Learning |
 * ─────────────────────────────────────────────────────────────
 * | Training Data | Not required | Large dataset needed|
 * | Explainability | Fully transparent | Black box |
 * | Maintenance | Easy to update | Needs retraining |
 * | Performance | Instant | Depends on model |
 * | IT Domain | Well-defined | Overkill |
 * | Viva-friendly | Easy to explain | Complex |
 * ─────────────────────────────────────────────────────────────
 * 
 * IT issues use predictable vocabulary, making rule-based
 * NLP more practical and maintainable for this use case.
 */
@Service
public class TextPreprocessingService {

    private static final Logger logger = LoggerFactory.getLogger(TextPreprocessingService.class);

    // Pattern to match non-alphanumeric characters (except spaces)
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile("[^a-zA-Z0-9\\s]");

    // Pattern to match multiple spaces
    private static final Pattern MULTIPLE_SPACES_PATTERN = Pattern.compile("\\s+");

    // Common stop words to filter out (not useful for intent detection)
    private static final List<String> STOP_WORDS = List.of(
            "a", "an", "the", "is", "are", "was", "were", "be", "been", "being",
            "have", "has", "had", "do", "does", "did", "will", "would", "could",
            "should", "may", "might", "must", "shall", "can", "need", "dare",
            "i", "me", "my", "myself", "we", "our", "ours", "ourselves",
            "you", "your", "yours", "yourself", "yourselves",
            "he", "him", "his", "himself", "she", "her", "hers", "herself",
            "it", "its", "itself", "they", "them", "their", "theirs", "themselves",
            "what", "which", "who", "whom", "this", "that", "these", "those",
            "am", "is", "are", "was", "were", "be", "been", "being",
            "have", "has", "had", "having", "do", "does", "did", "doing",
            "and", "but", "if", "or", "because", "as", "until", "while",
            "of", "at", "by", "for", "with", "about", "against", "between",
            "into", "through", "during", "before", "after", "above", "below",
            "to", "from", "up", "down", "in", "out", "on", "off", "over", "under",
            "again", "further", "then", "once", "here", "there", "when", "where",
            "why", "how", "all", "each", "few", "more", "most", "other", "some",
            "such", "no", "nor", "not", "only", "own", "same", "so", "than",
            "too", "very", "just", "also", "now", "please", "hi", "hello", "thanks");

    /**
     * Main method to preprocess text for NLP analysis.
     * 
     * @param rawText Raw ticket description text
     * @return Preprocessed text (lowercase, cleaned)
     */
    public String preprocess(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            logger.warn("Empty text received for preprocessing");
            return "";
        }

        logger.debug("Preprocessing text: {}", rawText);

        // Step 1: Convert to lowercase
        String processed = rawText.toLowerCase();

        // Step 2: Remove special characters and punctuation
        processed = SPECIAL_CHARS_PATTERN.matcher(processed).replaceAll(" ");

        // Step 3: Remove extra whitespaces
        processed = MULTIPLE_SPACES_PATTERN.matcher(processed).replaceAll(" ").trim();

        logger.debug("Preprocessed result: {}", processed);

        return processed;
    }

    /**
     * Tokenizes preprocessed text into individual words.
     * 
     * TOKENIZATION:
     * - Splits text by whitespace
     * - Each word becomes a token
     * - Tokens are used for keyword matching
     * 
     * @param preprocessedText Text after preprocessing
     * @return List of tokens (words)
     */
    public List<String> tokenize(String preprocessedText) {
        if (preprocessedText == null || preprocessedText.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String[] words = preprocessedText.split("\\s+");
        List<String> tokens = new ArrayList<>();

        for (String word : words) {
            if (!word.isEmpty()) {
                tokens.add(word);
            }
        }

        logger.debug("Tokenized into {} tokens: {}", tokens.size(), tokens);

        return tokens;
    }

    /**
     * Tokenizes text and removes stop words.
     * 
     * STOP WORDS:
     * Common words that don't contribute to meaning.
     * Examples: "the", "is", "at", "which", "on"
     * 
     * @param preprocessedText Text after preprocessing
     * @return List of meaningful tokens (without stop words)
     */
    public List<String> tokenizeAndRemoveStopWords(String preprocessedText) {
        List<String> tokens = tokenize(preprocessedText);
        List<String> filteredTokens = new ArrayList<>();

        for (String token : tokens) {
            if (!STOP_WORDS.contains(token)) {
                filteredTokens.add(token);
            }
        }

        logger.debug("After stop word removal: {} tokens: {}",
                filteredTokens.size(), filteredTokens);

        return filteredTokens;
    }

    /**
     * Complete preprocessing pipeline:
     * 1. Preprocess (lowercase, clean)
     * 2. Tokenize
     * 3. Remove stop words
     * 
     * @param rawText Raw ticket description
     * @return List of meaningful tokens ready for NLP
     */
    public List<String> processForNLP(String rawText) {
        logger.info("Starting NLP preprocessing pipeline");

        String preprocessed = preprocess(rawText);
        List<String> tokens = tokenizeAndRemoveStopWords(preprocessed);

        logger.info("NLP preprocessing complete. Tokens: {}", tokens);

        return tokens;
    }
}
