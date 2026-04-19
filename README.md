# ⚡ PowerGrid IT Service Management System (ITSM)

> **AI-Based Centralized IT Ticket Management System for POWERGRID**
>
> A full-stack enterprise-grade IT service management platform that automates ticket ingestion from email, intelligently classifies and routes tickets using NLP, provides self-service resolution via knowledge base, enforces SLA compliance with auto-escalation, and delivers real-time analytics dashboards for both engineers and management.

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [Tech Stack](#-tech-stack)
- [System Architecture](#-system-architecture)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Installation & Setup](#-installation--setup)
- [Running the Application](#-running-the-application)
- [User Roles & Access](#-user-roles--access)
- [Frontend Pages](#-frontend-pages)
- [Backend API Endpoints](#-backend-api-endpoints)
- [Database Schema](#-database-schema)
- [NLP & AI Classification Engine](#-nlp--ai-classification-engine)
- [SLA Monitoring & Escalation](#-sla-monitoring--escalation)
- [Email Integration](#-email-integration)
- [Self-Service Resolution](#-self-service-resolution)
- [Notification System](#-notification-system)
- [Configuration Guide](#-configuration-guide)
- [Screenshots](#-screenshots)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🔍 Overview

PowerGrid ITSM is an **AI-powered IT Service Management System** designed for POWERGRID Corporation to streamline IT support operations. The system replaces manual email-based ticketing with an automated, intelligent workflow that:

1. **Ingests tickets automatically** from a shared IT support email inbox (via IMAP)
2. **Classifies tickets using NLP** — automatically determines category, sub-category, priority, and team assignment
3. **Attempts self-service resolution** by matching tickets to knowledge base articles before assigning to engineers
4. **Assigns tickets intelligently** to the right team and engineer based on workload and expertise
5. **Monitors SLA compliance** in real-time with multi-level auto-escalation (Warning → Level 1 → Level 2 → Level 3)
6. **Sends professional email notifications** at every stage of the ticket lifecycle
7. **Provides management dashboards** with charts, analytics, and downloadable PDF reports

---

## ✨ Key Features

### 🎫 Ticket Management
- **Automated Email Ingestion** — Polls a Gmail inbox via IMAP, parses emails, and creates tickets automatically
- **Chatbot Ticket Creation** — REST API endpoint for chatbot-based ticket creation
- **Intelligent Classification** — NLP engine categorizes tickets by category, sub-category, and priority
- **Smart Team Assignment** — Automatically routes tickets to the appropriate team (Network, Hardware, Software, Security, DevOps, Telecom)
- **Ticket Lifecycle** — Full workflow: OPEN → IN_PROGRESS → RESOLVED → CLOSED (with REOPENED support)

### 🤖 AI & NLP Engine
- **Text Preprocessing** — Tokenization, stopword removal, and text normalization
- **Keyword Dictionary** — 1000+ IT-related keywords mapped to categories and sub-categories
- **Intent Detection** — Identifies user intent (report issue, request service, ask question, etc.)
- **Priority Engine** — Calculates priority (CRITICAL, HIGH, MEDIUM, LOW) based on urgency keywords, impact assessment, and system analysis
- **Team Assignment** — Maps categories to specialized IT teams with fallback logic

### 📚 Self-Service Resolution
- **Knowledge Base Matching** — Automatically matches incoming tickets to existing KB articles
- **Solution Delivery via Email** — Sends matched solutions directly to the ticket raiser
- **User Response Handling** — Users can confirm resolution or request engineer assistance via email links
- **Timeout Scheduler** — Auto-escalates to engineer if user doesn't respond within configured timeout
- **Confidence Scoring** — Only suggests solutions above a configurable confidence threshold

### ⏱️ SLA Monitoring & Escalation
- **Real-Time SLA Tracking** — Monitors every open ticket against SLA deadlines
- **Priority-Based SLA Times** — CRITICAL: 2hrs, HIGH: 4hrs, MEDIUM: 8hrs, LOW: 24hrs
- **Multi-Level Auto-Escalation:**
  - **Warning** (75%) — Alert to assigned engineer
  - **Level 1** (100%) — Escalate to Team Lead
  - **Level 2** (150%) — Escalate to Manager
  - **Level 3** (200%) — Escalate to IT Admin/Director
- **SLA Dashboard** — Visual tracking of SLA compliance across all tickets

### 📧 Notification System
- **Professional HTML Emails** — Beautiful, branded email templates for every event
- **Event-Based Notifications:**
  - Ticket Created
  - Ticket Assigned
  - Ticket Escalated
  - Ticket Resolved
  - Ticket Closed
  - SLA Warning
  - Ticket Reopened
- **Configurable Recipients** — Engineer, Team Lead, Manager, and Admin email chains

### 📊 Management Analytics
- **Real-Time Dashboard** — Live statistics, ticket trends, and team performance
- **Category Analysis** — Breakdown of tickets by category with visual charts
- **Engineer Performance** — Individual engineer metrics (resolution time, ticket count, SLA compliance)
- **SLA Analysis** — Compliance rates, breach analysis, and trend tracking
- **Department Reports** — Per-department ticket volume and performance
- **Response Time Analytics** — Average response/resolution times by priority and team
- **Team Workload** — Distribution of tickets across teams and engineers
- **Escalation Tracking** — Escalation frequency and patterns
- **Audit Logs** — Complete trail of all system actions
- **Downloadable PDF Reports** — Export analytics as PDF documents

### 👥 User Management
- **Role-Based Access Control (RBAC)** — IT_COORDINATOR, DEPARTMENT_HEAD, TEAM_LEAD, ENGINEER
- **Engineer Management** — Add, edit, deactivate engineers (Department Head)
- **Manager Management** — Add, edit, deactivate managers (IT Coordinator)
- **Department Management** — Create and manage departments
- **Profile Management** — Users can update their own profiles

---

## 🛠️ Tech Stack

### Frontend
| Technology | Version | Purpose |
|---|---|---|
| React | 18.2.0 | UI Component Library |
| React Router DOM | 6.20.0 | Client-Side Routing |
| React Scripts (CRA) | 5.0.1 | Build Tooling & Dev Server |
| Chart.js | 4.4.0 | Data Visualization & Charts |
| React-Chartjs-2 | 5.2.0 | React Wrapper for Chart.js |
| React Hot Toast | 2.4.1 | Toast Notifications |
| Web Vitals | 2.1.4 | Performance Monitoring |
| CSS3 | — | Custom Styling (Vanilla CSS) |

### Backend
| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Programming Language |
| Spring Boot | 3.5.10 | Application Framework |
| Spring Data JPA | — | Database ORM |
| Spring Security | — | Password Encryption (BCrypt) |
| Spring Mail | — | Email Sending (SMTP) |
| Spring Validation | — | Input Validation |
| MySQL | 8.0+ | Relational Database |
| Apache PDFBox | 2.0.28 | PDF Report Generation |
| Maven | — | Build & Dependency Management |

### Infrastructure
| Technology | Purpose |
|---|---|
| MySQL 8.0+ | Primary Database |
| Gmail IMAP | Email Ingestion |
| Gmail SMTP | Email Sending |

---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        FRONTEND (React)                         │
│                     http://localhost:3000                        │
│                                                                 │
│  ┌──────────┐  ┌──────────────┐  ┌───────────────────────────┐  │
│  │ Welcome  │  │  Login Page  │  │  Engineer Portal          │  │
│  │  Page    │  │  (Dual Role) │  │  - Dashboard              │  │
│  │          │  │              │  │  - My Tickets             │  │
│  │          │  │              │  │  - Resolve Ticket         │  │
│  │          │  │              │  │  - Knowledge Base         │  │
│  │          │  │              │  │  - SLA Status             │  │
│  │          │  │              │  │  - Profile & Settings     │  │
│  └──────────┘  └──────────────┘  └───────────────────────────┘  │
│                                  ┌───────────────────────────┐  │
│                                  │  Management Portal        │  │
│                                  │  - Dashboard & Analytics  │  │
│                                  │  - Ticket Overview        │  │
│                                  │  - Engineer Performance   │  │
│                                  │  - SLA Analysis           │  │
│                                  │  - Department Reports     │  │
│                                  │  - User Management        │  │
│                                  │  - Audit Logs             │  │
│                                  │  - Reports (PDF Export)   │  │
│                                  └───────────────────────────┘  │
└───────────────────────────┬─────────────────────────────────────┘
                            │ HTTP (REST API)
                            │ Proxy: localhost:3000 → localhost:8080
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                     BACKEND (Spring Boot)                       │
│                     http://localhost:8080                        │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                   REST Controllers                      │    │
│  │  EngineerAuth │ ManagementAuth │ Dashboard │ SLA │ ...  │    │
│  └───────────────────────────┬─────────────────────────────┘    │
│                              │                                  │
│  ┌───────────────────────────┼─────────────────────────────┐    │
│  │                   Service Layer                         │    │
│  │  TicketService │ EngineerService │ ResolutionService     │    │
│  │  ManagementAuthService │ DepartmentFilterService         │    │
│  └───────────────────────────┬─────────────────────────────┘    │
│                              │                                  │
│  ┌──────────┐ ┌──────────┐ ┌┴─────────┐ ┌────────────────┐     │
│  │   NLP    │ │Self-Serv.│ │   SLA    │ │  Notification  │     │
│  │  Engine  │ │ Engine   │ │ Monitor  │ │   Service      │     │
│  │          │ │          │ │          │ │                │     │
│  │-Classify │ │-KB Match │ │-Deadline │ │-HTML Templates │     │
│  │-Priority │ │-Solution │ │-Escalate │ │-Event-Based    │     │
│  │-Assign   │ │-Timeout  │ │-Alert    │ │-Email Chains   │     │
│  └──────────┘ └──────────┘ └──────────┘ └────────────────┘     │
│                              │                                  │
│  ┌───────────────────────────┼─────────────────────────────┐    │
│  │              JPA Repositories (Data Layer)              │    │
│  │  TicketRepo │ EngineerRepo │ KBRepo │ ManagementRepo    │    │
│  └───────────────────────────┬─────────────────────────────┘    │
└──────────────────────────────┼──────────────────────────────────┘
                               │ JDBC
                               ▼
                    ┌──────────────────────┐
                    │   MySQL Database     │
                    │  powergrid_tickets   │
                    │                      │
                    │  Tables:             │
                    │  - tickets           │
                    │  - engineers         │
                    │  - management_users  │
                    │  - departments       │
                    │  - team_leads        │
                    │  - knowledge_base    │
                    └──────────────────────┘
                               ▲
         ┌─────────────────────┼──────────────────────┐
         │                     │                      │
┌────────┴─────────┐  ┌───────┴────────┐  ┌──────────┴───────┐
│  Gmail IMAP      │  │  Gmail SMTP    │  │   Schedulers     │
│  (Email Ingest)  │  │  (Send Email)  │  │  - Email Polling │
│  Polls every 30s │  │  Notifications │  │  - SLA Monitor   │
│                  │  │  & Solutions   │  │  - Self-Service  │
│                  │  │                │  │    Timeout       │
└──────────────────┘  └────────────────┘  └──────────────────┘
```

---

## 📁 Project Structure

```
IT-SERVICE/
│
├── .github/                          # GitHub configuration
│   └── java-upgrade/                 # Java version upgrade workflows
│
├── database/
│   └── powergrid.sql                 # Complete MySQL database dump (DDL + seed data)
│
├── frontend/                         # React Frontend Application
│   ├── package.json                  # Node.js dependencies & scripts
│   ├── package-lock.json             # Locked dependency versions
│   ├── public/                       # Static assets (index.html, favicon, etc.)
│   ├── build/                        # Production build output (generated)
│   └── src/
│       ├── index.js                  # React entry point
│       ├── index.css                 # Global styles (44KB - full design system)
│       ├── App.jsx                   # Root component with all routes
│       │
│       ├── components/               # Reusable UI Components
│       │   ├── Layout/
│       │   │   ├── Layout.jsx        # Main page layout wrapper
│       │   │   ├── Header.jsx        # Top navigation bar
│       │   │   ├── Sidebar.jsx       # Left sidebar navigation
│       │   │   ├── SecondaryNav.jsx   # Secondary navigation bar
│       │   │   └── Footer.jsx        # Page footer
│       │   └── common/
│       │       └── LoadingSpinner.jsx # Reusable loading spinner
│       │
│       ├── pages/                    # Page Components
│       │   ├── Welcome/
│       │   │   └── WelcomePage.jsx   # Landing page with features
│       │   ├── Login/
│       │   │   └── LoginPage.jsx     # Dual-portal login (Engineer/Management)
│       │   ├── Engineer/             # Engineer Portal Pages
│       │   │   ├── EngineerDashboard.jsx  # Engineer home dashboard
│       │   │   ├── MyTickets.jsx          # Assigned tickets list
│       │   │   ├── ResolveTicket.jsx      # Ticket resolution workspace
│       │   │   ├── KnowledgeBase.jsx      # KB article viewer
│       │   │   ├── SLAStatus.jsx          # SLA compliance tracker
│       │   │   ├── Profile.jsx            # User profile editor
│       │   │   └── Settings.jsx           # Account settings
│       │   └── Management/          # Management Portal Pages
│       │       ├── ManagementDashboard.jsx    # Analytics dashboard
│       │       ├── TicketsOverview.jsx        # All tickets view
│       │       ├── EngineerPerformance.jsx    # Engineer metrics
│       │       ├── CategoryAnalysis.jsx       # Category breakdown
│       │       ├── RecentTickets.jsx          # Recent ticket feed
│       │       ├── SLAAnalysis.jsx            # SLA compliance analysis
│       │       ├── DepartmentReport.jsx       # Dept-wise reports
│       │       ├── DepartmentManagement.jsx   # Manage departments
│       │       ├── ResponseTime.jsx           # Response time analytics
│       │       ├── TeamWorkload.jsx           # Team workload distribution
│       │       ├── Escalations.jsx            # Escalation tracking
│       │       ├── AuditLog.jsx               # System audit trail
│       │       ├── Reports.jsx                # Report generation & PDF download
│       │       ├── ManagementSettings.jsx     # System settings
│       │       ├── UserManagement.jsx         # Add/edit/delete users
│       │       └── UserManagement.css         # User management styles
│       │
│       └── utils/                    # Utility Modules
│           ├── api.js                # All REST API call functions
│           ├── auth.js               # Authentication helpers
│           ├── authContext.js         # React Auth Context provider
│           ├── rbac.js               # Role-Based Access Control logic
│           ├── toastConfig.js         # Toast notification configuration
│           ├── errorHandler.js        # Centralized error handling
│           ├── customHooks.js         # Custom React hooks
│           ├── validation.js          # Input validation utilities
│           ├── dataTransform.js       # Data transformation helpers
│           ├── apiNormalizer.js        # API response normalizer
│           └── departmentTeamMap.js    # Department-to-team mapping
│
├── backend/                          # Spring Boot Backend Application
│   ├── pom.xml                       # Maven dependencies & build config
│   └── src/main/
│       ├── resources/
│       │   └── application.properties # All application configuration
│       └── java/com/powergrid/ticketsystem/
│           │
│           ├── ItTicketManagementApplication.java  # Main Spring Boot entry point
│           │
│           ├── config/               # Configuration Classes
│           │   ├── SecurityConfig.java        # Spring Security (BCrypt + permit all)
│           │   ├── SchedulerConfig.java       # Async scheduler configuration
│           │   └── DataInitializer.java       # Database seed data on startup
│           │
│           ├── entity/               # JPA Entity Models (Database Tables)
│           │   ├── Ticket.java               # Ticket entity (main table)
│           │   ├── Engineer.java             # Engineer user entity
│           │   ├── ManagementUser.java        # Management user entity
│           │   ├── DepartmentEntity.java      # Department entity
│           │   ├── TeamLead.java             # Team lead entity
│           │   └── KnowledgeBase.java         # KB article entity
│           │
│           ├── repository/           # Spring Data JPA Repositories
│           │   ├── TicketRepository.java      # Ticket CRUD + custom queries
│           │   ├── EngineerRepository.java    # Engineer CRUD + custom queries
│           │   ├── ManagementUserRepository.java # Manager CRUD + queries
│           │   ├── DepartmentRepository.java  # Department CRUD
│           │   ├── TeamLeadRepository.java    # Team lead CRUD
│           │   └── KnowledgeBaseRepository.java # KB article CRUD + search
│           │
│           ├── service/              # Business Logic Services
│           │   ├── TicketService.java         # Core ticket operations
│           │   ├── EngineerService.java       # Engineer business logic
│           │   ├── ResolutionService.java     # Ticket resolution workflow
│           │   ├── ManagementAuthService.java # Management authentication
│           │   ├── ManagementUserService.java  # User CRUD operations
│           │   ├── DepartmentFilterService.java # Department-based filtering
│           │   ├── NormalizationService.java   # Data normalization
│           │   ├── EmailIngestionService.java  # IMAP email polling & parsing
│           │   ├── PasswordMigrationService.java # BCrypt password migration
│           │   └── DatabaseInitializationService.java # DB init helper
│           │
│           ├── controller/           # REST API Controllers
│           │   ├── EngineerAuthController.java      # Engineer login/logout
│           │   ├── ManagementAuthController.java     # Management login/logout
│           │   ├── EngineerController.java           # Engineer operations API
│           │   ├── DashboardController.java          # Engineer dashboard API
│           │   ├── DashboardStatsController.java     # Quick stats API
│           │   ├── ManagementDashboardController.java # Management analytics API
│           │   ├── ClassificationController.java     # NLP classification API
│           │   ├── SlaController.java                # SLA monitoring API
│           │   ├── KnowledgeBaseController.java      # KB article API
│           │   ├── SelfServiceController.java        # Self-service API
│           │   ├── DepartmentController.java         # Department CRUD API
│           │   ├── TeamController.java               # Team management API
│           │   ├── UserManagementController.java     # User CRUD API
│           │   ├── ChatbotTicketController.java      # Chatbot ticket API
│           │   └── VerificationController.java       # AI verification API
│           │
│           ├── dto/                  # Data Transfer Objects
│           │   ├── ApiResponse.java          # Standardized API response
│           │   ├── TicketResponse.java        # Ticket response DTO
│           │   └── ChatbotTicketRequest.java  # Chatbot request DTO
│           │
│           ├── constants/            # Enums & Constants
│           │   ├── Category.java             # Ticket categories enum
│           │   ├── SubCategory.java          # Sub-categories enum
│           │   ├── Priority.java             # Priority levels enum
│           │   ├── Department.java           # Departments enum
│           │   ├── Team.java                 # IT Teams enum
│           │   ├── EscalationLevel.java      # Escalation levels enum
│           │   ├── ResolutionStatus.java      # Resolution status enum
│           │   ├── SlaConfiguration.java     # SLA time configs
│           │   └── TicketConstants.java       # General ticket constants
│           │
│           ├── nlp/                  # NLP & AI Classification Engine
│           │   ├── TextPreprocessingService.java  # Text cleaning & tokenization
│           │   ├── KeywordDictionaryService.java   # IT keyword dictionary (1000+)
│           │   ├── ClassificationService.java      # Category classification
│           │   ├── IntentDetectionService.java     # User intent detection
│           │   ├── PriorityEngine.java             # Priority calculation
│           │   └── TeamAssignmentService.java      # Smart team routing
│           │
│           ├── sla/                  # SLA Monitoring Module
│           │   ├── SlaCalculationService.java      # SLA deadline calculation
│           │   ├── SlaMonitoringScheduler.java     # Scheduled SLA checks
│           │   ├── SlaNotificationService.java     # SLA breach notifications
│           │   └── EscalationService.java          # Multi-level escalation
│           │
│           ├── selfservice/          # Self-Service Resolution Module
│           │   ├── SelfServiceOrchestrator.java    # Main orchestrator
│           │   ├── SelfServiceEngine.java          # Matching engine
│           │   ├── KnowledgeBaseService.java       # KB search & match
│           │   ├── SolutionDeliveryService.java    # Email solution delivery
│           │   ├── UserResponseHandler.java        # Handle user responses
│           │   ├── TicketClosureService.java       # Auto-close resolved tickets
│           │   └── FallbackAssignmentService.java  # Fallback to engineer
│           │
│           ├── notification/         # Email Notification Module
│           │   ├── NotificationService.java        # Main notification dispatcher
│           │   ├── NotificationEvent.java          # Event types enum
│           │   ├── NotificationEmailService.java   # Email sending service
│           │   ├── EmailTemplateService.java       # Base templates
│           │   ├── HtmlEmailTemplateService.java   # HTML email builder
│           │   ├── ProfessionalEmailTemplateService.java # Premium templates
│           │   └── SimpleEmailTemplateService.java  # Fallback templates
│           │
│           ├── scheduler/            # Background Schedulers
│           │   └── SelfServiceTimeoutScheduler.java # Self-service timeout checker
│           │
│           ├── analytics/            # Analytics Module
│           │   ├── controller/       # Analytics API endpoints
│           │   ├── dto/              # Analytics data objects
│           │   ├── repository/       # Analytics queries
│           │   └── service/          # Analytics calculations
│           │
│           ├── verification/         # AI Verification Module
│           │   ├── AIVerificationService.java     # Resolution verification
│           │   └── VerificationScheduler.java     # Scheduled verification
│           │
│           └── exception/            # Exception Handling
│               └── GlobalExceptionHandler.java    # Global error handler
│
└── README.md                         # This file
```

---

## 📋 Prerequisites

Before running this project, ensure you have the following installed:

| Software | Minimum Version | Download Link |
|---|---|---|
| **Java JDK** | 17 or higher | [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://adoptium.net/) |
| **Node.js** | 16.x or higher | [Node.js](https://nodejs.org/) |
| **npm** | 8.x or higher | Comes with Node.js |
| **MySQL** | 8.0 or higher | [MySQL](https://dev.mysql.com/downloads/) |
| **Maven** | 3.8 or higher | [Apache Maven](https://maven.apache.org/download.cgi) |
| **Git** | 2.x | [Git](https://git-scm.com/downloads) |

### Verify Installations

```bash
# Check Java
java -version

# Check Node.js and npm
node -v
npm -v

# Check MySQL
mysql --version

# Check Maven
mvn -v

# Check Git
git --version
```

---

## 🚀 Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/IT-SERVICE.git
cd IT-SERVICE
```

### 2. Database Setup

```bash
# Login to MySQL
mysql -u root -p

# Create the database
CREATE DATABASE powergrid_tickets;

# Import the database schema and seed data
USE powergrid_tickets;
SOURCE database/powergrid.sql;

# Exit MySQL
EXIT;
```

### 3. Backend Configuration

Edit the database credentials in `backend/src/main/resources/application.properties`:

```properties
# Update with YOUR MySQL credentials
spring.datasource.url=jdbc:mysql://localhost:3306/powergrid_tickets?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

#### Email Configuration (Optional — for email features)

```properties
# Gmail IMAP (for ticket ingestion)
mail.imap.username=your-email@gmail.com
mail.imap.password=your-app-password

# Gmail SMTP (for sending notifications)
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

> **Note:** For Gmail, you need to generate an [App Password](https://myaccount.google.com/apppasswords). Regular passwords won't work with IMAP/SMTP.

### 4. Frontend Setup (Install Dependencies)

```bash
cd frontend
npm install
```

This single command installs all 7 required packages:
- `react`, `react-dom` — Core React library
- `react-router-dom` — Page routing
- `react-scripts` — Build tools (Create React App)
- `chart.js`, `react-chartjs-2` — Charts and graphs
- `react-hot-toast` — Toast notifications
- `web-vitals` — Performance monitoring

### 5. Backend Setup (Maven Dependencies)

```bash
cd backend
mvn clean install -DskipTests
```

This downloads all Java dependencies defined in `pom.xml`:
- Spring Boot Starter Web (REST APIs)
- Spring Data JPA (Database ORM)
- Spring Security (Password encryption)
- Spring Mail (Email sending)
- Spring Validation (Input validation)
- MySQL Connector/J (Database driver)
- Apache PDFBox (PDF generation)

---

## ▶️ Running the Application

### Start Backend (Terminal 1)

```bash
cd backend
mvn spring-boot:run
```

The backend starts at **http://localhost:8080**.

You should see:
```
================================================
  IT TICKET MANAGEMENT SYSTEM - STARTED
  Phase 1: Unified Ticket Ingestion
  Server running on: http://localhost:8080
================================================
```

### Start Frontend (Terminal 2)

```bash
cd frontend
npm start
```

The frontend starts at **http://localhost:3000** and automatically opens in your browser.

You should see:
```
Compiled successfully!
Local:    http://localhost:3000
```

> **Important:** The frontend proxies all `/api/*` requests to `http://localhost:8080` (configured in `package.json`). Both servers must be running simultaneously.

### Quick Start Commands Summary

| Action | Command | Directory |
|---|---|---|
| Start Backend | `mvn spring-boot:run` | `backend/` |
| Start Frontend | `npm start` | `frontend/` |
| Build Frontend (Production) | `npm run build` | `frontend/` |
| Run Tests (Frontend) | `npm test` | `frontend/` |
| Install Frontend Dependencies | `npm install` | `frontend/` |
| Install Backend Dependencies | `mvn clean install -DskipTests` | `backend/` |

---

## 👥 User Roles & Access

The system supports two main portals with four roles:

### Engineer Portal

| Role | Access Level | Capabilities |
|---|---|---|
| **Engineer** | Individual | View assigned tickets, resolve tickets, add notes, escalate, view knowledge base, track SLA status, manage profile |

### Management Portal

| Role | Access Level | Capabilities |
|---|---|---|
| **IT Coordinator** | Global (Super Admin) | Full system access, all analytics, manage all users & departments, generate reports, view audit logs |
| **Department Head** | Department-Scoped | View department analytics, manage engineers in department, generate department reports |
| **Team Lead** | Team-Scoped | View team tickets, manage team workload, handle escalations |

### Authentication Flow
1. User selects portal type (Engineer / Management) on login page
2. Credentials are validated against the database (BCrypt hashed passwords)
3. Session token is generated and stored in `sessionStorage`
4. Session token is sent with every API request via `X-Session-Token` header
5. Unauthorized requests (401) redirect to login page

---

## 🖥️ Frontend Pages

### Public Pages

| Route | Page | Description |
|---|---|---|
| `/` | Welcome Page | Landing page with animated feature showcase and auto-redirect to login |
| `/login` | Login Page | Dual-portal login (Engineer Portal / Management Portal) |

### Engineer Portal Pages

| Route | Page | Description |
|---|---|---|
| `/engineer/dashboard` | Dashboard | Overview stats (assigned tickets, resolved, SLA compliance), recent tickets list |
| `/engineer/tickets` | My Tickets | Full list of assigned tickets with priority/status filters, search functionality |
| `/engineer/ticket/:ticketId` | Resolve Ticket | Detailed ticket workspace — view info, add progress notes, submit resolution, escalate |
| `/engineer/knowledge` | Knowledge Base | Browse and search IT knowledge base articles |
| `/engineer/sla` | SLA Status | View SLA compliance for assigned tickets, deadline tracking |
| `/engineer/profile` | Profile | View and edit personal profile information |
| `/engineer/settings` | Settings | Account settings (change password, notification preferences) |

### Management Portal Pages

| Route | Page | Description |
|---|---|---|
| `/management/dashboard` | Analytics Dashboard | Live statistics with charts — ticket trends, SLA rates, team performance |
| `/management/tickets` | Tickets Overview | View all tickets across all teams with advanced filters |
| `/management/engineers` | Engineer Performance | Individual engineer metrics — resolution time, ticket count, SLA rate |
| `/management/categories` | Category Analysis | Ticket distribution by category with pie/bar charts |
| `/management/recent` | Recent Tickets | Real-time feed of newly created/updated tickets |
| `/management/sla` | SLA Analysis | SLA compliance rates, breach trends, priority breakdown |
| `/management/departments` | Department Reports | Per-department analytics and comparisons |
| `/management/department-mgmt` | Department Management | Create, edit, delete departments |
| `/management/response-time` | Response Time | Average response/resolution time analytics |
| `/management/workload` | Team Workload | Ticket distribution across teams and engineers |
| `/management/escalations` | Escalations | Escalation frequency, patterns, and history |
| `/management/audit` | Audit Log | Complete system activity trail with filters |
| `/management/reports` | Reports | Generate and download PDF reports |
| `/management/settings` | Settings | System-wide configuration settings |
| `/management/users` | User Management | Add, edit, deactivate engineers and managers |

---

## 🔌 Backend API Endpoints

### Authentication APIs

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/engineer/login` | Engineer login (username + password) |
| `POST` | `/api/management/auth/login` | Management login (username + password) |
| `GET` | `/api/management/auth/validate` | Validate management session token |
| `POST` | `/api/management/auth/logout` | Management logout |

### Engineer APIs

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/engineer/dashboard` | Get engineer dashboard data |
| `GET` | `/api/engineer/tickets` | Get assigned tickets (with filters) |
| `GET` | `/api/engineer/ticket/{id}` | Get single ticket details |
| `POST` | `/api/engineer/ticket/{id}/start` | Start working on a ticket |
| `POST` | `/api/engineer/ticket/{id}/resolve` | Submit ticket resolution |
| `POST` | `/api/engineer/ticket/{id}/notes` | Add progress notes |
| `POST` | `/api/engineer/ticket/{id}/escalate` | Escalate ticket |
| `GET` | `/api/engineer/stats` | Get engineer statistics |
| `GET` | `/api/engineer/team-leads` | Get list of team leads |
| `PUT` | `/api/engineer/profile` | Update engineer profile |

### Management Dashboard APIs

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/management/dashboard` | Get dashboard summary with charts |
| `GET` | `/api/management/dashboard/recent` | Get recent tickets |
| `GET` | `/api/management/dashboard/engineers` | Get engineer performance data |
| `GET` | `/api/management/dashboard/categories` | Get category analysis data |
| `GET` | `/api/management/dashboard/sla` | Get SLA analysis data |
| `GET` | `/api/management/dashboard/summary` | Get department summary data |
| `POST` | `/api/management/dashboard/reports/generate` | Generate analytics report |
| `GET` | `/api/management/dashboard/reports/download` | Download PDF report |
| `PUT` | `/api/management/profile` | Update management profile |

### User Management APIs

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/management/users/engineers` | List all engineers |
| `POST` | `/api/management/users/engineers` | Add new engineer |
| `PUT` | `/api/management/users/engineers/{id}` | Update engineer |
| `DELETE` | `/api/management/users/engineers/{id}` | Delete/deactivate engineer |
| `GET` | `/api/management/users/managers` | List all managers |
| `POST` | `/api/management/users/managers` | Add new manager |
| `PUT` | `/api/management/users/managers/{id}` | Update manager |
| `DELETE` | `/api/management/users/managers/{id}` | Delete/deactivate manager |

### Department APIs

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/management/departments` | List all departments |
| `POST` | `/api/management/departments` | Create department |
| `PUT` | `/api/management/departments/{code}` | Update department |
| `DELETE` | `/api/management/departments/{code}` | Delete department |

### Knowledge Base APIs

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/knowledge-base` | Get all KB articles |
| `GET` | `/api/knowledge-base/search?q=` | Search KB articles |

### SLA APIs

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/sla/dashboard` | Get SLA dashboard data |

### Self-Service APIs

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/self-service/verify/{token}` | Verify self-service resolution |
| `POST` | `/api/self-service/respond` | Handle user response to solution |

### Chatbot API

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/chatbot/ticket` | Create ticket from chatbot |

### Audit Log API

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/management/audit-log` | Get audit log entries |

---

## 🗄️ Database Schema

### Core Tables

#### `tickets` — Main Ticket Table
| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK) | Auto-generated ticket ID |
| ticket_number | VARCHAR | Unique ticket number (e.g., TKT-20260419-001) |
| subject | VARCHAR | Ticket subject/title |
| description | TEXT | Full ticket description |
| category | ENUM | NETWORK, HARDWARE, SOFTWARE, SECURITY, DEVOPS, TELECOM |
| sub_category | VARCHAR | Specific sub-category |
| priority | ENUM | CRITICAL, HIGH, MEDIUM, LOW |
| status | ENUM | OPEN, IN_PROGRESS, RESOLVED, CLOSED, REOPENED |
| source | ENUM | EMAIL, CHATBOT, MANUAL |
| sender_email | VARCHAR | Email of ticket raiser |
| assigned_engineer_id | BIGINT (FK) | Assigned engineer reference |
| assigned_team | VARCHAR | Assigned IT team |
| department | VARCHAR | Department code |
| sla_deadline | DATETIME | SLA deadline timestamp |
| escalation_level | INT | Current escalation level (0-3) |
| resolution_notes | TEXT | Resolution description |
| resolved_at | DATETIME | Resolution timestamp |
| created_at | DATETIME | Ticket creation timestamp |
| updated_at | DATETIME | Last update timestamp |

#### `engineers` — Engineer Users
| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK) | Engineer ID |
| name | VARCHAR | Full name |
| username | VARCHAR | Login username |
| password | VARCHAR | BCrypt hashed password |
| email | VARCHAR | Email address |
| department | VARCHAR | Department code |
| team | VARCHAR | IT team assignment |
| specialization | VARCHAR | Area of expertise |
| is_active | BOOLEAN | Account status |

#### `management_users` — Management Users
| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK) | Manager ID |
| name | VARCHAR | Full name |
| username | VARCHAR | Login username |
| password | VARCHAR | BCrypt hashed password |
| email | VARCHAR | Email address |
| role | ENUM | IT_COORDINATOR, DEPARTMENT_HEAD, TEAM_LEAD |
| department | VARCHAR | Department code |
| session_token | VARCHAR | Active session token |

#### `departments` — Department Registry
| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK) | Department ID |
| code | VARCHAR | Unique department code |
| name | VARCHAR | Department name |
| description | TEXT | Department description |

#### `knowledge_base` — KB Articles
| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK) | Article ID |
| title | VARCHAR | Article title |
| content | TEXT | Solution content |
| category | VARCHAR | Related category |
| sub_category | VARCHAR | Related sub-category |
| keywords | TEXT | Search keywords |
| times_used | INT | Usage count |

#### `team_leads` — Team Lead Registry
| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK) | Team lead ID |
| name | VARCHAR | Full name |
| email | VARCHAR | Email address |
| team | VARCHAR | Managed team |

---

## 🤖 NLP & AI Classification Engine

The backend includes a custom-built NLP pipeline for automatic ticket classification:

### Pipeline Flow

```
Incoming Ticket (Email/Chatbot)
        │
        ▼
┌──────────────────────┐
│ Text Preprocessing   │  → Lowercase, remove special chars, tokenize
│ TextPreprocessing     │  → Remove stopwords, extract key phrases
│ Service.java          │
└──────────┬───────────┘
           ▼
┌──────────────────────┐
│ Keyword Dictionary   │  → Match tokens against 1000+ IT keywords
│ KeywordDictionary     │  → Score by category: NETWORK, HARDWARE, etc.
│ Service.java          │  → Identify sub-category matches
└──────────┬───────────┘
           ▼
┌──────────────────────┐
│ Intent Detection     │  → Detect intent: REPORT_ISSUE, REQUEST_SERVICE
│ IntentDetection       │  → SEEK_INFORMATION, COMPLAINT, etc.
│ Service.java          │
└──────────┬───────────┘
           ▼
┌──────────────────────┐
│ Classification       │  → Combine keyword scores + intent
│ ClassificationService│  → Determine final Category & Sub-Category
│ .java                │  → Calculate confidence score
└──────────┬───────────┘
           ▼
┌──────────────────────┐
│ Priority Engine      │  → Analyze urgency keywords ("crash", "down")
│ PriorityEngine.java  │  → Assess business impact
│                      │  → Calculate: CRITICAL > HIGH > MEDIUM > LOW
└──────────┬───────────┘
           ▼
┌──────────────────────┐
│ Team Assignment      │  → Map category to team (e.g., NETWORK → Network Team)
│ TeamAssignment        │  → Consider department and specialization
│ Service.java          │  → Fallback assignment if no match
└──────────────────────┘
```

### Supported Categories
| Category | Teams | Example Issues |
|---|---|---|
| NETWORK | Network Team | WiFi down, VPN issues, connectivity, DNS |
| HARDWARE | Hardware Team | Printer issues, laptop repairs, monitor problems |
| SOFTWARE | Software Team | App crashes, installation, license issues |
| SECURITY | Security Team | Password reset, access control, malware |
| DEVOPS | DevOps Team | CI/CD pipeline, deployment, server issues |
| TELECOM | Telecom Team | Phone system, video conferencing, speaker issues |

---

## ⏱️ SLA Monitoring & Escalation

### SLA Time Limits (Default)

| Priority | Resolution Deadline | Warning At | Level 1 At | Level 2 At | Level 3 At |
|---|---|---|---|---|---|
| CRITICAL | 2 hours | 1.5 hrs (75%) | 2 hrs (100%) | 3 hrs (150%) | 4 hrs (200%) |
| HIGH | 4 hours | 3 hrs (75%) | 4 hrs (100%) | 6 hrs (150%) | 8 hrs (200%) |
| MEDIUM | 8 hours | 6 hrs (75%) | 8 hrs (100%) | 12 hrs (150%) | 16 hrs (200%) |
| LOW | 24 hours | 18 hrs (75%) | 24 hrs (100%) | 36 hrs (150%) | 48 hrs (200%) |

### Escalation Actions

| Level | Trigger | Action |
|---|---|---|
| **Warning** | 75% of SLA time elapsed | Email alert to assigned engineer |
| **Level 1** | 100% — SLA breached | Escalate to Team Lead |
| **Level 2** | 150% — Still unresolved | Escalate to Manager |
| **Level 3** | 200% — Critical breach | Escalate to IT Admin/Director |

### Monitoring Schedule
- **SLA check interval:** Every 10 minutes (configurable)
- **Daily summary report:** 8:00 AM (cron: `0 0 8 * * ?`)

---

## 📧 Email Integration

### Inbound (Ticket Ingestion via IMAP)
- Polls Gmail inbox every **30 seconds** (configurable)
- Processes up to **10 emails per cycle** (configurable)
- Filters by subject keywords: `ticket, support, help, issue, problem, error, not working, urgent, request, vpn, password, wifi, locked, crash`
- Automatically creates tickets from matching emails
- Marks processed emails as read

### Outbound (Notifications via SMTP)
- Uses Gmail SMTP (port 587 with TLS)
- Professional HTML email templates
- Sends notifications for all ticket lifecycle events
- Separate email chains for engineers, team leads, managers, and admins

---

## 🔧 Self-Service Resolution

### How It Works
1. New ticket arrives (from email/chatbot)
2. NLP engine classifies the ticket
3. Self-service engine searches Knowledge Base for matching solutions
4. If confidence score ≥ 60% (configurable), solution is emailed to user
5. User clicks "Yes, this resolved my issue" or "No, I need an engineer"
6. If "Yes" → ticket auto-closed ✅
7. If "No" → ticket escalated to engineer 🔧
8. If no response within 24 hours → auto-escalated to engineer ⏰

### Configuration
```properties
selfservice.enabled=true
selfservice.skip.critical=true          # Skip for CRITICAL tickets
selfservice.confidence.threshold=0.6     # Minimum match confidence
selfservice.response.timeout.hours=24    # Hours before auto-escalation
selfservice.response.reminder.hours=12   # Reminder before timeout
```

---

## 🔔 Notification System

### Email Notification Events

| Event | Recipients | When |
|---|---|---|
| Ticket Created | Ticket raiser (sender) | New ticket from email/chatbot |
| Ticket Assigned | Assigned engineer | Ticket assigned to engineer |
| SLA Warning | Assigned engineer | 75% of SLA time elapsed |
| Ticket Escalated (L1) | Team Lead | SLA breached (100%) |
| Ticket Escalated (L2) | Manager | SLA still breached (150%) |
| Ticket Escalated (L3) | IT Admin | Critical breach (200%) |
| Ticket Resolved | Ticket raiser | Engineer resolves ticket |
| Ticket Closed | Ticket raiser | Ticket officially closed |
| Ticket Reopened | Assigned engineer | Previously closed ticket reopened |

### Email Templates
The system includes **3 tiers** of email templates:
1. **ProfessionalEmailTemplateService** — Premium branded HTML templates (117KB)
2. **HtmlEmailTemplateService** — Standard HTML templates
3. **SimpleEmailTemplateService** — Fallback plain-text templates

---

## ⚙️ Configuration Guide

All configuration is centralized in `backend/src/main/resources/application.properties`:

### Key Configuration Sections

| Section | Properties | Description |
|---|---|---|
| **Database** | `spring.datasource.*` | MySQL connection URL, username, password |
| **Email Ingestion** | `mail.imap.*` | Gmail IMAP server settings for polling |
| **Email Sending** | `spring.mail.*` | Gmail SMTP settings for notifications |
| **Self-Service** | `selfservice.*` | Self-service resolution settings |
| **SLA Monitoring** | `sla.monitoring.*` | SLA check intervals and thresholds |
| **SLA Notifications** | `sla.notification.*` | Escalation email recipients |
| **Notifications** | `notification.*` | Global notification settings |
| **Logging** | `logging.*` | Log levels and file settings |
| **Server** | `server.port` | Backend port (default: 8080) |

---

## 🐛 Troubleshooting

### Common Issues

| Issue | Solution |
|---|---|
| `npm install` fails | Delete `node_modules` folder and `package-lock.json`, then run `npm install` again |
| Port 8080 already in use | Kill the process: `netstat -ano \| findstr :8080` then `taskkill /F /PID <pid>` |
| Port 3000 already in use | Kill the process or set `PORT=3001 npm start` |
| MySQL connection refused | Ensure MySQL is running and credentials in `application.properties` are correct |
| Browser popup "Sign in" | Fixed in SecurityConfig.java — HTTP Basic Auth is disabled |
| `npm warn deprecated` messages | Safe to ignore — these are sub-dependency warnings, not errors |
| Emails not being sent | Ensure you're using Gmail App Password (not regular password) |
| IMAP connection fails | Check firewall settings and Gmail "Less secure apps" or App Password |

### Log Files
- **Backend logs:** `backend/logs/it-ticket-system.log`
- **Frontend logs:** `frontend/frontend.log`
- **Console output:** Check the terminal where `mvn spring-boot:run` or `npm start` is running

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "Add your feature"`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

### Code Style
- **Frontend:** React functional components with hooks, vanilla CSS
- **Backend:** Spring Boot conventions, service-repository pattern, JPA entities

---

## 📄 License

This project is developed for **POWERGRID Corporation of India Limited** as part of the IT infrastructure modernization initiative.

---

## 👨‍💻 Authors

**IT Service Management Team**

---

<p align="center">
  <b>⚡ PowerGrid ITSM — Intelligent IT Service Management ⚡</b><br>
  <i>Automating IT Support with AI-Powered Classification, Self-Service Resolution & Real-Time SLA Monitoring</i>
</p>
