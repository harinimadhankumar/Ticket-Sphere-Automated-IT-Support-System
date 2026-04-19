import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout/Layout';
import { fetchManagementDepartments } from '../../utils/api';
import { showToast, getUserFriendlyMessage } from '../../utils/toastConfig';
import { isLoggedIn } from '../../utils/auth';

export default function DepartmentReport() {
    const navigate = useNavigate();
    const [refreshing, setRefreshing] = useState(false);
    const [summaryData, setSummaryData] = useState(null);

    const loadDepartments = useCallback(async () => {
        if (!isLoggedIn()) { navigate('/login', { replace: true }); return; }
        try {
            const response = await fetchManagementDepartments();
            console.log('Department summary response:', response);

            // Backend returns: { success, data: { totalTickets, resolvedTickets, openTickets, inProgressTickets, slaBreached, slaComplianceRate, byPriority } }
            const summary = response?.data || {};
            console.log('Extracted summary:', summary);
            setSummaryData(summary);
            setRefreshing(false);
        } catch (error) {
            showToast.error(getUserFriendlyMessage('Failed to load department data'));
            setSummaryData({});
            setRefreshing(false);
        }
    }, [navigate]);

    const handleRefresh = async () => {
        setRefreshing(true);
        await loadDepartments();
    };

    useEffect(() => { loadDepartments(); }, [loadDepartments]);

    if (!summaryData) return <Layout ><div className="empty-state"><h3>No data available</h3></div></Layout>;

    const totalTickets = summaryData.totalTickets || 0;
    const resolvedTickets = summaryData.resolvedTickets || 0;
    const openTickets = summaryData.openTickets || 0;
    const inProgressTickets = summaryData.inProgressTickets || 0;
    const slaBreached = summaryData.slaBreached || 0;
    const slaComplianceRate = summaryData.slaComplianceRate || 0;
    const byPriority = summaryData.byPriority || {};

    const pendingTickets = totalTickets - resolvedTickets;

    return (
        <Layout showNav={true} showUser={true} showSidebar={true} showSecondaryNav={true}>
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
                    <h1>Department Report</h1>
                    <p>Cross-departmental ticket statistics and SLA compliance summary.</p>
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
                    <div className="stat-label">Total Tickets</div>
                    <div className="stat-value">{totalTickets}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Resolved</div>
                    <div className="stat-value">{resolvedTickets}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Pending</div>
                    <div className="stat-value">{pendingTickets}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">SLA Compliance</div>
                    <div className="stat-value">
                        {slaComplianceRate.toFixed(1)}%
                    </div>
                </div>
            </div>

            {/* Summary Section */}
            <div className="card" style={{ marginBottom: 24 }}>
                <div className="card-header">
                    <h2>Summary Metrics</h2>
                </div>
                <div style={{ padding: '20px' }}>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: 16 }}>
                        <div style={{ textAlign: 'center', padding: '12px', background: '#F9FAFB', borderRadius: '8px' }}>
                            <div style={{ fontSize: 12, color: '#6B7280', marginBottom: 8 }}>Open Tickets</div>
                            <div style={{ fontSize: 24, fontWeight: 700, color: '#1E293B' }}>{openTickets}</div>
                        </div>
                        <div style={{ textAlign: 'center', padding: '12px', background: '#F9FAFB', borderRadius: '8px' }}>
                            <div style={{ fontSize: 12, color: '#6B7280', marginBottom: 8 }}>In Progress</div>
                            <div style={{ fontSize: 24, fontWeight: 700, color: '#1E293B' }}>{inProgressTickets}</div>
                        </div>
                        <div style={{ textAlign: 'center', padding: '12px', background: '#F9FAFB', borderRadius: '8px' }}>
                            <div style={{ fontSize: 12, color: '#6B7280', marginBottom: 8 }}>SLA Breached</div>
                            <div style={{ fontSize: 24, fontWeight: 700, color: '#1E293B' }}>{slaBreached}</div>
                        </div>
                        <div style={{ textAlign: 'center', padding: '12px', background: '#F9FAFB', borderRadius: '8px' }}>
                            <div style={{ fontSize: 12, color: '#6B7280', marginBottom: 8 }}>Compliance Rate</div>
                            <div style={{ fontSize: 24, fontWeight: 700, color: '#1E293B' }}>
                                {slaComplianceRate.toFixed(1)}%
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Priority Breakdown */}
            <div className="card" style={{ marginBottom: 24 }}>
                <div className="card-header">
                    <h2>Tickets by Priority</h2>
                </div>
                <div style={{ padding: '20px' }}>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: 16 }}>
                        {Object.entries(byPriority).map(([priority, count]) => (
                            <div key={priority} style={{ textAlign: 'center', padding: '12px', background: '#F9FAFB', borderRadius: '8px' }}>
                                <div style={{ fontSize: 12, color: '#6B7280', marginBottom: 8 }}>{priority}</div>
                                <div style={{ fontSize: 24, fontWeight: 700, color: '#1E293B' }}>{count}</div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>

            {/* Resolution Progress */}
            <div className="card" style={{ marginBottom: 24 }}>
                <div className="card-header">
                    <h2>Resolution Progress</h2>
                </div>
                <div style={{ padding: '32px', display: 'grid', gridTemplateColumns: '1fr 1.2fr', gap: 48, alignItems: 'center' }}>
                    {/* Left: Circular Progress */}
                    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
                        <div style={{
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            width: 180,
                            height: 180,
                            borderRadius: '50%',
                            background: `conic-gradient(
                                #1E293B 0% ${totalTickets > 0 ? (resolvedTickets / totalTickets) * 100 : 0}%,
                                #E5E7EB ${totalTickets > 0 ? (resolvedTickets / totalTickets) * 100 : 0}% 100%
                            )`,
                            padding: 6,
                            boxShadow: '0 4px 12px rgba(0, 0, 0, 0.08)',
                        }}>
                            <div style={{
                                display: 'flex',
                                flexDirection: 'column',
                                alignItems: 'center',
                                justifyContent: 'center',
                                width: '100%',
                                height: '100%',
                                borderRadius: '50%',
                                background: 'white',
                            }}>
                                <div style={{ fontSize: 36, fontWeight: 700, color: '#1E293B', letterSpacing: '-1px' }}>
                                    {totalTickets > 0 ? ((resolvedTickets / totalTickets) * 100).toFixed(0) : 0}%
                                </div>
                                <div style={{ fontSize: 13, color: '#1E293B', marginTop: 6, fontWeight: 500 }}>Resolved</div>
                            </div>
                        </div>
                        <div style={{ fontSize: 13, color: '#1E293B', marginTop: 20, fontWeight: 500, letterSpacing: '0.3px' }}>Overall Progress</div>
                        <div style={{ fontSize: 11, color: '#1E293B', marginTop: 6 }}>Out of {totalTickets} total tickets</div>
                    </div>

                    {/* Right: Metrics Grid */}
                    <div style={{ display: 'grid', gap: 16, gridTemplateColumns: '1fr 1fr' }}>
                        <div style={{ padding: '20px', background: '#F0FDF4', borderRadius: '12px', border: '1px solid #D1FAE5', transition: 'all 0.2s ease' }}>
                            <div style={{ fontSize: 12, color: '#059669', fontWeight: 600, marginBottom: 8, textTransform: 'uppercase', letterSpacing: '0.5px' }}>Resolved Tickets</div>
                            <div style={{ fontSize: 32, fontWeight: 700, color: '#10B981', marginBottom: 8 }}>{resolvedTickets}</div>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                <div style={{ flex: 1, height: 6, background: '#E0F2FE', borderRadius: '3px', overflow: 'hidden' }}>
                                    <div style={{
                                        height: '100%',
                                        width: `${totalTickets > 0 ? (resolvedTickets / totalTickets) * 100 : 0}%`,
                                        background: '#10B981',
                                        transition: 'width 0.3s ease'
                                    }}></div>
                                </div>
                                <span style={{ fontSize: 12, color: '#059669', fontWeight: 600, minWidth: 40 }}>
                                    {totalTickets > 0 ? ((resolvedTickets / totalTickets) * 100).toFixed(0) : 0}%
                                </span>
                            </div>
                        </div>
                        <div style={{ padding: '20px', background: '#FFFBEB', borderRadius: '12px', border: '1px solid #FEF3C7', transition: 'all 0.2s ease' }}>
                            <div style={{ fontSize: 12, color: '#92400E', fontWeight: 600, marginBottom: 8, textTransform: 'uppercase', letterSpacing: '0.5px' }}>Pending Tickets</div>
                            <div style={{ fontSize: 32, fontWeight: 700, color: '#F59E0B', marginBottom: 8 }}>{pendingTickets}</div>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                <div style={{ flex: 1, height: 6, background: '#E0F2FE', borderRadius: '3px', overflow: 'hidden' }}>
                                    <div style={{
                                        height: '100%',
                                        width: `${totalTickets > 0 ? (pendingTickets / totalTickets) * 100 : 0}%`,
                                        background: '#F59E0B',
                                        transition: 'width 0.3s ease'
                                    }}></div>
                                </div>
                                <span style={{ fontSize: 12, color: '#92400E', fontWeight: 600, minWidth: 40 }}>
                                    {totalTickets > 0 ? ((pendingTickets / totalTickets) * 100).toFixed(0) : 0}%
                                </span>
                            </div>
                        </div>
                        <div style={{ gridColumn: '1 / -1', padding: '16px', background: '#F9FAFB', borderRadius: '12px', border: '1px solid #E5E7EB', marginTop: 8 }}>
                            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 24, textAlign: 'center' }}>
                                <div>
                                    <div style={{ fontSize: 12, color: '#6B7280', marginBottom: 6 }}>Total Tickets</div>
                                    <div style={{ fontSize: 24, fontWeight: 700, color: '#1E293B' }}>{totalTickets}</div>
                                </div>
                                <div>
                                    <div style={{ fontSize: 12, color: '#6B7280', marginBottom: 6 }}>In Progress</div>
                                    <div style={{ fontSize: 24, fontWeight: 700, color: '#1E293B' }}>{inProgressTickets}</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </Layout>
    );
}
