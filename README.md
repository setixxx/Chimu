# Chimu

Chimu is a comprehensive platform for organizing, conducting, and managing Game Jam events. The project is implemented as a monorepository that includes both the server-side application (Backend) and a cross-platform client application (Client).

---

## Technology Stack

### Backend (`/api`)

The server-side application is built with Kotlin using the Spring ecosystem.

- **Language:** Kotlin  
- **Framework:** Spring Boot 3  
- **Database:** PostgreSQL  
- **Database Migrations:** Flyway  
- **Security:** Spring Security + JWT (JSON Web Tokens)  
- **Build Tool:** Gradle (Kotlin DSL)  
- **Containerization:** Docker Compose  

### Client (`/cli`)

The client application is built using Kotlin Multiplatform (KMP) and Compose Multiplatform for UI. It supports mobile, desktop, and web platforms.

- **Language:** Kotlin Multiplatform  
- **UI Framework:** Compose Multiplatform  
- **Target Platforms:** Android, iOS, Desktop (JVM), Web (Wasm/JS)  
- **Networking:** Ktor Client  
- **Dependency Injection:** Koin  
- **Architecture:** MVVM  

---

## Features

The application provides the following functionality:

- **Authentication & Users:** User registration, login, profile management, and developer specializations.
- **Game Jam Management:** Creation and editing of game jams, configuration of deadlines, descriptions, and evaluation criteria.
- **Teams:** Team creation, inviting participants via tokens, and team member management.
- **Registration:** Team registration for specific game jams.
- **Projects:** Project submission, including descriptions, links, and file uploads.
- **Judging:** Project evaluation system for judges based on predefined criteria.
- **Leaderboards:** Automatic score calculation and leaderboard generation.

---

## Project Structure

```
/api — Backend source code (Spring Boot)
/cli — Client source code (Compose Multiplatform)
```

---

## Setup and Installation

### Prerequisites

- JDK 17 or higher  
- Docker and Docker Compose (for database and environment setup)  
- Android Studio (for Android) or Xcode (for iOS)  

---

## Backend Setup (API)

### 1. Environment Variables

Before running the application, create a `.env` file in the root directory of `api` or configure environment variables in your container:

```
DB_URL=jdbc:postgresql://localhost:5432/chimu
DB_USER=postgres
DB_PASSWORD=postgres

JWT_SECRET=<base64_secret>
JWT_ACCESS_TOKEN_EXPIRATION=900000
JWT_REFRESH_TOKEN_EXPIRATION=604800000
```

To generate a secure `JWT_SECRET`, run:

```
openssl rand -base64 64
```

### 2. Run the Application

Navigate to the `api` directory:

```
cd api
```

Start required services (database) using Docker Compose:

```
docker-compose up -d
```

Run the application:

```
./gradlew bootRun
```

The server will be available at:

```
http://localhost:8080
```

---

## Client Setup (Client)

Navigate to the `cli` directory:

```
cd cli
```

### Run for Different Platforms

**Desktop:**

```
./gradlew :composeApp:run
```

**Android:**

Open the project in Android Studio and run the `composeApp` configuration.

**Web (Wasm):**

```
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

**iOS:**

Open `cli/iosApp/iosApp.xcodeproj` in Xcode.

---

## API

Main endpoints for interacting with the server:

```
POST   /api/auth/register   — Register a new user
POST   /api/auth            — Login (obtain tokens)
POST   /api/auth/refresh    — Refresh access token
POST   /api/auth/logout     — Logout
GET    /api/users/me        — Get current user information
```

Detailed API documentation is available in the project [Wiki](https://github.com/setixxx/Chimu/wiki).

---

## License

This project is distributed under the GNU GPL v3.0 license.  
See the `LICENSE` file for details.
