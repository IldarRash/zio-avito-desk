package com.example.zivito

import com.dimafeng.testcontainers.PostgreSQLContainer
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.utility.DockerImageName
import zio.*

import javax.sql.DataSource

/** Shared Testcontainers Postgres support for the storage integration tests.
  *
  * The container is started inside a scoped [[ZLayer]] (so it is created once per spec and torn down afterwards). The Quill repos use the `Escape` naming
  * strategy, which quotes the exact PascalCase case-class names; the schema below mirrors the production Flyway migrations (which live in the `server` module
  * and are not on this module's classpath), creating the quoted identifiers the repos expect.
  */
object PostgresSupport {

  private val ddl: List[String] = List(
    """CREATE TABLE IF NOT EXISTS "CategoryTable" (
      |  "id"   UUID PRIMARY KEY,
      |  "name" VARCHAR(255) NOT NULL
      |)""".stripMargin,
    """CREATE TABLE IF NOT EXISTS "UserTable" (
      |  "id"           UUID PRIMARY KEY,
      |  "email"        VARCHAR(255) NOT NULL UNIQUE,
      |  "passwordHash" VARCHAR(255) NOT NULL,
      |  "displayName"  VARCHAR(255) NOT NULL,
      |  "createdAt"    TIMESTAMP    NOT NULL DEFAULT now()
      |)""".stripMargin,
    """CREATE TABLE IF NOT EXISTS "ItemTable" (
      |  "id"          UUID PRIMARY KEY,
      |  "name"        VARCHAR(255)   NOT NULL,
      |  "description" VARCHAR(2000)  NOT NULL,
      |  "price"       DECIMAL(20, 2) NOT NULL,
      |  "categoryId"  UUID           NOT NULL REFERENCES "CategoryTable" ("id"),
      |  "location"    VARCHAR(255)   NOT NULL,
      |  "imageUrl"    VARCHAR(1000)  NOT NULL DEFAULT '',
      |  "createdAt"   TIMESTAMP      NOT NULL DEFAULT now(),
      |  "ownerId"     UUID           REFERENCES "UserTable" ("id") ON DELETE SET NULL
      |)""".stripMargin,
    """CREATE TABLE IF NOT EXISTS "SessionTable" (
      |  "token"     VARCHAR(255) PRIMARY KEY,
      |  "userId"    UUID         NOT NULL REFERENCES "UserTable" ("id") ON DELETE CASCADE,
      |  "createdAt" TIMESTAMP    NOT NULL DEFAULT now(),
      |  "expiresAt" TIMESTAMP    NOT NULL
      |)""".stripMargin
  )

  private def startContainer: Task[PostgreSQLContainer] =
    ZIO.attemptBlocking {
      val c = PostgreSQLContainer(dockerImageNameOverride = DockerImageName.parse("postgres:16"))
      c.start()
      c
    }

  private def dataSourceFor(c: PostgreSQLContainer): DataSource = {
    val ds = new PGSimpleDataSource()
    ds.setUrl(c.jdbcUrl)
    ds.setUser(c.username)
    ds.setPassword(c.password)
    ds
  }

  private def initSchema(ds: DataSource): Task[Unit] =
    ZIO.attemptBlocking {
      val conn = ds.getConnection
      try {
        val stmt = conn.createStatement()
        try ddl.foreach(stmt.execute)
        finally stmt.close()
      } finally conn.close()
    }

  /** A scoped DataSource backed by a freshly-started Postgres container with the schema created. Available to repos as their `DataSource` dependency.
    */
  val layer: ZLayer[Any, Throwable, DataSource] =
    ZLayer.scoped {
      for {
        container <- ZIO.acquireRelease(startContainer)(c => ZIO.attemptBlocking(c.stop()).orDie)
        ds = dataSourceFor(container)
        _ <- initSchema(ds)
      } yield ds
    }
}
