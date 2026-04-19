/**
 * Shared data transformation utilities used across multiple pages
 */

/**
 * Transform engineer data response to standardized format
 */
export const transformEngineerData = (engineersData) => {
    if (!engineersData || !Array.isArray(engineersData)) {
        return [];
    }

    return engineersData.map(eng => ({
        id: eng.id || eng.engineerId,
        name: eng.name || eng.engineerName || 'Unknown',
        team: eng.team || eng.teamName || 'Unassigned',
        assigned: eng.assigned || eng.assignedTickets || 0,
        resolved: eng.resolved || eng.resolvedTickets || 0,
        resolutionRate: calculateResolutionRate(
            eng.resolved || eng.resolvedTickets || 0,
            eng.assigned || eng.assignedTickets || 0
        ),
        avgResolutionTime: eng.avgResolutionTime || 0,
        slaCompliance: Math.max(0, Math.min(100, parseFloat(eng.slaCompliance || 0))),
        inProgress: eng.inProgress || 0,
        status: eng.status || 'ACTIVE',
        performance: calculatePerformanceTier(
            parseFloat(eng.slaCompliance || 0),
            calculateResolutionRate(eng.resolved || 0, eng.assigned || 0)
        ),
    }));
};

/**
 * Calculate resolution rate percentage
 */
export const calculateResolutionRate = (resolved, assigned) => {
    if (assigned === 0) return 0;
    const rate = (resolved / assigned) * 100;
    return Math.round(rate * 100) / 100; // Round to 2 decimals
};

/**
 * Calculate performance tier based on SLA compliance and resolution rate
 */
export const calculatePerformanceTier = (slaCompliance, resolutionRate) => {
    const avgScore = (slaCompliance + resolutionRate) / 2;
    if (avgScore >= 90) return 'EXCELLENT';
    if (avgScore >= 75) return 'GOOD';
    if (avgScore >= 60) return 'FAIR';
    return 'NEEDS_IMPROVEMENT';
};

/**
 * Transform category data response to standardized format
 */
export const transformCategoryData = (categoryData) => {
    if (!categoryData) return [];

    if (Array.isArray(categoryData)) return categoryData;

    // If it's an object like { HARDWARE: 5, NETWORK: 3 }
    if (typeof categoryData === 'object') {
        return Object.entries(categoryData)
            .map(([category, count]) => ({
                category: category || 'GENERAL',
                count: Number.isFinite(count) ? count : 0,
            }))
            .filter(item => item.count > 0)
            .sort((a, b) => b.count - a.count);
    }

    return [];
};

/**
 * Transform SLA data response to standardized format
 */
export const transformSLAData = (slaData) => {
    if (!slaData) return {};

    return {
        onTrack: Number.isFinite(slaData.onTrack) ? slaData.onTrack : 0,
        atRisk: Number.isFinite(slaData.atRisk) ? slaData.atRisk : 0,
        breached: Number.isFinite(slaData.breached) ? slaData.breached : 0,
        compliance: Math.max(0, Math.min(100, parseFloat(slaData.compliance || 0))),
    };
};

/**
 * Transform ticket data response to standardized format
 */
export const transformTicketData = (tickets) => {
    if (!Array.isArray(tickets)) return [];

    return tickets.map(ticket => ({
        id: ticket.id || ticket.ticketId,
        ticketId: ticket.ticketId || ticket.id,
        subject: ticket.subject || ticket.title || ticket.emailSubject || 'No subject',
        category: ticket.category || 'GENERAL',
        priority: ticket.priority || 'MEDIUM',
        status: ticket.status || 'OPEN',
        assignedTo: ticket.assignedTo || ticket.assignee || 'Unassigned',
        slaStatus: ticket.slaStatus || 'ON_TRACK',
        slaRemaining: ticket.slaRemaining || 'N/A',
        createdAt: ticket.createdAt || new Date().toISOString(),
        updatedAt: ticket.updatedAt || new Date().toISOString(),
    }));
};

/**
 * Transform team workload data
 */
export const transformTeamWorkloadData = (teams) => {
    if (!Array.isArray(teams)) return [];

    return teams.map(team => ({
        name: team.name || 'Unknown Team',
        totalTickets: team.totalTickets || 0,
        resolvedTickets: team.resolvedTickets || 0,
        activeTickets: team.activeTickets || 0,
        engineersCount: team.engineersCount || 0,
        avgResolutionTime: team.avgResolutionTime || 0,
        utilization: calculateUtilization(
            team.activeTickets || 0,
            team.engineersCount || 1
        ),
    }));
};

/**
 * Calculate team utilization percentage
 */
export const calculateUtilization = (activeTickets, engineersCount) => {
    if (engineersCount === 0) return 0;
    // Assume each engineer can handle ~5 tickets actively
    const maxCapacity = engineersCount * 5;
    const utilization = (activeTickets / maxCapacity) * 100;
    return Math.min(100, Math.round(utilization));
};

/**
 * Group data by property
 */
export const groupBy = (data, property) => {
    return data.reduce((acc, item) => {
        const key = item[property];
        if (!acc[key]) acc[key] = [];
        acc[key].push(item);
        return acc;
    }, {});
};

/**
 * Sort data by multiple properties
 */
export const sortByMultiple = (data, sortConfig) => {
    return [...data].sort((a, b) => {
        for (const { key, direction } of sortConfig) {
            const aVal = a[key];
            const bVal = b[key];

            if (aVal < bVal) return direction === 'asc' ? -1 : 1;
            if (aVal > bVal) return direction === 'asc' ? 1 : -1;
        }
        return 0;
    });
};
