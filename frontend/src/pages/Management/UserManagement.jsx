import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout/Layout';
import { isLoggedIn, getManagementData } from '../../utils/auth';
import { showToast, getUserFriendlyMessage } from '../../utils/toastConfig';
import { canManageUsers } from '../../utils/rbac';
import { getTeamsForDepartment } from '../../utils/departmentTeamMap';
import {
    fetchEngineersList,
    fetchManagersList,
    addEngineer,
    updateEngineer,
    deleteEngineer,
    addManager,
    updateManager,
    deleteManager,
    fetchDepartments,
} from '../../utils/api';

export default function UserManagement() {
    const navigate = useNavigate();
    const [userRole, setUserRole] = useState(null);
    const [userDept, setUserDept] = useState(null);

    // Engineers
    const [engineers, setEngineers] = useState([]);
    const [engineersLoading, setEngineersLoading] = useState(false);
    const [showEngineerModal, setShowEngineerModal] = useState(false);
    const [editingEngineer, setEditingEngineer] = useState(null);
    const [engineerForm, setEngineerForm] = useState({ name: '', email: '', password: '', team: 'Network Team' });

    // Managers
    const [managers, setManagers] = useState([]);
    const [managersLoading, setManagersLoading] = useState(false);
    const [showManagerModal, setShowManagerModal] = useState(false);
    const [editingManager, setEditingManager] = useState(null);
    const [managerForm, setManagerForm] = useState({ name: '', username: '', password: '', department: 'NETWORK', email: '' });

    // Delete modal
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [deleteInfo, setDeleteInfo] = useState(null);

    // Departments - fetch from backend
    const [departments, setDepartments] = useState([]);

    // Engineer filters
    const [engineerFilterTab, setEngineerFilterTab] = useState('all');
    const [engineerSearchQuery, setEngineerSearchQuery] = useState('');

    // Manager filters
    const [managerFilterTab, setManagerFilterTab] = useState('all');
    const [managerSearchQuery, setManagerSearchQuery] = useState('');

    // Initialize
    useEffect(() => {
        if (!isLoggedIn()) {
            navigate('/login', { replace: true });
            return;
        }
        const mgmtData = getManagementData();
        if (mgmtData) {
            setUserRole(mgmtData.role);
            setUserDept(mgmtData.department);

            // Verify user has permission to access this page
            if (!canManageUsers(mgmtData.role)) {
                showToast.error('You do not have permission to access this page');
                navigate('/management/dashboard', { replace: true });
                return;
            }

            loadDepartments();
            if (mgmtData.role === 'IT_COORDINATOR') {
                loadManagers();
            } else {
                loadEngineers();
            }
        } else {
            showToast.error('Unable to load user data');
            navigate('/management/dashboard', { replace: true });
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [navigate]);

    const loadEngineers = useCallback(async () => {
        setEngineersLoading(true);
        try {
            const response = await fetchEngineersList();
            if (response.success && response.data) {
                setEngineers(response.data);
            } else {
                showToast.error(response.message || 'Failed to load engineers');
                setEngineers([]);
            }
        } catch (error) {
            showToast.error(getUserFriendlyMessage(error.message));
            setEngineers([]);
        } finally {
            setEngineersLoading(false);
        }
    }, []);

    const loadManagers = useCallback(async () => {
        setManagersLoading(true);
        try {
            const response = await fetchManagersList();
            if (response.success && response.data) {
                setManagers(response.data);
            } else {
                showToast.error(response.message || 'Failed to load managers');
                setManagers([]);
            }
        } catch (error) {
            showToast.error(getUserFriendlyMessage(error.message));
            setManagers([]);
        } finally {
            setManagersLoading(false);
        }
    }, []);

    const loadDepartments = useCallback(async () => {
        try {
            const response = await fetchDepartments();
            if (response.success && response.data) {
                setDepartments(response.data.map(d => d.code));
            } else {
                setDepartments(['NETWORK', 'HARDWARE', 'SOFTWARE', 'EMAIL', 'ACCESS', 'GENERAL']);
            }
        } catch (error) {
            setDepartments(['NETWORK', 'HARDWARE', 'SOFTWARE', 'EMAIL', 'ACCESS', 'GENERAL']);
        }
    }, []);

    // Engineer handlers
    const handleAddEngineer = () => {
        const deptTeams = getTeamsForDepartment(userDept);
        const defaultTeam = deptTeams.length > 0 ? deptTeams[0] : '';
        setEngineerForm({ name: '', email: '', password: '', team: defaultTeam });
        setEditingEngineer(null);
        setShowEngineerModal(true);
    };

    const handleEditEngineer = (engineer) => {
        setEngineerForm({ name: engineer.name, email: engineer.email, password: '', team: engineer.team });
        setEditingEngineer(engineer.id);
        setShowEngineerModal(true);
    };

    const handleSaveEngineer = async () => {
        if (!engineerForm.name || !engineerForm.email || !engineerForm.team) {
            showToast.warning('Please fill all required fields');
            return;
        }
        try {
            let response;
            if (editingEngineer) {
                response = await updateEngineer(editingEngineer, { name: engineerForm.name, email: engineerForm.email, team: engineerForm.team });
            } else {
                if (!engineerForm.password) {
                    showToast.warning('Password required for new engineer');
                    return;
                }
                response = await addEngineer(engineerForm);
            }
            if (response.success) {
                showToast.success(`Engineer ${editingEngineer ? 'updated' : 'added'} successfully`);
                setShowEngineerModal(false);
                await loadEngineers();
            } else {
                showToast.error(response.message || 'Failed to save engineer');
            }
        } catch (error) {
            showToast.error(getUserFriendlyMessage(error.message));
        }
    };

    const handleDeleteEngineer = (id, name) => {
        setDeleteInfo({ type: 'engineer', id, name });
        setShowDeleteModal(true);
    };

    // Manager handlers
    const handleAddManager = () => {
        setManagerForm({ name: '', username: '', password: '', department: 'NETWORK', email: '' });
        setEditingManager(null);
        setShowManagerModal(true);
    };

    const handleEditManager = (manager) => {
        setManagerForm({ name: manager.name, username: manager.username, password: '', department: manager.department, email: manager.email });
        setEditingManager(manager.id);
        setShowManagerModal(true);
    };

    const handleSaveManager = async () => {
        if (!managerForm.name || !managerForm.username || !managerForm.department || !managerForm.email) {
            showToast.warning('Please fill all required fields');
            return;
        }
        try {
            let response;
            if (editingManager) {
                response = await updateManager(editingManager, { name: managerForm.name, username: managerForm.username, department: managerForm.department, email: managerForm.email });
            } else {
                if (!managerForm.password) {
                    showToast.warning('Password required for new manager');
                    return;
                }
                response = await addManager(managerForm);
            }
            if (response.success) {
                showToast.success(`Manager ${editingManager ? 'updated' : 'added'} successfully`);
                setShowManagerModal(false);
                await loadManagers();
            } else {
                showToast.error(response.message || 'Failed to save manager');
            }
        } catch (error) {
            showToast.error(getUserFriendlyMessage(error.message));
        }
    };

    const handleDeleteManager = (id, name) => {
        setDeleteInfo({ type: 'manager', id, name });
        setShowDeleteModal(true);
    };

    const confirmDelete = async () => {
        if (!deleteInfo) return;
        try {
            const response = deleteInfo.type === 'engineer' ? await deleteEngineer(deleteInfo.id) : await deleteManager(deleteInfo.id);
            if (response.success) {
                showToast.success(`${deleteInfo.type === 'engineer' ? 'Engineer' : 'Manager'} deleted`);
                setShowDeleteModal(false);
                setDeleteInfo(null);
                deleteInfo.type === 'engineer' ? await loadEngineers() : await loadManagers();
            } else {
                showToast.error(response.message || 'Failed to delete');
            }
        } catch (error) {
            showToast.error(getUserFriendlyMessage(error.message));
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
                    color: #1E293B;
                    padding: 6px 8px;
                    border-radius: 4px;
                    transition: all 0.3s ease;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    width: 32px;
                    height: 32px;
                }
                .refresh-icon-btn:hover {
                    background: #F3F4F6;
                }
                .refresh-icon-btn.rotating {
                    animation: spin 1s linear infinite;
                }
                @keyframes spin {
                    from { transform: rotate(0deg); }
                    to { transform: rotate(360deg); }
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
                    z-index: 1001;
                }
                .modal {
                    background: white;
                    border-radius: 8px;
                    padding: 24px;
                    max-width: 500px;
                    width: 90%;
                    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
                }
                .modal h3 {
                    margin: 0 0 20px 0;
                    font-size: 16px;
                    font-weight: 700;
                    color: #1F2937;
                }
                .form-group {
                    margin-bottom: 16px;
                }
                .form-group label {
                    display: block;
                    margin-bottom: 6px;
                    font-weight: 500;
                    color: #374151;
                    font-size: 14px;
                }
                .form-group input,
                .form-group select {
                    width: 100%;
                    padding: 10px 12px;
                    border: 1px solid #D1D5DB;
                    border-radius: 6px;
                    font-size: 14px;
                    font-family: inherit;
                }
                .form-group input:focus,
                .form-group select:focus {
                    outline: none;
                    border-color: #1B2A4A;
                    box-shadow: 0 0 0 3px rgba(27, 42, 74, 0.1);
                }
                .form-actions {
                    display: flex;
                    gap: 12px;
                    justify-content: flex-end;
                    margin-top: 24px;
                    padding-top: 16px;
                    border-top: 1px solid #E5E7EB;
                }
                .btn-cancel {
                    background: #E5E7EB;
                    color: #374151;
                    padding: 10px 16px;
                    border: none;
                    border-radius: 6px;
                    cursor: pointer;
                    font-weight: 500;
                    font-size: 14px;
                }
                .btn-cancel:hover {
                    background: #D1D5DB;
                }
                .btn-save {
                    background: linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%);
                    color: white;
                    padding: 10px 16px;
                    border: none;
                    border-radius: 6px;
                    cursor: pointer;
                    font-weight: 500;
                    font-size: 14px;
                }
                .btn-save:hover {
                    background: linear-gradient(135deg, #0F172A 0%, #1B2d48 50%, #0F172A 100%);
                }
                .btn-add {
                    padding: 8px 16px;
                    border: none;
                    border-radius: 6px;
                    font-size: 14px;
                    font-weight: 600;
                    cursor: pointer;
                    transition: all 0.2s ease;
                    font-family: inherit;
                    white-space: nowrap;
                    background: linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%);
                    color: white;
                    flexshrink: 0;
                }
                .btn-add:hover:not(:disabled) {
                    transform: translateY(-2px);
                    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
                    background: linear-gradient(135deg, #0F172A 0%, #1B2d48 50%, #0F172A 100%);
                }
                .icon-btn {
                    background: none;
                    border: none;
                    cursor: pointer;
                    padding: 6px 8px;
                    transition: all 0.2s;
                    border-radius: 4px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    width: 32px;
                    height: 32px;
                    color: #1E293B;
                }
                .icon-btn-edit {
                    color: #1E293B;
                }
                .icon-btn-edit:hover {
                    background: #F3F4F6;
                }
                .icon-btn-delete {
                    color: #1E293B;
                }
                .icon-btn-delete:hover {
                    background: #F3F4F6;
                }
                .action-icons {
                    display: flex;
                    gap: 8px;
                    justify-content: center;
                }
            `}</style>

            <div style={{ padding: '24px 32px' }}>
                {/* PAGE HEADER */}
                <div className="page-header">
                    <div>
                        <h1>User Management</h1>
                        <p>{userRole === 'IT_COORDINATOR' ? 'Manage all department heads across the organization' : `Manage engineers in the ${userDept} department`}</p>
                    </div>
                    <div>
                        <button className="refresh-icon-btn" onClick={() => userRole === 'IT_COORDINATOR' ? loadManagers() : loadEngineers()} title="Refresh users">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                <path d="M23 4v6h-6"></path>
                                <path d="M1 20v-6h6"></path>
                                <path d="M3.51 9a9 9 0 0 1 14.85-3.36M20.49 15a9 9 0 0 1-14.85 3.36"></path>
                            </svg>
                        </button>
                    </div>
                </div>

                {/* ENGINEERS */}
                {userRole === 'DEPARTMENT_HEAD' && (
                    <>
                        <div className="stats-grid">
                            <div className="stat-card">
                                <div className="stat-label">Total Engineers</div>
                                <div className="stat-value">{engineers.length}</div>
                            </div>
                            <div className="stat-card">
                                <div className="stat-label">Active</div>
                                <div className="stat-value">{engineers.filter(e => e.isActive).length}</div>
                            </div>
                            <div className="stat-card">
                                <div className="stat-label">Inactive</div>
                                <div className="stat-value">{engineers.filter(e => !e.isActive).length}</div>
                            </div>
                            <div className="stat-card">
                                <div className="stat-label">Department</div>
                                <div className="stat-value">{userDept}</div>
                            </div>
                        </div>

                        <div className="filters-bar">
                            <div className="filter-tabs">
                                <button
                                    className={`filter-tab ${engineerFilterTab === 'all' ? 'active' : ''}`}
                                    onClick={() => setEngineerFilterTab('all')}
                                >
                                    All <span className="tab-count">{engineers.length}</span>
                                </button>
                                <button
                                    className={`filter-tab ${engineerFilterTab === 'active' ? 'active' : ''}`}
                                    onClick={() => setEngineerFilterTab('active')}
                                >
                                    Active <span className="tab-count">{engineers.filter(e => e.isActive).length}</span>
                                </button>
                                <button
                                    className={`filter-tab ${engineerFilterTab === 'inactive' ? 'active' : ''}`}
                                    onClick={() => setEngineerFilterTab('inactive')}
                                >
                                    Inactive <span className="tab-count">{engineers.filter(e => !e.isActive).length}</span>
                                </button>
                                <button
                                    className={`filter-tab ${engineerFilterTab === 'engineer' ? 'active' : ''}`}
                                    onClick={() => setEngineerFilterTab('engineer')}
                                >
                                    Role: Engineer <span className="tab-count">{engineers.filter(e => e.role === 'ENGINEER').length}</span>
                                </button>
                                <button
                                    className={`filter-tab ${engineerFilterTab === 'manager' ? 'active' : ''}`}
                                    onClick={() => setEngineerFilterTab('manager')}
                                >
                                    Role: Manager <span className="tab-count">{engineers.filter(e => e.role === 'MANAGER').length}</span>
                                </button>
                            </div>
                            <div className="filter-actions">
                                <input
                                    type="text"
                                    className="search-input"
                                    placeholder="Search engineers..."
                                    value={engineerSearchQuery}
                                    onChange={(e) => setEngineerSearchQuery(e.target.value)}
                                    style={{ width: 280, flexShrink: 0 }}
                                />
                                <button className="btn-add" onClick={handleAddEngineer} style={{ flexShrink: 0 }}>+ Add Engineer</button>
                            </div>
                        </div>

                        <div className="card">
                            <div className="card-header">
                                <h2 style={{ color: '#1E293B', margin: 0 }}>Engineers <span style={{ background: 'linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%)', color: 'white', padding: '3px 10px', borderRadius: 12, fontSize: 12, marginLeft: 8 }}>{engineers.length}</span></h2>
                            </div>
                            {engineersLoading ? (
                                <div style={{ padding: '40px', textAlign: 'center', color: '#6B7280' }}>
                                    <div style={{ fontSize: 14 }}>Loading engineers...</div>
                                </div>
                            ) : engineers.length === 0 ? (
                                <div className="empty-state">
                                    <h3>No engineers found</h3>
                                    <p>No engineers assigned to your department</p>
                                </div>
                            ) : (
                                <div style={{ overflowX: 'auto' }}>
                                    <table className="data-table">
                                        <thead>
                                            <tr>
                                                <th style={{ width: 200, textAlign: 'center' }}>Name</th>
                                                <th style={{ width: 200, textAlign: 'center' }}>Email</th>
                                                <th style={{ width: 150, textAlign: 'center' }}>Team</th>
                                                <th style={{ width: 100, textAlign: 'center' }}>Status</th>
                                                <th style={{ width: 120, textAlign: 'center' }}>Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {(() => {
                                                let filtered = engineers;

                                                // Apply tab filter
                                                if (engineerFilterTab === 'active') {
                                                    filtered = filtered.filter(e => e.isActive);
                                                } else if (engineerFilterTab === 'inactive') {
                                                    filtered = filtered.filter(e => !e.isActive);
                                                } else if (engineerFilterTab === 'engineer') {
                                                    filtered = filtered.filter(e => e.role === 'ENGINEER');
                                                } else if (engineerFilterTab === 'manager') {
                                                    filtered = filtered.filter(e => e.role === 'MANAGER');
                                                }

                                                // Apply search filter
                                                if (engineerSearchQuery.trim()) {
                                                    const q = engineerSearchQuery.toLowerCase();
                                                    filtered = filtered.filter(e =>
                                                        (e.name && e.name.toLowerCase().includes(q)) ||
                                                        (e.email && e.email.toLowerCase().includes(q))
                                                    );
                                                }

                                                return filtered.map((eng) => (
                                                    <tr key={eng.id}>
                                                        <td style={{ width: 200, textAlign: 'center', fontWeight: 500 }}>{eng.name}</td>
                                                        <td style={{ width: 200, textAlign: 'center', fontSize: 13 }}>{eng.email}</td>
                                                        <td style={{ width: 150, textAlign: 'center' }}>{eng.team}</td>
                                                        <td style={{ width: 100, textAlign: 'center' }}><span className={`badge ${eng.isActive ? 'badge-good' : 'badge-poor'}`}>{eng.isActive ? 'Active' : 'Inactive'}</span></td>
                                                        <td style={{ width: 120, textAlign: 'center' }}>
                                                            <div className="action-icons">
                                                                <button className="icon-btn icon-btn-edit" onClick={() => handleEditEngineer(eng)} title="Edit">
                                                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                                                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                                                                        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                                                                    </svg>
                                                                </button>
                                                                <button className="icon-btn icon-btn-delete" onClick={() => handleDeleteEngineer(eng.id, eng.name)} title="Delete">
                                                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                                                        <polyline points="3 6 5 6 21 6"></polyline>
                                                                        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                                                                        <line x1="10" y1="11" x2="10" y2="17"></line>
                                                                        <line x1="14" y1="11" x2="14" y2="17"></line>
                                                                    </svg>
                                                                </button>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                ));
                                            })()}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </div>

                        {showEngineerModal && (
                            <div className="modal-overlay" onClick={() => setShowEngineerModal(false)}>
                                <div className="modal" onClick={(e) => e.stopPropagation()}>
                                    <h3>{editingEngineer ? 'Edit Engineer' : 'Add Engineer'}</h3>
                                    <form onSubmit={(e) => { e.preventDefault(); handleSaveEngineer(); }}>
                                        <div className="form-group">
                                            <label>Name *</label>
                                            <input type="text" value={engineerForm.name} onChange={(e) => setEngineerForm({ ...engineerForm, name: e.target.value })} placeholder="Engineer name" />
                                        </div>
                                        <div className="form-group">
                                            <label>Email *</label>
                                            <input type="email" value={engineerForm.email} onChange={(e) => setEngineerForm({ ...engineerForm, email: e.target.value })} placeholder="engineer@example.com" />
                                        </div>
                                        <div className="form-group">
                                            <label>Team * <span style={{ fontSize: '12px', color: '#6B7280' }}>(Read-only - Department {userDept})</span></label>
                                            <select value={engineerForm.team} disabled style={{ opacity: editingEngineer ? 1 : 0.7, cursor: 'not-allowed', backgroundColor: '#F3F4F6' }}>
                                                {getTeamsForDepartment(userDept).map((team) => (
                                                    <option key={team.id} value={team.name}>{team.name}</option>
                                                ))}
                                            </select>
                                        </div>
                                        {!editingEngineer && (
                                            <div className="form-group">
                                                <label>Password *</label>
                                                <input type="password" value={engineerForm.password} onChange={(e) => setEngineerForm({ ...engineerForm, password: e.target.value })} placeholder="Password" />
                                            </div>
                                        )}
                                        <div className="form-actions">
                                            <button type="button" className="btn-cancel" onClick={() => setShowEngineerModal(false)}>Cancel</button>
                                            <button type="submit" className="btn-save">{editingEngineer ? 'Update' : 'Add'}</button>
                                        </div>
                                    </form>
                                </div>
                            </div>
                        )}
                    </>
                )}

                {/* MANAGERS */}
                {userRole === 'IT_COORDINATOR' && (
                    <>
                        <div className="stats-grid">
                            <div className="stat-card">
                                <div className="stat-label">Total Managers</div>
                                <div className="stat-value">{managers.length}</div>
                            </div>
                            <div className="stat-card">
                                <div className="stat-label">Departments</div>
                                <div className="stat-value">{new Set(managers.map(m => m.department)).size}</div>
                            </div>
                            <div className="stat-card">
                                <div className="stat-label">Coverage</div>
                                <div className="stat-value">100%</div>
                            </div>
                            <div className="stat-card">
                                <div className="stat-label">Status</div>
                                <div className="stat-value">Active</div>
                            </div>
                        </div>

                        <div className="filters-bar">
                            <div className="filter-tabs">
                                <button
                                    className={`filter-tab ${managerFilterTab === 'all' ? 'active' : ''}`}
                                    onClick={() => setManagerFilterTab('all')}
                                >
                                    All <span className="tab-count">{managers.length}</span>
                                </button>
                                <button
                                    className={`filter-tab ${managerFilterTab === 'active' ? 'active' : ''}`}
                                    onClick={() => setManagerFilterTab('active')}
                                >
                                    Active <span className="tab-count">{managers.filter(m => m.isActive).length}</span>
                                </button>
                                <button
                                    className={`filter-tab ${managerFilterTab === 'inactive' ? 'active' : ''}`}
                                    onClick={() => setManagerFilterTab('inactive')}
                                >
                                    Inactive <span className="tab-count">{managers.filter(m => !m.isActive).length}</span>
                                </button>
                                <button
                                    className={`filter-tab ${managerFilterTab === 'engineer' ? 'active' : ''}`}
                                    onClick={() => setManagerFilterTab('engineer')}
                                >
                                    Role: Engineer <span className="tab-count">{managers.filter(m => m.role === 'ENGINEER').length}</span>
                                </button>
                                <button
                                    className={`filter-tab ${managerFilterTab === 'manager' ? 'active' : ''}`}
                                    onClick={() => setManagerFilterTab('manager')}
                                >
                                    Role: Manager <span className="tab-count">{managers.filter(m => m.role === 'MANAGER').length}</span>
                                </button>
                            </div>
                            <div className="filter-actions">
                                <input
                                    type="text"
                                    className="search-input"
                                    placeholder="Search managers..."
                                    value={managerSearchQuery}
                                    onChange={(e) => setManagerSearchQuery(e.target.value)}
                                    style={{ width: 280, flexShrink: 0 }}
                                />
                                <button className="btn-add" onClick={handleAddManager} style={{ flexShrink: 0 }}>+ Add Manager</button>
                            </div>
                        </div>

                        <div className="card">
                            <div className="card-header">
                                <h2 style={{ color: '#1E293B', margin: 0 }}>Department Heads <span style={{ background: 'linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%)', color: 'white', padding: '3px 10px', borderRadius: 12, fontSize: 12, marginLeft: 8 }}>{managers.length}</span></h2>
                            </div>
                            {managersLoading ? (
                                <div style={{ padding: '40px', textAlign: 'center', color: '#6B7280' }}>
                                    <div style={{ fontSize: 14 }}>Loading managers...</div>
                                </div>
                            ) : managers.length === 0 ? (
                                <div className="empty-state">
                                    <h3>No managers found</h3>
                                    <p>No department heads found</p>
                                </div>
                            ) : (
                                <div style={{ overflowX: 'auto' }}>
                                    <table className="data-table">
                                        <thead>
                                            <tr>
                                                <th style={{ width: 180, textAlign: 'center' }}>Name</th>
                                                <th style={{ width: 150, textAlign: 'center' }}>Username</th>
                                                <th style={{ width: 200, textAlign: 'center' }}>Email</th>
                                                <th style={{ width: 150, textAlign: 'center' }}>Department</th>
                                                <th style={{ width: 120, textAlign: 'center' }}>Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {(() => {
                                                let filtered = managers;

                                                // Apply tab filter
                                                if (managerFilterTab === 'active') {
                                                    filtered = filtered.filter(m => m.isActive);
                                                } else if (managerFilterTab === 'inactive') {
                                                    filtered = filtered.filter(m => !m.isActive);
                                                } else if (managerFilterTab === 'engineer') {
                                                    filtered = filtered.filter(m => m.role === 'ENGINEER');
                                                } else if (managerFilterTab === 'manager') {
                                                    filtered = filtered.filter(m => m.role === 'MANAGER');
                                                }

                                                // Apply search filter
                                                if (managerSearchQuery.trim()) {
                                                    const q = managerSearchQuery.toLowerCase();
                                                    filtered = filtered.filter(m =>
                                                        (m.name && m.name.toLowerCase().includes(q)) ||
                                                        (m.email && m.email.toLowerCase().includes(q)) ||
                                                        (m.username && m.username.toLowerCase().includes(q))
                                                    );
                                                }

                                                return filtered.map((mgr) => (
                                                    <tr key={mgr.id}>
                                                        <td style={{ width: 180, textAlign: 'center', fontWeight: 500 }}>{mgr.name}</td>
                                                        <td style={{ width: 150, textAlign: 'center' }}>{mgr.username}</td>
                                                        <td style={{ width: 200, textAlign: 'center', fontSize: 13 }}>{mgr.email}</td>
                                                        <td style={{ width: 150, textAlign: 'center' }}>{mgr.department}</td>
                                                        <td style={{ width: 120, textAlign: 'center' }}>
                                                            <div className="action-icons">
                                                                <button className="icon-btn icon-btn-edit" onClick={() => handleEditManager(mgr)} title="Edit">
                                                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                                                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                                                                        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                                                                    </svg>
                                                                </button>
                                                                <button className="icon-btn icon-btn-delete" onClick={() => handleDeleteManager(mgr.id, mgr.name)} title="Delete">
                                                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                                                        <polyline points="3 6 5 6 21 6"></polyline>
                                                                        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                                                                        <line x1="10" y1="11" x2="10" y2="17"></line>
                                                                        <line x1="14" y1="11" x2="14" y2="17"></line>
                                                                    </svg>
                                                                </button>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                ));
                                            })()}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </div>

                        {showManagerModal && (
                            <div className="modal-overlay" onClick={() => setShowManagerModal(false)}>
                                <div className="modal" onClick={(e) => e.stopPropagation()}>
                                    <h3>{editingManager ? 'Edit Manager' : 'Add Manager'}</h3>
                                    <form onSubmit={(e) => { e.preventDefault(); handleSaveManager(); }}>
                                        <div className="form-group">
                                            <label>Name *</label>
                                            <input type="text" value={managerForm.name} onChange={(e) => setManagerForm({ ...managerForm, name: e.target.value })} placeholder="Manager name" />
                                        </div>
                                        <div className="form-group">
                                            <label>Username *</label>
                                            <input type="text" value={managerForm.username} onChange={(e) => setManagerForm({ ...managerForm, username: e.target.value })} placeholder="Username" />
                                        </div>
                                        <div className="form-group">
                                            <label>Email *</label>
                                            <input type="email" value={managerForm.email} onChange={(e) => setManagerForm({ ...managerForm, email: e.target.value })} placeholder="manager@example.com" />
                                        </div>
                                        <div className="form-group">
                                            <label>Department *</label>
                                            <select value={managerForm.department} onChange={(e) => setManagerForm({ ...managerForm, department: e.target.value })}>
                                                {departments.map((d) => <option key={d} value={d}>{d}</option>)}
                                            </select>
                                        </div>
                                        {!editingManager && (
                                            <div className="form-group">
                                                <label>Password *</label>
                                                <input type="password" value={managerForm.password} onChange={(e) => setManagerForm({ ...managerForm, password: e.target.value })} placeholder="Password" />
                                            </div>
                                        )}
                                        <div className="form-actions">
                                            <button type="button" className="btn-cancel" onClick={() => setShowManagerModal(false)}>Cancel</button>
                                            <button type="submit" className="btn-save">{editingManager ? 'Update' : 'Add'}</button>
                                        </div>
                                    </form>
                                </div>
                            </div>
                        )}
                    </>
                )}

                {/* DELETE CONFIRMATION */}
                {showDeleteModal && deleteInfo && (
                    <div className="modal-overlay" onClick={() => setShowDeleteModal(false)}>
                        <div className="modal" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 400 }}>
                            <h3 style={{ marginBottom: 20, textAlign: 'center' }}>Confirm Delete</h3>
                            <p style={{ textAlign: 'center', marginBottom: 24, color: '#6B7280' }}>
                                Delete {deleteInfo.type === 'engineer' ? 'engineer' : 'manager'} "<strong>{deleteInfo.name}</strong>"? This action cannot be undone.
                            </p>
                            <div className="form-actions" style={{ justifyContent: 'center', gap: 12, borderTop: 'none', marginTop: 0, paddingTop: 0 }}>
                                <button type="button" className="btn-cancel" onClick={() => setShowDeleteModal(false)} style={{ minWidth: 100 }}>Cancel</button>
                                <button type="button" className="btn-save" onClick={confirmDelete} style={{ minWidth: 100, background: '#DC2626', borderRadius: '6px' }} onMouseEnter={(e) => e.target.style.background = '#B91C1C'} onMouseLeave={(e) => e.target.style.background = '#DC2626'}>Delete</button>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </Layout>
    );
}
