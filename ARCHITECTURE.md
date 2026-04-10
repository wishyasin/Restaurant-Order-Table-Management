# Software Architecture Document
## Restaurant Order & Table Management System

---

## Title Page

| Field | Value |
|-------|-------|
| **Project** | Restaurant Order & Table Management System |
| **Course** | SWE332 – Software Architecture |
| **Document** | Architecture Description (Part 2) |
| **Version** | 1.0 |
| **Date** | 10 April 2026 |

---

## Change History

Version | Date | Author |Description |
0.1 | 05 April 2026 | Zeynep Kılıç  | Created initial template and defined the Project Scope.
0.2 | 06 April 2026 | Yasin Özçelik | Designed and documented the Logical Architecture for Backend and Android.
0.3 | 07 April 2026 | Yasin Özçelik | "Documented Authentication flow, JWT implementation, and API endpoints."
0.4 | 07 April 2026 | Barkın Kul | Added Android Client layer diagrams and Navigation structure.
0.5 | 08 April 2026 | Abdülkadir Açıkkol | Detailed the Model-View-ViewModel (MVVM) implementation.
0.6 | 08 April 2026 | Burak Berkay Ak | Added Database Schema details to Logical Architecture section.
0.7 | 09 April 2026 | Yasin Özçelik | Documented Physical Architecture and Deployment environment setup.
0.8 | 09 April 2026 | Yasin Özçelik | "Defined Architectural Goals, Constraints, and Quality attributes."
0.9 | 10 April 2026 | Zeynep Kılıç | Refined Scenarios and Use-Case descriptions.
1.0 | 10 April 2026 | Yasin Özçelik | "Final technical review, Table of Contents, and submission formatting."

---

## Table of Contents

