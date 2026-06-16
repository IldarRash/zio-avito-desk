package com.example.zivito

import java.time.Instant
import java.util.UUID

import com.example.zivito.Domain.User
import io.getquill.*
import javax.sql.DataSource
import zio.{Task, ZLayer}

case class UserTable(id: UUID, email: String, passwordHash: String, displayName: String, createdAt: Instant)

case class UserRepoPersist(ds: DataSource) extends UserRepo {

  val ctx = new PostgresZioJdbcContext(Escape)

  import ctx.*

  private val toUser = (t: UserTable) => User(t.id, t.email, t.displayName, t.createdAt)

  override def create(user: User, passwordHash: String): Task[User] =
    ctx
      .run(query[UserTable].insertValue(lift(UserTable(user.id, user.email, passwordHash, user.displayName, user.createdAt))))
      .as(user)
      .provide(ZLayer.succeed(ds))

  override def get(id: UUID): Task[Option[User]] =
    ctx
      .run(query[UserTable].filter(_.id == lift(id)))
      .map(_.headOption.map(toUser))
      .provide(ZLayer.succeed(ds))

  override def findByEmail(email: String): Task[Option[UserRepo.Credentials]] =
    ctx
      .run(query[UserTable].filter(_.email == lift(email)))
      .map(_.headOption.map(t => UserRepo.Credentials(toUser(t), t.passwordHash)))
      .provide(ZLayer.succeed(ds))
}

object UserRepoPersist {
  def layer: ZLayer[DataSource, Nothing, UserRepo] =
    ZLayer.fromFunction(UserRepoPersist(_))
}
