package com.example.zivito

import zio._
import java.util.UUID

trait CategoryService {
  def createCategory(category: Domain.Category): Task[Domain.Category]
  def getCategory(id: UUID): Task[Option[Domain.Category]]
  def getAllCategories: Task[List[Domain.Category]]
  def updateCategory(category: Domain.Category): Task[Domain.Category]
  def deleteCategory(id: UUID): Task[Boolean]
}