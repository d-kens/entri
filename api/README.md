# Entri API

Backend API powering the Entri event management platform. Built with Spring Boot 4, Java 21, and MySQL.

## Prerequisites

- Java 21
- MySQL
- Gradle (or use the included `./gradlew` wrapper)

## Setup

The app reads configuration from environment variables. Defaults for local development are already set in `src/main/resources/application-dev.yaml`. Override any of them as needed:

| Variable | Description |
|---|---|
| `SPRING_DATASOURCE_USERNAME` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | DB password |
| `JWT_SECRET` | JWT signing secret |
| `APP_BASE_URL` | Frontend base URL |
| `APP_CORS_ALLOWED_ORIGIN` | Allowed CORS origin |

## Commands

### Run

```bash
./gradlew bootRun
```

Starts the server on `http://localhost:9096`.

### Test

```bash
# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests "com.entri.modules.service.EventServiceTest"

# Run a specific test method
./gradlew test --tests "com.entri.modules.service.EventServiceTest.savesEventWithCorrectFields"
```

### Build

```bash
./gradlew clean bootJar
```

Output jar: `build/libs/*.jar`

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4, Spring Security |
| Language | Java 21 |
| Database | MySQL + Liquibase |
| Auth | JWT (jjwt) |
| Notifications | Novu, Firebase Admin |
| Build | Gradle 8 |
