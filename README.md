<div align="center">

# 🛒 ZIO Avito Desk

**A production-shaped classifieds board — a purely-functional Scala 3 / ZIO 2 backend with a React 19 frontend.**

[![Scala](https://img.shields.io/badge/Scala-3.3.8_LTS-DC322F?logo=scala&logoColor=white)](https://www.scala-lang.org/)
[![ZIO](https://img.shields.io/badge/ZIO-2.1.26-7B68EE)](https://zio.dev/)
[![zio-http](https://img.shields.io/badge/zio--http-3.11.2-1E90FF)](https://github.com/zio/zio-http)
[![Quill](https://img.shields.io/badge/Quill_ProtoQuill-4.8.6-2E8B57)](https://github.com/zio/zio-protoquill)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=white)](https://react.dev/)
[![Tests](https://img.shields.io/badge/tests-33%2F33_passing-brightgreen)](#-testing)
[![License](https://img.shields.io/badge/license-MIT-blue)](#-license)

</div>

---

ZIO Avito Desk is a full-stack marketplace / classifieds board ("доска объявлений"). The backend is a
**layered, purely-functional ZIO 2 application on Scala 3**; the frontend is a **React 19 + TypeScript**
single-page app. It's built the way a real service would be: clear module boundaries, dependency
injection via `ZLayer`, a typed effect channel, compile-time-checked SQL, versioned database
migrations, session-cookie authentication, and a test suite spanning services, HTTP routes, and the
database.

## 📸 Screenshots

|  |  |
|:--:|:--:|
| **Browse the board** | **Post a new ad** |
| ![Board](docs/screenshots/01-board.png) | ![Create](docs/screenshots/02-create.png) |
| **Item detail** | **Mobile** |
| ![Detail](docs/screenshots/03-detail.png) | <img src="docs/screenshots/04-mobile.png" width="220" /> |

## ✨ Features

- 📋 **Browse** listings with **filter** (category, price range, location), **sort** (newest / price),
  and **pagination**
- 🔍 **Search** listings by name/description (combines with the filters)
- 🪟 **Item detail** in a polished modal
- ➕ **Create** and ✏️ **edit** listings, with **image upload** (stored server-side)
- 🗑️ **Delete** your own listings (with confirm)
- 🔐 **Accounts** — register / log in / log out with secure session cookies
- 👤 **Ownership** — you can only edit/delete your own listings; a **"My listings"** view
- 🏷️ **Categories** with a browse rail and seeded demo data
- ❤️ **Operability** — `/health` endpoint, CORS, request logging, typed errors → correct HTTP status

## 🧰 Tech stack

| Layer | Technology | Version | Why |
|---|---|---|---|
| Language | **Scala 3** | 3.3.8 (LTS) | Modern syntax, `derives`, long-term support |
| Effects | **ZIO 2** | 2.1.26 | Typed, composable, testable concurrency |
| HTTP | **ZIO HTTP** | 3.11.2 | Declarative routing, middleware, `HandlerAspect` auth |
| JSON | **zio-json** | 0.9.1 | Fast, derivation-based codecs (`derives`) |
| Persistence | **Quill (ProtoQuill)** | 4.8.6 | Compile-time-checked SQL, ZIO-native |
| Database | **PostgreSQL** | 16 | Real RDBMS with constraints & indexes |
| Migrations | **Flyway** | 11.1 | Versioned, repeatable schema migrations |
| Auth | **bcrypt** (favre) | 0.10.2 | Password hashing |
| Testing | **zio-test** + **Testcontainers** | 2.1.26 / 0.41 | Service, route, and real-DB integration tests |
| Build | **sbt** | 1.12.5 | Multi-module build |
| Frontend | **React + TypeScript** | 19 / CRA | SPA with `fetch` + a dev proxy |

## 🏗️ Architecture

The backend is a **strict layered onion** — a multi-module sbt build whose dependencies point in **one
direction only**.

```mermaid
flowchart LR
    domain[/"domain<br/><i>models · codecs · errors</i>"/]
    storage["storage<br/><i>repos · Quill · Postgres</i>"]
    service["service<br/><i>business logic · auth</i>"]
    route["route<br/><i>zio-http routes · middleware</i>"]
    server["server<br/><i>Main · Flyway · wiring</i>"]
    domain --> storage --> service --> route --> server
    classDef d fill:#eef2ff,stroke:#6366f1,color:#1e1b4b;
    class domain,storage,service,route,server d;
```

Each component is a `trait` + a `case class` implementation that exposes a `ZLayer` in its companion.
All layers are assembled **once**, in `Main`. Every effect is typed as `Task[A]`; typed `AppError`s in
the error channel are mapped centrally to HTTP status codes.

### Modules

| Module | Responsibility |
|---|---|
| `domain` | Pure models (`Item`, `Category`, `User`, `ItemFilter`, `Page`) + codecs + typed `AppError`. |
| `storage` | `ItemRepo`/`CategoryRepo`/`UserRepo`/`SessionRepo` traits and Quill ProtoQuill impls over Postgres. |
| `service` | `ItemService`/`CategoryService`/`AuthService` (bcrypt, sessions) business logic. |
| `route` | `zio-http` routes, validation, error mapping (`ApiError`), CORS, the auth `HandlerAspect`, uploads. |
| `server` | `Main` — composition root; runs Flyway, provides the shared `DataSource` and all layers. |

## 🚀 Getting started

### Prerequisites

- **JDK 17+**, **sbt 1.12.x**
- **Docker** (for PostgreSQL via docker-compose)
- **Node.js 18+** and npm

### 1. Start PostgreSQL

```bash
docker compose up -d        # starts postgres:16 on localhost:5432 (db/user/pass: avito)
```

> If port 5432 is taken, run with an override: `POSTGRES_PORT=55432 docker compose up -d`
> and set `DB_URL=jdbc:postgresql://localhost:55432/avito` when starting the backend.

### 2. Run the backend

```bash
sbt run        # runs Flyway migrations, then serves http://localhost:8080
```

On first run Flyway creates the schema and seeds categories, demo listings, and a **demo user**
(`demo@avito.example` / `password`). Configuration is read from environment variables (with sensible
localhost defaults):

| Env var | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/avito` | JDBC URL |
| `DB_USER` / `DB_PASSWORD` | `avito` / `avito` | DB credentials |
| `APP_PORT` | `8080` | HTTP port |
| `CORS_ORIGIN` | `http://localhost:3000` | Allowed browser origin (`*` to allow any) |
| `UPLOAD_DIR` | `./uploads` | Where uploaded images are stored |
| `COOKIE_SECURE` | `false` | Set `true` to mark the session cookie `Secure` (HTTPS) |

### 3. Run the frontend

```bash
cd frontend
npm install
npm start      # http://localhost:3000 (proxies API calls to :8080)
```

## 🔌 API reference

Errors are returned as JSON `{ "error": "..." }` with the appropriate status (`400` validation,
`401` unauthenticated, `403` not owner, `404` missing, `409` conflict).

| Method | Path | Auth | Description |
|---|---|:--:|---|
| `GET` | `/health` | — | Liveness check |
| `GET` | `/items` | — | List items — query params `categoryId`, `q`, `minPrice`, `maxPrice`, `location`, `sort` (`newest`\|`price_asc`\|`price_desc`), `limit`, `offset`; returns a `Page` |
| `GET` | `/items/{id}` | — | Get an item (`404` if missing) |
| `GET` | `/items/search/{query}` | — | Search items (legacy convenience) |
| `GET` | `/items/category/{categoryId}` | — | Items in a category |
| `POST` | `/items` | ✅ | Create an item (owned by you) |
| `PUT` | `/items/{id}` | ✅ owner | Update an item |
| `POST` | `/items/{id}/image` | ✅ owner | Upload an image (raw body, `Content-Type: image/*`, ≤ 5 MB) |
| `DELETE` | `/items/{id}` | ✅ owner | Delete an item |
| `GET` | `/uploads/{file}` | — | Serve an uploaded image |
| `GET` | `/categories` `/categories/{id}` | — | List / get categories |
| `POST` `PUT` `DELETE` | `/categories` … | ✅ | Manage categories |
| `POST` | `/auth/register` | — | Register `{email, password, displayName}` → sets session cookie |
| `POST` | `/auth/login` | — | Log in `{email, password}` → sets session cookie |
| `POST` | `/auth/logout` | — | Log out (clears session) |
| `GET` | `/auth/me` | ✅ | Current user |

### Examples

```bash
# Log in (stores the session cookie in cookies.txt)
curl -c cookies.txt -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@avito.example","password":"password"}'

# Create an item (authenticated)
curl -b cookies.txt -X POST http://localhost:8080/items \
  -H 'Content-Type: application/json' \
  -d '{"name":"Road Bike","description":"Lightweight frame","price":499.00,
       "categoryId":"33333333-3333-3333-3333-333333333333","location":"Berlin"}'

# Filter + sort + paginate
curl 'http://localhost:8080/items?minPrice=100&maxPrice=500&sort=price_asc&limit=10'

# Upload an image for an item you own
curl -b cookies.txt -X POST http://localhost:8080/items/<id>/image \
  -H 'Content-Type: image/png' --data-binary @photo.png
```

## 🧪 Testing

```bash
sbt test        # 33 tests: service (in-memory) + route (HTTP) + storage (Testcontainers Postgres)
```

- **Service specs** run each service against a `Ref`-backed in-memory repository — fast, no DB.
- **Route specs** drive the real `zio-http` routes (validation → `400`, missing → `404`,
  ownership → `403`, pagination, health).
- **Repo specs** run the Quill repositories against a throwaway **PostgreSQL Testcontainer**
  (requires Docker).

Frontend: `cd frontend && npm test`.

## 🔧 CI

`.github/workflows/ci.yml` installs JDK 17 + sbt (via `sbt/setup-sbt`), spins up a PostgreSQL service
container, then runs `scalafmtCheckAll` → `compile` → `test` → `stage`.

## 📂 Project structure

```
zio-avito-desk/
├── domain/      # models, JSON codecs, typed errors
├── storage/     # repositories (Quill + Postgres)
├── service/     # business logic + auth (+ tests)
├── route/       # zio-http routes, middleware, uploads (+ tests)
├── server/      # Main + application.conf + Flyway migrations
├── frontend/    # React 19 + TypeScript SPA
├── docker-compose.yml  # local PostgreSQL
└── docs/        # screenshots
```

## 📄 License

Released under the [MIT License](LICENSE). Free to use as a reference or starting point.
