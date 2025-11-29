package com.example.zivito

import com.example.zivito.Domain.Order
import io.getquill.{Escape, H2ZioJdbcContext}
import io.getquill.context.ZioJdbc.DataSourceLayer
import javax.sql.DataSource
import zio.{Task, ZLayer, ZIO}
import java.util.UUID
import java.time.Instant

final case class OrderTable(id: UUID, userId: UUID, totalPrice: BigDecimal, status: String, createdAt: Instant)
final case class OrderItemTable(orderId: UUID, itemId: UUID)

final case class OrderRepoPersist(ds: DataSource) extends OrderRepo {
  private val ctx = new H2ZioJdbcContext(Escape)
  import ctx._

  override def create(order: Order): Task[Order] = {
    val orderRow = OrderTable(order.id, order.userId, order.totalPrice, order.status, order.createdAt)
    val itemRows = order.itemIds.map(itemId => OrderItemTable(order.id, itemId))

    transaction {
      for {
        _ <- ctx.run(query[OrderTable].insertValue(lift(orderRow)))
        _ <- ctx.run(liftQuery(itemRows).foreach(i => query[OrderItemTable].insertValue(i)))
      } yield order
    }.provide(ZLayer.succeed(ds))
  }

  override def get(id: UUID): Task[Option[Order]] = {
    transaction {
      for {
        orderRow <- ctx.run(query[OrderTable].filter(_.id == lift(id))).map(_.headOption)
        items    <- ctx.run(query[OrderItemTable].filter(_.orderId == lift(id)).map(_.itemId))
      } yield orderRow.map(o => Order(o.id, o.userId, items.toList, o.totalPrice, o.status, o.createdAt))
    }.provide(ZLayer.succeed(ds))
  }

  override def listByUser(userId: UUID): Task[Seq[Order]] = {
    transaction {
      for {
        orders <- ctx.run(query[OrderTable].filter(_.userId == lift(userId)).sortBy(_.createdAt)(Ord.desc))
        // This N+1 is not ideal but simple for now. Ideally use join or group by.
        result <- ZIO.foreach(orders) { o =>
          ctx.run(query[OrderItemTable].filter(_.orderId == lift(o.id)).map(_.itemId))
             .map(items => Order(o.id, o.userId, items.toList, o.totalPrice, o.status, o.createdAt))
        }
      } yield result
    }.provide(ZLayer.succeed(ds))
  }
}

object OrderRepoPersist {
  def layer: ZLayer[DataSource, Throwable, OrderRepo] =
    DataSourceLayer.fromPrefix("App") >>> ZLayer.fromFunction(OrderRepoPersist(_))
}

