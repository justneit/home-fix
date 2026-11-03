# API

Base path: **`/api/v1`**. JSON over HTTPS. Interactive documentation (dev):
<http://localhost:8080/swagger-ui.html> · OpenAPI document: `/v3/api-docs`.

## Conventions

| Topic | Convention |
|---|---|
| Resources | Plural nouns (`/repair-requests`, `/bookings/{id}`); actions on state machines as sub-resources (`PATCH /bookings/{id}/status`, `POST /quotations/{id}/accept`) |
| Auth | `Authorization: Bearer <accessToken>` except public endpoints below |
| Success bodies | The resource DTO itself (no envelope). `201 Created` for creation, `204` for no content |
| Lists | `?page=0&size=20&sort=createdAt,desc` + filters as query params → `PageResponse` `{content, page, size, totalElements, totalPages}`. Small bounded reference lists (e.g. categories) return a plain array |
| Errors | RFC 9457 `application/problem+json` (see below) |
| Time | ISO-8601 UTC instants (`2026-11-02T09:24:46.792Z`) |
| IDs | Numeric; every access is authorization-checked, so IDs are not secrets |

## Error format

```json
{
  "title": "Bad Request",
  "status": 400,
  "detail": "Request validation failed",
  "instance": "/api/v1/auth/register",
  "code": "VALIDATION_FAILED",
  "errors": [
    { "field": "email", "code": "Email", "message": "must be a well-formed email address" }
  ]
}
```

`code` values (`common/error/ErrorCode.java`) are stable and translated by the frontend
(`frontend/src/locales/vi.ts → errors`). `detail` is English and safe to show to developers.

| Code | HTTP | Meaning |
|---|---|---|
| `VALIDATION_FAILED` | 400 | Field validation failed; see `errors[]` |
| `MALFORMED_REQUEST` | 400 | Unreadable JSON, wrong types, invalid enum value |
| `UNAUTHORIZED` | 401 | Missing/invalid/expired access token (`WWW-Authenticate: Bearer`) |
| `INVALID_CREDENTIALS` | 401 | Login failed (same response for unknown e-mail and wrong password) |
| `INVALID_REFRESH_TOKEN` | 401 | Refresh cookie missing, expired, revoked or reused |
| `ACCESS_DENIED` | 403 | Authenticated but not allowed (role, ownership, missing CSRF token) |
| `ACCOUNT_INACTIVE` | 403 | Correct password but account suspended/disabled |
| `NOT_FOUND` | 404 | Resource does not exist (or is not visible to the caller) |
| `EMAIL_ALREADY_REGISTERED` | 409 | Registration with an existing e-mail |
| `CONFLICT` | 409 | Concurrent modification / constraint violation |
| `RATE_LIMITED` | 429 | Too many attempts (`Retry-After` header) |
| `INTERNAL_ERROR` | 500 | Unexpected failure (details only in server logs) |

## Implemented endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/auth/register` | public | `{email, password, fullName, phone?, accountType: CUSTOMER\|WORKER}` → `201 AuthResponse` + refresh cookie |
| POST | `/auth/login` | public | `{email, password}` → `200 AuthResponse` + refresh cookie |
| GET | `/auth/csrf` | public | `204`, sets `XSRF-TOKEN` cookie |
| POST | `/auth/refresh` | refresh cookie + `X-XSRF-TOKEN` | Rotates cookie → `200 AuthResponse` |
| POST | `/auth/logout` | refresh cookie + `X-XSRF-TOKEN` | Revokes session family, clears cookie → `204` |
| GET | `/users/me` | bearer | Current account (`UserResponse`) |
| GET | `/services` | public | Active service categories in display order |
| GET | `/actuator/health` (+`/liveness`, `/readiness`) | public | Health probes (outside `/api/v1`) |

`AuthResponse`: `{accessToken, tokenType: "Bearer", expiresIn (seconds), user: UserResponse}`.
`UserResponse`: `{id, email, fullName, phone, role, status, createdAt}`.

## Planned endpoints (indicative)

| Phase | Endpoints |
|---|---|
| 2 | `GET/PUT /users/me/profile`, `/users/me/addresses`, `GET/PUT /workers/me` (+ `/skills`, `/service-areas`, `/availability`), `GET /workers/{id}` (public profile) |
| 3 | `POST/GET /repair-requests`, `GET /repair-requests/{id}`, `POST /repair-requests/{id}/media`, `POST /repair-requests/{id}/cancel` |
| 4 | `GET /repair-requests/{id}/recommendations` (explainable scores), `GET /worker/requests` (matching open requests), `GET /geocoding/search` |
| 5 | `POST /repair-requests/{id}/quotations`, `GET /repair-requests/{id}/quotations`, `POST /quotations/{id}/accept`, `POST /quotations/{id}/withdraw`, `GET /bookings`, `PATCH /bookings/{id}/status` |
| 6 | `GET /bookings/{id}/messages`, STOMP `/ws` (`/app/bookings/{id}/messages`, `/user/queue/notifications`), `GET /notifications`, `POST /notifications/{id}/read` |
| 7 | `POST /bookings/{id}/review`, `POST /complaints`, `/admin/**` (users, workers/verify, categories, requests, bookings, reviews, complaints, stats) |
| 8 | `POST /ai/classify-request` |
