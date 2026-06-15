---
name: new-component
description: Scaffold a React 19 + TypeScript function component in frontend/src — the component file, a plain CSS file, its API/prop types in src/types/api.ts, and an optional fetch helper — following the project's existing conventions.
disable-model-invocation: true
---

# new-component

Scaffold a frontend component following the `frontend/` conventions. Usage: `/new-component <ComponentName>` (e.g. `/new-component ItemCard`). Read `CLAUDE.md` and `frontend/src/App.tsx` for the existing style before generating.

## Steps

Given `<Name>` (ask for its props/data shape if not provided):

1. Create `frontend/src/components/<Name>.tsx` — a TypeScript **function component** with a typed `Props` interface, using `useState`/`useEffect` only as needed. Apply styles via `className`.
2. Create `frontend/src/components/<Name>.css` — plain CSS, imported at the top of the component.
3. Add any request/response interfaces to `frontend/src/types/api.ts` (alongside `Item`, `Category`) — do not inline API types in the component.
4. If the component fetches data, use native `fetch` to relative URLs (`/items`, etc.). For authenticated calls add `{ credentials: 'include' }` (session-cookie auth). If `src/services/api.ts` exists, add the call there; otherwise keep the fetch in the component and suggest centralizing once calls multiply.

## Conventions to honor

- TypeScript `strict` — fully type props and payloads, no `any`.
- No new dependencies (no Tailwind/styled-components/axios/router) unless the user explicitly asks — flag it if you must.
- Keep state local unless it genuinely must be shared.

## Done

- `cd frontend && npm run build` passes.
- Tell the user where the component was created and how to mount it in `App.tsx`.
