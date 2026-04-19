package com.powergrid.ticketsystem.service;

import com.powergrid.ticketsystem.entity.Ticket;
import com.powergrid.ticketsystem.selfservice.UserResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.FlagTerm;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ============================================================
 * EMAIL INGESTION SERVICE
 * ============================================================
 * 
 * This service implements the EMAIL INGESTION CONNECTOR.
 * 
 * PURPOSE:
 * - Polls the IT support inbox (it-support@powergrid.in) periodically
 * - Reads unread emails only
 * - Extracts ticket data from emails
 * - Forwards extracted data to NormalizationService
 * - Creates tickets in MongoDB via TicketService
 * 
 * TECHNOLOGY:
 * - Java Mail API for email reading
 * - Spring Scheduler for periodic polling
 * - IMAP protocol for inbox access
 * 
 * SCHEDULING:
 * - Runs every 5 minutes (configurable)
 * - Only processes unread/unseen emails
 * - Marks processed emails as READ
 */
@Service
public class EmailIngestionService {

    private static final Logger logger = LoggerFactory.getLogger(EmailIngestionService.class);

    private final TicketService ticketService;

    // ============================================================
    // EMAIL CONFIGURATION (from application.properties)
    // ============================================================

    @Value("${mail.imap.host}")
    private String imapHost;

    @Value("${mail.imap.port}")
    private String imapPort;

    @Value("${mail.imap.username}")
    private String emailUsername;

    @Value("${mail.imap.password}")
    private String emailPassword;

    @Value("${mail.imap.protocol}")
    private String protocol;

    @Value("${mail.ingestion.enabled}")
    private boolean ingestionEnabled;

    @Value("${mail.ingestion.maxEmailsPerPoll:10}")
    private int maxEmailsPerPoll;

    @Value("${mail.ingestion.subjectKeywords:}")
    private String subjectKeywordsConfig;

    private List<String> subjectKeywords;

    private UserResponseHandler userResponseHandler;

    // Pattern to extract ticket ID from email (e.g., TKT-1234567890)
    private static final Pattern TICKET_ID_PATTERN = Pattern.compile("TKT-\\d+");

