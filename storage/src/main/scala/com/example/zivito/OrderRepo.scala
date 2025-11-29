package com.example.zivito

import com.example.zivito.Domain.Order
import zio.{Task, ZIO}
import java.util.UUID

trait OrderRepo {
  def create(order: Order): Task[Order]
  def get(id: UUID): Task[Option[Order]]
  def listByUser(userId: UUID): Task[Seq[Order]]
}

object OrderRepo {
  def create(order: Order): ZIO[OrderRepo, Throwable, Order] = ZIO.serviceWithZIO[OrderRepo](_.create(order))
  def get(id: UUID): ZIO[OrderRepo, Throwable, Option[Order]] = ZIO.serviceWithZIO[OrderRepo](_.get(id))
  def listByUser(userId: UUID): ZIO[OrderRepo, Throwable, Seq[Order]] = ZIO.serviceWithZIO[OrderRepo](_.listByUser(userId))
}

