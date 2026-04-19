package com.powergrid.ticketsystem.notification;

import com.powergrid.ticketsystem.entity.Ticket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Professional Email Template Service
 * Industry-Standard Email Formatting
 * ONE common email: harinimadhan2005@gmail.com
 */
public class ProfessionalEmailTemplateService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final String COMPANY_NAME = "PowerGrid IT Support";
    private static final String SUPPORT_EMAIL = "harinimadhan2005@gmail.com";

    public static String generateNewTicketEmail(Ticket ticket) {
        String ticketId = ticket.getTicketId() != null ? ticket.getTicketId() : "N/A";
        String sender = ticket.getSenderEmail() != null ? ticket.getSenderEmail() : "Unknown";
        String subject = ticket.getEmailSubject() != null ? ticket.getEmailSubject() : "No Subject";
        String category = ticket.getCategory() != null ? ticket.getCategory() : "General";
        String priority = ticket.getPriority() != null ? ticket.getPriority() : "MEDIUM";
        String createdTime = ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT) : "N/A";
        String description = ticket.getIssueDescription() != null ? ticket.getIssueDescription() : "No description";

        return "====================================================\n" +
                "TICKET CREATED - ACTION REQUIRED\n" +
                "====================================================\n\n" +
                "Dear Team,\n\n" +
                "A new support ticket has been received and needs attention.\n\n" +
                "----------------------------------------------------\n" +
                "TICKET DETAILS\n" +
                "----------------------------------------------------\n\n" +
                "Ticket ID:    " + ticketId + "\n" +
                "From:         " + sender + "\n" +
                "Subject:      " + subject + "\n" +
                "Category:     " + category + "\n" +
                "Priority:     " + priority + "\n" +
                "Created:      " + createdTime + "\n\n" +
                "----------------------------------------------------\n" +
                "ISSUE DESCRIPTION\n" +
                "----------------------------------------------------\n\n" +
                description + "\n\n" +
                "----------------------------------------------------\n" +
                "NEXT STEPS\n" +
                "----------------------------------------------------\n\n" +
                "1. Review the ticket details above\n" +
                "2. Assign to appropriate team lead or engineer\n" +
                "3. Monitor ticket status and progress\n" +
                "4. Update status as ticket moves through workflow\n\n" +
                "====================================================\n" +
                "Email: " + SUPPORT_EMAIL + "\n" +
                "Company: " + COMPANY_NAME + "\n" +
                "This is an automated notification. Do not reply.\n" +
                "====================================================\n";
    }

    public static String generateTicketAssignedEmail(Ticket ticket, String assignedTo) {
        String ticketId = ticket.getTicketId() != null ? ticket.getTicketId() : "N/A";
        String sender = ticket.getSenderEmail() != null ? ticket.getSenderEmail() : "Unknown";
        String subject = ticket.getEmailSubject() != null ? ticket.getEmailSubject() : "No Subject";
        String priority = ticket.getPriority() != null ? ticket.getPriority() : "MEDIUM";
        String createdTime = ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT) : "N/A";
        String slaDeadline = ticket.getSlaDeadline() != null ? ticket.getSlaDeadline().format(DATE_FORMAT) : "N/A";

        return "====================================================\n" +
                "TICKET ASSIGNED TO YOU\n" +
                "====================================================\n\n" +
                "Hi " + assignedTo + ",\n\n" +
                "A support ticket has been assigned to you.\n\n" +
                "----------------------------------------------------\n" +
                "TICKET INFORMATION\n" +
                "----------------------------------------------------\n\n" +
                "Ticket ID:    " + ticketId + "\n" +
                "From:         " + sender + "\n" +
                "Subject:      " + subject + "\n" +
                "Priority:     " + priority + "\n" +
                "Created:      " + createdTime + "\n" +
                "SLA Deadline: " + slaDeadline + "\n\n" +
                "----------------------------------------------------\n" +
                "ACTION REQUIRED\n" +
                "----------------------------------------------------\n\n" +
                "1. Review ticket details carefully\n" +
                "2. Start working on the issue immediately\n" +
                "3. Update ticket status as you progress\n" +
                "4. Provide resolution or escalate if needed\n\n" +
                "====================================================\n" +
                "Email: " + SUPPORT_EMAIL + "\n" +
                "Company: " + COMPANY_NAME + "\n" +
                "This is an automated notification. Do not reply.\n" +
                "====================================================\n";
    }

    public static String generateTicketEscalatedEmail(Ticket ticket, String escalatedTo) {
        String ticketId = ticket.getTicketId() != null ? ticket.getTicketId() : "N/A";
        String sender = ticket.getSenderEmail() != null ? ticket.getSenderEmail() : "Unknown";
        String subject = ticket.getEmailSubject() != null ? ticket.getEmailSubject() : "No Subject";
        String priority = ticket.getPriority() != null ? ticket.getPriority() : "HIGH";
        String escalationLevel = ticket.getEscalationLevel() != null ? ticket.getEscalationLevel() : "LEVEL_1";
        String escalationReason = ticket.getEscalationReason() != null ? ticket.getEscalationReason()
                : "Complex issue requiring expertise";
        String slaDeadline = ticket.getSlaDeadline() != null ? ticket.getSlaDeadline().format(DATE_FORMAT) : "N/A";

        return "====================================================\n" +
                "URGENT: TICKET ESCALATED\n" +
                "====================================================\n\n" +
                escalatedTo + ",\n\n" +
                "A support ticket requires immediate escalation.\n\n" +
                "----------------------------------------------------\n" +
                "ESCALATED TICKET DETAILS\n" +
                "----------------------------------------------------\n\n" +
                "Ticket ID:          " + ticketId + "\n" +
                "From:               " + sender + "\n" +
                "Subject:            " + subject + "\n" +
                "Priority:           " + priority + "\n" +
                "Escalation Level:   " + escalationLevel + "\n" +
                "SLA Deadline:       " + slaDeadline + "\n\n" +
                "----------------------------------------------------\n" +
                "ESCALATION REASON\n" +
                "----------------------------------------------------\n\n" +
                escalationReason + "\n\n" +
                "----------------------------------------------------\n" +
                "IMMEDIATE ACTION REQUIRED\n" +
                "----------------------------------------------------\n\n" +
                "1. URGENTLY review all ticket details\n" +
                "2. Determine appropriate resolution approach\n" +
                "3. Allocate resources if necessary\n" +
                "4. Update ticket status immediately\n" +
                "5. Communicate with affected parties\n\n" +
                "====================================================\n" +
                "Email: " + SUPPORT_EMAIL + "\n" +
                "Company: " + COMPANY_NAME + "\n" +
                "This is an URGENT automated notification. Do not reply.\n" +
                "====================================================\n";
    }

    public static String generateTicketResolvedEmail(Ticket ticket) {
        String ticketId = ticket.getTicketId() != null ? ticket.getTicketId() : "N/A";
        String sender = ticket.getSenderEmail() != null ? ticket.getSenderEmail() : "Unknown";
        String subject = ticket.getEmailSubject() != null ? ticket.getEmailSubject() : "No Subject";
        String createdTime = ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT) : "N/A";
        String resolvedTime = LocalDateTime.now().format(DATE_FORMAT);
        String resolutionNotes = ticket.getResolutionNotes() != null ? ticket.getResolutionNotes() : "No notes";

        return "====================================================\n" +
                "TICKET RESOLVED\n" +
                "====================================================\n\n" +
                "Dear Team,\n\n" +
                "A support ticket has been successfully resolved.\n\n" +
                "----------------------------------------------------\n" +
                "TICKET DETAILS\n" +
                "----------------------------------------------------\n\n" +
                "Ticket ID:    " + ticketId + "\n" +
                "From:         " + sender + "\n" +
                "Subject:      " + subject + "\n" +
                "Status:       RESOLVED\n" +
                "Created:      " + createdTime + "\n" +
                "Resolved:     " + resolvedTime + "\n\n" +
                "----------------------------------------------------\n" +
                "RESOLUTION NOTES\n" +
                "----------------------------------------------------\n\n" +
                resolutionNotes + "\n\n" +
                "====================================================\n" +
                "Email: " + SUPPORT_EMAIL + "\n" +
                "Company: " + COMPANY_NAME + "\n" +
                "This is an automated notification. Do not reply.\n" +
                "====================================================\n";
    }

    public static String generateSLAWarningEmail(Ticket ticket) {
        String ticketId = ticket.getTicketId() != null ? ticket.getTicketId() : "N/A";
        String subject = ticket.getEmailSubject() != null ? ticket.getEmailSubject() : "No Subject";
        String priority = ticket.getPriority() != null ? ticket.getPriority() : "HIGH";
        String createdTime = ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT) : "N/A";
        String slaDeadline = ticket.getSlaDeadline() != null ? ticket.getSlaDeadline().format(DATE_FORMAT) : "N/A";

        return "====================================================\n" +
                "SLA BREACH WARNING\n" +
                "====================================================\n\n" +
                "Alert!\n\n" +
                "A support ticket is approaching SLA deadline.\n\n" +
                "----------------------------------------------------\n" +
                "TICKET DETAILS\n" +
                "----------------------------------------------------\n\n" +
                "Ticket ID:     " + ticketId + "\n" +
                "Subject:       " + subject + "\n" +
                "Priority:      " + priority + "\n" +
                "Created:       " + createdTime + "\n" +
                "SLA Deadline:  " + slaDeadline + "\n" +
                "Status:        NEARING SLA DEADLINE\n\n" +
                "----------------------------------------------------\n" +
                "URGENT ACTION REQUIRED\n" +
                "----------------------------------------------------\n\n" +
                "This ticket will breach SLA if not resolved immediately.\n" +
                "Please take immediate action to prevent SLA violation.\n\n" +
                "====================================================\n" +
                "Email: " + SUPPORT_EMAIL + "\n" +
                "Company: " + COMPANY_NAME + "\n" +
                "This is an URGENT automated alert. Do not reply.\n" +
                "====================================================\n";
    }

    // ============================================================
    // HTML EMAIL TEMPLATES (Professional Styling)
    // ============================================================

    /**
     * Generate professional HTML email for TICKET_RESOLVED event.
     * Uses gradient header and professional styling matching website UI.
     */
    public static String generateTicketResolvedEmailHtml(Ticket ticket) {
        String ticketId = ticket.getTicketId() != null ? ticket.getTicketId() : "N/A";
        String category = ticket.getCategory() != null ? ticket.getCategory() : "General";
        String priority = ticket.getPriority() != null ? ticket.getPriority() : "MEDIUM";
        String createdTime = ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT) : "N/A";
        String resolvedTime = LocalDateTime.now().format(DATE_FORMAT);
        String issueDescription = ticket.getIssueDescription() != null ? ticket.getIssueDescription()
                : "No description";
        String resolutionNotes = ticket.getResolutionNotes() != null ? ticket.getResolutionNotes()
                : "Issue has been resolved.";
        String resolvedBy = ticket.getAssignedEngineer() != null ? ticket.getAssignedEngineer() : "IT Support Team";

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Ticket Resolved - PowerGrid ITSM</title>
                    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background-color: #F4F5F7;">

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #1B2A4A 0%%%%, #1e3a5f 50%%%%, #1B2A4A 100%%%%); margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 32px 16px; text-align: center;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="text-align: center; padding: 0;">
                                            <div style="width: 48px; height: 48px; background: #1B2A4A; border-radius: 12px; display: inline-flex; align-items: center; justify-content: center; margin-bottom: 12px; font-size: 24px;">✅</div>
                                            <h1 style="margin: 8px 0 4px 0; color: white; font-size: 20px; font-weight: 700; letter-spacing: -0.3px;">Ticket Resolved</h1>
                                            <p style="margin: 0; color: rgba(255,255,255,0.85); font-size: 13px;">PowerGrid IT Service Management</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #f5f7fa 0%%%%, #c3cfe2 100%%%%); margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 0;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="padding: 32px;">
                                            <p style="margin: 0 0 16px 0; font-size: 14px; line-height: 1.6; color: #172B4D;">
                                                Hello,<br><br>
                                                We are pleased to inform you that your IT support ticket has been resolved by our technical team.
                                            </p>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; text-transform: uppercase; margin-bottom: 12px; letter-spacing: 0.3px; padding-bottom: 8px; border-bottom: 2px solid #1B2A4A;">📋 Ticket Details</div>
                                                <table width="100%%%%" cellpadding="0" cellspacing="0" style="border-collapse: collapse;">
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; width: 120px; font-size: 13px;">Ticket ID</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Category</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Priority</td>
                                                        <td style="color: #172B4D; font-size: 13px;"><span style="display: inline-block; background: rgba(255, 153, 31, 0.1); color: #FF991F; padding: 4px 10px; border-radius: 12px; font-weight: 600; font-size: 11px; text-transform: uppercase;">%s</span></td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Status</td>
                                                        <td style="color: #172B4D; font-size: 13px;"><span style="display: inline-block; background: rgba(54, 179, 126, 0.1); color: #36B37E; padding: 4px 10px; border-radius: 12px; font-weight: 600; font-size: 11px; text-transform: uppercase;">RESOLVED</span></td>
                                                    </tr>
                                                </table>
                                            </div>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; text-transform: uppercase; margin-bottom: 12px; letter-spacing: 0.3px; padding-bottom: 8px; border-bottom: 2px solid #1B2A4A;">📝 Original Issue</div>
                                                <p style="margin: 12px 0; padding: 12px; background: rgba(27, 42, 74, 0.05); border-radius: 8px; color: #172B4D; font-size: 13px; line-height: 1.5;">%s</p>
                                            </div>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; margin-bottom: 8px; text-transform: uppercase; letter-spacing: 0.3px;">✨ Resolution Details</div>
                                                <p style="margin: 0; font-size: 13px; color: #172B4D; line-height: 1.6;"><strong>Resolved By:</strong> %s<br><strong>Resolution:</strong> %s</p>
                                            </div>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; text-transform: uppercase; margin-bottom: 12px; letter-spacing: 0.3px; padding-bottom: 8px; border-bottom: 2px solid #1B2A4A;">📊 Service Metrics</div>
                                                <table width="100%%%%" cellpadding="0" cellspacing="0" style="border-collapse: collapse;">
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; width: 120px; font-size: 13px;">Created</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Resolved</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                </table>
                                            </div>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: #F4F5F7; margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 24px 16px; text-align: center;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="text-align: center; padding: 0;">
                                            <p style="margin: 8px 0; font-size: 12px; font-weight: 600; color: #172B4D;"><strong>PowerGrid IT Support</strong></p>
                                            <p style="margin: 4px 0; font-size: 11px; color: #97A0AF;">This is an automated notification. Please do not reply directly to this email.</p>
                                            <p style="margin: 4px 0; font-size: 11px; color: #97A0AF;">For support, contact: <strong>support@powergrid.com</strong></p>
                                            <p style="margin: 8px 0 0 0; font-size: 10px; color: #97A0AF;">© 2026 PowerGrid Corporation of India. All rights reserved.</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                </body>
                </html>
                """
                .formatted(ticketId, category, priority, issueDescription, resolvedBy, resolutionNotes, createdTime,
                        resolvedTime);
    }

    /**
     * Generate professional HTML email for NEW TICKET CREATED.
     * Sent to the employee who created the ticket.
     */
    public static String generateNewTicketEmailHtml(Ticket ticket) {
        String ticketId = ticket.getTicketId() != null ? ticket.getTicketId() : "N/A";
        String subject = ticket.getEmailSubject() != null ? ticket.getEmailSubject() : "No Subject";
        String category = ticket.getCategory() != null ? ticket.getCategory() : "General";
        String priority = ticket.getPriority() != null ? ticket.getPriority() : "MEDIUM";
        String createdTime = ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT) : "N/A";
        String description = ticket.getIssueDescription() != null ? ticket.getIssueDescription() : "No description";

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Your Support Ticket Created - PowerGrid ITSM</title>
                    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background-color: #F4F5F7;">

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #1B2A4A 0%%%%, #1e3a5f 50%%%%, #1B2A4A 100%%%%); margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 32px 16px; text-align: center;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="text-align: center; padding: 0;">
                                            <div style="width: 48px; height: 48px; background: #1B2A4A; border-radius: 12px; display: inline-flex; align-items: center; justify-content: center; margin-bottom: 12px; font-size: 24px;">📋</div>
                                            <h1 style="margin: 8px 0 4px 0; color: white; font-size: 20px; font-weight: 700; letter-spacing: -0.3px;">Ticket Created</h1>
                                            <p style="margin: 0; color: rgba(255,255,255,0.85); font-size: 13px;">PowerGrid IT Service Management</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #f5f7fa 0%%%%, #c3cfe2 100%%%%); margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 0;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="padding: 32px;">
                                            <p style="margin: 0 0 16px 0; font-size: 14px; line-height: 1.6; color: #172B4D;">
                                                Hello,<br><br>
                                                Your IT support ticket has been successfully created and registered in our system. Our team will review and respond to your issue shortly.
                                            </p>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; text-transform: uppercase; margin-bottom: 12px; letter-spacing: 0.3px; padding-bottom: 8px; border-bottom: 2px solid #1B2A4A;">📋 Ticket Details</div>
                                                <table width="100%%%%" cellpadding="0" cellspacing="0" style="border-collapse: collapse;">
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; width: 120px; font-size: 13px;">Ticket ID</td>
                                                        <td style="color: #172B4D; font-size: 13px;"><strong>%s</strong></td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Subject</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Category</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Priority</td>
                                                        <td style="color: #172B4D; font-size: 13px;"><span style="display: inline-block; background: rgba(255, 153, 31, 0.1); color: #FF991F; padding: 4px 10px; border-radius: 12px; font-weight: 600; font-size: 11px; text-transform: uppercase;">%s</span></td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Created</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                </table>
                                            </div>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; text-transform: uppercase; margin-bottom: 12px; letter-spacing: 0.3px; padding-bottom: 8px; border-bottom: 2px solid #1B2A4A;">📝 Issue Description</div>
                                                <p style="margin: 12px 0; padding: 12px; background: rgba(27, 42, 74, 0.05); border-radius: 8px; color: #172B4D; font-size: 13px; line-height: 1.5;">%s</p>
                                            </div>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; margin-bottom: 8px; text-transform: uppercase; letter-spacing: 0.3px;">✅ What's Next</div>
                                                <p style="margin: 0; font-size: 13px; color: #172B4D; line-height: 1.6;">Our IT Support team will review your request and assign it to an appropriate engineer. You'll receive an update email once your ticket is assigned.</p>
                                            </div>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: #F4F5F7; margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 24px 16px; text-align: center;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="text-align: center; padding: 0;">
                                            <p style="margin: 8px 0; font-size: 12px; font-weight: 600; color: #172B4D;"><strong>PowerGrid IT Support</strong></p>
                                            <p style="margin: 4px 0; font-size: 11px; color: #97A0AF;">For support, contact: <strong>support@powergrid.com</strong></p>
                                            <p style="margin: 8px 0 0 0; font-size: 10px; color: #97A0AF;">© 2026 PowerGrid Corporation of India. All rights reserved.</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                </body>
                </html>
                """
                .formatted(ticketId, subject, category, priority, createdTime, description);
    }

    /**
     * Generate professional HTML email for SLA WARNING.
     * Sent when ticket reaches 75%% of SLA time.
     */
    public static String generateSlaWarningEmailHtml(Ticket ticket) {
        String ticketId = ticket.getTicketId() != null ? ticket.getTicketId() : "N/A";
        String category = ticket.getCategory() != null ? ticket.getCategory() : "General";
        String priority = ticket.getPriority() != null ? ticket.getPriority() : "MEDIUM";
        String createdTime = ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT) : "N/A";
        String slaDeadline = ticket.getSlaDeadline() != null ? ticket.getSlaDeadline().format(DATE_FORMAT) : "N/A";

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>SLA Warning - PowerGrid ITSM</title>
                    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background-color: #F4F5F7;">

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #1B2A4A 0%%%%, #1e3a5f 50%%%%, #1B2A4A 100%%%%); margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 32px 16px; text-align: center;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="text-align: center; padding: 0;">
                                            <div style="width: 48px; height: 48px; background: #1B2A4A; border-radius: 12px; display: inline-flex; align-items: center; justify-content: center; margin-bottom: 12px; font-size: 24px;">⏰</div>
                                            <h1 style="margin: 8px 0 4px 0; color: white; font-size: 20px; font-weight: 700; letter-spacing: -0.3px;">SLA Warning</h1>
                                            <p style="margin: 0; color: rgba(255,255,255,0.85); font-size: 13px;">PowerGrid IT Service Management</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #f5f7fa 0%%%%, #c3cfe2 100%%%%); margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 0;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="padding: 32px;">
                                            <p style="margin: 0 0 16px 0; font-size: 14px; line-height: 1.6; color: #172B4D;">
                                                Hello,<br><br>
                                                This is a courtesy reminder that the following ticket is approaching its SLA deadline. Please take immediate action to resolve this issue.
                                            </p>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; margin-bottom: 8px; text-transform: uppercase; letter-spacing: 0.3px;">⚠️ SLA Status</div>
                                                <p style="margin: 0; font-size: 13px; color: #E67E22; line-height: 1.6;"><strong>75%% of SLA time used</strong><br>You have limited time remaining to resolve this ticket.</p>
                                            </div>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; text-transform: uppercase; margin-bottom: 12px; letter-spacing: 0.3px; padding-bottom: 8px; border-bottom: 2px solid #1B2A4A;">📋 Ticket Details</div>
                                                <table width="100%%%%" cellpadding="0" cellspacing="0" style="border-collapse: collapse;">
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; width: 120px; font-size: 13px;">Ticket ID</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Category</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Priority</td>
                                                        <td style="color: #172B4D; font-size: 13px;"><span style="display: inline-block; background: rgba(255, 153, 31, 0.1); color: #FF991F; padding: 4px 10px; border-radius: 12px; font-weight: 600; font-size: 11px; text-transform: uppercase;">%s</span></td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Created</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">SLA Deadline</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                </table>
                                            </div>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; margin-bottom: 8px; text-transform: uppercase; letter-spacing: 0.3px;">✅ Recommended Actions</div>
                                                <p style="margin: 0; font-size: 13px; color: #172B4D; line-height: 1.8;">1. Review ticket status and blockers<br>2. Accelerate resolution efforts<br>3. Escalate if additional resources needed<br>4. Update ticket with progress</p>
                                            </div>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: #F4F5F7; margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 24px 16px; text-align: center;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="text-align: center; padding: 0;">
                                            <p style="margin: 8px 0; font-size: 12px; font-weight: 600; color: #172B4D;"><strong>PowerGrid IT Support</strong></p>
                                            <p style="margin: 4px 0; font-size: 11px; color: #97A0AF;">This is an SLA warning notification.</p>
                                            <p style="margin: 4px 0; font-size: 11px; color: #97A0AF;">For support, contact: <strong>support@powergrid.com</strong></p>
                                            <p style="margin: 8px 0 0 0; font-size: 10px; color: #97A0AF;">© 2026 PowerGrid Corporation of India. All rights reserved.</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                </body>
                </html>
                """
                .formatted(ticketId, category, priority, createdTime, slaDeadline);
    }

    /**
     * Generate professional HTML email for SOLUTION DELIVERY (self-service).
     * Used when a knowledge base solution is delivered to the user.
     */
    public static String generateSolutionEmailHtml(Ticket ticket, String solutionSteps) {
        String ticketId = ticket.getTicketId() != null ? ticket.getTicketId() : "N/A";
        String category = ticket.getCategory() != null ? ticket.getCategory() : "General";
        String priority = ticket.getPriority() != null ? ticket.getPriority() : "MEDIUM";
        String createdTime = ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT) : "N/A";
        String issueDescription = ticket.getIssueDescription() != null ? ticket.getIssueDescription()
                : "No description";
        String steps = solutionSteps != null && !solutionSteps.isEmpty() ? solutionSteps
                : "Please contact IT support for assistance.";

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Solution for Your IT Issue - PowerGrid ITSM</title>
                    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background-color: #F4F5F7;">

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #1B2A4A 0%%%%, #1e3a5f 50%%%%, #1B2A4A 100%%%%); margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 32px 16px; text-align: center;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="text-align: center; padding: 0;">
                                            <div style="width: 48px; height: 48px; background: #1B2A4A; border-radius: 12px; display: inline-flex; align-items: center; justify-content: center; margin-bottom: 12px; font-size: 24px;">💡</div>
                                            <h1 style="margin: 8px 0 4px 0; color: white; font-size: 20px; font-weight: 700; letter-spacing: -0.3px;">Solution Available</h1>
                                            <p style="margin: 0; color: rgba(255,255,255,0.85); font-size: 13px;">PowerGrid IT Service Management</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #f5f7fa 0%%%%, #c3cfe2 100%%%%); margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 0;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="padding: 32px;">
                                            <p style="margin: 0 0 16px 0; font-size: 14px; line-height: 1.6; color: #172B4D;">
                                                Hello,<br><br>
                                                We've detected an issue similar to one in our knowledge base and have a solution for you. Please try the following steps to resolve your issue.
                                            </p>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; text-transform: uppercase; margin-bottom: 12px; letter-spacing: 0.3px; padding-bottom: 8px; border-bottom: 2px solid #1B2A4A;">📋 Issue & Ticket Info</div>
                                                <table width="100%%%%" cellpadding="0" cellspacing="0" style="border-collapse: collapse;">
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; width: 120px; font-size: 13px;">Ticket ID</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Category</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Priority</td>
                                                        <td style="color: #172B4D; font-size: 13px;"><span style="display: inline-block; background: rgba(255, 153, 31, 0.1); color: #FF991F; padding: 4px 10px; border-radius: 12px; font-weight: 600; font-size: 11px; text-transform: uppercase;">%s</span></td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Created On</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                </table>
                                            </div>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; text-transform: uppercase; margin-bottom: 12px; letter-spacing: 0.3px; padding-bottom: 8px; border-bottom: 2px solid #1B2A4A;">📝 Issue Description</div>
                                                <p style="margin: 12px 0; padding: 12px; background: rgba(27, 42, 74, 0.05); border-radius: 8px; color: #172B4D; font-size: 13px; line-height: 1.5;">%s</p>
                                            </div>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; margin-bottom: 12px; text-transform: uppercase; letter-spacing: 0.3px;">🛠️ Solution Steps</div>
                                                <div style="font-size: 13px; color: #172B4D; line-height: 1.8;">%s</div>
                                            </div>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; margin-bottom: 8px; text-transform: uppercase; letter-spacing: 0.3px;">✅ Next Steps</div>
                                                <p style="margin: 0; font-size: 13px; color: #2E7D32; line-height: 1.6;">Please try the above steps. If the issue is resolved, reply <strong>YES</strong>. If you still need help, reply <strong>NO</strong>.</p>
                                            </div>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: #F4F5F7; margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 24px 16px; text-align: center;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="text-align: center; padding: 0;">
                                            <p style="margin: 8px 0; font-size: 12px; font-weight: 600; color: #172B4D;"><strong>PowerGrid IT Support</strong></p>
                                            <p style="margin: 4px 0; font-size: 11px; color: #97A0AF;">This is an automated solution suggestion. Please do not reply directly to this email.</p>
                                            <p style="margin: 4px 0; font-size: 11px; color: #97A0AF;">For additional support, contact: <strong>support@powergrid.com</strong></p>
                                            <p style="margin: 8px 0 0 0; font-size: 10px; color: #97A0AF;">© 2026 PowerGrid Corporation of India. All rights reserved.</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                </body>
                </html>
                """
                .formatted(ticketId, category, priority, createdTime, issueDescription, steps);
    }

    /**
     * Generate professional HTML email for TICKET ASSIGNED.
     * Sent to engineer when ticket is assigned.
     */
    public static String generateTicketAssignedEmailHtml(Ticket ticket) {
        String ticketId = ticket.getTicketId() != null ? ticket.getTicketId() : "N/A";
        String subject = ticket.getEmailSubject() != null ? ticket.getEmailSubject() : "No Subject";
        String category = ticket.getCategory() != null ? ticket.getCategory() : "General";
        String priority = ticket.getPriority() != null ? ticket.getPriority() : "MEDIUM";
        String createdTime = ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT) : "N/A";
        String slaDeadline = ticket.getSlaDeadline() != null ? ticket.getSlaDeadline().format(DATE_FORMAT) : "N/A";
        String description = ticket.getIssueDescription() != null ? ticket.getIssueDescription() : "No description";

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Ticket Assigned - PowerGrid ITSM</title>
                    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background-color: #F4F5F7;">

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #1B2A4A 0%%%%, #1e3a5f 50%%%%, #1B2A4A 100%%%%); margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 32px 16px; text-align: center;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="text-align: center; padding: 0;">
                                            <div style="width: 48px; height: 48px; background: #1B2A4A; border-radius: 12px; display: inline-flex; align-items: center; justify-content: center; margin-bottom: 12px; font-size: 24px;">📌</div>
                                            <h1 style="margin: 8px 0 4px 0; color: white; font-size: 20px; font-weight: 700; letter-spacing: -0.3px;">Ticket Assigned</h1>
                                            <p style="margin: 0; color: rgba(255,255,255,0.85); font-size: 13px;">PowerGrid IT Service Management</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #f5f7fa 0%%%%, #c3cfe2 100%%%%); margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 0;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="padding: 32px;">
                                            <p style="margin: 0 0 16px 0; font-size: 14px; line-height: 1.6; color: #172B4D;">
                                                Hello,<br><br>
                                                A new IT support ticket has been assigned to you. Please review and begin working on this ticket at your earliest convenience.
                                            </p>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; text-transform: uppercase; margin-bottom: 12px; letter-spacing: 0.3px; padding-bottom: 8px; border-bottom: 2px solid #1B2A4A;">📋 Ticket Details</div>
                                                <table width="100%%%%" cellpadding="0" cellspacing="0" style="border-collapse: collapse;">
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; width: 120px; font-size: 13px;">Ticket ID</td>
                                                        <td style="color: #172B4D; font-size: 13px;"><strong>%s</strong></td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Subject</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Category</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Priority</td>
                                                        <td style="color: #172B4D; font-size: 13px;"><span style="display: inline-block; background: rgba(255, 153, 31, 0.1); color: #FF991F; padding: 4px 10px; border-radius: 12px; font-weight: 600; font-size: 11px; text-transform: uppercase;">%s</span></td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Created</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">SLA Deadline</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                </table>
                                            </div>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; text-transform: uppercase; margin-bottom: 12px; letter-spacing: 0.3px; padding-bottom: 8px; border-bottom: 2px solid #1B2A4A;">📝 Issue Description</div>
                                                <p style="margin: 12px 0; padding: 12px; background: rgba(27, 42, 74, 0.05); border-radius: 8px; color: #172B4D; font-size: 13px; line-height: 1.5;">%s</p>
                                            </div>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; margin-bottom: 8px; text-transform: uppercase; letter-spacing: 0.3px;">✅ Action Required</div>
                                                <p style="margin: 0; font-size: 13px; color: #172B4D; line-height: 1.6;">1. Review ticket details<br>2. Start working on the issue<br>3. Update status as you progress<br>4. Request help if needed</p>
                                            </div>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: #F4F5F7; margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 24px 16px; text-align: center;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="text-align: center; padding: 0;">
                                            <p style="margin: 8px 0; font-size: 12px; font-weight: 600; color: #172B4D;"><strong>PowerGrid IT Support</strong></p>
                                            <p style="margin: 4px 0; font-size: 11px; color: #97A0AF;">For support, contact: <strong>support@powergrid.com</strong></p>
                                            <p style="margin: 8px 0 0 0; font-size: 10px; color: #97A0AF;">© 2026 PowerGrid Corporation of India. All rights reserved.</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                </body>
                </html>
                """
                .formatted(ticketId, subject, category, priority, createdTime, slaDeadline, description);
    }

    /**
     * Generate professional HTML email for TICKET CLOSED.
     * Sent to employee when ticket is closed after verification.
     */
    public static String generateTicketClosedEmailHtml(Ticket ticket) {
        String ticketId = ticket.getTicketId() != null ? ticket.getTicketId() : "N/A";
        String category = ticket.getCategory() != null ? ticket.getCategory() : "General";
        String priority = ticket.getPriority() != null ? ticket.getPriority() : "MEDIUM";
        String createdTime = ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT) : "N/A";
        String closedTime = LocalDateTime.now().format(DATE_FORMAT);
        String description = ticket.getIssueDescription() != null ? ticket.getIssueDescription() : "No description";
        String resolution = ticket.getResolutionNotes() != null ? ticket.getResolutionNotes() : "Issue resolved";

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Ticket Closed - PowerGrid ITSM</title>
                    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background-color: #F4F5F7;">

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #1B2A4A 0%%%%, #1e3a5f 50%%%%, #1B2A4A 100%%%%); margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 32px 16px; text-align: center;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="text-align: center; padding: 0;">
                                            <div style="width: 48px; height: 48px; background: #1B2A4A; border-radius: 12px; display: inline-flex; align-items: center; justify-content: center; margin-bottom: 12px; font-size: 24px;">🎉</div>
                                            <h1 style="margin: 8px 0 4px 0; color: white; font-size: 20px; font-weight: 700; letter-spacing: -0.3px;">Ticket Closed</h1>
                                            <p style="margin: 0; color: rgba(255,255,255,0.85); font-size: 13px;">PowerGrid IT Service Management</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #f5f7fa 0%%%%, #c3cfe2 100%%%%); margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 0;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="padding: 32px;">
                                            <p style="margin: 0 0 16px 0; font-size: 14px; line-height: 1.6; color: #172B4D;">
                                                Hello,<br><br>
                                                Your IT support ticket has been verified and is now officially CLOSED. Thank you for using our service!
                                            </p>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; text-transform: uppercase; margin-bottom: 12px; letter-spacing: 0.3px; padding-bottom: 8px; border-bottom: 2px solid #1B2A4A;">📋 Ticket Summary</div>
                                                <table width="100%%%%" cellpadding="0" cellspacing="0" style="border-collapse: collapse;">
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; width: 120px; font-size: 13px;">Ticket ID</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Category</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Priority</td>
                                                        <td style="color: #172B4D; font-size: 13px;"><span style="display: inline-block; background: rgba(54, 179, 126, 0.1); color: #36B37E; padding: 4px 10px; border-radius: 12px; font-weight: 600; font-size: 11px; text-transform: uppercase;">%s</span></td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Status</td>
                                                        <td style="color: #172B4D; font-size: 13px;"><span style="display: inline-block; background: rgba(54, 179, 126, 0.1); color: #36B37E; padding: 4px 10px; border-radius: 12px; font-weight: 600; font-size: 11px; text-transform: uppercase;">CLOSED</span></td>
                                                    </tr>
                                                </table>
                                            </div>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; text-transform: uppercase; margin-bottom: 12px; letter-spacing: 0.3px; padding-bottom: 8px; border-bottom: 2px solid #1B2A4A;">📝 Issue & Resolution</div>
                                                <p style="margin: 0 0 12px 0; padding: 12px; background: rgba(27, 42, 74, 0.05); border-radius: 8px; color: #172B4D; font-size: 13px; line-height: 1.5;"><strong>Issue:</strong> %s</p>
                                                <p style="margin: 0; padding: 12px; background: rgba(27, 42, 74, 0.05); border-radius: 8px; color: #172B4D; font-size: 13px; line-height: 1.5;"><strong>Resolution:</strong> %s</p>
                                            </div>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; text-transform: uppercase; margin-bottom: 12px; letter-spacing: 0.3px; padding-bottom: 8px; border-bottom: 2px solid #1B2A4A;">📊 Timeline</div>
                                                <table width="100%%%%" cellpadding="0" cellspacing="0" style="border-collapse: collapse;">
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; width: 120px; font-size: 13px;">Created</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Closed</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                </table>
                                            </div>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: #F4F5F7; margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 24px 16px; text-align: center;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="text-align: center; padding: 0;">
                                            <p style="margin: 8px 0; font-size: 12px; font-weight: 600; color: #172B4D;"><strong>PowerGrid IT Support</strong></p>
                                            <p style="margin: 4px 0; font-size: 11px; color: #97A0AF;">Thank you for using our IT support services!</p>
                                            <p style="margin: 8px 0 0 0; font-size: 10px; color: #97A0AF;">© 2026 PowerGrid Corporation of India. All rights reserved.</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                </body>
                </html>
                """
                .formatted(ticketId, category, priority, description, resolution, createdTime, closedTime);
    }

    /**
     * Generate professional HTML email for TICKET REOPENED.
     * Sent when ticket fails verification and needs more work.
     */
    public static String generateTicketReopenedEmailHtml(Ticket ticket) {
        String ticketId = ticket.getTicketId() != null ? ticket.getTicketId() : "N/A";
        String category = ticket.getCategory() != null ? ticket.getCategory() : "General";
        String priority = ticket.getPriority() != null ? ticket.getPriority() : "MEDIUM";
        String createdTime = ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT) : "N/A";
        String description = ticket.getIssueDescription() != null ? ticket.getIssueDescription() : "No description";

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Ticket Reopened - PowerGrid ITSM</title>
                    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background-color: #F4F5F7;">

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #1B2A4A 0%%%%, #1e3a5f 50%%%%, #1B2A4A 100%%%%); margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 32px 16px; text-align: center;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="text-align: center; padding: 0;">
                                            <div style="width: 48px; height: 48px; background: #1B2A4A; border-radius: 12px; display: inline-flex; align-items: center; justify-content: center; margin-bottom: 12px; font-size: 24px;">🔄</div>
                                            <h1 style="margin: 8px 0 4px 0; color: white; font-size: 20px; font-weight: 700; letter-spacing: -0.3px;">Ticket Reopened</h1>
                                            <p style="margin: 0; color: rgba(255,255,255,0.85); font-size: 13px;">PowerGrid IT Service Management</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #f5f7fa 0%%%%, #c3cfe2 100%%%%); margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 0;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="padding: 32px;">
                                            <p style="margin: 0 0 16px 0; font-size: 14px; line-height: 1.6; color: #172B4D;">
                                                Hello,<br><br>
                                                Your IT support ticket has been reopened. The verification process detected that additional work is needed to fully resolve this issue.
                                            </p>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; margin-bottom: 8px; text-transform: uppercase; letter-spacing: 0.3px;">⚠️ Ticket Reopened</div>
                                                <p style="margin: 0; font-size: 13px; color: #E67E22; line-height: 1.6;">The ticket has been reopened for further investigation and resolution.</p>
                                            </div>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; text-transform: uppercase; margin-bottom: 12px; letter-spacing: 0.3px; padding-bottom: 8px; border-bottom: 2px solid #1B2A4A;">📋 Ticket Details</div>
                                                <table width="100%%%%" cellpadding="0" cellspacing="0" style="border-collapse: collapse;">
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; width: 120px; font-size: 13px;">Ticket ID</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Category</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Priority</td>
                                                        <td style="color: #172B4D; font-size: 13px;"><span style="display: inline-block; background: rgba(255, 153, 31, 0.1); color: #FF991F; padding: 4px 10px; border-radius: 12px; font-weight: 600; font-size: 11px; text-transform: uppercase;">%s</span></td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Status</td>
                                                        <td style="color: #172B4D; font-size: 13px;"><span style="display: inline-block; background: rgba(255, 153, 31, 0.1); color: #FF991F; padding: 4px 10px; border-radius: 12px; font-weight: 600; font-size: 11px; text-transform: uppercase;">REOPENED</span></td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Created</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                </table>
                                            </div>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; text-transform: uppercase; margin-bottom: 12px; letter-spacing: 0.3px; padding-bottom: 8px; border-bottom: 2px solid #1B2A4A;">📝 Issue Description</div>
                                                <p style="margin: 12px 0; padding: 12px; background: rgba(27, 42, 74, 0.05); border-radius: 8px; color: #172B4D; font-size: 13px; line-height: 1.5;">%s</p>
                                            </div>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: #F4F5F7; margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 24px 16px; text-align: center;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="text-align: center; padding: 0;">
                                            <p style="margin: 8px 0; font-size: 12px; font-weight: 600; color: #172B4D;"><strong>PowerGrid IT Support</strong></p>
                                            <p style="margin: 4px 0; font-size: 11px; color: #97A0AF;">For support, contact: <strong>support@powergrid.com</strong></p>
                                            <p style="margin: 8px 0 0 0; font-size: 10px; color: #97A0AF;">© 2026 PowerGrid Corporation of India. All rights reserved.</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                </body>
                </html>
                """
                .formatted(ticketId, category, priority, createdTime, description);
    }

    /**
     * Generate professional HTML email for TEAM LEAD ESCALATION.
     * Sent to team lead when engineer escalates ticket.
     */
    public static String generateTeamLeadEscalationEmailHtml(Ticket ticket, String escalationReason) {
        String ticketId = ticket.getTicketId() != null ? ticket.getTicketId() : "N/A";
        String priority = ticket.getPriority() != null ? ticket.getPriority() : "HIGH";
        String category = ticket.getCategory() != null ? ticket.getCategory() : "General";
        String createdTime = ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT) : "N/A";
        String description = ticket.getIssueDescription() != null ? ticket.getIssueDescription() : "No description";
        String reason = escalationReason != null ? escalationReason : "Requires team lead attention";

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Team Lead Escalation - PowerGrid ITSM</title>
                    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background-color: #F4F5F7;">

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #1B2A4A 0%%%%, #1e3a5f 50%%%%, #1B2A4A 100%%%%); margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 32px 16px; text-align: center;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="text-align: center; padding: 0;">
                                            <div style="width: 48px; height: 48px; background: #1B2A4A; border-radius: 12px; display: inline-flex; align-items: center; justify-content: center; margin-bottom: 12px; font-size: 24px;">🆙</div>
                                            <h1 style="margin: 8px 0 4px 0; color: white; font-size: 20px; font-weight: 700; letter-spacing: -0.3px;">Team Lead Required</h1>
                                            <p style="margin: 0; color: rgba(255,255,255,0.85); font-size: 13px;">PowerGrid IT Service Management</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #f5f7fa 0%%%%, #c3cfe2 100%%%%); margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 0;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="padding: 32px;">
                                            <p style="margin: 0 0 16px 0; font-size: 14px; line-height: 1.6; color: #172B4D;">
                                                Hello,<br><br>
                                                A ticket has been escalated to you for team lead attention. An engineer requires your support to resolve this issue.
                                            </p>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; margin-bottom: 8px; text-transform: uppercase; letter-spacing: 0.3px;">🔺 Escalation Alert</div>
                                                <p style="margin: 0; font-size: 13px; color: #172B4D; line-height: 1.6;"><strong>Reason:</strong> %s</p>
                                            </div>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; text-transform: uppercase; margin-bottom: 12px; letter-spacing: 0.3px; padding-bottom: 8px; border-bottom: 2px solid #1B2A4A;">📋 Ticket Details</div>
                                                <table width="100%%%%" cellpadding="0" cellspacing="0" style="border-collapse: collapse;">
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; width: 120px; font-size: 13px;">Ticket ID</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Category</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Priority</td>
                                                        <td style="color: #172B4D; font-size: 13px;"><span style="display: inline-block; background: rgba(222, 53, 11, 0.1); color: #DE350B; padding: 4px 10px; border-radius: 12px; font-weight: 600; font-size: 11px; text-transform: uppercase;">%s</span></td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Created</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                </table>
                                            </div>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; text-transform: uppercase; margin-bottom: 12px; letter-spacing: 0.3px; padding-bottom: 8px; border-bottom: 2px solid #1B2A4A;">📝 Issue Description</div>
                                                <p style="margin: 12px 0; padding: 12px; background: rgba(27, 42, 74, 0.05); border-radius: 8px; color: #172B4D; font-size: 13px; line-height: 1.5;">%s</p>
                                            </div>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: #F4F5F7; margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 24px 16px; text-align: center;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="text-align: center; padding: 0;">
                                            <p style="margin: 8px 0; font-size: 12px; font-weight: 600; color: #172B4D;"><strong>PowerGrid IT Support</strong></p>
                                            <p style="margin: 4px 0; font-size: 11px; color: #97A0AF;">For urgent matters, contact: <strong>support@powergrid.com</strong></p>
                                            <p style="margin: 8px 0 0 0; font-size: 10px; color: #97A0AF;">© 2026 PowerGrid Corporation of India. All rights reserved.</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                </body>
                </html>
                """
                .formatted(reason, ticketId, category, priority, createdTime, description);
    }

    /**
     * Generate professional HTML email for EMPLOYEE ESCALATION NOTIFICATION.
     * Sent to employee when their ticket is escalated.
     */
    public static String generateEmployeeEscalationEmailHtml(Ticket ticket) {
        String ticketId = ticket.getTicketId() != null ? ticket.getTicketId() : "N/A";
        String priority = ticket.getPriority() != null ? ticket.getPriority() : "HIGH";
        String category = ticket.getCategory() != null ? ticket.getCategory() : "General";
        String createdTime = ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT) : "N/A";
        String description = ticket.getIssueDescription() != null ? ticket.getIssueDescription() : "No description";

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Ticket Escalated - PowerGrid ITSM</title>
                    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background-color: #F4F5F7;">

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #1B2A4A 0%%%%, #1e3a5f 50%%%%, #1B2A4A 100%%%%); margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 32px 16px; text-align: center;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="text-align: center; padding: 0;">
                                            <div style="width: 48px; height: 48px; background: #1B2A4A; border-radius: 12px; display: inline-flex; align-items: center; justify-content: center; margin-bottom: 12px; font-size: 24px;">📢</div>
                                            <h1 style="margin: 8px 0 4px 0; color: white; font-size: 20px; font-weight: 700; letter-spacing: -0.3px;">Ticket Escalated</h1>
                                            <p style="margin: 0; color: rgba(255,255,255,0.85); font-size: 13px;">PowerGrid IT Service Management</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #f5f7fa 0%%%%, #c3cfe2 100%%%%); margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 0;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="padding: 32px;">
                                            <p style="margin: 0 0 16px 0; font-size: 14px; line-height: 1.6; color: #172B4D;">
                                                Hello,<br><br>
                                                Your IT support ticket has been escalated to our senior technical team. This means your issue is being given higher priority for faster resolution.
                                            </p>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; margin-bottom: 8px; text-transform: uppercase; letter-spacing: 0.3px;">✅ Good News</div>
                                                <p style="margin: 0; font-size: 13px; color: #172B4D; line-height: 1.6;">Your ticket is now receiving priority attention from our senior team.</p>
                                            </div>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; text-transform: uppercase; margin-bottom: 12px; letter-spacing: 0.3px; padding-bottom: 8px; border-bottom: 2px solid #1B2A4A;">📋 Ticket Details</div>
                                                <table width="100%%%%" cellpadding="0" cellspacing="0" style="border-collapse: collapse;">
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; width: 120px; font-size: 13px;">Ticket ID</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Category</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Priority</td>
                                                        <td style="color: #172B4D; font-size: 13px;"><span style="display: inline-block; background: rgba(222, 53, 11, 0.1); color: #DE350B; padding: 4px 10px; border-radius: 12px; font-weight: 600; font-size: 11px; text-transform: uppercase;">%s</span></td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Status</td>
                                                        <td style="color: #172B4D; font-size: 13px;"><span style="display: inline-block; background: rgba(222, 53, 11, 0.1); color: #DE350B; padding: 4px 10px; border-radius: 12px; font-weight: 600; font-size: 11px; text-transform: uppercase;">ESCALATED</span></td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Created</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                </table>
                                            </div>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; text-transform: uppercase; margin-bottom: 12px; letter-spacing: 0.3px; padding-bottom: 8px; border-bottom: 2px solid #1B2A4A;">📝 Issue Description</div>
                                                <p style="margin: 12px 0; padding: 12px; background: rgba(27, 42, 74, 0.05); border-radius: 8px; color: #172B4D; font-size: 13px; line-height: 1.5;">%s</p>
                                            </div>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: #F4F5F7; margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 24px 16px; text-align: center;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="text-align: center; padding: 0;">
                                            <p style="margin: 8px 0; font-size: 12px; font-weight: 600; color: #172B4D;"><strong>PowerGrid IT Support</strong></p>
                                            <p style="margin: 4px 0; font-size: 11px; color: #97A0AF;">For support, contact: <strong>support@powergrid.com</strong></p>
                                            <p style="margin: 8px 0 0 0; font-size: 10px; color: #97A0AF;">© 2026 PowerGrid Corporation of India. All rights reserved.</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                </body>
                </html>
                """
                .formatted(ticketId, category, priority, createdTime, description);
    }

    /**
     * Generate professional HTML email for TICKET_ESCALATED event.
     */
    public static String generateEscalationEmailHtml(Ticket ticket) {
        String ticketId = ticket.getTicketId() != null ? ticket.getTicketId() : "N/A";
        String priority = ticket.getPriority() != null ? ticket.getPriority() : "HIGH";
        String category = ticket.getCategory() != null ? ticket.getCategory() : "General";
        String createdTime = ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT) : "N/A";
        String issueDescription = ticket.getIssueDescription() != null ? ticket.getIssueDescription()
                : "No description";

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Ticket Escalated - PowerGrid ITSM</title>
                    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background-color: #F4F5F7;">

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #1B2A4A 0%%%%, #1e3a5f 50%%%%, #1B2A4A 100%%%%); margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 32px 16px; text-align: center;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="text-align: center; padding: 0;">
                                            <div style="width: 48px; height: 48px; background: #1B2A4A; border-radius: 12px; display: inline-flex; align-items: center; justify-content: center; margin-bottom: 12px; font-size: 24px;">⚠️</div>
                                            <h1 style="margin: 8px 0 4px 0; color: white; font-size: 20px; font-weight: 700; letter-spacing: -0.3px;">Ticket Escalated</h1>
                                            <p style="margin: 0; color: rgba(255,255,255,0.85); font-size: 13px;">PowerGrid IT Service Management</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #f5f7fa 0%%%%, #c3cfe2 100%%%%); margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 0;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="padding: 32px;">
                                            <p style="margin: 0 0 16px 0; font-size: 14px; line-height: 1.6; color: #172B4D;">
                                                Hello,<br><br>
                                                Your IT support ticket has been escalated due to exceeding SLA timeframe. Our team is now working on resolving this urgently.
                                            </p>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; margin-bottom: 8px; text-transform: uppercase; letter-spacing: 0.3px;">⚠️ Escalation Alert</div>
                                                <p style="margin: 0; font-size: 13px; color: #172B4D; line-height: 1.6;"><strong>Reason:</strong> SLA Breach - Priority Escalation<br><strong>Escalated At:</strong> %s</p>
                                            </div>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; text-transform: uppercase; margin-bottom: 12px; letter-spacing: 0.3px; padding-bottom: 8px; border-bottom: 2px solid #1B2A4A;">📋 Ticket Details</div>
                                                <table width="100%%%%" cellpadding="0" cellspacing="0" style="border-collapse: collapse;">
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; width: 120px; font-size: 13px;">Ticket ID</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Created On</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Priority</td>
                                                        <td style="color: #172B4D; font-size: 13px;"><span style="display: inline-block; background: rgba(222, 53, 11, 0.1); color: #DE350B; padding: 4px 10px; border-radius: 12px; font-weight: 600; font-size: 11px; text-transform: uppercase;">%s</span></td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Category</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                </table>
                                            </div>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; text-transform: uppercase; margin-bottom: 12px; letter-spacing: 0.3px; padding-bottom: 8px; border-bottom: 2px solid #1B2A4A;">📝 Issue Description</div>
                                                <p style="margin: 12px 0; padding: 12px; background: rgba(27, 42, 74, 0.05); border-radius: 8px; color: #172B4D; font-size: 13px; line-height: 1.5;">%s</p>
                                            </div>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; margin-bottom: 8px; text-transform: uppercase; letter-spacing: 0.3px;">⏱️ SLA Status</div>
                                                <p style="margin: 0; font-size: 13px; color: #172B4D; line-height: 1.6;"><strong>Status:</strong> <strong style="color: #DE350B;">SLA ESCALATED</strong><br>Our senior team is now actively working on this.</p>
                                            </div>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: #F4F5F7; margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 24px 16px; text-align: center;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="text-align: center; padding: 0;">
                                            <p style="margin: 8px 0; font-size: 12px; font-weight: 600; color: #172B4D;"><strong>PowerGrid IT Support - Escalation Team</strong></p>
                                            <p style="margin: 4px 0; font-size: 11px; color: #97A0AF;">This is an automated escalation notification.</p>
                                            <p style="margin: 4px 0; font-size: 11px; color: #97A0AF;">For support, contact: <strong>escalation@powergrid.com</strong></p>
                                            <p style="margin: 8px 0 0 0; font-size: 10px; color: #97A0AF;">© 2026 PowerGrid Corporation of India. All rights reserved.</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                </body>
                </html>
                """
                .formatted(LocalDateTime.now().format(DATE_FORMAT), ticketId, createdTime, priority, category,
                        issueDescription);
    }

    /**
     * Generate professional HTML email for ENGINEER ESCALATION CONFIRMATION.
     * Sent to engineer when escalation is confirmed.
     */
    public static String generateEngineerEscalationConfirmationEmailHtml(Ticket ticket) {
        String ticketId = ticket.getTicketId() != null ? ticket.getTicketId() : "N/A";
        String priority = ticket.getPriority() != null ? ticket.getPriority() : "HIGH";
        String category = ticket.getCategory() != null ? ticket.getCategory() : "General";
        String escalationTime = LocalDateTime.now().format(DATE_FORMAT);
        String createdTime = ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT) : "N/A";

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Escalation Confirmation - PowerGrid ITSM</title>
                    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background-color: #F4F5F7;">

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #1B2A4A 0%%%%, #1e3a5f 50%%%%, #1B2A4A 100%%%%); margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 32px 16px; text-align: center;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="text-align: center; padding: 0;">
                                            <div style="width: 48px; height: 48px; background: #1B2A4A; border-radius: 12px; display: inline-flex; align-items: center; justify-content: center; margin-bottom: 12px; font-size: 24px;">✅</div>
                                            <h1 style="margin: 8px 0 4px 0; color: white; font-size: 20px; font-weight: 700; letter-spacing: -0.3px;">Escalation Successful</h1>
                                            <p style="margin: 0; color: rgba(255,255,255,0.85); font-size: 13px;">PowerGrid IT Service Management</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #f5f7fa 0%%%%, #c3cfe2 100%%%%); margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 0;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="padding: 32px;">
                                            <p style="margin: 0 0 16px 0; font-size: 14px; line-height: 1.6; color: #172B4D;">
                                                Hello,<br><br>
                                                Your escalation has been successfully processed and confirmed.
                                            </p>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; margin-bottom: 8px; text-transform: uppercase; letter-spacing: 0.3px;">✅ Confirmation</div>
                                                <p style="margin: 0; font-size: 13px; color: #172B4D; line-height: 1.6;">Your escalation has been successfully processed. The ticket is now being handled with high priority.</p>
                                            </div>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; text-transform: uppercase; margin-bottom: 12px; letter-spacing: 0.3px; padding-bottom: 8px; border-bottom: 2px solid #1B2A4A;">📋 Ticket Details</div>
                                                <table width="100%%%%" cellpadding="0" cellspacing="0" style="border-collapse: collapse;">
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; width: 120px; font-size: 13px;">Ticket ID</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Category</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Priority</td>
                                                        <td style="color: #172B4D; font-size: 13px;"><span style="display: inline-block; background: rgba(222, 53, 11, 0.1); color: #DE350B; padding: 4px 10px; border-radius: 12px; font-weight: 600; font-size: 11px; text-transform: uppercase;">%s</span></td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Status</td>
                                                        <td style="color: #172B4D; font-size: 13px;"><span style="display: inline-block; background: rgba(54, 179, 126, 0.1); color: #36B37E; padding: 4px 10px; border-radius: 12px; font-weight: 600; font-size: 11px; text-transform: uppercase;">ESCALATED</span></td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Created</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; font-weight: 600; color: #1B2A4A; font-size: 13px;">Escalated At</td>
                                                        <td style="color: #172B4D; font-size: 13px;">%s</td>
                                                    </tr>
                                                </table>
                                            </div>

                                            <div style="margin: 24px 0;">
                                                <div style="font-weight: 600; font-size: 12px; color: #1B2A4A; margin-bottom: 8px; text-transform: uppercase; letter-spacing: 0.3px;">📢 Next Steps</div>
                                                <p style="margin: 0; font-size: 13px; color: #172B4D; line-height: 1.6;">The senior team will now be handling this ticket with higher priority. You will be kept updated on the progress.</p>
                                            </div>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                    <table width="100%%%%" cellpadding="0" cellspacing="0" style="background: #F4F5F7; margin: 0; padding: 0;">
                        <tr>
                            <td style="padding: 24px 16px; text-align: center;">
                                <table width="600" cellpadding="0" cellspacing="0" style="margin: 0 auto; width: 100%%%% max-width: 600px;">
                                    <tr>
                                        <td style="text-align: center; padding: 0;">
                                            <p style="margin: 8px 0; font-size: 12px; font-weight: 600; color: #172B4D;"><strong>PowerGrid IT Support</strong></p>
                                            <p style="margin: 4px 0; font-size: 11px; color: #97A0AF;">For support, contact: <strong>support@powergrid.com</strong></p>
                                            <p style="margin: 8px 0 0 0; font-size: 10px; color: #97A0AF;">© 2026 PowerGrid Corporation of India. All rights reserved.</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>

                </body>
                </html>
                """
                .formatted(ticketId, category, priority, createdTime, escalationTime);
    }
}
