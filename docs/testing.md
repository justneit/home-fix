# Testing

## Strategy

| Level | Tooling | Location | Focus |
|---|---|---|---|
| Backend unit | JUnit 5, Mockito, AssertJ | `backend/src/test/**/*Test.java` (no Spring context) | Business rules in services, algorithms (rate limiter, later matching scores) |
| Backend repository | `@DataJpaTest` + Testcontainers MySQL | `*RepositoryTest.java` | Real Flyway migrations, constraints, custom queries |
| Backend integration | `@IntegrationTest` (`@SpringBootTest` + MockMvc + Testcontainers) | `*IntegrationTest.java`, `*ControllerTest.java` | HTTP contract, security rules, error format, transactions |
| Frontend unit/component | Vitest, React Testing Library, user-event, jsdom | `frontend/src/**/*.test.ts(x)` | Schemas, forms, route guards, error mapping |
| End-to-end | Playwright (desktop Chrome + Pixel 7) | `frontend/e2e/` | Critical user journeys against a running stack |

The MySQL Testcontainer (`mysql:8.4`) is shared by all Spring contexts in one JVM
(`support/TestcontainersConfiguration`). Tests use unique e-mails instead of rollbacks, because
MockMvc flows span several committed transactions.

## Commands

```bash
cd backend && ./mvnw verify                       # all backend tests + Spotless (Docker required)
cd backend && ./mvnw test -Dtest=AuthFlowIntegrationTest
cd frontend && npm test                           # Vitest once;  npm run test:watch
# E2E: start a stack with dev seed data, then
cd frontend && npm run test:e2e                   # against http://localhost:5173 (npm run dev)
E2E_BASE_URL=http://localhost:3000 npm run test:e2e  # against docker compose --profile app
```

Repeated E2E runs from one machine can hit the login rate limit (10/min). For local loops set
`AUTH_RATE_LIMIT_PER_MINUTE=100` in `.env` (CI does this).

## Current coverage of critical rules

| Scenario | Test |
|---|---|
| Register customer/worker, cookie flags, worker profile `PENDING` | `AuthFlowIntegrationTest` |
| Duplicate e-mail (case-insensitive), invalid input field errors, cannot self-register ADMIN | `AuthFlowIntegrationTest` |
| Generic login failure; suspended account blocked; timing-safe unknown e-mail | `AuthFlowIntegrationTest`, `AuthServiceTest` |
| Unauthenticated / invalid token → 401 problem detail; wrong role → 403 | `AuthFlowIntegrationTest` |
| Refresh needs CSRF; rotation; reuse revokes family; logout revokes | `AuthFlowIntegrationTest` |
| Rate limiting per IP | `AuthRateLimitIntegrationTest`, `FixedWindowRateLimiterTest` |
| Schema constraints (unique e-mail, token hash), family revocation, expiry cleanup | `RefreshTokenRepositoryTest` |
| Health, readiness, OpenAPI published | `HomeFixApplicationTests` |
| Category listing (public, ordered, DTO only) | `ServiceCategoryControllerTest` |
| Form validation, server error display, route guards | Vitest suites |
| Register (customer & worker), login error, session restore on reload, logout, role isolation | `e2e/auth.spec.ts` |

## Required tests for upcoming phases

Each phase adds tests for its negative paths: invalid state transitions, duplicate quotation
acceptance (concurrent test with two threads), double booking, invalid uploads (type/size),
unauthorized chat subscription, review without completed booking, duplicate review. The E2E
suite grows to the full journey: request → quotation → acceptance → lifecycle → review.
