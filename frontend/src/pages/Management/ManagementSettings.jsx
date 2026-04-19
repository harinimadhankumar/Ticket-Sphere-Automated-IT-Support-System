import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import Layout from '../../components/Layout/Layout';
import { isLoggedIn, getManagementData, getInitials } from '../../utils/auth';
import { showToast, getUserFriendlyMessage } from '../../utils/toastConfig';
import { updateManagementProfile } from '../../utils/api';

export default function ManagementSettings() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const [activeTab, setActiveTab] = useState(searchParams.get('tab') || 'profile');
    const [formData, setFormData] = useState({});
    const [saving, setSaving] = useState(false);
    const [editMode, setEditMode] = useState(false);
    const [preferences, setPreferences] = useState({
        emailDailyReport: true,
        slaAlerts: true,
        escalationNotifications: true,
        weeklyMetrics: true,
        theme: 'light'
    });

    useEffect(() => {
        if (!isLoggedIn()) { navigate('/login', { replace: true }); return; }

        const managementData = getManagementData();
        if (managementData) {
            setFormData(managementData);
        } else {
            const userData = sessionStorage.getItem('userData');
            if (userData) {
                try {
                    const parsed = JSON.parse(userData);
                    setFormData({
                        name: parsed.name || parsed.userName || 'Management User',
                        email: parsed.email || '',
                        department: parsed.department || 'Management',
                        role: parsed.role || 'Manager',
                        phone: parsed.phone || '',
                    });
                } catch (e) {
                    setFormData({
                        name: 'Management User',
                        email: '',
                        department: 'Management',
                        role: 'Manager',
                        phone: '',
                    });
                }
            }
        }
    }, [navigate]);

    const handleFieldChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSaveProfile = async () => {
        if (!formData.name || !formData.email) {
            showToast.warning('Please fill in all required fields');
            return;
        }
        setSaving(true);
        try {
            const response = await updateManagementProfile({
                name: formData.name,
                email: formData.email
            });

            if (response.success) {
                sessionStorage.setItem('managementData', JSON.stringify(response.management));
                setFormData(response.management);
                setEditMode(false);
                showToast.success('✓ Profile updated successfully');
            } else {
                showToast.error(getUserFriendlyMessage(response.message || 'Failed to update profile'));
            }
        } catch (error) {
            const friendlyMessage = getUserFriendlyMessage(error.message);
            showToast.error(friendlyMessage);
        } finally {
            setSaving(false);
        }
    };

    const handleCancel = () => {
        const managementData = getManagementData();
        if (managementData) {
            setFormData(managementData);
        }
        setEditMode(false);
    };

    const handleEditClick = () => {
        setEditMode(true);
    };

    const handlePreferenceChange = (prefKey) => {
        setPreferences(prev => ({
            ...prev,
            [prefKey]: !prev[prefKey]
        }));
    };


    return (
        <Layout showNav={true} showUser={true} showSidebar={true} showSecondaryNav={true} >
            <style>{`
                * { box-sizing: border-box; }
                html, body { margin: 0; padding: 0; overflow-x: hidden; }

                .settings-main-container {
                    min-height: calc(100vh - 176px);
                    padding: 32px;
                    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
                    display: flex;
                    flex-direction: column;
                }

                .page-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 32px;
                    padding-bottom: 20px;
                    border-bottom: 2px solid #1E293B;
                    animation: fadeInDown 0.3s ease;
                }

                .page-header h1 {
                    font-size: 28px;
                    font-weight: 700;
                    color: #1E293B;
                    margin: 0 0 8px 0;
                }

                .page-header p {
                    font-size: 14px;
                    color: #1E293B;
                    margin: 0;
                }

                .settings-tabs {
                    display: flex;
                    gap: 16px;
                    margin-bottom: 24px;
                }

                .settings-tab {
                    padding: 12px 16px;
                    background: transparent;
                    border: none;
                    border-bottom: 3px solid transparent;
                    color: #1E293B;
                    font-weight: 500;
                    cursor: pointer;
                    transition: all 0.2s ease;
                    font-family: inherit;
                    font-size: 14px;
                }

                .settings-tab:hover {
                    color: #1E293B;
                }

                .settings-tab.active {
                    color: #1E293B;
                    border-bottom-color: #1E293B;
                }

                .profile-card {
                    background: white;
                    border-radius: 12px;
                    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
                    overflow: hidden;
                    animation: fadeInUp 0.4s ease;
                }

                .profile-card-header {
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    padding: 20px;
                    border-bottom: 1px solid #E5E7EB;
                    background: #F9FAFB;
                    gap: 12px;
                }

                .profile-card-title {
                    font-size: 18px;
                    font-weight: 600;
                    color: #1E293B;
                    margin: 0;
                    flex: 1;
                }

                .profile-actions {
                    display: flex;
                    gap: 12px;
                    flex-wrap: wrap;
                }

                .profile-btn {
                    padding: 8px 16px;
                    border: none;
                    border-radius: 6px;
                    font-size: 14px;
                    font-weight: 600;
                    cursor: pointer;
                    transition: all 0.2s ease;
                    font-family: inherit;
                    white-space: nowrap;
                }

                .profile-btn:hover:not(:disabled) {
                    transform: translateY(-2px);
                    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
                }

                .btn-edit {
                    background: linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%);
                    color: white;
                }

                .btn-edit:hover {
                    background: linear-gradient(135deg, #0F172A 0%, #1B2d48 50%, #0F172A 100%);
                }

                .btn-save {
                    background: linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%);
                    color: white;
                }

                .btn-save:hover {
                    background: linear-gradient(135deg, #0F172A 0%, #1B2d48 50%, #0F172A 100%);
                }

                .btn-save:disabled {
                    background: #CBD5E1;
                    cursor: not-allowed;
                }

                .btn-cancel {
                    background: linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%);
                    color: white;
                }

                .btn-cancel:hover {
                    background: linear-gradient(135deg, #0F172A 0%, #1B2d48 50%, #0F172A 100%);
                }

                .profile-card-body {
                    padding: 24px;
                    animation: fadeIn 0.4s ease 0.1s both;
                }

                .profile-avatar-section {
                    display: flex;
                    align-items: center;
                    gap: 16px;
                    margin-bottom: 24px;
                    padding-bottom: 24px;
                    border-bottom: 1px solid #E5E7EB;
                }

                .profile-avatar {
                    width: 90px;
                    height: 90px;
                    border-radius: 50%;
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    color: white;
                    font-size: 36px;
                    font-weight: 700;
                    flex-shrink: 0;
                    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
                    animation: scaleIn 0.4s ease;
                }

                .profile-avatar-info h2 {
                    font-size: 17px;
                    font-weight: 700;
                    color: #1E293B;
                    margin: 0 0 6px 0;
                }

                .profile-avatar-info p {
                    font-size: 14px;
                    color: #1E293B;
                    margin: 0 0 6px 0;
                }

                .status-badge {
                    display: inline-block;
                    padding: 4px 12px;
                    border-radius: 20px;
                    font-size: 12px;
                    font-weight: 600;
                    background: linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%);
                    color: white;
                    text-transform: capitalize;
                    margin-top: 6px;
                }

                .profile-form-section {
                    margin-bottom: 24px;
                }

                .profile-form-section:last-child {
                    margin-bottom: 0;
                }

                .profile-section-title {
                    font-size: 15px;
                    font-weight: 600;
                    color: #1E293B;
                    margin: 0 0 12px 0;
                    padding-bottom: 10px;
                    border-bottom: 2px solid #E5E7EB;
                }

                .profile-form-grid {
                    display: grid;
                    grid-template-columns: 1fr 1fr;
                    gap: 20px;
                }

                .profile-form-group {
                    display: flex;
                    flex-direction: column;
                }

                .profile-form-group.full {
                    grid-column: 1 / -1;
                }

                .profile-label {
                    font-size: 13px;
                    font-weight: 600;
                    color: #1E293B;
                    margin-bottom: 8px;
                    text-transform: uppercase;
                    letter-spacing: 0.5px;
                }

                .profile-value {
                    font-size: 14px;
                    color: #1E293B;
                    padding: 12px;
                    background: #F9FAFB;
                    border-radius: 6px;
                    border: 1px solid #E5E7EB;
                    font-weight: 500;
                }

                .profile-input {
                    font-size: 14px;
                    color: #1E293B;
                    padding: 12px;
                    border: 1px solid #D1D5DB;
                    border-radius: 6px;
                    font-family: inherit;
                    background: white;
                }

                .profile-input:focus {
                    outline: none;
                    border-color: #1E293B;
                    box-shadow: 0 0 0 3px rgba(30, 41, 59, 0.1);
                }

                .notifications-grid {
                    display: grid;
                    grid-template-columns: 1fr 1fr;
                    gap: 32px;
                    align-items: flex-start;
                }

                .notifications-list {
                    display: flex;
                    flex-direction: column;
                    gap: 16px;
                }

                .notification-item {
                    font-size: 14px;
                    color: #1E293B;
                    font-weight: 500;
                }

                .notifications-checkboxes {
                    display: flex;
                    flex-direction: column;
                    gap: 16px;
                    align-items: center;
                    justify-content: flex-start;
                }

                .checkbox-wrapper {
                    display: flex;
                    align-items: center;
                    width: 100%;
                    justify-content: center;
                }

                .checkbox-input {
                    width: 18px;
                    height: 18px;
                    cursor: pointer;
                    accent-color: #1B2A4A;
                }

                .theme-options {
                    display: flex;
                    gap: 24px;
                    flex-wrap: wrap;
                }

                .theme-radio-group {
                    display: flex;
                    align-items: center;
                    gap: 8px;
                    cursor: pointer;
                }

                .theme-radio {
                    width: 18px;
                    height: 18px;
                    cursor: pointer;
                    accent-color: #1B2A4A;
                }

                .theme-label {
                    font-size: 14px;
                    color: #1E293B;
                    cursor: pointer;
                    font-weight: 500;
                }

                @keyframes fadeIn {
                    from { opacity: 0; }
                    to { opacity: 1; }
                }

                @keyframes fadeInUp {
                    from { opacity: 0; transform: translateY(10px); }
                    to { opacity: 1; transform: translateY(0); }
                }

                @keyframes fadeInDown {
                    from { opacity: 0; transform: translateY(-10px); }
                    to { opacity: 1; transform: translateY(0); }
                }

                @keyframes scaleIn {
                    from { opacity: 0; transform: scale(0.8); }
                    to { opacity: 1; transform: scale(1); }
                }

                @media (max-width: 1024px) {
                    .settings-main-container { padding: 24px; }
                    .profile-form-grid { gap: 16px; }
                }

                @media (max-width: 768px) {
                    .settings-main-container {
                        padding: 16px;
                        min-height: calc(100vh - 160px);
                    }

                    .page-header { margin-bottom: 16px; }
                    .page-header h1 { font-size: 24px; }

                    .profile-card-header {
                        flex-direction: column;
                        align-items: flex-start;
                        padding: 16px;
                    }

                    .profile-card-title { width: 100%; }
                    .profile-actions { width: 100%; flex-direction: column; }
                    .profile-btn { width: 100%; text-align: center; }

                    .profile-card-body { padding: 16px; }

                    .profile-avatar-section {
                        flex-direction: column;
                        text-align: center;
                        gap: 16px;
                        margin-bottom: 24px;
                        padding-bottom: 24px;
                    }

                    .profile-avatar {
                        width: 100px;
                        height: 100px;
                        font-size: 40px;
                    }

                    .profile-form-grid {
                        grid-template-columns: 1fr;
                        gap: 16px;
                    }

                    .notifications-grid {
                        grid-template-columns: 1fr;
                        gap: 20px;
                    }

                    .notifications-checkboxes {
                        flex-direction: row;
                        gap: 24px;
                    }

                    .checkbox-wrapper {
                        flex: 1;
                    }

                    .theme-options {
                        gap: 16px;
                    }
                }

                @media (max-width: 480px) {
                    .settings-main-container { padding: 12px; }
                    .page-header h1 { font-size: 20px; }

                    .profile-card-header { padding: 12px; }
                    .profile-card-body { padding: 12px; }

                    .profile-avatar {
                        width: 80px;
                        height: 80px;
                        font-size: 32px;
                    }

                    .profile-avatar-info h2 { font-size: 18px; }
                    .profile-label { font-size: 11px; margin-bottom: 6px; }
                    .profile-value, .profile-input { font-size: 12px; padding: 8px; }
                    .profile-btn { padding: 8px 12px; font-size: 12px; }

                    .notifications-grid {
                        grid-template-columns: 1fr;
                    }

                    .notifications-checkboxes {
                        flex-direction: row;
                        gap: 16px;
                    }

                    .notification-item {
                        font-size: 13px;
                    }

                    .theme-options {
                        flex-direction: column;
                        gap: 12px;
                    }

                    .theme-radio-group {
                        width: 100%;
                    }
                }

                .empty-state {
                  display: flex;
                  flex-direction: column;
                  align-items: center;
                  justify-content: center;
                  text-align: center;
                  padding: 80px 24px;
                  color: #6B7280;
                  min-height: 400px;
                  animation: fadeIn 0.4s ease;
                }

                .empty-state h3 {
                  margin: 0 0 8px 0;
                  font-size: 20px;
                  font-weight: 600;
                  color: #111827;
                }

                .empty-state p {
                  margin: 0;
                  font-size: 14px;
                  color: #6B7280;
                }

                .empty-icon {
                  width: 80px;
                  height: 80px;
                  background: transparent;
                  border-radius: 50%;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  margin-bottom: 24px;
                  font-size: 48px;
                }
            `}</style>

            <div className="settings-main-container">
                <div className="page-header">
                    <div>
                        <h1>Settings</h1>
                        <p>Manage your account and preferences</p>
                    </div>
                </div>

                <div className="settings-tabs">
                    <button
                        className={`settings-tab ${activeTab === 'profile' ? 'active' : ''}`}
                        onClick={() => { setActiveTab('profile'); navigate('?tab=profile'); }}
                    >
                        Profile
                    </button>
                    <button
                        className={`settings-tab ${activeTab === 'preferences' ? 'active' : ''}`}
                        onClick={() => { setActiveTab('preferences'); navigate('?tab=preferences'); }}
                    >
                        Preferences
                    </button>
                </div>

                {/* Profile Tab */}
                {activeTab === 'profile' && (
                    <div className="profile-card">
                        <div className="profile-card-header">
                            <h2 className="profile-card-title">Profile Information</h2>
                            <div className="profile-actions">
                                {!editMode ? (
                                    <button className="profile-btn btn-edit" onClick={handleEditClick} disabled={!formData.name}>
                                        Edit Profile
                                    </button>
                                ) : (
                                    <>
                                        <button
                                            className="profile-btn btn-save"
                                            onClick={handleSaveProfile}
                                            disabled={saving}
                                        >
                                            {saving ? 'Saving...' : 'Save Changes'}
                                        </button>
                                        <button className="profile-btn btn-cancel" onClick={handleCancel}>
                                            Cancel
                                        </button>
                                    </>
                                )}
                            </div>
                        </div>

                        <div className="profile-card-body">
                            {!formData.name ? (
                                <div className="empty-state">
                                    <div className="empty-icon">📋</div>
                                    <h3>No Profile Data Available</h3>
                                    <p>Unable to load your profile. Please try refreshing or contact support.</p>
                                </div>
                            ) : (
                                <>
                                    {/* Avatar Section */}
                                    <div className="profile-avatar-section">
                                        <div className="profile-avatar">
                                            {getInitials(formData.name || 'User')}
                                        </div>
                                        <div className="profile-avatar-info">
                                            <h2>{formData.name || 'User'}</h2>
                                            <p>{formData.role || 'Manager'}</p>
                                            <span className="status-badge">Active</span>
                                        </div>
                                    </div>

                                    {/* Personal Information Section */}
                                    <div className="profile-form-section">
                                        <h3 className="profile-section-title">Personal Information</h3>
                                        <div className="profile-form-grid">
                                            <div className="profile-form-group">
                                                <label className="profile-label">Full Name</label>
                                                {editMode ? (
                                                    <input
                                                        type="text"
                                                        name="name"
                                                        value={formData.name || ''}
                                                        onChange={handleFieldChange}
                                                        className="profile-input"
                                                        placeholder="Enter your full name"
                                                    />
                                                ) : (
                                                    <div className="profile-value">{formData.name || 'N/A'}</div>
                                                )}
                                            </div>

                                            <div className="profile-form-group">
                                                <label className="profile-label">Email</label>
                                                {editMode ? (
                                                    <input
                                                        type="email"
                                                        name="email"
                                                        value={formData.email || ''}
                                                        onChange={handleFieldChange}
                                                        className="profile-input"
                                                        placeholder="Enter your email"
                                                    />
                                                ) : (
                                                    <div className="profile-value">{formData.email || 'N/A'}</div>
                                                )}
                                            </div>

                                            <div className="profile-form-group">
                                                <label className="profile-label">Phone</label>
                                                {editMode ? (
                                                    <input
                                                        type="tel"
                                                        name="phone"
                                                        value={formData.phone || ''}
                                                        onChange={handleFieldChange}
                                                        className="profile-input"
                                                        placeholder="Enter your phone"
                                                    />
                                                ) : (
                                                    <div className="profile-value">{formData.phone || 'N/A'}</div>
                                                )}
                                            </div>

                                            <div className="profile-form-group">
                                                <label className="profile-label">Department</label>
                                                {editMode ? (
                                                    <input
                                                        type="text"
                                                        name="department"
                                                        value={formData.department || ''}
                                                        onChange={handleFieldChange}
                                                        className="profile-input"
                                                        placeholder="Enter your department"
                                                    />
                                                ) : (
                                                    <div className="profile-value">{formData.department || 'N/A'}</div>
                                                )}
                                            </div>
                                        </div>
                                    </div>

                                    {/* Role Information Section */}
                                    <div className="profile-form-section">
                                        <h3 className="profile-section-title">Role Information</h3>
                                        <div className="profile-form-grid">
                                            <div className="profile-form-group full">
                                                <label className="profile-label">Role</label>
                                                {editMode ? (
                                                    <input
                                                        type="text"
                                                        name="role"
                                                        value={formData.role || ''}
                                                        onChange={handleFieldChange}
                                                        className="profile-input"
                                                        placeholder="Enter your role"
                                                    />
                                                ) : (
                                                    <div className="profile-value">{formData.role || 'N/A'}</div>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                </>
                            )}
                        </div>
                    </div>
                )}

                {/* Preferences Tab */}
                {activeTab === 'preferences' && (
                    <div className="profile-card">
                        <div className="profile-card-header">
                            <h2 className="profile-card-title">Preferences</h2>
                        </div>

                        <div className="profile-card-body">
                            {/* Notifications Section */}
                            <div className="profile-form-section">
                                <h3 className="profile-section-title">Notifications</h3>
                                <div className="notifications-grid">
                                    <div className="notifications-list">
                                        <div className="notification-item">Daily Report Email</div>
                                        <div className="notification-item">SLA Alerts</div>
                                        <div className="notification-item">Escalation Notifications</div>
                                        <div className="notification-item">Weekly Metrics</div>
                                    </div>
                                    <div className="notifications-checkboxes">
                                        <div className="checkbox-wrapper">
                                            <input
                                                type="checkbox"
                                                className="checkbox-input"
                                                checked={preferences.emailDailyReport}
                                                onChange={() => handlePreferenceChange('emailDailyReport')}
                                            />
                                        </div>
                                        <div className="checkbox-wrapper">
                                            <input
                                                type="checkbox"
                                                className="checkbox-input"
                                                checked={preferences.slaAlerts}
                                                onChange={() => handlePreferenceChange('slaAlerts')}
                                            />
                                        </div>
                                        <div className="checkbox-wrapper">
                                            <input
                                                type="checkbox"
                                                className="checkbox-input"
                                                checked={preferences.escalationNotifications}
                                                onChange={() => handlePreferenceChange('escalationNotifications')}
                                            />
                                        </div>
                                        <div className="checkbox-wrapper">
                                            <input
                                                type="checkbox"
                                                className="checkbox-input"
                                                checked={preferences.weeklyMetrics}
                                                onChange={() => handlePreferenceChange('weeklyMetrics')}
                                            />
                                        </div>
                                    </div>
                                </div>
                            </div>

                            {/* Appearance Section */}
                            <div className="profile-form-section">
                                <h3 className="profile-section-title">Appearance</h3>
                                <div className="theme-options">
                                    <label className="theme-radio-group">
                                        <input
                                            type="radio"
                                            name="theme"
                                            value="light"
                                            checked={preferences.theme === 'light'}
                                            onChange={() => setPreferences(prev => ({ ...prev, theme: 'light' }))}
                                            className="theme-radio"
                                        />
                                        <span className="theme-label">Light Mode</span>
                                    </label>
                                    <label className="theme-radio-group">
                                        <input
                                            type="radio"
                                            name="theme"
                                            value="dark"
                                            checked={preferences.theme === 'dark'}
                                            onChange={() => setPreferences(prev => ({ ...prev, theme: 'dark' }))}
                                            className="theme-radio"
                                        />
                                        <span className="theme-label">Dark Mode</span>
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </Layout>
    );
}
