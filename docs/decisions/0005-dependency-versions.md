# 0005 — Dependency versions

**Status:** accepted (2026-11-02). Versions verified against Maven Central metadata and the npm
registry on that date.

| Component | Version | Note |
|---|---|---|
| Java | 21 (LTS) | Mandated |
| Spring Boot | 4.1.2 | Latest GA (4.2 only RC). Boot 4 uses modular starters (`spring-boot-starter-webmvc`, `-flyway`, …) and Jackson 3 |
| Spring Security | 7.1.2 | Via Boot |
| springdoc-openapi | 3.2.0 | Built against Boot 4.1.2 |
| MapStruct | 1.6.3 | Latest stable (1.7 in beta) |
| Testcontainers | 2.x | Managed by Boot (`testcontainers-mysql`) |
| Maven | 3.10.0 | Via wrapper (system Maven 3.8.7 is too old to rely on) |
| MySQL | 8.4 LTS image | Mandated |
| Node.js | 24 LTS | |
| TypeScript | 6.0.3 | **Not 7.0**: typescript-eslint 8.72 supports `<6.1`; revisit when it supports 7 |
| React / Vite / React Router | 19.3 / 8.3 / 8.5 | |
| Tailwind / shadcn CLI | 4.3 / 4.23 (style `radix-nova`) | |
| Zod / React Hook Form / TanStack Query / Zustand | 4.6 / 7.91 / 5.105 / 5.0 | |
| Vitest / Playwright | 5.0 / 1.64 | |

**Deferred until used** (to avoid unused dependencies): Leaflet + react-leaflet (Phase 4),
`@stomp/stompjs` and `spring-boot-starter-websocket` (Phase 6), MinIO/S3 client (Phase 3),
AI provider SDK (Phase 8). Redis is not used: the only candidate (rate limiting) is fine
in-memory for a single instance.

**Not used:** Lombok (records + explicit code are sufficient and avoid annotation-processor
interplay with MapStruct).
