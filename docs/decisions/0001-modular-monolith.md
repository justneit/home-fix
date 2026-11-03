# 0001 — Modular monolith

**Status:** accepted (2026-11-02)

**Context.** Two developers, one academic year, a domain with ~15 business areas that interact
heavily (bookings touch quotations, chat, reviews, notifications). Operating multiple services
would cost more than it returns.

**Decision.** One Spring Boot application, packaged by business module (`com.homefix.<module>`),
each owning controller/service/repository/entities/DTOs. Modules depend on each other's
services only, without cycles; reactions across modules use Spring application events.
One MySQL schema with Flyway.

**Consequences.** Simple build, deployment, transactions and debugging. Boundaries are enforced
by convention and review (Spring Modulith or ArchUnit tests can be added later to verify them
automatically). Extracting a module into a service later remains possible because modules do
not share repositories.
