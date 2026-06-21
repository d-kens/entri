# Entri

An event discovery and management platform. Users can browse, search, and register for events. Organizers can create and manage events through an authenticated dashboard.

## Stack

| Layer | Technology |
|---|---|
| Frontend | Angular 21, Angular Material, Tailwind CSS |
| Backend | Spring Boot 4, Java 21 |
| Database | MySQL 8 |
| Migrations | Liquibase |
| Auth | JWT (access + refresh tokens) |
| Storage | Firebase Storage |
| Notifications | Novu |
| Reverse proxy | Nginx |
| Container updates | Watchtower |
| CI/CD | GitHub Actions → GHCR |
| Dependency updates | Renovate |

## Getting Started

### Prerequisites

- Docker and Docker Compose
- Java 21
- Node.js 20+

### Running locally

Copy the environment variables template and fill in the values:

```bash
cp .env.example .env
```

Start the services:

```bash
docker compose up -d
```

The API will be available at `http://localhost:8080` and the web app at `http://localhost:80`.

### Running the API in development

```bash
cd api
./gradlew bootRun
```

### Running the web in development

```bash
cd web
npm install
npm start
```

## CI/CD

Pushing to `master`:

- Changes in `api/` → runs tests then builds and pushes `ghcr.io/d-kens/entri-api`
- Changes in `web/` → builds and pushes `ghcr.io/d-kens/entri-web`

Watchtower polls GHCR every 5 minutes and automatically pulls updated images on the server.

Renovate runs every Saturday at 8am and opens PRs for outdated dependencies.
