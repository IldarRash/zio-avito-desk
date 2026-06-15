# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

ZIO Avito Desk is a classifieds-board demo: a ZIO 2 / Scala 3.3.8 (LTS) backend (multi-module sbt build) plus a React 19 + TypeScript frontend in `frontend/`. The Scala package is `com.example.zivito` throughout.

Backend stack: Scala 3.3.8, sbt 1.12.5, zio/zio-streams 2.1.26, zio-http 3.11.2, zio-json 0.9.1, Quill ProtoQuill (quill-jdbc-zio 4.8.6), H2 2.4.240, zio-test 2.1.26 (Test scope, `service` module).

## Commands

Backend (run from repo root):

```bash
sbt compile              # compile all modules (Scala 3, -Xfatal-warnings on)
sbt test                 # run all ZIO Test specs (ItemServiceSpec, CategoryServiceSpec in service/)
sbt run                  # start the HTTP server on port 8080 (entry point: server/.../Main.scala)
sbt scalafmtAll          # format all Scala sources (.scalafmt.conf, scala3 dialect, maxColumn=160)
sbt "project service" test   # run tests for a single module
sbt "testOnly com.example.zivito.ItemServiceSpec"        # run one spec
sbt "testOnly *ItemServiceSpec -- -t \"test name\""      # run a single test case
```

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

- **domain** — pure model & contracts only. `Domain` (Item, Category) with `zio-json` codecs via `derives`, and `Error`. No ZIO effects beyond types. Has no module dependencies.
- **storage** — repository traits + implementations. `ItemRepo`/`ItemRepoPersist` and `CategoryRepo`/`CategoryRepoPersist` use Quill ProtoQuill's `H2ZioJdbcContext(Escape)`.
- **service** — business logic as `XxxService` trait + `XxxServiceImpl` case class. Thin pass-throughs to repos today.
- **route** — `zio-http` 3.x declarative routes (`Routes(Method.GET / "items" -> handler { ... })`), one object per resource (`ItemRoutes`, `CategoryRoutes`). Path params use codecs (`uuid("id")`, `string("query")`); static segments are ordered before `uuid("id")`. Request DTOs (e.g. `CreateItemRequest`) and their `zio-json` codecs (`derives JsonDecoder`) are defined inline here. Each `routes` is a `Routes[Service, Throwable]`.
- **server** — composition root. `Main` wires routes and provides all `ZLayer`s; reads config under the `App` prefix.

### Dependency injection conventions

- Each component is a `case class` implementing a `trait`, exposing a `def layer: ZLayer[...]` in its companion (e.g. `ItemServiceImpl.layer = ZLayer.fromFunction(ItemServiceImpl(_))`).
- Routes access services via `ZIO.serviceWithZIO[ItemService](_.method)`.
- The Quill data source is provided **once** in `Main` via `Quill.DataSource.fromPrefix("App")` (single shared pool). Repo `layer`s are `ZLayer.fromFunction(XxxRepoPersist(_)): ZLayer[DataSource, Nothing, XxxRepo]` (no per-repo `fromPrefix`); each repo call ends with `.provide(ZLayer.succeed(ds))`.
- `ItemRepoPersist.create` owns item id generation (`Random.nextUUID`); routes pass a placeholder id that the repo overwrites.
- All effects are typed as `Task[A]` (error channel = `Throwable`). Keep this convention unless deliberately introducing typed errors from `Error.scala`.
- Layer wiring is assembled only in `Main.run`'s `.provide(...)`. Add new layers there.

### Config & persistence

- Runtime config lives in `server/src/main/resources/application.conf` under the `App` key (H2 datasource). Quill reads it directly via `Quill.DataSource.fromPrefix("App")` (Typesafe config) — no `zio-config` dependency.
- H2 is file-backed (`jdbc:h2:file:./userapp`) and runs `classpath:h2-schema.sql` on init.

## Gotchas

- The real entry point is `Main` (`server/.../Main.scala`); it calls `Server.serve(routes.handleError(...))` with all layers provided once.
- Routes also include `GET /items/category/{categoryId}` (beyond the README list).
- CI (`.github/workflows/ci.yml`) runs `scalafmtCheckAll` → `compile` → `test` → `sbt stage` (native-packager / `JavaAppPackaging`) on JDK 17 for the `master`/`main` branches.
- `.gitignore` covers all `target/` dirs and `*.mv.db`/`*.trace.db`; build artifacts are no longer tracked. Don't edit anything under a `target/` directory.
- `scalacOptions` (project/Settings.scala) is an explicit Scala-3 set: `-deprecation -feature -unchecked -encoding utf-8 -Wunused:all -Xfatal-warnings`. `-Xfatal-warnings` is on — **unused imports** (`-Wunused:all`) / deprecations break the build. sbt-tpolecat was dropped in favor of these explicit flags.
- ProtoQuill needs `import io.getquill.*` (for `query`/`lift`/`insertValue`/`updateValue`/`delete`) **plus** `import ctx.*` (for `run`). The Scala-2 single `import ctx._` is not enough.
- In routes, `zio.http.uuid` (path codec) collides with `zio.json.uuid` — exclude it via `import zio.json.{uuid as _, *}`.
- zio-http 3.x pulls zio-schema-json needing zio-json 0.9.x; `build.sbt` sets `libraryDependencySchemes += "dev.zio" %% "zio-json" % VersionScheme.Always` so Quill's older zio-json constraint doesn't trip the eviction error.
