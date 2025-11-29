package com.example.zivito

import com.example.zivito.Domain.CartItem
import io.getquill.{Escape, H2ZioJdbcContext}
import io.getquill.context.ZioJdbc.DataSourceLayer
import javax.sql.DataSource
import zio.{Task, ZLayer}
import java.util.UUID

final case class CartItemTable(userId: UUID, itemId: UUID)

final case class CartRepoPersist(ds: DataSource) extends CartRepo {
  private val ctx = new H2ZioJdbcContext(Escape)
  import ctx._

  override def addItem(userId: UUID, itemId: UUID): Task[Unit] =
    ctx.run(query[CartItemTable].insertValue(lift(CartItemTable(userId, itemId)))).unit.provide(ZLayer.succeed(ds))

  override def removeItem(userId: UUID, itemId: UUID): Task[Unit] =
    ctx.run(query[CartItemTable].filter(c => c.userId == lift(userId) && c.itemId == lift(itemId)).delete).unit.provide(ZLayer.succeed(ds))

  override def getItems(userId: UUID): Task[Seq[UUID]] =
    ctx.run(query[CartItemTable].filter(_.userId == lift(userId)).map(_.itemId)).provide(ZLayer.succeed(ds))

  override def clearCart(userId: UUID): Task[Unit] =
    ctx.run(query[CartItemTable].filter(_.userId == lift(userId)).delete).unit.provide(ZLayer.succeed(ds))
}

object CartRepoPersist {
  def layer: ZLayer[DataSource, Throwable, CartRepo] =
    DataSourceLayer.fromPrefix("App") >>> ZLayer.fromFunction(CartRepoPersist(_))
}

