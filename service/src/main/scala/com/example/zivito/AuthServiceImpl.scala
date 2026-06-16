package com.example.zivito

import zio._

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.zivito.Domain.User

case class AuthServiceImpl(users: UserRepo, sessions: SessionRepo) extends AuthService {

  private val sessionTtl = 7.days

  override def register(email: String, password: String, displayName: String): Task[(User, String)] =
    for {
      existing <- users.findByEmail(email)
      _        <- ZIO.when(existing.isDefined)(ZIO.fail(AppError.Conflict("An account with that email already exists")))
      hash     <- ZIO.attempt(BCrypt.withDefaults().hashToString(10, password.toCharArray))
      id       <- Random.nextUUID
      now      <- Clock.instant
      user = User(id, email, displayName, now)
      _     <- users.create(user, hash)
      token <- startSession(user.id, now)
    } yield (user, token)

  override def login(email: String, password: String): Task[(User, String)] =
    for {
      creds <- users.findByEmail(email).someOrFail(AppError.Unauthorized("Invalid email or password"))
      ok    <- ZIO.attempt(BCrypt.verifyer().verify(password.toCharArray, creds.passwordHash.toCharArray).verified)
      _     <- ZIO.unless(ok)(ZIO.fail(AppError.Unauthorized("Invalid email or password")))
      now   <- Clock.instant
      token <- startSession(creds.user.id, now)
    } yield (creds.user, token)

  override def logout(token: String): Task[Unit] = sessions.delete(token)

  override def authenticate(token: String): Task[Option[User]] =
    for {
      now    <- Clock.instant
      userId <- sessions.userIdFor(token, now)
      user   <- ZIO.foreach(userId)(users.get).map(_.flatten)
    } yield user

  private def startSession(userId: java.util.UUID, now: java.time.Instant): Task[String] =
    for {
      bytes <- Random.nextBytes(32)
      token = bytes.toArray.map(b => "%02x".format(b & 0xff)).mkString
      _ <- sessions.create(token, userId, now, now.plus(sessionTtl))
    } yield token
}

object AuthServiceImpl {
  def layer: ZLayer[UserRepo & SessionRepo, Nothing, AuthService] =
    ZLayer.fromFunction(AuthServiceImpl(_, _))
}
