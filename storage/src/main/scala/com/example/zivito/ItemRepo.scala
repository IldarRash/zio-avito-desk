package com.example.zivito

import java.util.UUID

import com.example.zivito.Domain.Item
import zio.{Task, ZIO}

trait ItemRepo {
  def getByCategoryID(categoryId: UUID): Task[Seq[Item]]

  def create(item: Item): Task[Item]

  def delete(id: UUID): Task[Unit]
}
object ItemRepo {
  def getByCategoryID(categoryId: UUID): ZIO[ItemRepo, Throwable, Seq[Item]] =
    ZIO.serviceWithZIO[ItemRepo](_.getByCategoryID(categoryId))

  def create(item: Item): ZIO[ItemRepo, Throwable, Item] =
    ZIO.serviceWithZIO[ItemRepo](_.create(item))

  def delete(id: UUID): ZIO[ItemRepo, Throwable, Unit] =
    ZIO.serviceWithZIO[ItemRepo](_.delete(id))
}

