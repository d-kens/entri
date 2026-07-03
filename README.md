## What is this?

A full-stack event discovery and management platform. Attendees browse and register for events; organizers get an authenticated dashboard to create and run them.

---

## Architecture

```
entri/
├── api/     → Spring Boot 4 · Java 21 · MySQL 8 · Liquibase
└── web/     → Angular 21 · Angular Material · Tailwind CSS 4
```

**Auth** — JWT (access + refresh tokens)  
**Storage** — Firebase Storage  
**Notifications** — Novu  
**Infra** — Nginx · Docker · Watchtower · GitHub Actions → GHCR

---

## Running locally

**Full stack (Docker)**

```bash
cp .env.example .env   # fill in the values
docker compose up -d
```

| Service | URL |
|---|---|
| API | http://localhost:8080 |
| Web | http://localhost:80 |

**API only (dev)**

```bash
cd api && ./gradlew bootRun
```

**Web only (dev)**

```bash
cd web && npm install && npm start
# → http://localhost:4200
```

---

## CI/CD

Every push to `master`:

- `api/**` changed → tests run → `ghcr.io/d-kens/entri-api` built and pushed
- `web/**` changed → `ghcr.io/d-kens/entri-web` built and pushed

Watchtower polls GHCR every 5 minutes and hot-swaps containers on the server automatically. Renovate opens dependency PRs every Saturday at 8am.
