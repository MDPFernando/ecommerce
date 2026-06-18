# 🌐 NET (Next Era Technologies) — Future-Grade Cybernetic E-Commerce Grid

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Database](https://img.shields.io/badge/H2%20/%20MySQL-Fallback-005C84?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)

Welcome to **NET (Next Era Technologies)**, a premium, production-ready e-commerce platform featuring a state-of-the-art cyberpunk/futuristic aesthetic. Engineered with **Spring Boot** and styled with a custom dark-mode, glassmorphism UI, this application offers a seamless grid experience for both shoppers and store administrators.

---

## 🌌 Core Features

### 🛒 Client Portal (Shopper Grid)
- **Holographic Landing Page:** Interactive homepage featuring a dynamic canvas-based galaxy starfield background, parallax cyber-banner sliders, and quick-search suggests.
- **Dynamic Search Suggest Engine:** Search bar with instantaneous search suggestions powered by asynchronously fetched API search data.
- **Tactical Deal Hub:** A dedicated carousel showcasing active discount offers, complete with product links, custom promotional badges, and glowing animations.
- **Product Catalogue:** Responsive catalog with advanced product search, sorting (price, date, rating), and instant category filtering.
- **Advanced Review System:** Customers can submit star ratings and detailed reviews, including uploading images of their new gear.
- **Quantum Cart System:** Real-time shopping cart handling with live price calculations and checkout staging.
- **Timeline-based Order Tracking:** An interactive timeline visualization showing order progress across four milestones: **Staged**, **Compiled**, **Dispatched**, and **Delivered**.
- **Active WhatsApp Notifications:** Automated transactional WhatsApp message format generation, sending customers a direct link to track their order on the grid.
- **Holographic Assistant:** Floating AI chat widget offering real-time guidance to users.

### 🔐 Admin Control Center
- **Mission-Critical Analytics:** High-level dashboard summarizing gross sales, order volume, catalog size, and active promotions.
- **Inventory & Category Control:** Full CRUD interface for managing categories and product listings, complete with drag-and-drop file upload handling.
- **Promotion Scheduler:** Administrative control panel to create, edit, or disable tactical promotional banners featured in the deal hub.
- **Order Dispatch Operations:** View user orders, monitor payment checkout details, and update shipping milestones in real-time.
- **Secure System Configurations:**
  - Rotate admin username and password.
  - Password visibility toggle (👁️ / 🙈) to safely inspect inputs.
  - Setup and edit the store's official WhatsApp Business configuration number.

---

## 🛠️ Technology Stack

- **Backend Framework:** Spring Boot (Java 21/24)
- **Security & Session Management:** Custom session filters with Bcrypt password hashing.
- **Persistence Layer:** Spring Data JPA + Hibernate ORM
- **Database Engine:**
  - **Local:** H2 File-based Fallback Database (`ecommerce_db`)
  - **Production:** MySQL / PostgreSQL support out-of-the-box (detects `DB_PROVIDER` automatically)
- **Template Engine:** Thymeleaf with dynamic layout dialect integrations.
- **Frontend Styling:** Vanilla HTML5, CSS3 Custom Properties (glassmorphism UI), and Modern JavaScript (ES6+).
- **Deployment Platform:** Dockerized setup ready for Railway, Fly.io, or AWS ECS.

---

## 🚀 Local Quickstart

### Prerequisites
- **Java JDK 21** or later installed.
- **Docker & Docker Compose** (Optional, for database/container virtualization).

### 1. Clone the Grid Repository
```bash
git clone https://github.com/MDPFernando/ecommerce.git
cd ecommerce
```

### 2. Run the Grid Server locally
You can start the application using the Gradle wrapper. It will automatically initialize the local H2 file database:
```bash
# Windows
.\gradlew bootRun

# macOS / Linux
./gradlew bootRun
```
The server will boot on [http://localhost:8081](http://localhost:8081).

### 3. Build Production Bundle
To compile and package the application into a runnable jar:
```bash
# Windows
.\gradlew build -x test

# macOS / Linux
./gradlew build -x test
```
The resulting jar is stored at `build/libs/ecommerce-0.0.1-SNAPSHOT.jar`.

---

## 🐋 Running with Docker

You can spin up the application along with a dedicated MySQL database container:
```bash
docker-compose up --build
```
This starts:
- **MySQL DB** on port `3306`
- **Spring Boot Grid** on port `8081`

---

## 🔑 Administrative Access & Recovery

### Default Admin Credentials
- **Username:** `admin`
- **Password:** `admin123`

> [!TIP]
> **Credential Lockout Recovery:** If you modify the admin credentials and lock yourself out, simply restart/redeploy the container. The startup seeder automatically resets/seeds the credentials back to `admin`/`admin123` (or the values specified in your environment variables).

---

## 🌐 Environment Variables

| Variable | Description | Default Value |
| :--- | :--- | :--- |
| `PORT` | The network port the server binds to | `8081` |
| `ADMIN_USERNAME` | Custom admin username seeded on startup | `admin` |
| `ADMIN_PASSWORD` | Custom admin password seeded on startup | `admin123` |
| `DB_PROVIDER` | Remote database type (`mysql` or `postgres`) | `h2` (fallback) |
| `JDBC_URL` | Database connection URL | `jdbc:h2:file:./ecommerce_db` |
| `DB_USER` | Database user account | `sa` |
| `DB_PASS` | Database password | `sa` |

---

## 📁 Repository Structure
```
.
├── src/main/java/com/example/ecommerce
│   ├── config/          # Configurations (JPA, Datasource, Web)
│   ├── controller/      # Controllers (Admin, Auth, Products, Orders)
│   ├── model/           # Domain Entities (User, Product, Order, Review)
│   ├── repository/      # JPA Data repositories
│   └── service/         # Business logic & file uploads
├── src/main/resources
│   ├── static/          # CSS Stylesheets, JS, Client Images
│   ├── templates/       # Thymeleaf HTML Templates
│   └── application.properties
├── Dockerfile           # Multi-stage production container build
├── docker-compose.yml   # Multi-container deployment configuration
└── build.gradle         # Build automation & dependencies file
```

---

## 📡 Deployment (Railway / Auto-Deploy)

This repository is optimized for **Railway** auto-deployment. Any changes pushed to the `main` branch trigger an automated build via the configured `Dockerfile` and instantly deploy the latest grid revision.

```bash
git add .
git commit -m "docs: create comprehensive grid readme documentation"
git push origin main
```
