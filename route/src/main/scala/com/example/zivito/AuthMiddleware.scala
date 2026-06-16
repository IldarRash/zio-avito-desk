package com.example.zivito

import zio.*
import zio.http.*
import zio.json.*

import com.example.zivito.Domain.User

/** Session-cookie authentication. The [[authAspect]] resolves the `session` cookie to the current [[Domain.User]] and makes it available to downstream
  * handlers; requests without a valid session are rejected with `401`.
  */
object AuthMiddleware {

  val cookieName = "session"

  private def unauthorized(message: String): Response =
    Response.json(ApiError.ErrorBody(message).toJson).status(Status.Unauthorized)

  val authAspect: HandlerAspect[AuthService, User] =
    HandlerAspect.interceptIncomingHandler(Handler.fromFunctionZIO[Request] { request =>
      request.cookie(cookieName) match {
        case Some(cookie) =>
          ZIO
            .serviceWithZIO[AuthService](_.authenticate(cookie.content))
            .foldZIO(
              _ => ZIO.fail(Response.json(ApiError.ErrorBody("Authentication check failed").toJson).status(Status.InternalServerError)),
              {
                case Some(user) => ZIO.succeed((request, user))
                case None       => ZIO.fail(unauthorized("Invalid or expired session"))
              }
            )
        case None => ZIO.fail(unauthorized("Authentication required"))
      }
    })
}
