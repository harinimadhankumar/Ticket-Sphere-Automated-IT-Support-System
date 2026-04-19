import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout/Layout';
import { fetchManagementSLA } from '../../utils/api';
import { showToast, getUserFriendlyMessage } from '../../utils/toastConfig';
import { isLoggedIn } from '../../utils/auth';

export default function SLAAnalysis() {
    const navigate = useNavigate();
    const [refreshing, setRefreshing] = useState(false);
    const [slaData, setSlaData] = useState(null);
    const [categories, setCategories] = useState([]);
    const [filterTab, setFilterTab] = useState('all');
    const [searchQuery, setSearchQuery] = useState('');

    const loadSLA = useCallback(async () => {
        if (!isLoggedIn()) { navigate('/login', { replace: true }); return; }
        try {
            const response = await fetchManagementSLA();
            console.log('SLA response:', response);

            // Backend returns: { success, data: { totalTickets, slaMet, slaBreached, complianceRate, byPriority, breachByCategory } }
            const slaDataObj = response?.data || {};
            console.log('Extracted SLA data:', slaDataObj);
            setSlaData(slaDataObj);

            // Extract and populate categories from breach data
            let categoriesData = slaDataObj?.breachByCategory || [];
            console.log('Categories from backend:', categoriesData);

            if (Array.isArray(categoriesData)) {
                // Ensure each category has the required fields
                const validatedCategories = categoriesData.map(cat => ({
                    category: cat.category || cat.name || 'Unknown',
                    total: cat.total || cat.count || 0,
                    breachedCount: cat.breachedCount || cat.breaches || 0,
                    count: cat.count || cat.total || 0
                }));
                console.log('Validated categories:', validatedCategories);
                setCategories(validatedCategories);
            } else {
                console.log('Categories not an array or empty');
                setCategories([]);
            }

            setRefreshing(false);
        } catch (error) {
            showToast.error(getUserFriendlyMessage('Failed to load SLA data'));
            setSlaData({});
            setCategories([]);
            setRefreshing(false);
        }
    }, [navigate]);

    const handleRefresh = async () => {
        setRefreshing(true);
        await loadSLA();
    };

    useEffect(() => { loadSLA(); }, [loadSLA]);


    // Calculate SLA status tab counts
    const metCount = categories.filter(c => (c.compRate || 100) >= 90).length;
    const atRiskCountTabs = categories.filter(c => {
        const comp = c.compRate || 100;
        return comp >= 70 && comp < 90;
    }).length;
    const breachedCount = categories.filter(c => (c.compRate || 100) < 70).length;

    const totalTickets = slaData?.totalTickets || 0;
    const slaMet = slaData?.slaMet || 0;
    const slaBreached = slaData?.slaBreached || 0;
    const atRisk = slaData?.atRisk || 0;
    const complianceRate = slaData?.complianceRate || 0;
    const byPriority = slaData?.byPriority || {};

    // Apply category filtering
    let filteredCategories = categories;
    if (filterTab === 'met') {
        filteredCategories = filteredCategories.filter(c => (c.compRate || 100) >= 90);
    } else if (filterTab === 'atrisk') {
        filteredCategories = filteredCategories.filter(c => {
            const comp = c.compRate || 100;
            return comp >= 70 && comp < 90;
        });
    } else if (filterTab === 'breached') {
        filteredCategories = filteredCategories.filter(c => (c.compRate || 100) < 70);
    }

    if (searchQuery) {
        const q = searchQuery.toLowerCase();
        filteredCategories = filteredCategories.filter(c =>
            (c.category && c.category.toLowerCase().includes(q)) ||
            (c.name && c.name.toLowerCase().includes(q))
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
                    <h1>SLA Compliance Analysis</h1>
                    <p>Comprehensive SLA metrics, breach analysis, and compliance trends.</p>
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
                    <div className="stat-label">SLA Compliance</div>
                    <div className="stat-value">
                        {complianceRate}%
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">SLA Met</div>
                    <div className="stat-value">{slaMet}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">SLA Breached</div>
                    <div className="stat-value">{slaBreached}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">At Risk</div>
                    <div className="stat-value">{atRisk}</div>
                </div>
            </div>

            {/* SLA Overview */}
            <div className="grid-2">
                {/* SLA Status Breakdown */}
                <div className="card">
                    <div className="card-header">
                        <h3>Tickets by SLA Status</h3>
                    </div>
                    <div className="card-body">
                        <div style={{ display: 'grid', gap: 16 }}>
                            {/* SLA Met */}
                            <div style={{ padding: '12px', background: '#F9FAFB', borderRadius: '8px' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                                    <span style={{ fontWeight: 600, color: '#059669' }}>SLA Met</span>
                                    <span style={{ fontWeight: 600, fontSize: 16, color: '#059669' }}>{slaMet}</span>
                                </div>
                                <div className="progress-bar">
                                    <div className="progress-fill success" style={{ width: totalTickets > 0 ? `${(slaMet / totalTickets) * 100}%` : '0%' }}></div>
                                </div>
                                <div style={{ fontSize: 12, color: '#6B7280', marginTop: 4 }}>
                                    {totalTickets > 0 ? `${((slaMet / totalTickets) * 100).toFixed(1)}%` : '0%'} of total
                                </div>
                            </div>

                            {/* At Risk */}
                            <div style={{ padding: '12px', background: '#F9FAFB', borderRadius: '8px' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                                    <span style={{ fontWeight: 600, color: '#D97706' }}>At Risk</span>
                                    <span style={{ fontWeight: 600, fontSize: 16, color: '#D97706' }}>{atRisk}</span>
                                </div>
                                <div className="progress-bar">
                                    <div className="progress-fill warning" style={{ width: totalTickets > 0 ? `${(atRisk / totalTickets) * 100}%` : '0%' }}></div>
                                </div>
                                <div style={{ fontSize: 12, color: '#6B7280', marginTop: 4 }}>
                                    {totalTickets > 0 ? `${((atRisk / totalTickets) * 100).toFixed(1)}%` : '0%'} of total
                                </div>
                            </div>

                            {/* Breached */}
                            <div style={{ padding: '12px', background: '#F9FAFB', borderRadius: '8px' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                                    <span style={{ fontWeight: 600, color: '#DC2626' }}>Breached</span>
                                    <span style={{ fontWeight: 600, fontSize: 16, color: '#DC2626' }}>{slaBreached}</span>
                                </div>
                                <div className="progress-bar">
                                    <div className="progress-fill danger" style={{ width: totalTickets > 0 ? `${(slaBreached / totalTickets) * 100}%` : '0%' }}></div>
                                </div>
                                <div style={{ fontSize: 12, color: '#6B7280', marginTop: 4 }}>
                                    {totalTickets > 0 ? `${((slaBreached / totalTickets) * 100).toFixed(1)}%` : '0%'} of total
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Priority Breakdown */}
                <div className="card">
                    <div className="card-header">
                        <h3>SLA Metrics by Priority</h3>
                    </div>
                    <div className="card-body">
                        {Object.entries(byPriority).length > 0 ? (
                            <div style={{ display: 'grid', gap: 12 }}>
                                {Object.entries(byPriority).map(([priority, count], idx) => (
                                    <div key={idx} style={{ padding: '12px', background: '#F9FAFB', borderRadius: 8 }}>
                                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                                            <span style={{ fontWeight: 600, fontSize: 14 }}>
                                                {priority}
                                            </span>
                                            <span style={{ fontWeight: 600, color: '#1E293B' }}>{count} tickets</span>
                                        </div>
                                        <div style={{ fontSize: 12, color: '#6B7280' }}>
                                            {totalTickets > 0 ? `${((count / totalTickets) * 100).toFixed(1)}%` : '0%'} of total
                                        </div>
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <div style={{ textAlign: 'center', color: '#9CA3AF', padding: '20px' }}>
                                No priority breakdown available
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* Filters */}
            <div className="filters-bar" style={{ marginTop: 24 }}>
                <div className="filter-tabs">
                    <button className={`filter-tab ${filterTab === 'all' ? 'active' : ''}`} onClick={() => setFilterTab('all')}>
                        All <span className="tab-count">{categories.length}</span>
                    </button>
                    <button className={`filter-tab ${filterTab === 'met' ? 'active' : ''}`} onClick={() => setFilterTab('met')}>
                        SLA Met (≥90%) <span className="tab-count">{metCount}</span>
                    </button>
                    <button className={`filter-tab ${filterTab === 'atrisk' ? 'active' : ''}`} onClick={() => setFilterTab('atrisk')}>
                        At Risk (70-89%) <span className="tab-count">{atRiskCountTabs}</span>
                    </button>
                    <button className={`filter-tab ${filterTab === 'breached' ? 'active' : ''}`} onClick={() => setFilterTab('breached')}>
                        Breached (&lt;70%) <span className="tab-count">{breachedCount}</span>
                    </button>
                </div>
                <div className="filter-actions">
                    <input
                        type="text"
                        className="search-input"
                        placeholder="Search categories..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        style={{ width: 280 }}
                    />
                </div>
            </div>

            {/* SLA Breaches by Category */}
            <div className="card">
                <div className="card-header">
                    <h3>SLA Breaches by Category</h3>
                    <span className="badge badge-neutral">{filteredCategories.length} categories</span>
                </div>
                {filteredCategories.length === 0 ? (
                    <div className="card-body">
                        <div style={{ textAlign: 'center', color: '#9CA3AF', padding: '30px' }}>
                            No category data available
                        </div>
                    </div>
                ) : (
                    <div style={{ overflowX: 'auto' }}>
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th style={{ width: 180, textAlign: 'center' }}>Category</th>
                                    <th style={{ width: 85, textAlign: 'center' }}>Total</th>
                                    <th style={{ width: 85, textAlign: 'center' }}>Breaches</th>
                                    <th style={{ width: 120, textAlign: 'center' }}>Breach %</th>
                                    <th style={{ width: 120, textAlign: 'center' }}>Compliance %</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredCategories.map((cat, idx) => {
                                    const catTotal = cat.total || cat.count || 0;
                                    const breachCount = cat.breachedCount || cat.breaches || 0;
                                    const breachRate = catTotal > 0 ? ((breachCount / catTotal) * 100).toFixed(1) : 0;
                                    const compRate = 100 - parseFloat(breachRate);

                                    return (
                                        <tr key={idx}>
                                            <td style={{ width: 180, textAlign: 'center' }}><span className="category-tag">{cat.category || cat.name || 'Unknown'}</span></td>
                                            <td style={{ width: 85, textAlign: 'center', fontWeight: 500 }}>{catTotal}</td>
                                            <td style={{ width: 85, textAlign: 'center', color: breachCount > 0 ? 'var(--danger)' : undefined, fontWeight: 600 }}>{breachCount}</td>
                                            <td style={{ width: 120, textAlign: 'center' }}>
                                                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8 }}>
                                                    <div className="progress-bar" style={{ width: 80 }}>
                                                        <div className={`progress-fill ${breachRate > 30 ? 'danger' : breachRate > 15 ? 'warning' : 'success'}`} style={{ width: `${Math.min(breachRate, 100)}%` }}></div>
                                                    </div>
                                                    <span style={{ minWidth: 35 }}>{breachRate}%</span>
                                                </div>
                                            </td>
                                            <td style={{ width: 120, textAlign: 'center' }}><span className={`badge ${compRate >= 90 ? 'badge-excellent' : compRate >= 80 ? 'badge-good' : compRate >= 70 ? 'badge-fair' : 'badge-poor'}`}>{compRate.toFixed(1)}%</span></td>
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
