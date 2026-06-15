---
name: add-auth-route
description: Scaffold a new zio-http route guarded by the session-cookie auth middleware in the com.example.zivito backend. Use after the auth-implementer has established AuthService and the session middleware.
disable-model-invocation: true
---

# add-auth-route

Scaffold a **protected** backend route guarded by session-cookie auth. Usage: `/add-auth-route <description>` (e.g. `/add-auth-route POST /items requires login`). Read `CLAUDE.md` first.

## Prerequisite

The session-cookie auth must already exist (created by the **auth-implementer** agent): `AuthService.validate(sessionId)`, the `SessionRepository`, and the zio-http auth middleware/`HandlerAspect`. If they don't exist yet, stop and tell the user to run the auth-implementer agent first.

## Steps

1. Add the route to the appropriate `*Routes` object (or create one), following the zio-http 3.x `Routes(Method.X / ... -> handler { ... })` + inline DTO (`derives JsonDecoder`) pattern in `ItemRoutes`.
2. Read the session cookie (`req.cookie(name)`) and resolve the current user via `AuthService.validate`; short-circuit with `Status.Unauthorized` when the cookie is missing or invalid. Prefer applying the existing auth middleware (`routes @@ authAspect`) over re-implementing the check inline.
3. If the handler needs the authenticated user's identity, thread it from the validated session — never trust a user id from the request body.
4. Wire any new service/route into `Main` and `Main.run`'s `.provide(...)` if not already present.

## Guardrails

- Keep the `Task[A]` error channel; use the typed auth errors in `domain/.../Error.scala`.
- Never expose `passwordHash` or the raw session id in a response body.
- Respect the onion; layers wired only in `Main.run`.

## Done

`sbt compile` clean. Verify: a request without the cookie gets 401; with a valid session it succeeds. Hand off to **zio-reviewer** for a security pass.
