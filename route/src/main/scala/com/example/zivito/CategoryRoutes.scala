package com.example.zivito

import zio.*
import zio.json.{uuid as _, *}
import zio.http.*
import java.util.UUID

object CategoryRoutes {
  final case class CreateCategoryRequest(name: String) derives JsonDecoder
  final case class UpdateCategoryRequest(id: UUID, name: String) derives JsonDecoder

  private def parseBody[A](req: Request)(using JsonDecoder[A]): ZIO[Any, Throwable, A] =
    req.body.asString.flatMap(b => ZIO.fromEither(b.fromJson[A]).mapError(m => AppError.Validation("body", s"is not valid JSON: $m")))

  val publicRoutes: Routes[CategoryService, Throwable] =
    Routes(
      Method.GET / "categories" -> handler { (_: Request) =>
        ZIO.serviceWithZIO[CategoryService](_.getAllCategories.map(categories => Response.json(categories.toJson)))
      },
      Method.GET / "categories" / uuid("id") -> handler { (id: UUID, _: Request) =>
        ZIO.serviceWithZIO[CategoryService](_.getCategory(id)).someOrFail(AppError.NotFound("Category")).map(c => Response.json(c.toJson))
      }
    )

  val protectedRoutes: Routes[CategoryService, Throwable] =
    Routes(
      Method.POST / "categories" -> handler { (req: Request) =>
        for {
          request <- parseBody[CreateCategoryRequest](req)
          name    <- Validate.text("name", request.name, 255)
          id      <- Random.nextUUID
          created <- ZIO.serviceWithZIO[CategoryService](_.createCategory(Domain.Category(id, name)))
        } yield Response.json(created.toJson).status(Status.Created)
      },
      Method.PUT / "categories" -> handler { (req: Request) =>
        for {
          request <- parseBody[UpdateCategoryRequest](req)
          name    <- Validate.text("name", request.name, 255)
          _       <- ZIO.serviceWithZIO[CategoryService](_.getCategory(request.id)).someOrFail(AppError.NotFound("Category"))
          updated <- ZIO.serviceWithZIO[CategoryService](_.updateCategory(Domain.Category(request.id, name)))
        } yield Response.json(updated.toJson)
      },
      Method.DELETE / "categories" / uuid("id") -> handler { (id: UUID, _: Request) =>
        ZIO
          .serviceWithZIO[CategoryService](_.deleteCategory(id))
          .map(deleted => if (deleted) Response.status(Status.NoContent) else ApiError.toResponse(AppError.NotFound("Category")))
      }
    )
}
