package com.example.zivito

import java.time.Instant
import java.util.UUID

import io.getquill.*
import io.getquill.extras.InstantOps
import javax.sql.DataSource
import zio.{Task, ZLayer}

case class SessionTable(token: String, userId: UUID, createdAt: Instant, expiresAt: Instant)

case class SessionRepoPersist(ds: DataSource) extends SessionRepo {

  val ctx = new PostgresZioJdbcContext(Escape)

  import ctx.*

  override def create(token: String, userId: UUID, createdAt: Instant, expiresAt: Instant): Task[Unit] =
    ctx
      .run(query[SessionTable].insertValue(lift(SessionTable(token, userId, createdAt, expiresAt))))
      .unit
      .provide(ZLayer.succeed(ds))

  override def userIdFor(token: String, now: Instant): Task[Option[UUID]] =
    ctx
      .run(query[SessionTable].filter(s => s.token == lift(token) && s.expiresAt > lift(now)))
      .map(_.headOption.map(_.userId))
      .provide(ZLayer.succeed(ds))

  override def delete(token: String): Task[Unit] =
    ctx
      .run(query[SessionTable].filter(_.token == lift(token)).delete)
      .unit
      .provide(ZLayer.succeed(ds))
}

object SessionRepoPersist {
  def layer: ZLayer[DataSource, Nothing, SessionRepo] =
    ZLayer.fromFunction(SessionRepoPersist(_))
}
