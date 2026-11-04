# Mini Employee Task Management

This workspace contains a Java 17 / Spring Boot 3 backend and an Angular 13 frontend for employee and task management. The Maven backend is in `backend/`; the Angular application is in `frontend/`.

Important: there was no AGENTS.md, CODEX_PROJECT_BLUEPRINT.md, or employer reference PDF present in this workspace, so this project was created directly from the task description without inventing additional requirements.

## Stack

- Java 17
- Spring Boot 3.3.x
- Maven
- Spring Data JPA
- MySQL 8 (`taskdb`)
- Spring Security + JWT

## Local configuration

Set the following environment variables before running the app:

- `DB_HOST` (optional; defaults to `localhost`)
- `DB_PORT` (optional; defaults to `3306`)
- `DB_NAME` (optional; defaults to `taskdb`)
- `DB_USERNAME` (required)
- `DB_PASSWORD` (required)
- `JWT_SECRET` (required; use a long random value)

Example:

```powershell
$env:DB_HOST = "localhost"
$env:DB_PORT = "3306"
$env:DB_NAME = "taskdb"
$env:DB_USERNAME = "your-local-mysql-user"
$env:DB_PASSWORD = "your-local-mysql-password"
$env:JWT_SECRET = "a-long-random-secret-not-committed-to-git"
```

## Run locally

```powershell
Set-Location backend
mvn spring-boot:run
```

In another terminal, start the frontend with its project-local CLI:

```powershell
Set-Location frontend
npm start
```

The frontend calls `http://localhost:8080/api`; Spring CORS allows Angular's default port 4200.

## H2 demonstration mode

For a temporary H2-backed demonstration, double-click `START-PROJECT.cmd` in the project root. It opens separate backend and frontend CMD windows only when ports 8080 and 4200 are not already in use, waits for the frontend, then opens `http://localhost:4200/login`.

Run `STOP-PROJECT.cmd` to stop only the server process trees that the launcher itself recorded. It never targets unrelated Node, Java, or MySQL processes. The H2 demonstration data is in-memory and resets when its backend window closes.

## Persistent MySQL mode

Ensure MySQL is running and `taskdb` exists, then double-click `START-MYSQL-PROJECT.cmd`. The launcher securely prompts for the MySQL username and password, starts Spring Boot with the `mysql` profile, and never saves database credentials in project files.

The MySQL profile uses `schema.sql` with non-destructive `CREATE TABLE IF NOT EXISTS` statements for `users`, `user_roles`, and `tasks`. Existing records are preserved. `DatabaseSeeder` creates a demo account only when it is missing and does not overwrite existing passwords, roles, or user data.

Keep the frontend running with `frontend\run-dev-server.cmd` or start it separately after MySQL backend startup. Stop the MySQL backend by closing its dedicated launcher window; this does not affect MySQL Server or the H2 launcher.

## Demo accounts

The application seeds four demo accounts:

- Admin: `admin@fifo.local`
- Manager: `manager@fifo.local`
- Employees: `employee1@fifo.local`, `employee2@fifo.local`

Passwords are seeded for local development only. Change or replace them before a real deployment.

## Security notes

- Login is public; user creation/registration is restricted to administrators.
- Passwords are stored using BCrypt.
- Authentication is stateless using JWT bearer tokens.
- Authorization is enforced with `@PreAuthorize`.

## Main API routes

- `POST /api/auth/login`
- `GET /api/dashboard/stats`
- `GET|POST|PUT|DELETE /api/users` (administrator scope)
- `GET /api/users/me`, `GET /api/users/team` (manager team)
- `GET|POST|PUT|DELETE /api/tasks`, `PUT /api/tasks/{id}/status`
- `GET /api/reports/tasks/pdf` (administrator/manager scope)

See `docs/FIFO-Tech.postman_collection.json` and `docs/UML_DIAGRAMS.md` for submission support material.
