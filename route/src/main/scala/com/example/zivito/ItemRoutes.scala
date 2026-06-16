package com.example.zivito

import zio.*
import zio.json.{uuid as _, *}
import zio.http.*
import java.util.UUID

import com.example.zivito.Domain.{Item, ItemFilter, SortOrder, User}

object ItemRoutes {

  /** Request body for creating or updating an item. `imageUrl` is optional and defaults to empty; images are usually attached via `POST /items/{id}/image`.
    */
  final case class ItemRequest(
      name: String,
      description: String,
      price: BigDecimal,
      categoryId: UUID,
      location: String,
      imageUrl: Option[String]
  ) derives JsonDecoder

  // ---- query-param parsing for the listing endpoint ---------------------------

  private def parseFilter(req: Request): IO[AppError, ItemFilter] = {
    val qp                             = req.url.queryParams
    def str(k: String): Option[String] = qp.queryParam(k).map(_.trim).filter(_.nonEmpty)

    def uuidParam(k: String): IO[AppError, Option[UUID]] =
      ZIO.foreach(str(k))(s => ZIO.attempt(UUID.fromString(s)).orElseFail(AppError.Validation(k, "must be a UUID")))
    def decimalParam(k: String): IO[AppError, Option[BigDecimal]] =
      ZIO.foreach(str(k))(s => ZIO.attempt(BigDecimal(s)).orElseFail(AppError.Validation(k, "must be a number")))
    def intParam(k: String, default: Int, min: Int, max: Int): IO[AppError, Int] =
      str(k) match {
        case None    => ZIO.succeed(default)
        case Some(s) => ZIO.attempt(s.toInt).orElseFail(AppError.Validation(k, "must be an integer")).map(_.max(min).min(max))
      }
    def sortParam: IO[AppError, SortOrder] =
      str("sort") match {
        case None    => ZIO.succeed(SortOrder.Newest)
        case Some(s) => ZIO.fromOption(SortOrder.fromString(s)).orElseFail(AppError.Validation("sort", "must be one of newest, price_asc, price_desc"))
      }

    for {
      categoryId <- uuidParam("categoryId")
      minPrice   <- decimalParam("minPrice")
      maxPrice   <- decimalParam("maxPrice")
      sort       <- sortParam
      limit      <- intParam("limit", default = 24, min = 1, max = 100)
      offset     <- intParam("offset", default = 0, min = 0, max = Int.MaxValue)
    } yield ItemFilter(
      categoryId = categoryId,
      query = str("q"),
      minPrice = minPrice,
      maxPrice = maxPrice,
      location = str("location"),
      sort = sort,
      limit = limit,
      offset = offset
    )
  }

  // ---- request validation & ownership ----------------------------------------

  /** Validates and normalizes an item request, and confirms the category exists. */
  private def validated(req: ItemRequest): ZIO[CategoryService, AppError, ItemRequest] =
    for {
      name        <- Validate.text("name", req.name, 255)
      description <- Validate.text("description", req.description, 2000)
      price       <- Validate.nonNegative("price", req.price)
      location    <- Validate.text("location", req.location, 255)
      imageUrl    <- Validate.optionalText("imageUrl", req.imageUrl.getOrElse(""), 1000)
      _ <- ZIO
        .serviceWithZIO[CategoryService](_.getCategory(req.categoryId))
        .mapError(t => AppError.Validation("categoryId", s"could not be verified: ${t.getMessage}"))
        .someOrFail(AppError.Validation("categoryId", "does not refer to an existing category"))
    } yield req.copy(name = name, description = description, price = price, location = location, imageUrl = Some(imageUrl))

  private def parseBody(req: Request): ZIO[Any, Throwable, ItemRequest] =
    req.body.asString.flatMap(b => ZIO.fromEither(b.fromJson[ItemRequest]).mapError(m => AppError.Validation("body", s"is not valid JSON: $m")))

  private def requireOwner(user: User, item: Item): IO[AppError, Unit] =
    ZIO.unless(item.ownerId.contains(user.id))(ZIO.fail(AppError.Forbidden("You can only modify your own listings"))).unit

  // ---- public routes (no authentication) -------------------------------------