1. [Scope](#1-scope)
2. [References](#2-references)
3. [Software Architecture](#3-software-architecture)
4. [Architectural Goals & Constraints](#4-architectural-goals--constraints)
5. [Logical Architecture](#5-logical-architecture)
6. [Process Architecture](#6-process-architecture)
7. [Development Architecture](#7-development-architecture)
8. [Physical Architecture](#8-physical-architecture)
9. [Scenarios](#9-scenarios)
10. [Size and Performance](#10-size-and-performance)
11. [Quality](#11-quality)

**Appendices**
- [Acronyms and Abbreviations](#acronyms-and-abbreviations)
- [Definitions](#definitions)
- [Design Principles](#design-principles)

---

## List of Figures

- Figure 1 – High-Level System Overview
- Figure 2 – Logical Layer Diagram (Backend)
- Figure 3 – Logical Layer Diagram (Android Client)
- Figure 4 – Process / Sequence Diagram: Order Flow
- Figure 5 – Component Diagram (Development View)
- Figure 6 – Physical Deployment Diagram
- Figure 7 – Use-Case Scenario: Waiter Places Order
- Figure 8 – Use-Case Scenario: Admin Views Daily Report

---

## 1. Scope

This document describes the software architecture of the **Restaurant Order & Table Management System**. The system automates table management, order creation, and daily revenue reporting for a restaurant environment. It consists of a Node.js/Express REST API backend connected to a PostgreSQL database, and a Kotlin/Jetpack Compose Android mobile application consumed by restaurant staff.

The document follows the **4+1 Architectural View Model** (Kruchten, 1995), covering the Logical, Process, Development, and Physical views alongside key use-case Scenarios.

---

## 2. References

- Kruchten, P. B. (1995). *The 4+1 View Model of Architecture*. IEEE Software, 12(6), 42–50.
- [4+1 Architectural View Model – Wikipedia](https://en.wikipedia.org/wiki/4%2B1_architectural_view_model)
- Express.js Documentation – https://expressjs.com
- Android Jetpack Compose Documentation – https://developer.android.com/compose
- PostgreSQL Documentation – https://www.postgresql.org/docs/
- JSON Web Token (JWT) Specification – RFC 7519

---

## 3. Software Architecture

The system follows a **Client–Server** architectural style with a clear separation between the mobile front-end and the backend service tier.

```
┌─────────────────────────┐        HTTP/REST (JSON)       ┌──────────────────────────┐
│   Android Mobile App    │ ◄─────────────────────────── │   Node.js / Express API   │
│  (Kotlin + Compose)     │ ─────────────────────────►   │   (REST Backend)           │
└─────────────────────────┘                               └────────────┬─────────────┘
                                                                       │ SQL
                                                          ┌────────────▼─────────────┐
                                                          │     PostgreSQL Database   │
                                                          └──────────────────────────┘
```
*Figure 1 – High-Level System Overview*

The backend exposes a stateless REST API. The Android client consumes this API over HTTP. Authentication is handled via JWT tokens issued at login and carried in the `Authorization` header on subsequent requests.

---

## 4. Architectural Goals & Constraints

### Goals

| Goal | Description |
|------|-------------|
| **Simplicity** | Keep both the API surface and the mobile navigation model small and easy to understand for student developers. |
| **Maintainability** | Separate concerns clearly (routes, database, UI screens, ViewModels) so that features can be added or changed in isolation. |
| **Responsiveness** | The Android UI must remain responsive; all network and database I/O is performed asynchronously. |
| **Security** | Authenticate users and protect sensitive routes using JWT. Credentials are stored via environment variables, not hard-coded. |
| **Portability** | The backend must run on any machine with Node.js ≥ 18 and PostgreSQL ≥ 14. |

### Constraints

- The Android application targets Android SDK 26 (Android 8) as minimum and SDK 36 as the compilation target.
- The backend runs as a single Node.js process (no clustering); horizontal scaling is out of scope.
- The database schema is relational (PostgreSQL); NoSQL alternatives are not used.
- Network calls from the Android app are made over plain HTTP during development; production deployment should enforce HTTPS.
- No external payment processor is integrated; the system simply marks an order as `paid`.

---

## 5. Logical Architecture

The logical architecture describes the system's key abstractions and their responsibilities, independent of how they are packaged or deployed.

### 5.1 Backend Layers

```
┌────────────────────────────────────────────┐
│             Express Route Layer            │  ← HTTP entry points (app.js)
├────────────────────────────────────────────┤
│           Business Logic Layer             │  ← Validation, status transitions,
│         (inline in route handlers)         │    JWT signing/verification
├────────────────────────────────────────────┤
│           Data Access Layer                │  ← PostgreSQL queries via pg Pool (db.js)
├────────────────────────────────────────────┤
│          PostgreSQL Database               │  ← Tables, Orders, OrderItems, MenuItems,
│                                            │    Users
└────────────────────────────────────────────┘
```
*Figure 2 – Logical Layer Diagram (Backend)*

**Key Domain Entities (Backend)**

| Entity | Attributes | Responsibility |
|--------|-----------|----------------|
| `Users` | id, username, password, role | Authentication and role-based access |
| `Tables` | id, table_number, status | Tracks real-time table occupancy state |
| `MenuItems` | id, name, price, category | The restaurant's menu catalogue |
| `Orders` | id, table_id, status, created_at | Lifecycle of a single bill (open → paid) |
| `OrderItems` | id, order_id, menu_item_id, quantity | Line items within an order |

**Table Status State Machine**

```
[empty] ──► [occupied] ──► [waiting_bill] ──► [empty]
```

**Order Status State Machine**

```
[open] ──► [paid]
```

### 5.2 Android Client Layers

```
┌─────────────────────────────────────────────────┐
│             UI Layer (Jetpack Compose)           │
│  LoginScreen │ TablesScreen │ TableDetailScreen  │
│  MenuScreen  │ AdminPanelScreen │ DailyReportScreen │
├─────────────────────────────────────────────────┤
│          ViewModel Layer (Android ViewModel)     │
│    TableViewModel │ MenuViewModel │ OrderViewModel│
├─────────────────────────────────────────────────┤
│        Navigation Layer (NavGraph / Screen)      │
├─────────────────────────────────────────────────┤
│       Network / Repository Layer (HTTP calls)   │
├─────────────────────────────────────────────────┤
│            Mock Data Layer (MockData)            │
│          (used for offline / prototype mode)     │
└─────────────────────────────────────────────────┘
```
*Figure 3 – Logical Layer Diagram (Android Client)*

**Key Screens and their Purpose**

| Screen | Purpose |
|--------|---------|
| `LoginScreen` | Credential entry; triggers JWT authentication |
| `TablesScreen` | Grid view of all tables with colour-coded status |
| `TableDetailScreen` | Shows active orders for a selected table; allows status changes and payment |
| `MenuScreen` | Category-filtered menu browser; allows adding items to an order |
| `AdminPanelScreen` | CRUD operations on menu items and tables (admin role only) |
| `DailyReportScreen` | Displays daily order count, revenue, and table-status breakdown |

---

## 6. Process Architecture

The process architecture describes the system's runtime concurrency and communication flows.

### 6.1 Concurrent Processes

| Process | Technology | Notes |
|---------|-----------|-------|
| Android UI Thread | Android Main Thread | Renders Compose UI; must never be blocked |
| ViewModel Coroutines | Kotlin Coroutines / `viewModelScope` | Performs async network calls off the main thread |
| Express HTTP Server | Node.js Event Loop (single-threaded) | Handles all incoming API requests asynchronously |
| PostgreSQL Connection Pool | `pg.Pool` (up to N connections) | Manages database connection reuse |

### 6.2 Order Placement Sequence

```
Waiter (App)          ViewModel           Express API          PostgreSQL
    │                     │                    │                    │
    │── tap "Place Order"─►│                    │                    │
    │                     │── POST /api/orders─►│                    │
    │                     │                    │── INSERT Orders ───►│
    │                     │                    │◄── order.id ────────│
    │                     │                    │── INSERT OrderItems►│
    │                     │                    │── UPDATE Tables ───►│
    │                     │◄── 201 Created ────│                    │
    │◄── UI updated ──────│                    │                    │
```
*Figure 4 – Process / Sequence Diagram: Order Flow*

### 6.3 Authentication Flow

1. The Android app sends `POST /api/login` with `{username, password}`.
2. The server verifies credentials against the `Users` table.
3. On success, a signed JWT (8-hour expiry, HS256) is returned.
4. Subsequent API calls carry the token in the `Authorization: Bearer <token>` header.

---

## 7. Development Architecture

The development architecture describes how source code is organised into modules and packages.

### 7.1 Repository Structure

```
Restaurant-Order-Table-Management/
├── src/
│   ├── app.js            # Express application – route definitions & server startup
│   └── db.js             # PostgreSQL connection pool factory
├── package.json          # Node.js dependencies & npm scripts
├── .env                  # Environment variables (DB credentials, JWT secret, PORT)
├── .gitignore
└── android/              # Android Studio project root
    └── app/
        └── src/
            └── main/
                └── java/com/restaurantmanagement/
                    ├── MainActivity.kt           # Compose entry point
                    ├── navigation/
                    │   ├── NavGraph.kt           # Navigation graph definition
                    │   └── Screen.kt             # Sealed class of routes
                    ├── data/
                    │   └── model/
                    │       ├── User.kt / UserRole.kt
                    │       ├── Table.kt / TableStatus.kt
                    │       ├── MenuItem.kt / MenuCategory.kt
                    │       ├── Order.kt / OrderItem.kt / OrderStatus.kt
                    │       └── MockData.kt        # Static prototype data
                    ├── ui/
                    │   ├── screens/
                    │   │   ├── LoginScreen.kt
                    │   │   ├── TablesScreen.kt
                    │   │   ├── TableDetailScreen.kt
                    │   │   ├── MenuScreen.kt
                    │   │   ├── AdminPanelScreen.kt
                    │   │   └── DailyReportScreen.kt
                    │   └── theme/
                    │       ├── Color.kt
                    │       ├── Theme.kt
                    │       └── Type.kt
                    └── viewmodel/
                        ├── TableViewModel.kt
                        ├── MenuViewModel.kt
                        └── OrderViewModel.kt
```
*Figure 5 – Component Diagram (Development View)*

### 7.2 Backend Modules

| File | Responsibility |
|------|---------------|
| `src/db.js` | Creates and exports a shared `pg.Pool` instance configured from environment variables |
| `src/app.js` | Registers all Express routes; applies `express.json()` middleware; starts the HTTP server |

### 7.3 Backend API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Health check |
| POST | `/api/login` | Authenticate user, return JWT |
| GET | `/api/tables` | List all tables ordered by number |
| PATCH | `/api/tables/:id` | Update a table's status |
| GET | `/api/menu` | List all menu items ordered by category and name |
| POST | `/api/orders` | Create a new order for a table |
| GET | `/api/orders/:table_id` | Get open orders for a specific table |
| POST | `/api/orders/:id/pay` | Mark an order as paid and free the table |
| GET | `/api/reports/daily` | Return today's order count, revenue, and table breakdown |

### 7.4 Android Dependencies

| Library | Purpose |
|---------|---------|
| Jetpack Compose | Declarative UI framework |
| Navigation Compose | Screen-to-screen navigation with back stack |
| ViewModel + LiveData | UI-state management, lifecycle-aware |
| Kotlin Coroutines | Asynchronous operations off the main thread |
| Room (AndroidX) | (Referenced in Manifest) Local database abstraction |
| Retrofit / OkHttp | (Intended) HTTP client for REST calls |

---

## 8. Physical Architecture

The physical architecture describes how software artefacts map to hardware infrastructure.

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Restaurant Local Network                         │
│                                                                         │
│  ┌──────────────────────┐          ┌──────────────────────────────────┐│
│  │  Android Tablet/Phone │          │  Development / Production Server ││
│  │  (Waiter / Manager)   │◄────────►│  Ubuntu / Any OS                ││
│  │                       │  HTTP    │                                  ││
│  │  Android 8+           │  :3000   │  Node.js 18+  (Express)         ││
│  │  Compose UI           │          │  port 3000                       ││
│  └──────────────────────┘          │                                  ││
│                                    │  PostgreSQL 14+                  ││
│  ┌──────────────────────┐          │  port 5432                       ││
│  │  Android Tablet/Phone │          │  Database: restaurantdb          ││
│  │  (Waiter / Manager)   │◄────────►│                                  ││
│  └──────────────────────┘          └──────────────────────────────────┘│
└────────────────────────────────────────────────────────────────────────┘
```
*Figure 6 – Physical Deployment Diagram*

**Deployment Notes**

- The Node.js server and PostgreSQL database are co-located on the same machine during development (localhost).
- In a production setting, the database should be on a separate server or a managed service.
- Android devices connect to the server's IP address over the local Wi-Fi network.
- Environment-specific configuration (host, port, JWT secret) is supplied via the `.env` file and never committed to version control.

---

## 9. Scenarios

Scenarios (use cases) are used to validate and illustrate the architecture.

### 9.1 Scenario 1 – Waiter Places an Order

**Actor:** Waiter  
**Precondition:** Waiter is logged in; table is `empty`.

1. Waiter opens the **TablesScreen** and selects a table.
2. The **TableDetailScreen** is displayed; the waiter taps "Add Order".
3. The **MenuScreen** opens; the waiter browses categories and adds items to the current order.
4. Waiter taps "Confirm Order". The Android app calls `POST /api/orders` with `{table_id, items}`.
5. The backend inserts a new `Orders` record and corresponding `OrderItems` rows, then updates the table status to `occupied`.
6. The Android app receives HTTP 201 and navigates back to `TablesScreen`, showing the table as occupied.

```
[TablesScreen] ──► [TableDetailScreen] ──► [MenuScreen] ──► POST /api/orders ──► [TablesScreen (updated)]
```
*Figure 7 – Use-Case Scenario: Waiter Places Order*

### 9.2 Scenario 2 – Manager Views Daily Report

**Actor:** Manager (admin role)  
**Precondition:** Manager is logged in with admin credentials.

1. Manager opens the app and navigates to **DailyReportScreen**.
2. The `OrderViewModel` calls `GET /api/reports/daily`.
3. The backend queries `Orders`, `OrderItems`, `MenuItems`, and `Tables` for the current date.
4. The JSON response `{total_orders, total_revenue, tables}` is rendered in the report screen.

```
[DailyReportScreen] ──► GET /api/reports/daily ──► Aggregated SQL queries ──► Report rendered
```
*Figure 8 – Use-Case Scenario: Admin Views Daily Report*

### 9.3 Scenario 3 – Payment Processing

**Actor:** Waiter  
**Precondition:** Table is `occupied` with an open order.

1. Waiter opens **TableDetailScreen** for the relevant table.
2. Waiter taps "Request Bill"; the app calls `PATCH /api/tables/:id` with `{status: "waiting_bill"}`.
3. After the customer pays, waiter taps "Pay". The app calls `POST /api/orders/:id/pay`.
4. The backend updates the order status to `paid` and the table status back to `empty`.
5. The **TablesScreen** refreshes and the table is shown as empty.

---

## 10. Size and Performance

| Metric | Value / Target |
|--------|---------------|
| Backend API response time (local network) | < 200 ms per request |
| PostgreSQL pool size | Default (`pg.Pool` default = 10 connections) |
| Android minimum SDK | 26 (Android 8.0) |
| Android target SDK | 36 |
| Number of Express routes | 9 |
| Number of database tables | 5 (`Users`, `Tables`, `MenuItems`, `Orders`, `OrderItems`) |
| Number of Android screens | 6 |
| Number of Android ViewModels | 3 (`TableViewModel`, `MenuViewModel`, `OrderViewModel`) |
| Expected concurrent users | < 20 (single restaurant, local network) |

The system is designed for single-restaurant, local-network use and is not intended to serve thousands of concurrent users. The single Node.js process is adequate for this scale.

---

## 11. Quality

| Quality Attribute | How It Is Achieved |
|-------------------|--------------------|
| **Security** | JWT-based authentication with 8-hour token expiry; credentials read from `.env` file |
| **Maintainability** | Clear layer separation in both backend (routes / db) and Android (screens / viewmodels / data models); each concern is isolated in its own file |
| **Reliability** | All database operations are wrapped in `try/catch` blocks; the server returns appropriate HTTP error codes (400, 401, 404, 500) |
| **Usability** | Jetpack Compose declarative UI with colour-coded table statuses; category tabs in the menu for quick navigation |
| **Testability** | `MockData.kt` in the Android project enables UI development and testing without a live backend |
| **Scalability** | Stateless REST API allows future horizontal scaling; JWT removes server-side session state |
| **Portability** | Backend runs on any Node.js 18+ environment; Android app runs on any Android 8+ device |

---

## Appendices

### Acronyms and Abbreviations

| Term | Definition |
|------|-----------|
| API | Application Programming Interface |
| JWT | JSON Web Token |
| REST | Representational State Transfer |
| HTTP | Hypertext Transfer Protocol |
| SQL | Structured Query Language |
| MVVM | Model-View-ViewModel |
| SDK | Software Development Kit |
| UI | User Interface |
| OS | Operating System |
| CRUD | Create, Read, Update, Delete |

### Definitions

| Term | Definition |
|------|-----------|
| **Table** | A physical dining table in the restaurant, tracked as a software entity with a status. |
| **Order** | A bill opened for a specific table, containing one or more order items. |
| **Order Item** | A single menu item within an order, with a quantity. |
| **Menu Item** | A food or beverage product offered by the restaurant, belonging to a category. |
| **Status (Table)** | One of: `empty`, `occupied`, `waiting_bill`. |
| **Status (Order)** | One of: `open`, `paid`. |
| **JWT** | A compact, URL-safe token carrying user identity claims, signed with a secret key. |
| **Pool** | A managed set of pre-opened database connections reused across requests. |
| **ViewModel** | An Android architecture component that holds UI state and survives configuration changes. |
| **Composable** | A function annotated with `@Composable` in Jetpack Compose that describes a UI element. |

### Design Principles

The following design principles guided architectural decisions:

1. **Separation of Concerns** – Business logic, data access, and presentation are kept in distinct layers/files so that changes in one area minimally impact others.
2. **Single Responsibility** – Each Express route handler addresses exactly one API resource operation; each Android screen focuses on one user task.
3. **Stateless Server** – The backend does not maintain session state; all state is either in the database or encoded in the client's JWT.
4. **Environment-Driven Configuration** – All environment-specific values (database host, JWT secret, port) are provided via environment variables, keeping the codebase environment-agnostic.
5. **Fail Fast** – Route handlers validate inputs at the earliest opportunity and return descriptive error responses before performing any database operation.
6. **Async First** – All I/O operations (database queries, network calls) are non-blocking and handled with `async/await` on the backend and Kotlin Coroutines on the Android client.
