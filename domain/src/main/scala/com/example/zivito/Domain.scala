package com.example.zivito

import java.util.UUID
import java.time.Instant
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

  /**
   * Input filters for searching items.
   */
  final case class ItemSearchFilters(
    keywords: Option[String] = None,
    categoryId: Option[UUID] = None,
    minPrice: Option[BigDecimal] = None,
    maxPrice: Option[BigDecimal] = None,
    location: Option[String] = None
  )

  object ItemSearchFilters {
    implicit val encoder: JsonEncoder[ItemSearchFilters] = DeriveJsonEncoder.gen[ItemSearchFilters]
    implicit val decoder: JsonDecoder[ItemSearchFilters] = DeriveJsonDecoder.gen[ItemSearchFilters]
  }

  /**
   * Represents a chat dialog between two users.
   */
  final case class Dialog(
    id: UUID,
    userAId: UUID,
    userBId: UUID,
    lastMessageAt: Instant
  )

  object Dialog {
    implicit val encoder: JsonEncoder[Dialog] = DeriveJsonEncoder.gen[Dialog]
    implicit val decoder: JsonDecoder[Dialog] = DeriveJsonDecoder.gen[Dialog]
  }

  /**
   * Represents a chat message sent in a dialog.
   */
  final case class ChatMessage(
    id: UUID,
    dialogId: UUID,
    senderId: UUID,
    text: String,
    createdAt: Instant
  )

  object ChatMessage {
    implicit val encoder: JsonEncoder[ChatMessage] = DeriveJsonEncoder.gen[ChatMessage]
    implicit val decoder: JsonDecoder[ChatMessage] = DeriveJsonDecoder.gen[ChatMessage]
  }

  /**
   * Represents an image attached to an item.
   * @param id Unique image identifier
   * @param itemId Item this image belongs to
   * @param url Public URL or path to the image
   * @param isCover Whether this image is a cover for the item
   * @param order Display order among item's images
   */
  final case class ItemImage(
    id: UUID,
    itemId: UUID,
    url: String,
    isCover: Boolean = false,
    order: Int = 0
  )

  object ItemImage {
    implicit val encoder: JsonEncoder[ItemImage] = DeriveJsonEncoder.gen[ItemImage]
    implicit val decoder: JsonDecoder[ItemImage] = DeriveJsonDecoder.gen[ItemImage]
  }

  /**
   * Represents an item in a user's shopping cart.
   */
  final case class CartItem(
    userId: UUID,
    itemId: UUID
  )

  object CartItem {
    implicit val encoder: JsonEncoder[CartItem] = DeriveJsonEncoder.gen[CartItem]
    implicit val decoder: JsonDecoder[CartItem] = DeriveJsonDecoder.gen[CartItem]
  }

  /**
   * Represents an order placed by a user.
   */
  final case class Order(
    id: UUID,
    userId: UUID,
    itemIds: List[UUID],
    totalPrice: BigDecimal,
    status: String, // e.g., "CREATED", "PAID", "DELIVERED"
    createdAt: Instant
  )

  object Order {
    implicit val encoder: JsonEncoder[Order] = DeriveJsonEncoder.gen[Order]
    implicit val decoder: JsonDecoder[Order] = DeriveJsonDecoder.gen[Order]
  }
}