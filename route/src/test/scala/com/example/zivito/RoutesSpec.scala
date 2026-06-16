package com.example.zivito

import com.example.zivito.Domain.{Category, Item, ItemFilter, Page, SortOrder, User}
import zio.*
import zio.http.*
import zio.json.*
import zio.test.*

import java.time.Instant
import java.util.UUID

/** HTTP-layer tests. Routes are driven directly (no real server bind) by attaching the production error handler [[ApiError.handle]] and calling `runZIO`, which
  * mirrors how `Main` assembles the app.
  *
  * `runZIO` requires the route's error channel to be `Response`; our routes are typed `Throwable`, so `handleErrorCauseZIO(ApiError.handle)` is applied first
  * (exactly as `Main` does), turning every failure into the JSON error response.
  *
  * The protected item routes require a `User` in the environment (handlers call `ZIO.service[User]`). We provide it via `ZLayer.succeed(user)` rather than
  * exercising the cookie auth aspect, which keeps these tests focused on the handler/validation/ownership logic. The auth aspect itself is covered by driving
  * it through `Main`-style wiring would require a real session; instead the auth flow is left to the service-level/repo tests.
  */
object RoutesSpec extends ZIOSpecDefault {

  // ---- in-memory repositories (route module test scope cannot see the ones in
  //      the service test scope, so they are re-declared here) ------------------

  final case class InMemItemRepo(ref: Ref[Map[UUID, Item]]) extends ItemRepo {
    def get(id: UUID): Task[Option[Item]] = ref.get.map(_.get(id))
    def list(filter: ItemFilter): Task[Page[Item]] =
      ref.get.map { items =>
        val filtered = items.values.toList.filter { i =>
          filter.categoryId.forall(_ == i.categoryId) &&
          filter.query.forall(q => i.name.toLowerCase.contains(q.toLowerCase) || i.description.toLowerCase.contains(q.toLowerCase))
        }
        val sorted = filter.sort match {
          case SortOrder.PriceAsc  => filtered.sortBy(_.price)
          case SortOrder.PriceDesc => filtered.sortBy(_.price).reverse
          case SortOrder.Newest    => filtered.sortWith((a, b) => a.createdAt.isAfter(b.createdAt))
        }
        Page(sorted.drop(filter.offset).take(filter.limit), sorted.size.toLong, filter.limit, filter.offset)
      }
    def create(item: Item): Task[Item] = ref.update(_ + (item.id -> item)).as(item)
    def update(item: Item): Task[Option[Item]] =
      ref.modify(m => if (m.contains(item.id)) (Some(item), m + (item.id -> item)) else (None, m))
    def delete(id: UUID): Task[Boolean] = ref.modify(m => (m.contains(id), m - id))
  }

  final case class InMemCategoryRepo(ref: Ref[Map[UUID, Category]]) extends CategoryRepo {
    def create(c: Category): Task[Category]   = ref.update(_ + (c.id -> c)).as(c)
    def get(id: UUID): Task[Option[Category]] = ref.get.map(_.get(id))
    def getAll: Task[Seq[Category]]           = ref.get.map(_.values.toSeq)
    def update(c: Category): Task[Category]   = ref.update(_ + (c.id -> c)).as(c)
    def delete(id: UUID): Task[Boolean]       = ref.modify(m => (m.contains(id), m - id))
  }

  // ---- fixtures ----------------------------------------------------------------

  private val categoryId = new UUID(0L, 1L)
  private val owner      = User(new UUID(0L, 100L), "owner@x.io", "Owner", Instant.EPOCH)
  private val stranger   = User(new UUID(0L, 200L), "other@x.io", "Other", Instant.EPOCH)

  private def item(id: UUID, name: String, price: BigDecimal, ownerId: Option[UUID]): Item =
    Item(id, name, "desc", price, categoryId, "NY", "", Instant.EPOCH, ownerId)

