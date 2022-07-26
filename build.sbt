import sbt._
import Settings._

lazy val domain = project
  .settings(commonSettings)
  .settings(libraryDependencies ++= Settings.domain)

lazy val storage = project
  .settings(commonSettings)
  .settings(libraryDependencies ++= repos)
  .dependsOn(domain)

lazy val service = project
  .settings(commonSettings)
  .settings(libraryDependencies ++= core)
  .dependsOn(storage)

lazy val route = project
  .settings(commonSettings)
  .settings(libraryDependencies ++= httpRoutes)
  .dependsOn(service)

lazy val server = project
  .settings(commonSettings)
  .settings(libraryDependencies ++= Settings.server)
  .dependsOn(route)

lazy val `avito-desk` = Project("avito-desk", file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings)
  .settings(organization := "com.example.avito")
  .settings(moduleName := "avito-desk")
  .settings(name := "avito-desk")
  .aggregate(
    domain,
    storage,
    service,
    route,
    server
  )
  .dependsOn(
    domain,
    storage,
    service,
    route,
    server
  )