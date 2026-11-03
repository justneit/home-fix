# Roadmap

Each phase ends with working, tested functionality (backend + frontend + tests) merged to
`main`. Owners per [collaboration.md](collaboration.md): **D1** = Developer 1, **D2** = Developer 2.

| Phase | Scope | Owner | Status |
|---|---|---|---|
| 0 | Monorepo, tooling, Docker Compose, CI, docs, vertical slice | both | ✅ done |
| 1 | Architecture conventions, Flyway baseline, authentication (JWT + rotating refresh cookie, CSRF, rate limit), role-based routing | both | ✅ done (admin account management moves to Phase 7) |
| 2 | Customer profile & addresses (D1); worker profile, skills, service areas, weekly availability, verification documents (D2) | D1 + D2 | ⏳ next |
| 3 | Service catalog admin CRUD (D2), repair requests with lifecycle, image upload to MinIO (`storage` module) (D1) | D1 + D2 | ⏳ |
| 4 | Leaflet/OSM address picker, geocoding adapter, Haversine filtering, matching engine with explainable scores (D1) | D1 | ⏳ |
| 5 | Quotations, acceptance with locking, bookings, conflict detection, status history (D2) | D2 | ⏳ |
| 6 | STOMP WebSocket (authenticated), booking chat, notifications (D2) | D2 | ⏳ |
| 7 | Reviews, complaints + evidence, admin dashboard & management, audit log (D1: user/worker admin; D2: service/booking admin, reviews, complaints) | D1 + D2 | ⏳ |
| 8 | AI adapter interface, mock provider, real provider behind config, customer correction UI (D1) | D1 | ⏳ |
| 9 | Full E2E journey, hardening, performance check, deployment, final documentation | both | ⏳ |

## Next tasks (Phase 2)

1. D1 — `V3__customer_profiles_addresses.sql`; `GET/PUT /users/me/profile`; address CRUD
   (owner-scoped); customer profile page with forms.
2. D2 — `V4__worker_profile_details.sql` (bio, experience, radius, base location, rating
   aggregates), `worker_skills`, `worker_service_areas`, `worker_availability`;
   `/workers/me/**` endpoints; worker profile & weekly schedule editor.
3. Both — portal navigation shell (sidebar on desktop, sheet on mobile) replacing the
   placeholder cards as features land; `PageResponse`-based list conventions in the frontend.

## Deferred / out of scope for the MVP

Payments, live GPS tracking, push/e-mail/SMS notifications, multi-instance deployment,
microservices, Kafka, Elasticsearch, Kubernetes.
