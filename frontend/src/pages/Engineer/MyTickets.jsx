import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout/Layout';
import { fetchEngineerDashboard, getAssignedTickets } from '../../utils/api';
import { showToast, getUserFriendlyMessage } from '../../utils/toastConfig';

function MyTickets() {
  const navigate = useNavigate();
  const [tickets, setTickets] = useState([]);
  const [filteredTickets, setFilteredTickets] = useState([]);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [categoryFilter, setCategoryFilter] = useState('all');
  const [priorityFilter, setPriorityFilter] = useState('all');

  useEffect(() => {
    loadTickets();
  }, []);

  const loadTickets = async () => {
    try {
      setError(null);

      // Try dashboard first
      console.log('Loading tickets from dashboard...');
      const dashboardData = await fetchEngineerDashboard();
      console.log('Dashboard data:', dashboardData);
      let ticketArray = dashboardData.tickets || [];

      // If no tickets from dashboard, try tickets endpoint
      if (ticketArray.length === 0) {
        console.log('No tickets from dashboard, fetching from tickets endpoint...');
        const ticketsData = await getAssignedTickets(null, null, false);
        console.log('Tickets endpoint response:', ticketsData);
        ticketArray = ticketsData.tickets || ticketsData.data || ticketsData.items || [];
      }

      console.log('Final tickets loaded:', ticketArray);
      setTickets(ticketArray);
      setFilteredTickets(ticketArray);

      if (ticketArray.length === 0) {
        setError('No tickets assigned to you');
      }
    } catch (err) {
      const errorMsg = getUserFriendlyMessage(err.message || 'Failed to load tickets');
      setError(errorMsg);
      showToast.error(errorMsg);
      setTickets([]);
      setFilteredTickets([]);
    } finally {
    }
  };

  useEffect(() => {
    let result = tickets;

    // Apply status filter
    if (statusFilter !== 'all') {
      if (statusFilter === 'open') {
        result = result.filter(ticket => ['NEW', 'OPEN', 'ASSIGNED'].includes(ticket.status?.toUpperCase()));
      } else if (statusFilter === 'in-progress') {
        result = result.filter(ticket => ['IN_PROGRESS', 'IN-PROGRESS'].includes(ticket.status?.toUpperCase()));
      } else if (statusFilter === 'resolved') {
        result = result.filter(ticket => ['RESOLVED', 'CLOSED'].includes(ticket.status?.toUpperCase()));
      }
    }

    // Apply category filter
    if (categoryFilter !== 'all') {
      result = result.filter(ticket =>
        ticket.category?.toLowerCase() === categoryFilter.toLowerCase()
      );
    }

    // Apply priority filter
    if (priorityFilter !== 'all') {
      result = result.filter(ticket =>
        ticket.priority?.toLowerCase() === priorityFilter.toLowerCase()
      );
    }

    // Apply search filter
    if (searchTerm.trim()) {
      const search = searchTerm.toLowerCase();
      result = result.filter(ticket =>
        ticket.id?.toString().toLowerCase().includes(search) ||
        ticket.ticketId?.toString().toLowerCase().includes(search) ||
        ticket.subject?.toLowerCase().includes(search) ||
        ticket.title?.toLowerCase().includes(search)
      );
    }

    setFilteredTickets(result);
  }, [searchTerm, statusFilter, categoryFilter, priorityFilter, tickets]);

  const handleViewDetails = (ticketId) => {
    navigate(`/engineer/ticket/${ticketId}`);
  };

  // Calculate stats
  const stats = {
    total: tickets.length,
    open: tickets.filter(t => ['NEW', 'OPEN', 'ASSIGNED'].includes(t.status?.toUpperCase())).length,
    inProgress: tickets.filter(t => t.status?.toUpperCase() === 'IN_PROGRESS' || t.status?.toUpperCase() === 'IN-PROGRESS').length,
    resolved: tickets.filter(t => ['RESOLVED', 'CLOSED'].includes(t.status?.toUpperCase())).length,
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

        .tickets-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 32px;
          padding-bottom: 20px;
          border-bottom: 2px solid #1E293B;
          animation: fadeInDown 0.3s ease;
        }

        .tickets-header h1 {
          font-size: 30px;
          font-weight: 700;
          color: #1E293B;
          margin: 0 0 8px 0;
        }

        .tickets-header p {
          font-size: 14px;
          color: #1E293B;
          margin: 0;
        }

        .tickets-controls {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 16px;
          margin-bottom: 24px;
          align-items: end;
          animation: fadeInUp 0.3s ease;
        }

        .search-box {
          display: flex;
          align-items: center;
          background: white;
          border: 1px solid #D1D5DB;
          border-radius: 8px;
          padding: 11px 14px;
          gap: 10px;
          box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
          transition: all 0.2s ease;
        }

        .search-box:focus-within {
          border-color: var(--primary-light, #2563EB);
          box-shadow: 0 2px 8px rgba(37, 99, 235, 0.1);
        }

        .search-box:hover {
          border-color: #9CA3AF;
          box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
        }

        .filters-group {
          display: flex;
          gap: 12px;
          flex: 1;
        }

        .search-box:focus-within {
          border-color: var(--primary-light, #2563EB);
          box-shadow: 0 2px 8px rgba(37, 99, 235, 0.1);
        }

        .search-box input {
          flex: 1;
          border: none;
          outline: none;
          font-size: 14px;
          font-family: inherit;
          color: #1F2937;
          background: transparent;
          transition: all 0.2s ease;
        }

        .search-box input::placeholder {
          color: #9CA3AF;
        }

        .search-box svg {
          width: 18px;
          height: 18px;
          color: #9CA3AF;
          transition: color 0.2s ease;
        }

        .search-box:focus-within svg {
          color: var(--primary-light, #2563EB);
        }

        .status-filter {
          background: #FFFFFF;
          border: 1px solid #D1D5DB;
          border-radius: 8px;
          padding: 12px 36px 12px 14px;
          font-size: 13px;
          font-weight: 500;
          font-family: inherit;
          color: #374151;
          cursor: pointer;
          box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
          transition: all 0.2s ease;
          white-space: nowrap;
          flex: 1;
          min-width: 120px;
          appearance: none;
          background-image: url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 20 20' fill='none' stroke='%23374151' stroke-width='1.5'%3e%3cpolyline points='5 7.5 10 12.5 15 7.5'%3e%3c/polyline%3e%3c/svg%3e");
          background-repeat: no-repeat;
          background-position: right 10px center;
          background-size: 16px;
          text-overflow: ellipsis;
        }

        .status-filter:hover {
          border-color: #BFDBFE;
          background-color: #FFFFFF;
          box-shadow: 0 2px 6px rgba(37, 99, 235, 0.1);
        }

        .status-filter:focus {
          outline: none;
          border-color: #2563EB;
          box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
        }

        .status-filter:active {
          background-color: #F9FAFB;
        }

        .tickets-table-wrapper {
          background: white;
          border-radius: 12px;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
          overflow: hidden;
          animation: fadeInUp 0.4s ease;
        }

        .tickets-table {
          width: 100%;
          border-collapse: collapse;
        }

        .tickets-table thead {
          background: #F9FAFB;
          border-bottom: 1px solid #E5E7EB;
        }

        .tickets-table thead th {
          padding: 16px 18px;
          text-align: center;
          font-size: 13px;
          font-weight: 600;
          color: #1E293B;
          text-transform: uppercase;
          letter-spacing: 0.5px;
        }

        .tickets-table tbody tr {
          border-bottom: 1px solid #F3F4F6;
          transition: all 0.2s ease;
          cursor: pointer;
        }

        .tickets-table tbody tr:hover {
          background-color: #F9FAFB;
          transform: translateY(-1px);
        }

        .tickets-table tbody td {
          padding: 16px 18px;
          font-size: 14px;
          color: #1E293B;
          text-align: center;
        }

        .ticket-id {
          font-weight: 600;
          color: var(--primary-light, #2563EB);
        }

        .ticket-subject {
          font-weight: 500;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          text-align: left;
        }

        .tickets-table thead th:nth-child(2) {
          text-align: left;
        }

        .status-badge {
          display: inline-block;
          padding: 6px 14px;
          border-radius: 16px;
          font-size: 12px;
          font-weight: 600;
          text-transform: capitalize;
          transition: all 0.2s ease;
        }

        .priority-indicator {
          display: inline-flex;
          align-items: center;
          gap: 6px;
          padding: 6px 12px;
          border-radius: 16px;
          font-size: 12px;
          font-weight: 600;
          text-transform: capitalize;
          transition: all 0.2s ease;
        }

        .priority-indicator::before {
          content: '';
          width: 8px;
          height: 8px;
          border-radius: 50%;
          background: currentColor;
          transition: transform 0.2s ease;
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

        @keyframes fadeIn {
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

        .error-state {
          background: #FEE2E2;
          border: 1px solid #FECACA;
          border-radius: 8px;
          padding: 16px;
          color: #991B1B;
          text-align: center;
          margin-bottom: 24px;
          animation: slideDown 0.3s ease;
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

        .tickets-count {
          font-size: 13px;
          color: #6B7280;
          padding: 16px;
          background: #F9FAFB;
          border-top: 1px solid #E5E7EB;
          text-align: center;
        }

        .filters-bar {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 16px;
          gap: 16px;
        }

        .filter-tabs {
          display: flex;
          gap: 24px;
          flex-wrap: wrap;
          align-items: center;
        }

        .filter-tab {
          background: transparent;
          border: none;
          padding: 8px 0;
          border-bottom: 2px solid transparent;
          cursor: pointer;
          font-size: 14px;
          font-weight: 500;
          color: #374151;
          transition: all 0.2s ease;
          white-space: nowrap;
        }

        .filter-tab:hover {
          color: #111827;
        }

        .filter-tab.active {
          color: #1E293B;
          border-bottom-color: #1E293B;
          font-weight: 600;
        }

        .count {
          margin-left: 4px;
          color: #6B7280;
          font-weight: 500;
        }

        .filter-tab.active .count {
          color: #1E293B;
        }

        .filter-actions {
          display: flex;
          gap: 12px;
          align-items: center;
        }

        @keyframes slideDown {
          from {
            opacity: 0;
            transform: translateY(-10px);
            max-height: 0;
          }
          to {
            opacity: 1;
            transform: translateY(0);
            max-height: 100px;
          }
        }

        @media (max-width: 1024px) {
          .my-tickets-container {
            padding: 24px;
          }

          .tickets-controls {
            grid-template-columns: 1fr 1.2fr;
            gap: 12px;
          }

          .filters-group {
            gap: 10px;
          }

          .status-filter {
            font-size: 12px;
            padding: 10px 32px 10px 12px;
            background-size: 14px;
            min-width: 100px;
          }

          .tickets-table thead th {
            padding: 12px 16px;
            font-size: 12px;
          }

          .tickets-table thead th:nth-child(2) {
            text-align: left;
          }

          .tickets-table tbody td {
            padding: 12px 16px;
            font-size: 13px;
          }
        }

        @media (max-width: 768px) {
          .my-tickets-container {
            padding: 16px;
            min-height: calc(100vh - 160px);
          }

          .tickets-header {
            margin-bottom: 16px;
            flex-direction: column;
            align-items: flex-start;
          }

          .tickets-header h1 {
            font-size: 24px;
          }

          .tickets-header p {
            font-size: 13px;
          }

          .tickets-controls {
            flex-direction: column;
            margin-bottom: 16px;
            gap: 10px;
          }

          .search-box {
            width: 100%;
            max-width: 100%;
          }

          .filters-group {
            display: flex;
            flex-direction: column;
            width: 100%;
            gap: 10px;
          }

          .status-filter {
            width: 100%;
            font-size: 13px;
            padding: 10px 12px;
          }

          .tickets-table thead th,
          .tickets-table tbody td {
            padding: 10px 12px;
            font-size: 12px;
          }

          .tickets-table thead th {
            font-size: 11px;
          }

          .empty-state {
            padding: 60px 12px;
            min-height: 300px;
          }

          .empty-icon {
            width: 60px;
            height: 60px;
            font-size: 32px;
            margin-bottom: 16px;
          }

          .empty-state h3 {
            font-size: 18px;
          }
        }

        @media (max-width: 480px) {
          .my-tickets-container {
            padding: 12px;
          }

          .tickets-header h1 {
            font-size: 20px;
            margin-bottom: 4px;
          }

          .tickets-header p {
            font-size: 12px;
          }

          .tickets-controls {
            gap: 8px;
            margin-bottom: 12px;
          }

          .search-box {
            padding: 6px 10px;
          }

          .search-box input {
            font-size: 13px;
          }

          .status-filter {
            padding: 6px 10px;
            font-size: 13px;
          }

          .tickets-table thead th {
            padding: 10px 6px;
            font-size: 11px;
          }

          .tickets-table tbody td {
            padding: 10px 6px;
            font-size: 12px;
          }

          .ticket-id {
            font-size: 12px;
          }

          .ticket-subject {
            max-width: 100px;
            font-size: 12px;
          }

          .empty-state {
            padding: 40px 12px;
            min-height: 250px;
          }

          .empty-icon {
            width: 50px;
            height: 50px;
            font-size: 28px;
            margin-bottom: 12px;
          }

          .empty-state h3 {
            font-size: 16px;
          }

          .empty-state p {
            font-size: 12px;
          }
        }
      `}</style>

      <div className="tickets-header">
        <div>
          <h1>My Tickets</h1>
          <p>View and manage all your support tickets</p>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-label">Total Tickets</div>
          <div className="stat-value">{stats.total || 0}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Open</div>
          <div className="stat-value">{stats.open || 0}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">In Progress</div>
          <div className="stat-value">{stats.inProgress || 0}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Resolved</div>
          <div className="stat-value">{stats.resolved || 0}</div>
        </div>
      </div>

      {error && (
        <div className="error-state">
          <strong>Error:</strong> {error}
        </div>
      )}

      {/* Single Filters Bar - Status Tabs on Left, Search/Dropdowns on Right */}
      <div className="filters-bar">
        <div className="filter-tabs">
          <button className={`filter-tab ${statusFilter === 'all' ? 'active' : ''}`} onClick={() => setStatusFilter('all')}>
            All <span className="count">{tickets.length}</span>
          </button>
          <button className={`filter-tab ${statusFilter === 'open' ? 'active' : ''}`} onClick={() => setStatusFilter('open')}>
            Open <span className="count">{stats.open}</span>
          </button>
          <button className={`filter-tab ${statusFilter === 'in-progress' ? 'active' : ''}`} onClick={() => setStatusFilter('in-progress')}>
            In Progress <span className="count">{stats.inProgress}</span>
          </button>
          <button className={`filter-tab ${statusFilter === 'resolved' ? 'active' : ''}`} onClick={() => setStatusFilter('resolved')}>
            Resolved <span className="count">{stats.resolved}</span>
          </button>
        </div>
        <div className="filter-actions">
          <select
            className="search-input"
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
            style={{ width: 120, flexShrink: 0 }}
          >
            <option value="all">All Categories</option>
            <option value="hardware">Hardware</option>
            <option value="software">Software</option>
            <option value="network">Network</option>
            <option value="email">Email</option>
            <option value="access">Access</option>
          </select>
          <select
            className="search-input"
            value={priorityFilter}
            onChange={(e) => setPriorityFilter(e.target.value)}
            style={{ width: 120, flexShrink: 0 }}
          >
            <option value="all">All Priorities</option>
            <option value="critical">Critical</option>
            <option value="high">High</option>
            <option value="medium">Medium</option>
            <option value="low">Low</option>
          </select>
          <input
            type="text"
            className="search-input"
            placeholder="Search by ticket ID or subject..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ width: 280, flexShrink: 0 }}
          />
        </div>
      </div>

      {filteredTickets.length === 0 ? (
        <div className="empty-state">
          <h3>No Tickets Found</h3>
          <p>
            {searchTerm || statusFilter !== 'all' || categoryFilter !== 'all' || priorityFilter !== 'all'
              ? 'Try adjusting your filters or search terms'
              : 'No tickets assigned to you yet'}
          </p>
        </div>
      ) : (
        <div className="card">
          <div className="card-header">
            <h2 style={{ color: '#1E293B', margin: 0 }}>My Tickets <span style={{ background: 'linear-gradient(135deg, #1B2A4A 0%, #1e3a5f 50%, #1B2A4A 100%)', color: 'white', padding: '3px 10px', borderRadius: 12, fontSize: 12, marginLeft: 8 }}>{filteredTickets.length}</span></h2>
          </div>
            <div style={{ overflowX: 'auto' }}>
              <table className="data-table">
              <thead>
                <tr>
                  <th style={{ width: 130, textAlign: 'center' }}>Ticket ID</th>
                  <th style={{ width: 280, textAlign: 'center' }}>Subject</th>
                  <th style={{ width: 110, textAlign: 'center' }}>Category</th>
                  <th style={{ width: 100, textAlign: 'center' }}>Priority</th>
                  <th style={{ width: 110, textAlign: 'center' }}>Status</th>
                  <th style={{ width: 150, textAlign: 'center' }}>SLA Remaining</th>
                </tr>
              </thead>
              <tbody>
                {filteredTickets.map((ticket) => (
                  <tr key={ticket.id || ticket.ticketId} onClick={() => handleViewDetails(ticket.ticketId || ticket.id)} style={{ cursor: 'pointer' }}>
                    <td style={{ width: 130, textAlign: 'center' }}><span style={{ fontWeight: 600, color: 'var(--primary-light)' }}>{ticket.ticketId || ticket.id}</span></td>
                    <td style={{ width: 280, textAlign: 'center', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {ticket.subject || ticket.title || 'No subject'}
                    </td>
                    <td style={{ width: 110, textAlign: 'center' }}><span className="category-tag">{ticket.category || 'GENERAL'}</span></td>
                    <td style={{ width: 100, textAlign: 'center' }}><span className={`badge badge-${(ticket.priority || 'medium').toLowerCase()}`}>{ticket.priority || 'MEDIUM'}</span></td>
                    <td style={{ width: 110, textAlign: 'center' }}><span className={`badge badge-${(ticket.status || 'assigned').toLowerCase().replace('_', '-')}`}>{ticket.status || 'Unknown'}</span></td>
                    <td style={{ width: 150, textAlign: 'center' }}>
                      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8 }}>
                        <span className={`sla-dot ${(ticket.slaStatus || 'on-track').toLowerCase().replace('_', '-')}`}></span>
                        <span className={`sla-time ${(ticket.slaStatus || 'on-track').toLowerCase().replace('_', '-')}`}>{ticket.slaRemaining || 'N/A'}</span>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            </div>
          </div>
        )}
    </Layout>
  );
}

export default MyTickets;
