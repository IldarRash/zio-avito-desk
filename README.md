# ZIO Avito Desk

A modern, type-safe REST API built with ZIO, featuring a clean architecture with domain-driven design principles.

## 🚀 Features

- **Type-safe HTTP API** using ZIO HTTP
- **Clean Architecture** with separate domain, storage, service, and route layers
- **Database Integration** with Quill ORM and H2 database
- **JSON Serialization** using ZIO JSON
- **Configuration Management** with ZIO Config
- **Functional Programming** with ZIO effects

## 📋 Prerequisites

- Java 11 or higher
- SBT (Scala Build Tool)1.5 or higher
- Scala 2.13.6

## 🛠️ Installation & Setup

### 1. Clone the repository
```bash
git clone <repository-url>
cd zio-avito-desk
```

### 2. Build the project
```bash
sbt compile
```

### 3. Run the application
```bash
sbt "project server" run
```

The server will start on port 8080

### 4. Run tests
```bash
sbt test
```

## 🏗️ Project Architecture

The project follows a clean architecture pattern with the following modules:

```
zio-avito-desk/
├── domain/          # Domain models and business logic
├── storage/         # Data access layer (repositories)
├── service/         # Business logic and use cases
├── route/           # HTTP routes and API endpoints
└── server/          # Application entry point and configuration
```

### Module Dependencies

```
server → route → service → storage → domain
```

## 📚 API Documentation

### Items API

#### Get All Items
```http
GET /items
```

#### Get Item by ID
```http
GET /items/{id}
```

#### Create Item
```http
POST /items
Content-Type: application/json
[object Object]name": "Item Name,categoryId": "uuid-string"
}
```

#### Update Item
```http
PUT /items
Content-Type: application/json

{
  id: uuid-string",
name": Updated Item Name,categoryId": "uuid-string"
}
```

#### Delete Item
```http
DELETE /items/{id}
```

#### Get Items by Category
```http
GET /items/category/{categoryId}
```

### Categories API

#### Get All Categories
```http
GET /categories
```

#### Get Category by ID
```http
GET /categories/{id}
```

#### Create Category
```http
POST /categories
Content-Type: application/json

{
 name": Category Name"
}
```

#### Update Category
```http
PUT /categories
Content-Type: application/json

{
  id: uuid-string",
nameUpdated Category Name"
}
```

#### Delete Category
```http
DELETE /categories/{id}
```

## 🛠️ Development

### Project Structure

- **Domain Layer**: Contains core business entities (`Item`, `Category`, `User`)
- **Storage Layer**: Handles data persistence with repository pattern
- **Service Layer**: Implements business logic and orchestrates operations
- **Route Layer**: Defines HTTP endpoints and request/response handling
- **Server Layer**: Application configuration and startup

### Key Technologies

- **ZIO**: Functional effect system for Scala
- **ZIO HTTP**: Type-safe HTTP library
- **Quill**: Compile-time SQL query library
- **ZIO JSON**: JSON serialization/deserialization
- **ZIO Config**: Configuration management
- **H2abase**: In-memory database for development

### Adding New Features

1**Domain Models**: Add new case classes in `domain/src/main/scala/com/example/zivito/Domain.scala`
2pository**: Create repository trait and implementation in `storage/`
3. **Service**: Add service trait and implementation in `service/`
4. **Routes**: Define HTTP endpoints in `route/`5 **Dependencies**: Update `project/Dependencies.scala` if needed

## 🧪 Testing

The project uses ZIO Test framework. Run tests with:

```bash
sbt test
```

## 📦 Building

Create a JAR file:

```bash
sbt assembly
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

For issues and questions, please create an issue in the repository.
