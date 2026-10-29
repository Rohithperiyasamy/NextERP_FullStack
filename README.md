# NextERP — Workflow Management

Spring Boot + React workflow management application. This working copy adds a more informative dashboard and workflow search/filtering, plus a resubmission path when a manager requests additional information.

## Features in this version

- Dashboard KPI cards for users, workflows, tasks, and audit actions.
- Workflow status distribution with progress bars.
- Recent workflow activity and task overview for the signed-in user.
- Search workflows by title, description, type, or ID and filter by status.
- Employee resubmission with an updated description when a request is marked `NEEDS_MORE_INFO`.
- Corrected review behavior so the service no longer invokes Java's `Object.notify()` accidentally when a manager starts a review.
- Credentials moved out of tracked Spring configuration and Docker Compose defaults.

## Frontend

```bash
cd Frontend/FrontEnd/worflow
npm install
cp .env.example .env
npm run dev
```

Set `VITE_API_BASE_URL` to your backend base URL, including `/api`, for example `http://localhost:8081/api`.

## Backend configuration

Run the Spring Boot app from `BackEnd`. Configure these environment variables before startup:

- `SPRING_DATASOURCE_URL` — JDBC URL for the MySQL database.
- `SPRING_DATASOURCE_USERNAME` — database username.
- `SPRING_DATASOURCE_PASSWORD` — database password.
- `JWT_SECRET` — a strong, random secret of at least 32 characters.
- `MAIL_USERNAME` — SMTP sender account.
- `MAIL_PASSWORD` — SMTP app password or provider credential.
- `APP_CORS_ALLOWED_ORIGINS` — comma-separated frontend origin(s), e.g. `http://localhost:5173`.
- `SPRING_JPA_SHOW_SQL` — optional; defaults to `false`.

The app's configured port is currently `8081` in `application.properties`. Align the hosting platform's port setting, Docker configuration, and Spring `server.port` before deployment.

## Before deploying

1. Rotate every credential that appeared in any previously shared ZIP, source control history, IDE configuration, or deployment logs. Removing a value from the latest file does not remove it from Git history.
2. Set the environment variables above in the hosting provider. Never commit `.env` files or app passwords.
3. Configure `APP_CORS_ALLOWED_ORIGINS` to the exact production frontend origin.
4. Run backend tests and `npm run build` in a clean environment.
5. Verify role authorization on every write endpoint. The existing API still passes user/manager IDs from request parameters, so server-side identity/role enforcement should be reviewed before production use.

## Note on this source archive

`BackEnd/workflow/workflow` appears to be a second, older copy of the backend project. The primary backend source used for the changes above is `BackEnd/src/main`. Prefer one canonical backend module before final deployment.
