# ZIO Avito Desk

> A classifieds-board backend (Avito-style) built on the ZIO effect stack in Scala, with a layered, dependency-injected module design and a React + TypeScript frontend.

![Scala](https://img.shields.io/badge/Scala-2.13-DC322F?logo=scala&logoColor=white)
![ZIO](https://img.shields.io/badge/ZIO-effect%20stack-7B1FA2)
![ZIO HTTP](https://img.shields.io/badge/ZIO%20HTTP-server-7B1FA2)
![Quill](https://img.shields.io/badge/Quill-compile--time%20SQL-1f6feb)
![H2](https://img.shields.io/badge/H2-in--memory%20DB-0066A1)
![React](https://img.shields.io/badge/React-UI-61DAFB?logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-frontend-3178C6?logo=typescript&logoColor=white)
![sbt](https://img.shields.io/badge/sbt-build-CC2927)

## What & why

ZIO Avito Desk demonstrates how to build a simple classifieds board end to end on the **ZIO** stack: typed, composable effects on the backend with clean separation between domain, persistence, business logic, and HTTP routing, served to a React frontend. The backend is organized as a chain of sbt modules wired through ZIO's dependency-injection layers (`ZLayer`), so each concern depends only on the one beneath it. Users can browse, search, view, create, and delete listings.

## Features

- View a list of items on the board
- Search for items
- View item details
- Create new items
- Delete items

## Architecture

A layered sbt multi-module backend; each module depends only on the layer below it, and ZIO `ZLayer` wires them together:

```text
  server   ← application entrypoint, HTTP server bootstrap
    │ depends on
  route    ← ZIO HTTP routes / endpoints
    │
  service  ← business logic (use cases)
    │
  storage  ← persistence: Quill queries over the H2 database
    │
  domain   ← core models and pure domain logic
```

The React + TypeScript frontend in `frontend/` consumes the REST API.

### API endpoints

| Method | Path                    | Description           |
|--------|-------------------------|-----------------------|
| GET    | `/items`                | Get all items         |
| GET    | `/items/{id}`           | Get an item by id     |
| GET    | `/items/search/{query}` | Search for items      |
| POST   | `/items`                | Create a new item     |
| DELETE | `/items/{id}`           | Delete an item        |
| GET    | `/categories`           | Get all categories    |
| GET    | `/categories/{id}`      | Get a category by id  |

## Tech stack

- **Backend:** [ZIO](https://zio.dev/) (async/concurrent effects), [ZIO HTTP](https://zio.dev/zio-http/) (server), [Quill](https://getquill.io/) (compile-time query generation), [H2](https://www.h2database.com/) (in-memory database).
- **Frontend:** [React](https://reactjs.org/) + [TypeScript](https://www.typescriptlang.org/).

## Getting started

### Prerequisites

- JDK 11+
- [sbt](https://www.scala-sbt.org/)
- Node.js + npm

### Run the backend

```bash
sbt run
```

The server starts on port `8080`.

### Run the frontend

```bash
cd frontend
npm install
npm start
```

The app is available at [http://localhost:3000](http://localhost:3000).

## Project structure

```text
domain/    core models + pure domain logic
storage/   persistence (Quill over H2)
service/   business logic / use cases
route/     ZIO HTTP routes
server/    application entrypoint + HTTP server
frontend/  React + TypeScript UI
project/   sbt build configuration
build.sbt  sbt multi-module definition
```

## Contributing

Contributions are welcome — feel free to open an issue or submit a pull request.
