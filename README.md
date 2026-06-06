# AU Van Monorepo

AU Van is a portfolio-grade rewrite of a campus van booking system for Assumption University. It focuses on practical operations: student LIFF booking, admin schedule management, seat locking, manual PromptPay review, audit logs, reminders, and LINE integration.

This repository is the clean rewrite monorepo. The original Next.js prototype remains outside this repo as a legacy reference and is not copied here.

## Stack

- Frontend: React, Vite, React Router, TanStack Query, Tailwind CSS
- Backend: Java 21, Spring Boot, Spring Security, JPA
- Database: PostgreSQL with Flyway migrations
- Tooling: pnpm workspaces, Turborepo, Maven

## Structure

```txt
apps/
  web/  React/Vite admin and LIFF frontend
  api/  Spring Boot API
docs/   Architecture and development notes
infra/  Local and deployment infrastructure notes
```

## Local Setup

Install frontend dependencies:

```bash
pnpm install
```

Run the web app:

```bash
pnpm dev:web
```

Run the API:

```bash
cd apps/api
./mvnw spring-boot:run
```

On Windows PowerShell, use:

```powershell
cd apps/api
.\mvnw.cmd spring-boot:run
```

## Checks

```bash
pnpm build:web
pnpm test:api
```

## Project Direction

The rewrite keeps the code intentionally simple. Backend code is grouped by domain, with controllers, DTOs, services, repositories, entities, enums, and domain-owned helpers kept close together. Shared infrastructure lives under `shared/`. Extra architecture is added only when it removes real complexity.
