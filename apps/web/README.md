# AU Van Frontend

New React frontend for AU Van.

This app is intentionally separate from `AU-Van/` so we can migrate safely:

- `AU-Van/` stays as the working reference app.
- `frontend/` becomes the new React-only client.
- `backend/` remains the single Java API/backend.

Readability and structure rules live in [`docs/frontend-patterns.md`](./docs/frontend-patterns.md).

## Principles

- No server-side business logic in this frontend.
- One API client layer for backend communication.
- React Query for caching and request state.
- Feature folders kept shallow and readable.
- UI can be migrated from `AU-Van/` page-by-page without copying old backend code.

## Planned Migration Order

1. LIFF shell and route schedule page
2. Auth screens
3. Route and timeslot browsing
4. Seat selection and booking
5. Payment proof flow
6. Admin dashboard pages

## Run

```bash
npm install
npm run dev
```

## Environment

Create `.env` in `frontend/`:

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_LINE_LIFF_ID=
VITE_PAYMENT_QR_IMAGE_URL=
VITE_PAYMENT_ACCOUNT_NAME=
VITE_PAYMENT_ACCOUNT_NUMBER=
VITE_PAYMENT_BANK_NAME=
```

Notes:

- `VITE_API_BASE_URL` should point to the Spring Boot backend.
- `VITE_LINE_LIFF_ID` is required for student LIFF login.
- Payment env values are optional and only affect what payment instructions the screen displays.
- Admin login can work without LIFF, but LIFF routes will stay behind the LINE auth gate.
