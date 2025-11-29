package com.example.zivito

import com.example.zivito.Domain.Order
import zio.{Task, ZLayer}
import java.util.UUID

trait OrderService {
  def getOrders(userId: UUID): Task[List[Order]]
}

final case class OrderServiceImpl(repo: OrderRepo) extends OrderService {
  override def getOrders(userId: UUID): Task[List[Order]] = repo.listByUser(userId).map(_.toList)
}

object OrderServiceImpl {
  val layer: ZLayer[OrderRepo, Nothing, OrderService] = ZLayer.fromFunction(OrderServiceImpl(_))
}

