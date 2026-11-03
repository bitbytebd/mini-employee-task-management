# Database Assets

The authoritative runtime SQL scripts are `backend/src/main/resources/schema.sql` and `backend/src/main/resources/data.sql` because Spring Boot loads them from the backend classpath.

Do not create a second editable copy of these scripts here. Add future versioned database migrations to this directory only after the live `taskdb` schema has been inspected with authorized read-only access.
