# ZIO Avito Desk

ZIO Avito Desk is a web application that demonstrates how to build a simple classifieds board using the ZIO stack on the backend and React on the frontend.

## Features

*   **View a list of items:** See all the items available on the board.
*   **Search for items:** Find specific items using a search bar.
*   **View item details:** Click on an item to see more information about it.
*   **Create new items:** Add your own items to the board.
*   **Delete items:** Remove items from the board.

## Tech Stack

*   **Backend:**
    *   [ZIO](https://zio.dev/): A library for asynchronous and concurrent programming in Scala.
    *   [ZIO HTTP](https://zio.github.io/zio-http/): A high-performance, easy-to-use HTTP server and client library for ZIO.
    *   [Quill](https://getquill.io/): A compile-time language-integrated query library for Scala.
    *   [H2 Database](https://www.h2database.com/): An in-memory, relational database.
*   **Frontend:**
    *   [React](https://reactjs.org/): A JavaScript library for building user interfaces.
    *   [TypeScript](https://www.typescriptlang.org/): A typed superset of JavaScript that compiles to plain JavaScript.

## Getting Started

### Prerequisites

*   [Java Development Kit (JDK) 11](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html) or later.
*   [sbt](https://www.scala-sbt.org/): The interactive build tool for Scala.
*   [Node.js](https://nodejs.org/) and [npm](https://www.npmjs.com/).

### Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-username/zio-avito-desk.git
    cd zio-avito-desk
    ```
2.  **Run the backend:**
    Open a terminal and run the following command to start the backend server:
    ```bash
    sbt "run"
    ```
    The server will start on port 8080.

3.  **Run the frontend:**
    Open another terminal, navigate to the `frontend` directory, and install the dependencies:
    ```bash
    cd frontend
    npm install
    ```
    Then, start the frontend development server:
    ```bash
    npm start
    ```
    The application will be available at [http://localhost:3000](http://localhost:3000).

## How to Use

*   **Search:** Type in the search bar and click the "Search" button to filter the items.
*   **View Details:** Click on any item card to see more details (not yet implemented).
*   **Create/Delete:** Use a tool like [Postman](https://www.postman.com/) or `curl` to send requests to the backend API to create or delete items.

### API Endpoints

*   `GET /items`: Get all items.
*   `GET /items/{id}`: Get an item by its ID.
*   `GET /items/search/{query}`: Search for items.
*   `POST /items`: Create a new item.
*   `DELETE /items/{id}`: Delete an item.
*   `GET /categories`: Get all categories.
*   `GET /categories/{id}`: Get a category by its ID.

## Contributing

Contributions are welcome! Please feel free to open an issue or submit a pull request.
