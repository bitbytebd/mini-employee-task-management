# Mini Employee & Task Management System

## Overview

This repository contains a complete role-based employee and task management system. The Spring Boot backend is in `backend/` and the Angular 13 frontend is in `frontend/`.

## Technology Stack

- Java 17
- Spring Boot 3.3.x
- Maven 3.9.x
- Spring Data JPA
- Spring Security with JWT
- MySQL 8 for runtime data
- H2 for the existing integration-test profile
- Angular 13, TypeScript, Reactive Forms, and HttpClient
- Bootstrap 5

## Backend Package Structure

`com.fifo.employeetask` contains the Spring Boot application class and these packages:

- `config` configures security and demo data.
- `controller` exposes HTTP endpoints.
- `dto` defines request and response payloads.
- `entity` defines JPA domain models and enums.
- `exception` provides API error handling.
- `repository` provides database access interfaces.
- `security` processes JWT authentication.
- `service` contains application use cases.

## Layer Responsibilities

Controllers receive HTTP requests, validate input, and return HTTP responses. Services apply authentication and business rules. Repositories use Spring Data JPA to read and store entities.

## Backend Commands

From the repository root:

```powershell
Set-Location backend
mvn compile
mvn test
mvn spring-boot:run
```

For the persistent MySQL mode, run `START-MYSQL-PROJECT.cmd` from the repository root. It requests database credentials securely for the current session and starts Spring Boot with the `mysql` profile. Then run `frontend\run-dev-server.cmd`.

For a self-contained H2 demonstration, run `START-PROJECT.cmd`. Open `http://localhost:4200/` after the frontend reports ready.

The MySQL schema is defined in `backend/src/main/resources/schema.sql` with non-destructive `CREATE TABLE IF NOT EXISTS` statements for `users`, `user_roles`, and `tasks`.

## Current Status

The application includes JWT authentication, role-based authorization, scoped user/task APIs, dashboard statistics, PDF task reports, MySQL persistence, and an Angular UI with protected routes. The H2 integration suite covers authentication, administration, task authorization, dashboard, and reporting. The Postman collection and UML diagrams are under `docs/`.

Latest verified release checks (4 November 2026): 21 Maven H2 tests passed and Maven package completed successfully. A real MySQL-backed Admin → Manager → Employee workflow, including PDF authorization and ownership restrictions, was verified through the running API.
