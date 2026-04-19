const API_BASE_ENGINEER = '/api/engineer';
const API_BASE_MANAGEMENT = '/api/management';

export function getSessionToken() {
    return sessionStorage.getItem('sessionToken');
}

export async function fetchWithAuth(url, options = {}) {
    const token = getSessionToken();
    const headers = {
        ...options.headers,
    };
    if (token) {
        headers['X-Session-Token'] = token;
    }
    if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData)) {
        headers['Content-Type'] = 'application/json';
        options.body = JSON.stringify(options.body);
    }
    const response = await fetch(url, { ...options, headers });
    if (response.status === 401) {
        sessionStorage.clear();
        window.location.href = '/login';
        throw new Error('Session expired');
    }
    return response;
}

export async function engineerLogin(username, password) {
    const response = await fetch(`${API_BASE_ENGINEER}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
    });
    return response.json();
}

export async function managementLogin(username, password) {
    const response = await fetch(`${API_BASE_MANAGEMENT}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
    });
    return response.json();
}

export async function validateManagementSession() {
    const token = getSessionToken();
    if (!token) return false;
    try {
        const response = await fetch(`${API_BASE_MANAGEMENT}/auth/validate`, {
            headers: { 'X-Session-Token': token },
        });
        const result = await response.json();
        return result.success;
    } catch {
        return false;
    }
}

export async function fetchEngineerDashboard() {
    try {
        const res = await fetchWithAuth(`${API_BASE_ENGINEER}/dashboard`);
        if (!res.ok) {
            const errorText = await res.text();
            console.error('Dashboard API error:', res.status, errorText);
            throw new Error(`Dashboard API error: ${res.status}`);
        }
        const data = await res.json();
        console.log('Dashboard data received from backend:', data);
        return data;
    } catch (error) {
        console.error('Dashboard fetch error:', error);
        throw error;
    }
}

export async function startTicket(ticketId) {
    const res = await fetchWithAuth(`${API_BASE_ENGINEER}/ticket/${ticketId}/start`, { method: 'POST' });
    return res.json();
}

export async function resolveTicket(ticketId, resolutionNotes) {
    const res = await fetchWithAuth(`${API_BASE_ENGINEER}/ticket/${ticketId}/resolve`, {
        method: 'POST',
        body: { resolutionNotes },
    });
    return res.json();
}

export async function fetchTicketDetails(ticketId) {
    const res = await fetchWithAuth(`${API_BASE_ENGINEER}/ticket/${ticketId}`);
    return res.json();
}

export async function submitProgressNote(ticketId, note) {
    const res = await fetchWithAuth(`${API_BASE_ENGINEER}/ticket/${ticketId}/notes`, {
        method: 'POST',
        body: { notes: note },
    });
    return res.json();
}

export async function submitEscalation(ticketId, reason, targetTeamLeadId) {
    const res = await fetchWithAuth(`${API_BASE_ENGINEER}/ticket/${ticketId}/escalate`, {
        method: 'POST',
        body: { escalationReason: reason, targetTeamLead: targetTeamLeadId },
    });
    return res.json();
}

export async function searchKnowledgeBase(query) {
    const res = await fetch(`/api/knowledge-base/search?q=${encodeURIComponent(query)}`);
    return res.json();
}

export async function fetchAllKnowledgeBase() {
    try {
        const res = await fetch(`/api/knowledge-base`);
        if (!res.ok) {
            const errorText = await res.text();
            console.error('KB API error:', res.status, errorText);
            throw new Error(`KB API error: ${res.status}`);
        }
        const data = await res.json();
        console.log('Knowledge Base data from backend:', data);
        return data;
    } catch (error) {
        console.error('KB fetch error:', error);
        throw error;
    }
}

export async function fetchSLAStatus() {
    try {
        const res = await fetch(`/api/sla/dashboard`);
        if (!res.ok) {
            const errorText = await res.text();
            console.error('SLA API error:', res.status, errorText);
            throw new Error(`SLA API error: ${res.status}`);
        }
        const data = await res.json();
        console.log('SLA Status data from backend:', data);
        return data;
    } catch (error) {
        console.error('SLA fetch error:', error);
        throw error;
    }
}

export async function fetchTeamLeads() {
    const res = await fetchWithAuth(`${API_BASE_ENGINEER}/team-leads`);
    return res.json();
}

export async function getAssignedTickets(priority, status, activeOnly) {
    try {
        let url = `${API_BASE_ENGINEER}/tickets?activeOnly=${activeOnly || false}`;
        if (priority) url += `&priority=${priority}`;
        if (status) url += `&status=${status}`;
        console.log('Fetching tickets from:', url);
        const res = await fetchWithAuth(url);
        if (!res.ok) {
            console.error('Tickets API error:', res.status);
            return { tickets: [], count: 0 };
        }
        const data = await res.json();
        console.log('Tickets endpoint response:', data);
        return data;
    } catch (error) {
        console.error('Tickets fetch error:', error);
        return { tickets: [], count: 0 };
    }
}

