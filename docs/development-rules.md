# Development Rules

AU Van should stay simple, readable, and easy to explain.

## Backend

- Group backend code by domain: `booking`, `payment`, `route`, `timeslot`, `seat`, `user`, `notification`, `reminder`, `audit`, `auth`, `dashboard`, and `line`.
- Keep cross-cutting code in `shared`: config, common DTOs, exception handling, mapping, security, scheduling support, and idempotency.
- Controllers are thin: parse input, call a service, return a response.
- Services own business logic and transaction boundaries.
- Spring Data repositories own database access and live under the domain's `repository/` folder.
- Do not create fake repositories for domains that do not own persisted entities.
- DTOs define request and response shapes inside the domain that owns the API.
- Exceptions go through centralized handlers in `shared/exception`.
- Security checks must be explicit at the API boundary.
- Prefer straightforward methods over layered patterns that do not add value.
- Do not add ports, adapters, command buses, or domain-event frameworks unless a real feature needs them.

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
