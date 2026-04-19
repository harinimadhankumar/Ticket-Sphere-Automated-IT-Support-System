package com.powergrid.ticketsystem.sla;

import com.powergrid.ticketsystem.constants.EscalationLevel;
import com.powergrid.ticketsystem.constants.SlaConfiguration;
import com.powergrid.ticketsystem.entity.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * SLA Calculation Service - handles deadline, breach detection, escalation.
 * SLA: CRITICAL=2h, HIGH=4h, MEDIUM=8h, LOW=24h
 */
@Service
public class SlaCalculationService {

    private static final Logger logger = LoggerFactory.getLogger(SlaCalculationService.class);

    public LocalDateTime calculateSlaDeadline(Ticket ticket) {
        if (ticket.getCreatedTime() == null) {
            logger.warn("Ticket {} has no created time, using current time", ticket.getTicketId());
            return LocalDateTime.now().plusHours(SlaConfiguration.DEFAULT_SLA_HOURS);
        }
        int slaHours = SlaConfiguration.getSlaHoursForPriority(ticket.getPriority());
        LocalDateTime deadline = ticket.getCreatedTime().plusHours(slaHours);
        logger.debug("SLA deadline for ticket {} (Priority: {}): {} hours = {}", ticket.getTicketId(),
                ticket.getPriority(), slaHours, deadline);
        return deadline;
    }

    public Duration calculateElapsedTime(Ticket ticket) {
        if (ticket.getCreatedTime() == null)
            return Duration.ZERO;
        return Duration.between(ticket.getCreatedTime(), LocalDateTime.now());
    }

    public double calculateElapsedHours(Ticket ticket) {
        return calculateElapsedTime(ticket).toMinutes() / 60.0;
    }

    public Duration calculateRemainingTime(Ticket ticket) {
        LocalDateTime deadline = ticket.getSlaDeadline();
        if (deadline == null)
            deadline = calculateSlaDeadline(ticket);
        return Duration.between(LocalDateTime.now(), deadline);
    }

    public boolean isSlaBreached(Ticket ticket) {
        LocalDateTime deadline = ticket.getSlaDeadline();
        if (deadline == null)
            deadline = calculateSlaDeadline(ticket);
        return LocalDateTime.now().isAfter(deadline);
    }

    public double calculateSlaPercentage(Ticket ticket) {
        int slaMinutes = SlaConfiguration.getSlaMinutesForPriority(ticket.getPriority());
        double elapsedMinutes = calculateElapsedTime(ticket).toMinutes();
        if (slaMinutes == 0)
            return 0;
        return (elapsedMinutes / slaMinutes) * 100.0;
    }

    public Duration calculateOvertime(Ticket ticket) {
        Duration remaining = calculateRemainingTime(ticket);
        return remaining.isNegative() ? remaining.negated() : Duration.ZERO;
    }

    public double calculateOvertimeHours(Ticket ticket) {
        return calculateOvertime(ticket).toMinutes() / 60.0;
    }

    public EscalationLevel determineEscalationLevel(Ticket ticket) {
        double slaPercentage = calculateSlaPercentage(ticket);
        if (slaPercentage >= SlaConfiguration.LEVEL_3_THRESHOLD_PERCENT)
            return EscalationLevel.LEVEL_3;
        if (slaPercentage >= SlaConfiguration.LEVEL_2_THRESHOLD_PERCENT)
            return EscalationLevel.LEVEL_2;
        if (slaPercentage >= SlaConfiguration.LEVEL_1_THRESHOLD_PERCENT)
            return EscalationLevel.LEVEL_1;
        return EscalationLevel.NONE;
    }

    public boolean isInWarningZone(Ticket ticket) {
        double slaPercentage = calculateSlaPercentage(ticket);
        return slaPercentage >= SlaConfiguration.WARNING_THRESHOLD_PERCENT
                && slaPercentage < SlaConfiguration.LEVEL_1_THRESHOLD_PERCENT;
    }

    public boolean needsEscalationUpgrade(Ticket ticket) {
        if (ticket.getEscalationLevel() == null)
            return isSlaBreached(ticket);
        EscalationLevel currentLevel;
        try {
            currentLevel = EscalationLevel.valueOf(ticket.getEscalationLevel());
        } catch (IllegalArgumentException e) {
            currentLevel = EscalationLevel.NONE;
        }
        return determineEscalationLevel(ticket).getSeverity() > currentLevel.getSeverity();
    }

    public SlaStatus getSlaStatus(Ticket ticket) {
        return new SlaStatus(ticket.getTicketId(), ticket.getPriority(), calculateSlaDeadline(ticket),
                isSlaBreached(ticket), calculateSlaPercentage(ticket), calculateRemainingTime(ticket),
                calculateOvertime(ticket), determineEscalationLevel(ticket), isInWarningZone(ticket));
    }

    public static class SlaStatus {
        private final String ticketId;
        private final String priority;
        private final LocalDateTime slaDeadline;
        private final boolean breached;
        private final double slaPercentage;
        private final Duration remainingTime;
        private final Duration overtime;
        private final EscalationLevel escalationLevel;
        private final boolean inWarningZone;

        public SlaStatus(String ticketId, String priority, LocalDateTime slaDeadline, boolean breached,
                double slaPercentage, Duration remainingTime, Duration overtime, EscalationLevel escalationLevel,
                boolean inWarningZone) {
            this.ticketId = ticketId;
            this.priority = priority;
            this.slaDeadline = slaDeadline;
            this.breached = breached;
            this.slaPercentage = slaPercentage;
            this.remainingTime = remainingTime;
            this.overtime = overtime;
            this.escalationLevel = escalationLevel;
            this.inWarningZone = inWarningZone;
        }

        public String getTicketId() {
            return ticketId;
        }

        public String getPriority() {
            return priority;
        }

        public LocalDateTime getSlaDeadline() {
            return slaDeadline;
        }

        public boolean isBreached() {
            return breached;
        }

        public double getSlaPercentage() {
            return slaPercentage;
        }

        public Duration getRemainingTime() {
            return remainingTime;
        }

        public Duration getOvertime() {
            return overtime;
        }

        public EscalationLevel getEscalationLevel() {
            return escalationLevel;
        }

        public boolean isInWarningZone() {
            return inWarningZone;
        }

        public String getRemainingTimeFormatted() {
            if (remainingTime.isNegative()) {
                Duration abs = remainingTime.negated();
                return String.format("-%dh %dm OVERDUE", abs.toHours(), abs.toMinutesPart());
            }
            return String.format("%dh %dm", remainingTime.toHours(), remainingTime.toMinutesPart());
        }

        public String getOvertimeFormatted() {
            return String.format("%dh %dm", overtime.toHours(), overtime.toMinutesPart());
        }

        public double getElapsedHours() {
            if (remainingTime == null || slaDeadline == null)
                return 0.0;
            int slaMinutes = SlaConfiguration.getSlaMinutesForPriority(priority);
            return (slaMinutes - remainingTime.toMinutes()) / 60.0;
        }

        public double getOvertimeHours() {
            return overtime != null ? overtime.toMinutes() / 60.0 : 0.0;
        }

        public boolean isEscalationRequired() {
            return breached && escalationLevel != EscalationLevel.NONE;
        }

        public String getFormattedStatus() {
            if (breached)
                return String.format("BREACHED - %s escalation required", escalationLevel.getDisplayName());
            if (inWarningZone)
                return String.format("WARNING - %.0f%% of SLA used", slaPercentage);
            return String.format("ON TRACK - %.0f%% of SLA used", slaPercentage);
        }
    }
}
