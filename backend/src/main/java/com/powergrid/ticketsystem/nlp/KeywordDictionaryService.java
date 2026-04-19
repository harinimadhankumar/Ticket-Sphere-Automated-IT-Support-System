package com.powergrid.ticketsystem.nlp;

import com.powergrid.ticketsystem.constants.Category;
import com.powergrid.ticketsystem.constants.SubCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * ============================================================
 * KEYWORD DICTIONARY SERVICE
 * ============================================================
 * 
 * Phase 2: NLP-Based Issue Understanding
 * 
 * PURPOSE:
 * Maintains keyword dictionaries for each category and sub-category.
 * Used by Intent Detection Service to identify issue type.
 * 
 * WHY KEYWORD DICTIONARIES:
 * - IT issues use predictable vocabulary
 * - Same issue described differently by users
 * - Keywords bridge the gap between user language and categories
 * 
 * EXAMPLES:
 * User says: "VPN not working" → Keywords: [vpn, working]
 * User says: "Cannot access remote network" → Keywords: [access, remote,
 * network]
 * Both map to: Category=NETWORK, SubCategory=VPN
 * 
 * MAINTENANCE:
 * - Add new keywords as new issues are discovered
 * - Keywords are case-insensitive (handled by preprocessing)
 * - No retraining needed (unlike ML models)
 */
@Service
public class KeywordDictionaryService {

    private static final Logger logger = LoggerFactory.getLogger(KeywordDictionaryService.class);

    // ============================================================
    // KEYWORD DICTIONARIES
    // Map: Category/SubCategory → List of Keywords
    // ============================================================

    private final Map<Category, Set<String>> categoryKeywords = new HashMap<>();
    private final Map<SubCategory, Set<String>> subCategoryKeywords = new HashMap<>();

    // Urgency keywords for priority detection
    private final Set<String> urgentKeywords = new HashSet<>();
    private final Set<String> criticalKeywords = new HashSet<>();

    /**
     * Initialize all keyword dictionaries on service startup.
     */
    @PostConstruct
    public void initializeDictionaries() {
        logger.info("Initializing keyword dictionaries for NLP");

        initializeCategoryKeywords();
        initializeSubCategoryKeywords();
        initializeUrgencyKeywords();

        logger.info("Keyword dictionaries initialized successfully");
        logDictionaryStats();
    }

    // ============================================================
    // CATEGORY KEYWORDS
    // ============================================================

    private void initializeCategoryKeywords() {

        // NETWORK keywords
        categoryKeywords.put(Category.NETWORK, new HashSet<>(Arrays.asList(
                "vpn", "network", "internet", "wifi", "wireless", "lan", "ethernet",
                "connection", "connectivity", "connected", "connecting", "disconnect",
                "remote", "access", "firewall", "router", "switch", "ping", "dns",
                "ip", "address", "proxy", "bandwidth", "slow", "speed", "latency",
                "timeout", "unreachable", "offline", "online", "intranet", "extranet")));

        // SOFTWARE keywords
        categoryKeywords.put(Category.SOFTWARE, new HashSet<>(Arrays.asList(
                "software", "application", "app", "program", "install", "installation",
                "uninstall", "update", "upgrade", "patch", "version", "license",
                "crash", "crashes", "crashing", "freeze", "freezing", "frozen",
                "hang", "hanging", "stuck", "slow", "performance", "error", "errors",
                "bug", "bugs", "issue", "problem", "not working", "failed", "failure",
                "login", "logout", "signin", "signout", "authenticate", "authentication",
                "windows", "office", "excel", "word", "powerpoint", "teams", "zoom",
                "chrome", "browser", "adobe", "pdf", "java", "python", "sap", "erp")));

        // HARDWARE keywords
        categoryKeywords.put(Category.HARDWARE, new HashSet<>(Arrays.asList(
                "hardware", "laptop", "computer", "pc", "desktop", "workstation",
                "keyboard", "mouse", "monitor", "screen", "display", "printer",
                "scanner", "projector", "webcam", "camera", "microphone", "headset",
                "speaker", "usb", "port", "cable", "charger", "battery", "power",
                "overheating", "hot", "fan", "noise", "beeping", "blue screen",
                "bsod", "boot", "startup", "shutdown", "restart", "reboot",
                "memory", "ram", "storage", "disk", "drive", "ssd", "hdd")));

        // ACCESS keywords
        categoryKeywords.put(Category.ACCESS, new HashSet<>(Arrays.asList(
                "password", "reset", "forgot", "forgotten", "expired", "expiring",
                "account", "locked", "unlock", "disabled", "blocked", "access",
                "permission", "permissions", "denied", "unauthorized", "authorize",
                "authentication", "mfa", "otp", "token", "credential", "credentials",
                "new account", "create account", "user", "username", "id", "badge")));

        // EMAIL keywords
        categoryKeywords.put(Category.EMAIL, new HashSet<>(Arrays.asList(
                "email", "mail", "outlook", "inbox", "outbox", "sent", "received",
                "attachment", "calendar", "meeting", "invite", "invitation",
                "spam", "junk", "phishing", "mailbox", "quota", "full", "storage",
                "sync", "syncing", "synchronization", "exchange", "smtp", "imap")));

        // UNKNOWN - no specific keywords
        categoryKeywords.put(Category.UNKNOWN, new HashSet<>());
    }

