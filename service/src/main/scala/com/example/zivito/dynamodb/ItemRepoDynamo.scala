package com.example.zivito.dynamodb
import java.util.UUID

import com.example.zivito.DynamoDbModel._
import zio.Task
import zio.dynamodb.DynamoDBQuery.{deleteItem, get, put}

class ItemRepoDynamo extends ItemRepo {
  override def getByCategoryID(categoryId: UUID): Task[Seq[Item]] =
    get("items").execute

  override def create(item: Item): Task[Item] =
    put("items", item).execute

  override def delete(id: UUID): Task[Unit] = deleteItem("items", id)
}
