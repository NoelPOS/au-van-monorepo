# Architecture

AU Van is organized as a small full-stack monorepo with two deployable apps:

- `apps/web`: React/Vite frontend for admin and LIFF student flows.
- `apps/api`: Spring Boot backend for auth, booking, payment, routes, users, reminders, and LINE integration.

## Why This Monorepo

The frontend and backend move together during the rewrite. Keeping them in one repository makes API changes, documentation, CI, and portfolio review easier without turning the system into a complicated platform.

## Why React/Vite And Spring Boot

React/Vite is a good fit for authenticated operational screens and mobile LIFF flows where server rendering is not the main value. Spring Boot gives the backend a clear structure for controllers, services, repositories, validation, security, scheduled jobs, and PostgreSQL persistence.

## Simplicity Rule

Use the simple path first:

```txt
controller -> service -> repository -> DTO -> exception handler
```

Controllers should validate and delegate. Services should hold business rules. Repositories should only handle persistence. DTOs should keep API input and output explicit. Exceptions should be handled centrally.

## What To Avoid

- No complex clean architecture folder ceremony unless the feature actually needs it.
- No premature framework for domain events, queues, or generated clients.
- No generic helper folders that become dumping grounds.
- No abstraction until there is a second real use case.

The goal is production-minded code that is easy to read in a portfolio review.
