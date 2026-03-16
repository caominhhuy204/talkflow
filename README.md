# TaskFlow

TaskFlow la he thong quan ly cong viec/noi bo gom:
- Backend: Spring Boot REST API (JWT, PostgreSQL, Redis, Swagger)
- Frontend: React + Vite

## Cong nghe su dung

- Java 17, Spring Boot 3
- Spring Security + JWT
- Spring Data JPA (PostgreSQL)
- Redis (cache)
- OpenAPI/Swagger
- React 18, Vite 5
- Docker Compose (PostgreSQL + Redis)

## Cau truc thu muc

```text
taskflow/
|-- src/                  # Backend Spring Boot
|-- frontend/             # Frontend React
|-- docker-compose.yml    # PostgreSQL + Redis
|-- .env.example          # Bien moi truong backend
`-- pom.xml
```

## Yeu cau moi truong

- JDK 17+
- Maven (hoac dung `./mvnw`, `mvnw.cmd`)
- Node.js 18+ va npm
- Docker Desktop (neu chay DB/Redis bang Docker)

## Cau hinh bien moi truong

### Backend

1. Tao file `.env` tu `.env.example` o thu muc root.
2. Cap nhat cac bien chinh:

```env
SPRING_PROFILES_ACTIVE=dev

DB_HOST=localhost
DB_PORT=5433
DB_NAME=taskflow_db
DB_USERNAME=postgres
DB_PASSWORD=123456

REDIS_HOST=localhost
REDIS_PORT=6379

JWT_SECRET_KEY=replace_with_base64_secret_min_32_bytes
JWT_EXPIRATION_MS=3600000

GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_IDS=
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

### Frontend

1. Vao thu muc `frontend`.
2. Tao `.env` tu `frontend/.env.example`:

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_GOOGLE_CLIENT_ID=your_google_client_id
```

## Chay du an

### 1. Khoi dong PostgreSQL + Redis

```bash
docker compose up -d
```

- PostgreSQL: `localhost:5433`
- Redis: `localhost:6379`

### 2. Chay backend

Tai thu muc root:

```bash
./mvnw spring-boot:run
```

Windows:

```bash
mvnw.cmd spring-boot:run
```

Backend chay tai: `http://localhost:8080`

### 3. Chay frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend chay tai: `http://localhost:5173`

## Tai lieu API

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI docs: `http://localhost:8080/v3/api-docs`
- Health check: `http://localhost:8080/actuator/health`

## Luong dang nhap

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/google`
- Dung JWT token theo header:

```http
Authorization: Bearer <token>
```

## Cac nhom API chinh

- Auth: `/api/auth/*`
- Projects: `/api/projects/*`
- Internal requests: `/api/requests/*`
- Approvals: `/api/approvals/*`
- Reports: `/api/reports/*`
- Workflow policy (admin): `/api/workflow-policies/*`
- User options (HR): `/api/users/employee-options`

## Phan quyen chinh (theo code hien tai)

- `EMPLOYEE`: tao/xem don, xem du an, xem bao cao
- `MANAGER`: quan ly du an, duyet don, xem bao cao
- `HR`: giao nhan su vao du an, duyet don, xem bao cao
- `ADMIN`: quan ly workflow policy, xem bao cao

## Chay test backend

```bash
./mvnw test
```

Windows:

```bash
mvnw.cmd test
```

## Ghi chu

- Du an dung `spring.jpa.hibernate.ddl-auto=update`, schema se tu cap nhat theo entity khi chay.
- Frontend Vite da cau hinh proxy `/api` toi `http://localhost:8080` trong moi truong dev.
