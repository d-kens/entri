# Oro

A rental management platform.

## Stack

| Layer | Technology |
|---|---|
| Frontend | Angular 21, Angular Material |
| Backend | Spring Boot 4, Java 21 |
| Database | MySQL 8 |
| Migrations | Liquibase |
| Auth | JWT (access + refresh tokens) |
| Storage | Firebase Storage |
| Notifications | Novu |
| Reverse proxy | Nginx |
| Container updates | Watchtower |
| CI/CD | GitHub Actions → GHCR |

## Project Structure

```
oro/
├── api/                  # Spring Boot API
├── web/                  # Angular frontend
├── nginx/
│   └── nginx.conf        # EC2-level reverse proxy config
├── docker-compose.yml
└── .env.example
```

## Local Development

**Prerequisites:** Java 21, Node 20, Docker

**API**
```bash
cd api
./gradlew bootRun
# runs on http://localhost:9096
```

**Web**
```bash
cd web
npm install
npm start
# runs on http://localhost:4200
```

**Database only**
```bash
docker compose up db
```

## Environment Variables

Copy `.env.example` to `.env` and fill in the values:

```bash
cp .env.example .env
```

| Variable | Description |
|---|---|
| `MYSQL_USER` | Database user |
| `MYSQL_PASSWORD` | Database password |
| `MYSQL_ROOT_PASSWORD` | Database root password |
| `JWT_SECRET` | Random secret — `openssl rand -hex 64` |
| `APP_BASE_URL` | Public URL of the app e.g. `https://oro.co.ke` |
| `APP_CORS_ALLOWED_ORIGIN` | Allowed CORS origin e.g. `https://oro.co.ke` |
| `FIREBASE_SERVICE_ACCOUNT_PATH` | Path to Firebase service account JSON inside the API container |
| `FIREBASE_STORAGE_BUCKET` | Firebase storage bucket name |
| `NOVU_SECRET` | Novu API key |
| `GHCR_USERNAME` | GitHub username for Watchtower to pull images |
| `GHCR_TOKEN` | GitHub PAT with `read:packages` scope |

## Deployment (EC2)

### 1. Install prerequisites

```bash
sudo apt update && sudo apt install -y docker.io docker-compose-v2 nginx certbot
sudo systemctl enable --now docker
```

### 2. Get an SSL certificate

```bash
sudo certbot certonly --webroot -w /var/www/certbot -d oro.co.ke
```

### 3. Configure Nginx

```bash
sudo cp nginx/nginx.conf /etc/nginx/nginx.conf
sudo nginx -t && sudo systemctl reload nginx
```

### 4. Set environment variables

```bash
cp .env.example .env
nano .env   # fill in all values
```

### 5. Start containers

```bash
docker compose up -d
```

### Subsequent deploys

Handled automatically — Watchtower polls GHCR every 5 minutes and restarts any container whose image has been updated.

## CI/CD

Merging to `master` triggers a GitHub Actions workflow that:

1. Builds the `api` and `web` Docker images in parallel
2. Pushes them to GHCR as:
   - `ghcr.io/d-kens/oro-api:latest`
   - `ghcr.io/d-kens/oro-web:latest`
3. Also tags each image with the commit SHA for traceability

No secrets need to be configured — the workflow uses the built-in `GITHUB_TOKEN`.
