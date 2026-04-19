package com.powergrid.ticketsystem.notification;

import com.powergrid.ticketsystem.entity.Ticket;
import java.time.format.DateTimeFormatter;

/**
 * SIMPLE, CLEAN EMAIL TEMPLATE SERVICE
 * Table-based layout, inline CSS only
 * Gmail & Outlook compatible
 */
public class SimpleEmailTemplateService {

        private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        /**
         * Generic email template - works for all ticket emails
         * Parameters: title, icon, description, ticketId, subject, category, priority,
         * createdTime, slaDeadline, issueDescription
         */
        public static String generateEmailTemplate(
                        String title, String icon, String description,
                        String ticketId, String subject, String category, String priority,
                        String createdTime, String slaDeadline, String issueDescription,
                        String actionItems) {

                String normalizedDescription = description != null ? description.trim() : "";
                boolean descriptionHasGreeting = normalizedDescription.toLowerCase().startsWith("hello");
                String greetingPrefix = descriptionHasGreeting ? "" : "Hello,<br><br>";

                String priorityColor = priority.equalsIgnoreCase("HIGH") ? "#FF6B35"
                                : priority.equalsIgnoreCase("CRITICAL") ? "#D32F2F"
                                                : priority.equalsIgnoreCase("MEDIUM") ? "#FF9800" : "#4CAF50";

                return "<!DOCTYPE html>" +
                                "<html>" +
                                "<head>" +
                                "<meta charset='UTF-8'>" +
                                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                                "</head>" +
                                "<body style='font-family: Arial, sans-serif; background-color: #f4f6f8; margin: 0; padding: 20px;'>"
                                +

                                // OUTER WRAPPER TABLE TO CENTER CONTENT
                                "<table width='100%' border='0' cellpadding='0' cellspacing='0' style='width: 100%;'>" +
                                "<tbody>" +
                                "<tr>" +
                                "<td align='center' style='text-align: center;'>" +

                                // SINGLE CENTERED CONTAINER - 600px fixed width
                                "<table width='600' align='center' border='0' cellpadding='0' cellspacing='0' style='width: 600px; background-color: #ffffff; border-radius: 8px; overflow: hidden;'>"
                                +
                                "<tbody>" +

                                // HEADER SECTION - Inside main container
                                "<tr>" +
                                "<td style='text-align: center; padding: 30px 20px;'>" +
                                "<div style='font-size: 48px; margin-bottom: 10px;'>" + icon + "</div>" +
                                "<h1 style='color: #333333; font-size: 24px; margin: 0; font-weight: bold;'>" + title
                                + "</h1>" +
                                "</td>" +
                                "</tr>" +

                                // DIVIDER
                                "<tr>" +
                                "<td style='height: 1px; background-color: #eeeeee;'></td>" +
                                "</tr>" +

                                // GREETING & DESCRIPTION
                                "<tr>" +
                                "<td style='padding: 20px 20px;'>" +
                                "<div style='text-align: left; color: #555555; font-size: 14px; line-height: 1.6;'>" +
                                greetingPrefix +
                                normalizedDescription +
                                "</div>" +
                                "</td>" +
                                "</tr>" +

                                // DIVIDER
                                "<tr>" +
                                "<td style='height: 1px; background-color: #eeeeee;'></td>" +
                                "</tr>" +

                                // TICKET DETAILS SECTION
                                "<tr>" +
                                "<td style='border-left: 4px solid #5E35B1; padding: 20px 20px; text-align: left;'>" +
                                "<div style='color: #1a1a1a; font-size: 12px; font-weight: bold; margin-bottom: 15px; text-transform: uppercase;'>📌 TICKET DETAILS</div>"
                                +

                                // Details table
                                "<table width='100%' border='0' cellpadding='8' cellspacing='0' style='margin-bottom: 0;'>"
                                +
                                "<tr>" +
                                "<td style='width: 30%; color: #1a1a1a; font-weight: bold; font-size: 13px;'>Ticket ID:</td>"
                                +
                                "<td style='color: #333333; font-size: 13px;'><strong>" + ticketId + "</strong></td>" +
                                "</tr>" +
                                "<tr>" +
                                "<td style='color: #1a1a1a; font-weight: bold; font-size: 13px;'>Subject:</td>" +
                                "<td style='color: #333333; font-size: 13px;'>" + subject + "</td>" +
                                "</tr>" +
                                "<tr>" +
                                "<td style='color: #1a1a1a; font-weight: bold; font-size: 13px;'>Category:</td>" +
                                "<td style='color: #333333; font-size: 13px;'>" + category + "</td>" +
                                "</tr>" +
                                "<tr>" +
                                "<td style='color: #1a1a1a; font-weight: bold; font-size: 13px;'>Priority:</td>" +
                                "<td style='font-size: 13px;'><span style='background-color: " + priorityColor
                                + "; color: white; padding: 4px 12px; border-radius: 4px; font-weight: bold; display: inline-block;'>"
                                + priority + "</span></td>" +
                                "</tr>" +
                                "<tr>" +
                                "<td style='color: #1a1a1a; font-weight: bold; font-size: 13px;'>Created:</td>" +
                                "<td style='color: #333333; font-size: 13px;'>" + createdTime + "</td>" +
                                "</tr>" +
                                (slaDeadline != null && !slaDeadline.equals("N/A") ? "<tr>" +
                                                "<td style='color: #1a1a1a; font-weight: bold; font-size: 13px;'>SLA Deadline:</td>"
                                                +
                                                "<td style='color: #333333; font-size: 13px;'>" + slaDeadline + "</td>"
                                                +
                                                "</tr>" : "")
                                +
                                "</table>" +

                                "</td>" +
                                "</tr>" +

                                // DIVIDER
                                "<tr>" +
                                "<td style='height: 1px; background-color: #eeeeee;'></td>" +
                                "</tr>" +

                                // ISSUE DESCRIPTION
                                "<tr>" +
                                "<td style='border-left: 4px solid #5E35B1; padding: 20px 20px; text-align: left;'>" +
                                "<div style='color: #1a1a1a; font-size: 12px; font-weight: bold; margin-bottom: 15px; text-transform: uppercase;'>📝 ISSUE DESCRIPTION</div>"
                                +
                                "<div style='color: #333333; font-size: 13px; line-height: 1.6;'>" + issueDescription
                                + "</div>" +
                                "</td>" +
                                "</tr>" +

                                // DIVIDER
                                "<tr>" +
                                "<td style='height: 1px; background-color: #eeeeee;'></td>" +
                                "</tr>" +

                                // ACTION REQUIRED
                                "<tr>" +
                                "<td style='border-left: 4px solid #5E35B1; padding: 20px 20px; text-align: left;'>" +
                                "<div style='color: #1a1a1a; font-size: 12px; font-weight: bold; margin-bottom: 15px; text-transform: uppercase;'>✓ ACTION REQUIRED</div>"
                                +
                                "<div style='color: #333333; font-size: 13px; line-height: 1.8;'>" + actionItems
                                + "</div>" +
                                "</td>" +
                                "</tr>" +

                                // DIVIDER
                                "<tr>" +
                                "<td style='height: 1px; background-color: #eeeeee;'></td>" +
                                "</tr>" +

                                // FOOTER
                                "<tr>" +
                                "<td style='text-align: center; padding: 20px 20px;'>" +
                                "<div style='color: #666666; font-size: 11px; line-height: 1.6;'>" +
                                "<strong style='color: #333333;'>PowerGrid IT Support</strong><br>" +
                                "Email: support@powergrid.com<br>" +
                                "© 2026 PowerGrid Corporation. All rights reserved.<br>" +
                                "Reply YES or NO to proceed with your ticket." +
                                "</div>" +
                                "</td>" +
                                "</tr>" +

                                "</tbody>" +
                                "</table>" +

                                // CLOSE OUTER WRAPPER
                                "</td>" +
                                "</tr>" +
                                "</tbody>" +
                                "</table>" +

                                "</body>" +
                                "</html>";
        }

