# 🛒 Smart Retail Inventory Management System

A production-ready **Microservices Architecture** backend built with **Spring Boot**, designed for a smart retail inventory management environment. This project demonstrates real-world microservice patterns including service discovery, API gateway routing, asynchronous event-driven communication, JWT authentication, and a React frontend — all containerized with Docker.

---

## 📌 Project Overview

This system enables retail administrators and staff to manage products, orders, suppliers, restock requests, and receive real-time event-driven notifications — all through a single API Gateway entry point.

> Built as part of the **IT4020 – Modern Topics in IT** module at **SLIIT (Sri Lanka Institute of Information Technology)**, Year 4 Semester 1/2, 2026.

---

---

## 🧩 Microservices

| Service | Port | Database | Description |
|---|---|---|---|
| **API Gateway** | 9090 | — | Single entry point, routes all requests via Eureka |
| **Eureka Server** | 8761 | — | Service discovery & registry |
| **User Service** | 9091 | `smart_retail_users` | Auth, registration, role management |
| **Product Service** | 9092 | `smart_retail_products` | Product CRUD, stock management, low-stock detection |
| **Order Service** | 9093 | `smart_retail_orders` | Order lifecycle, inter-service stock coordination |
| **Supplier & Restock Service** | 9094 | `smart_retail_supplier` | Supplier management, restock request workflow |
| **Notification Service** | 9095 | `smart_retail_notifications` | Consumes RabbitMQ events, stores notification logs |
| **Frontend** | 3000 | — | React + Vite dashboard UI |

---

## ✨ Features

### 🔐 Authentication & Users
- User registration and login with **BCrypt** password hashing
- **JWT token-based** authentication
- Role-based access: `ADMIN` and `STAFF`
- Activate / deactivate user accounts

### 📦 Product Management
- Full CRUD for products (name, SKU, category, price, quantity, description)
- Increase / decrease stock endpoints
- **Low-stock detection** with configurable threshold per product
- Search by name, filter by category
- Auto-publishes `LOW_STOCK` event to RabbitMQ when stock drops below threshold

### 🧾 Order Management
- Create orders with multiple line items
- Inter-service stock validation and reduction via **OpenFeign**
- Order status lifecycle: `PENDING → COMPLETED / CANCELLED`
- Filter orders by user or status
- Publishes `ORDER_CREATED` and `ORDER_CANCELLED` events to RabbitMQ

### 🏭 Supplier & Restock
- Full CRUD for suppliers
- Restock request workflow: `PENDING → APPROVED → DELIVERED / CANCELLED`
- On delivery, automatically increases product stock via OpenFeign call to Product Service
- Supplier-product mapping with unit cost and lead time
- Publishes `RESTOCK_CREATED` and `RESTOCK_COMPLETED` events to RabbitMQ

### 🔔 Notification Service (Event-Driven)
- Consumes **RabbitMQ** events asynchronously:
  - `USER_REGISTERED`
  - `ORDER_CREATED`
  - `ORDER_CANCELLED`
  - `LOW_STOCK`
  - `RESTOCK_CREATED`
  - `RESTOCK_COMPLETED`
- Stores all notification logs in MongoDB
- REST endpoints to view and delete notifications

### 🌐 API Gateway
- **Single port entry point** (9090) — no need to know individual service ports
- Routes all traffic using **Spring Cloud Gateway + Eureka load balancing**
- Aggregated **Swagger UI** at `http://localhost:9090/swagger-ui.html` — all services in one place
- CORS configured for frontend integration

### 🔍 Service Discovery (Eureka)
- All services auto-register with Eureka on startup
- Gateway uses `lb://SERVICE-NAME` for dynamic load-balanced routing
- Eureka dashboard at `http://localhost:8761`

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Build Tool | Maven |
| Database | MongoDB 7 (separate DB per service) |
| Messaging | RabbitMQ 3 (with Management UI) |
| Service Discovery | Netflix Eureka (Spring Cloud) |
| API Gateway | Spring Cloud Gateway |
| Inter-Service Calls | OpenFeign |
| Authentication | JWT (jjwt 0.11.5) + BCrypt |
| API Documentation | SpringDoc OpenAPI / Swagger UI |
| Frontend | React + Vite + Nginx |
| Containerization | Docker + Docker Compose |
| Utilities | Lombok |

---

## 📁 Project Structure

