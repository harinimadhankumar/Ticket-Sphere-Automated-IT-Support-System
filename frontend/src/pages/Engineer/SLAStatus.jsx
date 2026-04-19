import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout/Layout';
import { fetchSLAStatus } from '../../utils/api';
import { showToast, getUserFriendlyMessage } from '../../utils/toastConfig';
import { isLoggedIn } from '../../utils/auth';

export default function SLAStatus() {
    const navigate = useNavigate();
    const [slaData, setSlaData] = useState([]);
    const [filteredData, setFilteredData] = useState([]);
    const [search, setSearch] = useState('');
    const [slaFilter, setSlaFilter] = useState('all');
    const [priorityFilter, setPriorityFilter] = useState('all');

    const getSLAStats = () => {
        return {
            total: slaData.length,
            onTrack: slaData.filter(s => s.slaStatus === 'on-track').length,
            warning: slaData.filter(s => s.slaStatus === 'warning').length,
            breached: slaData.filter(s => ['critical', 'breached'].includes(s.slaStatus)).length,
        };
    };

    const loadSLAData = useCallback(async () => {
        if (!isLoggedIn()) {
            navigate('/login', { replace: true });
            return;
        }
        try {
            console.log('Loading SLA status from backend...');
            const data = await fetchSLAStatus();
            console.log('SLA data from backend:', data);

            let slaArray = [];
            if (Array.isArray(data)) {
                slaArray = data;
            } else if (data?.data && Array.isArray(data.data)) {
                slaArray = data.data;
            } else if (data?.slaStatus && Array.isArray(data.slaStatus)) {
                slaArray = data.slaStatus;
            } else if (data?.items && Array.isArray(data.items)) {
                slaArray = data.items;
            } else if (data?.criticalBreachedTickets && Array.isArray(data.criticalBreachedTickets)) {
                // Handle dashboard response format
                slaArray = data.criticalBreachedTickets.map(ticket => ({
                    ticketId: ticket.ticketId,
                    priority: ticket.priority,
                    status: ticket.status === 'BREACHED' ? 'breached' : 'critical',
                    slaStatus: 'critical',
                    responseTime: ticket.createdTime ? new Date(ticket.createdTime).toLocaleString() : 'N/A',
                    resolutionTime: ticket.slaDeadline ? new Date(ticket.slaDeadline).toLocaleString() : 'N/A'
                }));
            } else if (data?.recentEscalations && Array.isArray(data.recentEscalations)) {
                // Fallback to recent escalations
                slaArray = data.recentEscalations.map(ticket => ({
                    ticketId: ticket.ticketId,
                    priority: ticket.priority,
                    status: ticket.status,
                    slaStatus: 'warning',
                    responseTime: ticket.createdTime ? new Date(ticket.createdTime).toLocaleString() : 'N/A',
                    resolutionTime: ticket.slaDeadline ? new Date(ticket.slaDeadline).toLocaleString() : 'N/A'
                }));
            }

            console.log('Parsed SLA data:', slaArray);
            setSlaData(slaArray);

            if (slaArray.length === 0) {
                showToast.info('ℹ No SLA status data available');
            }
        } catch (error) {
            showToast.error(getUserFriendlyMessage('Failed to load SLA status data'));
            setSlaData([]);
        }
    }, [navigate]);

    useEffect(() => {
        loadSLAData();
    }, [loadSLAData]);

    useEffect(() => {
        let result = slaData;

        // Apply SLA status filter
        if (slaFilter !== 'all') {
            result = result.filter(item =>
                item.slaStatus?.toLowerCase() === slaFilter.toLowerCase()
            );
        }

        // Apply priority filter
        if (priorityFilter !== 'all') {
            result = result.filter(item =>
                item.priority?.toLowerCase() === priorityFilter.toLowerCase()
            );
        }

        // Apply search filter
        if (search.trim()) {
            const searchLower = search.toLowerCase();
            result = result.filter(item =>
                item.ticketId?.toLowerCase().includes(searchLower) ||
                item.priority?.toLowerCase().includes(searchLower) ||
                item.status?.toLowerCase().includes(searchLower)
            );
        }

        setFilteredData(result);
    }, [search, slaFilter, priorityFilter, slaData]);

    const getSLAIndicator = (status) => {
        if (status === 'on-track') return { color: '#10B981', bg: '#ECFDF5', label: 'On Track' };
        if (status === 'warning') return { color: '#F59E0B', bg: '#FFFBEB', label: 'Warning' };
        if (status === 'critical' || status === 'breached') return { color: '#EF4444', bg: '#FEF2F2', label: 'Breached' };
        return { color: '#6B7280', bg: '#F9FAFB', label: 'Unknown' };
    };

    return (
        <Layout showSidebar={true} showSecondaryNav={true}>
            <div className="page-header">
                <div>
                    <h1>SLA Status</h1>
                    <p>Monitor your service level agreements and ticket timelines</p>
                </div>
            </div>

            {/* Stats Grid */}
            <div className="stats-grid">
                <div className="stat-card">
                    <div className="stat-label">Total Tickets</div>
                    <div className="stat-value">{getSLAStats().total || 0}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">On Track</div>
                    <div className="stat-value">{getSLAStats().onTrack || 0}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Warning</div>
                    <div className="stat-value">{getSLAStats().warning || 0}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Breached</div>
                    <div className="stat-value">{getSLAStats().breached || 0}</div>
                </div>
            </div>

            {/* Search and Filter Bar */}
            <div className="filters-bar">
                <div className="filter-tabs">
                    <button className={`filter-tab ${slaFilter === 'all' ? 'active' : ''}`} onClick={() => setSlaFilter('all')}>
                        All <span className="count">{slaData.length}</span>
                    </button>
                    <button className={`filter-tab ${slaFilter === 'on-track' ? 'active' : ''}`} onClick={() => setSlaFilter('on-track')}>
                        On Track <span className="count">{getSLAStats().onTrack}</span>
                    </button>
                    <button className={`filter-tab ${slaFilter === 'warning' ? 'active' : ''}`} onClick={() => setSlaFilter('warning')}>
                        Warning <span className="count">{getSLAStats().warning}</span>
                    </button>
                    <button className={`filter-tab ${slaFilter === 'critical' || slaFilter === 'breached' ? 'active' : ''}`} onClick={() => setSlaFilter('breached')}>
                        Breached <span className="count">{getSLAStats().breached}</span>
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
                        <option value="critical">Critical</option>
                        <option value="high">High</option>
                        <option value="medium">Medium</option>
                        <option value="low">Low</option>
                    </select>
                    <input
                        type="text"
                        className="search-input"
                        placeholder="Search by ticket ID..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        style={{ width: 280, flexShrink: 0 }}
                    />
                </div>
            </div>

            {filteredData.length === 0 ? (
                <div className="empty-state">
                    <h3>{slaData.length === 0 ? 'No SLA data available' : 'No results found'}</h3>
                    <p>{search || slaFilter !== 'all' || priorityFilter !== 'all' ? 'Try adjusting your filters' : 'No active SLA agreements to display'}</p>
                </div>
            ) : (
                <div className="grid-3">
                    {filteredData.map((sla, idx) => (
                        <div key={sla.id || idx} className="card">
                            <div className="card-body">
                                <h3 style={{ fontSize: '16px', fontWeight: 600, marginBottom: '12px', color: '#1E293B' }}>
                                    {sla.ticketId || 'Ticket #' + (idx + 1)}
                                </h3>
                                <div className={`sla-panel ${sla.slaStatus}`} style={{ padding: '12px', marginBottom: '12px' }}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                        <span className={`sla-dot ${sla.slaStatus}`}></span>
                                        <span style={{ fontWeight: 600, fontSize: '14px' }}>
                                            {getSLAIndicator(sla.slaStatus).label}
                                        </span>
                                    </div>
                                </div>
                                <div className="info-grid">
                                    <div className="info-item">
                                        <div className="info-label">Priority</div>
                                        <div className="info-value">{sla.priority || 'N/A'}</div>
                                    </div>
                                    <div className="info-item">
                                        <div className="info-label">Status</div>
                                        <div className="info-value">{sla.status || 'N/A'}</div>
                                    </div>
                                    <div className="info-item">
                                        <div className="info-label">Created</div>
                                        <div className="info-value" style={{ fontSize: '12px' }}>{sla.responseTime}</div>
                                    </div>
                                    <div className="info-item">
                                        <div className="info-label">Deadline</div>
                                        <div className="info-value" style={{ fontSize: '12px' }}>{sla.resolutionTime}</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </Layout>
    );
}
