package com.example.zivito

import java.util.UUID
import zio.json._

object Domain {

  case class Item(
                   id: UUID,
                   name: String,
                   categoryId: UUID,
                 )

  object Item {
    implicit val encoder: JsonEncoder[Item] = DeriveJsonEncoder.gen[Item]
    implicit val decoder: JsonDecoder[Item] = DeriveJsonDecoder.gen[Item]
  }

  case class Category(
                       id: UUID,
                       name: String,
                     )

  object Category {
    implicit val encoder: JsonEncoder[Category] = DeriveJsonEncoder.gen[Category]
    implicit val decoder: JsonDecoder[Category] = DeriveJsonDecoder.gen[Category]
  }

  case class User(
                   id: UUID,
                   name: String,
                   email: String
                 )

  object User {
    implicit val encoder: JsonEncoder[User] = DeriveJsonEncoder.gen[User]
    implicit val decoder: JsonDecoder[User] = DeriveJsonDecoder.gen[User]
  }
}