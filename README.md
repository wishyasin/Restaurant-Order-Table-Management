# Restaurant Order and Table Management System

## Team Details

| Name | Student ID | GitHub Username |
|------|-----------|-----------------|
| Zeynep Kılıç | 220513345 | [ZeynepKkilic](https://github.com/ZeynepKkilic) |
| Burak Berkay Ak | 220513355 | [burakberkayak](https://github.com/burakberkayak) |
| Abdülkadir Açıkkol | 230513328 | [kadiracikkol](https://github.com/kadiracikkol) |
| Yasin Özçelik | 220513368 | [wishyasin](https://github.com/wishyasin) |
| Barkın Kul | 220513344 | [barknkul](https://github.com/barknkul) |

## Project Introduction

The Restaurant Order and Table Management System is a full-stack mobile application designed to digitize and streamline restaurant operations. The system replaces paper-based order taking and manual table tracking with a real-time, role-aware platform for restaurant staff.

The application allows waitstaff to view table statuses, take customer orders directly from a mobile device, and manage the full order lifecycle from placement to payment. Administrators have access to an admin panel and daily sales reports for operational oversight.

The system consists of three main components:

- A **PostgreSQL** relational database for persistent data storage
- A **Node.js/Express** REST API backend handling authentication and business logic
- An **Android mobile application** (Kotlin + Jetpack Compose) as the primary user interface

## Architecture Link

[View full architecture documentation](./ARCHITECTURE.md)

---

## Getting Started

### Prerequisites

- [Node.js](https://nodejs.org) (LTS version)
- [PostgreSQL](https://postgresapp.com) (Postgres.app recommended for macOS)
- [Android Studio](https://developer.android.com/studio)

---

### 1. Clone the Repository

```bash
git clone https://github.com/wishyasin/Restaurant-Order-Table-Management.git
cd Restaurant-Order-Table-Management
```

---

### 2. Set Up the Database

Make sure PostgreSQL is running, then execute the setup script:

```bash
psql -U your_username -d postgres -f setup.sql
```

Replace `your_username` with your PostgreSQL username (usually your macOS username).

This will create the `restaurantdb` database, all tables, and sample data including:
- 3 default users (`admin/1234`, `staff/1234`, `barking/1702`)
- 6 tables
- 12 menu items

---

### 3. Configure the Backend

Create a `.env` file inside the `backend/` folder:

```bash
cd backend
nano .env
```

Add the following content:

```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=restaurantdb
DB_USER=your_username
DB_PASSWORD=
JWT_SECRET=restoran_gizli_anahtar_2024
PORT=3000
```

Replace `your_username` with your PostgreSQL username.

---

### 4. Start the Backend

```bash
cd backend
npm install
npm run dev
```

The server will start on `http://localhost:3000`.

---

### 5. Run the Android App

1. Open Android Studio
2. Select **File > Open** and choose the `android/` folder
3. Wait for Gradle sync to complete
4. Click the ▶️ **Run** button to launch the emulator

The app connects to the backend at `http://10.0.2.2:3000/api/` (Android emulator loopback address).

---

### Default Login Credentials

| Username | Password | Role |
|----------|----------|------|
| admin | 1234 | ADMIN |
| staff | 1234 | STAFF |
| barking | 1702 | STAFF |