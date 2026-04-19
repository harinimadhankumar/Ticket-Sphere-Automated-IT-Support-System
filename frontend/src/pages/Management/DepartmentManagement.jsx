import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout/Layout';
import { isLoggedIn, getManagementData } from '../../utils/auth';
import { fetchDepartments, addDepartment, updateDepartment, deleteDepartment } from '../../utils/api';
import { showToast, getUserFriendlyMessage } from '../../utils/toastConfig';

export default function DepartmentManagement() {
    const navigate = useNavigate();

    const [departments, setDepartments] = useState([]);

    const [showModal, setShowModal] = useState(false);
    const [editingDept, setEditingDept] = useState(null);
    const [deptForm, setDeptForm] = useState({ code: '', teamName: '', shortName: '' });

    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [deleteInfo, setDeleteInfo] = useState(null);
    const [searchQuery, setSearchQuery] = useState('');

    // Initialize
    useEffect(() => {
        if (!isLoggedIn()) {
            navigate('/login', { replace: true });
            return;
        }

        const mgmtData = getManagementData();
        if (mgmtData) {
            // Only IT_COORDINATOR can access
            if (mgmtData.role !== 'IT_COORDINATOR') {
                showToast.error('Only IT Coordinators can manage departments');
                navigate('/management/dashboard', { replace: true });
                return;
            }
        } else {
            showToast.error('Unable to load user data');
            navigate('/management/dashboard', { replace: true });
        }

        // Load departments from backend
        loadDepartments();
    }, [navigate]);

    const loadDepartments = async () => {
        try {
            const data = await fetchDepartments();
            if (data.success && data.data) {
                setDepartments(data.data);
            } else {
                showToast.error('Failed to load departments');
            }
        } catch (error) {
            showToast.error(getUserFriendlyMessage(error.message));
        } finally {
        }
    };

    const handleAddDept = () => {
        setDeptForm({ code: '', teamName: '', shortName: '' });
        setEditingDept(null);
        setShowModal(true);
    };

    const handleEditDept = (dept) => {
        setDeptForm({ code: dept.code, teamName: dept.teamName, shortName: dept.shortName });
        setEditingDept(dept.code);
        setShowModal(true);
    };

    const handleSaveDept = async () => {
        if (!deptForm.code || !deptForm.teamName || !deptForm.shortName) {
            showToast.warning('Please fill all required fields');
            return;
        }

        try {
            if (editingDept) {
                const result = await updateDepartment(editingDept, {
                    teamName: deptForm.teamName,
                    shortName: deptForm.shortName
                });
                if (result.success) {
                    setDepartments(departments.map(d =>
                        d.code === editingDept
                            ? { code: deptForm.code, name: deptForm.code, teamName: deptForm.teamName, shortName: deptForm.shortName }
                            : d
                    ));
                    showToast.success('Department updated successfully');
                } else {
                    showToast.error(result.error || 'Failed to update department');
                }
            } else {
                const result = await addDepartment({
                    code: deptForm.code.toUpperCase(),
                    teamName: deptForm.teamName,
                    shortName: deptForm.shortName
                });
                if (result.success) {
                    setDepartments([...departments, result.data]);
                    showToast.success('Department added successfully');
                } else {
                    showToast.error(result.error || 'Failed to add department');
                }
            }
            setShowModal(false);
        } catch (error) {
            console.error('Error saving department:', error);
            showToast.error(getUserFriendlyMessage(error.message));
        }
    };

    const handleDeleteDept = (code) => {
        setDeleteInfo(code);
        setShowDeleteModal(true);
    };

    const confirmDelete = async () => {
        try {
            const result = await deleteDepartment(deleteInfo);
            if (result.success) {
                setDepartments(departments.filter(d => d.code !== deleteInfo));
                showToast.success('Department deleted successfully');
            } else {
                showToast.error(result.error || 'Failed to delete department');
            }
        } catch (error) {
            console.error('Error deleting department:', error);
            showToast.error(getUserFriendlyMessage(error.message));
        } finally {
            setShowDeleteModal(false);
            setDeleteInfo(null);
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
                .form-group input {
                    width: 100%;
                    padding: 10px 12px;
                    border: 1px solid #D1D5DB;
                    border-radius: 6px;
                    font-size: 14px;
                    font-family: inherit;
                    box-sizing: border-box;
                }
                .form-group input:focus {
                    outline: none;
                    border-color: #1B2A4A;
                    box-shadow: 0 0 0 3px rgba(27, 42, 74, 0.1);
                }
                .form-group input:disabled {
                    background: #F3F4F6;
                    color: #6B7280;
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
                        <h1>Department Management</h1>
                        <p>Manage IT departments and assign them to managers</p>
                    </div>
                    <div>
                        <button className="refresh-icon-btn" title="Refresh" onClick={loadDepartments}>
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                <path d="M23 4v6h-6"></path>
                                <path d="M1 20v-6h6"></path>
                                <path d="M3.51 9a9 9 0 0 1 14.85-3.36M20.49 15a9 9 0 0 1-14.85 3.36"></path>
                            </svg>
                        </button>
                    </div>
                </div>

                {/* STATS GRID */}
                <div className="stats-grid">
                    <div className="stat-card">
                        <div className="stat-label">Total Departments</div>
                        <div className="stat-value">{departments.length}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Active</div>
                        <div className="stat-value">{departments.length}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Teams</div>
                        <div className="stat-value">{departments.length}</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-label">Status</div>
                        <div className="stat-value">Active</div>
                    </div>
                </div>

                {/* FILTERS BAR */}
                <div className="filters-bar">
                    <div className="filter-tabs">
                        <button className="filter-tab active">All <span className="tab-count">{departments.length}</span></button>
                    </div>
                    <div className="filter-actions">
                        <input
                            type="text"
                            className="search-input"
                            placeholder="Search departments..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            style={{ width: 280, flexShrink: 0 }}
                        />
                        <button className="btn-add" onClick={handleAddDept} style={{ flexShrink: 0 }}>+ Add Department</button>
                    </div>
                </div>

                {/* DEPARTMENTS TABLE */}
                <div className="card">
                    <div className="card-header">
                        <h2 style={{ color: '#1E293B', margin: 0 }}>Departments <span style={{ background: 'linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%)', color: 'white', padding: '3px 10px', borderRadius: 12, fontSize: 12, marginLeft: 8 }}>{departments.length}</span></h2>
                    </div>
                    <div style={{ overflowX: 'auto' }}>
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th style={{ width: 150, textAlign: 'center' }}>Code</th>
                                    <th style={{ width: 200, textAlign: 'center' }}>Team Name</th>
                                    <th style={{ width: 200, textAlign: 'center' }}>Short Name</th>
                                    <th style={{ width: 120, textAlign: 'center' }}>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {departments.filter(dept => {
                                    const search = searchQuery.toLowerCase();
                                    return (
                                        dept.code.toLowerCase().includes(search) ||
                                        dept.teamName.toLowerCase().includes(search) ||
                                        dept.shortName.toLowerCase().includes(search)
                                    );
                                }).map((dept) => (
                                    <tr key={dept.code}>
                                        <td style={{ width: 150, textAlign: 'center', fontWeight: 500 }}>{dept.code}</td>
                                        <td style={{ width: 200, textAlign: 'center' }}>{dept.teamName}</td>
                                        <td style={{ width: 200, textAlign: 'center' }}>{dept.shortName}</td>
                                        <td style={{ width: 120, textAlign: 'center' }}>
                                            <div className="action-icons">
                                                <button className="icon-btn icon-btn-edit" onClick={() => handleEditDept(dept)} title="Edit">
                                                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                                                        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                                                    </svg>
                                                </button>
                                                <button className="icon-btn icon-btn-delete" onClick={() => handleDeleteDept(dept.code)} title="Delete">
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
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>

                {/* ADD/EDIT MODAL */}
                {showModal && (
                    <div className="modal-overlay" onClick={() => setShowModal(false)}>
                        <div className="modal" onClick={(e) => e.stopPropagation()}>
                            <h3>{editingDept ? 'Edit Department' : 'Add Department'}</h3>
                            <form onSubmit={(e) => { e.preventDefault(); handleSaveDept(); }}>
                                <div className="form-group">
                                    <label>Department Code * {editingDept && '(Read-only)'}</label>
                                    <input
                                        type="text"
                                        value={deptForm.code}
                                        onChange={(e) => setDeptForm({ ...deptForm, code: e.target.value.toUpperCase() })}
                                        placeholder="e.g., NETWORK"
                                        disabled={!!editingDept}
                                    />
                                </div>
                                <div className="form-group">
                                    <label>Team Name *</label>
                                    <input
                                        type="text"
                                        value={deptForm.teamName}
                                        onChange={(e) => setDeptForm({ ...deptForm, teamName: e.target.value })}
                                        placeholder="e.g., Network Team"
                                    />
                                </div>
                                <div className="form-group">
                                    <label>Short Name *</label>
                                    <input
                                        type="text"
                                        value={deptForm.shortName}
                                        onChange={(e) => setDeptForm({ ...deptForm, shortName: e.target.value })}
                                        placeholder="e.g., Network Support"
                                    />
                                </div>
                                <div className="form-actions">
                                    <button type="button" className="btn-cancel" onClick={() => setShowModal(false)}>Cancel</button>
                                    <button type="submit" className="btn-save">{editingDept ? 'Update' : 'Add'}</button>
                                </div>
                            </form>
                        </div>
                    </div>
                )}

                {/* DELETE CONFIRMATION */}
                {showDeleteModal && deleteInfo && (
                    <div className="modal-overlay" onClick={() => setShowDeleteModal(false)}>
                        <div className="modal" onClick={(e) => e.stopPropagation()} style={{ maxWidth: 400 }}>
                            <h3 style={{ marginBottom: 20, textAlign: 'center' }}>Confirm Delete</h3>
                            <p style={{ textAlign: 'center', marginBottom: 24, color: '#6B7280' }}>
                                Delete department "<strong>{deleteInfo}</strong>"? This action cannot be undone.
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
