import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout/Layout';
import { fetchManagementTickets } from '../../utils/api';
import { showToast, getUserFriendlyMessage } from '../../utils/toastConfig';
import { isLoggedIn, formatStatus } from '../../utils/auth';

export default function TicketsOverview() {
    const navigate = useNavigate();
    const [refreshing, setRefreshing] = useState(false);
    const [allTickets, setAllTickets] = useState([]);
    const [stats, setStats] = useState({});
    const [currentFilter, setCurrentFilter] = useState('all');
    const [searchQuery, setSearchQuery] = useState('');
    const [priorityFilter, setPriorityFilter] = useState('all');
    const [statusFilter, setStatusFilter] = useState('all');

    const loadTickets = useCallback(async () => {
        if (!isLoggedIn()) { navigate('/login', { replace: true }); return; }
        try {
            const data = await fetchManagementTickets();
            console.log('Management tickets data:', data);

            // Backend returns: { success, data: { tickets: [...], count, department } }
            let tickets = data?.data?.tickets || [];
            if (!Array.isArray(tickets)) tickets = [];

            console.log('Extracted tickets:', tickets);
            setAllTickets(tickets);

            // Calculate stats
            const stats = {
                total: tickets.length,
                open: tickets.filter(t => t.status === 'OPEN' || t.status === 'ASSIGNED').length,
                inProgress: tickets.filter(t => t.status === 'IN_PROGRESS').length,
                closed: tickets.filter(t => t.status === 'CLOSED' || t.status === 'RESOLVED').length,
                breached: tickets.filter(t => t.slaBreached === true).length,
                atRisk: tickets.filter(t => ['CRITICAL', 'WARNING'].includes(t.slaStatus)).length,
            };
            setStats(stats);
            setRefreshing(false);
        } catch (error) {
            showToast.error(getUserFriendlyMessage('Failed to load tickets'));
            setRefreshing(false);
        }
    }, [navigate]);

    const handleRefresh = async () => {
        setRefreshing(true);
        await loadTickets();
    };

    useEffect(() => { loadTickets(); }, [loadTickets]);

    // Apply filters
    let filtered = allTickets;

    if (currentFilter === 'open') {
        filtered = allTickets.filter(t => ['OPEN', 'ASSIGNED'].includes(t.status));
    } else if (currentFilter === 'inprogress') {
        filtered = allTickets.filter(t => t.status === 'IN_PROGRESS');
    } else if (currentFilter === 'closed') {
        filtered = allTickets.filter(t => ['CLOSED', 'RESOLVED'].includes(t.status));
    } else if (currentFilter === 'breached') {
        filtered = allTickets.filter(t => t.slaBreached === true);
    } else if (currentFilter === 'atrisk') {
        filtered = allTickets.filter(t => ['CRITICAL', 'WARNING'].includes(t.slaStatus));
    }

    if (priorityFilter !== 'all') {
        filtered = filtered.filter(t => (t.priority || 'MEDIUM').toUpperCase() === priorityFilter.toUpperCase());
    }

    if (statusFilter !== 'all') {
        filtered = filtered.filter(t => (t.status || 'OPEN').toUpperCase() === statusFilter.toUpperCase());
    }

    if (searchQuery) {
        const q = searchQuery.toLowerCase();
        filtered = filtered.filter(t =>
            (t.ticketId && t.ticketId.toLowerCase().includes(q)) ||
            (t.issueDescription && t.issueDescription.toLowerCase().includes(q)) ||
            (t.category && t.category.toLowerCase().includes(q)) ||
            (t.assignedEngineer && t.assignedEngineer.toLowerCase().includes(q))
        );
    }


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
                    <h1>Tickets Overview</h1>
                    <p>Complete view of all tickets across departments with real-time status.</p>
                </div>
                <div>
                    <button
                        className={`refresh-icon-btn ${refreshing ? 'rotating' : ''}`}
                        onClick={handleRefresh}
                        disabled={refreshing}
                        title="Refresh tickets"
                    >
                        ↻
                    </button>
                </div>
            </div>

            {/* Stats Grid */}
            <div className="stats-grid">
                <div className="stat-card">
                    <div className="stat-label">Total Tickets</div>
                    <div className="stat-value">{stats.total || 0}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Open</div>
                    <div className="stat-value">{stats.open || 0}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">In Progress</div>
                    <div className="stat-value">{stats.inProgress || 0}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">SLA Breached</div>
                    <div className="stat-value">
                        {stats.breached || 0}
                    </div>
                </div>
            </div>

            {/* Filters */}
            <div className="filters-bar">
                <div className="filter-tabs">
                    <button className={`filter-tab ${currentFilter === 'all' ? 'active' : ''}`} onClick={() => setCurrentFilter('all')}>
                        All <span className="tab-count">{allTickets.length}</span>
                    </button>
                    <button className={`filter-tab ${currentFilter === 'open' ? 'active' : ''}`} onClick={() => setCurrentFilter('open')}>
                        Open <span className="tab-count">{stats.open}</span>
                    </button>
                    <button className={`filter-tab ${currentFilter === 'inprogress' ? 'active' : ''}`} onClick={() => setCurrentFilter('inprogress')}>
                        In Progress <span className="tab-count">{stats.inProgress}</span>
                    </button>
                    <button className={`filter-tab ${currentFilter === 'breached' ? 'active' : ''}`} onClick={() => setCurrentFilter('breached')}>
                        SLA Breached <span className="tab-count">{stats.breached}</span>
                    </button>
                </div>
                <div className="filter-actions">
                    <select
                        className="search-input"
                        value={priorityFilter}
                        onChange={(e) => setPriorityFilter(e.target.value)}
                        style={{ width: 120, flexShrink: 0 }}
                    >
                        <option value="all">All Priorities</option>
                        <option value="CRITICAL">Critical</option>
                        <option value="HIGH">High</option>
                        <option value="MEDIUM">Medium</option>
                        <option value="LOW">Low</option>
                    </select>
                    <select
                        className="search-input"
                        value={statusFilter}
                        onChange={(e) => setStatusFilter(e.target.value)}
                        style={{ width: 120, flexShrink: 0 }}
                    >
                        <option value="all">All Statuses</option>
                        <option value="OPEN">Open</option>
                        <option value="ASSIGNED">Assigned</option>
                        <option value="IN_PROGRESS">In Progress</option>
                        <option value="CLOSED">Closed</option>
                    </select>
                    <input
                        type="text"
                        className="search-input"
                        placeholder="Search by ticket ID or description..."
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
                        <h2 style={{ color: '#1E293B' }}>Tickets <span style={{ background: 'linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%)', color: 'white', padding: '3px 10px', borderRadius: 12, fontSize: 12, marginLeft: 8 }}>{filtered.length}</span></h2>
                    </div>
                </div>
                {filtered.length === 0 ? (
                    <div className="empty-state">
                        <h3>No tickets found</h3>
                        <p>No tickets match your current filters. Try adjusting your search criteria.</p>
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