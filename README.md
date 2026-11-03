# Mini Employee Task Management

This workspace contains a Java 17 / Spring Boot 3 backend for employee task management. The Maven backend is located in `backend/`.

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

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`

Example:

```powershell
$env:DB_HOST = "localhost"
$env:DB_PORT = "3306"
$env:DB_NAME = "taskdb"
$env:DB_USERNAME = "taskuser"
$env:DB_PASSWORD = "taskpass"
$env:JWT_SECRET = "change-me-to-a-long-random-secret"
```

## Run locally

```powershell
Set-Location backend
mvn spring-boot:run
```

## Demo credentials

The application seeds four demo accounts:

- admin@fifo.local / admin123
- manager@fifo.local / manager123
- employee1@fifo.local / employee123
- employee2@fifo.local / employee123

## Security notes

- No public registration endpoint is provided.
- Passwords are stored using BCrypt.
- Authentication is stateless using JWT bearer tokens.
- Authorization is enforced with `@PreAuthorize`.