  /** A fresh stack of in-memory repos + real services, optionally pre-seeded. */
  private def env(
      items: List[Item] = Nil,
      categories: List[Category] = List(Category(categoryId, "Electronics"))
  ): ZLayer[Any, Nothing, ItemService & CategoryService] = {
    val itemRepo     = ZLayer.fromZIO(Ref.make(items.map(i => i.id -> i).toMap).map(InMemItemRepo(_)): UIO[ItemRepo])
    val categoryRepo = ZLayer.fromZIO(Ref.make(categories.map(c => c.id -> c).toMap).map(InMemCategoryRepo(_)): UIO[CategoryRepo])
    (itemRepo >>> ItemServiceImpl.layer) ++ (categoryRepo >>> CategoryServiceImpl.layer)
  }

  /** Runs a request against the public item routes with the production error handler attached.
    */
  private def publicItems(req: Request): URIO[ItemService & Scope, Response] =
    ItemRoutes.publicRoutes.handleErrorCauseZIO(ApiError.handle).runZIO(req)

  private def protectedItems(req: Request): URIO[ItemService & CategoryService & User & Scope, Response] =
    ItemRoutes.protectedRoutes.handleErrorCauseZIO(ApiError.handle).runZIO(req)

  private def jsonBody(value: String): Body = Body.fromString(value)

  /** `Domain.Page` only derives `JsonEncoder` (it is a response type); this local mirror lets the tests decode the listing response body.
    */
  private final case class PageView(items: List[Item], total: Long, limit: Int, offset: Int) derives JsonDecoder

