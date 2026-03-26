# Oro - Delivery & Payment Management System

Oro is a monorepo containing a modern web application designed for delivery tracking and payment management. It consists of a Java Spring Boot backend and an Angular frontend.

## 🏗 Project Structure

```text
oro/
├── api/          # Java Spring Boot backend
└── web/          # Angular frontend
```

---

## 🚀 Backend (`api/`)

The backend is built with **Spring Boot 4.x** and **Java 21**. It uses **MySQL** for data storage and **Liquibase** for database migrations.

### 🛠 Tech Stack
- **Framework:** Spring Boot 4.0.1
- **Language:** Java 21
- **Build Tool:** Gradle
- **Database:** MySQL 8.0
- **Migration:** Liquibase
- **Security:** Spring Security with JWT
- **Integrations:** 
  - **Mpesa:** Payment gateway integration (STK Push)
  - **Novu:** Notification service
  - **OpenFeign:** Declarative REST client

### ⚙️ Prerequisites
- **Java 21** or higher
- **Docker** (for MySQL)

### 🏃 Setup & Run
1.  **Navigate to the api directory:**
    ```bash
    cd api
    ```
2.  **Start the database (via Docker Compose):**
    ```bash
    docker-compose up -d
    ```
3.  **Run the application:**
    ```bash
    ./gradlew bootRun
    ```
    The API will be available at `http://localhost:8080`.

### 🔐 Environment Variables
The following environment variables are required (either in your environment or defined in a custom `application-local.yaml`):

| Variable | Description |
| :--- | :--- |
| `JWT_SECRET` | Secret key for JWT signing |
| `NOVU_SECRET` | Secret key for Novu notification service |
| `MPESA_CONSUMER_KEY` | Mpesa App Consumer Key |
| `MPESA_CONSUMER_SECRET` | Mpesa App Consumer Secret |
| `MPESA_SHORTCODE` | Mpesa Business Shortcode |
| `MPESA_PASSKEY` | Mpesa Online Passkey |
| `MPESA_CALLBACK_URL` | Mpesa STK Push callback URL |
| `APP_BASE_URL` | Base URL of the application (e.g., `http://localhost:4200`) |

### 🧪 Tests
Run JUnit tests:
```bash
./gradlew test
```

---

## 🎨 Frontend (`web/`)

The frontend is a modern **Angular 21** application using **Tailwind CSS** for styling and **Material Design** components.

### 🛠 Tech Stack
- **Framework:** Angular 21
- **Styling:** Tailwind CSS 4, Angular Material
- **Package Manager:** npm
- **Testing:** Vitest

### ⚙️ Prerequisites
- **Node.js** (v20+ recommended)
- **npm** (v11+)

### 🏃 Setup & Run
1.  **Navigate to the web directory:**
    ```bash
    cd web
    ```
2.  **Install dependencies:**
    ```bash
    npm install
    ```
3.  **Run the development server:**
    ```bash
    npm start
    ```
    The application will be available at `http://localhost:4200`.

### 🧪 Tests
Run Vitest:
```bash
npm test
```