        /**
         * TICKET ASSIGNED EMAIL (For Engineers)
         */
        public static String generateTicketAssignedEmail(Ticket ticket, String engineerName, String engineerTeam) {
                String actionItems = "1. Review ticket details<br>" +
                                "2. Start working on the issue<br>" +
                                "3. Update status as you progress<br>" +
                                "4. Request help if needed";

                // Include engineer name and team in greeting
                String greeting = "Hello "
                                + (engineerName != null && !engineerName.isEmpty() ? engineerName + " ("
                                                + (engineerTeam != null ? engineerTeam : "N/A") + ")" : "Engineer")
                                + ",<br><br>" +
                                "A new IT support ticket has been assigned to you. Please review and begin working on this ticket at your earliest convenience.";

                return generateEmailTemplate(
                                "Ticket Assigned",
                                "📌",
                                greeting,
                                ticket.getTicketId() != null ? ticket.getTicketId() : "N/A",
                                ticket.getEmailSubject() != null ? ticket.getEmailSubject() : "No Subject",
                                ticket.getCategory() != null ? ticket.getCategory() : "General",
                                ticket.getPriority() != null ? ticket.getPriority() : "MEDIUM",
                                ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT) : "N/A",
                                ticket.getSlaDeadline() != null ? ticket.getSlaDeadline().format(DATE_FORMAT) : null,
                                ticket.getIssueDescription() != null ? ticket.getIssueDescription() : "No description",
                                actionItems);
        }

        /**
         * TICKET ASSIGNED EMAIL - Backward compatibility version
         */
        public static String generateTicketAssignedEmail(Ticket ticket) {
                return generateTicketAssignedEmail(ticket, ticket.getAssignedEngineer(), ticket.getAssignedTeam());
        }

        /**
         * TICKET RESOLVED EMAIL
         */
        public static String generateTicketResolvedEmail(Ticket ticket) {
                String actionItems = "✓ Issue has been resolved<br>" +
                                "✓ Resolution details have been documented<br>" +
                                "✓ You will be contacted if additional information is needed";

                return generateEmailTemplate(
                                "Ticket Resolved",
                                "✅",
                                "We are pleased to inform you that your IT support ticket has been resolved by our technical team.",
                                ticket.getTicketId() != null ? ticket.getTicketId() : "N/A",
                                ticket.getEmailSubject() != null ? ticket.getEmailSubject() : "No Subject",
                                ticket.getCategory() != null ? ticket.getCategory() : "General",
                                ticket.getPriority() != null ? ticket.getPriority() : "MEDIUM",
                                ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT) : "N/A",
                                null,
                                ticket.getIssueDescription() != null ? ticket.getIssueDescription() : "No description",
                                actionItems);
        }

        /**
         * TICKET ESCALATED EMAIL (For Employees - when KB solution is rejected)
         */
        public static String generateTicketEscalatedEmail(Ticket ticket) {
                String actionItems = "⚠️ Your ticket has been escalated<br>" +
                                "⚠️ Senior team is now handling this<br>" +
                                "⚠️ You will receive updates soon";

                // Generic greeting for employees - no team name
                String description = "Your IT support ticket has been escalated due to exceeding SLA timeframe. Our senior team is now working on resolving this urgently.";

                return generateEmailTemplate(
                                "Ticket Escalated",
                                "⚠️",
                                description,
                                ticket.getTicketId() != null ? ticket.getTicketId() : "N/A",
                                ticket.getEmailSubject() != null ? ticket.getEmailSubject() : "No Subject",
                                ticket.getCategory() != null ? ticket.getCategory() : "General",
                                "CRITICAL",
                                ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT) : "N/A",
                                ticket.getSlaDeadline() != null ? ticket.getSlaDeadline().format(DATE_FORMAT) : null,
                                ticket.getIssueDescription() != null ? ticket.getIssueDescription() : "No description",
                                actionItems);
        }

        /**
         * SOLUTION DELIVERY EMAIL (Self-Service KB)
         * Special version with YES/NO reply option
         */
        public static String generateSolutionDeliveryEmail(Ticket ticket, String solutionSteps) {
                String priorityColor = ticket.getPriority() != null && ticket.getPriority().equalsIgnoreCase("HIGH")
                                ? "#FF6B35"
                                : ticket.getPriority() != null && ticket.getPriority().equalsIgnoreCase("CRITICAL")
                                                ? "#D32F2F"
                                                : ticket.getPriority() != null
                                                                && ticket.getPriority().equalsIgnoreCase("MEDIUM")
                                                                                ? "#FF9800"
                                                                                : "#4CAF50";

                String actionItems = "1. Follow the solution steps below<br>" +
                                "2. Try each step carefully<br>" +
                                "3. Reply YES if issue is resolved<br>" +
                                "4. Reply NO if you need more help";

                return "<!DOCTYPE html>" +
                                "<html>" +
                                "<head>" +
                                "<meta charset='UTF-8'>" +
                                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                                "</head>" +
                                "<body style='font-family: Arial, sans-serif; background-color: #f4f6f8; margin: 0; padding: 20px;'>"
                                +
                                "<table width='100%' border='0' cellpadding='0' cellspacing='0' style='width: 100%;'>" +
                                "<tbody>" +
                                "<tr>" +
                                "<td align='center' style='text-align: center;'>" +
                                "<table width='600' align='center' border='0' cellpadding='0' cellspacing='0' style='width: 600px; background-color: #ffffff; border-radius: 8px; overflow: hidden;'>"
                                +
                                "<tbody>" +
                                // HEADER
                                "<tr>" +
                                "<td style='text-align: center; padding: 30px 20px;'>" +
                                "<div style='font-size: 48px; margin-bottom: 10px;'>💡</div>" +
                                "<h1 style='color: #333333; font-size: 24px; margin: 0; font-weight: bold;'>Solution Available</h1>"
                                +
                                "</td>" +
                                "</tr>" +

                                // DIVIDER
                                "<tr>" +
                                "<td style='height: 1px; background-color: #eeeeee;'></td>" +
                                "</tr>" +

                                // GREETING
                                "<tr>" +
                                "<td style='padding: 20px 20px; color: #555555; font-size: 14px; line-height: 1.6;'>" +
                                "We've detected an issue similar to one in our knowledge base and have a solution for you. Please try the following steps to resolve your issue."
                                +
                                "</td>" +
                                "</tr>" +

                                // DIVIDER
                                "<tr>" +
                                "<td style='height: 1px; background-color: #eeeeee;'></td>" +
                                "</tr>" +

                                // TICKET DETAILS
                                "<tr>" +
                                "<td style='border-left: 4px solid #5E35B1; padding: 20px 20px; text-align: left;'>" +
                                "<div style='color: #1a1a1a; font-size: 12px; font-weight: bold; margin-bottom: 15px; text-transform: uppercase;'>📌 TICKET DETAILS</div>"
                                +
                                "<table width='100%' border='0' cellpadding='8' cellspacing='0'>" +
                                "<tr><td style='width: 30%; color: #1a1a1a; font-weight: bold; font-size: 13px;'>Ticket ID:</td><td style='color: #333333; font-size: 13px;'><strong>"
                                + (ticket.getTicketId() != null ? ticket.getTicketId() : "N/A") + "</strong></td></tr>"
                                +
                                "<tr><td style='color: #1a1a1a; font-weight: bold; font-size: 13px;'>Subject:</td><td style='color: #333333; font-size: 13px;'>"
                                + (ticket.getEmailSubject() != null ? ticket.getEmailSubject() : "No Subject")
                                + "</td></tr>" +
                                "<tr><td style='color: #1a1a1a; font-weight: bold; font-size: 13px;'>Category:</td><td style='color: #333333; font-size: 13px;'>"
                                + (ticket.getCategory() != null ? ticket.getCategory() : "General") + "</td></tr>" +
                                "<tr><td style='color: #1a1a1a; font-weight: bold; font-size: 13px;'>Priority:</td><td style='font-size: 13px;'><span style='background-color: "
                                + priorityColor
                                + "; color: white; padding: 4px 12px; border-radius: 4px; font-weight: bold; display: inline-block;'>"
                                + (ticket.getPriority() != null ? ticket.getPriority() : "MEDIUM") + "</span></td></tr>"
                                +
                                "<tr><td style='color: #1a1a1a; font-weight: bold; font-size: 13px;'>Created:</td><td style='color: #333333; font-size: 13px;'>"
                                + (ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT)
                                                : "N/A")
                                + "</td></tr>" +
                                "</table>" +
                                "</td>" +
                                "</tr>" +

                                // DIVIDER
                                "<tr>" +
                                "<td style='height: 1px; background-color: #eeeeee;'></td>" +
                                "</tr>" +

                                // ISSUE DESCRIPTION
                                "<tr>" +
                                "<td style='border-left: 4px solid #5E35B1; padding: 20px 20px; text-align: left;'>" +
                                "<div style='color: #1a1a1a; font-size: 12px; font-weight: bold; margin-bottom: 15px; text-transform: uppercase;'>📝 ISSUE DESCRIPTION</div>"
                                +
                                "<div style='color: #333333; font-size: 13px; line-height: 1.6;'>"
                                + (ticket.getIssueDescription() != null ? ticket.getIssueDescription()
                                                : "No description")
                                + "</div>" +
                                "</td>" +
                                "</tr>" +

                                // DIVIDER
                                "<tr>" +
                                "<td style='height: 1px; background-color: #eeeeee;'></td>" +
                                "</tr>" +

                                // SOLUTION STEPS
                                "<tr>" +
                                "<td style='border-left: 4px solid #5E35B1; padding: 20px 20px; text-align: left;'>" +
                                "<div style='color: #1a1a1a; font-size: 12px; font-weight: bold; margin-bottom: 15px; text-transform: uppercase;'>💡 SOLUTION STEPS</div>"
                                +
                                "<div style='color: #333333; font-size: 13px; line-height: 1.8;'>"
                                + (solutionSteps != null ? solutionSteps.replace("\n", "<br>") : "No steps available")
                                + "</div>" +
                                "</td>" +
                                "</tr>" +

                                // DIVIDER
                                "<tr>" +
                                "<td style='height: 1px; background-color: #eeeeee;'></td>" +
                                "</tr>" +

                                // ACTION REQUIRED
                                "<tr>" +
                                "<td style='border-left: 4px solid #5E35B1; padding: 20px 20px; text-align: left;'>" +
                                "<div style='color: #1a1a1a; font-size: 12px; font-weight: bold; margin-bottom: 15px; text-transform: uppercase;'>✓ ACTION REQUIRED</div>"
                                +
                                "<div style='color: #333333; font-size: 13px; line-height: 1.8;'>" + actionItems
                                + "</div>" +
                                "</td>" +
                                "</tr>" +

                                // DIVIDER
                                "<tr>" +
                                "<td style='height: 1px; background-color: #eeeeee;'></td>" +
                                "</tr>" +

                                // FOOTER
                                "<tr>" +
                                "<td style='text-align: center; padding: 20px 20px;'>" +
                                "<div style='color: #666666; font-size: 11px; line-height: 1.6;'>" +
                                "<strong style='color: #333333;'>PowerGrid IT Support</strong><br>" +
                                "Email: support@powergrid.com<br>" +
                                "© 2026 PowerGrid Corporation. All rights reserved.<br>" +
                                "Reply YES or NO to proceed with your ticket." +
                                "</div>" +
                                "</td>" +
                                "</tr>" +

                                "</tbody>" +
                                "</table>" +

                                // CLOSE OUTER WRAPPER
                                "</td>" +
                                "</tr>" +
                                "</tbody>" +
                                "</table>" +

                                "</body>" +
                                "</html>";
        }

        /**
         * SLA WARNING EMAIL (For Engineers)
         */
        public static String generateSlaWarningEmail(Ticket ticket) {
                String actionItems = "⏰ 75% of SLA time has been used<br>" +
                                "⏰ Please accelerate resolution<br>" +
                                "⏰ Escalate if additional resources needed";

                // Include team name for engineers
                String greeting = "Hello "
                                + (ticket.getAssignedTeam() != null ? ticket.getAssignedTeam() + " Team," : "Team,")
                                + "<br><br>" +
                                "This is a courtesy reminder that the following ticket is approaching its SLA deadline. Please take immediate action to resolve this issue.";

                return generateEmailTemplate(
                                "SLA Warning",
                                "⏰",
                                greeting,
                                ticket.getTicketId() != null ? ticket.getTicketId() : "N/A",
                                ticket.getEmailSubject() != null ? ticket.getEmailSubject() : "No Subject",
                                ticket.getCategory() != null ? ticket.getCategory() : "General",
                                "MEDIUM",
                                ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT) : "N/A",
                                ticket.getSlaDeadline() != null ? ticket.getSlaDeadline().format(DATE_FORMAT) : null,
                                ticket.getIssueDescription() != null ? ticket.getIssueDescription() : "No description",
                                actionItems);
        }

        /**
         * TICKET CLOSED EMAIL
         */
        public static String generateTicketClosedEmail(Ticket ticket) {
                String actionItems = "✓ Issue has been verified resolved<br>" +
                                "✓ Ticket is now officially CLOSED<br>" +
                                "✓ Thank you for using IT Support";

                return generateEmailTemplate(
                                "Ticket Closed",
                                "🎉",
                                "Your IT support ticket has been verified and is now officially CLOSED. Thank you for using our service!",
                                ticket.getTicketId() != null ? ticket.getTicketId() : "N/A",
                                ticket.getEmailSubject() != null ? ticket.getEmailSubject() : "No Subject",
                                ticket.getCategory() != null ? ticket.getCategory() : "General",
                                ticket.getPriority() != null ? ticket.getPriority() : "MEDIUM",
                                ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT) : "N/A",
                                null,
                                ticket.getIssueDescription() != null ? ticket.getIssueDescription() : "No description",
                                actionItems);
        }

        /**
         * TICKET REOPENED EMAIL (For Engineers)
         */
        public static String generateTicketReopenedEmail(Ticket ticket) {
                String actionItems = "🔄 Ticket has been reopened<br>" +
                                "🔄 More work is needed<br>" +
                                "🔄 Please continue resolution efforts";

                // Include team name for engineers
                String greeting = "Hello "
                                + (ticket.getAssignedTeam() != null ? ticket.getAssignedTeam() + " Team," : "Team,")
                                + "<br><br>" +
                                "Your IT support ticket has been reopened. The verification process detected that additional work is needed to fully resolve this issue.";

                return generateEmailTemplate(
                                "Ticket Reopened",
                                "🔄",
                                greeting,
                                ticket.getTicketId() != null ? ticket.getTicketId() : "N/A",
                                ticket.getEmailSubject() != null ? ticket.getEmailSubject() : "No Subject",
                                ticket.getCategory() != null ? ticket.getCategory() : "General",
                                ticket.getPriority() != null ? ticket.getPriority() : "MEDIUM",
                                ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT) : "N/A",
                                null,
                                ticket.getIssueDescription() != null ? ticket.getIssueDescription() : "No description",
                                actionItems);
        }

        /**
         * NEW TICKET CREATED EMAIL
         */
        public static String generateNewTicketEmail(Ticket ticket) {
                String actionItems = "1. Review your ticket details<br>" +
                                "2. Our team will review your request<br>" +
                                "3. You will receive updates via email<br>" +
                                "4. Your ticket reference: "
                                + (ticket.getTicketId() != null ? ticket.getTicketId() : "TBD");

                return generateEmailTemplate(
                                "Ticket Created",
                                "📋",
                                "Thank you for contacting IT Support. Your ticket has been successfully created and is now in our system. Our team will review your request and respond shortly.",
                                ticket.getTicketId() != null ? ticket.getTicketId() : "N/A",
                                ticket.getEmailSubject() != null ? ticket.getEmailSubject() : "No Subject",
                                ticket.getCategory() != null ? ticket.getCategory() : "General",
                                ticket.getPriority() != null ? ticket.getPriority() : "MEDIUM",
                                ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT) : "N/A",
                                ticket.getSlaDeadline() != null ? ticket.getSlaDeadline().format(DATE_FORMAT) : null,
                                ticket.getIssueDescription() != null ? ticket.getIssueDescription() : "No description",
                                actionItems);
        }

        /**
         * TEAM LEAD ESCALATION EMAIL
         */
        public static String generateTeamLeadEscalationEmail(Ticket ticket, String escalationReason,
                        String engineerName, String engineerTeam) {
                return generateTeamLeadEscalationEmail(ticket, escalationReason, engineerName, engineerTeam,
                                ticket.getEscalatedToTeamLead());
        }

        /**
         * TEAM LEAD ESCALATION EMAIL - with recipient personalization
         */
        public static String generateTeamLeadEscalationEmail(Ticket ticket, String escalationReason,
                        String engineerName, String engineerTeam, String recipientName) {
                String actionItems = "⚠️ Ticket escalated due to SLA breach<br>" +
                                "⚠️ Please review ticket details<br>" +
                                "⚠️ Assign senior resources if needed<br>" +
                                "⚠️ Provide resolution timeline";

                String reasonText = escalationReason != null && !escalationReason.isEmpty()
                                ? "Escalation Reason: " + escalationReason + "<br>"
                                : "";

                String escalatedByText = "";
                if (engineerName != null && !engineerName.isEmpty()) {
                        escalatedByText = "<br>Escalated by: " + engineerName + " ("
                                        + (engineerTeam != null ? engineerTeam : "N/A") + ")";
                }

                String safeRecipientName = (recipientName != null && !recipientName.isBlank())
                                ? recipientName
                                : "Team Lead";
                String personalizedDescription = "Hello " + safeRecipientName + ",<br><br>"
                                + "A ticket assigned to your team has breached SLA and been escalated. " + reasonText
                                + "Your immediate attention and action is required." + escalatedByText;

                return generateEmailTemplate(
                                "Ticket Escalated",
                                "⚠️",
                                personalizedDescription,
                                ticket.getTicketId() != null ? ticket.getTicketId() : "N/A",
                                ticket.getEmailSubject() != null ? ticket.getEmailSubject() : "No Subject",
                                ticket.getCategory() != null ? ticket.getCategory() : "General",
                                "CRITICAL",
                                ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT) : "N/A",
                                ticket.getSlaDeadline() != null ? ticket.getSlaDeadline().format(DATE_FORMAT) : null,
                                ticket.getIssueDescription() != null ? ticket.getIssueDescription() : "No description",
                                actionItems);
        }

        /**
         * TEAM LEAD ESCALATION EMAIL - Backward compatibility version
         */
        public static String generateTeamLeadEscalationEmail(Ticket ticket, String escalationReason) {
                return generateTeamLeadEscalationEmail(ticket, escalationReason, ticket.getAssignedEngineer(),
                                ticket.getAssignedTeam());
        }

        /**
         * EMPLOYEE ESCALATION NOTIFICATION EMAIL
         */
        public static String generateEmployeeEscalationEmail(Ticket ticket) {
                String actionItems = "✓ Your ticket has been escalated<br>" +
                                "✓ Senior support team is now assigned<br>" +
                                "✓ You will receive updates soon<br>" +
                                "✓ We appreciate your patience";

                return generateEmailTemplate(
                                "Ticket Escalated",
                                "📢",
                                "Your IT support ticket has been escalated to our senior support team due to complexity. You will receive priority attention and regular updates.",
                                ticket.getTicketId() != null ? ticket.getTicketId() : "N/A",
                                ticket.getEmailSubject() != null ? ticket.getEmailSubject() : "No Subject",
                                ticket.getCategory() != null ? ticket.getCategory() : "General",
                                ticket.getPriority() != null ? ticket.getPriority() : "MEDIUM",
                                ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT) : "N/A",
                                ticket.getSlaDeadline() != null ? ticket.getSlaDeadline().format(DATE_FORMAT) : null,
                                ticket.getIssueDescription() != null ? ticket.getIssueDescription() : "No description",
                                actionItems);
        }

        /**
         * ENGINEER ESCALATION CONFIRMATION EMAIL - With personalization
         */
        public static String generateEngineerEscalationConfirmationEmail(Ticket ticket, String engineerName,
                        String engineerTeam) {
                String actionItems = "✓ Escalation received and logged<br>" +
                                "✓ Ticket transferred to senior team<br>" +
                                "✓ You will receive resolution updates<br>" +
                                "✓ Thank you for your effort";

                // Personalized greeting with engineer name and team
                String description = "Hello " + (engineerName != null && !engineerName.isEmpty()
                                ? engineerName + " (" + (engineerTeam != null ? engineerTeam : "N/A") + ")"
                                : "Engineer") + ",<br><br>" +
                                "Your escalation request for this ticket has been confirmed and received. The ticket has been transferred to our senior support team for priority handling.";

                return generateEmailTemplate(
                                "Escalation Confirmed",
                                "✅",
                                description,
                                ticket.getTicketId() != null ? ticket.getTicketId() : "N/A",
                                ticket.getEmailSubject() != null ? ticket.getEmailSubject() : "No Subject",
                                ticket.getCategory() != null ? ticket.getCategory() : "General",
                                ticket.getPriority() != null ? ticket.getPriority() : "MEDIUM",
                                ticket.getCreatedTime() != null ? ticket.getCreatedTime().format(DATE_FORMAT) : "N/A",
                                null,
                                ticket.getIssueDescription() != null ? ticket.getIssueDescription() : "No description",
                                actionItems);
        }

        /**
         * ENGINEER ESCALATION CONFIRMATION EMAIL - Backward compatibility version (for
         * old calls)
         */
        public static String generateEngineerEscalationConfirmationEmail(Ticket ticket) {
                return generateEngineerEscalationConfirmationEmail(ticket, ticket.getAssignedEngineer(),
                                ticket.getAssignedTeam());
        }
}
