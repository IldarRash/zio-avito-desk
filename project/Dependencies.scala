import sbt._

object Dependencies {
  val zioCore = "dev.zio" %% "zio" % Version.zio
  val zioStreams = "dev.zio" %% "zio-streams" % Version.zio
  val zioTest = "dev.zio" %% "zio-test" % Version.zio % Test
  val zioTestSbt = "dev.zio" %% "zio-test-sbt" % Version.zio % Test
  val http = "dev.zio" %% "zio-http" % Version.http
  val json = "dev.zio" %% "zio-json" % Version.json
  val quillJdbc = "io.getquill" %% "quill-jdbc-zio" % Version.quill
  val postgres = "org.postgresql" % "postgresql" % Version.postgres
  val flyway = "org.flywaydb" % "flyway-core" % Version.flyway
  val flywayPostgres = "org.flywaydb" % "flyway-database-postgresql" % Version.flyway
  val bcrypt = "at.favre.lib" % "bcrypt" % Version.bcrypt
  val testcontainersPg = "com.dimafeng" %% "testcontainers-scala-postgresql" % Version.testcontainers % Test
}

object Version {
  val zio = "2.1.26"
  val http = "3.11.2"
  val json = "0.9.1"
  val quill = "4.8.6"
  val postgres = "42.7.5"
  val flyway = "11.1.0"
  val bcrypt = "0.10.2"
  val testcontainers = "0.41.4"
}
