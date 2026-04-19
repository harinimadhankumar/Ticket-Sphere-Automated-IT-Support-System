import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout/Layout';
import { showToast, getUserFriendlyMessage } from '../../utils/toastConfig';
import { isLoggedIn, getEngineerData } from '../../utils/auth';
import { updateEngineerProfile } from '../../utils/api';

function Profile() {
  const navigate = useNavigate();
  const [profileData, setProfileData] = useState(null);
  const [formData, setFormData] = useState(null);
  const [editMode, setEditMode] = useState(false);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!isLoggedIn()) { navigate('/login', { replace: true }); return; }
    const engineerData = getEngineerData();
    if (engineerData) {
      setProfileData(engineerData);
      setFormData(engineerData);
    }
  }, [navigate]);

  const handleFieldChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSaveProfile = async () => {
    setSaving(true);
    try {
      const response = await updateEngineerProfile({
        name: formData.name,
        email: formData.email,
        phone: formData.phone,
        team: formData.team
      });

      if (response.success) {
        sessionStorage.setItem('engineerData', JSON.stringify(response.engineer));
        setProfileData(response.engineer);
        setFormData(response.engineer);
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
    setFormData(profileData);
    setEditMode(false);
  };

  const handleEditClick = () => {
    setFormData(profileData);
    setEditMode(true);
  };

  return (
    <Layout showSidebar={true} showSecondaryNav={true} >
      <style>{`
        * {
          box-sizing: border-box;
        }

        html, body {
          margin: 0;
          padding: 0;
          overflow-x: hidden;
        }

        .page-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
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

        .profile-wrapper {
          display: grid;
          grid-template-columns: 1fr;
          gap: 24px;
          max-width: 100%;
          width: 100%;
          animation: fadeInUp 0.4s ease;
        }

        .profile-card {
          background: white;
          border-radius: 12px;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
          overflow: hidden;
          transition: all 0.2s ease;
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
          transition: all 0.2s ease;
        }

        .profile-input {
          font-size: 14px;
          color: #1E293B;
          padding: 12px;
          border: 1px solid #D1D5DB;
          border-radius: 6px;
          font-family: inherit;
          transition: all 0.2s ease;
          background: white;
        }

        .profile-input:focus {
          outline: none;
          border-color: #1E293B;
          box-shadow: 0 0 0 3px rgba(30, 41, 59, 0.1);
        }

        .profile-input:disabled {
          background: #F9FAFB;
          color: #1E293B;
          cursor: not-allowed;
        }

        .profile-form-actions {
          display: flex;
          gap: 12px;
          padding-top: 24px;
          border-top: 1px solid #E5E7EB;
          flex-wrap: wrap;
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

          display: flex;
          flex-direction: column;
          justify-content: center;
          align-items: center;
          min-height: 400px;
          animation: fadeIn 0.3s ease;
        }

        .spinner {
          width: 50px;
          height: 50px;
          border: 4px solid #E5E7EB;
          border-top-color: #3B82F6;
          border-right-color: #3B82F6;
          border-radius: 50%;
          animation: spin 0.8s linear infinite;
        }

        @keyframes spin {
          to { transform: rotate(360deg); }
        }

          from { opacity: 0; }
          to { opacity: 1; }
        }

        @keyframes fadeInUp {
          from {
            opacity: 0;
            transform: translateY(10px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }

        @keyframes fadeInDown {
          from {
            opacity: 0;
            transform: translateY(-10px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }

        @keyframes scaleIn {
          from {
            opacity: 0;
            transform: scale(0.8);
          }
          to {
            opacity: 1;
            transform: scale(1);
          }
        }

        .info-row {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 20px;
          margin-bottom: 16px;
        }

        .info-row:last-child {
          margin-bottom: 0;
        }

        .info-field {
          display: flex;
          flex-direction: column;
        }

        .info-label {
          font-size: 13px;
          font-weight: 600;
          color: #1E293B;
          margin-bottom: 4px;
          text-transform: uppercase;
          letter-spacing: 0.5px;
        }

        .info-value {
          font-size: 14px;
          font-weight: 500;
          color: #1E293B;
        }

        @media (max-width: 1024px) {
          .profile-form-grid {
            gap: 16px;
          }
        }

        @media (max-width: 768px) {
          .page-header {
            margin-bottom: 16px;
          }

          .page-header h1 {
            font-size: 24px;
          }

          .page-header p {
            font-size: 13px;
          }

          .profile-card-header {
            flex-direction: column;
            align-items: flex-start;
            padding: 16px;
            gap: 12px;
          }

          .profile-card-title {
            width: 100%;
          }

          .profile-actions {
            width: 100%;
            flex-direction: column;
          }

          .profile-btn {
            width: 100%;
            text-align: center;
          }

          .profile-card-body {
            padding: 16px;
          }

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

          .profile-form-group.full {
            grid-column: 1;
          }

          .info-row {
            grid-template-columns: 1fr;
            gap: 16px;
          }

          .profile-section-title {
            font-size: 15px;
            margin-bottom: 12px;
          }

          .profile-label {
            font-size: 12px;
          }

          .profile-value,
          .profile-input {
            font-size: 13px;
            padding: 10px;
          }

          .profile-form-actions {
            flex-direction: column;
          }

          .profile-btn {
            padding: 10px 16px;
          }
        }

        @media (max-width: 480px) {
          .page-header h1 {
            font-size: 20px;
            margin-bottom: 4px;
          }

          .page-header p {
            font-size: 12px;
          }

          .profile-card {
            border-radius: 8px;
          }

          .profile-card-header {
            padding: 12px;
          }

          .profile-card-body {
            padding: 12px;
          }

          .profile-avatar {
            width: 80px;
            height: 80px;
            font-size: 32px;
          }

          .profile-avatar-info h2 {
            font-size: 18px;
          }

          .profile-avatar-info p {
            font-size: 12px;
          }

          .profile-form-grid {
            gap: 12px;
          }

          .profile-label {
            font-size: 11px;
            margin-bottom: 6px;
          }

          .profile-value,
          .profile-input {
            font-size: 12px;
            padding: 8px;
          }

          .profile-btn {
            padding: 8px 12px;
            font-size: 12px;
          }

          .profile-section-title {
            font-size: 14px;
            padding-bottom: 8px;
            margin-bottom: 10px;
          }

            min-height: 300px;
          }

          .spinner {
            width: 40px;
            height: 40px;
            border-width: 3px;
          }
        }
      `}</style>

      <div className="page-header">
        <div>
          <h1>My Profile</h1>
          <p>View and manage your personal and work information</p>
        </div>
      </div>

      <div className="profile-wrapper">
          <div className="profile-card">
            <div className="profile-card-header">
              <h2 className="profile-card-title">Profile Information</h2>
              <div className="profile-actions">
                {!editMode ? (
                  <button className="profile-btn btn-edit" onClick={handleEditClick}>
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
                  <div className="spinner"></div>
                </div>
              ) : !profileData ? (
                <div className="empty-state">
                  <div className="empty-icon">📋</div>
                  <h3>No Profile Data Available</h3>
                  <p>Unable to load your profile. Please try refreshing or contact support.</p>
                </div>
              ) : (
                <>
                  {/* Avatar and Name Section */}
                  <div className="profile-avatar-section">
                    <div className="profile-avatar">
                      {formData.name.split(' ').map(n => n[0]).join('')}
                    </div>
                    <div className="profile-avatar-info">
                      <h2>{formData.name}</h2>
                      <p>{formData.role || 'IT Engineer'}</p>
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
                            value={formData.name}
                            onChange={handleFieldChange}
                            className="profile-input"
                          />
                        ) : (
                          <div className="profile-value">{formData.name}</div>
                        )}
                      </div>

                      <div className="profile-form-group">
                        <label className="profile-label">Email Address</label>
                        {editMode ? (
                          <input
                            type="email"
                            name="email"
                            value={formData.email}
                            onChange={handleFieldChange}
                            className="profile-input"
                          />
                        ) : (
                          <div className="profile-value">{formData.email}</div>
                        )}
                      </div>

                      <div className="profile-form-group">
                        <label className="profile-label">Phone Number</label>
                        {editMode ? (
                          <input
                            type="tel"
                            name="phone"
                            value={formData.phone}
                            onChange={handleFieldChange}
                            className="profile-input"
                          />
                        ) : (
                          <div className="profile-value">{formData.phone || '-'}</div>
                        )}
                      </div>

                      <div className="profile-form-group">
                        <label className="profile-label">Join Date</label>
                        <div className="profile-value">{formData.joinDate ? new Date(formData.joinDate).toLocaleDateString() : '-'}</div>
                      </div>
                    </div>
                  </div>

                  {/* Work Information Section */}
                  {formData.team && (
                    <div className="profile-form-section">
                      <h3 className="profile-section-title">Work Information</h3>
                      <div className="profile-form-grid">
                        <div className="profile-form-group">
                          <label className="profile-label">Team</label>
                          <div className="profile-value">{formData.team || '-'}</div>
                        </div>

                        <div className="profile-form-group">
                          <label className="profile-label">Role</label>
                          <div className="profile-value">{formData.role || '-'}</div>
                        </div>

                        <div className="profile-form-group">
                          <label className="profile-label">Department</label>
                          <div className="profile-value">{formData.department || '-'}</div>
                        </div>

                        <div className="profile-form-group">
                          <label className="profile-label">Manager</label>
                          <div className="profile-value">{formData.manager || '-'}</div>
                        </div>

                        <div className="profile-form-group">
                          <label className="profile-label">Employee ID</label>
                          <div className="profile-value">{formData.employeeId || '-'}</div>
                        </div>
                      </div>
                    </div>
                  )}
                </>
              )}
            </div>
          </div>
        </Layout>
  );
}

export default Profile;
