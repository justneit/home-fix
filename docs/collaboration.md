# Collaboration guide

Two full-stack developers. Ownership is by **feature**, not by layer: the owner of a feature
builds its migration, backend module, frontend feature folder and tests.

## Module ownership

| Developer 1 | Developer 2 | Shared |
|---|---|---|
| `user` (accounts, profiles, addresses) | `worker` services: skills, areas, availability | Architecture, `common`, `config` |
| `repairrequest`, `storage` | `quotation`, `booking` (lifecycle) | Authentication conventions |
| `location`, `matching` | `chat`, `notification` | Database & API conventions |
| `ai` | `review`, `complaint` | Integration/E2E tests |
| Admin: users & worker verification | Admin: categories, bookings, reviews, complaints | Deployment, documentation |

The owner reviews every change to their module; shared areas need both developers' approval.

## Branching

- `main` is always releasable and protected: no direct pushes, CI must pass, 1 approval.
- Short-lived branches from `main`: `feat/<module>-<topic>`, `fix/<topic>`, `docs/<topic>`,
  `chore/<topic>` (e.g. `feat/booking-acceptance`).
- Rebase on `main` before opening/merging a PR; squash-merge with a Conventional Commit title.

## Commits

[Conventional Commits](https://www.conventionalcommits.org/): `feat(booking): accept quotation with row lock`,
`fix(auth): …`, `test(…)`, `docs(…)`, `chore(…)`, `refactor(…)`. Small, focused commits.

## Pull requests

- One feature slice per PR (migration + backend + frontend + tests), ideally < 600 changed lines.
- Description: what/why, screenshots for UI, migration notes, how it was tested.
- Checklist: tests added for rules and negative paths · `ErrorCode` + Vietnamese text added if
  new · strings in `vi.ts` · docs updated (`api.md`, `database.md`) · no secrets.

## Code review focus

1. Correctness of business rules and state transitions; transaction boundaries.
2. Authorization: is ownership checked in the service? Could another user read/modify this?
3. Data integrity: constraints, indexes, migration safety (never edit an applied migration).
4. API contract consistency (paths, status codes, error codes, pagination).
5. Tests cover failure paths. Then readability; formatting is automated.

## Contracts between modules

Agree on DTOs and endpoint shapes in the PR (or a short issue) **before** implementing across
both sides; record them in `docs/api.md`. Cross-module calls go through the other module's
service, never its repository. Migration numbers: claim the next `V<n>` in the PR title to
avoid collisions; if two PRs collide, the later one renumbers before merging.

## Cadence

- Weekly planning (30 min): pick roadmap items, agree contracts.
- Async daily update in the team channel: done / next / blocked.
- End of each phase: demo the working slice, update `roadmap.md`.
