<div align="center">

# Puuul — Web

**Angular 21 · Zoneless · Standalone · Fast**

</div>

---

## Quick start

```bash
npm install && npm start
# → http://localhost:4200
```

> Requires Node `>=20` and the API server on `http://localhost:9096` (see `api/`).

---

## Features

```
src/app/features/
├── auth/       → Login, registration, session handling
├── events/     → Event browsing and management
├── overview/   → Dashboard views
└── public/     → Public-facing pages (no auth required)
```

---

## Scripts

```bash
npm start          # Dev server with live reload   → :4200
npm run build      # Production build              → dist/
npm test           # Unit tests
npm run cy:open    # Cypress interactive
npm run cy:run     # Cypress headless (CI)
```

---

## Environment

```ts
// src/environments/environment.prod.ts
apiBaseUrl: 'http://localhost:9096'; // ← change before deploying
```

---

## Testing

See [TESTING.md](./TESTING.md).