  val publicRoutes: Routes[ItemService, Throwable] =
    Routes(
      Method.GET / "items" -> handler { (req: Request) =>
        for {
          filter <- parseFilter(req)
          page   <- ZIO.serviceWithZIO[ItemService](_.list(filter))
        } yield Response.json(page.toJson)
      },
      Method.GET / "items" / "search" / string("query") -> handler { (query: String, _: Request) =>
        ZIO.serviceWithZIO[ItemService](_.search(query).map(items => Response.json(items.toJson)))
      },
      Method.GET / "items" / "category" / uuid("categoryId") -> handler { (categoryId: UUID, _: Request) =>
        ZIO.serviceWithZIO[ItemService](_.getItemsByCategory(categoryId).map(items => Response.json(items.toJson)))
      },
      Method.GET / "items" / uuid("id") -> handler { (id: UUID, _: Request) =>
        ZIO.serviceWithZIO[ItemService](_.get(id)).someOrFail(AppError.NotFound("Item")).map(item => Response.json(item.toJson))
      }
    )

  // ---- protected routes (require a logged-in user) ---------------------------

  val protectedRoutes: Routes[ItemService & CategoryService & User, Throwable] =
    Routes(
      Method.POST / "items" -> handler { (req: Request) =>
        for {
          user   <- ZIO.service[User]
          parsed <- parseBody(req)
          valid  <- validated(parsed)
          id     <- Random.nextUUID
          now    <- Clock.instant
          item = Item(id, valid.name, valid.description, valid.price, valid.categoryId, valid.location, valid.imageUrl.getOrElse(""), now, Some(user.id))
          created <- ZIO.serviceWithZIO[ItemService](_.create(item))
        } yield Response.json(created.toJson).status(Status.Created)
      },
      Method.PUT / "items" / uuid("id") -> handler { (id: UUID, req: Request) =>
        for {
          user     <- ZIO.service[User]
          parsed   <- parseBody(req)
          valid    <- validated(parsed)
          existing <- ZIO.serviceWithZIO[ItemService](_.get(id)).someOrFail(AppError.NotFound("Item"))
          _        <- requireOwner(user, existing)
          updatedItem = existing.copy(
            name = valid.name,
            description = valid.description,
            price = valid.price,
            categoryId = valid.categoryId,
            location = valid.location,
            imageUrl = valid.imageUrl.getOrElse(existing.imageUrl)
          )
          saved <- ZIO.serviceWithZIO[ItemService](_.update(updatedItem)).someOrFail(AppError.NotFound("Item"))
        } yield Response.json(saved.toJson)
      },
      Method.POST / "items" / uuid("id") / "image" -> handler { (id: UUID, req: Request) =>
        for {
          user        <- ZIO.service[User]
          existing    <- ZIO.serviceWithZIO[ItemService](_.get(id)).someOrFail(AppError.NotFound("Item"))
          _           <- requireOwner(user, existing)
          contentType <- ZIO.fromOption(req.header(Header.ContentType)).orElseFail(AppError.Validation("Content-Type", "is required"))
          ext         <- Uploads.extensionFor(contentType.mediaType.fullType)
          bytes       <- req.body.asChunk
          _           <- ZIO.when(bytes.isEmpty)(ZIO.fail(AppError.Validation("image", "must not be empty")))
          _           <- ZIO.when(bytes.length > Uploads.maxBytes)(ZIO.fail(AppError.Validation("image", "must be at most 5 MB")))
          fileName = s"$id.$ext"
          _     <- Uploads.save(fileName, bytes)
          saved <- ZIO.serviceWithZIO[ItemService](_.update(existing.copy(imageUrl = s"/uploads/$fileName"))).someOrFail(AppError.NotFound("Item"))
        } yield Response.json(saved.toJson)
      },
      Method.DELETE / "items" / uuid("id") -> handler { (id: UUID, _: Request) =>
        for {
          user     <- ZIO.service[User]
          existing <- ZIO.serviceWithZIO[ItemService](_.get(id)).someOrFail(AppError.NotFound("Item"))
          _        <- requireOwner(user, existing)
          _        <- ZIO.serviceWithZIO[ItemService](_.delete(id))
        } yield Response.status(Status.NoContent)
      }
    )
}
