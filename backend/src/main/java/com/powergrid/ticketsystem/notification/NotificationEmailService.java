package com.powergrid.ticketsystem.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * ============================================================
 * NOTIFICATION EMAIL SERVICE
 * ============================================================
 * 
 * PHASE 8: NOTIFICATIONS & ALERTS
 * 
 * Low-level email sending service using Spring Mail.
 * Handles the actual delivery of notification emails.
 * 
 * FEATURES:
 * - Supports both plain text and HTML emails
 * - Asynchronous email sending
 * - Comprehensive error handling
 * - Logging for audit trail
 * - Configurable sender address
 * 
 * TECHNICAL STACK:
 * - Spring Mail (JavaMailSender)
 * - Gmail SMTP (already configured in application.properties)
 * - @Async for non-blocking email delivery
 */
@Service
public class NotificationEmailService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEmailService.class);

    private final JavaMailSender mailSender;

    @Value("${notification.email.from:it-support@powergrid.com}")
    private String fromAddress;

    @Value("${notification.email.from-name:PowerGrid IT Support}")
    private String fromName;

    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${notification.email.test-mode:false}")
    private boolean testMode;

    @Value("${notification.email.test-recipient:}")
    private String testRecipient;

    public NotificationEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        logger.info("╔══════════════════════════════════════════════════════════╗");
        logger.info("║   NOTIFICATION EMAIL SERVICE INITIALIZED                 ║");
        logger.info("╚══════════════════════════════════════════════════════════╝");
    }

    // ============================================================
    // PUBLIC EMAIL SENDING METHODS
    // ============================================================

    /**
     * Send a plain text email notification.
     * 
     * @param to      Recipient email address
     * @param subject Email subject line
     * @param body    Plain text email body
     * @return true if email was sent successfully, false otherwise
     */
    public boolean sendEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            logger.info("Email notifications disabled. Would send to: {}", to);
            return false;
        }

        // In test mode, redirect all emails to test recipient
        String actualRecipient = testMode && !testRecipient.isEmpty() ? testRecipient : to;

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(actualRecipient);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            logger.info("✅ Email sent successfully");
            logger.info("   To: {}", actualRecipient);
            logger.info("   Subject: {}", subject);

            return true;

        } catch (MailException e) {
            logger.error("❌ Failed to send email to {}: {}", actualRecipient, e.getMessage());
            return false;
        }
    }

    /**
     * Send an HTML email notification.
     * Used for rich-formatted notifications with professional styling.
     * 
     * @param to       Recipient email address
     * @param subject  Email subject line
     * @param htmlBody HTML formatted email body
     * @return true if email was sent successfully, false otherwise
     */
    public boolean sendHtmlEmail(String to, String subject, String htmlBody) {
        if (!emailEnabled) {
            logger.info("Email notifications disabled. Would send HTML email to: {}", to);
            return false;
        }

        // In test mode, redirect all emails to test recipient
        String actualRecipient = testMode && !testRecipient.isEmpty() ? testRecipient : to;

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromAddress, fromName);
            helper.setTo(actualRecipient);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = isHtml

            mailSender.send(mimeMessage);

            logger.info("✅ HTML Email sent successfully");
            logger.info("   To: {}", actualRecipient);
            logger.info("   Subject: {}", subject);

            return true;

        } catch (MailException | MessagingException | java.io.UnsupportedEncodingException e) {
            logger.error("❌ Failed to send HTML email to {}: {}", actualRecipient, e.getMessage());
            return false;
        }
    }

    /**
     * Send email asynchronously (non-blocking).
     * Use this for notifications that don't need immediate confirmation.
     * 
     * @param to      Recipient email address
     * @param subject Email subject line
     * @param body    Email body
     */
    @Async
    public void sendEmailAsync(String to, String subject, String body) {
        logger.debug("Sending async email to: {}", to);
        sendEmail(to, subject, body);
    }

    /**
     * Send HTML email asynchronously (non-blocking).
     * 
     * @param to       Recipient email address
     * @param subject  Email subject line
     * @param htmlBody HTML formatted email body
     */
    @Async
    public void sendHtmlEmailAsync(String to, String subject, String htmlBody) {
        logger.debug("Sending async HTML email to: {}", to);
        sendHtmlEmail(to, subject, htmlBody);
    }

    /**
     * Send email to multiple recipients.
     * 
     * @param recipients Array of recipient email addresses
     * @param subject    Email subject line
     * @param body       Email body
     * @return Number of emails successfully sent
     */
    public int sendBulkEmail(String[] recipients, String subject, String body) {
        int successCount = 0;

        for (String recipient : recipients) {
            if (sendEmail(recipient, subject, body)) {
                successCount++;
            }
        }

        logger.info("Bulk email completed: {}/{} sent successfully",
                successCount, recipients.length);

        return successCount;
    }

    // ============================================================
    // UTILITY METHODS
    // ============================================================

    /**
     * Check if email service is properly configured and enabled.
     * 
     * @return true if email service is ready to send
     */
    public boolean isEmailServiceReady() {
        return emailEnabled && mailSender != null;
    }

    /**
     * Get the configured sender address.
     * 
     * @return From email address
     */
    public String getFromAddress() {
        return fromAddress;
    }

    /**
     * Check if test mode is enabled.
     * 
     * @return true if in test mode
     */
    public boolean isTestMode() {
        return testMode;
    }
}