    // ============================================================
    // SUB-CATEGORY KEYWORDS
    // ============================================================

    private void initializeSubCategoryKeywords() {

        // ----- NETWORK SUB-CATEGORIES -----
        subCategoryKeywords.put(SubCategory.VPN, new HashSet<>(Arrays.asList(
                "vpn", "remote", "work from home", "wfh", "tunnel", "cisco",
                "anyconnect", "globalprotect", "pulse", "fortinet")));

        subCategoryKeywords.put(SubCategory.WIFI, new HashSet<>(Arrays.asList(
                "wifi", "wireless", "wi-fi", "wlan", "ssid", "hotspot")));

        subCategoryKeywords.put(SubCategory.LAN, new HashSet<>(Arrays.asList(
                "lan", "ethernet", "cable", "wired", "rj45", "network cable")));

        subCategoryKeywords.put(SubCategory.INTERNET, new HashSet<>(Arrays.asList(
                "internet", "browsing", "website", "web", "online", "offline")));

        subCategoryKeywords.put(SubCategory.FIREWALL, new HashSet<>(Arrays.asList(
                "firewall", "blocked", "port", "security", "filter")));

        // ----- SOFTWARE SUB-CATEGORIES -----
        subCategoryKeywords.put(SubCategory.LOGIN, new HashSet<>(Arrays.asList(
                "login", "log in", "signin", "sign in", "authenticate",
                "cannot login", "unable to login", "login failed", "credentials")));

        subCategoryKeywords.put(SubCategory.INSTALLATION, new HashSet<>(Arrays.asList(
                "install", "installation", "setup", "configure", "deploy")));

        subCategoryKeywords.put(SubCategory.UPDATE, new HashSet<>(Arrays.asList(
                "update", "upgrade", "patch", "version", "latest")));

        subCategoryKeywords.put(SubCategory.CRASH, new HashSet<>(Arrays.asList(
                "crash", "crashes", "crashing", "stopped working", "not responding",
                "freeze", "frozen", "hang", "hanging", "stuck")));

        subCategoryKeywords.put(SubCategory.LICENSE, new HashSet<>(Arrays.asList(
                "license", "licence", "activation", "activate", "key", "expired")));

        subCategoryKeywords.put(SubCategory.PERFORMANCE, new HashSet<>(Arrays.asList(
                "slow", "performance", "lag", "lagging", "speed", "takes long")));

        // ----- HARDWARE SUB-CATEGORIES -----
        subCategoryKeywords.put(SubCategory.LAPTOP, new HashSet<>(Arrays.asList(
                "laptop", "notebook", "macbook", "thinkpad", "dell", "hp", "lenovo")));

        subCategoryKeywords.put(SubCategory.DESKTOP, new HashSet<>(Arrays.asList(
                "desktop", "pc", "workstation", "tower", "cpu")));

        subCategoryKeywords.put(SubCategory.KEYBOARD, new HashSet<>(Arrays.asList(
                "keyboard", "keys", "typing", "key not working")));

        subCategoryKeywords.put(SubCategory.MOUSE, new HashSet<>(Arrays.asList(
                "mouse", "cursor", "pointer", "click", "clicking", "scroll")));

        subCategoryKeywords.put(SubCategory.MONITOR, new HashSet<>(Arrays.asList(
                "monitor", "screen", "display", "resolution", "flickering", "blank")));

        subCategoryKeywords.put(SubCategory.PRINTER, new HashSet<>(Arrays.asList(
                "printer", "print", "printing", "paper", "jam", "toner", "ink")));

        subCategoryKeywords.put(SubCategory.PERIPHERAL, new HashSet<>(Arrays.asList(
                "usb", "device", "peripheral", "webcam", "camera", "headset",
                "speaker", "microphone", "scanner", "projector")));

        // ----- ACCESS SUB-CATEGORIES -----
        subCategoryKeywords.put(SubCategory.PASSWORD, new HashSet<>(Arrays.asList(
                "password", "reset", "forgot", "forgotten", "change password",
                "new password", "expired password")));

        subCategoryKeywords.put(SubCategory.ACCOUNT_UNLOCK, new HashSet<>(Arrays.asList(
                "locked", "unlock", "blocked", "disabled", "account locked")));

        subCategoryKeywords.put(SubCategory.PERMISSION, new HashSet<>(Arrays.asList(
                "permission", "access denied", "unauthorized", "folder access",
                "share", "shared folder")));

        subCategoryKeywords.put(SubCategory.NEW_ACCOUNT, new HashSet<>(Arrays.asList(
                "new account", "create account", "new user", "onboarding")));

        // ----- EMAIL SUB-CATEGORIES -----
        subCategoryKeywords.put(SubCategory.OUTLOOK, new HashSet<>(Arrays.asList(
                "outlook", "office 365", "o365", "microsoft 365")));

        subCategoryKeywords.put(SubCategory.EMAIL_SEND, new HashSet<>(Arrays.asList(
                "send", "sending", "outbox", "sent items", "delivery failed")));

        subCategoryKeywords.put(SubCategory.EMAIL_RECEIVE, new HashSet<>(Arrays.asList(
                "receive", "receiving", "inbox", "not getting", "missing email")));

        subCategoryKeywords.put(SubCategory.CALENDAR, new HashSet<>(Arrays.asList(
                "calendar", "meeting", "invite", "appointment", "schedule")));

        // GENERAL - catch-all
        subCategoryKeywords.put(SubCategory.GENERAL, new HashSet<>());
    }

