export function isLoggedIn() {
    return !!sessionStorage.getItem('sessionToken');
}

export function getUserRole() {
    return sessionStorage.getItem('userRole') || '';
}

export function getEngineerData() {
    try {
        const data = sessionStorage.getItem('engineer');
        return data ? JSON.parse(data) : null;
    } catch {
        return null;
    }
}

export function getManagementData() {
    return {
        userId: sessionStorage.getItem('userId'),
        username: sessionStorage.getItem('username'),
        name: sessionStorage.getItem('userName'),
        role: sessionStorage.getItem('role'),
        roleDisplayName: sessionStorage.getItem('roleDisplayName'),
        department: sessionStorage.getItem('department'),
        departmentDisplayName: sessionStorage.getItem('departmentDisplayName'),
        hasFullAccess: sessionStorage.getItem('hasFullAccess') === 'true',
    };
}

export function saveEngineerSession(data) {
    sessionStorage.setItem('sessionToken', data.sessionToken);
    sessionStorage.setItem('engineer', JSON.stringify(data.engineer));
    sessionStorage.setItem('userRole', 'engineer');
}

export function saveManagementSession(data) {
    const userInfo = data.user || data.userInfo;
    sessionStorage.setItem('sessionToken', data.sessionToken);
    sessionStorage.setItem('userId', userInfo.userId);
    sessionStorage.setItem('username', userInfo.username);
    sessionStorage.setItem('userName', userInfo.name);
    sessionStorage.setItem('role', userInfo.role);
    sessionStorage.setItem('roleDisplayName', userInfo.roleDisplayName);
    sessionStorage.setItem('department', userInfo.department);
    sessionStorage.setItem('departmentDisplayName', userInfo.departmentDisplayName);
    sessionStorage.setItem('hasFullAccess', userInfo.hasFullAccess);
    sessionStorage.setItem('userRole', 'management');
}

export function logout() {
    sessionStorage.clear();
    window.location.href = '/login';
}

export function getInitials(name) {
    if (!name) return '--';
    const parts = name.split(' ');
    if (parts.length >= 2) {
        return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
}

export function formatStatus(status) {
    if (!status) return 'ASSIGNED';
    return status.replace(/_/g, ' ');
}

export function formatDate(dateStr) {
    if (!dateStr) return '---';
    const d = new Date(dateStr);
    return d.toLocaleString('en-IN', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    });
}