    /**
     * Constructor injection for TicketService.
     */
    public EmailIngestionService(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Setter injection for UserResponseHandler (to handle YES/NO replies).
     */
    @Autowired
    @Lazy
    public void setUserResponseHandler(UserResponseHandler userResponseHandler) {
        this.userResponseHandler = userResponseHandler;
        logger.info("UserResponseHandler injected into EmailIngestionService");
    }

    /**
     * Initialize keyword list after properties are loaded.
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        if (subjectKeywordsConfig != null && !subjectKeywordsConfig.trim().isEmpty()) {
            subjectKeywords = Arrays.asList(subjectKeywordsConfig.toLowerCase().split(","));
            logger.info("Email filter keywords configured: {}", subjectKeywords);
        } else {
            subjectKeywords = null;
            logger.info("No email filter keywords configured - processing all emails");
        }
    }

    // ============================================================
    // SCHEDULED EMAIL POLLING
    // ============================================================

    /**
     * Scheduled method to poll inbox for new emails.
     * 
     * SCHEDULE: Every 5 minutes (300000 milliseconds)
     * Configurable via: email.polling.interval property
     * 
     * FLOW:
     * 1. Connect to IMAP server
     * 2. Open INBOX folder
     * 3. Search for UNSEEN (unread) emails
     * 4. For each unread email:
     * a. Extract sender, subject, body
     * b. Create ticket via TicketService
     * c. Mark email as SEEN (read)
     * 5. Close connection
     */
    @Scheduled(fixedRateString = "${email.polling.interval}")
    public void pollInboxForNewEmails() {

        // Check if email ingestion is enabled
        if (!ingestionEnabled) {
            logger.debug("Email ingestion is disabled. Skipping poll.");
            return;
        }

        logger.info("========================================");
        logger.info("Starting email inbox polling...");
        logger.info("========================================");

        try {
            // Wrap everything in try-catch to prevent scheduler thread from dying
            executeEmailPolling();
        } catch (Exception e) {
            logger.error("Unexpected error during email polling. Will retry next cycle.", e);
        }
    }

    /**
     * Internal method that performs the actual email polling.
     * Separated to allow better exception handling at scheduler level.
     */
    private void executeEmailPolling() {
        Store store = null;
        Folder inbox = null;

        try {
            // Step 1: Get mail session with IMAP properties
            Properties properties = getMailProperties();
            Session session = Session.getInstance(properties);

            // Step 2: Connect to IMAP server
            store = session.getStore(protocol);
            store.connect(imapHost, emailUsername, emailPassword);
            logger.info("Connected to email server: {}", imapHost);

            // Step 3: Open INBOX folder in READ_WRITE mode
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
            logger.info("INBOX opened. Total messages: {}", inbox.getMessageCount());

            // Step 4: Search for unread emails only
            Message[] unreadMessages = inbox.search(
                    new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            logger.info("Unread emails found: {}", unreadMessages.length);

            // Step 5: Process each unread email (limit to prevent memory issues)
            int processedCount = 0;
            int skippedCount = 0;
            int checkedCount = 0;
            int maxCheck = Math.min(unreadMessages.length, maxEmailsPerPoll * 5); // Check up to 50 emails per cycle

            for (int i = 0; i < maxCheck && processedCount < maxEmailsPerPoll; i++) {
                try {
                    Message message = unreadMessages[i];
                    String subject = message.getSubject();
                    checkedCount++;

                    // Check if email matches filter keywords
                    if (shouldProcessEmail(subject)) {
                        processEmail(message);
                        processedCount++;
                    } else {
                        // Mark non-IT emails as read so they're not checked again
                        message.setFlag(Flags.Flag.SEEN, true);
                        skippedCount++;
                        logger.debug("Skipped non-IT email: {}", subject);
                    }
                } catch (Exception e) {
                    logger.error("Error processing email: {}", e.getMessage(), e);
                }
            }

            logger.info("Email polling completed. Processed: {}, Skipped: {}, Checked: {} of {}",
                    processedCount, skippedCount, checkedCount, unreadMessages.length);

        } catch (AuthenticationFailedException e) {
            logger.error("Email authentication failed. Check credentials.", e);
        } catch (MessagingException e) {
            logger.error("Email connection error: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during email processing: {}", e.getMessage(), e);
        } finally {
            // Step 6: Close connections
            closeResources(inbox, store);
        }
    }

    // ============================================================
    // EMAIL PROCESSING LOGIC
    // ============================================================

    /**
     * Processes a single email message.
     * 
     * LOGIC:
     * 1. Check if email is a REPLY (contains ticket ID or "Re:")
     * 2. If REPLY with ticket ID → Route to UserResponseHandler (YES/NO handling)
     * 3. If NEW email → Create new ticket
     * 
     * @param message Email message to process
     * @throws MessagingException If email reading fails
     * @throws IOException        If content extraction fails
     */
    private void processEmail(Message message) throws MessagingException, IOException {

        logger.debug("Processing email message...");

        // Extract sender email address
        Address[] fromAddresses = message.getFrom();
        String senderEmail = (fromAddresses != null && fromAddresses.length > 0)
                ? fromAddresses[0].toString()
                : "unknown@unknown.com";

        // Clean sender email (remove display name if present)
        senderEmail = extractEmailAddress(senderEmail);
        logger.debug("Sender: {}", senderEmail);

        // Extract email subject
        String subject = message.getSubject();
        if (subject == null || subject.isEmpty()) {
            subject = "No Subject";
        }
        logger.debug("Subject: {}", subject);

        // Extract email body
        String body = extractEmailBody(message);
        if (body == null || body.isEmpty()) {
            body = subject; // Use subject as body if body is empty
        }
        logger.debug("Body length: {} characters", body.length());

        // ================================================================
        // CHECK IF THIS IS A REPLY EMAIL (contains ticket ID)
        // ================================================================
        String ticketId = extractTicketIdFromEmail(subject, body);

        if (ticketId != null && userResponseHandler != null) {
            // This is a REPLY to an existing ticket - handle YES/NO response
            logger.info("Detected reply email for ticket: {}", ticketId);
            handleReplyEmail(ticketId, body, senderEmail, message);
            return;
        }

        // ================================================================
        // NEW TICKET - Create from email
        // ================================================================
        // Extract received date
        LocalDateTime receivedTime = message.getReceivedDate() != null
                ? message.getReceivedDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                : LocalDateTime.now();

        // Create ticket using TicketService
        Ticket createdTicket = ticketService.createEmailTicket(
                senderEmail, subject, body, receivedTime);

        if (createdTicket != null) {
            logger.info("Ticket created from email. TicketId: {}, Sender: {}",
                    createdTicket.getTicketId(), senderEmail);

            // Mark email as READ (SEEN)
            message.setFlag(Flags.Flag.SEEN, true);
            logger.debug("Email marked as READ");
        } else {
            logger.warn("Ticket not created (possibly duplicate). Sender: {}", senderEmail);
            // Still mark as read to avoid reprocessing
            message.setFlag(Flags.Flag.SEEN, true);
        }
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Extracts ticket ID from email subject or body.
     * Looks for pattern like TKT-1234567890
     * 
     * @param subject Email subject
     * @param body    Email body
     * @return Ticket ID if found, null otherwise
     */
    private String extractTicketIdFromEmail(String subject, String body) {
        // First check subject
        if (subject != null) {
            Matcher matcher = TICKET_ID_PATTERN.matcher(subject);
            if (matcher.find()) {
                return matcher.group();
            }
        }

        // Then check body
        if (body != null) {
            Matcher matcher = TICKET_ID_PATTERN.matcher(body);
            if (matcher.find()) {
                return matcher.group();
            }
        }

        return null;
    }

    /**
     * Handles a reply email for an existing ticket.
     * Routes to UserResponseHandler to process YES/NO responses.
     * 
     * @param ticketId    The ticket ID from the email
     * @param body        The email body containing user response
     * @param senderEmail The sender's email
     * @param message     The email message to mark as read
     */
    private void handleReplyEmail(String ticketId, String body, String senderEmail, Message message)
            throws MessagingException {
        try {
            logger.info("Processing reply email for ticket: {} from: {}", ticketId, senderEmail);

            // Extract the user's response (first line or first word)
            String userResponse = extractUserResponse(body);
            logger.info("Extracted user response: '{}' for ticket: {}", userResponse, ticketId);

            // Process the response using UserResponseHandler
            UserResponseHandler.ResponseResult result = userResponseHandler.processResponse(ticketId, userResponse,
                    "EMAIL");

            if (result.isProcessed()) {
                logger.info("Reply processed successfully for ticket: {}. Action: {}",
                        ticketId, result.getAction());
            } else {
                logger.warn("Reply processing failed for ticket: {}. Reason: {}",
                        ticketId, result.getMessage());
            }

            // Mark email as read
            message.setFlag(Flags.Flag.SEEN, true);
            logger.debug("Reply email marked as READ");

        } catch (Exception e) {
            logger.error("Error handling reply email for ticket {}: {}", ticketId, e.getMessage(), e);
            // Still mark as read to avoid reprocessing
            message.setFlag(Flags.Flag.SEEN, true);
        }
    }

    /**
     * Extracts the user's YES/NO response from email body.
     * Handles various email reply formats.
     * 
     * @param body Full email body
     * @return Extracted response (first meaningful word/line)
     */
    private String extractUserResponse(String body) {
        if (body == null || body.trim().isEmpty()) {
            return "";
        }

        // Get the first line of the email (user's actual response)
        String[] lines = body.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            // Skip empty lines and common email reply markers
            if (!trimmed.isEmpty() &&
                    !trimmed.startsWith(">") &&
                    !trimmed.startsWith("On ") &&
                    !trimmed.startsWith("From:") &&
                    !trimmed.startsWith("Sent:") &&
                    !trimmed.startsWith("To:") &&
                    !trimmed.startsWith("Subject:") &&
                    !trimmed.startsWith("---") &&
                    !trimmed.startsWith("___") &&
                    !trimmed.contains("wrote:")) {

                // Return the first meaningful line (likely "Yes" or "No")
                return trimmed;
            }
        }

        return body.trim();
    }

    /**
     * Creates IMAP mail properties for connection.
     * 
     * @return Properties configured for IMAP/IMAPS
     */
    private Properties getMailProperties() {
        Properties properties = new Properties();

        // IMAP configuration
        properties.put("mail.store.protocol", protocol);
        properties.put("mail.imap.host", imapHost);
        properties.put("mail.imap.port", imapPort);

        // SSL/TLS configuration for IMAPS
        properties.put("mail.imap.ssl.enable", "true");
        properties.put("mail.imap.ssl.trust", "*");
        properties.put("mail.imap.starttls.enable", "true");

        // Timeout settings (in milliseconds)
        properties.put("mail.imap.connectiontimeout", "10000");
        properties.put("mail.imap.timeout", "10000");

        return properties;
    }

    /**
     * Checks if an email should be processed based on subject keywords.
     * 
     * If no keywords are configured, all emails are processed.
     * If keywords are configured, only emails with matching subjects are processed.
     * 
     * @param subject Email subject line
     * @return true if email should be processed, false to skip
     */
    private boolean shouldProcessEmail(String subject) {
        // If no filter configured, process all emails
        if (subjectKeywords == null || subjectKeywords.isEmpty()) {
            return true;
        }

        // If subject is empty, skip
        if (subject == null || subject.trim().isEmpty()) {
            return false;
        }

        // Check if subject contains any of the keywords
        String lowerSubject = subject.toLowerCase();
        for (String keyword : subjectKeywords) {
            if (lowerSubject.contains(keyword.trim())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Extracts email address from a full address string.
     * 
     * Examples:
     * - "John Doe <john.doe@powergrid.in>" → "john.doe@powergrid.in"
     * - "john.doe@powergrid.in" → "john.doe@powergrid.in"
     * 
     * @param fullAddress Full email address string
     * @return Cleaned email address
     */
    private String extractEmailAddress(String fullAddress) {
        if (fullAddress == null) {
            return "unknown@unknown.com";
        }

        // Check if address contains angle brackets
        if (fullAddress.contains("<") && fullAddress.contains(">")) {
            int start = fullAddress.indexOf('<') + 1;
            int end = fullAddress.indexOf('>');
            return fullAddress.substring(start, end).trim().toLowerCase();
        }

        return fullAddress.trim().toLowerCase();
    }

    /**
     * Extracts the text body from an email message.
     * Handles both plain text and multipart (HTML) messages.
     * 
     * @param message Email message
     * @return Extracted text body
     * @throws MessagingException If reading fails
     * @throws IOException        If content extraction fails
     */
    private String extractEmailBody(Message message) throws MessagingException, IOException {
        Object content = message.getContent();

        // Handle plain text messages
        if (content instanceof String) {
            return (String) content;
        }

        // Handle multipart messages (with HTML/attachments)
        if (content instanceof MimeMultipart) {
            return extractTextFromMultipart((MimeMultipart) content);
        }

        // Fallback: return content type info
        return "Unable to extract email body. Content type: " + message.getContentType();
    }

    /**
     * Extracts text content from multipart message.
     * Prefers plain text over HTML.
     * 
     * @param multipart Multipart content
     * @return Extracted text
     * @throws MessagingException If reading fails
     * @throws IOException        If content extraction fails
     */
    private String extractTextFromMultipart(MimeMultipart multipart)
            throws MessagingException, IOException {

        StringBuilder result = new StringBuilder();
        int partCount = multipart.getCount();

        for (int i = 0; i < partCount; i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            String contentType = bodyPart.getContentType().toLowerCase();

            // Prefer plain text
            if (contentType.contains("text/plain")) {
                return (String) bodyPart.getContent();
            }

            // Handle nested multipart
            if (bodyPart.getContent() instanceof MimeMultipart) {
                result.append(extractTextFromMultipart((MimeMultipart) bodyPart.getContent()));
            }

            // Fallback to HTML (strip tags later in normalization)
            if (contentType.contains("text/html") && result.length() == 0) {
                result.append(bodyPart.getContent().toString());
            }
        }

        return result.toString();
    }

    /**
     * Safely closes mail resources.
     * 
     * @param inbox Folder to close
     * @param store Store to close
     */
    private void closeResources(Folder inbox, Store store) {
        try {
            if (inbox != null && inbox.isOpen()) {
                inbox.close(false);
                logger.debug("INBOX closed");
            }
        } catch (MessagingException e) {
            logger.warn("Error closing inbox: {}", e.getMessage());
        }

        try {
            if (store != null && store.isConnected()) {
                store.close();
                logger.debug("Store connection closed");
            }
        } catch (MessagingException e) {
            logger.warn("Error closing store: {}", e.getMessage());
        }
    }

    // ============================================================
    // MANUAL TRIGGER METHOD (for testing)
    // ============================================================

    /**
     * Manually triggers email polling.
     * Useful for testing or on-demand processing.
     */
    public void triggerManualPoll() {
        logger.info("Manual email poll triggered");
        pollInboxForNewEmails();
    }
}
