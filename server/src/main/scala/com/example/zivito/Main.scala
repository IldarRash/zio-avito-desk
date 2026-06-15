package com.example.zivito

import zio.*
import zio.http.*
import io.getquill.jdbczio.Quill

object Main extends ZIOAppDefault {

  private val routes = ItemRoutes.routes ++ CategoryRoutes.routes

  override def run: ZIO[Any, Throwable, Nothing] =
    (ZIO.logInfo("Starting server on port 8080") *>
      Server.serve(routes.handleError(e => Response.internalServerError(e.getMessage)))).provide(
      Server.defaultWithPort(8080),
      ItemServiceImpl.layer,
      ItemRepoPersist.layer,
      CategoryServiceImpl.layer,
      CategoryRepoPersist.layer,
      Quill.DataSource.fromPrefix("App")
    )
}
