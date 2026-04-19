import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout/Layout';
import { showToast, getUserFriendlyMessage } from '../../utils/toastConfig';
import { isLoggedIn } from '../../utils/auth';

export default function ResponseTime() {
    const navigate = useNavigate();
    const [refreshing, setRefreshing] = useState(false);
    const [responseData, setResponseData] = useState(null);
    const [categories, setCategories] = useState([]);
    const [filterTab, setFilterTab] = useState('all');
    const [searchQuery, setSearchQuery] = useState('');
    const [sortBy, setSortBy] = useState('avgResponse');

    const loadResponseTime = useCallback(async () => {
        if (!isLoggedIn()) { navigate('/login', { replace: true }); return; }
        try {
            const token = sessionStorage.getItem('sessionToken');
            const response = await fetch('/api/management/dashboard/response-time', {
                headers: { 'X-Session-Token': token },
            });
            if (!response.ok) {
                throw new Error(`Response Time API error: ${response.status}`);
            }
            const data = await response.json();
            console.log('Response time data:', data);

            // Extract data - backend returns ApiResponse with data wrapper
            const rtData = data.data || data;
            setResponseData(rtData);

            // Convert byPriority data to category format for display
            let catList = [];
            if (rtData.byPriority && typeof rtData.byPriority === 'object') {
                catList = Object.entries(rtData.byPriority).map(([priority, metrics]) => ({
                    category: priority || 'GENERAL',
                    name: priority || 'GENERAL',
                    avgFirstResponse: metrics.avgFirstResponse || 0,
                    avgResolution: metrics.avgResolution || 0,
                    fastestResponse: metrics.fastestResponse || 0,
                    slowestResponse: metrics.slowestResponse || 0
                }));
            }

            if (!Array.isArray(catList)) catList = [];
            setCategories(catList);

            setRefreshing(false);
        } catch (error) {
            showToast.error(getUserFriendlyMessage('Failed to load response time data'));
            setResponseData({});
            setCategories([]);
            setRefreshing(false);
        }
    }, [navigate]);

    const handleRefresh = async () => {
        setRefreshing(true);
        await loadResponseTime();
    };

    useEffect(() => { loadResponseTime(); }, [loadResponseTime]);


    // Calculate response time tab counts
    const criticalCount = categories.filter(c => (c.avgFirstResponse || 0) <= 120).length;
    const highCount = categories.filter(c => {
        const avg = c.avgFirstResponse || 0;
        return avg > 120 && avg <= 480;
    }).length;
    const mediumCount = categories.filter(c => {
        const avg = c.avgFirstResponse || 0;
        return avg > 480 && avg <= 1440;
    }).length;
    const lowCount = categories.filter(c => (c.avgFirstResponse || 0) > 1440).length;

    const rtData = responseData || {};
    const avgFirstResponse = rtData.avgFirstResponse || rtData.avgFirstResponseTime || 0;
    const avgResolution = rtData.avgResolution || rtData.avgResolutionTime || 0;
    const fastestResponse = rtData.fastestResponse || rtData.minResponseTime || 0;
    const slowestResponse = rtData.slowestResponse || rtData.maxResponseTime || 0;
    const byPriority = rtData.byPriority || {};

    // Filter categories by response time
    let filtered = categories;

    // Apply response time tab filter
    if (filterTab === 'critical') {
        filtered = filtered.filter(c => (c.avgFirstResponse || 0) <= 120);
    } else if (filterTab === 'high') {
        filtered = filtered.filter(c => {
            const avg = c.avgFirstResponse || 0;
            return avg > 120 && avg <= 480;
        });
    } else if (filterTab === 'medium') {
        filtered = filtered.filter(c => {
            const avg = c.avgFirstResponse || 0;
            return avg > 480 && avg <= 1440;
        });
    } else if (filterTab === 'low') {
        filtered = filtered.filter(c => (c.avgFirstResponse || 0) > 1440);
    }

    if (searchQuery) {
        const q = searchQuery.toLowerCase();
        filtered = filtered.filter(c =>
            (c.category && c.category.toLowerCase().includes(q)) ||
            (c.name && c.name.toLowerCase().includes(q))
        );
    }

    // Sort
    if (sortBy === 'avgResponse') {
        filtered = [...filtered].sort((a, b) => (a.avgFirstResponse || 0) - (b.avgFirstResponse || 0));
    } else if (sortBy === 'avgResolution') {
        filtered = [...filtered].sort((a, b) => (a.avgResolution || 0) - (b.avgResolution || 0));
    } else if (sortBy === 'fastest') {
        filtered = [...filtered].sort((a, b) => (a.fastestResponse || 0) - (b.fastestResponse || 0));
    } else if (sortBy === 'slowest') {
        filtered = [...filtered].sort((a, b) => (b.slowestResponse || 0) - (a.slowestResponse || 0));
    }

    const formatTime = (minutes) => {
        if (!minutes || minutes === 0) return 'N/A';
        const mins = Math.round(minutes);
        if (mins < 60) return `${mins}m`;
        const hours = Math.floor(mins / 60);
        const remainingMins = mins % 60;
        return `${hours}h ${remainingMins}m`;
    };

    const getResponseBadge = (responseTime) => {
        const mins = responseTime || 0;
        if (mins <= 30) return 'badge-excellent';
        if (mins <= 60) return 'badge-good';
        if (mins <= 120) return 'badge-fair';
        return 'badge-poor';
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
                    <h1>Response & Resolution Time Analysis</h1>
                    <p>Monitor first response time, resolution time, and SLA compliance.</p>
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
                    <div className="stat-label">Avg First Response</div>
                    <div className="stat-value" style={{ fontSize: 28 }}>{formatTime(avgFirstResponse)}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Avg Resolution</div>
                    <div className="stat-value" style={{ fontSize: 28 }}>{formatTime(avgResolution)}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Fastest Response</div>
                    <div className="stat-value" style={{ fontSize: 28 }}>{formatTime(fastestResponse)}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Slowest Response</div>
                    <div className="stat-value" style={{ fontSize: 28 }}>{formatTime(slowestResponse)}</div>
                </div>
            </div>

            {/* Response Time by Priority */}
            <div className="card" style={{ marginBottom: 24 }}>
                <div className="card-header">
                    <h2>Response Time by Priority</h2>
                </div>
                <div style={{ padding: '20px' }}>
                    <div style={{ display: 'grid', gap: 16 }}>
                        {Object.entries(byPriority).map(([priority, times]) => (
                            <div key={priority} style={{ padding: '16px', background: '#F9FAFB', borderRadius: '8px', border: '1px solid #E5E7EB' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
                                    <span style={{ fontWeight: 600, textTransform: 'uppercase', fontSize: 13, color: '#1E293B', letterSpacing: '0.3px' }}>{priority}</span>
                                    <div style={{ fontSize: 12, color: '#64748B', display: 'flex', gap: 24 }}>
                                        <span>Response: <strong style={{ color: '#1E293B' }}>{formatTime(times.avgFirstResponse || times.response || 0)}</strong></span>
                                        <span>Resolution: <strong style={{ color: '#1E293B' }}>{formatTime(times.avgResolution || times.resolution || 0)}</strong></span>
                                    </div>
                                </div>
                                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
                                    <div>
                                        <div style={{ fontSize: 12, color: '#64748B', marginBottom: 8, fontWeight: 500 }}>First Response Time</div>
                                        <div className="progress-bar">
                                            <div className={`progress-fill ${getResponseBadge(times.avgFirstResponse || times.response || 0)}`} style={{ width: `${Math.min((times.avgFirstResponse || times.response || 0) / (slowestResponse / 2), 100)}%` }}></div>
                                        </div>
                                    </div>
                                    <div>
                                        <div style={{ fontSize: 12, color: '#64748B', marginBottom: 8, fontWeight: 500 }}>Resolution Time</div>
                                        <div className="progress-bar">
                                            <div className={`progress-fill success`} style={{ width: `${Math.min((times.avgResolution || times.resolution || 0) / (avgResolution * 1.5 || 240), 100)}%` }}></div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>

            {/* Filters */}
            <div className="filters-bar" style={{ marginBottom: 24 }}>
                <div className="filter-tabs">
                    <button className={`filter-tab ${filterTab === 'all' ? 'active' : ''}`} onClick={() => setFilterTab('all')}>
                        All <span className="tab-count">{categories.length}</span>
                    </button>
                    <button className={`filter-tab ${filterTab === 'critical' ? 'active' : ''}`} onClick={() => setFilterTab('critical')}>
                        Critical (&lt;2h) <span className="tab-count">{criticalCount}</span>
                    </button>
                    <button className={`filter-tab ${filterTab === 'high' ? 'active' : ''}`} onClick={() => setFilterTab('high')}>
                        High (2-8h) <span className="tab-count">{highCount}</span>
                    </button>
                    <button className={`filter-tab ${filterTab === 'medium' ? 'active' : ''}`} onClick={() => setFilterTab('medium')}>
                        Medium (8-24h) <span className="tab-count">{mediumCount}</span>
                    </button>
                    <button className={`filter-tab ${filterTab === 'low' ? 'active' : ''}`} onClick={() => setFilterTab('low')}>
                        Low (&gt;24h) <span className="tab-count">{lowCount}</span>
                    </button>
                </div>
                <div className="filter-actions">
                    <select
                        value={sortBy}
                        onChange={(e) => setSortBy(e.target.value)}
                        className="search-input"
                        style={{ width: 200 }}
                    >
                        <option value="avgResponse">Sort by Avg Response</option>
                        <option value="avgResolution">Sort by Avg Resolution</option>
                        <option value="fastest">Sort by Fastest</option>
                        <option value="slowest">Sort by Slowest</option>
                    </select>
                    <input
                        type="text"
                        className="search-input"
                        placeholder="Search tickets..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        style={{ width: 280 }}
                    />
                </div>
            </div>

            {/* Category Breakdown */}
            <div className="card" style={{ marginBottom: 24 }}>
                <div className="card-header">
                    <h2>Response Time by Category</h2>
                    <span className="badge badge-neutral">{filtered.length} categories</span>
                </div>
                {filtered.length === 0 ? (
                    <div className="empty-state">
                        <h3>No data available</h3>
                        <p>No categories match your filters.</p>
                    </div>
                ) : (
                    <div style={{ padding: '20px' }}>
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th style={{ width: 160, textAlign: 'left', paddingLeft: 16 }}>Category</th>
                                    <th style={{ width: 110, textAlign: 'center' }}>Priority</th>
                                    <th style={{ width: 150, textAlign: 'center' }}>Avg First Response</th>
                                    <th style={{ width: 150, textAlign: 'center' }}>Avg Resolution</th>
                                    <th style={{ width: 130, textAlign: 'center' }}>Fastest Response</th>
                                    <th style={{ width: 130, textAlign: 'center' }}>Slowest Response</th>
                                    <th style={{ width: 100, textAlign: 'center' }}>Tickets</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filtered.map((cat, idx) => (
                                    <tr key={idx}>
                                        <td style={{ width: 160, textAlign: 'left', paddingLeft: 16, color: '#1E293B', fontWeight: 500 }}>{cat.category || cat.name || 'Unknown'}</td>
                                        <td style={{ width: 110, textAlign: 'center' }}><span className={`badge badge-${(cat.priority || 'medium').toLowerCase()}`}>{cat.priority || 'MEDIUM'}</span></td>
                                        <td style={{ width: 150, textAlign: 'center' }}>
                                            <span className={`badge ${getResponseBadge(cat.avgFirstResponse || cat.response || 0)}`}>
                                                {formatTime(cat.avgFirstResponse || cat.response || 0)}
                                            </span>
                                        </td>
                                        <td style={{ width: 150, textAlign: 'center', color: '#1E293B', fontWeight: 500 }}>{formatTime(cat.avgResolution || cat.resolution || 0)}</td>
                                        <td style={{ width: 130, textAlign: 'center', color: '#1E293B', fontWeight: 500 }}>{formatTime(cat.fastestResponse || cat.fastest || 0)}</td>
                                        <td style={{ width: 130, textAlign: 'center', color: '#1E293B', fontWeight: 500 }}>{formatTime(cat.slowestResponse || cat.slowest || 0)}</td>
                                        <td style={{ width: 100, textAlign: 'center', fontWeight: 600, color: '#1E293B' }}>{cat.ticketCount || cat.count || 0}</td>
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
