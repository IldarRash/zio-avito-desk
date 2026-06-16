package com.example.zivito

import zio.*
import zio.http.*
import zio.json.*

/** Liveness/readiness endpoints for load balancers and orchestrators. */
object HealthRoutes {

  final case class Health(status: String) derives JsonEncoder

  val routes: Routes[Any, Throwable] =
    Routes(
      Method.GET / "health" -> handler(Response.json(Health("ok").toJson))
    )
}
