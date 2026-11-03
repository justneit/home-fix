# 0004 — MinIO container image

**Status:** accepted (2026-11-02)

**Context.** MinIO is mandated for local S3-compatible storage. The upstream `minio/minio`
image is no longer pullable from Docker Hub (verified 2026-11-02: "repository does not exist"),
and `quay.io/minio/minio` requires authentication.

**Decision.** Use the community-maintained rebuild `pgsty/minio`, pinned to
`RELEASE.2026-08-04T00-00-00Z`, overridable with `MINIO_IMAGE` in `.env`. The backend will use
the standard S3 API only, so any S3-compatible server can replace it.

**Consequences.** We depend on a third-party build for local/dev storage. Review the pin
periodically; if it becomes unavailable, swap `MINIO_IMAGE` (or another S3-compatible server)
without code changes.
