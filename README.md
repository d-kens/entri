# Entri

Event management platform: an Angular frontend and a Spring Boot API.

## Structure

| Path | What it is |
|---|---|
| [`api/`](./api) | Spring Boot backend (Java 21, MySQL, RabbitMQ). See [api/README.md](./api/README.md). |
| [`web/`](./web) | Angular frontend. See [web/README.md](./web/README.md). |
| `rabbitmq/` | Queue/exchange definitions loaded into the local RabbitMQ container. |

## Local development

The API and web app run independently for local dev — see each subproject's
README for `./gradlew bootRun` / `npm start` instructions. `docker-compose.yml`
in this repo only brings up local dependencies (RabbitMQ, MySQL); there is no
production deployment stack in this repo.

## Releases

Each app is versioned independently via the Gradle Release Plugin (`api`) and
`standard-version` (`web`), producing `api-vX.Y.Z` / `web-vX.Y.Z` tags and a
`web/CHANGELOG.md`. There is currently no automated CI or deploy pipeline in
this repo — a prior GCP-based deployment setup (Terraform, nginx, Prometheus/
Grafana, GitHub Actions workflows) was removed to keep the project lean. The
web app is deployed to Vercel (`web/vercel.json`); the API has no deployment
target configured here.
