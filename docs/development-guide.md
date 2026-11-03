# Development guide

## Setup

1. Install Java 21, Node.js 24, Docker (Compose v2). Maven is provided by `backend/mvnw`.
2. `./scripts/init-env.sh` → `.env` with generated secrets (edit ports if needed).
3. Database, either:
   - **MySQL on the host** (8.0.19+; the project targets 8.4): `./scripts/setup-host-mysql.sh`
     creates the database and user from `.env` (runs `sudo mysql`; override with
     `MYSQL_ADMIN_CMD="mysql -uroot -p"`), with `DB_HOST=localhost`, `DB_PORT=3306`; or
   - **containerized MySQL 8.4**: `docker compose --profile db up -d`.

   Then `docker compose up -d` for MinIO. Tests always use a MySQL 8.4 Testcontainer.
4. Backend: `cd backend && ./mvnw spring-boot:run` (profile `dev`; Flyway migrates and seeds
   demo accounts automatically).
5. Frontend: `cd frontend && npm install && npm run dev` → <http://localhost:5173>.

IDE: import `backend/pom.xml` as a Maven project; run `HomeFixApplication` with working
directory `backend/` (so `../.env` is found). For the frontend, enable ESLint and Prettier
format-on-save.

## Environment variables

All variables are listed and explained in [`.env.example`](../.env.example). The backend reads
them from the environment, or from `.env`/`../.env` via `spring.config.import`. Key ones:

| Variable | Purpose | Default |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` (seed data, http cookies) or `prod` | `dev` |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | MySQL connection | host `localhost`, port `3306`, db `homefix` |
| `JWT_SECRET` | HS256 key, ≥ 32 chars | **required** |
| `JWT_ACCESS_TOKEN_TTL`, `REFRESH_TOKEN_TTL` | Token lifetimes | `15m`, `14d` |
| `COOKIE_SECURE` | `Secure` flag on refresh cookie | `false` in dev, `true` otherwise |
| `CORS_ALLOWED_ORIGINS` | Comma-separated extra origins | `http://localhost:5173` in dev |
| `AUTH_RATE_LIMIT_PER_MINUTE`, `REFRESH_RATE_LIMIT_PER_MINUTE` | Throttling | `10`, `60` |
| `API_DOCS_ENABLED` | Swagger UI + OpenAPI | `true` (dev), `false` (prod) |
| `MINIO_*` | Object storage (Phase 3) | — |
| `VITE_API_BASE_URL` (frontend) | API base URL | `/api/v1` |
| `VITE_DEV_BACKEND_URL` (frontend dev) | Vite proxy target | `http://localhost:8080` |

## Adding a backend feature (checklist)

1. Migration `V<next>__<what>.sql` (constraints + indexes; never edit applied ones).
2. Entity + repository inside the owning module package.
3. Request/response records in `<module>/dto`, MapStruct mapper.
4. Service with `@Transactional`, ownership checks, `BusinessException(ErrorCode…)` for rule
   violations. Add a new `ErrorCode` (and its Vietnamese text in `frontend/src/locales/vi.ts`)
   when clients must distinguish the case.
5. Thin controller under `/api/v1/...`, `@Valid`, OpenAPI annotations.
6. Security rule if it is public or role-restricted.
7. Tests: service unit tests for rules, integration test for HTTP + security, negative cases.
8. `./mvnw spotless:apply verify`.

## Adding a frontend feature (checklist)

1. `src/features/<feature>/` with `api.ts` (axios calls + TanStack Query hooks), `schemas.ts`
   (Zod, mirroring backend constraints), `components/`, `pages/`.
2. Strings go into `src/locales/vi.ts`; never hard-code UI text.
3. Every page that fetches data handles loading (Skeleton), empty (`EmptyState`) and error
   (`ErrorState` + retry) states.
4. Register the route in `src/app/router.tsx` with `lazy` and the right `RequireAuth roles`.
5. Use shadcn/ui primitives (`npx shadcn@latest add <component>`), Tailwind tokens from
   `src/index.css` — no ad-hoc colours.
6. Component tests for forms and guards; extend Playwright for new critical journeys.
7. `npm run lint && npm run format:check && npm run typecheck && npm test`.

## Code style

- Java: palantir-java-format via Spotless (`./mvnw spotless:apply`), records for DTOs,
  constructor injection, no Lombok.
- TypeScript: strict mode, ESLint (typescript-eslint, react-hooks incl. React Compiler rules),
  Prettier (no semicolons, single quotes, 100 cols, Tailwind class sorting).

## Troubleshooting

| Symptom | Fix |
|---|---|
| `Port 3306 already in use` when starting the MySQL container | A host MySQL is running: use it (`setup-host-mysql.sh`) or set `DB_PORT=3307` in `.env` |
| `Access denied for user 'homefix'` with host MySQL | Re-run `./scripts/setup-host-mysql.sh` (resets the password to `DB_PASSWORD`) |
| Backend fails: `homefix.security.jwt.secret … must be at least 32 characters` | Set `JWT_SECRET` in `.env` |
| `403 Invalid CORS request` behind a custom proxy | Forward `Host` with port and `X-Forwarded-Port`, or add the origin to `CORS_ALLOWED_ORIGINS` |
| Login returns `RATE_LIMITED` during testing | Raise `AUTH_RATE_LIMIT_PER_MINUTE` or restart the backend |
| Testcontainers cannot find Docker | Start Docker; the current user must be able to run `docker ps` |
| Reset local database | Host MySQL: `sudo mysql -e 'DROP DATABASE homefix'` then `./scripts/setup-host-mysql.sh`. Container: `docker compose --profile db down -v` (deletes volumes!) |
