import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout/Layout';
import { showToast, getUserFriendlyMessage } from '../../utils/toastConfig';
import { isLoggedIn, getInitials } from '../../utils/auth';

export default function TeamWorkload() {
    const navigate = useNavigate();
    const [refreshing, setRefreshing] = useState(false);
    const [teamData, setTeamData] = useState([]);
    const [searchQuery, setSearchQuery] = useState('');
    const [sortBy, setSortBy] = useState('workload');
    const [filterTab, setFilterTab] = useState('all');

    const loadTeamWorkload = useCallback(async () => {
        if (!isLoggedIn()) { navigate('/login', { replace: true }); return; }
        try {
            const token = sessionStorage.getItem('sessionToken');
            const response = await fetch('/api/management/dashboard/team-workload', {
                headers: { 'X-Session-Token': token },
            });
            if (!response.ok) {
                throw new Error(`Team Workload API error: ${response.status}`);
            }
            const data = await response.json();
            console.log('Team workload data:', data);

            // Extract data
            let teams = data.data?.teams || data.teams || [];
            if (!Array.isArray(teams)) teams = [];
            setTeamData(teams);

            setRefreshing(false);
        } catch (error) {
            showToast.error(getUserFriendlyMessage('Failed to load team workload data'));
            setTeamData([]);
            setRefreshing(false);
        }
    }, [navigate]);

    const handleRefresh = async () => {
        setRefreshing(true);
        await loadTeamWorkload();
    };

    useEffect(() => { loadTeamWorkload(); }, [loadTeamWorkload]);

    // Filter and sort
    let filtered = teamData;

    // Apply capacity tab filter
    if (filterTab === 'overloaded') {
        filtered = filtered.filter(t => {
            const total = (t.assignedTickets || 0) + (t.availableSlots || 0);
            return total > 0 && ((t.assignedTickets || 0) / total) * 100 > 100;
        });
    } else if (filterTab === 'high') {
        filtered = filtered.filter(t => {
            const total = (t.assignedTickets || 0) + (t.availableSlots || 0);
            const usage = total > 0 ? ((t.assignedTickets || 0) / total) * 100 : 0;
            return usage >= 80 && usage <= 100;
        });
    } else if (filterTab === 'adequate') {
        filtered = filtered.filter(t => {
            const total = (t.assignedTickets || 0) + (t.availableSlots || 0);
            const usage = total > 0 ? ((t.assignedTickets || 0) / total) * 100 : 0;
            return usage >= 60 && usage < 80;
        });
    } else if (filterTab === 'low') {
        filtered = filtered.filter(t => {
            const total = (t.assignedTickets || 0) + (t.availableSlots || 0);
            return total > 0 && ((t.assignedTickets || 0) / total) * 100 < 60;
        });
    }

    if (searchQuery) {
        const q = searchQuery.toLowerCase();
        filtered = filtered.filter(t =>
            (t.team && t.team.toLowerCase().includes(q)) ||
            (t.name && t.name.toLowerCase().includes(q)) ||
            (t.department && t.department.toLowerCase().includes(q)) ||
            (t.members?.some(m => m.toLowerCase().includes(q)))
        );
    }

    // Sort
    if (sortBy === 'workload') {
        filtered = [...filtered].sort((a, b) => (b.assignedTickets || 0) - (a.assignedTickets || 0));
    } else if (sortBy === 'capacity') {
        filtered = [...filtered].sort((a, b) => {
            const aCapacity = ((a.assignedTickets || 0) / Math.max(a.teamSize || 1, 1)) * 100;
            const bCapacity = ((b.assignedTickets || 0) / Math.max(b.teamSize || 1, 1)) * 100;
            return bCapacity - aCapacity;
        });
    } else if (sortBy === 'performance') {
        filtered = [...filtered].sort((a, b) => (b.resolution || 0) - (a.resolution || 0));
    } else if (sortBy === 'availability') {
        filtered = [...filtered].sort((a, b) => (a.availableSlots || 0) - (b.availableSlots || 0));
    }

    const getCapacityStatus = (assigned, available) => {
        const total = assigned + available;
        if (total === 0) return 'badge-neutral';
        const usage = (assigned / total) * 100;
        if (usage >= 90) return 'badge-danger';
        if (usage >= 75) return 'badge-warning';
        if (usage >= 50) return 'badge-info';
        return 'badge-success';
    };

    // Calculate capacity utilization tab counts
    const overloadedCount = teamData.filter(t => {
        const total = (t.assignedTickets || 0) + (t.availableSlots || 0);
        return total > 0 && ((t.assignedTickets || 0) / total) * 100 > 100;
    }).length;
    const highCount = teamData.filter(t => {
        const total = (t.assignedTickets || 0) + (t.availableSlots || 0);
        const usage = total > 0 ? ((t.assignedTickets || 0) / total) * 100 : 0;
        return usage >= 80 && usage <= 100;
    }).length;
    const adequateCount = teamData.filter(t => {
        const total = (t.assignedTickets || 0) + (t.availableSlots || 0);
        const usage = total > 0 ? ((t.assignedTickets || 0) / total) * 100 : 0;
        return usage >= 60 && usage < 80;
    }).length;
    const lowCount = teamData.filter(t => {
        const total = (t.assignedTickets || 0) + (t.availableSlots || 0);
        return total > 0 && ((t.assignedTickets || 0) / total) * 100 < 60;
    }).length;

    const totalWorkload = teamData.reduce((sum, t) => sum + (t.assignedTickets || 0), 0);
    const avgCapacity = teamData.length > 0
        ? (teamData.reduce((sum, t) => sum + ((t.assignedTickets || 0) / Math.max(t.teamSize || 1, 1)), 0) / teamData.length * 100).toFixed(1)
        : 0;

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
                    <h1>Team Workload Analysis</h1>
                    <p>Monitor team capacity, workload distribution, and resource allocation.</p>
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
                    <div className="stat-label">Teams</div>
                    <div className="stat-value">{teamData.length}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Total Workload</div>
                    <div className="stat-value">{totalWorkload}</div>
                    <div className="stat-sub">active tickets</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Avg Capacity</div>
                    <div className="stat-value">{avgCapacity}%</div>
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
                        All <span className="tab-count">{teamData.length}</span>
                    </button>
                    <button className={`filter-tab ${filterTab === 'overloaded' ? 'active' : ''}`} onClick={() => setFilterTab('overloaded')}>
                        Overloaded (&gt;100%) <span className="tab-count">{overloadedCount}</span>
                    </button>
                    <button className={`filter-tab ${filterTab === 'high' ? 'active' : ''}`} onClick={() => setFilterTab('high')}>
                        High (80-100%) <span className="tab-count">{highCount}</span>
                    </button>
                    <button className={`filter-tab ${filterTab === 'adequate' ? 'active' : ''}`} onClick={() => setFilterTab('adequate')}>
                        Adequate (60-80%) <span className="tab-count">{adequateCount}</span>
                    </button>
                    <button className={`filter-tab ${filterTab === 'low' ? 'active' : ''}`} onClick={() => setFilterTab('low')}>
                        Low (&lt;60%) <span className="tab-count">{lowCount}</span>
                    </button>
                </div>
                <div className="filter-actions">
                    <select
                        value={sortBy}
                        onChange={(e) => setSortBy(e.target.value)}
                        className="search-input"
                        style={{ width: 200 }}
                    >
                        <option value="workload">Sort by Workload</option>
                        <option value="capacity">Sort by Capacity %</option>
                        <option value="performance">Sort by Performance</option>
                        <option value="availability">Sort by Availability</option>
                    </select>
                    <input
                        type="text"
                        className="search-input"
                        placeholder="Search teams..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        style={{ width: 280 }}
                    />
                </div>
            </div>

            {/* Team Workload Cards */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: 16, marginBottom: 20 }}>
                {filtered.map((team, idx) => {
                    const assigned = team.assignedTickets || 0;
                    const available = team.availableSlots || 0;
                    const total = assigned + available;
                    const capacity = total > 0 ? ((assigned / total) * 100).toFixed(0) : 0;
                    return (
                        <div key={idx} className="card" style={{ display: 'flex', flexDirection: 'column' }}>
                            <div className="card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <div>
                                    <h3 style={{ margin: 0 }}>{team.team || team.name}</h3>
                                    <div style={{ fontSize: 12, color: '#94A3B8', marginTop: 4 }}>
                                        {team.department && `${team.department} • `}
                                        {team.teamSize || 0} members
                                    </div>
                                </div>
                                <span className={`badge ${getCapacityStatus(assigned, available)}`}>{capacity}%</span>
                            </div>
                            <div className="card-body">
                                <div style={{ marginBottom: 16 }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8, fontSize: 12, fontWeight: 500, color: '#1E293B' }}>
                                        <span>Capacity Usage</span>
                                        <span style={{ color: '#1E293B', fontWeight: 600 }}>{assigned} / {total} tickets</span>
                                    </div>
                                    <div className="progress-bar">
                                        <div className={`progress-fill ${getCapacityStatus(assigned, available)}`} style={{ width: `${Math.min(capacity, 100)}%` }}></div>
                                    </div>
                                </div>

                                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                                    <div style={{ padding: 12, backgroundColor: '#F8FAFC', borderRadius: 6, textAlign: 'center' }}>
                                        <div style={{ fontSize: 12, color: '#64748B', marginBottom: 6, fontWeight: 500 }}>Assigned</div>
                                        <div style={{ fontSize: 20, fontWeight: 700, color: '#1E293B' }}>{assigned}</div>
                                    </div>
                                    <div style={{ padding: 12, backgroundColor: '#F8FAFC', borderRadius: 6, textAlign: 'center' }}>
                                        <div style={{ fontSize: 12, color: '#64748B', marginBottom: 6, fontWeight: 500 }}>Available</div>
                                        <div style={{ fontSize: 20, fontWeight: 700, color: '#1E293B' }}>{available}</div>
                                    </div>
                                </div>

                                {team.members && team.members.length > 0 && (
                                    <div style={{ marginTop: 12, paddingTop: 12, borderTop: '1px solid #E2E8F0' }}>
                                        <div style={{ fontSize: 11, fontWeight: 600, color: '#64748B', marginBottom: 8 }}>TEAM MEMBERS</div>
                                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                                            {team.members.slice(0, 4).map((member, i) => (
                                                <div
                                                    key={i}
                                                    style={{
                                                        width: 28,
                                                        height: 28,
                                                        borderRadius: 50,
                                                        backgroundColor: '#2563EB',
                                                        color: 'white',
                                                        display: 'flex',
                                                        alignItems: 'center',
                                                        justifyContent: 'center',
                                                        fontSize: 10,
                                                        fontWeight: 600,
                                                        title: member,
                                                    }}
                                                >
                                                    {getInitials(member)}
                                                </div>
                                            ))}
                                            {team.members.length > 4 && (
                                                <div
                                                    style={{
                                                        width: 28,
                                                        height: 28,
                                                        borderRadius: 50,
                                                        backgroundColor: '#E2E8F0',
                                                        color: '#64748B',
                                                        display: 'flex',
                                                        alignItems: 'center',
                                                        justifyContent: 'center',
                                                        fontSize: 10,
                                                        fontWeight: 600,
                                                    }}
                                                >
                                                    +{team.members.length - 4}
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                )}

                                {team.resolution && (
                                    <div style={{ marginTop: 12, paddingTop: 12, borderTop: '1px solid #E2E8F0' }}>
                                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 12 }}>
                                            <span style={{ color: '#64748B' }}>Resolution Rate:</span>
                                            <span style={{ fontWeight: 600, color: '#1E293B' }}>{team.resolution}%</span>
                                        </div>
                                    </div>
                                )}
                            </div>
                        </div>
                    );
                })}
            </div>

            {/* Team Summary Table */}
            <div className="card">
                <div className="card-header">
                    <h3>Team Summary</h3>
                </div>
                {filtered.length === 0 ? (
                    <div className="empty-state">
                        <h3>No teams found</h3>
                        <p>No teams match your filter criteria.</p>
                    </div>
                ) : (
                    <div style={{ overflowX: 'auto' }}>
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th style={{ width: 150, textAlign: 'center' }}>Team</th>
                                    <th style={{ width: 100, textAlign: 'center' }}>Size</th>
                                    <th style={{ width: 120, textAlign: 'center' }}>Assigned</th>
                                    <th style={{ width: 120, textAlign: 'center' }}>Available</th>
                                    <th style={{ width: 120, textAlign: 'center' }}>Capacity %</th>
                                    <th style={{ width: 140, textAlign: 'center' }}>Status</th>
                                    <th style={{ width: 100, textAlign: 'center' }}>Health</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filtered.map((team, idx) => {
                                    const assigned = team.assignedTickets || 0;
                                    const available = team.availableSlots || 0;
                                    const total = assigned + available;
                                    const capacity = total > 0 ? ((assigned / total) * 100).toFixed(0) : 0;
                                    return (
                                        <tr key={idx}>
                                            <td style={{ width: 150, textAlign: 'center', paddingLeft: 16, fontWeight: 600 }}>{team.team || team.name}</td>
                                            <td style={{ width: 100, textAlign: 'center' }}>{team.teamSize || 0}</td>
                                            <td style={{ width: 120, textAlign: 'center', fontWeight: 600 }}>{assigned}</td>
                                            <td style={{ width: 120, textAlign: 'center', color: '#059669' }}>{available}</td>
                                            <td style={{ width: 120, textAlign: 'center' }}>
                                                <div className="progress-bar" style={{ minWidth: 40 }}>
                                                    <div className={`progress-fill ${getCapacityStatus(assigned, available)}`} style={{ width: `${Math.min(capacity, 100)}%` }}></div>
                                                </div>
                                                {capacity}%
                                            </td>
                                            <td style={{ width: 140, textAlign: 'center' }}><span className={`badge ${getCapacityStatus(assigned, available)}`}>{capacity >= 90 ? 'High Load' : capacity >= 70 ? 'Moderate' : 'Normal'}</span></td>
                                            <td style={{ width: 100, textAlign: 'center' }}>
                                                <span className={`badge ${team.resolution >= 80 ? 'badge-excellent' : team.resolution >= 60 ? 'badge-good' : 'badge-fair'}`}>
                                                    {team.resolution >= 80 ? 'Excellent' : team.resolution >= 60 ? 'Good' : 'Fair'}
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
