package com.example.zivito

import zio._
import java.util.UUID

import com.example.zivito.Domain.{Item, ItemFilter, Page}

case class ItemServiceImpl(repo: ItemRepo) extends ItemService {

  override def create(item: Item): Task[Item] = repo.create(item)

  override def update(item: Item): Task[Option[Item]] = repo.update(item)

  override def get(id: UUID): Task[Option[Item]] = repo.get(id)

  override def list(filter: ItemFilter): Task[Page[Item]] = repo.list(filter)

  override def getAll: Task[List[Item]] =
    repo.list(ItemFilter(limit = Int.MaxValue)).map(_.items)

  override def search(query: String): Task[List[Item]] =
    repo.list(ItemFilter(query = Some(query), limit = Int.MaxValue)).map(_.items)

  override def getItemsByCategory(categoryId: UUID): Task[List[Item]] =
    repo.list(ItemFilter(categoryId = Some(categoryId), limit = Int.MaxValue)).map(_.items)

  override def delete(id: UUID): Task[Boolean] = repo.delete(id)
}

object ItemServiceImpl {
  def layer: ZLayer[ItemRepo, Nothing, ItemService] =
    ZLayer.fromFunction(ItemServiceImpl(_))
}
