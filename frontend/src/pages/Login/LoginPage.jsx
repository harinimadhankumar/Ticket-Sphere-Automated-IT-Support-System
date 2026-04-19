import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../../components/Layout/Header';
import Footer from '../../components/Layout/Footer';
import { engineerLogin, managementLogin } from '../../utils/api';
import { saveEngineerSession, saveManagementSession, isLoggedIn, getUserRole } from '../../utils/auth';
import { showToast, getUserFriendlyMessage } from '../../utils/toastConfig';

export default function LoginPage() {
    const navigate = useNavigate();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [role, setRole] = useState('engineer');
    const [remember, setRemember] = useState(false);
    const [loading, setLoading] = useState(false);

    React.useEffect(() => {
        if (isLoggedIn()) {
            const userRole = getUserRole();
            if (userRole === 'management') {
                navigate('/management/dashboard', { replace: true });
            } else {
                navigate('/engineer/dashboard', { replace: true });
            }
        } else {
            // Load saved credentials if remember me was checked
            const savedUsername = localStorage.getItem('saved_username');
            const savedRole = localStorage.getItem('saved_role');
            const savedRemember = localStorage.getItem('saved_remember');

            if (savedUsername) {
                setUsername(savedUsername);
            }
            if (savedRole) {
                setRole(savedRole);
            }
            if (savedRemember === 'true') {
                setRemember(true);
            }
        }
    }, [navigate]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!username.trim() || !password) {
            showToast.error('Please enter username and password');
            return;
        }
        setLoading(true);
        try {
            if (role === 'engineer') {
                const data = await engineerLogin(username.trim(), password);
                if (data.success) {
                    // Save credentials if remember me is checked
                    if (remember) {
                        localStorage.setItem('saved_username', username.trim());
                        localStorage.setItem('saved_role', role);
                        localStorage.setItem('saved_remember', 'true');
                    } else {
                        localStorage.removeItem('saved_username');
                        localStorage.removeItem('saved_role');
                        localStorage.removeItem('saved_remember');
                    }

                    saveEngineerSession(data);
                    showToast.success('Login successful! Redirecting...');
                    setTimeout(() => navigate('/engineer/dashboard'), 800);
                } else {
                    throw new Error(data.message || 'Login failed');
                }
            } else {
                const result = await managementLogin(username.trim(), password);
                if (result.success && result.data) {
                    // Save credentials if remember me is checked
                    if (remember) {
                        localStorage.setItem('saved_username', username.trim());
                        localStorage.setItem('saved_role', role);
                        localStorage.setItem('saved_remember', 'true');
                    } else {
                        localStorage.removeItem('saved_username');
                        localStorage.removeItem('saved_role');
                        localStorage.removeItem('saved_remember');
                    }

                    saveManagementSession(result.data);
                    showToast.success(`Welcome, ${result.data.user?.name || result.data.userInfo?.name}! Redirecting...`);
                    setTimeout(() => navigate('/management/dashboard'), 800);
                } else {
                    throw new Error(result.message || 'Login failed. Please check your credentials.');
                }
            }
        } catch (error) {
            showToast.error(getUserFriendlyMessage(error.message || 'Connection error. Please check if the server is running.'));
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-page">
            <Header showNav={false} showUser={false} />
            <div className="login-container">
                <div className="login-left">
                    <div className="login-left-content">
                        <h1>Welcome to PowerGrid ITSM</h1>
                        <p>Efficiently manage, track and resolve IT service requests in one secure platform.</p>
                        <ul className="login-features">
                            <li className="login-feature">
                                <div className="login-feature-dot"></div>
                                <h3>Raise &amp; Track Tickets</h3>
                                <p>Quickly log and monitor issues with intelligent routing</p>
                            </li>
                            <li className="login-feature">
                                <div className="login-feature-dot"></div>
                                <h3>Knowledge Base Integration</h3>
                                <p>Auto-match solutions from the knowledge base</p>
                            </li>
                            <li className="login-feature">
                                <div className="login-feature-dot"></div>
                                <h3>SLA Compliance Tracking</h3>
                                <p>Monitor service level agreements in real-time</p>
                            </li>
                            <li className="login-feature">
                                <div className="login-feature-dot"></div>
                                <h3>Escalation Management</h3>
                                <p>Seamless ticket escalation with team lead routing</p>
                            </li>
                        </ul>
                    </div>
                </div>
                <div className="login-right">
                    <div className="login-card">
                        <div className="login-card-header">
                            <h2>Sign In</h2>
                            <p>Access your {role === 'engineer' ? 'engineer' : 'management'} dashboard</p>
                        </div>
                        <form onSubmit={handleSubmit}>
                            <div className="form-group">
                                <label htmlFor="role">Portal</label>
                                <select
                                    id="role"
                                    className="form-select"
                                    value={role}
                                    onChange={(e) => setRole(e.target.value)}
                                >
                                    <option value="engineer">Engineer Portal</option>
                                    <option value="management">Management Portal</option>
                                </select>
                            </div>
                            <div className="form-group">
                                <label htmlFor="username">Username</label>
                                <input
                                    type="text"
                                    id="username"
                                    className="form-input"
                                    placeholder={role === 'engineer' ? 'e.g. Sneha Patel (APP)' : 'Enter your username'}
                                    value={username}
                                    onChange={(e) => setUsername(e.target.value)}
                                    required
                                    autoFocus
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="password">Password</label>
                                <input
                                    type="password"
                                    id="password"
                                    className="form-input"
                                    placeholder="Enter your password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                />
                            </div>
                            <div className="form-options">
                                <label className="remember-me">
                                    <input
                                        type="checkbox"
                                        checked={remember}
                                        onChange={(e) => setRemember(e.target.checked)}
                                    />
                                    <span>Remember me</span>
                                </label>
                                <a href="#forgot" className="forgot-link" onClick={(e) => { e.preventDefault(); showToast.info('Contact your administrator to reset your password.'); }}>
                                    Forgot Password?
                                </a>
                            </div>
                            <button type="submit" className="login-btn" disabled={loading}>
                                {loading ? 'Logging in...' : 'Login'}
                            </button>
                        </form>
                        <div className="login-footer-text">
                            Secure access for authorized personnel only
                        </div>
                    </div>
                </div>
            </div>
            <Footer />
        </div>
    );
}
