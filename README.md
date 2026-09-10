# Entri

Entri is an event ticketing platform. Organizers create events and sell tickets; attendees browse events, buy tickets, and check in at the door. Payments and organizer payouts run through an in-app wallet backed by IntaSend.

This is a personal practice project used to work through Angular and Spring Boot patterns — not a production CarePay product.

## Stack

| Layer    | Tech                                                        |
| -------- | ------------------------------------------------------------ |
| Frontend | Angular (`web/`)                                              |
| Backend  | Spring Boot / Kotlin DSL Gradle build, Java 21 (`api/`)       |
| Database | MySQL 8.4, schema managed by Liquibase                        |
| Messaging| RabbitMQ                                                       |
| Payments | IntaSend                                                        |
| Notifications | Novu                                                       |

## Project layout

```
api/    Spring Boot API (events, tickets, checkout, payments, wallet, users, auth, notifications)
web/    Angular frontend
rabbitmq/  RabbitMQ definitions used by docker-compose
```

## Prerequisites

- Node.js + npm (see `web/package.json` for Angular CLI version)
- Java 21
- Docker (for RabbitMQ and MySQL)

## Running locally

1. **Start infrastructure**

   ```bash
   docker compose up -d
   ```

   This starts RabbitMQ (management UI on `:15672`) and MySQL 8.4. Requires a `.env` file at the repo root with `RABBITMQ_USERNAME` and `RABBITMQ_PASSWORD` set.

2. **Start the API**

   ```bash
   cd api
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   ```

   Runs on `http://localhost:9096`, context path `/api`. The `dev` profile provides local defaults for most settings, but a few values (JWT secret, Novu key, IntaSend keys, Firebase service account) must be supplied as environment variables — see `api/src/main/resources/application.yaml` for the full list of expected variables. Do not commit real secret values; use a local `.env` or exported shell variables.

3. **Start the web app**

   ```bash
   cd web
   npm install
   npm start
   ```

   Runs on `http://localhost:4200`.

## Useful commands

**API** (from `api/`)

```bash
./gradlew build        # compile + test
./gradlew test          # run tests
```

**Web** (from `web/`)

```bash
npm test                 # unit tests
npm run build             # production build
npm run format             # prettier --write
```

## Notes

- The API's Firebase service account file and any `.env` files are gitignored — obtain them from whoever manages the project's secrets rather than committing new ones.
- `docker-compose.yml` pins the RabbitMQ container hostname so its data volume survives recreates; see the inline comment there before changing it.
