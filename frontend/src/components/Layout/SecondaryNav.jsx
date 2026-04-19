import React, { useState, useRef, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { getUserRole, getInitials, getEngineerData, getManagementData, logout } from '../../utils/auth';

export default function SecondaryNav() {
    const navigate = useNavigate();
    const location = useLocation();
    const [showDropdown, setShowDropdown] = useState(false);
    const dropdownRef = useRef(null);
    const role = getUserRole();
    const engineer = getEngineerData();
    const mgmt = getManagementData();

    const userName = role === 'engineer' ? (engineer?.name || 'Engineer') : (mgmt?.name || 'Manager');
    const userInitials = getInitials(userName);

    // Close dropdown when clicking outside
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setShowDropdown(false);
            }
        };

        if (showDropdown) {
            document.addEventListener('mousedown', handleClickOutside);
            return () => {
                document.removeEventListener('mousedown', handleClickOutside);
            };
        }
    }, [showDropdown]);

    const navItems = role === 'engineer' ? [
        { label: 'Dashboard', path: '/engineer/dashboard' },
        { label: 'My Tickets', path: '/engineer/tickets' },
        { label: 'Knowledge Base', path: '/engineer/knowledge' },
        { label: 'SLA Status', path: '/engineer/sla' },
        { label: 'Settings', path: '/engineer/settings' },
    ] : role === 'management' ? [
        { label: 'Dashboard', path: '/management/dashboard' },
        { label: 'Tickets', path: '/management/tickets' },
        { label: 'Engineers', path: '/management/engineers' },
        { label: 'Categories', path: '/management/categories' },
        { label: 'Recent', path: '/management/recent' },
        { label: 'SLA', path: '/management/sla' },
        { label: 'Departments', path: '/management/departments' },
        { label: 'Response Time', path: '/management/response-time' },
        { label: 'Workload', path: '/management/workload' },
        { label: 'Escalations', path: '/management/escalations' },
        { label: 'Audit', path: '/management/audit' },
        { label: 'Reports', path: '/management/reports' },
        ...(mgmt?.role === 'IT_COORDINATOR' ? [{ label: 'Manage Departments', path: '/management/department-mgmt' }] : []),
        { label: 'Users', path: '/management/users' },
        { label: 'Settings', path: '/management/settings' },
    ] : [];

    const handleNavClick = (path) => {
        navigate(path);
    };

    const handleProfileClick = () => {
        navigate(role === 'engineer' ? '/engineer/settings?tab=profile' : '/management/settings?tab=profile');
        setShowDropdown(false);
    };

    const handleLogout = () => {
        logout();
        navigate('/login', { replace: true });
        setShowDropdown(false);
    };

    return (
        <nav className="secondary-nav">
                <div className="secondary-nav-left">
                    {navItems.map(item => (
                        <button
                            key={item.path}
                            className={`nav-link-btn ${location.pathname === item.path ? 'active' : ''}`}
                            onClick={() => handleNavClick(item.path)}
                        >
                            {item.label}
                        </button>
                    ))}
                </div>

                <div className="secondary-nav-right">
                    <div style={{ position: 'relative' }} ref={dropdownRef}>
                        <button
                            className="profile-btn"
                            onClick={() => setShowDropdown(!showDropdown)}
                            title={userName}
                        >
                            <div className="profile-avatar-small">{userInitials}</div>
                            <span>{userName}</span>
                        </button>

                        {showDropdown && (
                            <div
                                className="dropdown-menu-nav"
                                onClick={() => setShowDropdown(false)}
                            >
                                <button className="dropdown-item-nav" onClick={handleProfileClick}>
                                    Profile Settings
                                </button>
                                <button className="dropdown-item-nav" onClick={() => { /* Handle help */ }}>
                                    Help & Support
                                </button>
                                <div className="dropdown-divider-nav"></div>
                                <button className="dropdown-item-nav dropdown-logout" onClick={handleLogout}>
                                    Logout
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            </nav>
        );
    }
