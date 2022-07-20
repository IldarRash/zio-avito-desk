import sbt._

object Dependencies {
  val zioCore =  "dev.zio" %% "zio" % Version.zio
  val zioStreams = "dev.zio" %% "zio-streams" % Version.zio
}

object Version {
  val zio = "2.0.0"
}