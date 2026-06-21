# 🏇 Horse Racing Tournament Management System (HTMS)

<div align="center">

## Horse Racing Management Platform

Backend-focused tournament management system built with Java Spring Boot, PostgreSQL, JWT Authentication, and RESTful APIs.

![Java](https://img.shields.io/badge/Java-25-red)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue)
![JWT](https://img.shields.io/badge/Auth-JWT-orange)
![Swagger](https://img.shields.io/badge/API-Swagger-brightgreen)
![JPA](https://img.shields.io/badge/ORM-Spring%20Data%20JPA-blue)

</div>

---

# Project Overview

Horse Racing Tournament Management System (HTMS) is a comprehensive platform designed to manage horse racing tournaments, race registrations, jockey assignments, referee operations, betting activities, race results, prize distributions, and spectator engagement.

The system digitizes the entire horse racing lifecycle, from horse registration to race completion, while providing transparency, operational efficiency, and betting capabilities.

---

# Why This Project Matters

Traditional horse racing management often faces challenges such as:

* Manual race registration processes
* Complex jockey assignment management
* Inefficient referee reporting workflows
* Limited visibility into race results
* Lack of centralized betting and reward management

HTMS addresses these issues through:

* Centralized tournament administration
* Structured race management workflows
* Digital referee reporting
* Automated result publication
* Integrated betting and wallet systems
* Real-time notification delivery

The project architecture is designed to simulate real-world enterprise management systems commonly used in sports event platforms.

---

# System Actors

The platform supports five main actors:

| Role         | Description                                                                |
| ------------ | -------------------------------------------------------------------------- |
| Admin        | Manages tournaments, races, registrations, referees, and system operations |
| Horse Owner  | Registers horses, participates in races, and assigns jockeys               |
| Jockey       | Accepts invitations and participates in races                              |
| Race Referee | Monitors races and confirms official results                               |
| Spectator    | Views races, places bets, and tracks rewards                               |

---

# Core Features

## Authentication & Authorization

* User Registration
* Login & Logout
* JWT Authentication
* Password Encryption
* Role-Based Authorization

Supported Roles:

* ADMIN
* HORSE_OWNER
* JOCKEY
* RACE_REFEREE
* SPECTATOR

---

## Tournament Management

Admin can:

* Create tournaments
* Create tournament schedules
* Create races
* Configure prize structures
* Open and close registrations
* Assign referees
* Publish results

---

## Horse Management

Horse Owners can:

* Register horses
* Manage horse profiles
* Track race participation
* View horse performance history

---

## Jockey Assignment Management

The system supports:

* Jockey invitations
* Invitation acceptance/rejection
* Race assignment tracking
* Gate number management

---

## Race Operations

Features include:

* Race scheduling
* Registration approval
* Race monitoring
* Official result publication
* Ranking updates

---

## Referee Management

Race Referees can:

* Inspect race participants
* Monitor races
* Record violations
* Submit referee reports
* Confirm race outcomes

---

## Betting & Prediction System

Spectators can:

* View betting options
* Place bets
* Track betting history
* Receive rewards
* Monitor betting performance

---

## Wallet Management

The wallet system supports:

* Point balance tracking
* Top-up transactions
* Reward settlements
* Transaction history

---

## Notification System

Notifications include:

* Registration updates
* Jockey invitations
* Race result announcements
* Betting rewards
* Tournament updates

---

# Technology Stack

## Backend

* Java 25
* Spring Boot 4.0.6
* Spring Security
* Spring Data JPA
* JWT Authentication
* Lombok
* Bean Validation

---

## Database

* PostgreSQL
* H2 Database (Testing)

---

## API Documentation

* Swagger / OpenAPI

---

## Utilities

* Spring Boot DevTools
* Java Mail Sender

---

# System Architecture

## High-Level Architecture

```text
Client Applications
        ↓
JWT Authentication Filter
        ↓
REST Controllers
        ↓
DTO / Mapper Layer
        ↓
Service Layer
        ↓
Repository Layer
        ↓
PostgreSQL Database
```

---

# Main Business Modules

## User Management

```text
Users
JockeyProfiles
HorseOwnerProfiles
RefereeProfiles
```

Purpose:

Manage authentication, authorization, and actor-specific profile information.

---

## Tournament Management

```text
Tournaments
TournamentSchedules
Races
RaceRegistrations
```

Purpose:

Manage tournament lifecycle, schedules, races, and registrations.

---

## Race Operations

```text
JockeyHorseAssignments
RaceRefereeAssignments
RefereeReports
RaceResults
```

Purpose:

Manage race participation, officiating, and official result publication.

---

## Betting & Wallet

```text
BetOptions
Bets
Wallets
WalletTransactions
```

Purpose:

Support spectator betting, wallet balance management, and reward settlement.

---

## Prize Management

```text
PrizeDistributions
```

Purpose:

Manage prize structures and distribute rewards based on race results.

---

## Notification Module

```text
Notifications
```

Purpose:

Provide system-wide communication and event notifications.

---

# Core Business Workflow

## Horse Owner Workflow

```text
Register Horse
    ↓
Register Horse to Race
    ↓
Admin Approval
    ↓
Invite Jockey
    ↓
Jockey Accepts
    ↓
Race Participation
```

---

## Race Referee Workflow

```text
Assigned to Race
    ↓
Monitor Race
    ↓
Create Referee Report
    ↓
Confirm Result
    ↓
Publish Official Result
```

---

## Betting Workflow

```text
Race Open For Betting
    ↓
System Generates Bet Options
    ↓
Spectator Places Bet
    ↓
Race Result Published
    ↓
Bet Settlement
    ↓
Wallet Reward
```

---

# Security

HTMS uses:

* JWT Access Token Authentication
* Spring Security Filter Chain
* BCrypt Password Encoding
* Role-Based Authorization

Protected resources require valid JWT tokens.

---

# Source Code Structure

```text
src/main/java/com/group5/htms

├── config
├── filter
├── controller
├── dto
├── mapper
├── service
├── service/impl
├── repository
├── entity
├── exception
└── HTMSApplication.java
```

---

# API Documentation

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI Specification:

```text
http://localhost:8080/v3/api-docs
```

---

# Current Project Status

| Feature                   | Status         |
| ------------------------- | -------------- |
| Database Design (ERD)     | ✅ Completed    |
| Entity Layer              | ✅ Completed    |
| Repository Layer          | ✅ Completed    |
| Swagger Integration       | ✅ Completed    |
| JWT Authentication        | 🚧 In Progress |
| Service Layer             | 🚧 In Progress |
| REST Controllers          | 🚧 In Progress |
| Mail Notification         | 📌 Planned     |
| Wallet System             | 📌 Planned     |
| Betting Settlement Engine | 📌 Planned     |

---

# Future Improvements

Planned enhancements include:

* Real-time race updates
* Live betting statistics
* Email notifications
* WebSocket integration
* CI/CD pipelines
* Automated testing
* Cloud deployment
* Analytics dashboards

---

# Documentation Included

The repository includes:

* ERD Diagrams
* Context Diagram
* Use Case Diagram
* Business Flow Diagrams
* Swagger Documentation
* API Specifications
* Database Design Documents

---

# Academic Purpose

This project is developed as a Software Engineering capstone project and demonstrates:

* Enterprise database design
* RESTful API development
* Spring Boot architecture
* Security implementation
* Business process modeling
* Backend system design
* Software engineering best practices
