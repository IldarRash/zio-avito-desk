package com.example.zivito

import zio.*
import zio.http.*
import zio.json.*

/** Maps failures from the effect channel onto HTTP responses with a JSON body `{ "error": "..." }`. Typed [[AppError]]s map to 4xx; anything else is an
  * unexpected failure and becomes a `500` with a generic message (details are logged, not leaked to the client).
  */
object ApiError {

  final case class ErrorBody(error: String) derives JsonEncoder

  private def json(status: Status, message: String): Response =
    Response.json(ErrorBody(message).toJson).status(status)

  def toResponse(error: Throwable): Response =
    error match {
      case e: AppError.Validation   => json(Status.BadRequest, e.getMessage)
      case e: AppError.NotFound     => json(Status.NotFound, e.getMessage)
      case e: AppError.Unauthorized => json(Status.Unauthorized, e.getMessage)
      case e: AppError.Forbidden    => json(Status.Forbidden, e.getMessage)
      case e: AppError.Conflict     => json(Status.Conflict, e.getMessage)
      case _                        => json(Status.InternalServerError, "Internal server error")
    }

  /** Handle the whole route tree's error channel: log unexpected failures, then map every failure to a response. Typed [[AppError]]s are expected and not
    * logged at error level.
    */
  def handle(cause: Cause[Throwable]): ZIO[Any, Nothing, Response] =
    cause.failureOption match {
      case Some(e: AppError) => ZIO.succeed(toResponse(e))
      case Some(e)           => ZIO.logErrorCause("Unhandled request failure", cause).as(toResponse(e))
      case None              => ZIO.logErrorCause("Request defect", cause).as(json(Status.InternalServerError, "Internal server error"))
    }
}
