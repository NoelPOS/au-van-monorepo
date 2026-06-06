# Development Rules

AU Van should stay simple, readable, and easy to explain.

## Backend

- Controllers are thin: parse input, call a service, return a response.
- Services own business logic and transaction boundaries.
- Repositories own database access.
- DTOs define request and response shapes.
- Exceptions go through centralized handlers.
- Security checks must be explicit at the API boundary.
- Prefer straightforward methods over layered patterns that do not add value.

## Frontend

- Pages are route entry points, not business logic containers.
- Feature API files call the shared API client.
- React Query hooks live beside the feature they load.
- Components should not build URLs or call `fetch` directly.
- Keep feature folders shallow and predictable.

## General

- Keep names boring and obvious.
- Add comments only when they clarify non-obvious behavior.
- Do not introduce new tools unless they solve an actual project problem.
- Update docs when behavior, setup, or architecture changes.