export async function getEngineerStats() {
    const res = await fetchWithAuth(`${API_BASE_ENGINEER}/stats`);
    return res.json();
}

export async function updateEngineerProfile(profileData) {
    const res = await fetchWithAuth(`${API_BASE_ENGINEER}/profile`, {
        method: 'PUT',
        body: profileData
    });
    if (!res.ok) {
        try {
            const error = await res.json();
            console.error('Backend error response:', error);
            throw new Error(error.error || error.message || `HTTP ${res.status}: Failed to update profile`);
        } catch (e) {
            console.error('Failed to parse error response:', e);
            throw new Error(`HTTP ${res.status}: Failed to update profile`);
        }
    }
    return res.json();
}

export async function updateManagementProfile(profileData) {
    const res = await fetchWithAuth(`${API_BASE_MANAGEMENT}/profile`, {
        method: 'PUT',
        body: profileData
    });
    if (!res.ok) {
        try {
            const error = await res.json();
            console.error('Backend error response:', error);
            throw new Error(error.error || error.message || `HTTP ${res.status}: Failed to update profile`);
        } catch (e) {
            console.error('Failed to parse error response:', e);
            throw new Error(`HTTP ${res.status}: Failed to update profile`);
        }
    }
    return res.json();
}

export async function fetchManagementDashboard(period) {
    const token = getSessionToken();
    const res = await fetch(`${API_BASE_MANAGEMENT}/dashboard?period=${period || 'week'}`, {
        headers: { 'X-Session-Token': token },
    });
    if (res.status === 401) {
        sessionStorage.clear();
        window.location.href = '/login';
        throw new Error('Session expired');
    }
    return res.json();
}

export async function fetchManagementTickets() {
    try {
        const res = await fetchWithAuth(`${API_BASE_MANAGEMENT}/dashboard/recent?limit=100`);
        if (!res.ok) {
            const errorText = await res.text();
            console.error('Tickets API error:', res.status, errorText);
            throw new Error(`Tickets API error: ${res.status}`);
        }
        const data = await res.json();
        console.log('Management tickets data:', data);
        return data;
    } catch (error) {
        console.error('Tickets fetch error:', error);
        throw error;
    }
}

export async function fetchManagementEngineers(period = 'month', department = null) {
    try {
        let url = `${API_BASE_MANAGEMENT}/dashboard/engineers?period=${period}`;
        if (department) {
            url += `&department=${department}`;
        }
        const res = await fetchWithAuth(url);
        if (!res.ok) {
            const errorText = await res.text();
            console.error('Engineers API error:', res.status, errorText);
            throw new Error(`Engineers API error: ${res.status}`);
        }
        const data = await res.json();
        console.log('Management engineers data:', data);
        return data;
    } catch (error) {
        console.error('Engineers fetch error:', error);
        throw error;
    }
}

export async function fetchManagementCategories(period = 'month', department = null) {
    try {
        let url = `${API_BASE_MANAGEMENT}/dashboard/categories?period=${period}`;
        if (department) {
            url += `&department=${department}`;
        }
        const res = await fetchWithAuth(url);
        if (!res.ok) {
            const errorText = await res.text();
            console.error('Categories API error:', res.status, errorText);
            throw new Error(`Categories API error: ${res.status}`);
        }
        const data = await res.json();
        console.log('Management categories data:', data);
        return data;
    } catch (error) {
        console.error('Categories fetch error:', error);
        throw error;
    }
}

export async function fetchManagementRecentTickets() {
    try {
        const res = await fetchWithAuth(`${API_BASE_MANAGEMENT}/dashboard/recent?limit=50`);
        if (!res.ok) {
            const errorText = await res.text();
            console.error('Recent tickets API error:', res.status, errorText);
            throw new Error(`Recent tickets API error: ${res.status}`);
        }
        const data = await res.json();
        console.log('Management recent tickets data:', data);
        return data;
    } catch (error) {
        console.error('Recent tickets fetch error:', error);
        throw error;
    }
}

export async function fetchManagementSLA(period = 'month', department = null) {
    try {
        let url = `${API_BASE_MANAGEMENT}/dashboard/sla?period=${period}`;
        if (department) {
            url += `&department=${department}`;
        }
        const res = await fetchWithAuth(url);
        if (!res.ok) {
            const errorText = await res.text();
            console.error('SLA API error:', res.status, errorText);
            throw new Error(`SLA API error: ${res.status}`);
        }
        const data = await res.json();
        console.log('Management SLA data:', data);
        return data;
    } catch (error) {
        console.error('SLA fetch error:', error);
        throw error;
    }
}

