import sbt._

object Dependencies {
  val zioCore =  "dev.zio" %% "zio" % Version.zio
  val zioStreams = "dev.zio" %% "zio-streams" % Version.zio
  val zioConfig = "dev.zio" %% "zio-config" % Version.config
  val h2 = "com.h2database" % "h2" % Version.h2
  val http = "io.d11"  %% "zhttp" % Version.http
  val json = "dev.zio" %% "zio-json" % Version.json
  val quillZio = "io.getquill" %% "quill-zio" % Version.quill
  val quillJdbc = "io.getquill" %% "quill-jdbc-zio" % Version.quill
}

object Version {
  val zio = "2.0.0"
  val h2 = "2.1.212"
  val http = "2.0.0-RC10"
  val json = "0.3.0-RC9"
  val quill = "3.17.0-RC2"
  val config = "3.0.1"
}