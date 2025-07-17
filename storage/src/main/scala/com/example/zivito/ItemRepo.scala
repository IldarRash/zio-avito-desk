package com.example.zivito

import java.util.UUID

import com.example.zivito.Domain.Item
import zio.{Task, ZIO}

trait ItemRepo {

  /**
   * Retrieves an item by its ID.
   * @param id The ID of the item to retrieve.
   * @return A Task that resolves to an Option of the Item.
   */
  def get(id: UUID): Task[Option[Item]]

  /**
   * Retrieves all items.
   * @return A Task that resolves to a sequence of all items.
   */
  def getAll: Task[Seq[Item]]


  /**
   * Searches for items based on a query.
   * @param query The search query.
   * @return A Task that resolves to a sequence of matching items.
   */
  def search(query: String): Task[Seq[Item]]

  def getByCategoryID(categoryId: UUID): Task[Seq[Item]]

  def create(item: Item): Task[Item]

  def delete(id: UUID): Task[Unit]
}
object ItemRepo {

  def get(id: UUID): ZIO[ItemRepo, Throwable, Option[Item]] =
    ZIO.serviceWithZIO[ItemRepo](_.get(id))

  def getAll: ZIO[ItemRepo, Throwable, Seq[Item]] =
    ZIO.serviceWithZIO[ItemRepo](_.getAll)

  def search(query: String): ZIO[ItemRepo, Throwable, Seq[Item]] =
    ZIO.serviceWithZIO[ItemRepo](_.search(query))

  def getByCategoryID(categoryId: UUID): ZIO[ItemRepo, Throwable, Seq[Item]] =
    ZIO.serviceWithZIO[ItemRepo](_.getByCategoryID(categoryId))

  def create(item: Item): ZIO[ItemRepo, Throwable, Item] =
    ZIO.serviceWithZIO[ItemRepo](_.create(item))

  def delete(id: UUID): ZIO[ItemRepo, Throwable, Unit] =
    ZIO.serviceWithZIO[ItemRepo](_.delete(id))
}

