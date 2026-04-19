import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout/Layout';
import { fetchManagementEngineers } from '../../utils/api';
import { showToast, getUserFriendlyMessage } from '../../utils/toastConfig';
import { isLoggedIn, getInitials } from '../../utils/auth';

export default function EngineerPerformance() {
    const navigate = useNavigate();
    const [refreshing, setRefreshing] = useState(false);
    const [engineers, setEngineers] = useState([]);
    const [searchQuery, setSearchQuery] = useState('');
    const [filterTab, setFilterTab] = useState('all');
    const [sortBy, setSortBy] = useState('resolved');

    const loadEngineers = useCallback(async () => {
        if (!isLoggedIn()) { navigate('/login', { replace: true }); return; }
        try {
            const data = await fetchManagementEngineers();
            console.log('Engineer performance data:', data);

            // Backend returns: { success, data: { engineers: [ { engineer, totalTickets, resolved, inProgress, resolutionRate, team, slaBreached } ] } }
            let engList = data?.data?.engineers || [];
            if (!Array.isArray(engList)) engList = [];

            // Transform backend fields to frontend field names
            engList = engList.map(e => {
                const totalTickets = e.totalTickets || 0;
                const slaBreached = e.slaBreached || 0;
                let slaCompliance = 0;

                if (typeof e.slaCompliance === 'number' && !isNaN(e.slaCompliance)) {
                    slaCompliance = e.slaCompliance;
                } else if (totalTickets > 0) {
                    const complianceValue = ((totalTickets - slaBreached) / totalTickets) * 100;
                    slaCompliance = parseFloat(complianceValue.toFixed(1));
                }

                return {
                    name: e.engineer || 'Unknown',
                    assignedCount: totalTickets,
                    resolvedCount: e.resolved || 0,
                    inProgressCount: e.inProgress || 0,
                    slaCompliance: isNaN(slaCompliance) ? 0 : slaCompliance,
                    avgResolutionTime: e.avgResolutionTime || 0,
                    team: e.team || 'Unknown',
                    department: e.department || 'Unknown',
                };
            });

            console.log('Transformed engineers:', engList);
            setEngineers(engList);
            setRefreshing(false);
        } catch (error) {
            showToast.error(getUserFriendlyMessage('Failed to load engineer data'));
            setRefreshing(false);
        }
    }, [navigate]);

    const handleRefresh = async () => {
        setRefreshing(true);
        await loadEngineers();
    };

    useEffect(() => { loadEngineers(); }, [loadEngineers]);

    // Filter and sort
    let filtered = engineers;

    // Apply tab filter
    if (filterTab === 'excellent') {
        filtered = filtered.filter(e => (e.slaCompliance || 0) >= 90);
    } else if (filterTab === 'good') {
        filtered = filtered.filter(e => {
            const sla = e.slaCompliance || 0;
            return sla >= 80 && sla < 90;
        });
    } else if (filterTab === 'fair') {
        filtered = filtered.filter(e => {
            const sla = e.slaCompliance || 0;
            return sla >= 70 && sla < 80;
        });
    } else if (filterTab === 'poor') {
        filtered = filtered.filter(e => (e.slaCompliance || 0) < 70);
    }

    if (searchQuery) {
        const q = searchQuery.toLowerCase();
        filtered = filtered.filter(e =>
            (e.name && e.name.toLowerCase().includes(q)) ||
            (e.team && e.team.toLowerCase().includes(q)) ||
            (e.department && e.department.toLowerCase().includes(q))
        );
    }

    // Sort
    if (sortBy === 'resolved') {
        filtered = [...filtered].sort((a, b) => (b.resolvedCount || 0) - (a.resolvedCount || 0));
    } else if (sortBy === 'sla') {
        filtered = [...filtered].sort((a, b) => (b.slaCompliance || 0) - (a.slaCompliance || 0));
    } else if (sortBy === 'rate') {
        filtered = [...filtered].sort((a, b) => {
            const aRate = (a.assignedCount > 0) ? ((a.resolvedCount / a.assignedCount) * 100) : 0;
            const bRate = (b.assignedCount > 0) ? ((b.resolvedCount / b.assignedCount) * 100) : 0;
            return bRate - aRate;
        });
    } else if (sortBy === 'name') {
        filtered = [...filtered].sort((a, b) => (a.name || '').localeCompare(b.name || ''));
    }


    // Calculate status counts
    const excellentCount = engineers.filter(e => (e.slaCompliance || 0) >= 90).length;
    const goodCount = engineers.filter(e => {
        const sla = e.slaCompliance || 0;
        return sla >= 80 && sla < 90;
    }).length;
    const fairCount = engineers.filter(e => {
        const sla = e.slaCompliance || 0;
        return sla >= 70 && sla < 80;
    }).length;
    const poorCount = engineers.filter(e => (e.slaCompliance || 0) < 70).length;

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
                    <h1>Engineer Performance</h1>
                    <p>Individual engineer metrics, SLA compliance, and productivity rankings.</p>
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
                    <div className="stat-label">Total Engineers</div>
                    <div className="stat-value">{engineers.length}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Avg SLA Compliance</div>
                    <div className="stat-value">
                        {engineers.length > 0
                            ? (() => {
                                const sum = engineers.reduce((acc, e) => {
                                    const val = Number(e.slaCompliance) || 0;
                                    return acc + (isNaN(val) ? 0 : val);
                                }, 0);
                                const avg = (sum / engineers.length).toFixed(1);
                                return isNaN(avg) ? 0 : avg;
                            })()
                            : 0}%
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Total Resolved</div>
                    <div className="stat-value">
                        {engineers.reduce((sum, e) => sum + (e.resolvedCount || 0), 0)}
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Active Tickets</div>
                    <div className="stat-value">
                        {engineers.reduce((sum, e) => sum + (e.inProgressCount || 0), 0)}
                    </div>
                </div>
            </div>

            {/* Filters */}
            <div className="filters-bar">
                <div className="filter-tabs">
                    <button
                        className={`filter-tab ${filterTab === 'all' ? 'active' : ''}`}
                        onClick={() => setFilterTab('all')}
                    >
                        All <span className="tab-count">{engineers.length}</span>
                    </button>
                    <button
                        className={`filter-tab ${filterTab === 'excellent' ? 'active' : ''}`}
                        onClick={() => setFilterTab('excellent')}
                    >
                        Excellent (≥90%) <span className="tab-count">{excellentCount}</span>
                    </button>
                    <button
                        className={`filter-tab ${filterTab === 'good' ? 'active' : ''}`}
                        onClick={() => setFilterTab('good')}
                    >
                        Good (80-89%) <span className="tab-count">{goodCount}</span>
                    </button>
                    <button
                        className={`filter-tab ${filterTab === 'fair' ? 'active' : ''}`}
                        onClick={() => setFilterTab('fair')}
                    >
                        Fair (70-79%) <span className="tab-count">{fairCount}</span>
                    </button>
                    <button
                        className={`filter-tab ${filterTab === 'poor' ? 'active' : ''}`}
                        onClick={() => setFilterTab('poor')}
                    >
                        Needs Focus (&lt;70%) <span className="tab-count">{poorCount}</span>
                    </button>
                </div>
                <div className="filter-actions">
                    <select
                        value={sortBy}
                        onChange={(e) => setSortBy(e.target.value)}
                        className="search-input"
                        style={{ width: 200 }}
                    >
                        <option value="resolved">Sort by Resolved</option>
                        <option value="sla">Sort by SLA %</option>
                        <option value="rate">Sort by Resolution Rate</option>
                        <option value="name">Sort by Name</option>
                    </select>
                    <input
                        type="text"
                        className="search-input"
                        placeholder="Search engineers..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        style={{ width: 280 }}
                    />
                </div>
            </div>

            {/* Engineers Table */}
            <div className="card">
                <div className="card-header">
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <h2 style={{ color: '#1E293B' }}>Performance Metrics <span style={{ background: 'linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%)', color: 'white', padding: '3px 10px', borderRadius: 12, fontSize: 12, marginLeft: 8 }}>{filtered.length}</span></h2>
                    </div>
                </div>
                {filtered.length === 0 ? (
                    <div className="empty-state">
                        <h3>No engineers found</h3>
                        <p>No engineers match your search criteria.</p>
                    </div>
                ) : (
                    <div style={{ overflowX: 'auto' }}>
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th style={{ width: 200, textAlign: 'center' }}>Engineer</th>
                                    <th style={{ width: 100, textAlign: 'center' }}>Team</th>
                                    <th style={{ width: 90, textAlign: 'center' }}>Assigned</th>
                                    <th style={{ width: 90, textAlign: 'center' }}>Resolved</th>
                                    <th style={{ width: 110, textAlign: 'center' }}>Resolution %</th>
                                    <th style={{ width: 90, textAlign: 'center' }}>SLA %</th>
                                    <th style={{ width: 110, textAlign: 'center' }}>Avg Time</th>
                                    <th style={{ width: 90, textAlign: 'center' }}>Active</th>
                                    <th style={{ width: 120, textAlign: 'center' }}>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filtered.map((engineer, idx) => {
                                    const assigned = engineer.assignedCount || 0;
                                    const resolved = engineer.resolvedCount || 0;
                                    const resRate = assigned > 0 ? ((resolved / assigned) * 100).toFixed(0) : 0;
                                    const slaPct = engineer.slaCompliance || 0;
                                    const avgTime = engineer.avgResolutionTime || 0;
                                    const inProg = engineer.inProgressCount || 0;

                                    return (
                                        <tr key={idx}>
                                            <td style={{ width: 200, textAlign: 'center', paddingLeft: 16 }}>
                                                <div className="engineer-cell">
                                                    <div className="avatar">{getInitials(engineer.name)}</div>
                                                    <span>{engineer.name}</span>
                                                </div>
                                            </td>
                                            <td style={{ width: 100, textAlign: 'center' }}>{engineer.team || engineer.department || '---'}</td>
                                            <td style={{ width: 90, textAlign: 'center', fontWeight: 500 }}>{assigned}</td>
                                            <td style={{ width: 90, textAlign: 'center', fontWeight: 600, color: 'var(--success)' }}>{resolved}</td>
                                            <td style={{ width: 110, textAlign: 'center' }}>
                                                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8 }}>
                                                    <div className="progress-bar">
                                                        <div className={`progress-fill ${resRate >= 80 ? 'success' : resRate >= 50 ? 'warning' : 'danger'}`} style={{ width: `${resRate}%` }}></div>
                                                    </div>
                                                    <span>{resRate}%</span>
                                                </div>
                                            </td>
                                            <td style={{ width: 90, textAlign: 'center' }}><span className={`badge ${slaPct >= 90 ? 'badge-excellent' : slaPct >= 80 ? 'badge-good' : slaPct >= 70 ? 'badge-fair' : 'badge-poor'}`}>{slaPct}%</span></td>
                                            <td style={{ width: 110, textAlign: 'center', fontWeight: 500 }}>{typeof avgTime === 'number' ? (avgTime / 60).toFixed(1) : avgTime}</td>
                                            <td style={{ width: 90, textAlign: 'center', fontWeight: 500 }}>{inProg}</td>
                                            <td style={{ width: 120, textAlign: 'center' }}>
                                                <span className={`badge ${resRate >= 80 ? 'badge-excellent' : resRate >= 60 ? 'badge-good' : resRate >= 40 ? 'badge-fair' : 'badge-poor'}`}>
                                                    {resRate >= 80 ? 'Excellent' : resRate >= 60 ? 'Good' : resRate >= 40 ? 'Fair' : 'Needs Focus'}
                                                </span>
                                            </td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </Layout>
    );
}
