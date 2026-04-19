package com.powergrid.ticketsystem.selfservice;

import com.powergrid.ticketsystem.constants.ResolutionStatus;
import com.powergrid.ticketsystem.entity.Ticket;
import com.powergrid.ticketsystem.notification.NotificationEmailService;
import com.powergrid.ticketsystem.notification.HtmlEmailTemplateService;
import com.powergrid.ticketsystem.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * ============================================================
 * TICKET CLOSURE SERVICE
 * ============================================================
 * 
 * PHASE 4: SELF-SERVICE RESOLUTION
 * 
 * Handles automatic ticket closure when user confirms resolution.
 * 
 * CLOSURE FLOW:
 * 1. User responds "YES" to solution
 * 2. UserResponseHandler calls this service
 * 3. Ticket status → CLOSED
 * 4. closed_by → SYSTEM
 * 5. Resolution status → SELF_RESOLVED
 * 6. Update knowledge base success metrics
 * 
 * CLOSED BY VALUES:
 * - SYSTEM: Auto-closed after successful self-service
 * - ENGINEER: Closed by engineer after manual resolution
 * - USER: Closed by user request
 */
@Service
public class TicketClosureService {

    private static final Logger logger = LoggerFactory.getLogger(TicketClosureService.class);

    private final TicketRepository ticketRepository;
    private final KnowledgeBaseService knowledgeBaseService;
    private final NotificationEmailService notificationEmailService;
    private final HtmlEmailTemplateService htmlTemplateService;

    public TicketClosureService(TicketRepository ticketRepository,
            KnowledgeBaseService knowledgeBaseService,
            NotificationEmailService notificationEmailService,
            HtmlEmailTemplateService htmlTemplateService) {
        this.ticketRepository = ticketRepository;
        this.knowledgeBaseService = knowledgeBaseService;
        this.notificationEmailService = notificationEmailService;
        this.htmlTemplateService = htmlTemplateService;
    }

    // ============================================================
    // AUTO-CLOSE TICKET (Self-Service Resolution)
    // ============================================================

    /**
     * Auto-close a ticket after successful self-service resolution.
     * Called when user responds "YES" to solution confirmation.
     * 
     * @param ticketId        The ticket ID (TKT-XXXXXXXX format)
     * @param knowledgeBaseId The KB entry that provided the solution
     * @return Updated ticket
     */
    @Transactional
    public Ticket autoCloseTicket(String ticketId, Long knowledgeBaseId) {
        logger.info("Auto-closing ticket: {} (KB ID: {})", ticketId, knowledgeBaseId);

        Optional<Ticket> ticketOpt = ticketRepository.findByTicketId(ticketId);
        if (ticketOpt.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found: " + ticketId);
        }

        Ticket ticket = ticketOpt.get();

        // Validate ticket can be closed
        if ("CLOSED".equalsIgnoreCase(ticket.getStatus())) {
            logger.warn("Ticket already closed: {}", ticketId);
            return ticket;
        }

        // Update ticket status
        ticket.setStatus("CLOSED");
        ticket.setClosedBy("SYSTEM");
        ticket.setClosedTime(LocalDateTime.now());
        ticket.setResolutionStatus(ResolutionStatus.SELF_RESOLVED.name());
        ticket.setResolutionNotes("Auto-closed after user confirmed self-service solution worked.");

        // Save updated ticket
        Ticket savedTicket = ticketRepository.save(ticket);
        logger.info("Ticket auto-closed successfully: {}", ticketId);

        // Update knowledge base success metrics
        if (knowledgeBaseId != null) {
            knowledgeBaseService.recordSuccess(knowledgeBaseId);
            logger.info("Updated KB success metrics for entry: {}", knowledgeBaseId);
        }

        return savedTicket;
    }

    /**
     * Auto-close ticket by ticket ID only (when KB ID is stored in ticket).
     */
    @Transactional
    public Ticket autoCloseTicket(String ticketId) {
        Optional<Ticket> ticketOpt = ticketRepository.findByTicketId(ticketId);
        if (ticketOpt.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found: " + ticketId);
        }

        Ticket ticket = ticketOpt.get();
        Long kbId = ticket.getKnowledgeBaseId();

        return autoCloseTicket(ticketId, kbId);
    }

    // ============================================================
    // MANUAL CLOSE (Engineer Closure)
    // ============================================================