export async function fetchManagementDepartments(period = 'month', department = null) {
    try {
        let url = `${API_BASE_MANAGEMENT}/dashboard/summary?period=${period}`;
        if (department) {
            url += `&department=${department}`;
        }
        const res = await fetchWithAuth(url);
        if (!res.ok) {
            const errorText = await res.text();
            console.error('Departments API error:', res.status, errorText);
            throw new Error(`Departments API error: ${res.status}`);
        }
        const data = await res.json();
        console.log('Management departments data:', data);
        return data;
    } catch (error) {
        console.error('Departments fetch error:', error);
        throw error;
    }
}

// ============================================================
// USER MANAGEMENT - ENGINEERS (DEPARTMENT_HEAD only)
// ============================================================

export async function fetchEngineersList() {
    const res = await fetchWithAuth(`${API_BASE_MANAGEMENT}/users/engineers`);
    return res.json();
}

export async function addEngineer(engineerData) {
    const res = await fetchWithAuth(`${API_BASE_MANAGEMENT}/users/engineers`, {
        method: 'POST',
        body: engineerData,
    });
    return res.json();
}

export async function updateEngineer(engineerId, updates) {
    const res = await fetchWithAuth(`${API_BASE_MANAGEMENT}/users/engineers/${engineerId}`, {
        method: 'PUT',
        body: updates,
    });
    return res.json();
}

export async function deleteEngineer(engineerId) {
    const res = await fetchWithAuth(`${API_BASE_MANAGEMENT}/users/engineers/${engineerId}`, {
        method: 'DELETE',
    });
    return res.json();
}

// ============================================================
// USER MANAGEMENT - MANAGERS (IT_COORDINATOR only)
// ============================================================

export async function fetchManagersList() {
    const res = await fetchWithAuth(`${API_BASE_MANAGEMENT}/users/managers`);
    return res.json();
}

export async function addManager(managerData) {
    const res = await fetchWithAuth(`${API_BASE_MANAGEMENT}/users/managers`, {
        method: 'POST',
        body: managerData,
    });
    return res.json();
}

export async function updateManager(managerId, updates) {
    const res = await fetchWithAuth(`${API_BASE_MANAGEMENT}/users/managers/${managerId}`, {
        method: 'PUT',
        body: updates,
    });
    return res.json();
}

export async function deleteManager(managerId) {
    const res = await fetchWithAuth(`${API_BASE_MANAGEMENT}/users/managers/${managerId}`, {
        method: 'DELETE',
    });
    return res.json();
}

export async function fetchDepartments() {
    const res = await fetchWithAuth(`${API_BASE_MANAGEMENT}/departments`);
    return res.json();
}

export async function addDepartment(deptData) {
    const res = await fetchWithAuth(`${API_BASE_MANAGEMENT}/departments`, {
        method: 'POST',
        body: deptData,
    });
    return res.json();
}

export async function updateDepartment(code, updates) {
    const res = await fetchWithAuth(`${API_BASE_MANAGEMENT}/departments/${code}`, {
        method: 'PUT',
        body: updates,
    });
    return res.json();
}

export async function deleteDepartment(code) {
    const res = await fetchWithAuth(`${API_BASE_MANAGEMENT}/departments/${code}`, {
        method: 'DELETE',
    });
    return res.json();
}

// ============================================================
// REPORTS & DOWNLOADS
// ============================================================

export async function generateReport(reportType, dateRange, department, customStartDate, customEndDate) {
    try {
        const body = {
            reportType,
            dateRange,
        };

        if (department && department !== 'all') {
            body.department = department;
        }

        if (dateRange === 'custom') {
            body.customStartDate = customStartDate;
            body.customEndDate = customEndDate;
        }

        const res = await fetchWithAuth(`${API_BASE_MANAGEMENT}/dashboard/reports/generate`, {
            method: 'POST',
            body,
        });

        if (!res.ok) {
            const errorText = await res.text();
            console.error('Report generation error:', res.status, errorText);
            throw new Error(`Report generation failed: ${res.status}`);
        }

        const data = await res.json();
        console.log('Report generated:', data);
        return data;
    } catch (error) {
        console.error('Report generation error:', error);
        throw error;
    }
}

export async function downloadReport(reportType, format = 'pdf') {
    try {
        const res = await fetchWithAuth(
            `${API_BASE_MANAGEMENT}/dashboard/reports/download?type=${reportType}&format=${format}`,
            { method: 'GET' }
        );

        if (!res.ok) {
            throw new Error(`Download failed with status ${res.status}`);
        }

        return res.blob();
    } catch (error) {
        console.error('Download error:', error);
        throw error;
    }
}

export async function fetchAuditLog(limit = 100, filter = null) {
    try {
        let url = `${API_BASE_MANAGEMENT}/audit-log?limit=${limit}`;
        if (filter) {
            url += `&filter=${filter}`;
        }
        const res = await fetchWithAuth(url);
        if (!res.ok) {
            const errorText = await res.text();
            console.error('Audit log API error:', res.status, errorText);
            throw new Error(`Audit log API error: ${res.status}`);
        }
        const data = await res.json();
        console.log('Audit log data:', data);
        return data;
    } catch (error) {
        console.error('Audit log fetch error:', error);
        throw error;
    }
}