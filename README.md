# Entri

Event management platform: an Angular frontend, a Spring Boot API, and the
infrastructure that runs both in production.

## Structure

| Path | What it is |
|---|---|
| [`api/`](./api) | Spring Boot backend (Java 21, MySQL, RabbitMQ). See [api/README.md](./api/README.md). |
| [`web/`](./web) | Angular frontend. See [web/README.md](./web/README.md). |
| `infra/terraform/` | Terraform for the GCP infrastructure (per environment, under `environments/`). |
| `nginx/` | Reverse proxy config — routes `/`, `/api`, `/grafana` to the right service. |
| `monitoring/` | Prometheus scrape config and Grafana provisioning (dashboards, datasources). |
| `rabbitmq/` | Queue/exchange definitions loaded into the RabbitMQ container. |
| `scripts/` | One-off ops scripts (e.g. `create-secrets.sh` to populate GCP Secret Manager). |

## Local development

The API and web app run independently for local dev — see each subproject's
README for `./gradlew bootRun` / `npm start` instructions. `docker-compose.yml`
in this repo describes the **production** stack (api, web, rabbitmq,
prometheus, grafana, nginx) and isn't meant for day-to-day local dev.

## Deployment

Each piece deploys independently via its own GitHub Actions workflow in
`.github/workflows/`:

| Workflow | Deploys |
|---|---|
| `ci-api.yml` / `ci-web.yml` | Build, test, and release `api`/`web` on push to `master` (tags `api-vX.Y.Z` / `web-vX.Y.Z`) |
| `deploy.yml` | Pulls a released image tag and restarts the `api` or `web` service on the VM (`deploy.sh`) |
| `deploy-nginx.yml` | Applies `nginx/` config to the VM (`deploy-nginx.sh`) |
| `deploy-rabbitmq.yml` | Applies `rabbitmq/definitions.json` (`deploy-rabbitmq.sh`) |
| `deploy-monitoring.yml` | Applies `monitoring/` config and restarts Prometheus/Grafana (`deploy-monitoring.sh`) |

Secrets (DB credentials, JWT secret, Firebase service account, Novu, IntaSend)
live in GCP Secret Manager and are pulled onto the VM by `deploy.sh` on every
run — see `scripts/create-secrets.sh` for how they're populated.