    /**
     * Close ticket manually by engineer.
     * 
     * @param ticketId        The ticket ID
     * @param engineerName    Name of the engineer closing the ticket
     * @param resolutionNotes Notes about the resolution
     * @return Updated ticket
     */
    @Transactional
    public Ticket closeByEngineer(String ticketId, String engineerName, String resolutionNotes) {
        logger.info("Closing ticket {} by engineer: {}", ticketId, engineerName);

        Optional<Ticket> ticketOpt = ticketRepository.findByTicketId(ticketId);
        if (ticketOpt.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found: " + ticketId);
        }

        Ticket ticket = ticketOpt.get();

        if ("CLOSED".equalsIgnoreCase(ticket.getStatus())) {
            logger.warn("Ticket already closed: {}", ticketId);
            return ticket;
        }

        ticket.setStatus("CLOSED");
        ticket.setClosedBy(engineerName);
        ticket.setClosedTime(LocalDateTime.now());
        ticket.setResolutionNotes(resolutionNotes);

        // If this was escalated from self-service, update status
        if (ResolutionStatus.ESCALATED.name().equals(ticket.getResolutionStatus()) ||
                ResolutionStatus.TIMED_OUT.name().equals(ticket.getResolutionStatus())) {
            // Keep the resolution status to track that it was escalated
            ticket.setResolutionNotes("Escalated from self-service. " + resolutionNotes);
        } else {
            ticket.setResolutionStatus("ENGINEER_RESOLVED");
        }

        return ticketRepository.save(ticket);
    }

    // ============================================================
    // CLOSE BY USER REQUEST
    // ============================================================

    /**
     * Close ticket by user request (user resolved it themselves).
     */
    @Transactional
    public Ticket closeByUser(String ticketId, String reason) {
        logger.info("Closing ticket {} by user request", ticketId);

        Optional<Ticket> ticketOpt = ticketRepository.findByTicketId(ticketId);
        if (ticketOpt.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found: " + ticketId);
        }

        Ticket ticket = ticketOpt.get();

        if ("CLOSED".equalsIgnoreCase(ticket.getStatus())) {
            return ticket;
        }

        ticket.setStatus("CLOSED");
        ticket.setClosedBy("USER");
        ticket.setClosedTime(LocalDateTime.now());
        ticket.setResolutionNotes("Closed by user: " + (reason != null ? reason : "No reason provided"));
        ticket.setResolutionStatus("USER_CLOSED");

        return ticketRepository.save(ticket);
    }

    // ============================================================
    // REOPEN TICKET
    // ============================================================

    /**
     * Reopen a closed ticket.
     */
    @Transactional
    public Ticket reopenTicket(String ticketId, String reason) {
        logger.info("Reopening ticket: {} - Reason: {}", ticketId, reason);

        Optional<Ticket> ticketOpt = ticketRepository.findByTicketId(ticketId);
        if (ticketOpt.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found: " + ticketId);
        }

        Ticket ticket = ticketOpt.get();

        if (!"CLOSED".equalsIgnoreCase(ticket.getStatus())) {
            logger.warn("Ticket is not closed: {}", ticketId);
            return ticket;
        }

        ticket.setStatus("OPEN");
        ticket.setResolutionStatus(ResolutionStatus.PENDING.name());
        ticket.setResolutionNotes("Reopened: " + reason);
        // Keep closed_by and closed_time for audit trail
        // Reset knowledge base ID for new resolution attempt
        ticket.setKnowledgeBaseId(null);

        return ticketRepository.save(ticket);
    }

    // ============================================================
    // EMAIL CONFIRMATION METHODS
    // ============================================================

    /**
     * Send confirmation email when ticket is auto-closed (YES response).
     */
    public void sendClosureConfirmationEmail(Ticket ticket) {
        String recipientEmail = ticket.getSenderEmail();
        if (recipientEmail == null || recipientEmail.isEmpty()) {
            logger.warn("No email address for ticket: {}", ticket.getTicketId());
            return;
        }

        try {
            String subject = "[" + ticket.getTicketId() + "] Your Issue Has Been Resolved ✓";

            // Generate professional HTML email using template service
            String htmlContent = htmlTemplateService.generateTicketResolvedEmail(ticket);

            // Send as HTML email
            notificationEmailService.sendHtmlEmailAsync(recipientEmail, subject, htmlContent);
            logger.info("✓ Closure confirmation email sent to: {}", recipientEmail);

        } catch (Exception e) {
            logger.error("Failed to send closure confirmation email to: {}", recipientEmail, e);
        }
    }

    /**
     * Send escalation email when user says NO (solution didn't work).
     */
    public void sendEscalationEmail(Ticket ticket, String engineerName) {
        String recipientEmail = ticket.getSenderEmail();
        if (recipientEmail == null || recipientEmail.isEmpty()) {
            logger.warn("No email address for ticket: {}", ticket.getTicketId());
            return;
        }

        try {
            String subject = "[" + ticket.getTicketId() + "] Your Ticket Has Been Escalated to IT Support";

            // Generate professional HTML email using template service
            String htmlContent = htmlTemplateService.generateTicketEscalatedEmail(ticket, "IT Support");

            // Send as HTML email
            notificationEmailService.sendHtmlEmailAsync(recipientEmail, subject, htmlContent);
            logger.info("✓ Escalation notification email sent to: {}", recipientEmail);

        } catch (Exception e) {
            logger.error("Failed to send escalation email to: {}", recipientEmail, e);
        }
    }

    // ============================================================
    // REMOVED: Plain text email formatting methods
    // Now using HtmlEmailTemplateService for all HTML emails
    // ============================================================
}
