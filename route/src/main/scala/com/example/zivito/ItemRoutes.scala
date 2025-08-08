package com.example.zivito

import zio._
import zio.json._
import zhttp.http._
import zhttp.http.Method._
import java.util.UUID

object ItemRoutes {

  /**
   * Represents the request to create an item.
   *
   * @param name The name of the item.
   * @param description The description of the item.
   * @param price The price of the item.
   * @param categoryId The ID of the category the item belongs to.
   * @param location The location of the item.
   */
  case class CreateItemRequest(name: String, description: String, price: BigDecimal, categoryId: UUID, location: String)

  object CreateItemRequest {
    implicit val decoder: JsonDecoder[CreateItemRequest] = DeriveJsonDecoder.gen[CreateItemRequest]
  }

  def routes: HttpApp[ItemService, Throwable] =
    Http.collectZIO[Request] {
      
      case GET -> Root / "items" =>
        ZIO.serviceWithZIO[ItemService](_.getAll.map(items => Response.json(items.toJson)))

      case GET -> Root / "items" / "search" / query =>
        ZIO.serviceWithZIO[ItemService](_.search(query).map(items => Response.json(items.toJson)))

      case GET -> Root / "items" / id =>
        ZIO.attempt(java.util.UUID.fromString(id)).flatMap { uuid =>
          ZIO.serviceWithZIO[ItemService](_.get(uuid).map {
            case Some(item) => Response.json(item.toJson)
            case None       => Response.status(Status.NotFound)
          })
        }

      case req @ POST -> Root / "items" =>
        for {
          createItem <- req.bodyAsString.flatMap(body => ZIO.fromEither(body.fromJson[CreateItemRequest]))
          item <- ZIO.serviceWithZIO[ItemService](_.create(Domain.Item(UUID.randomUUID(), createItem.name, createItem.description, createItem.price, createItem.categoryId, createItem.location)))
        } yield Response.json(item.toJson)

      case DELETE -> Root / "items" / id =>
        ZIO.attempt(UUID.fromString(id)).flatMap { uuid =>
          ZIO.serviceWithZIO[ItemService](_.delete(uuid)).as(Response.ok)
        }
      
      case GET -> Root / "items" / "category" / id =>
        ZIO.attempt(UUID.fromString(id)).flatMap { catId =>
          ZIO.serviceWithZIO[ItemService](_.getItemsByCategory(catId).map(items => Response.json(items.toJson)))
        }
    }
}