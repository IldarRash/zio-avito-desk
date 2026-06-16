package com.example.zivito

import zio.*
import zio.http.*
import io.getquill.jdbczio.Quill

object Main extends ZIOAppDefault {

  private val port       = sys.env.get("APP_PORT").flatMap(_.toIntOption).getOrElse(8080)
  private val corsOrigin = sys.env.getOrElse("CORS_ORIGIN", "http://localhost:3000")

  private val corsConfig: Middleware.CorsConfig =
    Middleware.CorsConfig(
      allowedOrigin = origin =>
        if (corsOrigin == "*" || Header.Origin.render(origin) == corsOrigin) Some(Header.AccessControlAllowOrigin.Specific(origin))
        else None,
      allowedMethods = Header.AccessControlAllowMethods(Method.GET, Method.POST, Method.PUT, Method.DELETE, Method.OPTIONS),
      allowCredentials = Header.AccessControlAllowCredentials.Allow
    )

  private val protectedRoutes =
    (ItemRoutes.protectedRoutes ++ CategoryRoutes.protectedRoutes) @@ AuthMiddleware.authAspect

  private val httpApp =
    (ItemRoutes.publicRoutes ++
      CategoryRoutes.publicRoutes ++
      AuthRoutes.routes ++
      HealthRoutes.routes ++
      Uploads.routes ++
      protectedRoutes)
      .handleErrorCauseZIO(ApiError.handle) @@ Middleware.cors(corsConfig) @@ Middleware.requestLogging()

  override def run: ZIO[Any, Throwable, Nothing] =
    (DbMigration.migrate *>
      ZIO.logInfo(s"Starting server on port $port") *>
      Server.serve(httpApp)).provide(
      Server.defaultWithPort(port),
      ItemServiceImpl.layer,
      ItemRepoPersist.layer,
      CategoryServiceImpl.layer,
      CategoryRepoPersist.layer,
      AuthServiceImpl.layer,
      UserRepoPersist.layer,
      SessionRepoPersist.layer,
      Quill.DataSource.fromPrefix("App")
    )
}
