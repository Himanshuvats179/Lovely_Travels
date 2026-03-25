# Lovely Travels 

### 🌐 Live Demo: [https://lovely-travels.vercel.app](https://lovely-travels.vercel.app)

**Lovely Travels** is a scalable, full-stack travel platform (MakeMyTrip clone) featuring real-time cab booking, hotel management system, and multi-role dashboards.

## 🔗 Live Deployment

| Component | Status | URL |
| :--- | :--- | :--- |
| **Frontend (React)** | ![Vercel](https://img.shields.io/badge/Vercel-Live-success?logo=vercel) | [https://lovely-travels.vercel.app](https://lovely-travels.vercel.app) |
| **Backend (Spring Boot)** | ![Render](https://img.shields.io/badge/Render-Live-success?logo=render) | [https://mmt-backend-43he.onrender.com](https://mmt-backend-43he.onrender.com) |

---

##  Features & Functionality

### 1.  Secure Authentication
- **JWT & OAuth2**: Integrated Google OAuth and JWT-based session management.
- **Login**: Login through Email , Phone Number , Google Account.
- **Role-Based Access Control (RBAC)**: Specific interfaces for **Users**, **Drivers**, **Hotel Owners**, and **Admins**.

### 2.  Real-time Cab Booking
- **Live Location Tracking**: Powered by **WebSockets** (StompJS/SockJS) for real-time driver-passenger interaction.
- **Interactive Maps**: Uses **Leaflet** and **OSRM** for accurate road-distance and route calculations.
- **3-Tier Failsafe**: Intelligent fallback system (OSRM -> Haversine -> Hardcoded) for distance calculations.

### 3.  Hotel Management System
- **For Users**: Browse, filter, and book hotels with a seamless checkout flow.
- **For Owners**: Provision properties, manage room inventory, and upload property media.

### 4.  Modern Tech Stack
- **Frontend**: React 19, Vite, React Router 7, Axios, Leaflet, Tailwind CSS (via components).
- **Backend**: Java 21, Spring Boot 3.5, Spring Security, Spring Data JPA/MongoDB/Redis.
- **Databases**: PostgreSQL (Relational), MongoDB (NoSQL), Redis (Caching/Sessions).
- **Other**: Razorpay (Payments), Cloudinary (Media), Google Cloud Storage.

---

##  Deployment & DevOps

The project is fully containerized and automated:
- **Docker**: Each service contains its own `Dockerfile` for easy local and cloud deployment.
- **Docker Compose**: Orchestrates the entire app (Frontend, Backend, DBs) with a single command.
- **Blueprints**: Uses Render Blueprints for automated backend scaling and Vercel for fast frontend delivery.

##  Project Structure

- `/frontend` - React + Vite frontend source code.
- `/backend` - Spring Boot Java backend source code.
- `render.yaml` - Render.com Blueprint configuration.
- `vercel.json` - Vercel production routing rules.
- `mmt-docker-compose.yml` - Local orchestration file.

---

##  Local Development

1. **Clone the repo**:
   ```bash
   git clone https://github.com/Himanshuvats179/Lovely_Travels.git
   ```
2. **Run via Docker Compose**:
   ```bash
   docker compose -f mmt-docker-compose.yml up --build
   ```
3. **Access the app**:
   - Frontend: `http://localhost:80`
   - Backend: `http://localhost:8080`
