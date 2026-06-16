# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

ZIO Avito Desk is a classifieds-board demo: a ZIO 2 / Scala 3.3.8 (LTS) backend (multi-module sbt build) plus a React 19 + TypeScript frontend in `frontend/`. The Scala package is `com.example.zivito` throughout.

Backend stack: Scala 3.3.8, sbt 1.12.5, zio/zio-streams 2.1.26, zio-http 3.11.2, zio-json 0.9.1, Quill ProtoQuill (quill-jdbc-zio 4.8.6) over **PostgreSQL 16**, **Flyway 11** migrations, **bcrypt** (favre 0.10.2) for password hashing, zio-test 2.1.26 + **Testcontainers** (test scope in `service`, `route`, and `storage`).

## Commands

Backend (run from repo root). **PostgreSQL must be running** for `sbt run` and for the storage/route
Testcontainers specs (`docker compose up -d` starts it on :5432):

```bash
docker compose up -d     # start PostgreSQL 16 (db/user/pass: avito). POSTGRES_PORT overrides host port.
sbt compile              # compile all modules (Scala 3, -Xfatal-warnings on)
sbt test                 # all specs: service (in-memory) + route (HTTP) + storage (Testcontainers; needs Docker)
sbt run                  # runs Flyway migrations, then serves on :8080 (entry point: server/.../Main.scala)
sbt scalafmtAll          # format all Scala sources (.scalafmt.conf, scala3 dialect, maxColumn=160)
sbt "project service" test                               # run tests for a single module
sbt "testOnly com.example.zivito.ItemServiceSpec"        # run one spec
sbt "testOnly *ItemServiceSpec -- -t \"test name\""      # run a single test case
```

The backend reads env vars (defaults in parens): `DB_URL` (`jdbc:postgresql://localhost:5432/avito`),
`DB_USER`/`DB_PASSWORD` (`avito`), `APP_PORT` (`8080`), `CORS_ORIGIN` (`http://localhost:3000`),
`UPLOAD_DIR` (`./uploads`), `COOKIE_SECURE` (`false`).

Frontend (run from `frontend/`):

```bash
npm install
npm start                # dev server on http://localhost:3000
npm test                 # react-scripts (jest) test runner
npm run build
```

## Architecture

The backend is a strict layered onion. sbt modules depend in one direction only — **never** add a reverse dependency:

```
domain  ←  storage  ←  service  ←  route  ←  server
```

- **domain** — pure model & contracts only. `Domain` (Item, Category, User, `ItemFilter`, `Page`, `SortOrder`) with `zio-json` codecs via `derives`, and `AppError` (sealed, in `Error.scala`). No ZIO effects beyond types. Has no module dependencies.
- **storage** — repository traits + impls using Quill ProtoQuill's `PostgresZioJdbcContext(Escape)`: `ItemRepo`, `CategoryRepo`, `UserRepo`, `SessionRepo`. `ItemRepo.list(ItemFilter)` builds the filtered/sorted/paginated query.
- **service** — business logic as `XxxService` trait + `XxxServiceImpl` case class: `ItemService`, `CategoryService`, `AuthService` (register/login/logout/authenticate with bcrypt + sessions).
- **route** — `zio-http` 3.x declarative routes, one object per resource (`ItemRoutes`, `CategoryRoutes`, `AuthRoutes`, `HealthRoutes`). Cross-cutting helpers: `ApiError` (typed error → HTTP status JSON), `Validate` (field validators), `Uploads` (raw-body image storage + static serving), `AuthMiddleware` (cookie-auth `HandlerAspect` providing `User`). Item/Category routes are split into `publicRoutes` (GET) and `protectedRoutes` (mutations, guarded by the auth aspect; item edit/delete also check ownership). Path codecs: `uuid("id")`, `string("query")`; static segments ordered before `uuid("id")`.
- **server** — composition root. `Main` runs `DbMigration.migrate` (Flyway), applies CORS + request-logging middleware + central `handleErrorCauseZIO(ApiError.handle)`, and provides all `ZLayer`s.

### Dependency injection conventions

