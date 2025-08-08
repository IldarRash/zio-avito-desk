package com.example.zivito

import zio._
import zhttp.http._
import zhttp.service.Server
import zio.config._
import zio.config.typesafe._
import io.getquill.context.ZioJdbc.DataSourceLayer

object Main extends ZIOAppDefault {

  override def run: ZIO[Any, Throwable, Nothing] =
    (for {
      _ <- ZIO.logInfo("Starting server on port 8080")
      _ <- Server.start(
        port = 8080,
        http = ItemRoutes.routes ++ CategoryRoutes.routes ++ GraphQLRoutes.routes
      )
    } yield ()).provide(
      ItemServiceImpl.layer,
      ItemRepoPersist.layer,
      CategoryRepoPersist.layer,
      CategoryServiceImpl.layer,
      InMemoryUserRepo.layer,
      JwtAuthService.layer,
      zio.Scope.default,
      DataSourceLayer.fromPrefix("App")
    )
}