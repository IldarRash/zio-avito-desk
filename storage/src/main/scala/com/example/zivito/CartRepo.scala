package com.example.zivito

import com.example.zivito.Domain.CartItem
import zio.{Task, ZIO}
import java.util.UUID

trait CartRepo {
  def addItem(userId: UUID, itemId: UUID): Task[Unit]
  def removeItem(userId: UUID, itemId: UUID): Task[Unit]
  def getItems(userId: UUID): Task[Seq[UUID]]
  def clearCart(userId: UUID): Task[Unit]
}

object CartRepo {
  def addItem(userId: UUID, itemId: UUID): ZIO[CartRepo, Throwable, Unit] = ZIO.serviceWithZIO[CartRepo](_.addItem(userId, itemId))
  def removeItem(userId: UUID, itemId: UUID): ZIO[CartRepo, Throwable, Unit] = ZIO.serviceWithZIO[CartRepo](_.removeItem(userId, itemId))
  def getItems(userId: UUID): ZIO[CartRepo, Throwable, Seq[UUID]] = ZIO.serviceWithZIO[CartRepo](_.getItems(userId))
  def clearCart(userId: UUID): ZIO[CartRepo, Throwable, Unit] = ZIO.serviceWithZIO[CartRepo](_.clearCart(userId))
}

