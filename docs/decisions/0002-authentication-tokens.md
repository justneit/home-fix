# 0002 — Authentication tokens

**Status:** accepted (2026-11-02)

**Context.** SPA + REST API. Requirements: short-lived JWTs, rotating refresh tokens, no
long-lived credentials in `localStorage`, CSRF protection when cookies are used.

**Decision.**
- Access token: HS256 JWT (15 min) issued with Spring Security's `NimbusJwtEncoder` and
  validated by the OAuth2 resource-server filter (no hand-written JWT filter). Kept in memory.
- Refresh token: random opaque value, stored hashed, rotated on every use, organised in
  families for reuse detection; delivered as `HttpOnly; Secure; SameSite=Strict;
  Path=/api/v1/auth` cookie.
- The SPA restores a session on page load by calling `/auth/refresh`.
- CSRF double-submit token required only on the two cookie-authenticated endpoints.
- SPA and API are served from one origin (Vite proxy / nginx) so cookies are first-party.

**Alternatives.** Server sessions (simpler revocation, but WebSocket/STOMP and the mandated JWT
design favour tokens); JWT refresh tokens (cannot be revoked without a store anyway);
`localStorage` tokens (XSS-exfiltratable — rejected).

**Consequences.** Logout/suspension take effect for access tokens only after ≤ 15 min.
Concurrent refreshes from two tabs can trip reuse detection (rare; re-login). HS256 means the
backend is the only issuer and verifier; switch to RS256 if another service must verify tokens.
