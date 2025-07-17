package com.example.zivito

import java.util.UUID
import zio.json._

object Domain {

  /**
   * Represents an item on the board.
   * @param id The unique identifier of the item.
   * @param name The name of the item.
   * @param description The description of the item.
   * @param price The price of the item.
   * @param categoryId The identifier of the category the item belongs to.
   * @param location The location of the item.
   */
  case class Item(
                   id: UUID,
                   name: String,
                   description: String,
                   price: BigDecimal,
                   categoryId: UUID,
                   location: String
                 )

  object Item {
    implicit val encoder: JsonEncoder[Item] = DeriveJsonEncoder.gen[Item]
    implicit val decoder: JsonDecoder[Item] = DeriveJsonDecoder.gen[Item]
  }

  /**
   * Represents a category for items.
   * @param id The unique identifier of the category.
   * @param name The name of the category.
   */
  case class Category(
                       id: UUID,
                       name: String,
                     )

  object Category {
    implicit val encoder: JsonEncoder[Category] = DeriveJsonEncoder.gen[Category]
    implicit val decoder: JsonDecoder[Category] = DeriveJsonDecoder.gen[Category]
  }

  /**
   * Represents a user.
   * @param id The unique identifier of the user.
   * @param name The name of the user.
   * @param email The email of the user.
   */
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