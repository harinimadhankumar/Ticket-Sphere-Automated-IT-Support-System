import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout/Layout';
import { showToast, getUserFriendlyMessage } from '../../utils/toastConfig';
import {
    fetchTicketDetails,
    startTicket as apiStartTicket,
    resolveTicket as apiResolveTicket,
    submitEscalation as apiSubmitEscalation,
    searchKnowledgeBase,
    fetchTeamLeads,
} from '../../utils/api';
import { isLoggedIn, formatDate } from '../../utils/auth';

export default function ResolveTicket() {
    const { ticketId } = useParams();
    const navigate = useNavigate();
    const [ticket, setTicket] = useState(null);
    const [resolutionNotes, setResolutionNotes] = useState('');
    const [submitting, setSubmitting] = useState(false);
    const [verificationResult, setVerificationResult] = useState(null);
    const [slaTimeRemaining, setSlaTimeRemaining] = useState('--:--');

    // Modals
    const [showEscalationModal, setShowEscalationModal] = useState(false);
    const [escalationReason, setEscalationReason] = useState('');
    const [targetTeamLead, setTargetTeamLead] = useState('');
    const [teamLeads, setTeamLeads] = useState([]);
    const [showKBModal, setShowKBModal] = useState(false);
    const [kbQuery, setKbQuery] = useState('');
    const [kbResults, setKbResults] = useState([]);
    const [kbSearching, setKbSearching] = useState(false);

    const loadTicket = useCallback(async () => {
        if (!isLoggedIn()) { navigate('/login', { replace: true }); return; }
        try {
            const data = await fetchTicketDetails(ticketId);
            setTicket(data);
        } catch (error) {
            showToast.error(getUserFriendlyMessage('Failed to load ticket details'));
        }
    }, [ticketId, navigate]);

    useEffect(() => { loadTicket(); }, [loadTicket]);

    // SLA Countdown Timer
    useEffect(() => {
        if (!ticket || !ticket.slaDeadline) return;

        const updateSlaTime = () => {
            const now = new Date();
            const deadline = new Date(ticket.slaDeadline);
            const difference = deadline - now;

            if (difference <= 0) {
                setSlaTimeRemaining('EXPIRED');
                return;
            }

            const hours = Math.floor(difference / (1000 * 60 * 60));
            const minutes = Math.floor((difference % (1000 * 60 * 60)) / (1000 * 60));
            const seconds = Math.floor((difference % (1000 * 60)) / 1000);

            setSlaTimeRemaining(`${hours}h ${minutes}m ${seconds}s`);
        };

        updateSlaTime();
        const interval = setInterval(updateSlaTime, 1000);
        return () => clearInterval(interval);
    }, [ticket]);

    const handleStart = async () => {
        try {
            const result = await apiStartTicket(ticketId);
            if (result.success !== false) {
                showToast.success('✓ Started working on ticket');
                loadTicket();
            } else {
                showToast.error(getUserFriendlyMessage(result.message || 'Failed to start ticket'));
            }
        } catch (error) {
            showToast.error(getUserFriendlyMessage('Unable to start ticket'));
        }
    };

    const handleResolve = async () => {
        if (resolutionNotes.length < 20) {
            showToast.warning('Please provide resolution notes (minimum 20 characters)');
            return;
        }
        setSubmitting(true);
        try {
            const result = await apiResolveTicket(ticketId, resolutionNotes);
            if (result.success !== false) {
                if (result.verification) {
                    setVerificationResult(result.verification);
                }
                showToast.success('✓ Ticket resolved successfully');
                loadTicket();
            } else {
                if (result.verification) {
                    setVerificationResult(result.verification);
                }
                showToast.error(getUserFriendlyMessage(result.message || 'Failed to resolve ticket'));
            }
        } catch (error) {
            showToast.error(getUserFriendlyMessage('Unable to submit resolution'));
        } finally {
            setSubmitting(false);
        }
    };


    const handleEscalation = async () => {
        if (!escalationReason.trim()) { showToast.warning('Please provide escalation reason'); return; }
        try {
            const result = await apiSubmitEscalation(ticketId, escalationReason, targetTeamLead || null);
            if (result.success !== false) {
                showToast.success('✓ Ticket escalated successfully');
                setShowEscalationModal(false);
                setEscalationReason('');
                setTargetTeamLead('');
                loadTicket();
            } else {
                showToast.error(getUserFriendlyMessage(result.message || 'Failed to escalate'));
            }
        } catch (error) {
            showToast.error(getUserFriendlyMessage('Unable to escalate ticket'));
        }
    };

    const openEscalation = async () => {
        setShowEscalationModal(true);
        try {
            const response = await fetchTeamLeads();
            console.log('Team leads response:', response);

            let leads = [];
            if (Array.isArray(response)) {
                leads = response;
            } else if (response?.teamLeads && Array.isArray(response.teamLeads)) {
                leads = response.teamLeads;
            } else if (response?.data && Array.isArray(response.data)) {
                leads = response.data;
            } else if (response?.leaders && Array.isArray(response.leaders)) {
                leads = response.leaders;
            }

            console.log('Parsed team leads:', leads);
            setTeamLeads(leads);
        } catch (error) {
            showToast.warning('⚠ Could not load team leads - escalation may be auto-assigned');
        }
    };

    const handleKBSearch = async () => {
        if (!kbQuery.trim()) return;
        setKbSearching(true);
        try {
            const results = await searchKnowledgeBase(kbQuery);
            console.log('KB Search results:', results);

            // Handle different response formats from backend
            let searchResults = [];
            if (Array.isArray(results)) {
                searchResults = results;
            } else if (results?.data && Array.isArray(results.data)) {
                searchResults = results.data;
            } else if (results?.results && Array.isArray(results.results)) {
                searchResults = results.results;
            }

            if (searchResults.length === 0) {
                showToast.info('ℹ No solutions found for your search');
            }
            setKbResults(searchResults);
        } catch (error) {
            console.error('KB Search error:', error);
            showToast.error(getUserFriendlyMessage('Failed to search knowledge base'));
            setKbResults([]);
        }
        setKbSearching(false);
    };

    const applyKBSolution = (solution) => {
        // Parse solution_steps if it's a JSON string
        let stepsContent = '';
        if (solution.solutionSteps) {
            try {
                if (typeof solution.solutionSteps === 'string') {
                    const steps = JSON.parse(solution.solutionSteps);
                    stepsContent = Array.isArray(steps) ? steps.join('\n') : solution.solutionSteps;
                } else if (Array.isArray(solution.solutionSteps)) {
                    stepsContent = solution.solutionSteps.join('\n');
                } else {
                    stepsContent = String(solution.solutionSteps);
                }
            } catch (e) {
                stepsContent = String(solution.solutionSteps);
            }
        }

        const solutionText = `## Solution: ${solution.issueTitle || solution.title || 'Solution'}\n\n${stepsContent}`;
        setResolutionNotes(prev => prev + (prev ? '\n\n' : '') + solutionText);
        setShowKBModal(false);
        showToast.success('✓ Solution added to resolution notes');
    };


    if (!ticket) return <Layout ><div className="empty-state"><h3>Ticket not found</h3></div></Layout>;

    const isResolvable = ticket.status === 'IN_PROGRESS';
    const isStartable = ticket.status === 'ASSIGNED';
    const isAlreadyResolved = ticket.status === 'RESOLVED' || ticket.status === 'CLOSED';
    const isEscalated = ticket.status === 'ESCALATED';

    return (
        <Layout showNav={true} showUser={true} showSidebar={true} showSecondaryNav={true}>
            <style>{`
                * {
                    box-sizing: border-box;
                }

                .ticket-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: flex-start;
                    margin-bottom: 32px;
                    padding-bottom: 20px;
                    border-bottom: 2px solid #1E293B;
                }

                .ticket-header h1 {
                    font-size: 30px;
                    font-weight: 700;
                    color: #1E293B;
                    margin: 0 0 8px 0;
                }

                .ticket-header p {
                    font-size: 14px;
                    color: #1E293B;
                    margin: 0;
                }

                .btn {
                    padding: 10px 16px;
                    border: none;
                    border-radius: 8px;
                    font-size: 14px;
                    font-weight: 600;
                    cursor: pointer;
                    transition: all 0.2s ease;
                    font-family: inherit;
                }

                .btn-secondary {
                    background: linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%);
                    color: white;
                }

                .btn-secondary:hover {
                    background: linear-gradient(135deg, #0F172A 0%, #1B2d48 50%, #0F172A 100%);
                    transform: translateY(-2px);
                    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
                }

                .btn-sm {
                    padding: 8px 14px;
                    font-size: 13px;
                }
                
                .grid-sidebar {
                    display: grid;
                    grid-template-columns: 2fr 1fr;
                    gap: 24px;
                }

                .card {
                    background: white;
                    border-radius: 12px;
                    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
                    overflow: hidden;
                    margin-bottom: 20px;
                }

                .card-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    padding: 16px 20px;
                    background: #F9FAFB;
                    border-bottom: 1px solid #E5E7EB;
                }

                .card-header h2 {
                    font-size: 16px;
                    font-weight: 600;
                    color: #1E293B;
                    margin: 0;
                }

                .card-body {
                    padding: 20px;
                }

                .info-grid {
                    display: table;
                    width: 100%;
                    border-collapse: collapse;
                }

                .info-grid.two-column {
                    display: grid;
                    grid-template-columns: 1fr 1fr;
                    gap: 0;
                }

                .info-grid.two-column .info-item {
                    display: contents;
                }

                .info-grid.two-column .info-label {
                    grid-column: 1;
                    width: auto;
                }

                .info-grid.two-column .info-value {
                    grid-column: 2;
                }

                .info-item {
                    display: table-row;
                }

                .info-label {
                    font-size: 12px;
                    font-weight: 600;
                    color: #6B7280;
                    text-transform: uppercase;
                    letter-spacing: 0.5px;
                    padding: 12px 16px;
                    border-bottom: 1px solid #E5E7EB;
                    display: table-cell;
                    width: 25%;
                    background: #FAFBFC;
                }

                .info-value {
                    font-size: 13px;
                    color: #1E293B;
                    font-weight: 500;
                    padding: 12px 16px;
                    border-bottom: 1px solid #E5E7EB;
                    display: table-cell;
                    text-align: right;
                }

                .category-tag {
                    display: inline-block;
                    background: linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%);
                    color: white;
                    padding: 4px 10px;
                    border-radius: 6px;
                    font-size: 12px;
                    font-weight: 600;
                }

                .badge {
                    display: inline-block;
                    padding: 6px 14px;
                    border-radius: 16px;
                    font-size: 12px;
                    font-weight: 600;
                    text-transform: capitalize;
                }

                .issue-section {
                    margin-top: 24px;
                    padding: 16px;
                    background: #F9FAFB;
                    border-left: 4px solid #2563EB;
                    border-radius: 6px;
                }

                .issue-label {
                    font-size: 12px;
                    font-weight: 600;
                    color: #6B7280;
                    text-transform: uppercase;
                    letter-spacing: 0.5px;
                    margin-bottom: 8px;
                }

                .issue-content {
                    font-size: 14px;
                    color: #1F2937;
                    line-height: 1.6;
                    white-space: pre-wrap;
                    word-break: break-word;
                }

                .plain-content {
                    font-size: 14px;
                    color: #1F2937;
                    line-height: 1.6;
                    white-space: pre-wrap;
                    word-break: break-word;
                    background: transparent;
                    border: none;
                }

                .form-group {
                    margin-bottom: 16px;
                }

                .form-group label {
                    display: block;
                    font-size: 14px;
                    font-weight: 600;
                    color: #1E293B;
                    margin-bottom: 8px;
                }

                .form-input {
                    width: 100%;
                    padding: 10px 12px;
                    border: 1px solid #D1D5DB;
                    border-radius: 8px;
                    font-size: 14px;
                    font-family: inherit;
                    transition: all 0.2s ease;
                }

                .form-input:focus {
                    outline: none;
                    border-color: #2563EB;
                    box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
                }

                .btn-primary {
                    background: linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%);
                    color: white;
                }

                .btn-primary:hover {
                    background: linear-gradient(135deg, #0F172A 0%, #1B2d48 50%, #0F172A 100%);
                    transform: translateY(-2px);
                    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
                }

                .btn-success {
                    background: linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%);
                    color: white;
                }

                .btn-success:hover {
                    background: linear-gradient(135deg, #0F172A 0%, #1B2d48 50%, #0F172A 100%);
                    transform: translateY(-2px);
                    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
                }

                .btn:disabled {
                    opacity: 0.6;
                    cursor: not-allowed;
                    transform: none;
                }

                .quick-actions {
                    display: grid;
                    grid-template-columns: 1fr;
                    gap: 10px;
                }

                .quick-action-btn {
                    background: linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%);
                    color: white;
                    border: none;
                    padding: 12px 16px;
                    border-radius: 8px;
                    font-size: 14px;
                    font-weight: 600;
                    cursor: pointer;
                    transition: all 0.2s ease;
                }

                .quick-action-btn:hover {
                    background: linear-gradient(135deg, #0F172A 0%, #1B2d48 50%, #0F172A 100%);
                    transform: translateY(-2px);
                    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
                }

                .sla-panel {
                    background: white;
                    border-radius: 12px;
                    padding: 20px;
                    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
                    margin-bottom: 20px;
                    border-left: 4px solid #2563EB;
                }

                .sla-panel.breached {
                    border-left-color: #DC2626;
                }

                .sla-panel.warning {
                    border-left-color: #F59E0B;
                }

                .sla-panel.on-track {
                    border-left-color: #10B981;
                }

                .sla-header h3 {
                    font-size: 14px;
                    font-weight: 600;
                    color: #1E293B;
                    margin: 0 0 12px 0;
                }

                .sla-time-big {
                    font-size: 32px;
                    font-weight: 700;
                    color: #1E293B;
                    margin-bottom: 8px;
                }

                .sla-deadline {
                    font-size: 12px;
                    color: #6B7280;
                }

                .timeline-item {
                    display: flex;
                    gap: 12px;
                    padding: 12px 0;
                    border-bottom: 1px solid #E5E7EB;
                }

                .timeline-item:last-child {
                    border-bottom: none;
                }

                .timeline-dot {
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    width: 32px;
                    height: 32px;
                    background: linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%);
                    color: white;
                    border-radius: 50%;
                    font-weight: 600;
                    flex-shrink: 0;
                }

                .timeline-content .title {
                    font-size: 14px;
                    font-weight: 600;
                    color: #1E293B;
                }

                .timeline-content .time {
                    font-size: 12px;
                    color: #6B7280;
                    margin-top: 2px;
                }

                .modal-overlay {
                    position: fixed;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0;
                    background: rgba(0, 0, 0, 0.5);
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    z-index: 1000;
                }

                .modal-content {
                    background: white;
                    border-radius: 12px;
                    box-shadow: 0 20px 25px rgba(0, 0, 0, 0.15);
                    max-width: 500px;
                    width: 90%;
                    max-height: 90vh;
                    overflow-y: auto;
                }

                .modal-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    padding: 20px;
                    border-bottom: 1px solid #E5E7EB;
                    background: #F9FAFB;
                }

                .modal-header h2 {
                    font-size: 18px;
                    font-weight: 600;
                    color: #1E293B;
                    margin: 0;
                }

                .modal-close {
                    background: none;
                    border: none;
                    font-size: 24px;
                    color: #6B7280;
                    cursor: pointer;
                    padding: 0;
                    width: 32px;
                    height: 32px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    transition: all 0.2s ease;
                }

                .modal-close:hover {
                    color: #1E293B;
                    background: white;
                    border-radius: 6px;
                }

                .modal-body {
                    padding: 20px;
                }

                .modal-footer {
                    display: flex;
                    gap: 12px;
                    padding: 16px 20px;
                    border-top: 1px solid #E5E7EB;
                    background: #F9FAFB;
                    justify-content: flex-end;
                }

                .verification-result {
                    margin-top: 24px;
                    padding: 16px;
                    background: #F0FDF4;
                    border: 1px solid #6EE7B7;
                    border-radius: 8px;
                }

                .verification-result.failed {
                    background: #FEF2F2;
                    border-color: #FCA5A5;
                }

                .verification-header h3 {
                    font-size: 14px;
                    font-weight: 600;
                    color: #047857;
                    margin: 0 0 12px 0;
                }

                .verification-result.failed .verification-header h3 {
                    color: #991B1B;
                }

                @media (max-width: 1024px) {
                    .grid-sidebar {
                        grid-template-columns: 1fr;
                    }

                    .info-grid {
                        display: table;
                    }

                    .info-label {
                        width: 35%;
                    }
                }

                @media (max-width: 768px) {
                    .ticket-header {
                        flex-direction: column;
                        gap: 12px;
                    }

                    .ticket-header h1 {
                        font-size: 26px;
                    }

                    .info-grid {
                        display: table;
                    }

                    .info-label {
                        width: 40%;
                        padding: 10px 12px;
                        font-size: 11px;
                    }

                    .info-value {
                        padding: 10px 12px;
                        font-size: 10px;
                    }
                }
            `}</style>

            {/* Page Header */}
            <div className="ticket-header">
                <div>
                    <h1>Ticket Details</h1>
                    <p>View and manage ticket information, resolution, and escalation</p>
                </div>
            </div>

            <div className="grid-sidebar">
                {/* Left Column - Main Content */}
                <div>
                    {/* Ticket Information Card */}
                    <div className="card" style={{ marginBottom: 20 }}>
                        <div className="card-header"><h2>Ticket Information</h2></div>
                        <div className="card-body">
                            <div className="info-grid">
                                <div className="info-item">
                                    <div className="info-label">Ticket ID</div>
                                    <div className="info-value">{ticket.ticketId}</div>
                                </div>
                                <div className="info-item">
                                    <div className="info-label">Created</div>
                                    <div className="info-value">{formatDate(ticket.createdTime || ticket.createdAt)}</div>
                                </div>
                                <div className="info-item">
                                    <div className="info-label">Source</div>
                                    <div className="info-value">{ticket.source || 'EMAIL'}</div>
                                </div>
                                <div className="info-item">
                                    <div className="info-label">Employee ID</div>
                                    <div className="info-value">{ticket.employeeId || '---'}</div>
                                </div>
                                <div className="info-item">
                                    <div className="info-label">Category</div>
                                    <div className="info-value"><span className="category-tag">{ticket.category || 'GENERAL'}</span></div>
                                </div>
                                <div className="info-item">
                                    <div className="info-label">Sub-Category</div>
                                    <div className="info-value"><span style={{ fontSize: 12, padding: '4px 10px', background: '#E5E7EB', borderRadius: 6, display: 'inline-block' }}>{ticket.subCategory || 'N/A'}</span></div>
                                </div>
                                <div className="info-item">
                                    <div className="info-label">Priority</div>
                                    <div className="info-value"><span className={`badge badge-${(ticket.priority || 'medium').toLowerCase()}`}>{ticket.priority || 'MEDIUM'}</span></div>
                                </div>
                                <div className="info-item">
                                    <div className="info-label">Status</div>
                                    <div className="info-value"><span className={`badge badge-${(ticket.status || 'assigned').toLowerCase().replace('_', '-')}`}>{ticket.status || 'Unknown'}</span></div>
                                </div>
                                <div className="info-item">
                                    <div className="info-label">Assigned Team</div>
                                    <div className="info-value">{ticket.assignedTeam || '---'}</div>
                                </div>
                                <div className="info-item">
                                    <div className="info-label">Assigned Engineer</div>
                                    <div className="info-value">{ticket.assignedEngineer || '---'}</div>
                                </div>
                                <div className="info-item">
                                    <div className="info-label">Requester Email</div>
                                    <div className="info-value">{ticket.senderEmail || ticket.requester || '---'}</div>
                                </div>
                                {ticket.resolutionStatus && (
                                    <div className="info-item">
                                        <div className="info-label">Resolution Status</div>
                                        <div className="info-value"><span className={`badge badge-${(ticket.resolutionStatus || 'pending').toLowerCase().replace('_', '-')}`}>{ticket.resolutionStatus}</span></div>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>

                    {/* Issue Description Card */}
                    <div className="card" style={{ marginBottom: 20 }}>
                        <div className="card-header"><h2>Issue Description</h2></div>
                        <div className="card-body">
                            <div className="plain-content">{ticket.emailBody || ticket.description || ticket.subject || 'No description available'}</div>
                        </div>
                    </div>

                    {/* Resolution Notes Card */}
                    {!isAlreadyResolved && !isEscalated && (
                        <div className="card" style={{ marginBottom: 20 }}>
                            <div className="card-header"><h2>Resolution Notes</h2></div>
                            <div className="card-body">
                                <div className="form-group">
                                    <textarea
                                        className="form-input"
                                        value={resolutionNotes}
                                        onChange={(e) => setResolutionNotes(e.target.value)}
                                        placeholder="Describe how you resolved this issue. Include steps taken, solutions applied, and any follow-up actions required..."
                                        style={{ minHeight: 160 }}
                                    />
                                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 12, color: 'var(--text-muted)', marginTop: 6 }}>
                                        <span>Minimum 20 characters with detailed steps</span>
                                        <span style={{ fontWeight: 500, color: resolutionNotes.length >= 20 ? 'var(--success)' : undefined }}>
                                            {resolutionNotes.length} / 20
                                        </span>
                                    </div>
                                </div>
                                <div style={{ display: 'flex', gap: 12, marginTop: 16 }}>
                                    <button className="btn btn-secondary" onClick={() => navigate('/engineer/dashboard')}>Cancel</button>
                                    {isStartable && (
                                        <button className="btn btn-primary" onClick={handleStart} style={{ flex: 1 }}>Start Working</button>
                                    )}
                                    {isResolvable && (
                                        <button className="btn btn-success" onClick={handleResolve} disabled={submitting || resolutionNotes.length < 20} style={{ flex: 1 }}>
                                            {submitting ? <><span className="btn-spinner"></span> Submitting...</> : 'Submit Resolution'}
                                        </button>
                                    )}
                                </div>
                            </div>
                        </div>
                    )}

                    {/* Resolution Notes Display Card - When already resolved */}
                    {isAlreadyResolved && ticket.resolutionNotes && (
                        <div className="card" style={{ marginBottom: 20 }}>
                            <div className="card-header"><h2>Resolution Notes</h2></div>
                            <div className="card-body">
                                <div className="plain-content">{ticket.resolutionNotes}</div>
                            </div>
                        </div>
                    )}

                    {/* Verification Status Card */}
                    {isAlreadyResolved && (
                        <div className="card" style={{ marginBottom: 20 }}>
                            <div className="card-header"><h2>Verification Status</h2></div>
                            <div className="card-body">
                                <div className="info-grid two-column">
                                    <div className="info-item">
                                        <div className="info-label">Status</div>
                                        <div className="info-value">{ticket.verificationStatus || 'Unknown'}</div>
                                    </div>
                                    <div className="info-item">
                                        <div className="info-label">Score</div>
                                        <div className="info-value">{ticket.verificationScore || 'N/A'}/100</div>
                                    </div>
                                    <div className="info-item">
                                        <div className="info-label">Verified Time</div>
                                        <div className="info-value">{ticket.verifiedTime ? formatDate(ticket.verifiedTime) : 'Pending'}</div>
                                    </div>
                                    <div className="info-item">
                                        <div className="info-label">Attempts</div>
                                        <div className="info-value">{ticket.verificationAttempts || 0}</div>
                                    </div>
                                </div>
                                {ticket.verificationNotes && (
                                    <div style={{ marginTop: 16, paddingTop: 16, borderTop: '1px solid #E5E7EB' }}>
                                        <div className="info-label">Notes</div>
                                        <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginTop: 8 }}>
                                            {ticket.verificationNotes.split('\n').map((line, i) => (
                                                <div key={i} style={{ fontSize: 13, color: '#1F2937', lineHeight: 1.4 }}>
                                                    {line.trim() ? line : <>&nbsp;</>}
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                )}
                            </div>
                        </div>
                    )}

                    {/* Verification Result Alert */}
                    {verificationResult && (
                        <div className={`verification-result ${verificationResult.passed ? 'success' : 'failed'}`} style={{ marginTop: 20 }}>
                            <div className="verification-header">
                                <h3>{verificationResult.passed ? '✓ Verification Passed' : '⚠ Verification Issues'}</h3>
                            </div>
                            <span className="verification-score">Score: {verificationResult.score || 0}/100</span>
                            {verificationResult.issues && verificationResult.issues.length > 0 && (
                                <div style={{ marginTop: 12 }}>
                                    <h4 style={{ fontSize: 12, fontWeight: 600, marginBottom: 8 }}>Issues to Address:</h4>
                                    <ul style={{ paddingLeft: 20 }}>
                                        {verificationResult.issues.map((issue, i) => (
                                            <li key={i} style={{ fontSize: 13, marginBottom: 4 }}>{issue}</li>
                                        ))}
                                    </ul>
                                </div>
                            )}
                        </div>
                    )}
                </div>

                {/* Right Column - Sidebar */}
                <div>
                    {/* 1. SLA Status Card */}
                    <div className="card" style={{ marginBottom: 24 }}>
                        <div className="card-header"><h2>SLA Status</h2></div>
                        <div className="card-body">
                            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 20 }}>
                                <div style={{
                                    width: 12,
                                    height: 12,
                                    borderRadius: '50%',
                                    background: ticket.slaStatus === 'on-track' ? '#10B981' : ticket.slaStatus === 'warning' ? '#F59E0B' : '#DC2626',
                                    flexShrink: 0
                                }}></div>
                                <div style={{ fontSize: 12, fontWeight: 600, color: '#6B7280', textTransform: 'uppercase' }}>
                                    {(ticket.slaStatus || 'on-track').toUpperCase().replace('_', ' ')}
                                </div>
                            </div>
                            <div style={{ marginBottom: 16 }}>
                                <div style={{ fontSize: 12, color: '#6B7280', marginBottom: 6 }}>Time Remaining</div>
                                <div style={{ fontSize: 26, fontWeight: 700, color: '#1E293B' }}>{slaTimeRemaining}</div>
                            </div>
                            <div>
                                <div style={{ fontSize: 12, color: '#6B7280', marginBottom: 6 }}>Deadline</div>
                                <div style={{ fontSize: 13, color: '#1F2937' }}>{formatDate(ticket.slaDeadline)}</div>
                            </div>
                        </div>
                    </div>

                    {/* 2. SLA Timeline Section - WITH CARD BACKGROUND */}
                    <div className="card" style={{ marginBottom: 24 }}>
                        <div className="card-header"><h2>SLA Timeline</h2></div>
                        <div className="card-body">
                            <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
                                {ticket.createdAt && (
                                    <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12 }}>
                                        <div style={{ fontSize: 12, fontWeight: 700, color: '#10B981', minWidth: 60 }}>CREATED</div>
                                        <div style={{ fontSize: 13, color: '#1F2937' }}>{formatDate(ticket.createdAt || ticket.createdTime)}</div>
                                    </div>
                                )}
                                {ticket.slaDeadline && (
                                    <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12 }}>
                                        <div style={{ fontSize: 12, fontWeight: 700, color: ticket.slaBreached ? '#DC2626' : '#F59E0B', minWidth: 60 }}>DEADLINE</div>
                                        <div style={{ fontSize: 13, color: '#1F2937' }}>{formatDate(ticket.slaDeadline)}</div>
                                    </div>
                                )}
                                {ticket.resolvedAt && (
                                    <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12 }}>
                                        <div style={{ fontSize: 12, fontWeight: 700, color: '#10B981', minWidth: 60 }}>RESOLVED</div>
                                        <div style={{ fontSize: 13, color: '#1F2937' }}>{formatDate(ticket.resolvedAt)}</div>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>

                    {/* 3. Escalation Status Section - WITH CARD BACKGROUND */}
                    {ticket.escalationLevel && (
                        <div className="card" style={{ marginBottom: 24 }}>
                            <div className="card-header"><h2>Escalation Status</h2></div>
                            <div className="card-body">
                                <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                                    <div style={{ fontSize: 13, color: '#DC2626', fontWeight: 600 }}>{ticket.escalationLevel}</div>
                                    {ticket.escalatedToTeamLead && (
                                        <div>
                                            <div style={{ fontSize: 12, color: '#6B7280', marginBottom: 4 }}>Escalated To</div>
                                            <div style={{ fontSize: 13, color: '#1F2937', fontWeight: 500 }}>{ticket.escalatedToTeamLead}</div>
                                        </div>
                                    )}
                                    {ticket.escalatedAt && (
                                        <div>
                                            <div style={{ fontSize: 12, color: '#6B7280', marginBottom: 4 }}>Date</div>
                                            <div style={{ fontSize: 13, color: '#1F2937' }}>{formatDate(ticket.escalatedAt)}</div>
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>
                    )}

                    {/* 4. Escalation Alert Section - WITH CARD BACKGROUND */}
                    {ticket.escalated && ticket.escalationReason && (
                        <div className="card" style={{ marginBottom: 24, borderLeft: '4px solid #DC2626' }}>
                            <div className="card-header"><h2>Escalation Alert</h2></div>
                            <div className="card-body">
                                <div style={{ fontSize: 13, color: '#1F2937', lineHeight: 1.5 }}>{ticket.escalationReason}</div>
                            </div>
                        </div>
                    )}

                    {/* Quick Actions */}
                    {!isAlreadyResolved && !isEscalated && (
                        <div className="card" style={{ marginBottom: 24 }}>
                            <div className="card-header"><h2>Quick Actions</h2></div>
                            <div className="card-body">
                                <div className="quick-actions">
                                    <button className="quick-action-btn" onClick={openEscalation}>
                                        Request Escalation
                                    </button>
                                    <button className="quick-action-btn" onClick={() => setShowKBModal(true)}>
                                        Search Knowledge Base
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* Ticket Status Section - PROPER STYLING */}
                    {isEscalated && (
                        <div className="card">
                            <div className="card-header">
                                <h2>Ticket Status</h2>
                            </div>
                            <div className="card-body">
                                <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
                                    {/* Escalated To Field */}
                                    <div style={{ paddingBottom: 16, borderBottom: '1px solid #E5E7EB' }}>
                                        <div style={{
                                            fontSize: 12,
                                            color: '#1E293B',
                                            marginBottom: 8,
                                            fontWeight: 700,
                                            textTransform: 'uppercase',
                                            letterSpacing: '0.5px'
                                        }}>
                                            Escalated To
                                        </div>
                                        <div style={{
                                            fontSize: 15,
                                            color: '#1F2937',
                                            fontWeight: 600
                                        }}>
                                            {ticket.escalatedToTeamLead || 'Management'}
                                        </div>
                                    </div>

                                    {/* Current Status Field */}
                                    <div style={{ paddingBottom: 16, borderBottom: '1px solid #E5E7EB' }}>
                                        <div style={{
                                            fontSize: 12,
                                            color: '#1E293B',
                                            marginBottom: 8,
                                            fontWeight: 700,
                                            textTransform: 'uppercase',
                                            letterSpacing: '0.5px'
                                        }}>
                                            Current Status
                                        </div>
                                        <div style={{
                                            fontSize: 15,
                                            color: '#1F2937',
                                            fontWeight: 600
                                        }}>
                                            Assigned to Team Engineer
                                        </div>
                                    </div>

                                    {/* Info Message Box */}
                                    <div style={{
                                        padding: '16px 16px',
                                        backgroundColor: '#EFF6FF',
                                        border: '1px solid #BFDBFE',
                                        borderRadius: '8px',
                                        fontSize: 13,
                                        color: '#0369A1',
                                        lineHeight: 1.7,
                                        fontWeight: 500
                                    }}>
                                        This ticket has been escalated to <strong>{ticket.escalatedToTeamLead || 'Management'}</strong> and assigned to an engineer in the corresponding team. They will continue working on the resolution.
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}

                    {/* 5. Activity Log Timeline - WITH CARD BACKGROUND, FILTERED BY TIMESTAMP */}
                    <div className="card">
                        <div className="card-header"><h2>Activity Log</h2></div>
                        <div className="card-body">
                            <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                                {ticket.createdAt && (
                                    <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12 }}>
                                        <div style={{ fontSize: 16, width: 24, height: 24, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#10B981', fontWeight: 700, flexShrink: 0 }}>+</div>
                                        <div>
                                            <div style={{ fontSize: 13, fontWeight: 500, color: '#1E293B' }}>Ticket Created</div>
                                            <div style={{ fontSize: 12, color: '#6B7280', marginTop: 4 }}>{formatDate(ticket.createdAt)}</div>
                                        </div>
                                    </div>
                                )}
                                {ticket.assignedAt && (
                                    <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12 }}>
                                        <div style={{ fontSize: 16, width: 24, height: 24, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#3B82F6', fontWeight: 700, flexShrink: 0 }}>→</div>
                                        <div>
                                            <div style={{ fontSize: 13, fontWeight: 500, color: '#1E293B' }}>Assigned to {ticket.assignedEngineer || 'Engineer'}</div>
                                            <div style={{ fontSize: 12, color: '#6B7280', marginTop: 4 }}>{formatDate(ticket.assignedAt)}</div>
                                        </div>
                                    </div>
                                )}
                                {ticket.startedAt && (
                                    <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12 }}>
                                        <div style={{ fontSize: 16, width: 24, height: 24, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#8B5CF6', fontWeight: 700, flexShrink: 0 }}>▶</div>
                                        <div>
                                            <div style={{ fontSize: 13, fontWeight: 500, color: '#1E293B' }}>Work Started</div>
                                            <div style={{ fontSize: 12, color: '#6B7280', marginTop: 4 }}>{formatDate(ticket.startedAt)}</div>
                                        </div>
                                    </div>
                                )}
                                {ticket.escalatedAt && (
                                    <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12 }}>
                                        <div style={{ fontSize: 16, width: 24, height: 24, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#DC2626', fontWeight: 700, flexShrink: 0 }}>⬆</div>
                                        <div>
                                            <div style={{ fontSize: 13, fontWeight: 500, color: '#1E293B' }}>Escalated</div>
                                            <div style={{ fontSize: 12, color: '#6B7280', marginTop: 4 }}>{formatDate(ticket.escalatedAt)}</div>
                                        </div>
                                    </div>
                                )}
                                {ticket.resolvedAt && (
                                    <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12 }}>
                                        <div style={{ fontSize: 16, width: 24, height: 24, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#10B981', fontWeight: 700, flexShrink: 0 }}>✓</div>
                                        <div>
                                            <div style={{ fontSize: 13, fontWeight: 500, color: '#1E293B' }}>Resolved</div>
                                            <div style={{ fontSize: 12, color: '#6B7280', marginTop: 4 }}>{formatDate(ticket.resolvedAt)}</div>
                                        </div>
                                    </div>
                                )}
                                {ticket.closedAt && (
                                    <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12 }}>
                                        <div style={{ fontSize: 16, width: 24, height: 24, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#6B7280', fontWeight: 700, flexShrink: 0 }}>●</div>
                                        <div>
                                            <div style={{ fontSize: 13, fontWeight: 500, color: '#1E293B' }}>Closed</div>
                                            <div style={{ fontSize: 12, color: '#6B7280', marginTop: 4 }}>{formatDate(ticket.closedAt)}</div>
                                        </div>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            </div>


            {/* Escalation Modal */}
            {showEscalationModal && (
                <div className="modal-overlay" onClick={(e) => { if (e.target === e.currentTarget) setShowEscalationModal(false); }}>
                    <div className="modal-content" style={{ maxWidth: '500px' }}>
                        <div className="modal-header">
                            <h2>Request Escalation</h2>
                            <button className="modal-close" onClick={() => setShowEscalationModal(false)}>&times;</button>
                        </div>
                        <div className="modal-body" style={{ padding: '24px' }}>
                            {/* ESCALATION REASON Section */}
                            <div style={{ marginBottom: '24px' }}>
                                <label style={{
                                    fontSize: 12,
                                    fontWeight: 700,
                                    textTransform: 'uppercase',
                                    letterSpacing: '0.5px',
                                    color: '#1E293B',
                                    marginBottom: '12px',
                                    display: 'block'
                                }}>
                                    ESCALATION REASON
                                </label>
                                <textarea
                                    value={escalationReason}
                                    onChange={(e) => setEscalationReason(e.target.value)}
                                    placeholder="Why do you need to escalate this ticket?"
                                    style={{
                                        width: '100%',
                                        minHeight: 100,
                                        borderRadius: '8px',
                                        border: '1px solid #D1D5DB',
                                        padding: '12px 14px',
                                        fontFamily: 'inherit',
                                        fontSize: '14px',
                                        fontWeight: '400',
                                        color: '#1F2937',
                                        backgroundColor: '#FFFFFF',
                                        resize: 'vertical',
                                        transition: 'all 0.2s ease',
                                        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.06)'
                                    }}
                                    onFocus={(e) => {
                                        e.target.style.borderColor = '#2563EB';
                                        e.target.style.boxShadow = '0 2px 8px rgba(37, 99, 235, 0.1)';
                                    }}
                                    onBlur={(e) => {
                                        e.target.style.borderColor = '#D1D5DB';
                                        e.target.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.06)';
                                    }}
                                />
                            </div>

                            {/* ROUTE ESCALATION TO TEAM LEAD Section */}
                            <div style={{ marginBottom: '24px' }}>
                                <label style={{
                                    fontSize: 12,
                                    fontWeight: 700,
                                    textTransform: 'uppercase',
                                    letterSpacing: '0.5px',
                                    color: '#1E293B',
                                    marginBottom: '12px',
                                    display: 'block'
                                }}>
                                    ROUTE ESCALATION TO TEAM LEAD
                                </label>
                                <select
                                    value={targetTeamLead}
                                    onChange={(e) => setTargetTeamLead(e.target.value)}
                                    style={{
                                        width: '100%',
                                        padding: '12px 36px 12px 14px',
                                        borderRadius: '8px',
                                        border: '1px solid #D1D5DB',
                                        fontSize: '14px',
                                        fontWeight: '500',
                                        fontFamily: 'inherit',
                                        backgroundColor: '#FFFFFF',
                                        cursor: 'pointer',
                                        color: '#374151',
                                        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.06)',
                                        appearance: 'none',
                                        backgroundImage: `url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 20 20' fill='none' stroke='%23374151' stroke-width='1.5'%3e%3cpolyline points='5 7.5 10 12.5 15 7.5'%3e%3c/polyline%3e%3c/svg%3e")`,
                                        backgroundRepeat: 'no-repeat',
                                        backgroundPosition: 'right 10px center',
                                        backgroundSize: '16px',
                                        paddingRight: '36px',
                                        transition: 'all 0.2s ease'
                                    }}
                                    onFocus={(e) => {
                                        e.target.style.borderColor = '#2563EB';
                                        e.target.style.boxShadow = '0 2px 8px rgba(37, 99, 235, 0.1)';
                                    }}
                                    onBlur={(e) => {
                                        e.target.style.borderColor = '#D1D5DB';
                                        e.target.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.06)';
                                    }}
                                    onMouseEnter={(e) => {
                                        if (e.target !== document.activeElement) {
                                            e.target.style.borderColor = '#BFDBFE';
                                            e.target.style.boxShadow = '0 2px 6px rgba(37, 99, 235, 0.1)';
                                        }
                                    }}
                                    onMouseLeave={(e) => {
                                        if (e.target !== document.activeElement) {
                                            e.target.style.borderColor = '#D1D5DB';
                                            e.target.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.06)';
                                        }
                                    }}
                                >
                                    <option value="">Select Team Lead</option>
                                    {teamLeads && teamLeads.length > 0 ? (
                                        teamLeads.map((lead, i) => (
                                            <option key={i} value={lead.name || lead.email || lead.id || lead.userId}>
                                                {lead.name || 'Team Lead'} {lead.email ? `(${lead.email})` : ''} {lead.team || lead.department ? `— ${lead.team || lead.department}` : ''}
                                            </option>
                                        ))
                                    ) : (
                                        <option disabled>No team leads available</option>
                                    )}
                                </select>
                            </div>

                            {/* Info Paragraph Card */}
                            <div style={{
                                background: '#F0F9FF',
                                border: '1px solid #BAE6FD',
                                borderRadius: '8px',
                                padding: '14px 16px',
                                fontSize: 13,
                                color: '#0369A1',
                                lineHeight: 1.6,
                                marginBottom: '0'
                            }}>
                                The escalation will be routed to the selected team lead or auto-assigned based on ticket category.
                            </div>
                        </div>
                        <div className="modal-footer">
                            <button
                                onClick={() => setShowEscalationModal(false)}
                                style={{
                                    padding: '10px 16px',
                                    background: 'linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%)',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '8px',
                                    fontSize: '14px',
                                    fontWeight: '600',
                                    fontFamily: 'inherit',
                                    cursor: 'pointer',
                                    transition: 'all 0.2s ease'
                                }}
                                onMouseEnter={(e) => {
                                    e.target.style.background = 'linear-gradient(135deg, #0F172A 0%, #1B2d48 50%, #0F172A 100%)';
                                    e.target.style.transform = 'translateY(-2px)';
                                    e.target.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
                                }}
                                onMouseLeave={(e) => {
                                    e.target.style.background = 'linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%)';
                                    e.target.style.transform = 'translateY(0)';
                                    e.target.style.boxShadow = 'none';
                                }}
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleEscalation}
                                style={{
                                    padding: '10px 16px',
                                    background: 'linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%)',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '8px',
                                    fontSize: '14px',
                                    fontWeight: '600',
                                    fontFamily: 'inherit',
                                    cursor: 'pointer',
                                    transition: 'all 0.2s ease'
                                }}
                                onMouseEnter={(e) => {
                                    e.target.style.background = 'linear-gradient(135deg, #0F172A 0%, #1B2d48 50%, #0F172A 100%)';
                                    e.target.style.transform = 'translateY(-2px)';
                                    e.target.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
                                }}
                                onMouseLeave={(e) => {
                                    e.target.style.background = 'linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%)';
                                    e.target.style.transform = 'translateY(0)';
                                    e.target.style.boxShadow = 'none';
                                }}
                            >
                                Escalate Ticket
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* KB Search Modal */}
            {showKBModal && (
                <div className="modal-overlay" onClick={(e) => { if (e.target === e.currentTarget) setShowKBModal(false); }}>
                    <div className="modal-content" style={{ maxWidth: '600px' }}>
                        <div className="modal-header">
                            <h2>Search Knowledge Base</h2>
                            <button className="modal-close" onClick={() => setShowKBModal(false)}>&times;</button>
                        </div>
                        <div className="modal-body" style={{ padding: '24px' }}>
                            {/* SEARCH QUERY Section */}
                            <div style={{ marginBottom: '24px' }}>
                                <label style={{
                                    fontSize: 12,
                                    fontWeight: 700,
                                    textTransform: 'uppercase',
                                    letterSpacing: '0.5px',
                                    color: '#1E293B',
                                    marginBottom: '12px',
                                    display: 'block'
                                }}>
                                    SEARCH QUERY
                                </label>
                                <div style={{ display: 'flex', gap: 8 }}>
                                    <input
                                        type="text"
                                        value={kbQuery}
                                        onChange={(e) => setKbQuery(e.target.value)}
                                        placeholder="Search for solutions..."
                                        onKeyDown={(e) => e.key === 'Enter' && handleKBSearch()}
                                        style={{
                                            flex: 1,
                                            padding: '11px 14px',
                                            border: '1px solid #D1D5DB',
                                            borderRadius: '8px',
                                            fontSize: '14px',
                                            fontFamily: 'inherit',
                                            color: '#1F2937',
                                            backgroundColor: '#FFFFFF',
                                            transition: 'all 0.2s ease',
                                            boxShadow: '0 1px 3px rgba(0, 0, 0, 0.06)'
                                        }}
                                        onFocus={(e) => {
                                            e.target.style.borderColor = '#2563EB';
                                            e.target.style.boxShadow = '0 2px 8px rgba(37, 99, 235, 0.1)';
                                        }}
                                        onBlur={(e) => {
                                            e.target.style.borderColor = '#D1D5DB';
                                            e.target.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.06)';
                                        }}
                                    />
                                    <button
                                        onClick={handleKBSearch}
                                        disabled={kbSearching}
                                        style={{
                                            padding: '11px 20px',
                                            backgroundColor: '#1B2A4A',
                                            color: 'white',
                                            border: 'none',
                                            borderRadius: '8px',
                                            fontSize: '14px',
                                            fontWeight: '600',
                                            fontFamily: 'inherit',
                                            cursor: kbSearching ? 'not-allowed' : 'pointer',
                                            opacity: kbSearching ? 0.7 : 1,
                                            transition: 'all 0.2s ease',
                                            minWidth: '100px'
                                        }}
                                    >
                                        {kbSearching ? 'Searching...' : 'Search'}
                                    </button>
                                </div>
                            </div>

                            {/* SOLUTION Section - Results */}
                            {kbResults.length > 0 && (
                                <div style={{ marginBottom: '0' }}>
                                    <label style={{
                                        fontSize: 12,
                                        fontWeight: 700,
                                        textTransform: 'uppercase',
                                        letterSpacing: '0.5px',
                                        color: '#6B7280',
                                        marginBottom: '12px',
                                        display: 'block'
                                    }}>
                                        Solution
                                    </label>
                                    <div style={{
                                        display: 'flex',
                                        flexDirection: 'column',
                                        gap: 12,
                                        maxHeight: '400px',
                                        overflowY: 'auto'
                                    }}>
                                        {kbResults.map((result, i) => (
                                            <div
                                                key={i}
                                                onClick={() => applyKBSolution(result)}
                                                style={{
                                                    border: '1px solid #E5E7EB',
                                                    borderRadius: '8px',
                                                    padding: '12px 16px',
                                                    cursor: 'pointer',
                                                    backgroundColor: '#F9FAFB',
                                                    transition: 'all 0.2s ease',
                                                }}
                                                onMouseEnter={(e) => {
                                                    e.currentTarget.style.backgroundColor = '#F3F4F6';
                                                    e.currentTarget.style.borderColor = '#D1D5DB';
                                                }}
                                                onMouseLeave={(e) => {
                                                    e.currentTarget.style.backgroundColor = '#F9FAFB';
                                                    e.currentTarget.style.borderColor = '#E5E7EB';
                                                }}
                                            >
                                                <div style={{ fontSize: 14, fontWeight: 600, color: '#1E293B', marginBottom: 8 }}>
                                                    {result.issueTitle || result.title || 'Solution'}
                                                </div>
                                                <div style={{ fontSize: 12, color: '#6B7280', marginBottom: 8 }}>
                                                    Category: <span style={{ fontWeight: 500, color: '#1E293B' }}>{result.category || 'N/A'}</span>
                                                </div>
                                                <div style={{ fontSize: 11, color: '#94A3B8' }}>
                                                    Keywords: {result.keywords || 'N/A'}
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}

                            {!kbSearching && kbResults.length === 0 && kbQuery && (
                                <div style={{
                                    padding: '20px',
                                    textAlign: 'center',
                                    backgroundColor: '#F3F4F6',
                                    borderRadius: '8px',
                                    color: '#6B7280',
                                    fontSize: '14px',
                                    border: '1px solid #E5E7EB'
                                }}>
                                    No solutions found for "{kbQuery}"
                                </div>
                            )}
                        </div>
                        <div className="modal-footer">
                            <button
                                className="btn btn-secondary"
                                onClick={() => setShowKBModal(false)}
                                style={{
                                    padding: '10px 16px',
                                    background: 'linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%)',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '8px',
                                    fontSize: '14px',
                                    fontWeight: '600',
                                    cursor: 'pointer',
                                    transition: 'all 0.2s ease'
                                }}
                                onMouseEnter={(e) => {
                                    e.target.style.background = 'linear-gradient(135deg, #0F172A 0%, #1B2d48 50%, #0F172A 100%)';
                                    e.target.style.transform = 'translateY(-2px)';
                                    e.target.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
                                }}
                                onMouseLeave={(e) => {
                                    e.target.style.background = 'linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%)';
                                    e.target.style.transform = 'translateY(0)';
                                    e.target.style.boxShadow = 'none';
                                }}
                            >
                                Close
                            </button>
                        </div>
                    </div>
                </div>
            )}

        </Layout>
    );
}
