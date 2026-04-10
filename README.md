# Restaurant Order & Table Management System

## Team Details

| Name | Student ID | GitHub Username |
|------|------------|-----------------|
| Zeynep Kılıç | 220513345 | [ZeynepKkilic](https://github.com/ZeynepKkilic) |
| Burak Berkay Ak | 220513355 | [burakberkayak](https://github.com/burakberkayak) |
| Abdülkadir Açıkkol | 230513328 | [kadiracikkol](https://github.com/kadiracikkol) |
| Yasin Özçelik | 220513368 | [wishyasin](https://github.com/wishyasin) |
| Barkın Kul | 220513344 | [barknkul](https://github.com/barknkul) |

## Project Introduction

The **Restaurant Order & Table Management System** is a full-stack application designed to digitise and streamline daily restaurant operations. It addresses the common pain points of paper-based order tracking, manual table-status monitoring, and end-of-day revenue reconciliation.

The system consists of two tightly integrated components:

- **Backend REST API** – A Node.js/Express server backed by a PostgreSQL database. It exposes endpoints for authentication, table management, menu retrieval, order lifecycle (create → pay), and daily reporting.
- **Android Mobile Client** – A Kotlin/Jetpack Compose application that allows waitstaff and managers to view live table statuses, browse the menu, place orders, and generate daily reports from a handheld device.

Key capabilities include:

- Role-based login (JWT authentication) for staff and admin users.
- Real-time table status tracking (`empty`, `occupied`, `waiting_bill`).
- Category-based menu browsing with per-category filtering.
- Order creation, itemised order viewing per table, and one-tap payment processing.
- Daily report dashboard showing total orders, total revenue, and table-status breakdown.
- Admin panel for managing menu items and table configurations.

## Architecture Link

See [ARCHITECTURE.md](./ARCHITECTURE.md) for the full 4+1 architectural view model documentation.
