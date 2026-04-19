import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, ArcElement, PointElement, LineElement, Title, Tooltip, Legend, Filler } from 'chart.js';
import { Bar, Doughnut, Pie, Line } from 'react-chartjs-2';
import Layout from '../../components/Layout/Layout';
import { fetchManagementDepartments, fetchManagementEngineers, fetchManagementCategories, fetchManagementSLA } from '../../utils/api';
import { isLoggedIn, getUserRole, getManagementData, getInitials } from '../../utils/auth';
import { showToast, getUserFriendlyMessage } from '../../utils/toastConfig';

ChartJS.register(CategoryScale, LinearScale, BarElement, ArcElement, PointElement, LineElement, Title, Tooltip, Legend, Filler);

export default function ManagementDashboard() {
    const navigate = useNavigate();
    const [data, setData] = useState(null);
    const [activeTab, setActiveTab] = useState('overview');
    const [period, setPeriod] = useState('week');
    const mgmt = getManagementData();

    const loadDashboard = useCallback(async () => {
        if (!isLoggedIn() || getUserRole() !== 'management') {
            navigate('/login', { replace: true });
            return;
        }
        try {
            // Get department from management data (if Department Head is logged in)
            const deptCode = mgmt?.department || null;

            // Fetch all necessary data from backend with period and department parameters
            const [depRes, engRes, catRes, slaRes] = await Promise.all([
                fetchManagementDepartments(period, deptCode).catch(e => ({ data: {} })),
                fetchManagementEngineers(period, deptCode).catch(e => ({ data: { engineers: [] } })),
                fetchManagementCategories(period, deptCode).catch(e => ({ data: { categories: [] } })),
                fetchManagementSLA(period, deptCode).catch(e => ({ data: {} }))
            ]);

            const summaryData = depRes.data || {};

            // Safely extract arrays from responses
            let engineers = engRes.data?.engineers || engRes.engineers || [];
            if (!Array.isArray(engineers)) engineers = [];

            // Transform engineer fields from backend field names
            engineers = engineers.map(e => {
                const totalTickets = e.totalTickets || 0;
                const slaBreached = e.slaBreached || 0;
                const slaCompliance = totalTickets > 0
                    ? Math.round(((totalTickets - slaBreached) * 100 / totalTickets) * 100) / 100
                    : 100;
                return {
                    name: e.engineer || 'Unknown',
                    resolved: e.resolved || 0,
                    slaCompliance: slaCompliance,
                    inProgressCount: e.inProgress || 0,
                    team: e.team || 'Unknown',
                };
            });

            // Extract categories
            const categoriesData = catRes.data?.byCategory || catRes.data?.categories || catRes.categories || {};
            let categories = Object.entries(categoriesData).map(([category, count]) => ({
                category: category || 'GENERAL',
                name: category || 'GENERAL',
                count: count || 0,
                total: count || 0,
            }));
            if (!Array.isArray(categories)) categories = [];

            const slaData = slaRes.data || {};

            const fullData = {
                summary: summaryData,
                engineers: engineers,
                categories: categories,
                slaData: slaData,
                priorityBreakdown: summaryData.byPriority || {},
                totalTickets: summaryData.totalTickets || 0,
                resolvedCount: summaryData.resolvedTickets || 0,
                slaCompliancePercent: summaryData.slaComplianceRate || 0,
                avgResolutionTimeHours: summaryData.avgResolutionTime || 0,
                slaMetCount: Math.round(((summaryData.slaComplianceRate || 0) / 100) * (summaryData.totalTickets || 0)),
                slaBreachedCount: summaryData.slaBreached || 0,
                atRiskCount: summaryData.atRiskCount || 0,
                trends: summaryData.trends || { labels: [], dates: [], created: [], resolved: [] },
                insights: [
                    { type: 'info', message: `📊 You have ${summaryData.totalTickets || 0} total tickets in your view.` },
                    { type: summaryData.slaComplianceRate > 80 ? 'success' : 'warning', message: `🎯 Current SLA Compliance: ${(summaryData.slaComplianceRate || 0).toFixed(1)}%` }
                ]
            };

            setData(fullData);
        } catch (error) {
            console.error('Dashboard load error:', error);
            showToast.error(getUserFriendlyMessage('Failed to load dashboard data'));
        }
    }, [navigate, period, mgmt?.department]);

    useEffect(() => { loadDashboard(); }, [loadDashboard]);

    if (!data) return <Layout ><div className="empty-state"><h3>No data available</h3></div></Layout>;

    const tabs = [
        { key: 'overview', label: 'Overview' },
        { key: 'sla', label: 'SLA Analysis' },
        { key: 'engineers', label: 'Engineers' },
        { key: 'categories', label: 'Categories' },
        { key: 'trends', label: 'Trends' },
    ];

    // Extract data safely
    const summary = data.summary || data;
    const totalTickets = data.totalTickets || summary.totalTickets || 0;
    const resolvedCount = data.resolvedCount || summary.resolvedCount || summary.resolved || 0;
    const slaPercent = data.slaCompliancePercent ?? summary.slaCompliancePercent ?? summary.slaCompliance ?? summary.slaComplianceRate ?? 0;
    const avgResTime = data.avgResolutionTimeHours ?? summary.avgResolutionTimeHours ?? summary.avgResolutionTime ?? 0;
    const slaMet = data.slaMetCount || summary.slaMetCount || summary.slaMet || 0;
    const slaBreached = data.slaBreachedCount || summary.slaBreachedCount || summary.slaBreached || 0;
    const atRisk = summary.atRiskCount || summary.atRisk || 0;
    const engineers = data.engineers || data.engineerPerformance || [];
    let categories = data.categories || data.categoryBreakdown || [];
    const insights = data.insights || [];
    const trends = data.trends || data.trendData || {};

    // Ensure categories is an array
    if (!Array.isArray(categories)) categories = [];

    // SLA Gauge chart
    const slaGaugeData = {
        datasets: [{
            data: [slaPercent, 100 - slaPercent],
            backgroundColor: [slaPercent >= 90 ? '#059669' : slaPercent >= 70 ? '#D97706' : '#DC2626', '#E2E8F0'],
            borderWidth: 0,
            cutout: '80%',
        }],
    };

    // SLA Breakdown chart
    const slaBreakdownData = {
        labels: ['SLA Met', 'SLA Breached', 'At Risk'],
        datasets: [{
            data: [slaMet, slaBreached, atRisk],
            backgroundColor: ['#059669', '#DC2626', '#D97706'],
            borderWidth: 0,
        }],
    };

    // Priority chart
    const priorityData = data.priorityBreakdown || {};
    const priorityChartData = {
        labels: Object.keys(priorityData).length > 0 ? Object.keys(priorityData) : ['Critical', 'High', 'Medium', 'Low'],
        datasets: [{
            label: 'Tickets',
            data: Object.keys(priorityData).length > 0 ? Object.values(priorityData) : [0, 0, 0, 0],
            backgroundColor: ['#DC2626', '#EA580C', '#D97706', '#2563EB'],
            borderRadius: 6,
        }],
    };

    // Category volume chart
    const categoryChartData = {
        labels: categories.slice(0, 8).map(c => c.category || c.name || 'Unknown'),
        datasets: [{
            label: 'Volume',
            data: categories.slice(0, 8).map(c => c.count || c.total || 0),
            backgroundColor: '#2563EB',
            borderRadius: 6,
        }],
    };

    // Category pie
    const categoryPieData = {
        labels: categories.slice(0, 6).map(c => c.category || c.name || 'Unknown'),
        datasets: [{
            data: categories.slice(0, 6).map(c => c.count || c.total || 0),
            backgroundColor: ['#2563EB', '#059669', '#D97706', '#DC2626', '#7C3AED', '#0891B2'],
            borderWidth: 0,
        }],
    };

    // Trend chart
    const trendLabels = trends.labels || trends.dates || [];
    const trendChartData = {
        labels: trendLabels,
        datasets: [{
            label: 'Tickets Created',
            data: trends.created || trends.values || [],
            borderColor: '#2563EB',
            backgroundColor: 'rgba(37, 99, 235, 0.08)',
            fill: true,
            tension: 0.4,
        }, {
            label: 'Tickets Resolved',
            data: trends.resolved || [],
            borderColor: '#059669',
            backgroundColor: 'rgba(5, 150, 105, 0.08)',
            fill: true,
            tension: 0.4,
        }],
    };

    const chartOptions = { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } } };
    const chartOptionsLegend = { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom' } } };
    const barOptions = { ...chartOptions, scales: { y: { beginAtZero: true, grid: { color: '#F1F5F9' } }, x: { grid: { display: false } } } };

    const getSLABadge = () => {
        if (slaPercent >= 95) return 'badge-excellent';
        if (slaPercent >= 85) return 'badge-good';
        if (slaPercent >= 70) return 'badge-fair';
        return 'badge-poor';
    };

    const getPerformBadge = (rate) => {
        if (rate >= 95) return 'badge-excellent';
        if (rate >= 85) return 'badge-good';
        if (rate >= 70) return 'badge-fair';
        return 'badge-poor';
    };

    return (
        <Layout showNav={true} showUser={true} showSidebar={true} showSecondaryNav={true}>
            {/* Nav Tabs + Time Filter */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
                <div className="filter-tabs">
                    {tabs.map(t => (
                        <button key={t.key} className={`filter-tab ${activeTab === t.key ? 'active' : ''}`} onClick={() => setActiveTab(t.key)}>
                            {t.label}
                        </button>
                    ))}
                </div>
                <div style={{ display: 'flex', gap: 4, background: 'var(--bg-card)', padding: 4, borderRadius: 8, border: '1px solid var(--border)' }}>
                    {['today', 'week', 'month'].map(p => (
                        <button key={p} className={`filter-tab ${period === p ? 'active' : ''}`} style={{ padding: '6px 14px', fontSize: 12 }} onClick={() => setPeriod(p)}>
                            {p === 'today' ? 'Today' : p === 'week' ? '7 Days' : '30 Days'}
                        </button>
                    ))}
                </div>
            </div>

            {/* Department Label */}
            {mgmt.departmentDisplayName && (
                <div className="dept-label">{mgmt.departmentDisplayName || mgmt.department || 'All Departments'}</div>
            )}

            {/* Summary Cards */}
            <div className="stats-grid">
                <div className="stat-card">
                    <div className="stat-label">Total Tickets</div>
                    <div className="stat-value">{totalTickets}</div>
                    <div className="stat-sub">{period === 'today' ? 'today' : period === 'week' ? 'last 7 days' : 'last 30 days'}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">SLA Compliance</div>
                    <div className="stat-value">{slaPercent}%</div>
                    <div className="stat-sub">{slaMet} met / {slaBreached} breached</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Resolved</div>
                    <div className="stat-value">{resolvedCount}</div>
                    <div className="stat-sub">tickets resolved</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Avg Resolution Time</div>
                    <div className="stat-value">{typeof avgResTime === 'number' ? avgResTime.toFixed(1) : avgResTime}</div>
                    <div className="stat-sub">hours average</div>
                </div>
            </div>

            {/* =================== OVERVIEW TAB =================== */}
            {activeTab === 'overview' && (
                <div>
                    <div className="grid-3">
                        {/* SLA Gauge */}
                        <div className="card">
                            <div className="card-header">
                                <h3>SLA Compliance</h3>
                                <span className={`badge ${getSLABadge()}`}>{slaPercent >= 90 ? 'On Target' : slaPercent >= 70 ? 'Needs Attention' : 'Critical'}</span>
                            </div>
                            <div className="card-body">
                                <div className="sla-gauge-wrap">
                                    <div className="sla-gauge">
                                        <Doughnut data={slaGaugeData} options={{ responsive: true, maintainAspectRatio: true, cutout: '80%', plugins: { legend: { display: false }, tooltip: { enabled: false } } }} />
                                        <div className="sla-gauge-center">
                                            <div className="sla-gauge-value">{slaPercent}%</div>
                                            <div className="sla-gauge-label">Compliance</div>
                                        </div>
                                    </div>
                                    <div className="sla-stats">
                                        <div className="sla-stat success"><div className="num">{slaMet}</div><div className="lbl">SLA Met</div></div>
                                        <div className="sla-stat danger"><div className="num">{slaBreached}</div><div className="lbl">Breached</div></div>
                                        <div className="sla-stat warning"><div className="num">{atRisk}</div><div className="lbl">At Risk</div></div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Top Engineers */}
                        <div className="card">
                            <div className="card-header"><h3>Top Performers</h3></div>
                            <div className="card-body" style={{ padding: 0 }}>
                                <table className="data-table">
                                    <thead><tr><th style={{textAlign:'center'}}>Engineer</th><th style={{textAlign:'center'}}>Resolved</th><th style={{textAlign:'center'}}>SLA %</th></tr></thead>
                                    <tbody>
                                        {engineers.slice(0, 5).map((eng, i) => (
                                            <tr key={i}>
                                                <td style={{textAlign:'center', display:'flex', justifyContent:'center', alignItems:'center', gap:'8px'}}>
                                                    <div className="avatar">{getInitials(eng.name)}</div>
                                                    <span>{eng.name}</span>
                                                </td>
                                                <td style={{textAlign:'center'}}>{eng.resolvedCount || eng.resolved || 0}</td>
                                                <td style={{textAlign:'center'}}>{eng.slaCompliance || eng.slaPercent || 0}%</td>
                                            </tr>
                                        ))}
                                        {engineers.length === 0 && <tr><td colSpan="3" style={{ textAlign: 'center', color: 'var(--text-muted)', padding: 20 }}>No data</td></tr>}
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        {/* Top Categories */}
                        <div className="card">
                            <div className="card-header"><h3>Top Categories</h3></div>
                            <div className="card-body" style={{ padding: 0 }}>
                                <table className="data-table">
                                    <thead><tr><th style={{textAlign:'center'}}>Category</th><th style={{textAlign:'center'}}>Count</th><th style={{textAlign:'center'}}>%</th></tr></thead>
                                    <tbody>
                                        {categories.slice(0, 5).map((cat, i) => (
                                            <tr key={i}>
                                                <td style={{textAlign:'center'}}><span className="category-tag">{cat.category || cat.name || 'Unknown'}</span></td>
                                                <td style={{textAlign:'center'}}>{cat.count || cat.total || 0}</td>
                                                <td style={{textAlign:'center'}}>{totalTickets > 0 ? (((cat.count || cat.total || 0) / totalTickets) * 100).toFixed(1) : 0}%</td>
                                            </tr>
                                        ))}
                                        {categories.length === 0 && <tr><td colSpan="3" style={{ textAlign: 'center', color: 'var(--text-muted)', padding: 20 }}>No data</td></tr>}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

                    {/* Key Insights */}
                    {insights.length > 0 && (
                        <div className="card">
                            <div className="card-header"><h3>Key Insights &amp; Recommendations</h3></div>
                            <div style={{ padding: '20px' }}>
                                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 16 }}>
                                    {insights.map((insight, i) => (
                                        <div key={i} style={{ padding: '16px', background: '#F9FAFB', border: '1px solid #E5E7EB', borderRadius: '12px' }}>
                                            <div style={{ fontSize: 12, color: '#6B7280', fontWeight: 600, marginBottom: 8, textTransform: 'uppercase', letterSpacing: '0.5px' }}>
                                                {insight.type === 'success' ? 'Success' : insight.type === 'warning' ? 'Warning' : 'Information'}
                                            </div>
                                            <p style={{ margin: 0, color: '#1E293B', fontSize: 13, fontWeight: 500, lineHeight: 1.5 }}>
                                                {String(insight.message || insight.text || insight).replace(/^[📊🎯✓⚠✕ℹ]\s*/, '')}
                                            </p>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            )}

            {/* =================== SLA ANALYSIS TAB =================== */}
            {activeTab === 'sla' && (
                <div>
                    <div className="grid-2">
                        <div className="card">
                            <div className="card-header"><h3>SLA Met vs Breached</h3></div>
                            <div className="card-body">
                                <div className="chart-container"><Doughnut data={slaBreakdownData} options={chartOptionsLegend} /></div>
                            </div>
                        </div>
                        <div className="card">
                            <div className="card-header"><h3>Tickets by Priority</h3></div>
                            <div className="card-body">
                                <div className="chart-container"><Bar data={priorityChartData} options={barOptions} /></div>
                            </div>
                        </div>
                    </div>
                    <div className="card">
                        <div className="card-header"><h3>SLA Breaches by Category</h3></div>
                        <div className="card-body" style={{ padding: 0 }}>
                            <table className="data-table">
                                <thead><tr><th style={{textAlign:'center'}}>Category</th><th style={{textAlign:'center'}}>Breaches</th><th style={{textAlign:'center'}}>Total</th><th style={{textAlign:'center'}}>Breach Rate</th></tr></thead>
                                <tbody>
                                    {categories.map((cat, i) => {
                                        const breachCount = cat.breachedCount || cat.breaches || 0;
                                        const catTotal = cat.count || cat.total || 0;
                                        const breachRate = catTotal > 0 ? ((breachCount / catTotal) * 100).toFixed(1) : 0;
                                        return (
                                            <tr key={i}>
                                                <td style={{textAlign:'center'}}>{cat.category || cat.name || 'Unknown'}</td>
                                                <td style={{ color: breachCount > 0 ? 'var(--danger)' : undefined, fontWeight: 600, textAlign:'center' }}>{breachCount}</td>
                                                <td style={{textAlign:'center'}}>{catTotal}</td>
                                                <td style={{textAlign:'center'}}>
                                                    <div className="progress-bar">
                                                        <div className={`progress-fill ${breachRate > 30 ? 'danger' : breachRate > 15 ? 'warning' : 'success'}`} style={{ width: `${Math.min(breachRate, 100)}%` }}></div>
                                                    </div>
                                                    {breachRate}%
                                                </td>
                                            </tr>
                                        );
                                    })}
                                    {categories.length === 0 && <tr><td colSpan="4" style={{ textAlign: 'center', color: 'var(--text-muted)', padding: 20 }}>No data</td></tr>}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            )}

            {/* =================== ENGINEERS TAB =================== */}
            {activeTab === 'engineers' && (
                <div className="card">
                    <div className="card-header">
                        <h3>Engineer Performance</h3>
                        <span className="badge badge-neutral">{engineers.length} engineers</span>
                    </div>
                    <div className="card-body" style={{ padding: 0, overflowX: 'auto' }}>
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th style={{textAlign:'center'}}>Engineer</th>
                                    <th style={{textAlign:'center'}}>Team</th>
                                    <th style={{textAlign:'center'}}>Assigned</th>
                                    <th style={{textAlign:'center'}}>Resolved</th>
                                    <th style={{textAlign:'center'}}>Resolution Rate</th>
                                    <th style={{textAlign:'center'}}>SLA Compliance</th>
                                    <th style={{textAlign:'center'}}>Avg Time (hrs)</th>
                                    <th style={{textAlign:'center'}}>In Progress</th>
                                    <th style={{textAlign:'center'}}>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                {engineers.map((eng, i) => {
                                    const assigned = eng.assignedCount || eng.assigned || 0;
                                    const resolved = eng.resolvedCount || eng.resolved || 0;
                                    const resRate = assigned > 0 ? ((resolved / assigned) * 100).toFixed(0) : 0;
                                    const slaPct = eng.slaCompliance || eng.slaPercent || 0;
                                    const avgTime = eng.avgResolutionTime || eng.avgTime || 0;
                                    const inProg = eng.inProgressCount || eng.inProgress || 0;
                                    return (
                                        <tr key={i}>
                                            <td style={{textAlign:'center'}}><div className="engineer-cell"><div className="avatar">{getInitials(eng.name)}</div><span>{eng.name}</span></div></td>
                                            <td style={{textAlign:'center'}}>{eng.team || eng.department || '---'}</td>
                                            <td style={{textAlign:'center'}}>{assigned}</td>
                                            <td style={{textAlign:'center'}}>{resolved}</td>
                                            <td style={{textAlign:'center'}}>
                                                <div className="progress-bar"><div className={`progress-fill ${resRate >= 80 ? 'success' : resRate >= 50 ? 'warning' : 'danger'}`} style={{ width: `${resRate}%` }}></div></div>
                                                {resRate}%
                                            </td>
                                            <td style={{textAlign:'center'}}><span className={`badge ${getPerformBadge(slaPct)}`}>{slaPct}%</span></td>
                                            <td style={{textAlign:'center'}}>{typeof avgTime === 'number' ? avgTime.toFixed(1) : avgTime}</td>
                                            <td style={{textAlign:'center'}}>{inProg}</td>
                                            <td style={{textAlign:'center'}}><span className={`badge ${getPerformBadge(resRate)}`}>{resRate >= 80 ? 'Excellent' : resRate >= 60 ? 'Good' : resRate >= 40 ? 'Fair' : 'Needs Focus'}</span></td>
                                        </tr>
                                    );
                                })}
                                {engineers.length === 0 && <tr><td colSpan="9" style={{ textAlign: 'center', color: 'var(--text-muted)', padding: 20 }}>No engineer data available</td></tr>}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* =================== CATEGORIES TAB =================== */}
            {activeTab === 'categories' && (
                <div>
                    <div className="grid-2">
                        <div className="card">
                            <div className="card-header"><h3>Ticket Volume by Category</h3></div>
                            <div className="card-body"><div className="chart-container"><Bar data={categoryChartData} options={barOptions} /></div></div>
                        </div>
                        <div className="card">
                            <div className="card-header"><h3>Category Distribution</h3></div>
                            <div className="card-body"><div className="chart-container"><Pie data={categoryPieData} options={chartOptionsLegend} /></div></div>
                        </div>
                    </div>
                    <div className="card">
                        <div className="card-header">
                            <h3>Category Details</h3>
                            <span className="badge badge-neutral">{categories.length} categories</span>
                        </div>
                        <div className="card-body" style={{ padding: 0 }}>
                            <table className="data-table">
                                <thead><tr><th style={{textAlign:'center'}}>Category</th><th style={{textAlign:'center'}}>Total</th><th style={{textAlign:'center'}}>Resolved</th><th style={{textAlign:'center'}}>Pending</th><th style={{textAlign:'center'}}>SLA %</th></tr></thead>
                                <tbody>
                                    {categories.map((cat, i) => {
                                        const catTotal = cat.count || cat.total || 0;
                                        const catRes = cat.resolvedCount || cat.resolved || 0;
                                        const catPend = catTotal - catRes;
                                        const catSla = cat.slaCompliance || (catTotal > 0 ? (((catTotal - (cat.breachedCount || 0)) / catTotal) * 100).toFixed(0) : 0);
                                        return (
                                            <tr key={i}>
                                                <td style={{textAlign:'center'}}><span className="category-tag">{cat.category || cat.name || 'Unknown'}</span></td>
                                                <td style={{ fontWeight: 600, textAlign:'center' }}>{catTotal}</td>
                                                <td style={{ color: 'var(--success)', fontWeight: 500, textAlign:'center' }}>{catRes}</td>
                                                <td style={{ color: catPend > 0 ? 'var(--warning)' : undefined, textAlign:'center' }}>{catPend}</td>
                                                <td style={{textAlign:'center'}}><span className={`badge ${getPerformBadge(catSla)}`}>{catSla}%</span></td>
                                            </tr>
                                        );
                                    })}
                                    {categories.length === 0 && <tr><td colSpan="5" style={{ textAlign: 'center', color: 'var(--text-muted)', padding: 20 }}>No category data</td></tr>}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            )}

            {/* =================== TRENDS TAB =================== */}
            {activeTab === 'trends' && (
                <div>
                    <div className="card">
                        <div className="card-header"><h3>Ticket Trends</h3></div>
                        <div className="card-body">
                            <div className="chart-container" style={{ height: 400 }}>
                                <Line data={trendChartData} options={{
                                    responsive: true,
                                    maintainAspectRatio: false,
                                    plugins: { legend: { position: 'bottom' } },
                                    scales: {
                                        y: { beginAtZero: true, grid: { color: '#F1F5F9' } },
                                        x: { grid: { display: false } },
                                    },
                                }} />
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </Layout>
    );
}
