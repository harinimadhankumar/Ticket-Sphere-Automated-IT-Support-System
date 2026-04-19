import React from 'react';
import { useNavigate } from 'react-router-dom';
import { getUserRole, getManagementData } from '../../utils/auth';

export default function Sidebar() {
    const navigate = useNavigate();
    const role = getUserRole();
    const mgmt = getManagementData();

    const engineerMenuItems = [
        { id: 'dashboard', label: 'Dashboard', path: '/engineer/dashboard' },
        { id: 'tickets', label: 'My Tickets', path: '/engineer/tickets' },
        { id: 'knowledge', label: 'Knowledge Base', path: '/engineer/knowledge' },
        { id: 'sla', label: 'SLA Status', path: '/engineer/sla' },
        { id: 'settings', label: 'Settings', path: '/engineer/settings' },
    ];

    const managementMenuItems = [
        { id: 'dashboard', label: 'Dashboard', path: '/management/dashboard' },
        { id: 'tickets', label: 'Tickets Overview', path: '/management/tickets' },
        { id: 'recent', label: 'Recent Tickets', path: '/management/recent' },
        { id: 'engineers', label: 'Engineer Performance', path: '/management/engineers' },
        { id: 'categories', label: 'Category Analysis', path: '/management/categories' },
        { id: 'sla', label: 'SLA Analysis', path: '/management/sla' },
        { id: 'departments', label: 'Department Report', path: '/management/departments' },
        { id: 'response-time', label: 'Response Time', path: '/management/response-time' },
        { id: 'workload', label: 'Team Workload', path: '/management/workload' },
        { id: 'escalations', label: 'Escalations', path: '/management/escalations' },
        { id: 'audit', label: 'Audit Log', path: '/management/audit' },
        { id: 'reports', label: 'Reports', path: '/management/reports' },
        ...(mgmt?.role === 'IT_COORDINATOR' ? [{ id: 'dept-mgmt', label: 'Manage Departments', path: '/management/department-mgmt' }] : []),
        { id: 'users', label: 'User Management', path: '/management/users' },
        { id: 'settings', label: 'Settings', path: '/management/settings' },
    ];

    const menuItems = role === 'management' ? managementMenuItems : engineerMenuItems;

    const handleNavigation = (path) => {
        navigate(path);
    };

    return (
        <aside className="sidebar">
                <div className="sidebar-header">
                    <div className="sidebar-logo">PowerGrid</div>
                </div>

                <nav className="sidebar-nav">
                    <ul className="nav-menu">
                        {menuItems.map(item => (
                            <li key={item.id} className="nav-item">
                                <button
                                    className="nav-link"
                                    onClick={() => handleNavigation(item.path)}
                                    title={item.label}
                                >
                                    {item.label}
                                </button>
                            </li>
                        ))}
                    </ul>
                </nav>

                <div className="sidebar-footer">
                    <div className="support-card">
                        <button className="support-item">
                            <span>Help & Support</span>
                        </button>
                        <a href="mailto:support@powergrid.com" className="support-item" style={{ textDecoration: 'none', color: 'inherit' }}>
                            <span>support@powergrid.com</span>
                        </a>
                    </div>
                </div>
            </aside>
        );
    }
