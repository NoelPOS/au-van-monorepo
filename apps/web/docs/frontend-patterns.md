# Frontend Readability Pattern

This frontend should stay simple, predictable, and easy to onboard into.

## Core rules

1. One clear responsibility per file.
2. Pages are thin. Feature modules hold UI logic and data wiring.
3. Backend calls go through `src/api` or feature API files only.
4. React Query hooks live close to the feature they load.
5. Avoid duplicate layers. Do not add `service + hook + helper + wrapper` unless each adds real value.
6. Prefer named exports for consistency and easier refactors.

## Folder pattern

```txt
src/
  app/          app shell, providers, router, route constants
  api/          shared HTTP client
  components/   layout and shared presentational pieces
  features/     feature-scoped UI, hooks, and API calls
  lib/          small framework-agnostic helpers
  pages/        route entry files only
  styles/       global styles and tokens
  types/        shared TypeScript types
```

## File order

Use this order whenever possible:

1. imports
2. types
3. constants
4. small helpers
5. component/export

## Naming rules

- Components: `PascalCase.tsx`
- Hooks: `useSomething.ts`
- API modules: `api.ts`
- Shared types: `camel-case.ts` or grouped by domain
- Route files should read like entry points, not business logic containers

## Data rules

- Query hooks call feature API functions.
- Feature API functions call the shared API client.
- Components should not build URLs or call `fetch` directly.

## What to avoid

- Business logic inside route/page files
- Duplicated request logic across components
- Generic utility files that become dumping grounds
- Overusing abstractions before a second real use case exists

## Current pattern to follow

- `pages/*` import one main feature view
- `features/*/components/*` render the feature
- `features/*/hooks/*` load data
- `features/*/api.ts` talks to the backend
- `src/api/client.ts` is the shared HTTP layer

