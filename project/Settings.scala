import Dependencies._
import sbt.Keys.{scalacOptions, _}
import sbt._
object Settings {

  val commonSettings = {
    Seq(
      scalaVersion := "2.13.6",
      scalacOptions := Seq(
        "-Ymacro-annotations",
        "-deprecation",
        "-encoding", "utf-8",
        "-explaintypes",
        "-feature",
        "-unchecked",
        "-language:postfixOps",
        "-language:higherKinds",
        "-language:implicitConversions",
        "-Xcheckinit",
        "-Xfatal-warnings"
      ),
      logLevel := Level.Debug,
      version := (version in ThisBuild).value,
      testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
      javaOptions += "-Dlogback.configurationFile=/src/resources/logback.xml",
      resolvers += Resolver.sonatypeRepo("snapshots")
    )
  }


  val core = List(zioCore, zioStreams, json, dynamoDb)
  val httpRoutes = List(http) ++ core
}