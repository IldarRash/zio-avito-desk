package com.example.zivito

import java.util.UUID

import zio.schema.{DeriveSchema, Schema}

object DynamoDbModel {

  case class Item(
                   id: UUID,
                   name: String,
                   categoryId: UUID,
                 )

  object Item {
    implicit lazy val schema: Schema[Item] = DeriveSchema.gen[Item]
  }

  case class Category(
                       id: UUID,
                       name: String,
                     )

  object Category {
    implicit lazy val schema: Schema[Category] = DeriveSchema.gen[Category]
  }

  case class User(
                   id: UUID,
                   name: String,
                   email: String
                 )


  object User {
    implicit lazy val schema: Schema[User] = DeriveSchema.gen[User]
  }

}