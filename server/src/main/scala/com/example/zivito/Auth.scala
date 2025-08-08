package com.example.zivito

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import zio._
import java.time.Instant
import java.util.{Date, UUID}

object AuthDomain {
  final case class Credentials(email: String, password: String)
  final case class JwtToken(value: String)
  final case class User(id: UUID, email: String, name: String, role: String = "user")
}

trait UserRepo {
  def create(user: AuthDomain.User, password: String): Task[AuthDomain.User]
  def findByEmail(email: String): Task[Option[(AuthDomain.User, String)]]
}

final case class InMemoryUserRepo(state: Ref[Map[String, (AuthDomain.User, String)]]) extends UserRepo {
  override def create(user: AuthDomain.User, password: String): Task[AuthDomain.User] =
    state.updateAndGet(_ + (user.email -> (user -> password))).as(user)

  override def findByEmail(email: String): Task[Option[(AuthDomain.User, String)]] =
    state.get.map(_.get(email))
}

object InMemoryUserRepo {
  val layer: ZLayer[Any, Nothing, UserRepo] = ZLayer.fromZIO(Ref.make(Map.empty[String, (AuthDomain.User, String)]).map(InMemoryUserRepo(_)))
}

trait AuthService {
  def register(name: String, email: String, password: String): Task[AuthDomain.User]
  def login(email: String, password: String): Task[AuthDomain.JwtToken]
}

final case class JwtAuthService(repo: UserRepo, secret: String = "dev-secret") extends AuthService {
  private val algorithm = Algorithm.HMAC256(secret)

  override def register(name: String, email: String, password: String): Task[AuthDomain.User] = {
    val user = AuthDomain.User(UUID.randomUUID(), email, name)
    repo.create(user, password)
  }

  override def login(email: String, password: String): Task[AuthDomain.JwtToken] =
    for {
      found <- repo.findByEmail(email)
      token <- found match {
        case Some((user, stored)) if stored == password =>
          val now = Instant.now()
          val jwt = JWT.create()
            .withSubject(user.id.toString)
            .withClaim("email", user.email)
            .withClaim("name", user.name)
            .withClaim("role", user.role)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusSeconds(60 * 60)))
            .sign(algorithm)
          ZIO.succeed(AuthDomain.JwtToken(jwt))
        case _ => ZIO.fail(new Exception("Invalid credentials"))
      }
    } yield token
}

object JwtAuthService {
  val layer: ZLayer[UserRepo, Nothing, AuthService] = ZLayer.fromFunction(JwtAuthService(_))
}
