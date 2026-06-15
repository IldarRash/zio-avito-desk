package com.example.zivito

import zio.*
import zio.json.{uuid as _, *}
import zio.http.*
import java.util.UUID

object CategoryRoutes {
  final case class CreateCategoryRequest(name: String) derives JsonDecoder
  final case class UpdateCategoryRequest(id: UUID, name: String) derives JsonDecoder

  val routes: Routes[CategoryService, Throwable] =
    Routes(
      Method.GET / "categories" -> handler { (_: Request) =>
        ZIO.serviceWithZIO[CategoryService](_.getAllCategories.map(categories => Response.json(categories.toJson)))
      },
      Method.GET / "categories" / uuid("id") -> handler { (id: UUID, _: Request) =>
        ZIO.serviceWithZIO[CategoryService](_.getCategory(id)).map {
          case Some(value) => Response.json(value.toJson)
          case None        => Response.status(Status.NotFound)
        }
      },
      Method.POST / "categories" -> handler { (req: Request) =>
        for {
          body    <- req.body.asString
          request <- ZIO.fromEither(body.fromJson[CreateCategoryRequest]).mapError(new RuntimeException(_))
          id      <- Random.nextUUID
          created <- ZIO.serviceWithZIO[CategoryService](_.createCategory(Domain.Category(id, request.name)))
        } yield Response.json(created.toJson)
      },
      Method.PUT / "categories" -> handler { (req: Request) =>
        for {
          body    <- req.body.asString
          request <- ZIO.fromEither(body.fromJson[UpdateCategoryRequest]).mapError(new RuntimeException(_))
          updated <- ZIO.serviceWithZIO[CategoryService](_.updateCategory(Domain.Category(request.id, request.name)))
        } yield Response.json(updated.toJson)
      },
      Method.DELETE / "categories" / uuid("id") -> handler { (id: UUID, _: Request) =>
        ZIO.serviceWithZIO[CategoryService](_.deleteCategory(id)).map(deleted => if (deleted) Response.ok else Response.status(Status.NotFound))
      }
    )
}
