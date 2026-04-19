package com.powergrid.ticketsystem.analytics.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * PEAK TICKET HOURS REPORT DTO
 * ============================================================
 * 
 * PHASE 9: REPORTS & ANALYTICS
 * 
 * PURPOSE:
 * ─────────
 * Shows which hours of the day have the highest ticket creation.
 * Helps management with staffing and shift planning.
 * 
 * WHY THIS REPORT MATTERS:
 * ────────────────────────
 * - Optimal staff scheduling
 * - Identifying when IT support demand is highest
 * - Planning shift rotations
 * - Ensuring adequate coverage during peak times
 * 
 * EXAMPLE OUTPUT:
 * ───────────────
 * Peak Ticket Time: 09:00 AM – 11:00 AM
 * Peak Hour: 10:00 AM (45 tickets)
 */
public class PeakHoursReport {

    // ============================================================
    // PEAK HOURS DATA
    // ============================================================

    /**
     * Hourly breakdown of ticket creation (0-23 hours).
     */
    private List<HourlyCount> hourlyDistribution;

    /**
     * The peak hour (0-23).
     */
    private int peakHour;

    /**
     * Count at peak hour.
     */
    private long peakHourCount;

    /**
     * Formatted peak time window (e.g., "09:00 AM - 11:00 AM").
     */
    private String peakTimeWindow;

    /**
     * Peak period start hour.
     */
    private int peakPeriodStart;

    /**
     * Peak period end hour.
     */
    private int peakPeriodEnd;

    // ============================================================
    // ADDITIONAL INSIGHTS
    // ============================================================

    /**
     * Lowest activity hour.
     */
    private int quietHour;

    /**
     * Count at quiet hour.
     */
    private long quietHourCount;

    /**
     * Average tickets per hour.
     */
    private double averageTicketsPerHour;

    /**
     * Total tickets analyzed.
     */
    private long totalTickets;

    // ============================================================
    // REPORT METADATA
    // ============================================================

    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private LocalDateTime generatedAt;
    private String periodDescription;

    // ============================================================
    // NESTED CLASS FOR HOURLY COUNT
    // ============================================================

    /**
     * Ticket count for a specific hour.
     */
    public static class HourlyCount {
        private int hour;
        private String hourFormatted;
        private long count;
        private double percentage;

        public HourlyCount() {
        }

        public HourlyCount(int hour, long count) {
            this.hour = hour;
            this.count = count;
            this.hourFormatted = formatHour(hour);
        }

        /**
         * Format hour to 12-hour format (e.g., "09:00 AM").
         */
        private String formatHour(int hour) {
            if (hour == 0) {
                return "12:00 AM";
            } else if (hour < 12) {
                return String.format("%02d:00 AM", hour);
            } else if (hour == 12) {
                return "12:00 PM";
            } else {
                return String.format("%02d:00 PM", hour - 12);
            }
        }

        // Getters and Setters
        public int getHour() {
            return hour;
        }

        public void setHour(int hour) {
            this.hour = hour;
            this.hourFormatted = formatHour(hour);
        }

        public String getHourFormatted() {
            return hourFormatted;
        }

        public void setHourFormatted(String hourFormatted) {
            this.hourFormatted = hourFormatted;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public double getPercentage() {
            return percentage;
        }

        public void setPercentage(double percentage) {
            this.percentage = percentage;
        }
    }

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public PeakHoursReport() {
        this.hourlyDistribution = new ArrayList<>();
        this.generatedAt = LocalDateTime.now();
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Calculate peak hours after distribution is set.
     */
    public void calculatePeakHours() {
        if (hourlyDistribution.isEmpty()) {
            return;
        }

        long maxCount = 0;
        long minCount = Long.MAX_VALUE;
        long total = 0;

        for (HourlyCount hc : hourlyDistribution) {
            total += hc.getCount();
            if (hc.getCount() > maxCount) {
                maxCount = hc.getCount();
                peakHour = hc.getHour();
            }
            if (hc.getCount() < minCount) {
                minCount = hc.getCount();
                quietHour = hc.getHour();
            }
        }

        this.peakHourCount = maxCount;
        this.quietHourCount = minCount;
        this.totalTickets = total;
        this.averageTicketsPerHour = Math.round((double) total / 24 * 100.0) / 100.0;

        // Calculate percentages
        for (HourlyCount hc : hourlyDistribution) {
            if (total > 0) {
                hc.setPercentage(Math.round(((double) hc.getCount() / total) * 10000.0) / 100.0);
            }
        }

        // Determine peak window (2-hour window around peak)
        peakPeriodStart = Math.max(0, peakHour - 1);
        peakPeriodEnd = Math.min(23, peakHour + 1);

        // Format peak time window
        peakTimeWindow = formatHourRange(peakPeriodStart, peakPeriodEnd);
    }

    /**
     * Format hour range (e.g., "09:00 AM - 11:00 AM").
     */
    private String formatHourRange(int startHour, int endHour) {
        return formatSingleHour(startHour) + " - " + formatSingleHour(endHour + 1);
    }

    private String formatSingleHour(int hour) {
        hour = hour % 24;
        if (hour == 0) {
            return "12:00 AM";
        } else if (hour < 12) {
            return String.format("%02d:00 AM", hour);
        } else if (hour == 12) {
            return "12:00 PM";
        } else {
            return String.format("%02d:00 PM", hour - 12);
        }
    }

    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================

    public List<HourlyCount> getHourlyDistribution() {
        return hourlyDistribution;
    }

    public void setHourlyDistribution(List<HourlyCount> hourlyDistribution) {
        this.hourlyDistribution = hourlyDistribution;
    }

    public int getPeakHour() {
        return peakHour;
    }

    public void setPeakHour(int peakHour) {
        this.peakHour = peakHour;
    }

    public long getPeakHourCount() {
        return peakHourCount;
    }

    public void setPeakHourCount(long peakHourCount) {
        this.peakHourCount = peakHourCount;
    }

    public String getPeakTimeWindow() {
        return peakTimeWindow;
    }

    public void setPeakTimeWindow(String peakTimeWindow) {
        this.peakTimeWindow = peakTimeWindow;
    }

    public int getPeakPeriodStart() {
        return peakPeriodStart;
    }

    public void setPeakPeriodStart(int peakPeriodStart) {
        this.peakPeriodStart = peakPeriodStart;
    }

    public int getPeakPeriodEnd() {
        return peakPeriodEnd;
    }

    public void setPeakPeriodEnd(int peakPeriodEnd) {
        this.peakPeriodEnd = peakPeriodEnd;
    }

    public int getQuietHour() {
        return quietHour;
    }

    public void setQuietHour(int quietHour) {
        this.quietHour = quietHour;
    }

    public long getQuietHourCount() {
        return quietHourCount;
    }

    public void setQuietHourCount(long quietHourCount) {
        this.quietHourCount = quietHourCount;
    }

    public double getAverageTicketsPerHour() {
        return averageTicketsPerHour;
    }

    public void setAverageTicketsPerHour(double averageTicketsPerHour) {
        this.averageTicketsPerHour = averageTicketsPerHour;
    }

    public long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(long totalTickets) {
        this.totalTickets = totalTickets;
    }

    public LocalDateTime getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDateTime periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDateTime getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDateTime periodEnd) {
        this.periodEnd = periodEnd;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getPeriodDescription() {
        return periodDescription;
    }

    public void setPeriodDescription(String periodDescription) {
        this.periodDescription = periodDescription;
    }
}
