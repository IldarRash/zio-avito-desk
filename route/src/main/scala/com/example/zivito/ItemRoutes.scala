package com.example.zivito

import zio.*
import zio.json.{uuid as _, *}
import zio.http.*
import java.util.UUID

object ItemRoutes {

  /** Represents the request to create an item.
    *
    * @param name
    *   The name of the item.
    * @param description
    *   The description of the item.
    * @param price
    *   The price of the item.
    * @param categoryId
    *   The ID of the category the item belongs to.
    * @param location
    *   The location of the item.
    * @param imageUrl
    *   The optional URL of the item's photo.
    */
  final case class CreateItemRequest(
      name: String,
      description: String,
      price: BigDecimal,
      categoryId: UUID,
      location: String,
      imageUrl: Option[String]
  ) derives JsonDecoder

  val routes: Routes[ItemService, Throwable] =
    Routes(
      Method.GET / "items" -> handler { (_: Request) =>
        ZIO.serviceWithZIO[ItemService](_.getAll.map(items => Response.json(items.toJson)))
      },
      Method.GET / "items" / "search" / string("query") -> handler { (query: String, _: Request) =>
        ZIO.serviceWithZIO[ItemService](_.search(query).map(items => Response.json(items.toJson)))
      },
      Method.GET / "items" / "category" / uuid("categoryId") -> handler { (categoryId: UUID, _: Request) =>
        ZIO.serviceWithZIO[ItemService](_.getItemsByCategory(categoryId).map(items => Response.json(items.toJson)))
      },
      Method.GET / "items" / uuid("id") -> handler { (id: UUID, _: Request) =>
        ZIO.serviceWithZIO[ItemService](_.get(id)).map {
          case Some(value) => Response.json(value.toJson)
          case None        => Response.status(Status.NotFound)
        }
      },
      Method.POST / "items" -> handler { (req: Request) =>
        for {
          body    <- req.body.asString
          request <- ZIO.fromEither(body.fromJson[CreateItemRequest]).mapError(new RuntimeException(_))
          item <- ZIO.serviceWithZIO[ItemService](
            _.create(
              Domain.Item(
                new UUID(0L, 0L),
                request.name,
                request.description,
                request.price,
                request.categoryId,
                request.location,
                request.imageUrl.getOrElse("")
              )
            )
          )
        } yield Response.json(item.toJson)
      },
      Method.DELETE / "items" / uuid("id") -> handler { (id: UUID, _: Request) =>
        ZIO.serviceWithZIO[ItemService](_.delete(id)).as(Response.ok)
      }
    )
}
