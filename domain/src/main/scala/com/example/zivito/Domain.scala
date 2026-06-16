package com.example.zivito

import java.time.Instant
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
      imageUrl: String,
      createdAt: Instant,
      ownerId: Option[UUID]
  ) derives JsonEncoder,
        JsonDecoder

  /** A registered user. Deliberately excludes the password hash so it is safe to serialize to clients.
    * @param id
    *   The unique identifier of the user.
    * @param email
    *   The user's email (login).
    * @param displayName
    *   The name shown on the user's listings.
    * @param createdAt
    *   When the account was created.
    */
  final case class User(
      id: UUID,
      email: String,
      displayName: String,
      createdAt: Instant
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

  /** Sort order for item listings. */
  enum SortOrder:
    case Newest, PriceAsc, PriceDesc

  object SortOrder:
    def fromString(s: String): Option[SortOrder] =
      s.trim.toLowerCase match
        case "newest"     => Some(Newest)
        case "price_asc"  => Some(PriceAsc)
        case "price_desc" => Some(PriceDesc)
        case _            => None

  /** Filter / sort / pagination criteria for listing items. */
  final case class ItemFilter(
      categoryId: Option[UUID] = None,
      query: Option[String] = None,
      minPrice: Option[BigDecimal] = None,
      maxPrice: Option[BigDecimal] = None,
      location: Option[String] = None,
      sort: SortOrder = SortOrder.Newest,
      limit: Int = 24,
      offset: Int = 0
  )

  /** A page of results plus the total count matching the filter (ignoring limit/offset), so clients can render pagination controls.
    */
  final case class Page[A](items: List[A], total: Long, limit: Int, offset: Int) derives JsonEncoder
}
