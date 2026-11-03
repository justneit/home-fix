# HomeFix — Home Repair Service Marketplace

HomeFix connects customers who need home repairs with qualified technicians.
Customer posts a repair request → the matching engine recommends workers → workers quote →
the customer picks one → booking → work done → completion confirmed → review.

University Software Engineering graduation project (2 full-stack developers).
Architecture: **modular monolith** (Spring Boot) + **React SPA**, MySQL, REST `/api/v1`.

> **Status:** Phase 0–1 foundations are done: repository, tooling, CI, Docker, database
> migrations, authentication (JWT + rotating refresh cookies), service catalog, and a working
> frontend ↔ backend slice. Business modules follow the [roadmap](docs/roadmap.md).

## Tech stack

| Layer | Technologies |
|---|---|
| Frontend | React 19, TypeScript 6.0, Vite 8, React Router 8, Tailwind CSS 4, shadcn/ui (Radix), TanStack Query 5, Zustand 5, React Hook Form + Zod 4, Axios, Lucide |
| Backend | Java 21, Spring Boot 4.1.2 (Web MVC, Security 7, OAuth2 Resource Server/JWT, Data JPA / Hibernate 7, Validation, Actuator), Flyway, MapStruct 1.6, springdoc-openapi 3.2 |
| Data | MySQL 8.4 (InnoDB, utf8mb4), MinIO (S3-compatible, from Phase 3) |
| Testing | JUnit 5, Mockito, Testcontainers 2, Spring MockMvc · Vitest 5, React Testing Library · Playwright |
| Tooling | Maven Wrapper (3.10), npm, ESLint 10, Prettier, Spotless (palantir-java-format), Docker Compose, GitHub Actions |

Leaflet/OpenStreetMap, STOMP/WebSocket and the AI adapter are added in the phases that use
them (4, 6, 8). See [docs/roadmap.md](docs/roadmap.md).

## Repository layout

```
├── backend/                Spring Boot modular monolith (com.homefix.<module>)
├── frontend/               React SPA (feature-based src/features/*), Playwright e2e/
├── infrastructure/         Infrastructure notes and future deployment assets
├── docs/                   Architecture, database, API, security, testing, ADRs
├── scripts/                Developer scripts (init-env.sh, setup-host-mysql.sh)
├── .github/workflows/      CI pipeline
├── docker-compose.yml      MySQL, MinIO (+ backend, frontend with --profile app)
└── .env.example            All configuration variables, documented
```

## Prerequisites

- Java 21, Node.js 24 + npm, Docker with Compose v2 (Maven comes with the wrapper)
- Free ports: 3306 (or change `DB_PORT`), 8080, 5173, 9000/9001, 3000

## Quick start

### 1. Configure

```bash
./scripts/init-env.sh          # creates .env with random local secrets
```

### 2a. Local development (hot reload) — recommended

```bash
# Database — pick one:
./scripts/setup-host-mysql.sh             # a) MySQL installed on this machine (creates db + user; needs sudo)
docker compose --profile db up -d         # b) containerized MySQL 8.4 (if host port 3306 is taken, set DB_PORT=3307)
docker compose up -d                      # MinIO
cd backend && ./mvnw spring-boot:run      # http://localhost:8080 (profile "dev", reads ../.env)
cd frontend && npm install && npm run dev # http://localhost:5173 (proxies /api to :8080)
```

### 2b. Everything in containers

```bash
docker compose --profile app up -d --build --wait
# App:     http://localhost:3000   (nginx serves the SPA and proxies /api)
# API:     http://localhost:8080
```

### Useful URLs

| What | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Health | http://localhost:8080/actuator/health |
| MinIO console | http://localhost:9001 |

### Demo accounts (dev profile only)

Password for all: `HomeFix@123`

| Role | Email |
|---|---|
| Admin | `admin@homefix.test` |
| Customer | `khachhang@homefix.test` |
| Worker (verified) | `thosua@homefix.test` |

## Common commands

```bash
# Backend
cd backend
./mvnw verify                 # compile + all tests (needs Docker for Testcontainers) + format check
./mvnw spotless:apply         # format Java code
# Frontend
cd frontend
npm run lint && npm run format:check && npm run typecheck
npm test                      # Vitest
npm run build
E2E_BASE_URL=http://localhost:3000 npm run test:e2e   # Playwright (stack must be running)
```

## Documentation

- [Architecture](docs/architecture.md) · [Requirements](docs/requirements.md) · [Database](docs/database.md) · [API](docs/api.md)
- [Development guide](docs/development-guide.md) · [Collaboration](docs/collaboration.md) · [Testing](docs/testing.md)
- [Security](docs/security.md) · [Deployment](docs/deployment.md) · [Roadmap](docs/roadmap.md) · [Decisions (ADRs)](docs/decisions/)
