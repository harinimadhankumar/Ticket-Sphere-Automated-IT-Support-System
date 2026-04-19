import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
// import statements 
import WelcomePage from './pages/Welcome/WelcomePage';
import LoginPage from './pages/Login/LoginPage';
import EngineerDashboard from './pages/Engineer/EngineerDashboard';
import MyTickets from './pages/Engineer/MyTickets';
import KnowledgeBase from './pages/Engineer/KnowledgeBase';
import SLAStatus from './pages/Engineer/SLAStatus';
import Settings from './pages/Engineer/Settings';
import Profile from './pages/Engineer/Profile';
import ResolveTicket from './pages/Engineer/ResolveTicket';
import ManagementDashboard from './pages/Management/ManagementDashboard';
import TicketsOverview from './pages/Management/TicketsOverview';
import EngineerPerformance from './pages/Management/EngineerPerformance';
import CategoryAnalysis from './pages/Management/CategoryAnalysis';
import RecentTickets from './pages/Management/RecentTickets';
import SLAAnalysis from './pages/Management/SLAAnalysis';
import DepartmentReport from './pages/Management/DepartmentReport';
import DepartmentManagement from './pages/Management/DepartmentManagement';
import AuditLog from './pages/Management/AuditLog';
import Reports from './pages/Management/Reports';
import ResponseTime from './pages/Management/ResponseTime';
import TeamWorkload from './pages/Management/TeamWorkload';
import Escalations from './pages/Management/Escalations';
import ManagementSettings from './pages/Management/ManagementSettings';
import UserManagement from './pages/Management/UserManagement';

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<WelcomePage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/engineer/dashboard" element={<EngineerDashboard />} />
                <Route path="/engineer/tickets" element={<MyTickets />} />
                <Route path="/engineer/knowledge" element={<KnowledgeBase />} />
                <Route path="/engineer/sla" element={<SLAStatus />} />
                <Route path="/engineer/settings" element={<Settings />} />
                <Route path="/engineer/profile" element={<Profile />} />
                <Route path="/engineer/ticket/:ticketId" element={<ResolveTicket />} />
                <Route path="/management/dashboard" element={<ManagementDashboard />} />
                <Route path="/management/tickets" element={<TicketsOverview />} />
                <Route path="/management/engineers" element={<EngineerPerformance />} />
                <Route path="/management/categories" element={<CategoryAnalysis />} />
                <Route path="/management/recent" element={<RecentTickets />} />
                <Route path="/management/sla" element={<SLAAnalysis />} />
                <Route path="/management/departments" element={<DepartmentReport />} />
                <Route path="/management/department-mgmt" element={<DepartmentManagement />} />
                <Route path="/management/audit" element={<AuditLog />} />
                <Route path="/management/reports" element={<Reports />} />
                <Route path="/management/response-time" element={<ResponseTime />} />
                <Route path="/management/workload" element={<TeamWorkload />} />
                <Route path="/management/escalations" element={<Escalations />} />
                <Route path="/management/settings" element={<ManagementSettings />} />
                <Route path="/management/users" element={<UserManagement />} />
                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </Router>
    );
}

export default App;
