---
name: new-endpoint
description: Scaffold a complete backend vertical slice (domain model + repository + service + routes + wiring) for a new resource in the com.example.zivito ZIO codebase, mirroring the existing Item stack.
disable-model-invocation: true
---

# new-endpoint

Scaffold a new backend resource across all layers of the onion. Usage: `/new-endpoint <ResourceName>` (e.g. `/new-endpoint Order`). Read `CLAUDE.md` for architecture before generating.

Mirror the existing **Item** stack exactly; use it as the canonical template:
- `domain/.../Domain.scala` (model + `zio-json` codecs)
- `storage/.../ItemRepo.scala` + `storage/.../ItemRepoPersist.scala`
- `service/.../ItemService.scala` + `service/.../ItemServiceImpl.scala`
- `route/.../ItemRoutes.scala`
- `server/.../Main.scala` (wiring)

## Steps

Given `<Resource>` (and ask the user for its fields if not provided):

1. **domain** — add `final case class <Resource>(id: UUID, ...fields) derives JsonEncoder, JsonDecoder` (Scala 3 `derives`).
2. **storage** — create `<Resource>Repo` trait (`Task`-typed CRUD) and `<Resource>RepoPersist(ds: DataSource)` using ProtoQuill (`new H2ZioJdbcContext(Escape)`, `import io.getquill.*` + `import ctx.*`), each query ending `.provide(ZLayer.succeed(ds))`. Add a `<Resource>Table` case class if the row shape differs. Companion `def layer = ZLayer.fromFunction(<Resource>RepoPersist(_))` typed `ZLayer[DataSource, Nothing, <Resource>Repo]` (consumes the shared DataSource — no per-repo `fromPrefix`).
3. **service** — `<Resource>Service` trait + `<Resource>ServiceImpl(repo)` + companion `def layer = ZLayer.fromFunction(<Resource>ServiceImpl(_))`.
4. **route** — `<Resource>Routes` object exposing `routes: Routes[<Resource>Service, Throwable]` built with zio-http 3.x `Routes(Method.X / ... -> handler { ... })`; standard GET/GET-by-id(`uuid("id")`)/POST/DELETE; inline `Create<Resource>Request` DTO (`derives JsonDecoder`); services via `ZIO.serviceWithZIO`. Use `import zio.json.{uuid as _, *}` to dodge the `zio.http.uuid` clash; static path segments before `uuid("id")`.
5. **server** — combine `++ <Resource>Routes.routes` in `Main` and add the new `layer`s to `Main.run`'s `.provide(...)` (the single `Quill.DataSource.fromPrefix("App")` already there feeds the new repo).
6. If the H2 schema needs a table, note that `application.conf` runs `classpath:h2-schema.sql` on init — add the `CREATE TABLE` there.

## Done

- `sbt compile` passes clean (`-Xfatal-warnings` is on — no unused imports).
- Tell the user to consider `/gen-zio-test <Resource>Service` for coverage.
- Remind that all layers must be wired only in `Main.run` and the onion direction must hold.
