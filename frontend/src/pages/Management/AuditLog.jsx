import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout/Layout';
import { showToast, getUserFriendlyMessage } from '../../utils/toastConfig';
import { isLoggedIn } from '../../utils/auth';

export default function AuditLog() {
    const navigate = useNavigate();
    const [refreshing, setRefreshing] = useState(false);
    const [auditLogs, setAuditLogs] = useState([]);
    const [searchQuery, setSearchQuery] = useState('');
    const [filterTab, setFilterTab] = useState('all');
    const [sortBy, setSortBy] = useState('recent');

    const loadAuditLogs = useCallback(async () => {
        if (!isLoggedIn()) { navigate('/login', { replace: true }); return; }
        try {
            // Fetch audit logs from backend
            const token = sessionStorage.getItem('sessionToken');
            const response = await fetch('/api/management/dashboard/audit-logs', {
                headers: { 'X-Session-Token': token },
            });
            if (!response.ok) {
                throw new Error(`Audit API error: ${response.status}`);
            }
            const data = await response.json();
            console.log('Audit logs data:', data);

            // Extract audit logs from response
            let logs = data.data?.logs || data.logs || [];
            if (!Array.isArray(logs)) logs = [];

            setAuditLogs(logs);
            setRefreshing(false);
        } catch (error) {
            showToast.error(getUserFriendlyMessage('Failed to load audit logs'));
            setAuditLogs([]);
            setRefreshing(false);
        }
    }, [navigate]);

    const handleRefresh = async () => {
        setRefreshing(true);
        await loadAuditLogs();
    };

    useEffect(() => { loadAuditLogs(); }, [loadAuditLogs]);

    // Get unique users and actions for filters
    const uniqueUsers = [...new Set(auditLogs.map(log => log.userId || log.user))].filter(Boolean);
    const uniqueActions = [...new Set(auditLogs.map(log => log.action))].filter(Boolean);

    // Filter and sort
    let filtered = auditLogs;

    // Apply action tab filter
    if (filterTab === 'create') {
        filtered = filtered.filter(log => ['CREATE', 'ADD', 'NEW'].some(a => (log.action || '').toUpperCase().includes(a)));
    } else if (filterTab === 'update') {
        filtered = filtered.filter(log => ['UPDATE', 'EDIT', 'MODIFY'].some(a => (log.action || '').toUpperCase().includes(a)));
    } else if (filterTab === 'delete') {
        filtered = filtered.filter(log => ['DELETE', 'REMOVE'].some(a => (log.action || '').toUpperCase().includes(a)));
    } else if (filterTab === 'login') {
        filtered = filtered.filter(log => ['LOGIN', 'LOGOUT'].some(a => (log.action || '').toUpperCase().includes(a)));
    } else if (filterTab === 'other') {
        filtered = filtered.filter(log => {
            const action = (log.action || '').toUpperCase();
            return !['CREATE', 'ADD', 'NEW', 'UPDATE', 'EDIT', 'MODIFY', 'DELETE', 'REMOVE', 'LOGIN', 'LOGOUT'].some(a => action.includes(a));
        });
    }

    if (searchQuery) {
        const q = searchQuery.toLowerCase();
        filtered = filtered.filter(log =>
            (log.userId && log.userId.toLowerCase().includes(q)) ||
            (log.user && log.user.toLowerCase().includes(q)) ||
            (log.action && log.action.toLowerCase().includes(q)) ||
            (log.description && log.description.toLowerCase().includes(q)) ||
            (log.resource && log.resource.toLowerCase().includes(q))
        );
    }

    // Sort
    if (sortBy === 'recent') {
        filtered = [...filtered].sort((a, b) => new Date(b.timestamp || b.createdAt) - new Date(a.timestamp || a.createdAt));
    } else if (sortBy === 'oldest') {
        filtered = [...filtered].sort((a, b) => new Date(a.timestamp || a.createdAt) - new Date(b.timestamp || b.createdAt));
    } else if (sortBy === 'action') {
        filtered = [...filtered].sort((a, b) => (a.action || '').localeCompare(b.action || ''));
    } else if (sortBy === 'user') {
        filtered = [...filtered].sort((a, b) => (a.userId || a.user || '').localeCompare(b.userId || b.user || ''));
    }

    const getActionBadgeColor = (action) => {
        const actionUpper = (action || '').toUpperCase();
        if (['CREATE', 'ADD', 'NEW'].some(a => actionUpper.includes(a))) return 'badge-success';
        if (['UPDATE', 'EDIT', 'MODIFY'].some(a => actionUpper.includes(a))) return 'badge-info';
        if (['DELETE', 'REMOVE'].some(a => actionUpper.includes(a))) return 'badge-danger';
        if (['LOGIN', 'LOGOUT'].some(a => actionUpper.includes(a))) return 'badge-warning';
        return 'badge-neutral';
    };

    const formatTimestamp = (timestamp) => {
        if (!timestamp) return 'N/A';
        const date = new Date(timestamp);
        return date.toLocaleString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    };


    // Calculate tab counts
    const createCount = auditLogs.filter(log => ['CREATE', 'ADD', 'NEW'].some(a => (log.action || '').toUpperCase().includes(a))).length;
    const updateCount = auditLogs.filter(log => ['UPDATE', 'EDIT', 'MODIFY'].some(a => (log.action || '').toUpperCase().includes(a))).length;
    const deleteCount = auditLogs.filter(log => ['DELETE', 'REMOVE'].some(a => (log.action || '').toUpperCase().includes(a))).length;
    const loginCount = auditLogs.filter(log => ['LOGIN', 'LOGOUT'].some(a => (log.action || '').toUpperCase().includes(a))).length;
    const otherCount = auditLogs.filter(log => {
        const action = (log.action || '').toUpperCase();
        return !['CREATE', 'ADD', 'NEW', 'UPDATE', 'EDIT', 'MODIFY', 'DELETE', 'REMOVE', 'LOGIN', 'LOGOUT'].some(a => action.includes(a));
    }).length;

    return (
        <Layout showNav={true} showUser={true} showSidebar={true} showSecondaryNav={true} >
            <style>{`
                @keyframes spin {
                    from { transform: rotate(0deg); }
                    to { transform: rotate(360deg); }
                }

                .refresh-icon-btn {
                    background: none;
                    border: none;
                    cursor: pointer;
                    font-size: 24px;
                    color: #1E293B;
                    padding: 8px;
                    border-radius: 6px;
                    transition: all 0.3s ease;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    width: 40px;
                    height: 40px;
                }

                .refresh-icon-btn:hover {
                    background: #F3F4F6;
                    transform: scale(1.1);
                }

                .refresh-icon-btn.rotating {
                    animation: spin 1s linear infinite;
                }
            `}</style>

            {/* Page Header */}
            <div className="page-header">
                <div>
                    <h1>Audit Log</h1>
                    <p>Complete activity history and system changes tracking.</p>
                </div>
                <button
                    className={`refresh-icon-btn ${refreshing ? 'rotating' : ''}`}
                    onClick={handleRefresh}
                    disabled={refreshing}
                    title="Refresh logs"
                >
                    ↻
                </button>
            </div>

            {/* Stats */}
            <div className="stats-grid">
                <div className="stat-card">
                    <div className="stat-label">Total Activities</div>
                    <div className="stat-value">{auditLogs.length}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Unique Users</div>
                    <div className="stat-value">{uniqueUsers.length}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Action Types</div>
                    <div className="stat-value">{uniqueActions.length}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Filtered Results</div>
                    <div className="stat-value">{filtered.length}</div>
                </div>
            </div>

            {/* Filters */}
            <div className="filters-bar">
                <div className="filter-tabs">
                    <button className={`filter-tab ${filterTab === 'all' ? 'active' : ''}`} onClick={() => setFilterTab('all')}>
                        All <span className="tab-count">{auditLogs.length}</span>
                    </button>
                    <button className={`filter-tab ${filterTab === 'create' ? 'active' : ''}`} onClick={() => setFilterTab('create')}>
                        Create <span className="tab-count">{createCount}</span>
                    </button>
                    <button className={`filter-tab ${filterTab === 'update' ? 'active' : ''}`} onClick={() => setFilterTab('update')}>
                        Update <span className="tab-count">{updateCount}</span>
                    </button>
                    <button className={`filter-tab ${filterTab === 'delete' ? 'active' : ''}`} onClick={() => setFilterTab('delete')}>
                        Delete <span className="tab-count">{deleteCount}</span>
                    </button>
                    <button className={`filter-tab ${filterTab === 'login' ? 'active' : ''}`} onClick={() => setFilterTab('login')}>
                        Login <span className="tab-count">{loginCount}</span>
                    </button>
                    <button className={`filter-tab ${filterTab === 'other' ? 'active' : ''}`} onClick={() => setFilterTab('other')}>
                        Other <span className="tab-count">{otherCount}</span>
                    </button>
                </div>
                <div className="filter-actions">
                    <select
                        value={sortBy}
                        onChange={(e) => setSortBy(e.target.value)}
                        className="search-input"
                        style={{ width: 140 }}
                    >
                        <option value="recent">Most Recent</option>
                        <option value="oldest">Oldest First</option>
                        <option value="action">By Action</option>
                        <option value="user">By User</option>
                    </select>
                    <input
                        type="text"
                        className="search-input"
                        placeholder="Search logs..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        style={{ width: 280 }}
                    />
                </div>
            </div>

            {/* Audit Logs Table */}
            <div className="card">
                <div className="card-header">
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <h2 style={{ color: '#1E293B' }}>Activity Log <span style={{ background: 'linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%)', color: 'white', padding: '3px 10px', borderRadius: 12, fontSize: 12, marginLeft: 8 }}>{filtered.length}</span></h2>
                    </div>
                </div>
                {filtered.length === 0 ? (
                    <div className="empty-state">
                        <h3>No activities found</h3>
                        <p>No logs match your filter criteria.</p>
                    </div>
                ) : (
                    <div style={{ overflowX: 'auto' }}>
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th style={{ width: 150, textAlign: 'center' }}>Timestamp</th>
                                    <th style={{ width: 120, textAlign: 'center' }}>User</th>
                                    <th style={{ width: 130, textAlign: 'center' }}>Action</th>
                                    <th style={{ width: 120, textAlign: 'center' }}>Resource</th>
                                    <th style={{ width: 200, textAlign: 'center' }}>Description</th>
                                    <th style={{ width: 100, textAlign: 'center' }}>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filtered.map((log, idx) => (
                                    <tr key={idx}>
                                        <td style={{ width: 150, textAlign: 'center', paddingLeft: 16, fontSize: 12 }}>{formatTimestamp(log.timestamp || log.createdAt)}</td>
                                        <td style={{ width: 120, textAlign: 'center', fontSize: 12, fontWeight: 500 }}>{log.userId || log.user || 'System'}</td>
                                        <td style={{ width: 130, textAlign: 'center' }}><span className={`badge ${getActionBadgeColor(log.action)}`}>{log.action}</span></td>
                                        <td style={{ width: 120, textAlign: 'center', fontSize: 12 }}>{log.resource || log.resourceType || '---'}</td>
                                        <td style={{ width: 200, textAlign: 'center' }} className="description-cell">{log.description || log.details || 'N/A'}</td>
                                        <td style={{ width: 100, textAlign: 'center' }}>
                                            <span className={`badge ${log.status === 'success' || log.status === 'SUCCESS' ? 'badge-success' : log.status === 'error' || log.status === 'ERROR' ? 'badge-danger' : 'badge-neutral'}`}>
                                                {log.status || 'OK'}
                                            </span>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </Layout>
    );
}
