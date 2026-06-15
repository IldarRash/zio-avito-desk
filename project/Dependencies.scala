import sbt._

object Dependencies {
  val zioCore = "dev.zio" %% "zio" % Version.zio
  val zioStreams = "dev.zio" %% "zio-streams" % Version.zio
  val zioTest = "dev.zio" %% "zio-test" % Version.zio % Test
  val zioTestSbt = "dev.zio" %% "zio-test-sbt" % Version.zio % Test
  val h2 = "com.h2database" % "h2" % Version.h2
  val http = "dev.zio" %% "zio-http" % Version.http
  val json = "dev.zio" %% "zio-json" % Version.json
  val quillJdbc = "io.getquill" %% "quill-jdbc-zio" % Version.quill
}

object Version {
  val zio = "2.1.26"
  val h2 = "2.4.240"
  val http = "3.11.2"
  val json = "0.9.1"
  val quill = "4.8.6"
}
