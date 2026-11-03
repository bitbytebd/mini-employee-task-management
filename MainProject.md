# Mini Employee & Task Management System

## Overview

This repository contains the backend foundation for a role-based employee and task management system. The active Spring Boot project is in `backend/`. A frontend has not been created yet.

## Technology Stack

- Java 17
- Spring Boot 3.3.x
- Maven 3.9.x
- Spring Data JPA
- Spring Security with JWT
- MySQL 8 for runtime data
- H2 for the existing integration-test profile

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

Set the required database and JWT environment variables before running against MySQL. The existing test profile uses H2.

## Current Status

The Spring Boot backend, JWT authentication, role-based authorization, basic user routes, and basic task routes are present. The backend API and test coverage are incomplete, MySQL schema compatibility is unverified, and no frontend has been created.
