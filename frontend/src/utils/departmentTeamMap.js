/**
 * Department-to-Teams mapping utility
 * TODO: Move to backend API endpoint /api/departments/{id}/teams
 */

export const DEPARTMENT_TEAMS_MAP = {
    'NETWORK': [
        { id: 'net-team-1', name: 'Network Team' },
        { id: 'net-team-2', name: 'Infrastructure Team' },
    ],
    'HARDWARE': [
        { id: 'hw-team-1', name: 'Hardware Support Team' },
        { id: 'hw-team-2', name: 'Desktop Support Team' },
    ],
    'SOFTWARE': [
        { id: 'sw-team-1', name: 'Application Support Team' },
        { id: 'sw-team-2', name: 'Development Support Team' },
    ],
    'EMAIL': [
        { id: 'email-team-1', name: 'Email Support Team' },
    ],
    'ACCESS': [
        { id: 'sec-team-1', name: 'IT Security Team' },
        { id: 'sec-team-2', name: 'Access Management Team' },
    ],
    'GENERAL': [
        { id: 'gen-team-1', name: 'General IT Support' },
    ],
};

/**
 * Get teams for a department
 */
export const getTeamsForDepartment = (department) => {
    return DEPARTMENT_TEAMS_MAP[department] || [];
};

/**
 * Get team name by department and team ID
 */
export const getTeamName = (department, teamId) => {
    const teams = getTeamsForDepartment(department);
    const team = teams.find(t => t.id === teamId);
    return team ? team.name : teamId;
};

/**
 * Get all departments
 */
export const getAllDepartments = () => {
    return Object.keys(DEPARTMENT_TEAMS_MAP).map(dept => ({
        code: dept,
        name: dept.charAt(0) + dept.slice(1).toLowerCase(),
    }));
};
