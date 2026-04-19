/**
 * Centralized Role-Based Access Control (RBAC)
 * Defines permissions for different user roles
 */

export const ROLES = {
    ENGINEER: 'ENGINEER',
    DEPARTMENT_HEAD: 'DEPARTMENT_HEAD',
    IT_COORDINATOR: 'IT_COORDINATOR',
};

export const PERMISSIONS = {
    // Engineer permissions
    VIEW_OWN_TICKETS: 'VIEW_OWN_TICKETS',
    RESOLVE_TICKETS: 'RESOLVE_TICKETS',
    VIEW_KB: 'VIEW_KB',
    VIEW_PROFILE: 'VIEW_PROFILE',

    // DEPARTMENT_HEAD permissions (extends Engineer)
    VIEW_TEAM_TICKETS: 'VIEW_TEAM_TICKETS',
    MANAGE_TEAM: 'MANAGE_TEAM',
    VIEW_TEAM_ANALYTICS: 'VIEW_TEAM_ANALYTICS',
    ESCALATE_TICKETS: 'ESCALATE_TICKETS',

    // IT_COORDINATOR permissions (management level)
    VIEW_ALL_TICKETS: 'VIEW_ALL_TICKETS',
    VIEW_ALL_ANALYTICS: 'VIEW_ALL_ANALYTICS',
    MANAGE_ENGINEERS: 'MANAGE_ENGINEERS',
    MANAGE_DEPARTMENTS: 'MANAGE_DEPARTMENTS',
    MANAGE_MANAGERS: 'MANAGE_MANAGERS',
    VIEW_AUDIT_LOG: 'VIEW_AUDIT_LOG',
    GENERATE_REPORTS: 'GENERATE_REPORTS',
};

/**
 * Role to permissions mapping
 */
const rolePermissions = {
    [ROLES.ENGINEER]: [
        PERMISSIONS.VIEW_OWN_TICKETS,
        PERMISSIONS.RESOLVE_TICKETS,
        PERMISSIONS.VIEW_KB,
        PERMISSIONS.VIEW_PROFILE,
    ],
    [ROLES.DEPARTMENT_HEAD]: [
        // All engineer permissions
        PERMISSIONS.VIEW_OWN_TICKETS,
        PERMISSIONS.RESOLVE_TICKETS,
        PERMISSIONS.VIEW_KB,
        PERMISSIONS.VIEW_PROFILE,
        // Department head specific
        PERMISSIONS.VIEW_TEAM_TICKETS,
        PERMISSIONS.MANAGE_TEAM,
        PERMISSIONS.MANAGE_ENGINEERS,
        PERMISSIONS.VIEW_TEAM_ANALYTICS,
        PERMISSIONS.ESCALATE_TICKETS,
    ],
    [ROLES.IT_COORDINATOR]: [
        // All permissions
        PERMISSIONS.VIEW_OWN_TICKETS,
        PERMISSIONS.RESOLVE_TICKETS,
        PERMISSIONS.VIEW_KB,
        PERMISSIONS.VIEW_PROFILE,
        PERMISSIONS.VIEW_TEAM_TICKETS,
        PERMISSIONS.MANAGE_TEAM,
        PERMISSIONS.VIEW_TEAM_ANALYTICS,
        PERMISSIONS.ESCALATE_TICKETS,
        PERMISSIONS.VIEW_ALL_TICKETS,
        PERMISSIONS.VIEW_ALL_ANALYTICS,
        PERMISSIONS.MANAGE_ENGINEERS,
        PERMISSIONS.MANAGE_DEPARTMENTS,
        PERMISSIONS.MANAGE_MANAGERS,
        PERMISSIONS.VIEW_AUDIT_LOG,
        PERMISSIONS.GENERATE_REPORTS,
    ],
};

/**
 * Check if user has specific permission
 */
export const hasPermission = (userRole, permission) => {
    if (!userRole || !rolePermissions[userRole]) {
        return false;
    }
    return rolePermissions[userRole].includes(permission);
};

/**
 * Check if user has any of the given permissions
 */
export const hasAnyPermission = (userRole, permissions) => {
    return permissions.some(permission => hasPermission(userRole, permission));
};

/**
 * Check if user has all the given permissions
 */
export const hasAllPermissions = (userRole, permissions) => {
    return permissions.every(permission => hasPermission(userRole, permission));
};

/**
 * Get all pages accessible by user role
 */
export const getAccessiblePages = (userRole) => {
    const pages = {
        [ROLES.ENGINEER]: [
            '/engineer/dashboard',
            '/engineer/tickets',
            '/engineer/ticket/:id',
            '/engineer/kb',
            '/engineer/sla',
            '/engineer/profile',
            '/engineer/settings',
        ],
        [ROLES.DEPARTMENT_HEAD]: [
            '/engineer/dashboard',
            '/engineer/tickets',
            '/engineer/ticket/:id',
            '/engineer/kb',
            '/engineer/sla',
            '/engineer/profile',
            '/engineer/settings',
            '/management/dashboard',
            '/management/engineers',
            '/management/tickets',
        ],
        [ROLES.IT_COORDINATOR]: [
            '/engineer/dashboard',
            '/engineer/tickets',
            '/engineer/ticket/:id',
            '/engineer/kb',
            '/engineer/sla',
            '/engineer/profile',
            '/engineer/settings',
            '/management/dashboard',
            '/management/engineers',
            '/management/tickets',
            '/management/categories',
            '/management/sla',
            '/management/reports',
            '/management/response-time',
            '/management/teamworkload',
            '/management/escalations',
            '/management/audit-log',
            '/management/users',
            '/management/departments',
            '/management/department-report',
            '/management/settings',
            '/management/recent-tickets',
        ],
    };

    return pages[userRole] || [];
};

/**
 * Check if page is accessible by user role
 */
export const isPageAccessible = (userRole, pagePath) => {
    const accessiblePages = getAccessiblePages(userRole);

    // Match exact path or pattern (e.g., /engineer/ticket/:id)
    return accessiblePages.some(page => {
        const pattern = page.replace(/:id/g, '[^/]+');
        const regex = new RegExp(`^${pattern}$`);
        return regex.test(pagePath);
    });
};

/**
 * Validate management page access
 */
export const canAccessManagementPage = (userRole) => {
    return hasPermission(userRole, PERMISSIONS.VIEW_ALL_TICKETS) ||
           hasPermission(userRole, PERMISSIONS.VIEW_TEAM_TICKETS);
};

/**
 * Check if can manage users (engineers or managers)
 */
export const canManageUsers = (userRole) => {
    return hasPermission(userRole, PERMISSIONS.MANAGE_ENGINEERS) ||
           hasPermission(userRole, PERMISSIONS.MANAGE_MANAGERS);
};

/**
 * Check if can view analytics/reports
 */
export const canViewAnalytics = (userRole) => {
    return hasPermission(userRole, PERMISSIONS.VIEW_ALL_ANALYTICS) ||
           hasPermission(userRole, PERMISSIONS.VIEW_TEAM_ANALYTICS);
};
