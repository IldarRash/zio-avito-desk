package com.example.zivito

import zio.*
import zio.json.*
import zio.http.*

import com.example.zivito.Domain.User

object AuthRoutes {

  final case class RegisterRequest(email: String, password: String, displayName: String) derives JsonDecoder
  final case class LoginRequest(email: String, password: String) derives JsonDecoder

  private val secureCookies: Boolean = sys.env.get("COOKIE_SECURE").exists(_.equalsIgnoreCase("true"))
  private val sessionTtl: Duration   = 7.days

  private def sessionCookie(token: String, maxAge: Duration): Cookie.Response =
    Cookie.Response(
      name = AuthMiddleware.cookieName,
      content = token,
      domain = None,
      path = Some(Path.root),
      isSecure = secureCookies,
      isHttpOnly = true,
      maxAge = Some(maxAge),
      sameSite = Some(Cookie.SameSite.Lax)
    )

  private def parseBody[A](req: Request)(using JsonDecoder[A]): ZIO[Any, Throwable, A] =
    req.body.asString.flatMap(b => ZIO.fromEither(b.fromJson[A]).mapError(m => AppError.Validation("body", s"is not valid JSON: $m")))

  val routes: Routes[AuthService, Throwable] =
    Routes(
      Method.POST / "auth" / "register" -> handler { (req: Request) =>
        for {
          body  <- parseBody[RegisterRequest](req)
          email <- Validate.text("email", body.email, 255)
          _     <- ZIO.when(!email.contains("@"))(ZIO.fail(AppError.Validation("email", "must be a valid email address")))
          _     <- ZIO.when(body.password.length < 8)(ZIO.fail(AppError.Validation("password", "must be at least 8 characters")))
          name  <- Validate.text("displayName", body.displayName, 255)
          res   <- ZIO.serviceWithZIO[AuthService](_.register(email, body.password, name))
        } yield Response.json(res._1.toJson).status(Status.Created).addCookie(sessionCookie(res._2, sessionTtl))
      },
      Method.POST / "auth" / "login" -> handler { (req: Request) =>
        for {
          body <- parseBody[LoginRequest](req)
          res  <- ZIO.serviceWithZIO[AuthService](_.login(body.email, body.password))
        } yield Response.json(res._1.toJson).addCookie(sessionCookie(res._2, sessionTtl))
      },
      Method.POST / "auth" / "logout" -> handler { (req: Request) =>
        ZIO
          .foreachDiscard(req.cookie(AuthMiddleware.cookieName).map(_.content))(t => ZIO.serviceWithZIO[AuthService](_.logout(t)))
          .as(Response.ok.addCookie(sessionCookie("", Duration.Zero)))
      },
      Method.GET / "auth" / "me" -> handler { (req: Request) =>
        req.cookie(AuthMiddleware.cookieName).map(_.content) match {
          case Some(token) =>
            ZIO.serviceWithZIO[AuthService](_.authenticate(token)).someOrFail(AppError.Unauthorized()).map(u => Response.json(u.toJson))
          case None => ZIO.fail(AppError.Unauthorized())
        }
      }
    )
}
