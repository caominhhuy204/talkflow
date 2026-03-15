# TaskFlow Frontend (React.js)

React UI synced with current backend APIs:
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `GET/POST/PUT/DELETE /api/projects`
- `POST/GET /api/requests`
- `POST /api/requests/{id}/submit`
- `POST /api/requests/{id}/cancel`
- `GET /api/approvals/inbox`
- `POST /api/approvals/{requestId}/approve`
- `POST /api/approvals/{requestId}/reject`
- `GET /api/reports/summary`
- `GET/PUT /api/workflow-policies`

## Run locally

1. Install dependencies:

```bash
npm install
```

2. Configure API base URL:

```bash
cp .env.example .env
```

3. Start dev server:

```bash
npm run dev
```

Default URL: `http://localhost:5173`

## API integration behavior

- Uses JWT returned by backend login/register and stores it in localStorage.
- Sends `Authorization: Bearer <token>` for project APIs.
- Enables role-based UI:
  - `ADMIN`: full project CRUD + update workflow policy
  - `MANAGER`, `EMPLOYEE`, `HR`, `FINANCE`: read-only project list
  - `ADMIN`, `MANAGER`, `HR`, `FINANCE`: report summary
- In dev mode, Vite proxies `/api/*` to `http://localhost:8080`.
