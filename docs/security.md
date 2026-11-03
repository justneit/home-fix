# Security

## Authentication

| Aspect | Implementation |
|---|---|
| Passwords | `DelegatingPasswordEncoder` → BCrypt (cost 10); hashes stored with `{bcrypt}` prefix so the algorithm can be upgraded. Max 72 UTF-8 bytes (BCrypt limit) enforced explicitly |
| Access token | HS256 JWT, 15 min (`JWT_ACCESS_TOKEN_TTL`), claims `iss`, `sub` (user id), `role`, `iat`, `exp`. Validated by Spring Security's resource server (signature, expiry, issuer). Held **in memory only** by the SPA |
| Refresh token | 256-bit random, opaque; only its SHA-256 hash is stored. HttpOnly, `Secure` (prod), `SameSite=Strict`, `Path=/api/v1/auth` cookie, 14 days |
| Rotation & reuse detection | Each refresh revokes the presented token and issues a successor in the same *family*. Presenting a revoked token revokes the entire family. Rows are locked (`SELECT … FOR UPDATE`) during rotation |
| Logout | Revokes the family and clears the cookie. Already-issued access tokens stay valid until expiry (≤ 15 min) |
| Login failures | Same `401 INVALID_CREDENTIALS` and comparable timing for unknown e-mail and wrong password (dummy hash check). Account status is revealed only after a correct password |
| Account status | `SUSPENDED`/`DISABLED` accounts cannot log in or refresh |
| Rate limiting | Per client IP, fixed one-minute window: login & register 10/min, refresh 60/min (configurable). In-memory → valid for a single instance |
| Signing key | `JWT_SECRET` ≥ 32 chars, required at startup (validated). Generate with `openssl rand -base64 48`. Rotating it logs every user out |

## CSRF

Bearer-token requests are immune to CSRF (browsers never attach the header automatically).
Only endpoints authenticated by the refresh **cookie** — `POST /auth/refresh` and
`POST /auth/logout` — require a CSRF token, using Spring Security's SPA double-submit pattern
(`XSRF-TOKEN` cookie → `X-XSRF-TOKEN` header). `SameSite=Strict` on the refresh cookie is a
second, independent layer.

## Authorization

- URL rules in `SecurityConfig`: public endpoints are explicitly listed; `/api/v1/admin/**`
  requires `ADMIN`; everything else requires authentication.
- `@EnableMethodSecurity` is on for `@PreAuthorize` where useful.
- **Object-level checks live in services** (e.g. "only the booking's customer or worker may read
  its messages"). Services receive the caller's id (`@CurrentUserId`) and must verify ownership
  before returning or mutating data; prefer repository queries scoped by owner
  (`findByIdAndCustomerId`) and return `404` for resources the caller may not see.
- WebSocket (Phase 6): authenticate the STOMP `CONNECT` frame with the access token and authorize
  every `SUBSCRIBE`/`SEND` destination against booking participation.

## Transport, CORS, headers

- Production must be served over HTTPS; `COOKIE_SECURE=true` (default outside `dev`).
- The SPA and API share one origin through nginx/Vite, so CORS is normally unused. When a
  separate origin is needed, list it exactly in `CORS_ALLOWED_ORIGINS` (no wildcards; credentials
  allowed only for listed origins).
- Spring Security default headers (`X-Content-Type-Options`, `X-Frame-Options: DENY`,
  cache control, HSTS over HTTPS); nginx adds `Referrer-Policy`.
- `server.forward-headers-strategy=native`: `X-Forwarded-*` is honoured only from internal
  proxy addresses, so clients cannot spoof their IP for rate limiting.

## Data protection & privacy

- Error responses never contain stack traces or SQL (`server.error.include-*: never`; generic
  `INTERNAL_ERROR`); details go to server logs.
- Do not log passwords, tokens, full addresses or phone numbers. Log user ids instead.
- Precise customer addresses are visible only to the customer and the assigned worker; other
  workers see approximate location (Phase 4).
- Data sent to an external AI provider (Phase 8) is limited to the issue description and
  category hints — no names, phone numbers, addresses or images with personal data.

## File uploads (Phase 3)

Validate size (`spring.servlet.multipart` limits: 5 MB/file, 25 MB/request), sniff content type
from magic bytes (allow JPEG/PNG/WebP only), generate random object keys, store in a private
MinIO bucket and serve via short-lived pre-signed URLs after an authorization check.

## Secrets

- Never commit `.env` or real credentials (`.gitignore` covers `.env*` except `.env.example`).
- Demo accounts exist only in the `dev` profile (`db/dev-seed`), with a documented password.
- CI generates throwaway secrets per run.

## Audit (Phase 7)

`audit_logs` records sensitive admin actions (worker verification, account suspension, complaint
resolution, review moderation): actor, action, target type/id, before/after summary, timestamp.

## Known limitations

- Access tokens cannot be revoked before expiry (short TTL mitigates). A token-version column
  on `users` can be added if immediate revocation becomes necessary.
- Two browser tabs refreshing at exactly the same moment may trigger reuse detection and log
  the user out (the SPA de-duplicates refreshes within a tab).
- Rate limiting is per instance and in memory.
- Registration reveals whether an e-mail is registered (`409`); accepted trade-off for usability,
  mitigated by rate limiting.
