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
  val caliban = "com.github.ghostdogpr" %% "caliban" % Version.caliban
  val calibanZioHttp = "com.github.ghostdogpr" %% "caliban-zio-http" % Version.caliban
  val javaJwt = "com.auth0" % "java-jwt" % Version.javaJwt
}

object Version {
  val zio = "2.0.15"
  val h2 = "2.1.214"
  val http = "2.0.0-RC10"
  val json = "0.3.0-RC9"
  val quill = "3.17.0-RC2"
  val config = "3.0.1"
  val caliban = "2.5.2"
  val javaJwt = "4.4.0"
}