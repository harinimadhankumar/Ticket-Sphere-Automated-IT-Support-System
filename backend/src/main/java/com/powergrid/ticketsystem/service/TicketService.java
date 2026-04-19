package com.powergrid.ticketsystem.service;

import com.powergrid.ticketsystem.constants.TicketConstants;
import com.powergrid.ticketsystem.dto.ChatbotTicketRequest;
import com.powergrid.ticketsystem.dto.TicketResponse;
import com.powergrid.ticketsystem.entity.Ticket;
import com.powergrid.ticketsystem.nlp.ClassificationService;
import com.powergrid.ticketsystem.nlp.ClassificationService.ClassificationResult;
import com.powergrid.ticketsystem.notification.NotificationService;
import com.powergrid.ticketsystem.repository.TicketRepository;
import com.powergrid.ticketsystem.selfservice.FallbackAssignmentService;
import com.powergrid.ticketsystem.selfservice.SelfServiceOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Ticket Service - Central business logic for ticket operations */
@Service
public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final NormalizationService normalizationService;
    private NotificationService notificationService;
    private ClassificationService classificationService;
    private SelfServiceOrchestrator selfServiceOrchestrator;
    private FallbackAssignmentService fallbackAssignmentService;

    public TicketService(TicketRepository ticketRepository,
            NormalizationService normalizationService) {
        this.ticketRepository = ticketRepository;
        this.normalizationService = normalizationService;
    }

    @Autowired
    @Lazy
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
        logger.info("NotificationService injected into Ticket Service");
    }

    @Autowired
    @Lazy
    public void setClassificationService(ClassificationService classificationService) {
        this.classificationService = classificationService;
        logger.info("ClassificationService injected into Ticket Service");
    }

    @Autowired
    @Lazy
    public void setSelfServiceOrchestrator(SelfServiceOrchestrator selfServiceOrchestrator) {
        this.selfServiceOrchestrator = selfServiceOrchestrator;
        logger.info("SelfServiceOrchestrator injected into Ticket Service");
    }

    @Autowired
    @Lazy
    public void setFallbackAssignmentService(FallbackAssignmentService fallbackAssignmentService) {
        this.fallbackAssignmentService = fallbackAssignmentService;
        logger.info("FallbackAssignmentService injected into Ticket Service");
    }

    /** Creates a new ticket from Chatbot source */
    public TicketResponse createChatbotTicket(ChatbotTicketRequest request) {

        logger.info("Creating chatbot ticket for employee: {}", request.getEmployeeId());

        Ticket normalizedTicket = normalizationService.normalizeChatbotTicket(request);

        if (!normalizationService.validateTicket(normalizedTicket)) {
            logger.error("Ticket validation failed for employee: {}", request.getEmployeeId());
            throw new IllegalArgumentException("Invalid ticket data");
        }

        Ticket savedTicket = ticketRepository.save(normalizedTicket);

        logger.info("Chatbot ticket created successfully. TicketId: {}",
                savedTicket.getTicketId());

        return convertToResponse(savedTicket);
    }

    /** Creates a new ticket from Email source */
    public Ticket createEmailTicket(String senderEmail, String emailSubject,
            String emailBody, LocalDateTime receivedTime) {

        logger.info("Creating email ticket from: {}", senderEmail);

        // Allow repeated incidents with same subject/sender to create fresh tickets.
        // Email polling already marks processed emails as SEEN, so strict
        // sender+subject
        // dedup blocks valid follow-up incidents (especially demo Scenario 3).
        if (ticketRepository.existsBySenderEmailAndEmailSubject(senderEmail, emailSubject)) {
            logger.info(
                    "Existing ticket found for same sender+subject; creating a new ticket instance. Sender: {}, Subject: {}",
                    senderEmail, emailSubject);
        }

        Ticket normalizedTicket = normalizationService.normalizeEmailTicket(
                senderEmail, emailSubject, emailBody, receivedTime);

        if (!normalizationService.validateTicket(normalizedTicket)) {
            logger.error("Email ticket validation failed. Sender: {}", senderEmail);
            return null;
        }

        Ticket savedTicket = ticketRepository.save(normalizedTicket);

        logger.info("Email ticket created successfully. TicketId: {}, Status: NEW (awaiting KB check)",
                savedTicket.getTicketId());

        // Check Knowledge Base before classification
        try {
            if (selfServiceOrchestrator != null && "EMAIL".equalsIgnoreCase(savedTicket.getSource())) {
                logger.info("════════════════════════════════════════════════════════");
                logger.info("CHECKING KNOWLEDGE BASE BEFORE CLASSIFICATION");
                logger.info("Ticket: {} - Status: NEW", savedTicket.getTicketId());
                logger.info("════════════════════════════════════════════════════════");

                // Check if KB solution exists for this issue (using issue description only)
                SelfServiceOrchestrator.OrchestrationResult kbResult = selfServiceOrchestrator
                        .checkAndDeliverSolution(savedTicket);

                if (kbResult.isSolutionDelivered()) {
                    // KB solution found and sent to user
                    // DO NOT classify, DO NOT assign engineer
                    // Wait for user's YES/NO response
                    logger.info("✓ KB Solution sent to user. Ticket {} is AWAITING_RESPONSE",
                            savedTicket.getTicketId());
                    logger.info("  → NO classification performed");
                    logger.info("  → NO engineer assigned");
                    logger.info("  → Waiting for user's YES/NO reply");

                    // Return ticket with AWAITING_RESPONSE status
                    return ticketRepository.findByTicketId(savedTicket.getTicketId())
                            .orElse(savedTicket);
                } else {
                    // No KB match found - proceed with classification
                    logger.info("✗ No KB solution found. Proceeding with classification...");
                }
            }
        } catch (Exception e) {
            logger.warn("KB check failed for {}: {}. Proceeding with classification...",
                    savedTicket.getTicketId(), e.getMessage());
        }

        // No KB match - classify and assign engineer
        logger.info("Proceeding with classification and engineer assignment for: {}", savedTicket.getTicketId());

        // Send TICKET_CREATED notification
        try {
            if (notificationService != null) {
                notificationService.notifyTicketCreated(savedTicket);
            }
        } catch (Exception e) {
            logger.warn("Failed to send TICKET_CREATED notification for {}: {}",
                    savedTicket.getTicketId(), e.getMessage());
        }

        // Now classify and assign
        try {
            if (classificationService != null) {
                savedTicket = autoClassifyTicket(savedTicket);
            } else {
                logger.warn("ClassificationService unavailable for {}. Using fallback direct assignment.",
                        savedTicket.getTicketId());
                savedTicket = forceDirectAssignmentFallback(savedTicket, "Classification service unavailable");
            }
        } catch (Exception e) {
            logger.warn("Auto-classification failed for {}: {}",
                    savedTicket.getTicketId(), e.getMessage());
            savedTicket = forceDirectAssignmentFallback(savedTicket, "Auto-classification failure");
        }

        return savedTicket;
    }

    /**
     * Fallback path to guarantee no-KB tickets are directly assigned to engineers.
     */
    private Ticket forceDirectAssignmentFallback(Ticket ticket, String reason) {
        try {
            if (fallbackAssignmentService != null) {
                Ticket assigned = fallbackAssignmentService.assignDueToNoSolution(ticket.getTicketId());
                logger.info("Fallback direct assignment completed for {}. Reason: {}", ticket.getTicketId(), reason);
                return assigned;
            }
        } catch (Exception fallbackError) {
            logger.error("Fallback direct assignment failed for {}: {}", ticket.getTicketId(),
                    fallbackError.getMessage());
        }

        logger.error("No fallback assignment service available for {}. Ticket may remain OPEN.", ticket.getTicketId());
        return ticket;
    }

    /** Auto-classifies a ticket using NLP classification service */
    private Ticket autoClassifyTicket(Ticket ticket) {
        logger.info("Auto-classifying ticket: {}", ticket.getTicketId());

        // Run classification
        ClassificationResult result = classificationService.classifyTicket(
                ticket.getIssueDescription());

        // Update ticket with classification results
        ticket.setCategory(result.getCategory().name());
        ticket.setSubCategory(result.getSubCategory().name());
        ticket.setPriority(result.getPriority().name());
        ticket.setAssignedTeam(result.getTeam().getTeamName());
        ticket.setAssignedEngineer(result.getAssignedEngineer());
        ticket.setConfidenceScore(result.getConfidenceScore());
        ticket.setIsClassified(true);
        ticket.setClassifiedTime(LocalDateTime.now());
        ticket.setStatus("ASSIGNED");

        // Save classified ticket
        Ticket classifiedTicket = ticketRepository.save(ticket);

        logger.info("Ticket {} auto-classified: Category={}, Priority={}, Engineer={}",
                ticket.getTicketId(), result.getCategory(), result.getPriority(),
                result.getAssignedEngineer());

        // Send TICKET_ASSIGNED notification to engineer
        try {
            if (notificationService != null && classifiedTicket.getAssignedEngineer() != null) {
                notificationService.notifyTicketAssigned(classifiedTicket);
            }
        } catch (Exception e) {
            logger.warn("Failed to send TICKET_ASSIGNED notification for {}: {}",
                    classifiedTicket.getTicketId(), e.getMessage());
        }

        return classifiedTicket;
    }

    /** Retrieves all tickets ordered by creation time */
    public List<TicketResponse> getAllTickets() {

        logger.info("Fetching all tickets from database");

        List<Ticket> tickets = ticketRepository.findAllByOrderByCreatedTimeDesc();

        logger.info("Total tickets retrieved: {}", tickets.size());

        return tickets.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /** Retrieves tickets filtered by source channel */
    public List<TicketResponse> getTicketsBySource(String source) {

        logger.info("Fetching tickets by source: {}", source);

        String normalizedSource = source.toUpperCase().trim();
        if (!normalizedSource.equals(TicketConstants.SOURCE_EMAIL) &&
                !normalizedSource.equals(TicketConstants.SOURCE_CHATBOT)) {
            logger.warn("Invalid source parameter: {}", source);
            throw new IllegalArgumentException("Invalid source. Must be EMAIL or CHATBOT");
        }

        List<Ticket> tickets = ticketRepository.findBySource(normalizedSource);

        logger.info("Tickets found for source {}: {}", normalizedSource, tickets.size());

        return tickets.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /** Retrieves only OPEN tickets */
    public List<TicketResponse> getOpenTickets() {

        logger.info("Fetching all OPEN tickets");

        List<Ticket> tickets = ticketRepository.findByStatusOrderByCreatedTimeDesc(
                TicketConstants.STATUS_OPEN);

        logger.info("Open tickets found: {}", tickets.size());

        return tickets.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /** Retrieves tickets filtered by status */
    public List<TicketResponse> getTicketsByStatus(String status) {

        logger.info("Fetching tickets by status: {}", status);

        String normalizedStatus = status.toUpperCase().trim();

        List<Ticket> tickets = ticketRepository.findByStatus(normalizedStatus);

        logger.info("Tickets found with status {}: {}", normalizedStatus, tickets.size());

        return tickets.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /** Retrieves a single ticket by ID */
    public TicketResponse getTicketById(String ticketId) {

        logger.info("Fetching ticket by ID: {}", ticketId);

        Optional<Ticket> ticketOpt = ticketRepository.findByTicketId(ticketId);

        if (ticketOpt.isPresent()) {
            logger.info("Ticket found: {}", ticketId);
            return convertToResponse(ticketOpt.get());
        } else {
            logger.warn("Ticket not found: {}", ticketId);
            return null;
        }
    }

    /** Retrieves tickets for a specific employee */
    public List<TicketResponse> getTicketsByEmployee(String employeeId) {

        logger.info("Fetching tickets for employee: {}", employeeId);

        List<Ticket> tickets = ticketRepository.findByEmployeeId(employeeId.toUpperCase());

        logger.info("Tickets found for employee {}: {}", employeeId, tickets.size());

        return tickets.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /** Searches tickets by keyword in issue description */
    public List<TicketResponse> searchTickets(String keyword) {

        logger.info("Searching tickets with keyword: {}", keyword);

        List<Ticket> tickets = ticketRepository.searchByIssueDescription(keyword);

        logger.info("Tickets found with keyword '{}': {}", keyword, tickets.size());

        return tickets.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /** Updates the status of a ticket */
    public TicketResponse updateTicketStatus(String ticketId, String newStatus) {

        logger.info("Updating ticket {} status to {}", ticketId, newStatus);

        Optional<Ticket> ticketOpt = ticketRepository.findByTicketId(ticketId);

        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            ticket.setStatus(newStatus.toUpperCase().trim());
            Ticket updatedTicket = ticketRepository.save(ticket);

            logger.info("Ticket status updated successfully: {}", ticketId);
            return convertToResponse(updatedTicket);
        } else {
            logger.warn("Ticket not found for status update: {}", ticketId);
            return null;
        }
    }

    /** Gets count of tickets by source */
    public long getTicketCountBySource(String source) {
        return ticketRepository.countBySource(source.toUpperCase());
    }

    /** Gets count of tickets by status */
    public long getTicketCountByStatus(String status) {
        return ticketRepository.countByStatus(status.toUpperCase());
    }

    /** Gets ticket counts grouped by status for dashboard */
    public java.util.Map<String, Long> getTicketCountsByStatus() {
        java.util.Map<String, Long> counts = new java.util.HashMap<>();
        counts.put("OPEN", ticketRepository.countByStatus("OPEN"));
        counts.put("IN_PROGRESS", ticketRepository.countByStatus("IN_PROGRESS"));
        counts.put("RESOLVED", ticketRepository.countByStatus("RESOLVED"));
        counts.put("CLOSED", ticketRepository.countByStatus("CLOSED"));
        return counts;
    }

    /** Gets the most recent tickets for dashboard display */
    public List<TicketResponse> getRecentTickets(int limit) {
        logger.info("Fetching {} most recent tickets", limit);

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);

        List<Ticket> tickets = ticketRepository.findAllByOrderByCreatedTimeDesc(pageable);

        return tickets.stream()
                .map(this::convertToResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    /** Converts Ticket entity to TicketResponse DTO */
    private TicketResponse convertToResponse(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setTicketId(ticket.getTicketId());
        response.setSource(ticket.getSource());
        response.setEmployeeId(ticket.getEmployeeId());
        response.setIssueDescription(ticket.getIssueDescription());
        response.setCreatedTime(ticket.getCreatedTime());
        response.setStatus(ticket.getStatus());
        response.setSenderEmail(ticket.getSenderEmail());
        response.setEmailSubject(ticket.getEmailSubject());

        // Phase 2 & 3: Classification fields
        response.setCategory(ticket.getCategory());
        response.setSubCategory(ticket.getSubCategory());
        response.setPriority(ticket.getPriority());
        response.setAssignedTeam(ticket.getAssignedTeam());
        response.setAssignedEngineer(ticket.getAssignedEngineer());
        response.setConfidenceScore(ticket.getConfidenceScore());
        response.setIsClassified(ticket.getIsClassified());
        response.setClassifiedTime(ticket.getClassifiedTime());

        return response;
    }
}
