import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout/Layout';
import { showToast, getUserFriendlyMessage } from '../../utils/toastConfig';
import { isLoggedIn } from '../../utils/auth';

export default function Escalations() {
    const navigate = useNavigate();
    const [refreshing, setRefreshing] = useState(false);
    const [escalations, setEscalations] = useState([]);
    const [searchQuery, setSearchQuery] = useState('');
    const [statusFilter, setStatusFilter] = useState('all');
    const [priorityFilter, setPriorityFilter] = useState('all');
    const [sortBy, setSortBy] = useState('recent');

    const loadEscalations = useCallback(async () => {
        if (!isLoggedIn()) { navigate('/login', { replace: true }); return; }
        try {
            const token = sessionStorage.getItem('sessionToken');
            const response = await fetch('/api/management/dashboard/escalations', {
                headers: { 'X-Session-Token': token },
            });
            if (!response.ok) {
                throw new Error(`Escalations API error: ${response.status}`);
            }
            const data = await response.json();
            console.log('Escalations data:', data);

            // Extract escalations
            let escalationList = data.data?.escalations || data.escalations || [];
            if (!Array.isArray(escalationList)) escalationList = [];
            setEscalations(escalationList);

            setRefreshing(false);
        } catch (error) {
            showToast.error(getUserFriendlyMessage('Failed to load escalations'));
            setEscalations([]);
            setRefreshing(false);
        }
    }, [navigate]);

    const handleRefresh = async () => {
        setRefreshing(true);
        await loadEscalations();
    };

    useEffect(() => { loadEscalations(); }, [loadEscalations]);

    // Filter and sort
    let filtered = escalations;

    if (statusFilter !== 'all') {
        filtered = filtered.filter(e => (e.status || 'pending').toUpperCase() === statusFilter.toUpperCase());
    }

    if (priorityFilter !== 'all') {
        filtered = filtered.filter(e => (e.priority || 'medium').toUpperCase() === priorityFilter.toUpperCase());
    }

    if (searchQuery) {
        const q = searchQuery.toLowerCase();
        filtered = filtered.filter(e =>
            (e.ticketId && e.ticketId.toLowerCase().includes(q)) ||
            (e.description && e.description.toLowerCase().includes(q)) ||
            (e.reason && e.reason.toLowerCase().includes(q)) ||
            (e.escalatedTo && e.escalatedTo.toLowerCase().includes(q)) ||
            (e.escalatedBy && e.escalatedBy.toLowerCase().includes(q))
        );
    }

    // Sort
    if (sortBy === 'recent') {
        filtered = [...filtered].sort((a, b) => new Date(b.escalatedAt || b.createdAt) - new Date(a.escalatedAt || a.createdAt));
    } else if (sortBy === 'oldest') {
        filtered = [...filtered].sort((a, b) => new Date(a.escalatedAt || a.createdAt) - new Date(b.escalatedAt || b.createdAt));
    } else if (sortBy === 'priority') {
        const priorityOrder = { CRITICAL: 0, HIGH: 1, MEDIUM: 2, LOW: 3 };
        filtered = [...filtered].sort((a, b) => (priorityOrder[a.priority] || 99) - (priorityOrder[b.priority] || 99));
    }

    const statusCounts = {
        pending: escalations.filter(e => (e.status || 'pending').toUpperCase() === 'PENDING').length,
        inProgress: escalations.filter(e => (e.status || '').toUpperCase() === 'IN_PROGRESS').length,
        resolved: escalations.filter(e => (e.status || '').toUpperCase() === 'RESOLVED').length,
        rejected: escalations.filter(e => (e.status || '').toUpperCase() === 'REJECTED').length,
    };

    const getStatusBadge = (status) => {
        const s = (status || 'pending').toUpperCase();
        if (s === 'PENDING') return 'badge-warning';
        if (s === 'IN_PROGRESS') return 'badge-info';
        if (s === 'RESOLVED') return 'badge-success';
        if (s === 'REJECTED') return 'badge-danger';
        return 'badge-neutral';
    };

    const formatDate = (date) => {
        if (!date) return 'N/A';
        const d = new Date(date);
        return d.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };


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
                    <h1>Escalations</h1>
                    <p>Track and manage ticket escalations and priority overrides.</p>
                </div>
                <button
                    className={`refresh-icon-btn ${refreshing ? 'rotating' : ''}`}
                    onClick={handleRefresh}
                    disabled={refreshing}
                    title="Refresh data"
                >
                    ↻
                </button>
            </div>

            {/* Stats Grid */}
            <div className="stats-grid">
                <div className="stat-card">
                    <div className="stat-label">Total Escalations</div>
                    <div className="stat-value">{escalations.length}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Pending</div>
                    <div className="stat-value">{statusCounts.pending}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">In Progress</div>
                    <div className="stat-value">{statusCounts.inProgress}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Resolved</div>
                    <div className="stat-value">{statusCounts.resolved}</div>
                </div>
            </div>

            {/* Filters */}
            <div className="filters-bar">
                <div className="filter-tabs">
                    <button className={`filter-tab ${statusFilter === 'all' ? 'active' : ''}`} onClick={() => setStatusFilter('all')}>
                        All <span className="tab-count">{escalations.length}</span>
                    </button>
                    <button className={`filter-tab ${statusFilter === 'PENDING' ? 'active' : ''}`} onClick={() => setStatusFilter('PENDING')}>
                        Pending <span className="tab-count">{statusCounts.pending}</span>
                    </button>
                    <button className={`filter-tab ${statusFilter === 'IN_PROGRESS' ? 'active' : ''}`} onClick={() => setStatusFilter('IN_PROGRESS')}>
                        In Progress <span className="tab-count">{statusCounts.inProgress}</span>
                    </button>
                    <button className={`filter-tab ${statusFilter === 'RESOLVED' ? 'active' : ''}`} onClick={() => setStatusFilter('RESOLVED')}>
                        Resolved <span className="tab-count">{statusCounts.resolved}</span>
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
                        <option value="priority">By Priority</option>
                    </select>
                    <select
                        value={priorityFilter}
                        onChange={(e) => setPriorityFilter(e.target.value)}
                        className="search-input"
                        style={{ width: 130 }}
                    >
                        <option value="all">All Priorities</option>
                        <option value="CRITICAL">Critical</option>
                        <option value="HIGH">High</option>
                        <option value="MEDIUM">Medium</option>
                        <option value="LOW">Low</option>
                    </select>
                    <input
                        type="text"
                        className="search-input"
                        placeholder="Search escalations..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        style={{ width: 500 }}
                    />
                </div>
            </div>

            {/* Escalations Table */}
            <div className="card">
                <div className="card-header">
                    <h2 style={{ color: '#1E293B', margin: 0 }}>Escalation History <span style={{ background: 'linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%)', color: 'white', padding: '3px 10px', borderRadius: 12, fontSize: 12, marginLeft: 8 }}>{filtered.length}</span></h2>
                </div>
                {filtered.length === 0 ? (
                    <div className="empty-state">
                        <h3>No escalations found</h3>
                        <p>No escalations match your filter criteria.</p>
                    </div>
                ) : (
                    <div style={{ overflowX: 'auto' }}>
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th style={{ width: 130, textAlign: 'center' }}>Ticket ID</th>
                                    <th style={{ width: 180, textAlign: 'center' }}>Description</th>
                                    <th style={{ width: 100, textAlign: 'center' }}>Priority</th>
                                    <th style={{ width: 110, textAlign: 'center' }}>Status</th>
                                    <th style={{ width: 130, textAlign: 'center' }}>Escalated By</th>
                                    <th style={{ width: 130, textAlign: 'center' }}>Escalated To</th>
                                    <th style={{ width: 150, textAlign: 'center' }}>Reason</th>
                                    <th style={{ width: 120, textAlign: 'center' }}>Date</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filtered.map((escalation, idx) => (
                                    <tr key={idx}>
                                        <td style={{ width: 130, textAlign: 'center', paddingLeft: 16 }}><span style={{ fontWeight: 600, color: '#2563EB' }}>{escalation.ticketId}</span></td>
                                        <td style={{ width: 180, textAlign: 'center' }} className="description-cell">{escalation.description || 'No description'}</td>
                                        <td style={{ width: 100, textAlign: 'center' }}><span className={`badge badge-${(escalation.priority || 'medium').toLowerCase()}`}>{escalation.priority || 'MEDIUM'}</span></td>
                                        <td style={{ width: 110, textAlign: 'center' }}><span className={`badge ${getStatusBadge(escalation.status)}`}>{escalation.status || 'PENDING'}</span></td>
                                        <td style={{ width: 130, textAlign: 'center', fontSize: 12 }}>{escalation.escalatedBy || 'System'}</td>
                                        <td style={{ width: 130, textAlign: 'center', fontSize: 12, fontWeight: 500 }}>{escalation.escalatedTo || 'Management'}</td>
                                        <td style={{ width: 150, textAlign: 'center', fontSize: 12 }} className="description-cell">{escalation.reason || 'SLA at risk'}</td>
                                        <td style={{ width: 120, textAlign: 'center', fontSize: 12 }}>{formatDate(escalation.escalatedAt || escalation.createdAt)}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

            {/* Escalation Reasons Summary */}
            {escalations.length > 0 && (
                <div className="card">
                    <div className="card-header">
                        <h3>Escalation Reasons</h3>
                    </div>
                    <div className="card-body">
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 12 }}>
                            {Array.from(new Set(escalations.map(e => e.reason || 'SLA at risk'))).map((reason, idx) => {
                                const count = escalations.filter(e => (e.reason || 'SLA at risk') === reason).length;
                                return (
                                    <div key={idx} style={{ padding: 12, backgroundColor: '#F8FAFC', borderRadius: 8, border: '1px solid #E2E8F0' }}>
                                        <div style={{ fontWeight: 600, fontSize: 13, color: '#1E293B' }}>{reason}</div>
                                        <div style={{ fontSize: 20, fontWeight: 700, color: '#2563EB', marginTop: 8 }}>{count}</div>
                                        <div style={{ fontSize: 11, color: '#94A3B8', marginTop: 4 }}>
                                            {((count / escalations.length) * 100).toFixed(1)}% of escalations
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                </div>
            )}
        </Layout>
    );
}
