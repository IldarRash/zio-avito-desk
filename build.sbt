import sbt._
import Settings._

lazy val domain = project
  .settings(commonSettings)
  .settings(libraryDependencies ++= core)

lazy val service = project
  .settings(commonSettings)
  .settings(libraryDependencies ++= core)
  .dependsOn(domain)

lazy val route = project
  .settings(commonSettings)
  .settings(libraryDependencies ++= httpRoutes)
  .dependsOn(service)

lazy val server = project
  .settings(commonSettings)
  .settings(libraryDependencies ++= httpRoutes)
  .dependsOn(route)

lazy val `avito-desk` = Project("avito-desk", file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings)
  .settings(organization := "com.example.avito")
  .settings(moduleName := "avito-desk")
  .settings(name := "avito-desk")
  .aggregate(
    domain,
    service,
    route,
    server
  )
  .dependsOn(
    domain,
    service,
    route,
    server
  )