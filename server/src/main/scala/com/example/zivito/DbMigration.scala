package com.example.zivito

import javax.sql.DataSource

import org.flywaydb.core.Flyway
import zio.*

/** Runs Flyway migrations (classpath:db/migration) against the shared DataSource at startup, before the HTTP server binds.
  */
object DbMigration {

  val migrate: ZIO[DataSource, Throwable, Unit] =
    ZIO.serviceWithZIO[DataSource] { ds =>
      ZIO.logInfo("Running Flyway migrations") *>
        ZIO
          .attemptBlocking {
            Flyway
              .configure(getClass.getClassLoader)
              .dataSource(ds)
              .locations("classpath:db/migration")
              .load()
              .migrate()
          }
          .map(r => s"Flyway applied ${r.migrationsExecuted} migration(s); schema version ${Option(r.targetSchemaVersion).getOrElse("(current)")}")
          .flatMap(ZIO.logInfo(_))
    }
}
