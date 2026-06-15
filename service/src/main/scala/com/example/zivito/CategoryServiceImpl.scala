package com.example.zivito

import zio._
import java.util.UUID

case class CategoryServiceImpl(repo: CategoryRepo) extends CategoryService {

  override def createCategory(category: Domain.Category): Task[Domain.Category] =
    repo.create(category)

  override def getCategory(id: UUID): Task[Option[Domain.Category]] =
    repo.get(id)

  override def getAllCategories: Task[List[Domain.Category]] =
    repo.getAll.map(_.toList)

  override def updateCategory(category: Domain.Category): Task[Domain.Category] =
    repo.update(category)

  override def deleteCategory(id: UUID): Task[Boolean] =
    repo.delete(id)
}

object CategoryServiceImpl {
  def layer: ZLayer[CategoryRepo, Nothing, CategoryService] =
    ZLayer.fromFunction(CategoryServiceImpl(_))
}
