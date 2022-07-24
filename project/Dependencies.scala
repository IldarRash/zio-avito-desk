import sbt._

object Dependencies {
  val zioCore =  "dev.zio" %% "zio" % Version.zio
  val zioStreams = "dev.zio" %% "zio-streams" % Version.zio
  val dynamoDb = "dev.zio" %% "zio-dynamodb" % Version.dynamoDb
  val http = "io.d11"  %% "zhttp" % Version.http
  val json = "dev.zio" %% "zio-json" % Version.json
}

object Version {
  val zio = "2.0.0"
  val dynamoDb = "0.2.0-RC2"
  val http = "2.0.0-RC10"
  val json = "0.3.0-RC9"
}