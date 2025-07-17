package com.example.zivito

import zio._
import java.util.UUID

trait ItemService {
  def createItem(item: Domain.Item): Task[Domain.Item]
  def getItem(id: UUID): Task[Option[Domain.Item]]
  def getAllItems: Task[List[Domain.Item]]
  def updateItem(item: Domain.Item): Task[Domain.Item]
  def deleteItem(id: UUID): Task[Boolean]
  def getItemsByCategory(categoryId: UUID): Task[List[Domain.Item]]
}