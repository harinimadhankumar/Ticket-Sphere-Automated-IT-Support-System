import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout/Layout';
import { showToast, getUserFriendlyMessage } from '../../utils/toastConfig';
import { isLoggedIn } from '../../utils/auth';
import { generateReport, downloadReport } from '../../utils/api';

export default function Reports() {
    const navigate = useNavigate();
    const [refreshing, setRefreshing] = useState(false);
    const [generatingReport, setGeneratingReport] = useState(false);
    const [reportType, setReportType] = useState('summary');
    const [dateRange, setDateRange] = useState('30days');
    const [customStartDate, setCustomStartDate] = useState('');
    const [customEndDate, setCustomEndDate] = useState('');
    const [reportData, setReportData] = useState(null);
    const [selectedDepartment, setSelectedDepartment] = useState('all');
    const [searchQuery, setSearchQuery] = useState('');

    const reportTypes = [
        { value: 'summary', label: 'Executive Summary', description: 'Overall system metrics and KPIs' },
        { value: 'detailed', label: 'Detailed Analysis', description: 'In-depth ticket and performance data' },
        { value: 'compliance', label: 'Compliance Report', description: 'SLA compliance and policy adherence' },
        { value: 'performance', label: 'Team Performance', description: 'Engineer and team metrics' },
        { value: 'financial', label: 'Financial Impact', description: 'Cost analysis and ROI metrics' },
        { value: 'customer', label: 'Customer Feedback', description: 'Satisfaction and feedback analysis' },
    ];

    const departments = [
        { value: 'all', label: 'All Departments' },
        { value: 'network', label: 'Network' },
        { value: 'hardware', label: 'Hardware' },
        { value: 'software', label: 'Software' },
        { value: 'email', label: 'Email' },
        { value: 'access', label: 'Access' },
    ];

    const loadReports = useCallback(async () => {
        if (!isLoggedIn()) { navigate('/login', { replace: true }); return; }
        try {
        } catch (error) {
        }
    }, [navigate]);

    useEffect(() => { loadReports(); }, [loadReports]);

    const handleRefresh = async () => {
        setRefreshing(true);
        await new Promise(resolve => setTimeout(resolve, 500));
        setRefreshing(false);
        showToast.success('✓ Reports refreshed');
    };

    const handleGenerateReport = useCallback(async () => {
        setGeneratingReport(true);
        try {
            if (dateRange === 'custom') {
                if (!customStartDate || !customEndDate) {
                    showToast.warning('⚠ Please select both start and end dates for custom range');
                    setGeneratingReport(false);
                    return;
                }
            }

            const data = await generateReport(
                reportType,
                dateRange,
                selectedDepartment,
                customStartDate,
                customEndDate
            );

            // Extract the actual report data from the wrapped response
            let reportData = data.data?.report || data.data || data;
            setReportData(reportData);
            showToast.success('✓ Report generated successfully');
        } catch (error) {
            console.error('Error generating report:', error);
            showToast.error(getUserFriendlyMessage('Failed to generate report'));
        } finally {
            setGeneratingReport(false);
        }
    }, [reportType, dateRange, selectedDepartment, customStartDate, customEndDate]);

    const handleDownloadReport = async (format = 'pdf') => {
        try {
            if (!reportData) {
                showToast.warning('Please generate a report first');
                return;
            }

            const blob = await downloadReport(reportType, format);

            // Create and trigger download
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = `report-${reportType}-${Date.now()}.${format}`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);

            showToast.success(`Report downloaded as ${format.toUpperCase()}`);
        } catch (error) {
            console.error('Download error:', error);
            showToast.error('Failed to download report');
        }
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
                    <h1>Reports</h1>
                    <p>Generate and download custom reports for analysis and insights.</p>
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
            <div className="stats-grid" style={{ marginBottom: 24 }}>
                <div className="stat-card">
                    <div className="stat-label">Report Types</div>
                    <div className="stat-value">{reportTypes.length}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Available Formats</div>
                    <div className="stat-value">2</div>
                    <div className="stat-sub">PDF, Excel</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Date Ranges</div>
                    <div className="stat-value">5</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Departments</div>
                    <div className="stat-value">{departments.length}</div>
                </div>
            </div>

            {/* Filters */}
            <div className="filters-bar" style={{ marginBottom: 24 }}>
                <div className="filter-tabs">
                    {reportTypes.map(rt => (
                        <button
                            key={rt.value}
                            className={`filter-tab ${reportType === rt.value ? 'active' : ''}`}
                            onClick={() => setReportType(rt.value)}
                        >
                            {rt.label}
                        </button>
                    ))}
                </div>
                <div className="filter-actions">
                    <select
                        value={selectedDepartment}
                        onChange={(e) => setSelectedDepartment(e.target.value)}
                        className="search-input"
                        style={{ width: 180 }}
                    >
                        {departments.map(dept => (
                            <option key={dept.value} value={dept.value}>{dept.label}</option>
                        ))}
                    </select>
                    <input
                        type="text"
                        className="search-input"
                        placeholder="Search reports..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        style={{ width: 280 }}
                    />
                </div>
            </div>

            {/* Report Configuration */}
            <div className="grid-2" style={{ marginBottom: 24 }}>
                {/* Report Type Selection */}
                <div className="card">
                    <div className="card-header">
                        <h2>Select Report Type</h2>
                    </div>
                    <div style={{ padding: '20px' }}>
                        <div style={{ display: 'grid', gap: 12 }}>
                            {reportTypes.map(rt => (
                                <div
                                    key={rt.value}
                                    style={{
                                        padding: 14,
                                        border: reportType === rt.value ? '2px solid #1B2A4A' : '1px solid #E5E7EB',
                                        borderRadius: 8,
                                        cursor: 'pointer',
                                        transition: 'all 0.2s ease',
                                        backgroundColor: reportType === rt.value ? '#F3F4F6' : '#F9FAFB',
                                    }}
                                    onClick={() => setReportType(rt.value)}
                                >
                                    <div style={{ fontWeight: 600, color: '#1B2A4A', fontSize: 13 }}>{rt.label}</div>
                                    <div style={{ fontSize: 12, color: '#64748B', marginTop: 4 }}>{rt.description}</div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>

                {/* Report Parameters */}
                <div className="card">
                    <div className="card-header">
                        <h2>Report Parameters</h2>
                    </div>
                    <div style={{ padding: '20px' }}>
                        <div style={{ display: 'grid', gap: 16 }}>
                            <div>
                                <label style={{ display: 'block', fontWeight: 600, marginBottom: 8, fontSize: 13, color: '#1B2A4A' }}>Date Range</label>
                                <select
                                    value={dateRange}
                                    onChange={(e) => {
                                        setDateRange(e.target.value);
                                        // Reset custom dates when changing range
                                        if (e.target.value !== 'custom') {
                                            setCustomStartDate('');
                                            setCustomEndDate('');
                                        }
                                    }}
                                    className="search-input"
                                    style={{
                                        width: '100%',
                                        padding: '10px 12px',
                                        fontSize: 14,
                                        color: '#1B2A4A',
                                        borderRadius: 6,
                                        border: '1px solid #E5E7EB',
                                    }}
                                >
                                    <option value="7days">Last 7 Days</option>
                                    <option value="30days">Last 30 Days</option>
                                    <option value="90days">Last 90 Days</option>
                                    <option value="ytd">Year to Date</option>
                                    <option value="custom">Custom Date Range</option>
                                </select>
                            </div>

                            {dateRange === 'custom' && (
                                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                                    <div>
                                        <label style={{ display: 'block', fontWeight: 600, marginBottom: 8, fontSize: 13, color: '#1B2A4A' }}>Start Date</label>
                                        <input
                                            type="date"
                                            value={customStartDate}
                                            onChange={(e) => setCustomStartDate(e.target.value)}
                                            className="search-input"
                                            style={{
                                                width: '100%',
                                                padding: '10px 12px',
                                                fontSize: 14,
                                                color: '#1B2A4A',
                                                borderRadius: 6,
                                                border: '1px solid #E5E7EB',
                                            }}
                                        />
                                    </div>
                                    <div>
                                        <label style={{ display: 'block', fontWeight: 600, marginBottom: 8, fontSize: 13, color: '#1B2A4A' }}>End Date</label>
                                        <input
                                            type="date"
                                            value={customEndDate}
                                            onChange={(e) => setCustomEndDate(e.target.value)}
                                            className="search-input"
                                            style={{
                                                width: '100%',
                                                padding: '10px 12px',
                                                fontSize: 14,
                                                color: '#1B2A4A',
                                                borderRadius: 6,
                                                border: '1px solid #E5E7EB',
                                            }}
                                        />
                                    </div>
                                </div>
                            )}
                            <div>
                                <label style={{ display: 'block', fontWeight: 600, marginBottom: 8, fontSize: 13, color: '#1B2A4A' }}>Department</label>
                                <select
                                    value={selectedDepartment}
                                    onChange={(e) => setSelectedDepartment(e.target.value)}
                                    className="search-input"
                                    style={{
                                        width: '100%',
                                        padding: '10px 12px',
                                        fontSize: 14,
                                        color: '#1B2A4A',
                                        borderRadius: 6,
                                        border: '1px solid #E5E7EB',
                                    }}
                                >
                                    {departments.map(dept => (
                                        <option key={dept.value} value={dept.value}>{dept.label}</option>
                                    ))}
                                </select>
                            </div>
                            <button
                                onClick={handleGenerateReport}
                                disabled={generatingReport}
                                className="btn btn-primary"
                                style={{ width: '100%', marginTop: 8 }}
                            >
                                {generatingReport ? 'Generating...' : 'Generate Report'}
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* Report Preview */}
            {reportData && (
                <div className="card" style={{ marginBottom: 24 }}>
                    <div className="card-header">
                        <h2>Report Preview</h2>
                    </div>
                    <div style={{ overflowX: 'auto' }}>
                        {typeof reportData === 'string' ? (
                            <div style={{ whiteSpace: 'pre-wrap', fontSize: 13, fontFamily: 'monospace', color: '#1B2A4A', padding: '20px' }}>
                                {reportData}
                            </div>
                        ) : Object.keys(reportData).length === 0 ? (
                            <div className="empty-state">
                                <h3>No Report Data</h3>
                                <p>Generate a report to see the results here.</p>
                            </div>
                        ) : (
                            <>
                                {/* Generated Row */}
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '16px 20px', borderBottom: '1px solid #E5E7EB', backgroundColor: '#F9FAFB', gap: '16px' }}>
                                    <div style={{ fontWeight: 700, color: '#1B2A4A', fontSize: 14, whiteSpace: 'nowrap' }}>
                                        Generated: <span style={{ fontWeight: 700, color: '#1B2A4A' }}>{reportData.generatedAt}</span>
                                    </div>
                                    <button
                                        onClick={() => handleDownloadReport('pdf')}
                                        className="btn btn-primary"
                                        style={{ padding: '8px 24px', fontSize: 12, whiteSpace: 'nowrap', flexShrink: 0, display: 'inline-flex', width: 'auto', marginLeft: 'auto' }}
                                    >
                                        PDF
                                    </button>
                                </div>
                                {/* Data Table */}
                                <table className="data-table">
                                    <thead>
                                        <tr>
                                            <th style={{ width: '50%', textAlign: 'center', color: '#1B2A4A', fontWeight: 600, padding: '12px 16px' }}>Metric</th>
                                            <th style={{ width: '50%', textAlign: 'center', color: '#1B2A4A', fontWeight: 600, padding: '12px 16px' }}>Value</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {Object.entries(reportData)
                                            .filter(([key]) => !['reportType', 'format', 'generatedAt', 'metrics'].includes(key))
                                            .map(([key, value], idx) => {
                                                const formattedKey = String(key)
                                                    .replace(/([A-Z])/g, ' $1')
                                                    .replace(/^./, str => str.toUpperCase())
                                                    .trim();

                                                // Format value based on type
                                                let displayValue = value;
                                                if (typeof value === 'number') {
                                                    displayValue = value.toLocaleString();
                                                } else if (typeof value === 'boolean') {
                                                    displayValue = value ? 'Yes' : 'No';
                                                } else if (typeof value === 'object' && value !== null) {
                                                    displayValue = JSON.stringify(value, null, 2);
                                                }

                                                return (
                                                    <tr key={idx}>
                                                        <td style={{ width: '50%', textAlign: 'center', fontWeight: 600, color: '#1B2A4A', padding: '12px 16px' }}>
                                                            {formattedKey}
                                                        </td>
                                                        <td style={{ width: '50%', textAlign: 'center', padding: '12px 16px' }}>
                                                            <span style={{ fontWeight: 600, color: '#1B2A4A' }}>
                                                                {displayValue}
                                                            </span>
                                                        </td>
                                                    </tr>
                                                );
                                            })}
                                    </tbody>
                                </table>
                            </>
                        )}
                    </div>
                </div>
            )}

            {/* Report Information */}
            <div className="card" style={{ marginBottom: 24 }}>
                <div className="card-header">
                    <h2>Available Report Types</h2>
                </div>
                <div style={{ padding: '20px' }}>
                    <div style={{ display: 'grid', gap: 16 }}>
                        {reportTypes.map(rt => (
                            <div key={rt.value} style={{ padding: 14, background: '#F9FAFB', borderRadius: 8, border: '1px solid #E5E7EB' }}>
                                <div style={{ fontWeight: 600, color: '#1B2A4A', marginBottom: 4 }}>{rt.label}</div>
                                <div style={{ fontSize: 13, color: '#64748B' }}>{rt.description}</div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </Layout>
    );
}