- Each component is a `case class` implementing a `trait`, exposing a `def layer: ZLayer[...]` in its companion (e.g. `ItemServiceImpl.layer = ZLayer.fromFunction(ItemServiceImpl(_))`).
- Routes access services via `ZIO.serviceWithZIO[ItemService](_.method)`.
- The Quill data source is provided **once** in `Main` via `Quill.DataSource.fromPrefix("App")` (single shared pool). Repo `layer`s are `ZLayer.fromFunction(XxxRepoPersist(_)): ZLayer[DataSource, Nothing, XxxRepo]` (no per-repo `fromPrefix`); each repo call ends with `.provide(ZLayer.succeed(ds))`.
- Item **ids are generated in the route layer** (`Random.nextUUID`), consistent with categories — the repo inserts the item verbatim (no placeholder-id overwrite).
- All effects are typed as `Task[A]` (error channel = `Throwable`); services/routes fail with the typed `AppError` subtypes, which `ApiError.toResponse` maps to HTTP status codes. Keep this convention.
- Layer wiring is assembled only in `Main.run`'s `.provide(...)`. Add new layers there.

### Config & persistence

- Runtime config lives in `server/src/main/resources/application.conf` under the `App` key (HikariCP `jdbcUrl`/`username`/`password`, env-overridable via `${?DB_URL}` etc.). Quill reads it via `Quill.DataSource.fromPrefix("App")` (Typesafe config) — no `zio-config` dependency.
- Schema is managed by **Flyway** migrations in `server/src/main/resources/db/migration/` (`V1__init.sql` … `V4__users_and_sessions.sql`), run at startup by `DbMigration.migrate` before the server binds. Add a new `V{n}__*.sql` rather than editing existing migrations.
- Identifiers are quoted to match Quill's `Escape` strategy (PascalCase tables `"ItemTable"`, camelCase columns `"categoryId"`). Postgres folds unquoted identifiers to lowercase, so migrations **must** keep them quoted.
- Uploaded images are written to `UPLOAD_DIR` (default `./uploads`) and served at `GET /uploads/{file}`; `item.imageUrl` holds either an external URL or a `/uploads/...` path.

## Gotchas

- The real entry point is `Main` (`server/.../Main.scala`); it runs Flyway, then serves `httpApp` (routes `@@ Middleware.cors @@ Middleware.requestLogging`, errors via `handleErrorCauseZIO(ApiError.handle)`), with all layers provided once.
- CI (`.github/workflows/ci.yml`) **must install sbt** via `sbt/setup-sbt@v1` (GitHub's `ubuntu-latest` no longer preinstalls sbt — omitting it caused `sbt: command not found` / exit 127). CI also runs a `postgres:16` service container, then `scalafmtCheckAll` → `compile` → `test` → `stage` on JDK 17.
- **Dynamic Quill queries**: `ItemRepoPersist.list` composes optional filters as runtime `Quoted` values (`opt.fold(base)(v => quote(base.filter(...)))`) — ProtoQuill does **not** support `filterOpt` inside an inline `quote`. Sort branches pick a `sortBy(...)(Ord.asc|desc)`; paginate with `drop/take`.
- Timestamp comparisons in Quill (`s.expiresAt > lift(now)`) need `import io.getquill.extras.InstantOps`.
- CORS config type is the nested `Middleware.CorsConfig` (not a top-level `CorsConfig`); `allowCredentials = Header.AccessControlAllowCredentials.Allow` for cookie auth.
- Image upload (`POST /items/{id}/image`) takes the **raw request body** (not multipart); the `Content-Type` selects the extension.
- Auth uses a `HandlerAspect` (`AuthMiddleware.authAspect`) that resolves the `session` cookie to a `User`; protected handlers read it via `ZIO.service[User]`.
- `.gitignore` covers all `target/` dirs and `*.mv.db`/`*.trace.db`; build artifacts are no longer tracked. Don't edit anything under a `target/` directory.
- `scalacOptions` (project/Settings.scala) is an explicit Scala-3 set: `-deprecation -feature -unchecked -encoding utf-8 -Wunused:all -Xfatal-warnings`. `-Xfatal-warnings` is on — **unused imports** (`-Wunused:all`) / deprecations break the build. sbt-tpolecat was dropped in favor of these explicit flags.
- ProtoQuill needs `import io.getquill.*` (for `query`/`lift`/`insertValue`/`updateValue`/`delete`) **plus** `import ctx.*` (for `run`). The Scala-2 single `import ctx._` is not enough.
- In routes, `zio.http.uuid` (path codec) collides with `zio.json.uuid` — exclude it via `import zio.json.{uuid as _, *}`.
- zio-http 3.x pulls zio-schema-json needing zio-json 0.9.x; `build.sbt` sets `libraryDependencySchemes += "dev.zio" %% "zio-json" % VersionScheme.Always` so Quill's older zio-json constraint doesn't trip the eviction error.
