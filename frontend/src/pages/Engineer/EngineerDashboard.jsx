import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout/Layout';
import { fetchEngineerDashboard, getAssignedTickets } from '../../utils/api';
import { showToast, getUserFriendlyMessage } from '../../utils/toastConfig';
import { isLoggedIn, formatStatus } from '../../utils/auth';

export default function EngineerDashboard() {
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const [allTickets, setAllTickets] = useState([]);
    const [stats, setStats] = useState({});
    const [currentFilter, setCurrentFilter] = useState('all');
    const [searchQuery, setSearchQuery] = useState('');

    const loadDashboard = useCallback(async () => {
        if (!isLoggedIn()) { navigate('/login', { replace: true }); return; }
        setIsLoading(true);
        try {
            // Fetch dashboard data
            const data = await fetchEngineerDashboard();
            console.log('Dashboard full data:', data);

            // Get tickets from dashboard
            let tickets = data.tickets || [];
            console.log('Dashboard tickets:', tickets);

            // If no tickets from dashboard, try separate endpoint
            if (tickets.length === 0) {
                console.log('No tickets in dashboard, fetching from tickets endpoint...');
                try {
                    let ticketsData = await getAssignedTickets(null, null, true);
                    tickets = ticketsData.tickets || ticketsData.data || ticketsData.items || [];

                    // If still empty, try all tickets
                    if (tickets.length === 0) {
                        console.log('No active tickets, trying all tickets...');
                        ticketsData = await getAssignedTickets(null, null, false);
                        tickets = ticketsData.tickets || ticketsData.data || ticketsData.items || [];
                    }
                } catch (err) {
                    console.error('Error fetching tickets endpoint:', err);
                    showToast.error(getUserFriendlyMessage('Failed to load tickets'));
                    setIsLoading(false);
                    return;
                }
            }

            setAllTickets(tickets);
            setStats(data.stats || {});
            console.log('Final tickets set:', tickets);
            setIsLoading(false);
        } catch (error) {
            showToast.error(getUserFriendlyMessage('Failed to load dashboard'));
            setIsLoading(false);
        }
    }, [navigate]);

    const handleRefresh = async () => {
        setRefreshing(true);
        await loadDashboard();
    };

    useEffect(() => { loadDashboard(); }, [loadDashboard]);

    // Compute stats
    const activeCount = allTickets.filter(t => ['ASSIGNED', 'IN_PROGRESS'].includes(t.status)).length;
    const inProgressCount = allTickets.filter(t => t.status === 'IN_PROGRESS').length;
    const assignedCount = allTickets.filter(t => t.status === 'ASSIGNED').length;
    const slaAtRisk = allTickets.filter(t => ['CRITICAL', 'BREACHED', 'WARNING'].includes(t.slaStatus)).length;

    // Filter tickets
    let filtered = allTickets;
    if (currentFilter === 'ASSIGNED') filtered = allTickets.filter(t => t.status === 'ASSIGNED');
    else if (currentFilter === 'IN_PROGRESS') filtered = allTickets.filter(t => t.status === 'IN_PROGRESS');
    else if (currentFilter === 'sla') filtered = allTickets.filter(t => ['CRITICAL', 'BREACHED', 'WARNING'].includes(t.slaStatus));

    if (searchQuery) {
        const q = searchQuery.toLowerCase();
        filtered = filtered.filter(t =>
            (t.ticketId && t.ticketId.toLowerCase().includes(q)) ||
            (t.subject && t.subject.toLowerCase().includes(q)) ||
            (t.emailSubject && t.emailSubject.toLowerCase().includes(q)) ||
            (t.category && t.category.toLowerCase().includes(q))
        );
    }

    // SLA alert tickets
    const alertTickets = allTickets.filter(t => ['CRITICAL', 'BREACHED', 'WARNING'].includes(t.slaStatus));


    return (
        <Layout showNav={true} showUser={true} showSidebar={true} showSecondaryNav={true} isLoading={isLoading}>
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
                    <h1>Dashboard</h1>
                    <p>Welcome back! Here's an overview of your assigned tickets.</p>
                </div>
                <div>
                    <button
                        className={`refresh-icon-btn ${refreshing ? 'rotating' : ''}`}
                        onClick={handleRefresh}
                        disabled={refreshing}
                        title="Refresh dashboard"
                    >
                        ↻
                    </button>
                </div>
            </div>

            {/* Stats Grid */}
            <div className="stats-grid">
                <div className="stat-card">
                    <div className="stat-label">Active Tickets</div>
                    <div className="stat-value">{activeCount}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">In Progress</div>
                    <div className="stat-value">{inProgressCount}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Resolved Today</div>
                    <div className="stat-value">{stats.resolvedToday || 0}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">SLA At Risk</div>
                    <div className="stat-value" style={{ color: slaAtRisk > 0 ? 'var(--danger)' : undefined }}>{slaAtRisk}</div>
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
                                    onClick={() => navigate('/engineer/tickets')}
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
                                            <span className={`badge badge-${(t.priority || 'medium').toLowerCase()}`} style={{ fontSize: 11 }}>{t.priority || 'MEDIUM'}</span>
                                        </div>
                                        <div style={{ fontSize: 13, color: '#1E293B', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                            {t.subject || t.emailSubject || 'No subject'}
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
                    <button className={`filter-tab ${currentFilter === 'all' ? 'active' : ''}`} onClick={() => setCurrentFilter('all')}>
                        All <span className="count">{allTickets.length}</span>
                    </button>
                    <button className={`filter-tab ${currentFilter === 'ASSIGNED' ? 'active' : ''}`} onClick={() => setCurrentFilter('ASSIGNED')}>
                        Assigned <span className="count">{assignedCount}</span>
                    </button>
                    <button className={`filter-tab ${currentFilter === 'IN_PROGRESS' ? 'active' : ''}`} onClick={() => setCurrentFilter('IN_PROGRESS')}>
                        In Progress <span className="count">{inProgressCount}</span>
                    </button>
                    <button className={`filter-tab ${currentFilter === 'sla' ? 'active' : ''}`} onClick={() => setCurrentFilter('sla')}>
                        SLA Critical <span className="count">{slaAtRisk}</span>
                    </button>
                </div>
                <div className="filter-actions">
                    <input
                        type="text"
                        className="search-input"
                        placeholder="Search tickets..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        style={{ width: 280, flexShrink: 0 }}
                    />
                </div>
            </div>

            {/* Tickets Table */}
            <div className="card">
                <div className="card-header">
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <h2 style={{ color: '#1E293B' }}>My Tickets <span style={{ background: 'linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%)', color: 'white', padding: '3px 10px', borderRadius: 12, fontSize: 12, marginLeft: 8 }}>{filtered.length}</span></h2>
                    </div>
                    <button
                        onClick={() => navigate('/engineer/tickets')}
                        style={{
                            background: 'none',
                            border: 'none',
                            color: '#1E293B',
                            fontSize: 14,
                            fontWeight: 600,
                            cursor: 'pointer',
                            textDecoration: 'none',
                            transition: 'all 0.2s ease',
                            padding: '4px 8px',
                        }}
                        onMouseEnter={(e) => e.target.style.opacity = '0.7'}
                        onMouseLeave={(e) => e.target.style.opacity = '1'}
                    >
                        View All →
                    </button>
                </div>
                {filtered.length === 0 ? (
                    <div className="empty-state">
                        <h3>No tickets found</h3>
                        <p>You don't have any tickets matching the current filter. Check back later!</p>
                    </div>
                ) : (
                    <div style={{ overflowX: 'auto' }}>
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th style={{ width: 130, textAlign: 'center' }}>Ticket ID</th>
                                    <th style={{ width: 320, textAlign: 'center' }}>Subject</th>
                                    <th style={{ width: 110, textAlign: 'center' }}>Category</th>
                                    <th style={{ width: 100, textAlign: 'center' }}>Priority</th>
                                    <th style={{ width: 110, textAlign: 'center' }}>Status</th>
                                    <th style={{ width: 150, textAlign: 'center' }}>SLA Remaining</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filtered.map(ticket => (
                                    <tr key={ticket.ticketId}>
                                        <td style={{ width: 130, textAlign: 'center' }}><span style={{ fontWeight: 600, color: 'var(--primary-light)' }}>{ticket.ticketId}</span></td>
                                        <td style={{ width: 320, textAlign: 'center', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                            {ticket.subject || ticket.emailSubject || 'No subject'}
                                        </td>
                                        <td style={{ width: 110, textAlign: 'center' }}><span className="category-tag">{ticket.category || 'GENERAL'}</span></td>
                                        <td style={{ width: 100, textAlign: 'center' }}><span className={`badge badge-${(ticket.priority || 'medium').toLowerCase()}`}>{ticket.priority || 'MEDIUM'}</span></td>
                                        <td style={{ width: 110, textAlign: 'center' }}><span className={`badge badge-${(ticket.status || 'assigned').toLowerCase().replace('_', '-')}`}>{formatStatus(ticket.status)}</span></td>
                                        <td style={{ width: 150, textAlign: 'center' }}>
                                            <div className="sla-cell">
                                                <span className={`sla-dot ${(ticket.slaStatus || 'on-track').toLowerCase().replace('_', '-')}`}></span>
                                                <span className={`sla-time ${(ticket.slaStatus || 'on-track').toLowerCase().replace('_', '-')}`}>{ticket.slaRemaining || 'N/A'}</span>
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
