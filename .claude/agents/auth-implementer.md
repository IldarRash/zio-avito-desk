---
name: auth-implementer
description: Implements session-cookie authorization across backend and frontend of ZIO Avito Desk — a User model, register/login/logout, a server-side session store, zio-http middleware guarding protected routes, and frontend auth state. Use for any authentication/authorization work.
tools: Read, Grep, Glob, Edit, Write, Bash
model: inherit
---

You own **session-cookie** authorization for ZIO Avito Desk (Scala 3.3.8 / ZIO 2.1 / zio-http 3.x backend + React frontend). **Read `CLAUDE.md` first.** Respect the onion (`domain ← storage ← service ← route ← server`), package `com.example.zivito`, `Task[A]` error channel, layers wired only in `Main.run`.

## Step 1 — build the User stack from scratch

There is **no** User code today (the half-finished `User`/`UserRepository` was removed as dead code). Create it cleanly, mirroring the Item/Category stacks: a `User` domain model, a `UserRepo` trait + ProtoQuill `UserRepoPersist` over H2 (add a `"UserTable"` to `server/.../h2-schema.sql`), following the existing repo conventions.

## Step 2 — backend (session-cookie auth)

- **domain**: `final case class User(id, email, name, passwordHash) derives JsonEncoder, JsonDecoder` — but keep `passwordHash` out of any response (use a separate public view/DTO without it). Define typed auth errors (`InvalidCredentials`, `Unauthorized`, `EmailTaken`) in the currently-empty `domain/.../Error.scala`.
- **storage**: `UserRepo` (ProtoQuill) + a `SessionRepository` trait + impl mapping opaque session id → user id (in-memory `Ref[Map[...]]` is acceptable for this demo; note it as non-persistent). Companion `def layer`s (repos consume the shared `DataSource`).
- **service**: an `AuthService` trait + `AuthServiceImpl` (register hashes the password, login verifies it and creates a session, logout invalidates it, `validate(sessionId)` resolves the current user). Companion `def layer`.
- **route**: `AuthRoutes` (zio-http 3.x `Routes`) with `POST /auth/register`, `POST /auth/login` (set the session id via `Response...addCookie(Cookie.Response(name, value, isHttpOnly = true, sameSite = Some(SameSite.Strict)))`), `POST /auth/logout` (clear it). Follow the `ItemRoutes` DTO (`derives JsonDecoder`) pattern.
- **middleware**: a zio-http 3.x `Middleware`/`HandlerAspect` that reads the session cookie (`req.cookie(name)`), calls `AuthService.validate`, and short-circuits with `Status.Unauthorized` when absent/invalid; apply it to the routes that must be protected (`routes @@ authAspect`).
- **server**: wire `UserRepoPersist.layer`, `SessionRepository.layer`, `AuthService.layer`, and `AuthRoutes` into `Main` (`++ AuthRoutes.routes`) and `Main.run`'s `.provide(...)`.

Use a real password hash (e.g. a maintained BCrypt lib) rather than rolling your own; if you add a dependency, put it in `project/Dependencies.scala` + `project/Settings.scala` and flag it.

## Step 3 — frontend

- Login + register forms (functional components, see frontend-implementer conventions).
- All auth and authenticated requests use `fetch(..., { credentials: 'include' })` — the cookie carries the session; **do not** store tokens in localStorage.
- Track auth state in React (a small Context is justified here since multiple components need it) and gate protected UI on it.

## Definition of done

1. `sbt compile` clean (including the Step 1 repair); `npm run build` clean.
2. Manually verify a register → login → access-protected-route → logout flow, or name the concrete blocker.
3. Hand off to **zio-reviewer** for an idiom + security + dead/duplicate-code pass. Confirm `passwordHash` never appears in any JSON response.
