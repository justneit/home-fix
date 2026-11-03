# 0003 — Error format

**Status:** accepted (2026-11-02)

**Decision.** All errors are RFC 9457 problem details (Spring `ProblemDetail`) extended with
`code` (enum `ErrorCode`) and, for validation, `errors: [{field, code, message}]`. Security
filter failures are routed through the same `@RestControllerAdvice`. Success responses return
the DTO directly; lists use `PageResponse`.

**Consequences.** One parser in the frontend (`lib/api-error.ts`); localisation is done
client-side by `code`, so backend messages stay English and developer-oriented.
