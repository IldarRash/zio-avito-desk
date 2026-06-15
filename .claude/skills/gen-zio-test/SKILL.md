---
name: gen-zio-test
description: Scaffold a zio-test (ZIOSpecDefault) spec for a chosen service or repository in the com.example.zivito codebase, using Ref-backed in-memory dependencies for fast deterministic tests.
disable-model-invocation: true
---

# gen-zio-test

Scaffold a ZIO Test spec. Usage: `/gen-zio-test <ServiceOrRepoName>` (e.g. `/gen-zio-test ItemService`). zio-test + `ZTestFramework` are configured (Test scope on the `service` module); follow the existing `ItemServiceSpec`/`CategoryServiceSpec` (Scala 3, `Ref`-backed in-memory repo) as the pattern. Read `CLAUDE.md` first.

## Steps

1. Place the spec at `<module>/src/test/scala/com/example/zivito/<Name>Spec.scala`, mirroring the unit under test (e.g. service tests under `service/src/test/...`).
2. Extend `ZIOSpecDefault`; structure with `suite("...")(test("...") { ... })` and `assertTrue`.
3. Provide dependencies via `ZLayer`:
   - For **service** specs, layer the production `<Name>Impl.layer` on top of a **hand-written in-memory repo** backed by `Ref[Map[UUID, T]]` (fast, deterministic — no DB).
   - For **repository** specs that must hit H2, use an in-memory data source (`jdbc:h2:mem:...`).
4. Cover success **and** failure paths; keep the `Task[A]` error channel. For `ItemService`-style code, exercise create-assigns-UUID, search `like`, get-by-category, and delete.

## Guardrails

- Do **not** assert on unimplemented/stub behavior — if a feature is a stub, note it instead of writing a passing-but-meaningless test.
- `-Wunused:all` + `-Xfatal-warnings` are on — no unused imports.

## Done

Run `sbt "testOnly *<Name>Spec"` and report pass/fail plus anything that couldn't be tested and why.
