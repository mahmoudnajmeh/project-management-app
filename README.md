# 📊 ProjectFlow - Project Management Application

**A full-stack application with Spring AI, Data Engineering pipeline (Kafka + Spring Batch), real-time analytics, team collaboration, and task tracking.**

Built with **Spring Boot 3.2.0** and **React 18 + TypeScript**

## Author
**Mahmoud Najmeh**  
<img src="https://avatars.githubusercontent.com/u/78208459?u=c3f9c7d6b49fc9726c5ea8bce260656bcb9654b3&v=4" width="200px" style="border-radius: 50%;">

------------------------------------------------------------------------

## 📋 Table of Contents
- [🏗️ Project Architecture](#️-project-architecture)
- [🖥️ System Architecture Diagram](#️-system-architecture-diagram)
- [🗄️ Database Structure](#️-database-structure)
- [🔐 Authentication Flow Diagram](#-authentication-flow-diagram)
- [📡 Real-time Pipeline Diagram](#-real-time-pipeline-diagram)
- [✨ Features](#-features)
- [🛠️ Tech Stack](#️-tech-stack)
- [📁 Project Structure](#-project-structure)
- [🚀 Quick Start](#-quick-start)
- [📡 API Endpoints](#-api-endpoints)
- [🐳 Docker Deployment](#-docker-deployment)
- [🤝 Contributing](#-contributing)

---

# 🏗️ Project Architecture

![Project Architecture] <img width="7615" height="4746" alt="Image" src="https://github.com/user-attachments/assets/7c398115-f1cb-42ea-967b-2d1372a4c793" />

*Diagram shows the overall architecture of the ProjectFlow application including frontend, backend, database, and external services.*

---

# 🖥️ System Architecture Diagram

![System Architecture Diagram] <img width="9271" height="4934" alt="Image" src="https://github.com/user-attachments/assets/4891ae7b-7c33-4428-a9f2-78f58a7fbe38" />

*Complete system architecture showing all components and their interactions.*

---

# 🗄️ Database Structure

![Database Structure] <img width="6938" height="8128" alt="Image" src="https://github.com/user-attachments/assets/e9914865-540a-4028-a018-bec65858139c" />

Complete database schema showing all tables and their relationships.

## Entity Relationship Diagram

---

## Database Tables Detail

![Database Tables Detail] <img width="1698" height="1282" alt="Image" src="https://github.com/user-attachments/assets/81e00ebe-ecaf-4436-a1a5-edadd8ff8b6f" />

Detailed view of all database tables with column names and data types.

---

# 🔐 Authentication Flow Diagram

![Authentication Flow Diagram] <img width="4861" height="4801" alt="Image" src="https://github.com/user-attachments/assets/cef2cd60-7437-40d1-9085-dc5cd9921590" />

Sequence diagram showing the complete authentication process including login, registration, and password reset.

---

# 📡 Real-time Pipeline Diagram

![Real-time Pipeline Diagram] <img width="7783" height="1314" alt="Image" src="https://github.com/user-attachments/assets/3f9bdada-4af5-4759-9e5a-cc9ae8fede94" />

Event-driven architecture showing Kafka pipeline for real-time metrics processing.

---

## Event Flow Description

| Step | Component | Description |
|------|------------|-------------|
| 1 | ActivityEventPublisher | Captures user actions (task creation, completion, login) |
| 2 | Kafka Producer | Serializes events and sends to Kafka topic |
| 3 | Kafka Broker | Stores events partitioned by user_id for scalability |
| 4 | Kafka Consumer | Consumes events in real-time from project-metrics-group |
| 5 | RealTimeMetricsService | Updates in-memory counters (tasks created/completed today) |
| 6 | WebSocket Push | Pushes real-time updates to connected clients |
| 7 | Metrics Scheduler | Triggers daily batch job at 1 AM |
| 8 | Spring Batch | Calculates and stores daily metrics in database |

---

# ✨ Features

### 🤖 **Spring AI Integration** (⭐ Core Focus)
- AI-powered assistant for project management
- Intelligent task recommendations
- Automated responses and smart suggestions

### 📊 **Data Engineering & Analytics** (⭐ Core Focus)
- **Apache Kafka Event Streaming** - Real-time capture and processing of user activities
- **Spring Batch Processing** - Daily metrics calculation and trend analysis
- **Real-time Metrics Dashboard** - Live task completion rates and team velocity
- **User Activity Pipeline** - Event-driven architecture for performance tracking

## 🔐 Authentication & Security
- JWT-based authentication with refresh tokens
- OAuth2 integration (Google, GitHub)
- Password reset via email
- Role-based access control (USER, ADMIN)

## 👥 Team Collaboration
- Create and manage teams
- Add/remove team members
- Team profile pictures
- Real-time team chat with WebSocket

## 📋 Project Management
- Create, update, delete projects
- Project status tracking (PLANNED, IN_PROGRESS, COMPLETED)
- Assign projects to teams
- Project-level task organization

## ✅ Task Tracking
- Task assignment with priorities (HIGH, MEDIUM, LOW)
- Due date tracking
- Task status (TODO, IN_PROGRESS, DONE)
- Real-time task updates via WebSocket

## 📅 Calendar Integration
- Visual timeline of tasks and events
- Drag-and-drop event creation
- Color-coded event types

## 📈 Analytics & Reporting
- Real-time metrics dashboard
- Kafka event streaming for user activities
- Spring Batch daily metrics calculation
- Team velocity and productivity scores

## 💬 Real-time Features
- WebSocket chat between team members
- Live notifications for task assignments
- Activity feed
- Unread message indicators

## 🎨 UI/UX
- Dark/Light mode toggle
- Fully responsive design
- Profile picture upload
- Global search (projects, tasks, teams, users)
- Toast notifications

---

# 🛠️ Tech Stack

## Backend

| Technology | Version | Purpose |
|------------|----------|----------|
| Spring Boot | 3.2.0 | Core framework |
| Spring Security | 6.2.0 | Authentication & authorization |
| Spring Data JPA | 3.2.0 | Database ORM |
| Spring WebSocket | 3.2.0 | Real-time communication |
| Spring Kafka | 3.1.0 | Event streaming |
| Spring Batch | 5.1.0 | Batch processing |
| MySQL | 8.0+ | Relational database |
| JWT | 0.11.5 | Token-based authentication |
| Lombok | 1.18.30 | Boilerplate reduction |

## Frontend

| Technology | Version | Purpose |
|------------|----------|----------|
| React | 18.3.1 | UI framework |
| TypeScript | 5.6.2 | Type safety |
| Tailwind CSS | 3.4.17 | Styling |
| React Router DOM | 6.28.0 | Routing |
| React Hook Form | 7.54.2 | Form management |
| Zod | 3.24.1 | Validation |
| Axios | 1.7.9 | HTTP client |
| STOMPjs | 2.3.3 | WebSocket protocol |
| SockJS-client | 1.5.1 | WebSocket fallback |
| Lucide React | 0.468.0 | Icons |
| Vite | 5.4.11 | Build tool |

## DevOps
- Docker & Docker Compose
- GitHub Actions / GitLab CI
- Jenkins pipeline

---

# 📁 Project Structure

```text
project-management-app/
│
├── backend/
│   ├── src/main/java/.../project_management_app/
│   │   ├── config/           # Security, WebSocket, Kafka config
│   │   ├── controller/       # REST API controllers
│   │   ├── dto/              # Data Transfer Objects
│   │   ├── entity/           # JPA entities
│   │   ├── pipeline/         # Kafka event pipeline
│   │   │   ├── event/        # Event publishers
│   │   │   ├── kafka/        # Producers & Consumers
│   │   │   ├── model/        # Event models
│   │   │   └── scheduler/    # Batch job scheduler
│   │   ├── repository/       # JPA repositories
│   │   ├── service/          # Business logic
│   │   └── util/             # Utilities (JWT)
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── uploads/              # File storage
│   │   ├── chat/
│   │   ├── profile-pictures/
│   │   └── team-photos/
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── api/              # API service layer
│   │   ├── assets/           # Static assets
│   │   ├── components/       # React components
│   │   │   ├── ai/           # AI Assistant
│   │   │   ├── auth/         # Login/Register
│   │   │   ├── calendar/     # Calendar
│   │   │   ├── common/       # Reusable UI
│   │   │   ├── dashboard/    # Dashboard
│   │   │   ├── layout/       # Layout, Header
│   │   │   ├── profile/      # Profile settings
│   │   │   ├── projects/     # Projects
│   │   │   ├── tasks/        # Tasks
│   │   │   └── team/         # Team & chat
│   │   ├── context/          # React Context
│   │   ├── hooks/            # Custom hooks
│   │   ├── pages/            # Page components
│   │   ├── styles/           # Global styles
│   │   ├── types/            # TypeScript definitions
│   │   ├── utils/            # Helper functions
│   │   ├── App.tsx
│   │   └── main.tsx
│   ├── public/
│   ├── .env
│   ├── package.json
│   ├── tailwind.config.js
│   ├── tsconfig.json
│   └── vite.config.ts
│
├── .github/workflows/
├── docker-compose.yml
├── Dockerfile
└── README.md
```

---

# 🚀 Quick Start

## Prerequisites
- Java 21+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

## 1. Clone & Setup Backend

```bash
git clone https://github.com/yourusername/project-management-app.git
cd project-management-app

mysql -u root -p
CREATE DATABASE project_management_app;
```

## 2. Configure application.properties

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/project_management_app
spring.datasource.username=root
spring.datasource.password=yourpassword
jwt.secret=your_jwt_secret_key_here
```

## 3. Run Backend

```bash
mvn clean install
mvn spring-boot:run

# Backend: http://localhost:8080
```

## 4. Run Frontend

```bash
cd frontend
npm install
npm run dev

# Frontend: http://localhost:3000
```

---

# 📡 API Endpoints

| Category | Method | Endpoint | Description |
|----------|---------|-----------|-------------|
| Auth | POST | /api/auth/login | User login |
| Auth | POST | /api/auth/register | User registration |
| Auth | POST | /api/auth/forgot-password | Request password reset |
| Auth | POST | /api/auth/reset-password | Reset password |
| Users | GET | /api/users/me | Get current user |
| Users | PUT | /api/users/me | Update profile |
| Users | POST | /api/users/me/profile-picture | Upload avatar |
| Users | POST | /api/users/me/change-password | Change password |
| Teams | GET | /api/teams/my-teams | Get my teams |
| Teams | POST | /api/teams | Create team |
| Teams | GET | /api/teams/{id}/members | Get team members |
| Projects | GET | /api/projects/my-projects | Get my projects |
| Projects | POST | /api/projects | Create project |
| Tasks | GET | /api/tasks/my-tasks | Get my tasks |
| Tasks | POST | /api/tasks | Create task |
| Search | GET | /api/search?q={query} | Global search |

---

# 🐳 Docker Deployment

```bash
# Build and run
docker-compose up -d

# Stop containers
docker-compose down

# View logs
docker-compose logs -f
```

---

# 📄 Image Placeholders Reference

When adding your images to the documentation, use these exact filenames:

| Placeholder | Description | Recommended Content |
|-------------|-------------|---------------------|
| ./project-architecture.png | Project Architecture Diagram | High-level architecture overview |
| ./system-architecture.png | System Architecture Diagram | Detailed component interaction |
| ./database-structure.png | Database Structure | Complete database schema |
| ./database-tables-detail.png | Database Tables Detail | All tables with columns |
| ./authentication-flow.png | Authentication Flow Diagram | Login/register/password reset sequence |
| ./realtime-pipeline.png | Real-time Pipeline Diagram | Kafka event flow diagram |

---

# 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push branch (`git push origin feature/amazing`)
5. Open Pull Request

---

# 📄 License

MIT License

---

Built with ❤️ using Spring Boot, React, and TypeScript
