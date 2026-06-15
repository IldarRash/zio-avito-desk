package com.example.zivito

import java.util.UUID
import zio.json.*

object Domain {

  /** Represents an item on the board.
    * @param id
    *   The unique identifier of the item.
    * @param name
    *   The name of the item.
    * @param description
    *   The description of the item.
    * @param price
    *   The price of the item.
    * @param categoryId
    *   The identifier of the category the item belongs to.
    * @param location
    *   The location of the item.
    * @param imageUrl
    *   The URL of the item's photo (empty string when none).
    */
  final case class Item(
      id: UUID,
      name: String,
      description: String,
      price: BigDecimal,
      categoryId: UUID,
      location: String,
      imageUrl: String
  ) derives JsonEncoder,
        JsonDecoder

  /** Represents a category for items.
    * @param id
    *   The unique identifier of the category.
    * @param name
    *   The name of the category.
    */
  final case class Category(
      id: UUID,
      name: String
  ) derives JsonEncoder,
        JsonDecoder
}