```
smart-retail-system/
├── docker-compose.yml               # Full stack orchestration
├── .dockerignore
├── start-all.ps1                    # Windows PowerShell startup script
├── README.md
│
├── eureka-server/                   # Service discovery
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│
├── api-gateway/                     # Single entry point (port 9090)
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/smartretail/gateway/
│           │   ├── config/SwaggerAggregatorConfig.java
│           │   └── controller/SwaggerAggregationController.java
│           └── resources/
│               ├── application.yml
│               └── static/swagger-ui.html
│
├── user-service/                    # Auth + User management (port 9091)
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/smartretail/user/
│       ├── config/          (JWT, RabbitMQ, Security, Swagger)
│       ├── controller/      (UserController)
│       ├── dto/             (LoginRequest/Response, RegisterRequest, UserResponse...)
│       ├── entity/          (User, UserRole, UserStatus)
│       ├── exception/       (GlobalExceptionHandler + custom exceptions)
│       ├── messaging/       (EventPublisher, EventPayload)
│       ├── repository/      (UserRepository)
│       └── service/         (UserService, JwtUtil)
│
├── product-service/                 # Product + Stock management (port 9092)
├── order-service/                   # Order lifecycle (port 9093)
├── supplier-restock-service/        # Suppliers + Restock (port 9094)
├── notification-service/            # Event consumer (port 9095)
│
├── frontend/                        # React + Vite dashboard (port 3000)
│   ├── Dockerfile
│   ├── nginx.conf
│   └── src/
│       ├── pages/           (Dashboard, Products, Orders, Suppliers, Restocks, Notifications, Users)
│       ├── components/      (Layout, Sidebar, UI)
│       └── api/client.js
│
├── docs/
│   └── postman-test-flow.md
│
└── logs/                            # Runtime log files per service
```

---

## 🚀 Getting Started

### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running
- Git

### Run with Docker (Recommended — One Command)

```bash
git clone https://github.com/YOUR_USERNAME/smart-retail-system.git
cd smart-retail-system
docker-compose up --build
```

Docker will automatically start:
- MongoDB
- RabbitMQ
- Eureka Server
- API Gateway
- All 5 microservices
- React Frontend

### Access the System

| URL | Description |
|---|---|
| `http://localhost:3000` | React Frontend Dashboard |
| `http://localhost:9090/swagger-ui.html` | Aggregated Swagger UI (all services) |
| `http://localhost:8761` | Eureka Service Discovery Dashboard |
| `http://localhost:15672` | RabbitMQ Management UI (guest/guest) |

### Direct Service Swagger URLs

| Service | Direct Swagger URL |
|---|---|
| User Service | `http://localhost:9091/swagger-ui.html` |
| Product Service | `http://localhost:9092/swagger-ui.html` |
| Order Service | `http://localhost:9093/swagger-ui.html` |
| Supplier & Restock | `http://localhost:9094/swagger-ui.html` |
| Notification Service | `http://localhost:9095/swagger-ui.html` |

### Via API Gateway (Single Port)

```
GET  http://localhost:9090/gateway/users
POST http://localhost:9090/gateway/users/register
POST http://localhost:9090/gateway/users/login
GET  http://localhost:9090/gateway/products
POST http://localhost:9090/gateway/products
GET  http://localhost:9090/gateway/orders
POST http://localhost:9090/gateway/orders
GET  http://localhost:9090/gateway/suppliers
POST http://localhost:9090/gateway/restocks
GET  http://localhost:9090/gateway/notifications
```

---

## 🏃 Run Locally Without Docker

### Prerequisites
- Java 17
- Maven 3.8+
- MongoDB running on `localhost:27017`
- RabbitMQ running on `localhost:5672`

### Start order

```bash
# 1. Eureka Server
cd eureka-server && mvn spring-boot:run

# 2. All microservices (separate terminals)
cd user-service && mvn spring-boot:run
cd product-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd supplier-restock-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run

# 3. API Gateway (last)
cd api-gateway && mvn spring-boot:run

# 4. Frontend
cd frontend && npm install && npm run dev
```

> **Windows users:** Use the included `start-all.ps1` PowerShell script.

---

## 🔁 RabbitMQ Event Flow

```
User registers       → USER_REGISTERED      → Notification Service logs it
Order created        → ORDER_CREATED        → Notification Service logs it
Order cancelled      → ORDER_CANCELLED      → Notification Service logs it
Stock drops low      → LOW_STOCK            → Notification Service logs it
Restock created      → RESTOCK_CREATED      → Notification Service logs it
Restock delivered    → RESTOCK_COMPLETED    → Notification Service logs it
```

All events use a `smartretail.exchange` **TopicExchange** with routing keys.

---

## 🔗 Inter-Service Communication

**OpenFeign** is used for synchronous service-to-service calls:

- **Order Service → User Service**: validates user exists before creating order
- **Order Service → Product Service**: decreases stock when order is placed
- **Supplier & Restock Service → Product Service**: increases stock when restock is delivered

---

---