    // ============================================================
    // URGENCY KEYWORDS (for Priority Detection)
    // ============================================================

    private void initializeUrgencyKeywords() {

        // CRITICAL keywords - immediate action required
        criticalKeywords.addAll(Arrays.asList(
                "emergency", "critical", "down", "outage", "all users",
                "entire", "company", "organization", "security breach",
                "hacked", "virus", "ransomware", "data loss", "server down"));

        // URGENT keywords - high priority
        urgentKeywords.addAll(Arrays.asList(
                "urgent", "urgently", "asap", "immediately", "critical",
                "important", "priority", "cannot work", "blocked", "deadline",
                "production", "customer", "meeting", "presentation"));
    }

    // ============================================================
    // PUBLIC METHODS FOR KEYWORD MATCHING
    // ============================================================

    /**
     * Get all keywords for a category.
     */
    public Set<String> getKeywordsForCategory(Category category) {
        return categoryKeywords.getOrDefault(category, new HashSet<>());
    }

    /**
     * Get all keywords for a sub-category.
     */
    public Set<String> getKeywordsForSubCategory(SubCategory subCategory) {
        return subCategoryKeywords.getOrDefault(subCategory, new HashSet<>());
    }

    /**
     * Get urgent keywords.
     */
    public Set<String> getUrgentKeywords() {
        return urgentKeywords;
    }

    /**
     * Get critical keywords.
     */
    public Set<String> getCriticalKeywords() {
        return criticalKeywords;
    }

    /**
     * Check if a token matches any keyword in a category.
     */
    public boolean matchesCategory(String token, Category category) {
        Set<String> keywords = categoryKeywords.get(category);
        return keywords != null && keywords.contains(token.toLowerCase());
    }

    /**
     * Check if a token matches any keyword in a sub-category.
     */
    public boolean matchesSubCategory(String token, SubCategory subCategory) {
        Set<String> keywords = subCategoryKeywords.get(subCategory);
        return keywords != null && keywords.contains(token.toLowerCase());
    }

    /**
     * Find all matching categories for a list of tokens.
     */
    public Map<Category, Integer> findMatchingCategories(List<String> tokens) {
        Map<Category, Integer> matchCounts = new HashMap<>();

        for (Category category : Category.values()) {
            int count = 0;
            Set<String> keywords = categoryKeywords.get(category);
            if (keywords != null) {
                for (String token : tokens) {
                    if (keywords.contains(token)) {
                        count++;
                    }
                }
            }
            if (count > 0) {
                matchCounts.put(category, count);
            }
        }

        return matchCounts;
    }

    /**
     * Find all matching sub-categories for a list of tokens.
     */
    public Map<SubCategory, Integer> findMatchingSubCategories(List<String> tokens, Category category) {
        Map<SubCategory, Integer> matchCounts = new HashMap<>();

        for (SubCategory subCategory : SubCategory.values()) {
            // Only check sub-categories of the given category
            if (subCategory.getParentCategory() != category) {
                continue;
            }

            int count = 0;
            Set<String> keywords = subCategoryKeywords.get(subCategory);
            if (keywords != null) {
                for (String token : tokens) {
                    if (keywords.contains(token)) {
                        count++;
                    }
                }
            }
            if (count > 0) {
                matchCounts.put(subCategory, count);
            }
        }

        return matchCounts;
    }

    /**
     * Check if tokens contain urgent keywords.
     */
    public boolean containsUrgentKeywords(List<String> tokens) {
        for (String token : tokens) {
            if (urgentKeywords.contains(token)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if tokens contain critical keywords.
     */
    public boolean containsCriticalKeywords(List<String> tokens) {
        for (String token : tokens) {
            if (criticalKeywords.contains(token)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Log dictionary statistics.
     */
    private void logDictionaryStats() {
        int totalCategoryKeywords = 0;
        for (Set<String> keywords : categoryKeywords.values()) {
            totalCategoryKeywords += keywords.size();
        }

        int totalSubCategoryKeywords = 0;
        for (Set<String> keywords : subCategoryKeywords.values()) {
            totalSubCategoryKeywords += keywords.size();
        }

        logger.info("Dictionary Stats:");
        logger.info("  - Category keywords: {}", totalCategoryKeywords);
        logger.info("  - SubCategory keywords: {}", totalSubCategoryKeywords);
        logger.info("  - Urgent keywords: {}", urgentKeywords.size());
        logger.info("  - Critical keywords: {}", criticalKeywords.size());
    }
}
