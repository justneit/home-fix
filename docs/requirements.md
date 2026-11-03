# Requirements

## Goal

Connect customers who need home repairs with qualified technicians, covering the full
workflow from request to review. Three roles: **CUSTOMER**, **WORKER**, **ADMIN**.

## Functional requirements

Status legend: ✅ implemented · 🟡 partially · ⏳ planned (phase in brackets).

### Authentication & accounts
| ID | Requirement | Status |
|---|---|---|
| AUTH-1 | Customer and worker self-registration (admins are provisioned, never self-registered) | ✅ |
| AUTH-2 | Login with generic failure message; logout | ✅ |
| AUTH-3 | Short-lived JWT access token + rotating HttpOnly refresh cookie with reuse detection | ✅ |
| AUTH-4 | `GET /users/me` current account | ✅ |
| AUTH-5 | Role-based route protection (backend and frontend) | ✅ |
| AUTH-6 | Account status (ACTIVE / SUSPENDED / DISABLED) blocks login and refresh | ✅ |
| AUTH-7 | Rate limiting of login, register and refresh | ✅ |
| AUTH-8 | Admin suspends/reactivates accounts | ⏳ (7) |

### Customer
| ID | Requirement | Status |
|---|---|---|
| CUS-1 | Profile management, multiple addresses | ⏳ (2) |
| CUS-2 | Browse service categories | ✅ |
| CUS-3 | Create repair request (category, description, images, location, preferred time) | ⏳ (3) |
| CUS-4 | View recommended workers and quotations; accept one quotation | ⏳ (4, 5) |
| CUS-5 | Track bookings, chat with assigned worker | ⏳ (5, 6) |
| CUS-6 | Confirm completion, review, complain, service history | ⏳ (5, 7) |

### Worker
| ID | Requirement | Status |
|---|---|---|
| WRK-1 | Worker profile created at registration with `PENDING` verification | ✅ |
| WRK-2 | Professional profile, skills, experience, working areas, radius, weekly availability, available toggle | ⏳ (2) |
| WRK-3 | Browse matching requests, submit quotations | ⏳ (4, 5) |
| WRK-4 | Manage bookings and job status, chat, history, ratings | ⏳ (5–7) |

### Admin
| ID | Requirement | Status |
|---|---|---|
| ADM-1 | All `/api/v1/admin/**` endpoints restricted to ADMIN | ✅ (rule in place) |
| ADM-2 | Dashboard statistics; user, worker, category, request, booking management | ⏳ (7) |
| ADM-3 | Worker verification, review moderation, complaint resolution with audit trail | ⏳ (7) |

### Platform
| ID | Requirement | Status |
|---|---|---|
| PLT-1 | Deterministic, explainable worker matching (see architecture.md) | ⏳ (4) |
| PLT-2 | Maps (Leaflet + OSM), geocoding, nearby filtering, no live GPS tracking | ⏳ (4) |
| PLT-3 | Booking-scoped realtime chat and notifications (STOMP), persisted | ⏳ (6) |
| PLT-4 | Optional AI category/priority/skill suggestion with manual fallback | ⏳ (8) |

## Non-functional requirements

| Area | Requirement |
|---|---|
| Security | OWASP-aligned: hashed passwords (BCrypt), no secrets in code, ownership checks, safe error responses, CSRF protection for cookie-authenticated endpoints, CORS allowlist, upload validation (Phase 3) — see [security.md](security.md) |
| Consistency | Transactions for multi-step writes; DB constraints for invariants (unique accepted quotation, no double booking) |
| Usability | Vietnamese UI by default, strings centralised for i18n; mobile-first, accessible forms (labels, `aria-invalid`, focus states); loading/empty/error states |
| Maintainability | Modular monolith, DTO boundaries, Flyway, formatter + linter in CI |
| Operability | Health/readiness probes, Docker Compose for local and full-stack runs, env-based configuration |
| Testability | Unit, integration (Testcontainers MySQL), component and E2E tests for critical flows |
| Performance (MVP target) | P95 < 500 ms for typical API calls on a single small instance; paginated list endpoints |
