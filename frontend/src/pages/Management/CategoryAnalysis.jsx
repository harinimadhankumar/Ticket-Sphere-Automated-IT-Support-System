import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout/Layout';
import { fetchManagementCategories } from '../../utils/api';
import { showToast, getUserFriendlyMessage } from '../../utils/toastConfig';
import { isLoggedIn } from '../../utils/auth';

export default function CategoryAnalysis() {
    const navigate = useNavigate();
    const [refreshing, setRefreshing] = useState(false);
    const [categories, setCategories] = useState([]);
    const [searchQuery, setSearchQuery] = useState('');
    const [filterTab, setFilterTab] = useState('all');
    const [sortBy, setSortBy] = useState('volume');

    const loadCategories = useCallback(async () => {
        if (!isLoggedIn()) { navigate('/login', { replace: true }); return; }
        try {
            const data = await fetchManagementCategories();
            console.log('Category analysis data:', data);

            // Backend returns: { success, data: { byCategory: { CAT: count, ... } } }
            const categoriesData = data?.data?.byCategory || data?.data?.categories || {};

            // Transform to array format expected by frontend
            let catList = Object.entries(categoriesData).map(([category, count]) => ({
                category: category || 'GENERAL',
                name: category || 'GENERAL',
                count: count || 0,
                total: count || 0,
                resolvedCount: 0,
                breachedCount: 0,
                slaCompliance: 0,
            }));

            if (!Array.isArray(catList)) catList = [];

            console.log('Transformed categories:', catList);
            setCategories(catList);
            setRefreshing(false);
        } catch (error) {
            showToast.error(getUserFriendlyMessage('Failed to load category data'));
            setRefreshing(false);
        }
    }, [navigate]);

    const handleRefresh = async () => {
        setRefreshing(true);
        await loadCategories();
    };

    useEffect(() => { loadCategories(); }, [loadCategories]);

    // Calculate total tickets first
    const totalTickets = categories.reduce((sum, c) => sum + (c.count || 0), 0);
    const totalBreach = categories.reduce((sum, c) => sum + (c.breachedCount || 0), 0);
    const totalResolved = categories.reduce((sum, c) => sum + (c.resolvedCount || 0), 0);

    // Filter and sort
    let filtered = categories;

    // Apply tab filter based on ticket volume
    if (filterTab === 'high') {
        const avgVolume = totalTickets > 0 ? totalTickets / Math.max(categories.length, 1) : 0;
        filtered = filtered.filter(c => (c.count || 0) > avgVolume * 1.5);
    } else if (filterTab === 'medium') {
        const avgVolume = totalTickets > 0 ? totalTickets / Math.max(categories.length, 1) : 0;
        filtered = filtered.filter(c => {
            const count = c.count || 0;
            return count <= avgVolume * 1.5 && count >= avgVolume * 0.5;
        });
    } else if (filterTab === 'low') {
        const avgVolume = totalTickets > 0 ? totalTickets / Math.max(categories.length, 1) : 0;
        filtered = filtered.filter(c => (c.count || 0) < avgVolume * 0.5);
    }

    if (searchQuery) {
        const q = searchQuery.toLowerCase();
        filtered = filtered.filter(c =>
            (c.category && c.category.toLowerCase().includes(q)) ||
            (c.name && c.name.toLowerCase().includes(q))
        );
    }

    // Sort
    if (sortBy === 'volume') {
        filtered = [...filtered].sort((a, b) => (b.count || 0) - (a.count || 0));
    } else if (sortBy === 'breaches') {
        filtered = [...filtered].sort((a, b) => (b.breachedCount || 0) - (a.breachedCount || 0));
    } else if (sortBy === 'resolved') {
        filtered = [...filtered].sort((a, b) => (b.resolvedCount || 0) - (a.resolvedCount || 0));
    } else if (sortBy === 'name') {
        filtered = [...filtered].sort((a, b) => (a.category || a.name || '').localeCompare(b.category || b.name || ''));
    }


    // Calculate category volume counts
    const avgVolume = totalTickets > 0 ? totalTickets / Math.max(categories.length, 1) : 0;
    const highCount = categories.filter(c => (c.count || 0) > avgVolume * 1.5).length;
    const mediumCount = categories.filter(c => {
        const count = c.count || 0;
        return count <= avgVolume * 1.5 && count >= avgVolume * 0.5;
    }).length;
    const lowCount = categories.filter(c => (c.count || 0) < avgVolume * 0.5).length;

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
                    <h1>Category Analysis</h1>
                    <p>Detailed breakdown of ticket volume, resolution, and SLA compliance by category.</p>
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
                    <div className="stat-label">Total Categories</div>
                    <div className="stat-value">{categories.length}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Total Tickets</div>
                    <div className="stat-value">{totalTickets}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">SLA Breaches</div>
                    <div className="stat-value">
                        {totalBreach}
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Resolved</div>
                    <div className="stat-value">{totalResolved}</div>
                </div>
            </div>

            {/* Filters */}
            <div className="filters-bar">
                <div className="filter-tabs">
                    <button
                        className={`filter-tab ${filterTab === 'all' ? 'active' : ''}`}
                        onClick={() => setFilterTab('all')}
                    >
                        All <span className="tab-count">{categories.length}</span>
                    </button>
                    <button
                        className={`filter-tab ${filterTab === 'high' ? 'active' : ''}`}
                        onClick={() => setFilterTab('high')}
                    >
                        High Volume <span className="tab-count">{highCount}</span>
                    </button>
                    <button
                        className={`filter-tab ${filterTab === 'medium' ? 'active' : ''}`}
                        onClick={() => setFilterTab('medium')}
                    >
                        Medium <span className="tab-count">{mediumCount}</span>
                    </button>
                    <button
                        className={`filter-tab ${filterTab === 'low' ? 'active' : ''}`}
                        onClick={() => setFilterTab('low')}
                    >
                        Low Volume <span className="tab-count">{lowCount}</span>
                    </button>
                </div>
                <div className="filter-actions">
                    <select
                        value={sortBy}
                        onChange={(e) => setSortBy(e.target.value)}
                        className="search-input"
                        style={{ width: 200 }}
                    >
                        <option value="volume">Sort by Volume</option>
                        <option value="breaches">Sort by Breaches</option>
                        <option value="resolved">Sort by Resolved</option>
                        <option value="name">Sort by Name</option>
                    </select>
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

            {/* Categories Table */}
            <div className="card">
                <div className="card-header">
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <h2 style={{ color: '#1E293B' }}>Category Details <span style={{ background: 'linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%)', color: 'white', padding: '3px 10px', borderRadius: 12, fontSize: 12, marginLeft: 8 }}>{filtered.length}</span></h2>
                    </div>
                </div>
                {filtered.length === 0 ? (
                    <div className="empty-state">
                        <h3>No categories found</h3>
                        <p>No categories match your search criteria.</p>
                    </div>
                ) : (
                    <div style={{ overflowX: 'auto' }}>
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th style={{ width: 60, textAlign: 'center' }}>S.NO</th>
                                    <th style={{ width: 150, textAlign: 'center' }}>Category</th>
                                    <th style={{ width: 85, textAlign: 'center' }}>Total</th>
                                    <th style={{ width: 85, textAlign: 'center' }}>Resolved</th>
                                    <th style={{ width: 85, textAlign: 'center' }}>Pending</th>
                                    <th style={{ width: 85, textAlign: 'center' }}>Breached</th>
                                    <th style={{ width: 120, textAlign: 'center' }}>Breach Rate</th>
                                    <th style={{ width: 100, textAlign: 'center' }}>SLA %</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filtered.map((category, idx) => {
                                    const catTotal = category.count || 0;
                                    const catRes = category.resolvedCount || 0;
                                    const catPend = catTotal - catRes;
                                    const catBreach = category.breachedCount || 0;
                                    const breachRate = catTotal > 0 ? ((catBreach / catTotal) * 100).toFixed(1) : 0;
                                    const slaPct = category.slaCompliance || (catTotal > 0 ? (((catTotal - catBreach) / catTotal) * 100).toFixed(1) : 0);

                                    return (
                                        <tr key={idx}>
                                            <td style={{ width: 60, textAlign: 'center', fontWeight: 500 }}>{idx + 1}</td>
                                            <td style={{ width: 150, textAlign: 'center' }}><span className="category-tag">{category.category || category.name || 'Unknown'}</span></td>
                                            <td style={{ width: 85, textAlign: 'center', fontWeight: 600 }}>{catTotal}</td>
                                            <td style={{ width: 85, textAlign: 'center', color: 'var(--success)', fontWeight: 500 }}>{catRes}</td>
                                            <td style={{ width: 85, textAlign: 'center', color: catPend > 0 ? 'var(--warning)' : undefined, fontWeight: 500 }}>{catPend}</td>
                                            <td style={{ width: 85, textAlign: 'center', color: catBreach > 0 ? 'var(--danger)' : undefined, fontWeight: 600 }}>{catBreach}</td>
                                            <td style={{ width: 120, textAlign: 'center' }}>
                                                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8 }}>
                                                    <div className="progress-bar" style={{ width: 80 }}>
                                                        <div className={`progress-fill ${breachRate > 30 ? 'danger' : breachRate > 15 ? 'warning' : 'success'}`} style={{ width: `${Math.min(breachRate, 100)}%` }}></div>
                                                    </div>
                                                    <span style={{ minWidth: 35 }}>{breachRate}%</span>
                                                </div>
                                            </td>
                                            <td style={{ width: 100, textAlign: 'center' }}><span className={`badge ${slaPct >= 90 ? 'badge-excellent' : slaPct >= 80 ? 'badge-good' : slaPct >= 70 ? 'badge-fair' : 'badge-poor'}`}>{slaPct}%</span></td>
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
