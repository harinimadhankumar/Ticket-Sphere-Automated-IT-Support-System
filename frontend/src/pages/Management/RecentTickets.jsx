import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout/Layout';
import { fetchManagementRecentTickets } from '../../utils/api';
import { showToast, getUserFriendlyMessage } from '../../utils/toastConfig';
import { isLoggedIn, formatStatus } from '../../utils/auth';

export default function RecentTickets() {
    const navigate = useNavigate();
    const [refreshing, setRefreshing] = useState(false);
    const [tickets, setTickets] = useState([]);
    const [searchQuery, setSearchQuery] = useState('');
    const [filterStatus, setFilterStatus] = useState('all');

    const loadTickets = useCallback(async () => {
        if (!isLoggedIn()) { navigate('/login', { replace: true }); return; }
        try {
            const response = await fetchManagementRecentTickets();
            console.log('Recent tickets response:', response);

            // Backend returns: { success, data: { tickets: [...], count, department } }
            let ticketList = response?.data?.tickets || [];
            if (!Array.isArray(ticketList)) ticketList = [];

            console.log('Extracted tickets:', ticketList);
            setTickets(ticketList);
            setRefreshing(false);
        } catch (error) {
            showToast.error(getUserFriendlyMessage('Failed to load recent tickets'));
            setTickets([]);
            setRefreshing(false);
        }
    }, [navigate]);

    const handleRefresh = async () => {
        setRefreshing(true);
        await loadTickets();
    };

    useEffect(() => { loadTickets(); }, [loadTickets]);

    // Filter
    let filtered = tickets;

    if (filterStatus !== 'all') {
        filtered = filtered.filter(t => (t.status || 'OPEN').toUpperCase() === filterStatus.toUpperCase());
    }

    if (searchQuery) {
        const q = searchQuery.toLowerCase();
        filtered = filtered.filter(t =>
            (t.ticketId && t.ticketId.toLowerCase().includes(q)) ||
            (t.issueDescription && t.issueDescription.toLowerCase().includes(q)) ||
            (t.category && t.category.toLowerCase().includes(q))
        );
    }

    // Count SLA alerts
    const alertTickets = tickets.filter(t => ['CRITICAL', 'BREACHED', 'WARNING'].includes(t.slaStatus));


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
                    <h1>Recent Tickets</h1>
                    <p>Latest tickets and SLA alerts requiring immediate attention.</p>
                </div>
                <div>
                    <button
                        className={`refresh-icon-btn ${refreshing ? 'rotating' : ''}`}
                        onClick={handleRefresh}
                        disabled={refreshing}
                        title="Refresh data"
                    >
                        ↻
                    </button>
                </div>
            </div>

            {/* Stats Grid */}
            <div className="stats-grid">
                <div className="stat-card">
                    <div className="stat-label">Recent Tickets</div>
                    <div className="stat-value">{tickets.length}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">SLA Alerts</div>
                    <div className="stat-value">
                        {alertTickets.length}
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Critical Priority</div>
                    <div className="stat-value">
                        {tickets.filter(t => t.priority === 'CRITICAL').length}
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Open Tickets</div>
                    <div className="stat-value">
                        {tickets.filter(t => ['OPEN', 'ASSIGNED'].includes(t.status)).length}
                    </div>
                </div>
            </div>

            {/* SLA Alerts */}
            {alertTickets.length > 0 && (
                <div className="card" style={{ marginBottom: 32 }}>
                    <div className="card-header">
                        <h2>SLA Warnings — Immediate Attention Required</h2>
                    </div>
                    <div style={{ maxHeight: 300, overflowY: 'auto', padding: '16px' }}>
                        <div style={{ display: 'grid', gap: 12 }}>
                            {alertTickets.map(t => (
                                <div
                                    key={t.ticketId}
                                    onClick={() => navigate('/management/tickets')}
                                    style={{
                                        padding: '14px 16px',
                                        border: '1px solid #E5E7EB',
                                        borderRadius: '8px',
                                        background: '#F9FAFB',
                                        cursor: 'pointer',
                                        transition: 'all 0.2s ease',
                                        display: 'flex',
                                        justifyContent: 'space-between',
                                        alignItems: 'center',
                                    }}
                                    onMouseEnter={(e) => {
                                        e.currentTarget.style.background = '#F3F4F6';
                                        e.currentTarget.style.borderColor = '#D1D5DB';
                                        e.currentTarget.style.transform = 'translateY(-1px)';
                                    }}
                                    onMouseLeave={(e) => {
                                        e.currentTarget.style.background = '#F9FAFB';
                                        e.currentTarget.style.borderColor = '#E5E7EB';
                                        e.currentTarget.style.transform = 'translateY(0)';
                                    }}
                                >
                                    <div style={{ flex: 1 }}>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 6 }}>
                                            <span style={{ fontWeight: 600, color: 'var(--primary-light)', fontSize: 14 }}>{t.ticketId}</span>
                                            <span className={`badge badge-${(t.priority || 'medium').toLowerCase()}`} style={{ fontSize: 11 }}>
                                                {t.priority || 'MEDIUM'}
                                            </span>
                                        </div>
                                        <div style={{ fontSize: 13, color: '#1E293B', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                            {t.subject || t.issueDescription || 'No subject'}
                                        </div>
                                    </div>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginLeft: 16 }}>
                                        <span className={`sla-dot ${(t.slaStatus || 'critical').toLowerCase().replace('_', '-')}`}></span>
                                        <span style={{ fontWeight: 600, color: ['CRITICAL', 'BREACHED'].includes(t.slaStatus) ? '#DC2626' : '#D97706', minWidth: 60, textAlign: 'right', fontSize: 13 }}>
                                            {t.slaRemaining || 'BREACHED'}
                                        </span>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            )}

            {/* Filters */}
            <div className="filters-bar">
                <div className="filter-tabs">
                    <button className={`filter-tab ${filterStatus === 'all' ? 'active' : ''}`} onClick={() => setFilterStatus('all')}>
                        All <span className="tab-count">{tickets.length}</span>
                    </button>
                    <button className={`filter-tab ${filterStatus === 'OPEN' ? 'active' : ''}`} onClick={() => setFilterStatus('OPEN')}>
                        Open <span className="tab-count">{tickets.filter(t => t.status === 'OPEN').length}</span>
                    </button>
                    <button className={`filter-tab ${filterStatus === 'IN_PROGRESS' ? 'active' : ''}`} onClick={() => setFilterStatus('IN_PROGRESS')}>
                        In Progress <span className="tab-count">{tickets.filter(t => t.status === 'IN_PROGRESS').length}</span>
                    </button>
                </div>
                <div className="filter-actions">
                    <input
                        type="text"
                        className="search-input"
                        placeholder="Search by ticket ID or description..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        style={{ width: 700, flexShrink: 0 }}
                    />
                </div>
            </div>

            {/* Recent Tickets Table */}
            <div className="card">
                <div className="card-header">
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <h2 style={{ color: '#1E293B' }}>Tickets <span style={{ background: 'linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%)', color: 'white', padding: '3px 10px', borderRadius: 12, fontSize: 12, marginLeft: 8 }}>{filtered.length}</span></h2>
                    </div>
                </div>
                {filtered.length === 0 ? (
                    <div className="empty-state">
                        <h3>No tickets found</h3>
                        <p>No recent tickets match your current filters.</p>
                    </div>
                ) : (
                    <div style={{ overflowX: 'auto' }}>
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th style={{ width: 130, textAlign: 'center' }}>Ticket ID</th>
                                    <th style={{ width: 180, textAlign: 'center' }}>Description</th>
                                    <th style={{ width: 100, textAlign: 'center' }}>Category</th>
                                    <th style={{ width: 100, textAlign: 'center' }}>Priority</th>
                                    <th style={{ width: 110, textAlign: 'center' }}>Status</th>
                                    <th style={{ width: 130, textAlign: 'center' }}>Assigned To</th>
                                    <th style={{ width: 130, textAlign: 'center' }}>SLA Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filtered.map(ticket => (
                                    <tr key={ticket.ticketId}>
                                        <td style={{ width: 130, textAlign: 'center', paddingLeft: 16 }}><span style={{ fontWeight: 600, color: 'var(--primary-light)' }}>{ticket.ticketId}</span></td>
                                        <td style={{ width: 180, textAlign: 'center' }} className="description-cell">
                                            {ticket.issueDescription || 'No description'}
                                        </td>
                                        <td style={{ width: 100, textAlign: 'center' }}><span className="category-tag">{ticket.category || 'GENERAL'}</span></td>
                                        <td style={{ width: 100, textAlign: 'center' }}><span className={`badge badge-${(ticket.priority || 'medium').toLowerCase()}`}>{ticket.priority || 'MEDIUM'}</span></td>
                                        <td style={{ width: 110, textAlign: 'center' }}><span className={`badge badge-${(ticket.status || 'open').toLowerCase().replace('_', '-')}`}>{formatStatus(ticket.status)}</span></td>
                                        <td style={{ width: 130, textAlign: 'center', fontSize: 13 }}>{ticket.assignedEngineer || 'Unassigned'}</td>
                                        <td style={{ width: 130, textAlign: 'center' }}>
                                            <div className="sla-cell">
                                                <span className={`sla-dot ${(ticket.slaStatus || 'on-track').toLowerCase().replace('_', '-')}`}></span>
                                                <span className={`sla-time ${(ticket.slaStatus || 'on-track').toLowerCase().replace('_', '-')}`}>
                                                    {ticket.slaBreached ? 'BREACHED' : ticket.slaStatus || 'ON TRACK'}
                                                </span>
                                            </div>
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
