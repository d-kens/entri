# Web

Angular 21 frontend for the Puuul platform.

---

## Tech stack

| Tool | Version | Purpose |
|------|---------|---------|
| Angular | 21 | Framework — zoneless, standalone components |
| Angular Material | 21 | UI component library |
| Tailwind CSS | 4 | Utility styling |
| TypeScript | 5.9 | Language |
| Cypress | 15 | E2E and component testing |

---

## Prerequisites

- Node.js `>=20`
- The API server running on `http://localhost:9096` (see `api/` in the repo root)

---

## Getting started

```bash
npm install
npm start          # serves on http://localhost:4200
```

---

## Scripts

| Command | What it does |
|---------|-------------|
| `npm start` | Dev server on `localhost:4200` with live reload |
| `npm run build` | Production build to `dist/` |
| `npm test` | Unit tests |
| `npm run cy:open` | Open Cypress in interactive mode |
| `npm run cy:run` | Run all E2E tests headlessly |

---

## Environment

| Variable | Value |
|----------|-------|
| `apiBaseUrl` | `http://localhost:9096` |

Change this in `src/environments/environment.prod.ts` before deploying.

---

## Testing

See [TESTING.md](./TESTING.md).
