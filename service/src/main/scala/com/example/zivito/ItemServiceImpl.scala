package com.example.zivito

import zio._
import java.util.UUID

case class ItemServiceImpl(repo: ItemRepo) extends ItemService {

  override def create(item: Domain.Item): Task[Domain.Item] = repo.create(item)

  override def get(id: UUID): Task[Option[Domain.Item]] = repo.get(id)

  override def getAll: Task[List[Domain.Item]] = repo.getAll.map(_.toList)

  override def search(query: String): Task[List[Domain.Item]] = repo.search(query).map(_.toList)

  override def delete(id: UUID): Task[Unit] = repo.delete(id)

  override def getItemsByCategory(categoryId: UUID): Task[List[Domain.Item]] =
    repo.getByCategoryID(categoryId).map(_.toList)
}

object ItemServiceImpl {
  def layer: ZLayer[ItemRepo, Nothing, ItemService] =
    ZLayer.fromFunction(ItemServiceImpl(_))
}