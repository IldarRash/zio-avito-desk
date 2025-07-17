package com.example.zivito

import zio._
import zhttp.http._
import zhttp.service.Server
import zio.config._
import zio.config.typesafe._

object Main extends ZIOAppDefault {
  
  def run: ZIO[Environment with ZIOAppArgs, Throwable, ExitCode] = {
    val app = ItemRoutes.routes ++ CategoryRoutes.routes
    
    val program = for {
      _ <- ZIO.logInfo("Starting Avito Desk Server...")
      _ <- Server.start(8080)
        .provide(
          ItemServiceImpl.layer,
          CategoryServiceImpl.layer
        )
    } yield ()
    
    program.exitCode
  }
}