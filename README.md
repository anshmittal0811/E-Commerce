# E-Commerce Platform

A full-stack e-commerce platform featuring a Java Spring Boot microservices backend and a modern React frontend. The system implements service discovery, API gateway with JWT authentication, event-driven notifications, and is fully containerized with Docker.

## Table of Contents

- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Services](#services)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Frontend](#frontend)
- [Configuration](#configuration)

## Tech Stack

### Backend

| Category | Technology |
|----------|------------|
| Language | Java 17+ |
| Framework | Spring Boot 3.2.5, Spring Cloud |
| Database | MySQL 8.x |
| Messaging | Apache Kafka |
| API Gateway | Spring Cloud Gateway |
| Service Discovery | Netflix Eureka |
| Inter-Service Communication | OpenFeign |
| Security | JWT Authentication |
| Documentation | Swagger / OpenAPI 3 |
| Containerization | Docker & Docker Compose |

### Frontend

| Category | Technology |
|----------|------------|
| Framework | React 18 + TypeScript |
| Build Tool | Vite |
| Styling | Tailwind CSS |
| State Management | TanStack Query (React Query) |
| Routing | React Router v7 |
| HTTP Client | Axios |

## Architecture

The platform follows a microservices architecture with the following patterns:

- **Service Discovery**: All services register with Eureka for dynamic discovery
- **API Gateway**: Single entry point handling routing, authentication, and load balancing
- **Event-Driven**: Kafka enables asynchronous communication between Payment and Notification services
- **Feign Clients**: Declarative REST clients for synchronous inter-service communication
- **Modern Frontend**: React SPA with TanStack Query for efficient data fetching and caching

### Request Flow

1. User interacts with the **React Frontend** (port 3000)
2. Frontend sends API requests to **Gateway Service** (port 8090)
3. Gateway validates JWT token via `JwtAuthFilter`
4. Gateway discovers target service via **Eureka**
5. Request is routed to the appropriate microservice
6. Services communicate internally using **Feign clients**
7. Async events (e.g., payment completed) are published to **Kafka**

## Project Structure

```
e_commerce/
├── backend/                    # Backend microservices
│   ├── auth-service/       # Authentication & user management
│   ├── eureka-service/     # Service discovery server
│   ├── gateway-service/    # API Gateway with JWT auth
│   ├── notification-service/ # Email/SMS notifications
│   ├── order-service/      # Order management
│   ├── payment-service/    # Payment processing
│   ├── product-service/    # Product catalog
│   ├── shopping-service/   # Shopping cart
│   ├── docker-compose.yml      # Docker orchestration
│   └── pom.xml                 # Parent Maven POM
├── frontend/                   # React frontend
│   ├── src/
│   │   ├── api/               # API client & endpoints
│   │   ├── components/        # Reusable UI components
│   │   ├── context/           # React context (Auth)
│   │   ├── hooks/             # Custom React Query hooks
│   │   └── pages/             # Page components
│   ├── Dockerfile
│   └── package.json
└── README.md
```

## Services

| Service | Port | Description |
|---------|------|-------------|
| **Frontend** | 3000 | React SPA with Tailwind CSS |
| **eureka-service** | 8761 | Service registry and discovery server |
| **gateway-service** | 8090 | API Gateway with JWT authentication and routing |
| **auth-service** | 8081 | User registration, login, and JWT token management |
| **shopping-service** | 8082 | Shopping cart operations (add, remove, view items) |
| **product-service** | 8083 | Product catalog management (CRUD operations) |
| **payment-service** | 8084 | Payment processing and order completion |
| **order-service** | 8085 | Order creation and lifecycle management |
| **notification-service** | 8086 | Email and SMS notifications via Kafka consumer |

### Service Details

#### Auth Service
- User registration with email validation
- User login with JWT token generation
- User profile management
- Role-based access control

#### Shopping Service
- Add/remove products to cart
- View cart contents
- Cart persistence per user
- Integrates with Product Service for product validation

#### Product Service
- Create, read, update, delete products
- Product inventory management
- Role-based access for admin operations

#### Order Service
- Create orders from shopping cart
- Order status management (PENDING, COMPLETED, CANCELLED)
- Order history retrieval
- Integrates with Shopping and User services

#### Payment Service
- Process payments for orders
- Update order status to COMPLETED
- Publish payment events to Kafka
- Integrates with Order Service via Feign

#### Notification Service
- Kafka consumer for payment events
- Email notification service
- SMS notification service

## Prerequisites

- **Docker** (v20.10+)
- **Docker Compose** (v2.0+)
- **Java 17+** (for backend development)
- **Maven 3.8+** (for backend development)
- **Node.js 20+** (for frontend development)
- **npm 10+** (for frontend development)

## Getting Started

### Using Docker Compose (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd e_commerce
   ```

2. **Start all backend services**
   ```bash
   cd backend
   docker-compose up --build
   ```

3. **Start the frontend** (in a new terminal)
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

4. **Verify services are running**
   - Frontend: http://localhost:3000
   - Eureka Dashboard: http://localhost:8761
   - Gateway Swagger UI: http://localhost:8090/swagger-ui.html

5. **Stop all services**
   ```bash
   docker-compose down
   ```

### Local Development

1. **Start infrastructure services**
   ```bash
   cd backend
   docker-compose up mysql kafka -d
   ```

2. **Run each backend service individually**
   ```bash
   cd <service-name>
   ./mvnw spring-boot:run
   ```

   Start services in this order:
   1. eureka-service
   2. gateway-service
   3. auth-service
   4. Other services (any order)

3. **Run the frontend**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

## API Endpoints

All APIs are accessible through the Gateway at `http://localhost:8090`

### Authentication (Public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and receive JWT token |

### Users (Authenticated)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/admin/users` | Get all users (Admin only) |
| GET | `/api/users/client/user/{id}` | Get user by ID |
| GET | `/api/users/admin/verification` | Verify admin role |
| GET | `/api/users/client/verification` | Verify client role |

### Products (Authenticated)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/product/` | List all products |
| GET | `/api/product/{id}` | Get product by ID |
| POST | `/api/product/` | Create product (Admin) |
| PUT | `/api/product/{id}` | Update product (Admin) |
| DELETE | `/api/product/{id}` | Delete product (Admin) |

### Shopping Cart (Authenticated)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/shopping/cart` | View cart |
| POST | `/api/shopping/cart/add` | Add item to cart |
| DELETE | `/api/shopping/cart/remove/{itemId}` | Remove item from cart |
| DELETE | `/api/shopping/cart/clear` | Clear cart |

### Orders (Authenticated)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/orders/` | Create order from cart |
| GET | `/api/orders/` | List user orders |
| GET | `/api/orders/{id}` | Get order details |

### Payments (Authenticated)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/payment/order/{orderId}` | View order for payment |
| POST | `/api/payment/pay/{orderId}` | Process payment |

### Authentication Header

Include the JWT token in all authenticated requests:
```
Authorization: Bearer <your-jwt-token>
```

## Frontend

The frontend is a modern React single-page application built with:

- **React 18** with TypeScript for type safety
- **Vite** for fast development and optimized builds
- **Tailwind CSS** for utility-first styling
- **TanStack Query** for server state management
- **React Router v7** for client-side routing
- **Axios** for HTTP requests with interceptors

### Frontend Features

- 🔐 **Authentication**: Login and registration with JWT token management
- 🛍️ **Product Catalog**: Browse products with a beautiful grid layout
- 🛒 **Shopping Cart**: Add/remove items with real-time updates
- 📦 **Order Management**: View order history and details
- 💳 **Payment Integration (Dummy UI)**: Process payments for pending orders
- 📱 **Responsive Design**: Works on all screen sizes

### Running the Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend will be available at `http://localhost:3000` and will proxy API requests to the Gateway at `http://localhost:8090`.

## Configuration

### Environment Variables

Key environment variables used in `docker-compose.yml`:

| Variable | Service | Description |
|----------|---------|-------------|
| `MYSQL_ROOT_PASSWORD` | MySQL | Database root password |
| `MYSQL_DATABASE` | MySQL | Default database name |
| `JAVA_OPTS` | All services | JVM options |

### Application Configuration

Each service has its own `application.yml` in `src/main/resources/`:

- **Database**: MySQL connection at `mysql_docker:3306`
- **Eureka**: Service discovery at `eureka-service-api:8761`
- **Kafka**: Message broker at `kafka:9092`
- **JWT Secret**: Configured in gateway and auth services

### Ports Mapping

| Internal Port | External Port | Service |
|---------------|---------------|---------|
| 3000 | 3000 | Frontend |
| 3306 | 3307 | MySQL |
| 8761 | 8761 | Eureka |
| 8090 | 8090 | Gateway |
| 8081 | 8081 | Auth |
| 8082 | 8082 | Shopping |
| 8083 | 8083 | Product |
| 8084 | 8084 | Payment |
| 8085 | 8085 | Order |
| 8086 | 8086 | Notification |
| 9092 | 9092 | Kafka |

## API Documentation

Swagger UI is available at: `http://localhost:8090/swagger-ui.html`

Individual service API docs:
- Auth Service: `/v3/api-docs/auth`
- Product Service: `/v3/api-docs/product`
- Shopping Service: `/v3/api-docs/shopping`
- Order Service: `/v3/api-docs/order`
- Payment Service: `/v3/api-docs/payment`
- Notification Service: `/v3/api-docs/notification`