  def spec = suite("RoutesSpec")(
    test("GET /health returns 200 with status ok") {
      for {
        res  <- HealthRoutes.routes.handleErrorCauseZIO(ApiError.handle).runZIO(Request.get("/health"))
        body <- res.body.asString
      } yield assertTrue(res.status == Status.Ok, body.fromJson[Map[String, String]] == Right(Map("status" -> "ok")))
    },
    test("GET /items returns a Page JSON with total") {
      val seeded = List(
        item(new UUID(0L, 10L), "Phone", BigDecimal(5), Some(owner.id)),
        item(new UUID(0L, 11L), "Laptop", BigDecimal(10), Some(owner.id))
      )
      (for {
        res  <- publicItems(Request.get("/items"))
        body <- res.body.asString
        page = body.fromJson[PageView]
      } yield assertTrue(res.status == Status.Ok, page.map(_.total) == Right(2L), page.map(_.items.size) == Right(2)))
        .provideSome[Scope](env(items = seeded))
    },
    test("GET /items/{id} for a missing id returns 404 with a JSON error body") {
      (for {
        res  <- publicItems(Request.get(s"/items/${new UUID(0L, 999L)}"))
        body <- res.body.asString
      } yield assertTrue(
        res.status == Status.NotFound,
        body.fromJson[Map[String, String]].exists(_.contains("error"))
      )).provideSome[Scope](env())
    },
    test("GET /items/{id} returns the item when present") {
      val it = item(new UUID(0L, 20L), "Camera", BigDecimal(3), Some(owner.id))
      (for {
        res  <- publicItems(Request.get(s"/items/${it.id}"))
        body <- res.body.asString
      } yield assertTrue(res.status == Status.Ok, body.fromJson[Item] == Right(it)))
        .provideSome[Scope](env(items = List(it)))
    },
    test("POST /items with an empty name returns 400 (validation)") {
      val payload =
        s"""{"name":"  ","description":"d","price":1.00,"categoryId":"$categoryId","location":"NY","imageUrl":null}"""
      (for {
        res  <- protectedItems(Request.post("/items", jsonBody(payload)))
        body <- res.body.asString
      } yield assertTrue(
        res.status == Status.BadRequest,
        body.fromJson[Map[String, String]].exists(_.get("error").exists(_.toLowerCase.contains("name")))
      )).provideSome[Scope](env(), ZLayer.succeed(owner))
    },
    test("POST /items with a valid body creates the item (201) owned by the caller") {
      val payload =
        s"""{"name":"New Item","description":"d","price":12.50,"categoryId":"$categoryId","location":"NY","imageUrl":null}"""
      (for {
        res  <- protectedItems(Request.post("/items", jsonBody(payload)))
        body <- res.body.asString
        created = body.fromJson[Item]
      } yield assertTrue(
        res.status == Status.Created,
        created.map(_.name) == Right("New Item"),
        created.map(_.ownerId) == Right(Some(owner.id))
      )).provideSome[Scope](env(), ZLayer.succeed(owner))
    },
    test("POST /items referencing an unknown category returns 400") {
      val unknownCat = new UUID(7L, 7L)
      val payload =
        s"""{"name":"X","description":"d","price":1.00,"categoryId":"$unknownCat","location":"NY","imageUrl":null}"""
      (for {
        res  <- protectedItems(Request.post("/items", jsonBody(payload)))
        body <- res.body.asString
      } yield assertTrue(
        res.status == Status.BadRequest,
        body.fromJson[Map[String, String]].exists(_.get("error").exists(_.toLowerCase.contains("category")))
      )).provideSome[Scope](env(), ZLayer.succeed(owner))
    },
    test("PUT /items/{id} by a non-owner returns 403") {
      val it = item(new UUID(0L, 30L), "Owned", BigDecimal(5), Some(owner.id))
      val payload =
        s"""{"name":"Renamed","description":"d","price":5.00,"categoryId":"$categoryId","location":"NY","imageUrl":null}"""
      (for {
        res  <- protectedItems(Request.put(s"/items/${it.id}", jsonBody(payload)))
        body <- res.body.asString
      } yield assertTrue(
        res.status == Status.Forbidden,
        body.fromJson[Map[String, String]].exists(_.contains("error"))
      )).provideSome[Scope](env(items = List(it)), ZLayer.succeed(stranger))
    },
    test("PUT /items/{id} by the owner succeeds and updates the item") {
      val it = item(new UUID(0L, 31L), "Owned", BigDecimal(5), Some(owner.id))
      val payload =
        s"""{"name":"Renamed","description":"d","price":7.00,"categoryId":"$categoryId","location":"NY","imageUrl":null}"""
      (for {
        res  <- protectedItems(Request.put(s"/items/${it.id}", jsonBody(payload)))
        body <- res.body.asString
        updated = body.fromJson[Item]
      } yield assertTrue(
        res.status == Status.Ok,
        updated.map(_.name) == Right("Renamed"),
        updated.map(_.price) == Right(BigDecimal(7))
      )).provideSome[Scope](env(items = List(it)), ZLayer.succeed(owner))
    },
    test("DELETE /items/{id} by a non-owner returns 403") {
      val it = item(new UUID(0L, 40L), "Owned", BigDecimal(5), Some(owner.id))
      (for {
        res <- protectedItems(Request.delete(s"/items/${it.id}"))
      } yield assertTrue(res.status == Status.Forbidden))
        .provideSome[Scope](env(items = List(it)), ZLayer.succeed(stranger))
    },
    test("DELETE /items/{id} by the owner returns 204") {
      val it = item(new UUID(0L, 41L), "Owned", BigDecimal(5), Some(owner.id))
      (for {
        res <- protectedItems(Request.delete(s"/items/${it.id}"))
      } yield assertTrue(res.status == Status.NoContent))
        .provideSome[Scope](env(items = List(it)), ZLayer.succeed(owner))
    },
    test("GET /categories returns the seeded categories") {
      (for {
        res  <- CategoryRoutes.publicRoutes.handleErrorCauseZIO(ApiError.handle).runZIO(Request.get("/categories"))
        body <- res.body.asString
      } yield assertTrue(
        res.status == Status.Ok,
        body.fromJson[List[Category]].map(_.exists(_.name == "Electronics")) == Right(true)
      )).provideSome[Scope](env())
    },
    test("GET /categories/{id} for a missing id returns 404") {
      (for {
        res <- CategoryRoutes.publicRoutes.handleErrorCauseZIO(ApiError.handle).runZIO(Request.get(s"/categories/${new UUID(9L, 9L)}"))
      } yield assertTrue(res.status == Status.NotFound)).provideSome[Scope](env())
    }
  )
}
