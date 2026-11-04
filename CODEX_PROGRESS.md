# FIFO Tech Project Progress

Last updated: 4 November 2026 (Asia/Dhaka)

## Verified working backend

- Spring Boot 3.3.5 / Java 17 compilation completed successfully on 4 November: `mvn --batch-mode --no-transfer-progress compile` (exit code 0).
- The full Maven H2 suite was refreshed on 4 November: 21 tests, 0 failures, 0 errors.
- JWT login, missing/malformed/expired-token handling, role denial, employee task ownership, and status-transition checks are covered by the existing H2 integration suite.
- Latest Surefire reports show: authentication 11/11 passing, task management 5/5 passing, dashboard/PDF 2/2 passing, and user administration 3/3 passing.
- Dashboard statistics, role-scoped task reporting PDF, administrator user management, and scoped task APIs are implemented.
- MySQL profile verification on 4 November: Spring Boot started with Hikari/JPA/Tomcat, admin login returned HTTP 200, and live API CRUD, dashboard, and PDF checks succeeded against `taskdb`.

## Frontend status

- Angular 13 project exists in `frontend/` with its local dependencies present.
- Added real HTTP integration for login, JWT bearer token forwarding, protected routing, dashboard statistics, user management, task management, employee status updates, and PDF report download.
- Angular uses `http://localhost:8080/api`, matching the backend CORS configuration for port 4200.
- Live Angular development server verification succeeded on 4 November: `http://127.0.0.1:4200/` returned HTTP 200 after the initial development bundle completed. The current development server does not serve direct `/login` requests, so begin from the root URL and let Angular route to the login screen. The initial bundle took about 106 seconds on this PC.
- A verified portable Node 16.20.2 runtime was obtained without modifying system Node 24. Angular CLI 13.0.4 serves the development application after a slow initial bundle. Production build verification remains intentionally out of scope for the live demonstration.

## Important limitation

`AGENTS.md` and `CODEX_PROJECT_BLUEPRINT.md` are not present in the actual `E:\mini-employee-task-management` project tree. No unstated employer requirements were invented.

## Remaining verification

1. Confirm persistence with one controlled backend restart when it is safe to interrupt the active MySQL demonstration.
2. Run a manual browser-level smoke test from the user's browser if a screenshot is required; the in-app browser connection was unavailable during verification.

## Local commands

```bat
cd /d E:\mini-employee-task-management
START-PROJECT.cmd

REM Or use persistent MySQL storage (securely prompts for credentials)
START-MYSQL-PROJECT.cmd
frontend\run-dev-server.cmd
```
