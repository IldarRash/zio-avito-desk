import sbt._
import Settings._

lazy val model = project
  .settings(commonSettings)
  .settings(libraryDependencies ++= core)

lazy val service = project
  .settings(commonSettings)
  .settings(libraryDependencies ++= core)
  .dependsOn(model)

lazy val route = project
  .settings(commonSettings)
  .settings(libraryDependencies ++= core)
  .dependsOn(service)

lazy val server = project
  .settings(commonSettings)
  .settings(libraryDependencies ++= core)
  .dependsOn(route)

lazy val `avito-desk` = Project("avito-desk", file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings)
  .settings(organization := "com.example.avito")
  .settings(moduleName := "avito-desk")
  .settings(name := "avito-desk")
  .aggregate(
    model,
    service,
    route,
    server
  )
  .dependsOn(
    model,
    service,
    route,
    server
  )