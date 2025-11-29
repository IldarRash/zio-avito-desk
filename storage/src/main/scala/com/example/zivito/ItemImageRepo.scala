package com.example.zivito

import com.example.zivito.Domain.ItemImage
import zio.{Task, ZIO}
import java.util.UUID

trait ItemImageRepo {
  def listByItem(itemId: UUID): Task[Seq[ItemImage]]
  def add(image: ItemImage): Task[ItemImage]
  def setCover(itemId: UUID, imageId: UUID): Task[Unit]
  def delete(id: UUID): Task[Unit]
}

object ItemImageRepo {
  def listByItem(itemId: UUID): ZIO[ItemImageRepo, Throwable, Seq[ItemImage]] = ZIO.serviceWithZIO[ItemImageRepo](_.listByItem(itemId))
  def add(image: ItemImage): ZIO[ItemImageRepo, Throwable, ItemImage] = ZIO.serviceWithZIO[ItemImageRepo](_.add(image))
  def setCover(itemId: UUID, imageId: UUID): ZIO[ItemImageRepo, Throwable, Unit] = ZIO.serviceWithZIO[ItemImageRepo](_.setCover(itemId, imageId))
  def delete(id: UUID): ZIO[ItemImageRepo, Throwable, Unit] = ZIO.serviceWithZIO[ItemImageRepo](_.delete(id))
}

