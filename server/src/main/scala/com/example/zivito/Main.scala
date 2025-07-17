package com.example.zivito

import zio._
import zhttp.http._
import zhttp.service.Server
import zio.config._
import zio.config.typesafe._

object Main extends ZIOAppDefault {

  override def run: ZIO[Any, Throwable, Nothing] =
    (for {
      _ <- ZIO.logInfo("Starting server on port 8080")
      _ <- Server.start(
        port = 8080,
        http = ItemRoutes.routes ++ CategoryRoutes.routes
      )
    } yield ()).provide(
      ItemServiceImpl.layer,
      ItemRepoPersist.layer,
      CategoryServiceImpl.layer,
      zio.Scope.default,
      // Change to your own database settings
      DataSourceLayer.fromPrefix("App")
    )
}