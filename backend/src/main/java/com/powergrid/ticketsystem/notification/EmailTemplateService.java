package com.powergrid.ticketsystem.notification;

import com.powergrid.ticketsystem.entity.Ticket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ============================================================
 * EMAIL TEMPLATE SERVICE
 * ============================================================
 * 
 * PHASE 8: NOTIFICATIONS & ALERTS
 * 
 * Generates professional email content for all notification events.
 * Uses String-based templates (no external template engine required).
 * 
 * DESIGN PRINCIPLES:
 * - Clear and professional enterprise tone
 * - Consistent formatting across all emails
 * - All relevant ticket information included
 * - Actionable messages with clear next steps
 * 
 * EMAIL TYPES:
 * 1. Ticket Created - Confirmation to employee
 * 2. Ticket Assigned - Alert to engineer
 * 3. Ticket Escalated - Urgent alert to management
 * 4. Ticket Resolved - Resolution notification to employee
 * 5. Ticket Closed - Closure confirmation
 * 6. SLA Warning - Deadline approaching alert
 * 7. Ticket Reopened - Failed verification alert
 */
public class EmailTemplateService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");

    private static final String COMPANY_NAME = "PowerGrid Corporation";
    private static final String SUPPORT_EMAIL = "harinipriya3108@gmail.com";
    private static final String PORTAL_URL = "http://localhost:8080";

    // ============================================================
    // TICKET CREATED EMAIL
    // ============================================================

    /**
     * Generate email for TICKET_CREATED event.
     * Sent to: Employee (ticket owner)
     * Professional enterprise-grade HTML template with table-based layout
     */
    public static String generateTicketCreatedEmail(Ticket ticket) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "</head>\n" +
                "<body style='margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, \"Helvetica Neue\", Arial, sans-serif; background-color: #f4f6f8; line-height: 1.6; color: #333;'>\n"
                +
                "<table cellpadding='0' cellspacing='0' border='0' width='100%' style='background-color: #f4f6f8; margin: 0; padding: 0;'>\n"
                +
                "    <tr>\n" +
                "        <td align='center' style='padding: 0;'>\n" +
                "            <table cellpadding='0' cellspacing='0' border='0' width='600' style='max-width: 600px; background-color: #f4f6f8;'>\n"
                +
                "                <!-- Banner Image -->\n" +
                "                <tr>\n" +
                "                    <td style='padding: 0; margin: 0;'>\n" +
                "                        <img src='https://via.placeholder.com/600x120.png?text=PowerGrid+IT+Support' alt='PowerGrid IT Support' width='600' height='120' style='display: block; width: 100%; height: auto;'>\n"
                +
                "                    </td>\n" +
                "                </tr>\n" +
                "                <!-- Gradient Header -->\n" +
                "                <tr>\n" +
                "                    <td style='background: linear-gradient(135deg, #1a4fa8 0%, #0d3a7a 100%); padding: 30px 40px; text-align: center;'>\n"
                +
                "                        <h1 style='margin: 0; font-size: 28px; font-weight: 600; color: #ffffff; letter-spacing: 0.5px;'>IT SUPPORT TICKET CREATED</h1>\n"
                +
                "                        <p style='margin: 8px 0 0 0; font-size: 14px; color: #e0e8ff; font-weight: 300;'>Your request has been successfully logged</p>\n"
                +
                "                    </td>\n" +
                "                </tr>\n" +
                "                <!-- White Content Card -->\n" +
                "                <tr>\n" +
                "                    <td style='background-color: #ffffff; padding: 40px; margin: 20px 20px 0 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.08);'>\n"
                +
                "                        <!-- Greeting -->\n" +
                "                        <table cellpadding='0' cellspacing='0' border='0' width='100%'>\n" +
                "                            <tr>\n" +
                "                                <td style='padding: 0 0 25px 0; border-bottom: 1px solid #e8ecf1;'>\n"
                +
                "                                    <p style='margin: 0; font-size: 16px; color: #333; font-weight: 500;'>Dear User,</p>\n"
                +
                "                                    <p style='margin: 12px 0 0 0; font-size: 15px; color: #555; line-height: 1.7;'>Your IT support request has been successfully logged in our system. Our technical team will review and address your issue as soon as possible.</p>\n"
                +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                        </table>\n" +
                "                        <!-- Ticket Details Section -->\n" +
                "                        <table cellpadding='0' cellspacing='0' border='0' width='100%' style='margin-top: 30px;'>\n"
                +
                "                            <tr>\n" +
                "                                <td style='padding: 0 0 15px 0;'>\n" +
                "                                    <p style='margin: 0; font-size: 13px; font-weight: 700; color: #1a4fa8; text-transform: uppercase; letter-spacing: 1px;'>Ticket Details</p>\n"
                +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                            <tr>\n" +
                "                                <td style='padding: 0;'>\n" +
                "                                    <table cellpadding='0' cellspacing='0' border='0' width='100%' style='border: 1px solid #e8ecf1; border-radius: 6px;'>\n"
                +
                "                                        <tr style='border-bottom: 1px solid #e8ecf1;'>\n" +
                "                                            <td style='padding: 14px 16px; width: 45%; background-color: #f9fafb; font-size: 13px; font-weight: 600; color: #666;'>Ticket ID</td>\n"
                +
                "                                            <td style='padding: 14px 16px; font-size: 14px; color: #333; font-weight: 500;'>"
                + ticket.getTicketId() + "</td>\n" +
                "                                        </tr>\n" +
                "                                        <tr style='border-bottom: 1px solid #e8ecf1;'>\n" +
                "                                            <td style='padding: 14px 16px; background-color: #f9fafb; font-size: 13px; font-weight: 600; color: #666;'>Created On</td>\n"
                +
                "                                            <td style='padding: 14px 16px; font-size: 14px; color: #333;'>"
                + formatDateTime(ticket.getCreatedTime()) + "</td>\n" +
                "                                        </tr>\n" +
                "                                        <tr style='border-bottom: 1px solid #e8ecf1;'>\n" +
                "                                            <td style='padding: 14px 16px; background-color: #f9fafb; font-size: 13px; font-weight: 600; color: #666;'>Priority</td>\n"
                +
                "                                            <td style='padding: 14px 16px; font-size: 14px; color: #333;'>"
                + (ticket.getPriority() != null ? ticket.getPriority() : "PENDING") + "</td>\n" +
                "                                        </tr>\n" +
                "                                        <tr style='border-bottom: 1px solid #e8ecf1;'>\n" +
                "                                            <td style='padding: 14px 16px; background-color: #f9fafb; font-size: 13px; font-weight: 600; color: #666;'>Category</td>\n"
                +
                "                                            <td style='padding: 14px 16px; font-size: 14px; color: #333;'>"
                + (ticket.getCategory() != null ? ticket.getCategory() : "Under Classification") + "</td>\n" +
                "                                        </tr>\n" +
                "                                        <tr>\n" +
                "                                            <td style='padding: 14px 16px; background-color: #f9fafb; font-size: 13px; font-weight: 600; color: #666;'>Status</td>\n"
                +
                "                                            <td style='padding: 14px 16px; font-size: 14px; color: #333;'>OPEN</td>\n"
                +
                "                                        </tr>\n" +
                "                                    </table>\n" +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                        </table>\n" +
                "                        <!-- Issue Description Section -->\n" +
                "                        <table cellpadding='0' cellspacing='0' border='0' width='100%' style='margin-top: 30px;'>\n"
                +
                "                            <tr>\n" +
                "                                <td style='padding: 0 0 15px 0;'>\n" +
                "                                    <p style='margin: 0; font-size: 13px; font-weight: 700; color: #1a4fa8; text-transform: uppercase; letter-spacing: 1px;'>Issue Description</p>\n"
                +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                            <tr>\n" +
                "                                <td style='background-color: #f0f5ff; border: 1px solid #c5d9f1; border-radius: 6px; padding: 20px; font-size: 14px; color: #333; line-height: 1.6;'>\n"
                +
                "                                    " + truncateText(ticket.getIssueDescription(), 300) + "\n" +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                        </table>\n" +
                "                        <!-- Expected Response Time Section -->\n" +
                "                        <table cellpadding='0' cellspacing='0' border='0' width='100%' style='margin-top: 30px;'>\n"
                +
                "                            <tr>\n" +
                "                                <td style='padding: 0 0 15px 0;'>\n" +
                "                                    <p style='margin: 0; font-size: 13px; font-weight: 700; color: #1a4fa8; text-transform: uppercase; letter-spacing: 1px;'>Expected Response Time</p>\n"
                +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                            <tr>\n" +
                "                                <td style='background-color: #f5faf0; border-left: 4px solid #52a552; padding: 16px; border-radius: 4px; font-size: 14px; color: #333;'>\n"
                +
                "                                    Based on priority, you can expect a response within:<br>\n" +
                "                                    <strong style='font-size: 15px; color: #1a4fa8;'>"
                + getExpectedResponseTime(ticket.getPriority()) + "</strong>\n" +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                        </table>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "                <!-- Footer Section -->\n" +
                "                <tr>\n" +
                "                    <td style='background-color: #f9fafb; padding: 30px 40px; margin: 0 20px 20px 20px; border-radius: 8px; border-top: 1px solid #e8ecf1; text-align: center;'>\n"
                +
                "                        <p style='margin: 0 0 12px 0; font-size: 14px; color: #555; line-height: 1.6;'><strong>Need immediate assistance?</strong><br>Contact our support team at <a href='mailto:"
                + SUPPORT_EMAIL + "' style='color: #1a4fa8; text-decoration: none;'>" + SUPPORT_EMAIL + "</a></p>\n" +
                "                        <p style='margin: 16px 0; font-size: 12px; color: #888;'>Follow us on social media</p>\n"
                +
                "                        <table cellpadding='0' cellspacing='0' border='0' width='100%' align='center' style='table-layout: auto;'>\n"
                +
                "                            <tr>\n" +
                "                                <td align='center' style='padding: 0 5px;'>\n" +
                "                                    <a href='#' style='display: inline-block; width: 32px; height: 32px; background-color: #1a4fa8; color: white; text-decoration: none; line-height: 32px; font-size: 14px; border-radius: 50%; text-align: center;'>f</a>\n"
                +
                "                                </td>\n" +
                "                                <td align='center' style='padding: 0 5px;'>\n" +
                "                                    <a href='#' style='display: inline-block; width: 32px; height: 32px; background-color: #1a4fa8; color: white; text-decoration: none; line-height: 32px; font-size: 14px; border-radius: 50%; text-align: center;'>tw</a>\n"
                +
                "                                </td>\n" +
                "                                <td align='center' style='padding: 0 5px;'>\n" +
                "                                    <a href='#' style='display: inline-block; width: 32px; height: 32px; background-color: #1a4fa8; color: white; text-decoration: none; line-height: 32px; font-size: 14px; border-radius: 50%; text-align: center;'>in</a>\n"
                +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                        </table>\n" +
                "                        <p style='margin: 20px 0 0 0; font-size: 12px; color: #999; border-top: 1px solid #e8ecf1; padding-top: 16px;'>This is an automated email. Please do not reply directly to this message.<br>Portal: <a href='"
                + PORTAL_URL + "' style='color: #1a4fa8; text-decoration: none;'>" + PORTAL_URL + "</a></p>\n" +
                "                        <p style='margin: 8px 0 0 0; font-size: 11px; color: #bbb;'>&copy; "
                + COMPANY_NAME + " IT Support System. All rights reserved.</p>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "            </table>\n" +
                "        </td>\n" +
                "    </tr>\n" +
                "</table>\n" +
                "</body>\n" +
                "</html>";
    }

    // ============================================================
    // TICKET ASSIGNED EMAIL
    // ============================================================

    /**
     * Generate email for TICKET_ASSIGNED event.
     * Sent to: Assigned Engineer
     */
    public static String generateTicketAssignedEmail(Ticket ticket) {
        return """
                ══════════════════════════════════════════════════════════════
                🔔 NEW TICKET ASSIGNED TO YOU
                ══════════════════════════════════════════════════════════════

                Hello %s,

                A new IT support ticket has been assigned to you for resolution.
                Please review and begin working on this ticket at your earliest convenience.

                ─────────────────────────────────────────────────────────────
                📋 TICKET DETAILS
                ─────────────────────────────────────────────────────────────

                Ticket ID      : %s
                Created On     : %s
                Priority       : %s
                Category       : %s
                Sub-Category   : %s
                Status         : ASSIGNED

                ─────────────────────────────────────────────────────────────
                👤 REQUESTER INFORMATION
                ─────────────────────────────────────────────────────────────

                Employee ID    : %s
                Email          : %s

                ─────────────────────────────────────────────────────────────
                📝 ISSUE DESCRIPTION
                ─────────────────────────────────────────────────────────────

                %s

                ─────────────────────────────────────────────────────────────
                ⏰ SLA INFORMATION
                ─────────────────────────────────────────────────────────────

                SLA Deadline   : %s
                Time Remaining : %s

                ─────────────────────────────────────────────────────────────
                🔗 QUICK ACTIONS
                ─────────────────────────────────────────────────────────────

                Access Engineer Portal: %s/engineer/dashboard.html

                ══════════════════════════════════════════════════════════════
                %s IT Support System
                ══════════════════════════════════════════════════════════════
                """.formatted(
                ticket.getAssignedEngineer() != null ? ticket.getAssignedEngineer() : "Engineer",
                ticket.getTicketId(),
                formatDateTime(ticket.getCreatedTime()),
                ticket.getPriority(),
                ticket.getCategory(),
                ticket.getSubCategory() != null ? ticket.getSubCategory() : "N/A",
                ticket.getEmployeeId(),
                ticket.getSenderEmail() != null ? ticket.getSenderEmail() : "N/A",
                truncateText(ticket.getIssueDescription(), 400),
                formatDateTime(ticket.getSlaDeadline()),
                calculateTimeRemaining(ticket.getSlaDeadline()),
                PORTAL_URL,
                COMPANY_NAME);
    }

    // ============================================================
    // TICKET ESCALATED EMAIL
    // ============================================================

    /**
     * Generate email for TICKET_ESCALATED event.
     * Sent to: Escalation chain (Senior Engineer / Team Lead / Manager)
     * Professional enterprise-grade HTML template with table-based layout
     */
    public static String generateTicketEscalatedEmail(Ticket ticket, String escalationLevel) {
        String headerColor = getEscalationHeaderColor(escalationLevel);
        String levelLabel = getEscalationLevelLabel(escalationLevel);

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "</head>\n" +
                "<body style='margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, \"Helvetica Neue\", Arial, sans-serif; background-color: #f4f6f8; line-height: 1.6; color: #333;'>\n"
                +
                "<table cellpadding='0' cellspacing='0' border='0' width='100%' style='background-color: #f4f6f8; margin: 0; padding: 0;'>\n"
                +
                "    <tr>\n" +
                "        <td align='center' style='padding: 0;'>\n" +
                "            <table cellpadding='0' cellspacing='0' border='0' width='600' style='max-width: 600px; background-color: #f4f6f8;'>\n"
                +
                "                <!-- Banner Image -->\n" +
                "                <tr>\n" +
                "                    <td style='padding: 0; margin: 0;'>\n" +
                "                        <img src='https://via.placeholder.com/600x120.png?text=PowerGrid+IT+Support' alt='PowerGrid IT Support' width='600' height='120' style='display: block; width: 100%; height: auto;'>\n"
                +
                "                    </td>\n" +
                "                </tr>\n" +
                "                <!-- Gradient Header - Red for Escalation -->\n" +
                "                <tr>\n" +
                "                    <td style='background: linear-gradient(135deg, " + headerColor + " 0%, "
                + darkenColor(headerColor) + " 100%); padding: 30px 40px; text-align: center;'>\n" +
                "                        <h1 style='margin: 0; font-size: 28px; font-weight: 700; color: #ffffff; letter-spacing: 0.5px;'>"
                + levelLabel + "</h1>\n" +
                "                        <p style='margin: 8px 0 0 0; font-size: 14px; color: #ffe0e0; font-weight: 300;'>Immediate attention required</p>\n"
                +
                "                    </td>\n" +
                "                </tr>\n" +
                "                <!-- White Content Card -->\n" +
                "                <tr>\n" +
                "                    <td style='background-color: #ffffff; padding: 40px; margin: 20px 20px 0 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.08);'>\n"
                +
                "                        <!-- Alert Banner -->\n" +
                "                        <table cellpadding='0' cellspacing='0' border='0' width='100%'>\n" +
                "                            <tr>\n" +
                "                                <td style='background-color: #ffebee; border-left: 5px solid #d32f2f; padding: 16px; border-radius: 4px; margin-bottom: 25px;'>\n"
                +
                "                                    <p style='margin: 0; font-size: 15px; font-weight: 600; color: #b71c1c;'>CRITICAL: This ticket has been escalated and requires immediate attention.</p>\n"
                +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                        </table>\n" +
                "                        <!-- Escalation Details Section -->\n" +
                "                        <table cellpadding='0' cellspacing='0' border='0' width='100%' style='margin-bottom: 25px;'>\n"
                +
                "                            <tr>\n" +
                "                                <td style='padding: 0 0 15px 0;'>\n" +
                "                                    <p style='margin: 0; font-size: 13px; font-weight: 700; color: #d32f2f; text-transform: uppercase; letter-spacing: 1px;'>Escalation Details</p>\n"
                +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                            <tr>\n" +
                "                                <td style='padding: 0;'>\n" +
                "                                    <table cellpadding='0' cellspacing='0' border='0' width='100%' style='border: 1px solid #ffcccc; border-radius: 6px;'>\n"
                +
                "                                        <tr style='border-bottom: 1px solid #ffcccc;'>\n" +
                "                                            <td style='padding: 14px 16px; width: 45%; background-color: #fef5f5; font-size: 13px; font-weight: 600; color: #d32f2f;'>Escalation Level</td>\n"
                +
                "                                            <td style='padding: 14px 16px; font-size: 14px; color: #333; font-weight: 600;'>"
                + levelLabel + "</td>\n" +
                "                                        </tr>\n" +
                "                                        <tr style='border-bottom: 1px solid #ffcccc;'>\n" +
                "                                            <td style='padding: 14px 16px; background-color: #fef5f5; font-size: 13px; font-weight: 600; color: #d32f2f;'>Escalation Count</td>\n"
                +
                "                                            <td style='padding: 14px 16px; font-size: 14px; color: #333;'>"
                + (ticket.getEscalationCount() != null ? ticket.getEscalationCount() : 1) + "</td>\n" +
                "                                        </tr>\n" +
                "                                        <tr style='border-bottom: 1px solid #ffcccc;'>\n" +
                "                                            <td style='padding: 14px 16px; background-color: #fef5f5; font-size: 13px; font-weight: 600; color: #d32f2f;'>Escalated At</td>\n"
                +
                "                                            <td style='padding: 14px 16px; font-size: 14px; color: #333;'>"
                + formatDateTime(ticket.getEscalatedTime()) + "</td>\n" +
                "                                        </tr>\n" +
                "                                        <tr>\n" +
                "                                            <td style='padding: 14px 16px; background-color: #fef5f5; font-size: 13px; font-weight: 600; color: #d32f2f;'>Escalation Reason</td>\n"
                +
                "                                            <td style='padding: 14px 16px; font-size: 14px; color: #333;'>"
                + (ticket.getEscalationReason() != null ? ticket.getEscalationReason() : "SLA Breach") + "</td>\n" +
                "                                        </tr>\n" +
                "                                    </table>\n" +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                        </table>\n" +
                "                        <!-- Ticket Information Section -->\n" +
                "                        <table cellpadding='0' cellspacing='0' border='0' width='100%' style='margin-bottom: 25px;'>\n"
                +
                "                            <tr>\n" +
                "                                <td style='padding: 0 0 15px 0;'>\n" +
                "                                    <p style='margin: 0; font-size: 13px; font-weight: 700; color: #1a4fa8; text-transform: uppercase; letter-spacing: 1px;'>Ticket Information</p>\n"
                +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                            <tr>\n" +
                "                                <td style='padding: 0;'>\n" +
                "                                    <table cellpadding='0' cellspacing='0' border='0' width='100%' style='border: 1px solid #e8ecf1; border-radius: 6px;'>\n"
                +
                "                                        <tr style='border-bottom: 1px solid #e8ecf1;'>\n" +
                "                                            <td style='padding: 14px 16px; width: 45%; background-color: #f9fafb; font-size: 13px; font-weight: 600; color: #666;'>Ticket ID</td>\n"
                +
                "                                            <td style='padding: 14px 16px; font-size: 14px; color: #333; font-weight: 500;'>"
                + ticket.getTicketId() + "</td>\n" +
                "                                        </tr>\n" +
                "                                        <tr style='border-bottom: 1px solid #e8ecf1;'>\n" +
                "                                            <td style='padding: 14px 16px; background-color: #f9fafb; font-size: 13px; font-weight: 600; color: #666;'>Priority</td>\n"
                +
                "                                            <td style='padding: 14px 16px; font-size: 14px; color: #333;'>"
                + ticket.getPriority() + "</td>\n" +
                "                                        </tr>\n" +
                "                                        <tr style='border-bottom: 1px solid #e8ecf1;'>\n" +
                "                                            <td style='padding: 14px 16px; background-color: #f9fafb; font-size: 13px; font-weight: 600; color: #666;'>Category</td>\n"
                +
                "                                            <td style='padding: 14px 16px; font-size: 14px; color: #333;'>"
                + ticket.getCategory() + "</td>\n" +
                "                                        </tr>\n" +
                "                                        <tr style='border-bottom: 1px solid #e8ecf1;'>\n" +
                "                                            <td style='padding: 14px 16px; background-color: #f9fafb; font-size: 13px; font-weight: 600; color: #666;'>Status</td>\n"
                +
                "                                            <td style='padding: 14px 16px; font-size: 14px; color: #333;'>"
                + ticket.getStatus() + "</td>\n" +
                "                                        </tr>\n" +
                "                                        <tr>\n" +
                "                                            <td style='padding: 14px 16px; background-color: #f9fafb; font-size: 13px; font-weight: 600; color: #666;'>Created On</td>\n"
                +
                "                                            <td style='padding: 14px 16px; font-size: 14px; color: #333;'>"
                + formatDateTime(ticket.getCreatedTime()) + "</td>\n" +
                "                                        </tr>\n" +
                "                                    </table>\n" +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                        </table>\n" +
                "                        <!-- SLA Status Section -->\n" +
                "                        <table cellpadding='0' cellspacing='0' border='0' width='100%' style='margin-bottom: 25px;'>\n"
                +
                "                            <tr>\n" +
                "                                <td style='padding: 0 0 15px 0;'>\n" +
                "                                    <p style='margin: 0; font-size: 13px; font-weight: 700; color: #1a4fa8; text-transform: uppercase; letter-spacing: 1px;'>SLA Status</p>\n"
                +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                            <tr>\n" +
                "                                <td style='background-color: #fff3e0; border-left: 4px solid #f57c00; padding: 16px; border-radius: 4px; font-size: 14px; color: #333; line-height: 1.6;'>\n"
                +
                "                                    <strong>SLA Deadline:</strong> "
                + formatDateTime(ticket.getSlaDeadline()) + "<br>\n" +
                "                                    <strong>SLA Breached:</strong> "
                + (ticket.getSlaBreached() != null && ticket.getSlaBreached() ? "YES (CRITICAL)" : "NO") + "<br>\n" +
                "                                    <strong>Time Remaining:</strong> "
                + calculateTimeRemaining(ticket.getSlaDeadline()) + "\n" +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                        </table>\n" +
                "                        <!-- Issue Description Section -->\n" +
                "                        <table cellpadding='0' cellspacing='0' border='0' width='100%' style='margin-bottom: 25px;'>\n"
                +
                "                            <tr>\n" +
                "                                <td style='padding: 0 0 15px 0;'>\n" +
                "                                    <p style='margin: 0; font-size: 13px; font-weight: 700; color: #1a4fa8; text-transform: uppercase; letter-spacing: 1px;'>Issue Description</p>\n"
                +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                            <tr>\n" +
                "                                <td style='background-color: #f0f5ff; border: 1px solid #c5d9f1; border-radius: 6px; padding: 20px; font-size: 14px; color: #333; line-height: 1.6;'>\n"
                +
                "                                    " + truncateText(ticket.getIssueDescription(), 400) + "\n" +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                        </table>\n" +
                "                        <!-- Assignment History -->\n" +
                "                        <table cellpadding='0' cellspacing='0' border='0' width='100%' style='margin-bottom: 25px;'>\n"
                +
                "                            <tr>\n" +
                "                                <td style='padding: 0 0 15px 0;'>\n" +
                "                                    <p style='margin: 0; font-size: 13px; font-weight: 700; color: #1a4fa8; text-transform: uppercase; letter-spacing: 1px;'>Assignment Details</p>\n"
                +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                            <tr>\n" +
                "                                <td style='padding: 0;'>\n" +
                "                                    <table cellpadding='0' cellspacing='0' border='0' width='100%' style='border: 1px solid #e8ecf1; border-radius: 6px;'>\n"
                +
                "                                        <tr style='border-bottom: 1px solid #e8ecf1;'>\n" +
                "                                            <td style='padding: 14px 16px; width: 45%; background-color: #f9fafb; font-size: 13px; font-weight: 600; color: #666;'>Current Engineer</td>\n"
                +
                "                                            <td style='padding: 14px 16px; font-size: 14px; color: #333;'>"
                + (ticket.getAssignedEngineer() != null ? ticket.getAssignedEngineer() : "Unassigned") + "</td>\n" +
                "                                        </tr>\n" +
                "                                        <tr style='border-bottom: 1px solid #e8ecf1;'>\n" +
                "                                            <td style='padding: 14px 16px; background-color: #f9fafb; font-size: 13px; font-weight: 600; color: #666;'>Previous Engineer</td>\n"
                +
                "                                            <td style='padding: 14px 16px; font-size: 14px; color: #333;'>"
                + (ticket.getPreviousEngineer() != null ? ticket.getPreviousEngineer() : "N/A") + "</td>\n" +
                "                                        </tr>\n" +
                "                                        <tr>\n" +
                "                                            <td style='padding: 14px 16px; background-color: #f9fafb; font-size: 13px; font-weight: 600; color: #666;'>Assigned Team</td>\n"
                +
                "                                            <td style='padding: 14px 16px; font-size: 14px; color: #333;'>"
                + (ticket.getAssignedTeam() != null ? ticket.getAssignedTeam() : "N/A") + "</td>\n" +
                "                                        </tr>\n" +
                "                                    </table>\n" +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                        </table>\n" +
                "                        <!-- Required Actions -->\n" +
                "                        <table cellpadding='0' cellspacing='0' border='0' width='100%'>\n" +
                "                            <tr>\n" +
                "                                <td style='padding: 0 0 15px 0;'>\n" +
                "                                    <p style='margin: 0; font-size: 13px; font-weight: 700; color: #d32f2f; text-transform: uppercase; letter-spacing: 1px;'>Required Actions</p>\n"
                +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                            <tr>\n" +
                "                                <td style='background-color: #ffebee; border-left: 4px solid #d32f2f; padding: 16px; border-radius: 4px;'>\n"
                +
                "                                    <ol style='margin: 0; padding-left: 20px; color: #333;'>\n" +
                "                                        <li style='margin-bottom: 8px; font-size: 14px;'>Review ticket immediately in the Engineer Portal</li>\n"
                +
                "                                        <li style='margin-bottom: 8px; font-size: 14px;'>Contact the assigned engineer to understand blockers</li>\n"
                +
                "                                        <li style='margin-bottom: 8px; font-size: 14px;'>Allocate additional resources if necessary</li>\n"
                +
                "                                        <li style='margin-bottom: 8px; font-size: 14px;'>Provide resolution timeline to requestor</li>\n"
                +
                "                                        <li style='font-size: 14px;'>Update ticket status regularly</li>\n"
                +
                "                                    </ol>\n" +
                "                                </td>\n" +
                "                            </tr>\n" +
                "                        </table>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "                <!-- Footer Section -->\n" +
                "                <tr>\n" +
                "                    <td style='background-color: #f9fafb; padding: 30px 40px; margin: 0 20px 20px 20px; border-radius: 8px; border-top: 1px solid #e8ecf1; text-align: center;'>\n"
                +
                "                        <p style='margin: 0 0 12px 0; font-size: 14px; color: #555; line-height: 1.6;'><strong>Escalation Portal Access</strong><br><a href='"
                + PORTAL_URL
                + "/engineer/dashboard.html' style='color: #d32f2f; text-decoration: none; font-weight: 600;'>View Escalated Ticket Dashboard</a></p>\n"
                +
                "                        <p style='margin: 16px 0 0 0; font-size: 12px; color: #888; border-top: 1px solid #e8ecf1; padding-top: 16px;'>This is an automated escalation alert. Please do not reply directly to this message.<br>Support: <a href='mailto:"
                + SUPPORT_EMAIL + "' style='color: #1a4fa8; text-decoration: none;'>" + SUPPORT_EMAIL + "</a></p>\n" +
                "                        <p style='margin: 8px 0 0 0; font-size: 11px; color: #bbb;'>&copy; "
                + COMPANY_NAME + " IT Support System. All rights reserved.</p>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "            </table>\n" +
                "        </td>\n" +
                "    </tr>\n" +
                "</table>\n" +
                "</body>\n" +
                "</html>";
    }

    private static String getEscalationHeaderColor(String escalationLevel) {
        return switch (escalationLevel != null ? escalationLevel : "") {
            case "LEVEL_1" -> "#e67e22";
            case "LEVEL_2" -> "#e74c3c";
            case "LEVEL_3" -> "#c0392b";
            default -> "#d32f2f";
        };
    }

    private static String darkenColor(String color) {
        return switch (color) {
            case "#e67e22" -> "#d35400";
            case "#e74c3c" -> "#c0392b";
            case "#c0392b" -> "#a93226";
            default -> "#b71c1c";
        };
    }

    private static String getEscalationLevelLabel(String escalationLevel) {
        return switch (escalationLevel != null ? escalationLevel : "") {
            case "LEVEL_1" -> "Level 1 Escalation - Senior Engineer Required";
            case "LEVEL_2" -> "Level 2 Escalation - Team Lead Attention Needed";
            case "LEVEL_3" -> "Level 3 Escalation - Manager/Director Required";
            default -> "Ticket Escalation - Immediate Action Required";
        };
    }

    // ============================================================
    // TICKET RESOLVED EMAIL
    // ============================================================

    /**
     * Generate email for TICKET_RESOLVED event.
     * Sent to: Employee (ticket owner)
     */
    public static String generateTicketResolvedEmail(Ticket ticket) {
        return """
                ══════════════════════════════════════════════════════════════
                ✅ YOUR IT SUPPORT TICKET HAS BEEN RESOLVED
                ══════════════════════════════════════════════════════════════

                Dear User,

                We are pleased to inform you that your IT support ticket has been
                resolved by our technical team.

                ─────────────────────────────────────────────────────────────
                📋 TICKET DETAILS
                ─────────────────────────────────────────────────────────────

                Ticket ID      : %s
                Category       : %s
                Priority       : %s
                Status         : RESOLVED

                ─────────────────────────────────────────────────────────────
                📝 ORIGINAL ISSUE
                ─────────────────────────────────────────────────────────────

                %s

                ─────────────────────────────────────────────────────────────
                ✏️ RESOLUTION DETAILS
                ─────────────────────────────────────────────────────────────

                Resolved By    : %s
                Resolved On    : %s

                Resolution Notes:
                %s

                ─────────────────────────────────────────────────────────────
                📊 SERVICE METRICS
                ─────────────────────────────────────────────────────────────

                Created        : %s
                Resolved       : %s
                Resolution Time: %s

                ─────────────────────────────────────────────────────────────
                ❓ NEED FURTHER ASSISTANCE?
                ─────────────────────────────────────────────────────────────

                If the issue persists or you need additional help:
                • Reply to this email with details
                • Create a new ticket referencing: %s

                ══════════════════════════════════════════════════════════════
                Thank you for using %s IT Support.
                We appreciate your patience!
                ══════════════════════════════════════════════════════════════
                """.formatted(
                ticket.getTicketId(),
                ticket.getCategory(),
                ticket.getPriority(),
                truncateText(ticket.getIssueDescription(), 200),
                ticket.getAssignedEngineer() != null ? ticket.getAssignedEngineer() : "IT Support",
                formatDateTime(LocalDateTime.now()),
                ticket.getResolutionNotes() != null ? ticket.getResolutionNotes() : "Issue has been resolved.",
                formatDateTime(ticket.getCreatedTime()),
                formatDateTime(LocalDateTime.now()),
                calculateResolutionTime(ticket.getCreatedTime(), LocalDateTime.now()),
                ticket.getTicketId(),
                COMPANY_NAME);
    }

    // ============================================================
    // TICKET CLOSED EMAIL
    // ============================================================

    /**
     * Generate email for TICKET_CLOSED event.
     * Sent to: Employee (ticket owner)
     */
    public static String generateTicketClosedEmail(Ticket ticket) {
        return """
                ══════════════════════════════════════════════════════════════
                ✅ IT SUPPORT TICKET CLOSED
                ══════════════════════════════════════════════════════════════

                Dear User,

                Your IT support ticket has been verified and is now officially CLOSED.

                ─────────────────────────────────────────────────────────────
                📋 TICKET SUMMARY
                ─────────────────────────────────────────────────────────────

                Ticket ID      : %s
                Category       : %s
                Priority       : %s
                Final Status   : CLOSED
                Closed By      : %s

                ─────────────────────────────────────────────────────────────
                📝 ISSUE & RESOLUTION
                ─────────────────────────────────────────────────────────────

                Issue:
                %s

                Resolution:
                %s

                ─────────────────────────────────────────────────────────────
                📊 TICKET LIFECYCLE
                ─────────────────────────────────────────────────────────────

                Created        : %s
                Resolved       : %s
                Closed         : %s
                Total Duration : %s

                ─────────────────────────────────────────────────────────────
                ⭐ QUALITY METRICS
                ─────────────────────────────────────────────────────────────

                Verification Score : %d/100
                SLA Met            : %s

                ─────────────────────────────────────────────────────────────

                Thank you for using our IT Support services!

                ══════════════════════════════════════════════════════════════
                %s IT Support - Ticket Reference: %s
                ══════════════════════════════════════════════════════════════
                """.formatted(
                ticket.getTicketId(),
                ticket.getCategory(),
                ticket.getPriority(),
                ticket.getClosedBy() != null ? ticket.getClosedBy() : "System",
                truncateText(ticket.getIssueDescription(), 200),
                ticket.getResolutionNotes() != null ? ticket.getResolutionNotes() : "Issue resolved.",
                formatDateTime(ticket.getCreatedTime()),
                ticket.getVerifiedTime() != null ? formatDateTime(ticket.getVerifiedTime()) : "N/A",
                formatDateTime(ticket.getClosedTime()),
                calculateResolutionTime(ticket.getCreatedTime(), ticket.getClosedTime()),
                ticket.getVerificationScore() != null ? ticket.getVerificationScore() : 0,
                (ticket.getSlaBreached() == null || !ticket.getSlaBreached()) ? "YES ✅" : "NO ⚠️",
                COMPANY_NAME,
                ticket.getTicketId());
    }

    // ============================================================
    // SLA WARNING EMAIL
    // ============================================================

    /**
     * Generate email for SLA_WARNING event.
     * Sent to: Assigned Engineer
     */
    public static String generateSlaWarningEmail(Ticket ticket) {
        return """
                ══════════════════════════════════════════════════════════════
                ⚠️ SLA WARNING: TICKET REQUIRES IMMEDIATE ATTENTION
                ══════════════════════════════════════════════════════════════

                Hello %s,

                This is an automated warning that the following ticket is
                approaching its SLA deadline and requires immediate attention.

                ─────────────────────────────────────────────────────────────
                ⏰ SLA ALERT
                ─────────────────────────────────────────────────────────────

                ⚠️  75%% OF SLA TIME HAS ELAPSED  ⚠️

                SLA Deadline   : %s
                Time Remaining : %s

                ─────────────────────────────────────────────────────────────
                📋 TICKET DETAILS
                ─────────────────────────────────────────────────────────────

                Ticket ID      : %s
                Priority       : %s
                Category       : %s
                Current Status : %s
                Created On     : %s

                ─────────────────────────────────────────────────────────────
                📝 ISSUE
                ─────────────────────────────────────────────────────────────

                %s

                ─────────────────────────────────────────────────────────────
                🔗 REQUIRED ACTION
                ─────────────────────────────────────────────────────────────

                Please take one of the following actions:

                1. RESOLVE the ticket if the issue is fixed
                2. UPDATE the status if you're actively working on it
                3. ESCALATE if you need additional support

                Access Portal: %s/engineer/dashboard.html

                ─────────────────────────────────────────────────────────────
                ⚠️ WARNING
                ─────────────────────────────────────────────────────────────

                Failure to resolve within the SLA deadline will result in:
                • Automatic escalation to senior staff
                • SLA breach recorded in metrics
                • Impact on team performance score

                ══════════════════════════════════════════════════════════════
                %s IT Support - Automated SLA Monitoring
                ══════════════════════════════════════════════════════════════
                """.formatted(
                ticket.getAssignedEngineer() != null ? ticket.getAssignedEngineer() : "Engineer",
                formatDateTime(ticket.getSlaDeadline()),
                calculateTimeRemaining(ticket.getSlaDeadline()),
                ticket.getTicketId(),
                ticket.getPriority(),
                ticket.getCategory(),
                ticket.getStatus(),
                formatDateTime(ticket.getCreatedTime()),
                truncateText(ticket.getIssueDescription(), 300),
                PORTAL_URL,
                COMPANY_NAME);
    }

    // ============================================================
    // TICKET REOPENED EMAIL
    // ============================================================

    /**
     * Generate email for TICKET_REOPENED event.
     * Sent to: Assigned Engineer
     */
    public static String generateTicketReopenedEmail(Ticket ticket) {
        return """
                ══════════════════════════════════════════════════════════════
                🔄 TICKET REOPENED - ADDITIONAL ACTION REQUIRED
                ══════════════════════════════════════════════════════════════

                Hello %s,

                The following ticket has been reopened after AI verification.
                Please review the feedback and provide a more comprehensive resolution.

                ─────────────────────────────────────────────────────────────
                📋 TICKET DETAILS
                ─────────────────────────────────────────────────────────────

                Ticket ID      : %s
                Priority       : %s
                Category       : %s
                Status         : IN_PROGRESS (Reopened)

                ─────────────────────────────────────────────────────────────
                ❌ VERIFICATION RESULT
                ─────────────────────────────────────────────────────────────

                Verification Score : %d/100
                Attempt Number     : %d

                ─────────────────────────────────────────────────────────────
                📝 VERIFICATION FEEDBACK
                ─────────────────────────────────────────────────────────────

                %s

                ─────────────────────────────────────────────────────────────
                🔗 REQUIRED ACTION
                ─────────────────────────────────────────────────────────────

                Please:
                1. Review the verification feedback above
                2. Address the identified issues
                3. Provide more detailed resolution notes
                4. Resubmit the resolution

                Access Portal: %s/engineer/dashboard.html

                ══════════════════════════════════════════════════════════════
                %s IT Support - AI Verification System
                ══════════════════════════════════════════════════════════════
                """.formatted(
                ticket.getAssignedEngineer() != null ? ticket.getAssignedEngineer() : "Engineer",
                ticket.getTicketId(),
                ticket.getPriority(),
                ticket.getCategory(),
                ticket.getVerificationScore() != null ? ticket.getVerificationScore() : 0,
                ticket.getVerificationAttempts() != null ? ticket.getVerificationAttempts() : 1,
                ticket.getVerificationNotes() != null ? ticket.getVerificationNotes()
                        : "Verification failed. Please provide more details.",
                PORTAL_URL,
                COMPANY_NAME);
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Format LocalDateTime to readable string.
     */
    private static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        return dateTime.format(DATE_FORMAT);
    }

    /**
     * Truncate text to specified length with ellipsis.
     */
    private static String truncateText(String text, int maxLength) {
        if (text == null) {
            return "No description provided.";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Get expected response time based on priority.
     */
    private static String getExpectedResponseTime(String priority) {
        if (priority == null) {
            return "Within 24 hours";
        }
        return switch (priority.toUpperCase()) {
            case "CRITICAL" -> "Within 2 hours (CRITICAL PRIORITY)";
            case "HIGH" -> "Within 4 hours";
            case "MEDIUM" -> "Within 8 hours";
            case "LOW" -> "Within 24 hours";
            default -> "Within 24 hours";
        };
    }

    /**
     * Calculate time remaining until deadline.
     */
    private static String calculateTimeRemaining(LocalDateTime deadline) {
        if (deadline == null) {
            return "N/A";
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(deadline)) {
            long minutesOverdue = java.time.Duration.between(deadline, now).toMinutes();
            long hours = minutesOverdue / 60;
            long mins = minutesOverdue % 60;
            return "OVERDUE by " + hours + "h " + mins + "m";
        }

        long minutesRemaining = java.time.Duration.between(now, deadline).toMinutes();
        long hours = minutesRemaining / 60;
        long mins = minutesRemaining % 60;
        return hours + "h " + mins + "m remaining";
    }

    /**
     * Calculate resolution time.
     */
    private static String calculateResolutionTime(LocalDateTime created, LocalDateTime closed) {
        if (created == null || closed == null) {
            return "N/A";
        }

        long totalMinutes = java.time.Duration.between(created, closed).toMinutes();
        long hours = totalMinutes / 60;
        long mins = totalMinutes % 60;

        if (hours > 24) {
            long days = hours / 24;
            hours = hours % 24;
            return days + " day(s), " + hours + "h " + mins + "m";
        }

        return hours + "h " + mins + "m";
    }

    // ============================================================
    // ENGINEER ESCALATION EMAILS
    // ============================================================

    /**
     * Generate email for team lead when engineer escalates a ticket
     */
    public static String generateTeamLeadEscalationEmail(Ticket ticket, String escalationReason) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "    <style>\n" +
                "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; }\n"
                +
                "        .container { max-width: 600px; margin: 0 auto; background: #f9f9f9; }\n" +
                "        .header { background: linear-gradient(135deg, #0066cc 0%, #004499 100%); color: white; padding: 30px 20px; text-align: center; }\n"
                +
                "        .header h1 { margin: 0; font-size: 24px; font-weight: 600; }\n" +
                "        .content { background: white; padding: 30px 20px; }\n" +
                "        .section { margin-bottom: 25px; }\n" +
                "        .section-title { font-size: 14px; font-weight: 600; color: #0066cc; text-transform: uppercase; margin-bottom: 12px; border-bottom: 2px solid #0066cc; padding-bottom: 8px; }\n"
                +
                "        .field { display: flex; margin-bottom: 10px; }\n" +
                "        .field-label { font-weight: 600; width: 140px; color: #0066cc; }\n" +
                "        .field-value { flex: 1; color: #333; }\n" +
                "        .highlight { background-color: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; margin: 15px 0; }\n"
                +
                "        .footer { background: #f0f0f0; padding: 20px; text-align: center; font-size: 12px; color: #666; border-top: 1px solid #ddd; }\n"
                +
                "        .button { display: inline-block; background: #0066cc; color: white; padding: 10px 25px; text-decoration: none; border-radius: 4px; margin-top: 15px; }\n"
                +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='container'>\n" +
                "        <div class='header'>\n" +
                "            <h1>🚨 TICKET ESCALATION</h1>\n" +
                "        </div>\n" +
                "        <div class='content'>\n" +
                "            <p>A ticket has been escalated by an engineer and requires your immediate attention.</p>\n"
                +
                "            \n" +
                "            <div class='section'>\n" +
                "                <div class='section-title'>Ticket Details</div>\n" +
                "                <div class='field'>\n" +
                "                    <div class='field-label'>Ticket ID</div>\n" +
                "                    <div class='field-value'>" + ticket.getTicketId() + "</div>\n" +
                "                </div>\n" +
                "                <div class='field'>\n" +
                "                    <div class='field-label'>Priority</div>\n" +
                "                    <div class='field-value'>" + ticket.getPriority() + "</div>\n" +
                "                </div>\n" +
                "                <div class='field'>\n" +
                "                    <div class='field-label'>Category</div>\n" +
                "                    <div class='field-value'>" + ticket.getCategory() + "</div>\n" +
                "                </div>\n" +
                "                <div class='field'>\n" +
                "                    <div class='field-label'>Status</div>\n" +
                "                    <div class='field-value'>ESCALATED</div>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div class='section'>\n" +
                "                <div class='section-title'>Issue Description</div>\n" +
                "                <p>" + ticket.getIssueDescription() + "</p>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div class='section'>\n" +
                "                <div class='section-title'>Escalation Reason</div>\n" +
                "                <div class='highlight'>\n" +
                "                    <strong>" + escalationReason + "</strong>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div class='section'>\n" +
                "                <div class='section-title'>Escalated By</div>\n" +
                "                <div class='field'>\n" +
                "                    <div class='field-label'>Engineer</div>\n" +
                "                    <div class='field-value'>" + ticket.getAssignedEngineer() + "</div>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            \n" +
                "            <p style='text-align: center; margin-top: 25px;'>\n" +
                "                <a href='" + PORTAL_URL + "/ticket/" + ticket.getTicketId()
                + "' class='button'>View Ticket Details</a>\n" +
                "            </p>\n" +
                "        </div>\n" +
                "        <div class='footer'>\n" +
                "            <p style='margin: 0;'>" + COMPANY_NAME + " IT Support System</p>\n" +
                "            <p style='margin: 5px 0 0 0;'>This is an automated notification. Please do not reply to this email.</p>\n"
                +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * Generate email for employee when their ticket is escalated
     */
    public static String generateEmployeeEscalationEmail(Ticket ticket) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "    <style>\n" +
                "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; }\n"
                +
                "        .container { max-width: 600px; margin: 0 auto; background: #f9f9f9; }\n" +
                "        .header { background: linear-gradient(135deg, #0066cc 0%, #004499 100%); color: white; padding: 30px 20px; text-align: center; }\n"
                +
                "        .header h1 { margin: 0; font-size: 24px; font-weight: 600; }\n" +
                "        .content { background: white; padding: 30px 20px; }\n" +
                "        .section { margin-bottom: 25px; }\n" +
                "        .section-title { font-size: 14px; font-weight: 600; color: #0066cc; text-transform: uppercase; margin-bottom: 12px; border-bottom: 2px solid #0066cc; padding-bottom: 8px; }\n"
                +
                "        .field { display: flex; margin-bottom: 10px; }\n" +
                "        .field-label { font-weight: 600; width: 140px; color: #0066cc; }\n" +
                "        .field-value { flex: 1; color: #333; }\n" +
                "        .info-box { background: #e7f3ff; padding: 15px; border-left: 4px solid #0066cc; margin: 15px 0; }\n"
                +
                "        .info-box ul { margin: 10px 0; padding-left: 20px; }\n" +
                "        .info-box li { margin: 8px 0; }\n" +
                "        .footer { background: #f0f0f0; padding: 20px; text-align: center; font-size: 12px; color: #666; border-top: 1px solid #ddd; }\n"
                +
                "        .button { display: inline-block; background: #0066cc; color: white; padding: 10px 25px; text-decoration: none; border-radius: 4px; margin-top: 15px; }\n"
                +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='container'>\n" +
                "        <div class='header'>\n" +
                "            <h1>TICKET ESCALATED</h1>\n" +
                "        </div>\n" +
                "        <div class='content'>\n" +
                "            <p>Your IT support ticket has been escalated to a higher authority for priority handling and faster resolution.</p>\n"
                +
                "            \n" +
                "            <div class='section'>\n" +
                "                <div class='section-title'>Ticket Information</div>\n" +
                "                <div class='field'>\n" +
                "                    <div class='field-label'>Ticket ID</div>\n" +
                "                    <div class='field-value'>" + ticket.getTicketId() + "</div>\n" +
                "                </div>\n" +
                "                <div class='field'>\n" +
                "                    <div class='field-label'>Status</div>\n" +
                "                    <div class='field-value'>ESCALATED</div>\n" +
                "                </div>\n" +
                "                <div class='field'>\n" +
                "                    <div class='field-label'>Priority</div>\n" +
                "                    <div class='field-value'>HIGH</div>\n" +
                "                </div>\n" +
                "                <div class='field'>\n" +
                "                    <div class='field-label'>Category</div>\n" +
                "                    <div class='field-value'>" + ticket.getCategory() + "</div>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div class='section'>\n" +
                "                <div class='section-title'>What This Means</div>\n" +
                "                <div class='info-box'>\n" +
                "                    <ul>\n" +
                "                        <li>Your issue has been escalated due to its complexity or urgency</li>\n" +
                "                        <li>Priority level has been raised to HIGH</li>\n" +
                "                        <li>Senior team members are now working on your case</li>\n" +
                "                        <li>You can expect faster resolution</li>\n" +
                "                    </ul>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div class='section'>\n" +
                "                <div class='section-title'>Next Steps</div>\n" +
                "                <p>We are working on your issue with high priority. You will receive updates as progress is made.</p>\n"
                +
                "                <p>If you have additional information, please include your ticket ID <strong>"
                + ticket.getTicketId() + "</strong> when replying to this email.</p>\n" +
                "            </div>\n" +
                "            \n" +
                "            <p style='text-align: center; margin-top: 25px;'>\n" +
                "                <a href='" + PORTAL_URL + "/ticket/" + ticket.getTicketId()
                + "' class='button'>Track Your Ticket</a>\n" +
                "            </p>\n" +
                "        </div>\n" +
                "        <div class='footer'>\n" +
                "            <p style='margin: 0;'>" + COMPANY_NAME + " IT Support System</p>\n" +
                "            <p style='margin: 5px 0 0 0;'>This is an automated notification. Please do not reply to this email.</p>\n"
                +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * Generate confirmation email for engineer when they escalate a ticket
     */
    public static String generateEngineerEscalationConfirmationEmail(Ticket ticket) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "    <style>\n" +
                "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; }\n"
                +
                "        .container { max-width: 600px; margin: 0 auto; background: #f9f9f9; }\n" +
                "        .header { background: linear-gradient(135deg, #28a745 0%, #1e7e34 100%); color: white; padding: 30px 20px; text-align: center; }\n"
                +
                "        .header h1 { margin: 0; font-size: 24px; font-weight: 600; }\n" +
                "        .content { background: white; padding: 30px 20px; }\n" +
                "        .section { margin-bottom: 25px; }\n" +
                "        .section-title { font-size: 14px; font-weight: 600; color: #28a745; text-transform: uppercase; margin-bottom: 12px; border-bottom: 2px solid #28a745; padding-bottom: 8px; }\n"
                +
                "        .field { display: flex; margin-bottom: 10px; }\n" +
                "        .field-label { font-weight: 600; width: 140px; color: #28a745; }\n" +
                "        .field-value { flex: 1; color: #333; }\n" +
                "        .success-box { background: #d4edda; padding: 15px; border-left: 4px solid #28a745; margin: 15px 0; color: #155724; }\n"
                +
                "        .footer { background: #f0f0f0; padding: 20px; text-align: center; font-size: 12px; color: #666; border-top: 1px solid #ddd; }\n"
                +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class='container'>\n" +
                "        <div class='header'>\n" +
                "            <h1>✅ ESCALATION SUCCESSFUL</h1>\n" +
                "        </div>\n" +
                "        <div class='content'>\n" +
                "            <div class='success-box'>\n" +
                "                <strong>Your escalation has been successfully processed.</strong>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div class='section'>\n" +
                "                <div class='section-title'>Ticket Details</div>\n" +
                "                <div class='field'>\n" +
                "                    <div class='field-label'>Ticket ID</div>\n" +
                "                    <div class='field-value'>" + ticket.getTicketId() + "</div>\n" +
                "                </div>\n" +
                "                <div class='field'>\n" +
                "                    <div class='field-label'>Status</div>\n" +
                "                    <div class='field-value'>ESCALATED</div>\n" +
                "                </div>\n" +
                "                <div class='field'>\n" +
                "                    <div class='field-label'>Priority</div>\n" +
                "                    <div class='field-value'>" + ticket.getPriority() + "</div>\n" +
                "                </div>\n" +
                "                <div class='field'>\n" +
                "                    <div class='field-label'>Escalated At</div>\n" +
                "                    <div class='field-value'>"
                + (ticket.getEscalatedTime() != null ? ticket.getEscalatedTime().format(DATE_FORMAT) : "Now")
                + "</div>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div class='section'>\n" +
                "                <div class='section-title'>Escalated To</div>\n" +
                "                <div class='field'>\n" +
                "                    <div class='field-label'>Team Lead</div>\n" +
                "                    <div class='field-value'>" + ticket.getEscalatedToTeamLead() + "</div>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            \n" +
                "            <p>The above team lead will now be handling this ticket with higher priority.</p>\n" +
                "        </div>\n" +
                "        <div class='footer'>\n" +
                "            <p style='margin: 0;'>" + COMPANY_NAME + " IT Support System</p>\n" +
                "            <p style='margin: 5px 0 0 0;'>This is an automated notification. Please do not reply to this email.</p>\n"
                +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}
