import Dependencies._
import sbt.Keys.{scalacOptions, _}
import sbt._
object Settings {

  val commonSettings = {
    Seq(
      scalaVersion := "3.3.8",
      scalacOptions := Seq(
        "-deprecation",
        "-feature",
        "-unchecked",
        "-encoding",
        "utf-8",
        "-Wunused:all",
        "-Xfatal-warnings"
      ),
      logLevel := Level.Debug,
      version := (version in ThisBuild).value,
      testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
      javaOptions += "-Dlogback.configurationFile=/src/resources/logback.xml",
      resolvers += Resolver.sonatypeRepo("snapshots")
    )
  }

  val domain = List(json)
  val repos = List(quillJdbc, h2)
  val core = List(zioCore, zioStreams)
  val coreWithTest = core ++ List(zioTest, zioTestSbt)
  val httpRoutes = List(http) ++ core
  val server = List.empty
}
