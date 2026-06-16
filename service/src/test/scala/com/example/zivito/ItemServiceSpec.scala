package com.example.zivito

import com.example.zivito.Domain.{Item, ItemFilter, Page, SortOrder}
import zio.*
import zio.test.*

import java.time.Instant
import java.util.UUID

/** In-memory [[ItemRepo]] for fast, deterministic service tests. Unlike the persistent repo it stores items verbatim (id generation lives in the route layer),
  * and implements the same filter/sort/paginate semantics as SQL.
  */
final case class InMemoryItemRepo(ref: Ref[Map[UUID, Item]]) extends ItemRepo {
  override def get(id: UUID): Task[Option[Item]] = ref.get.map(_.get(id))

  override def list(filter: ItemFilter): Task[Page[Item]] =
    ref.get.map { items =>
      val filtered = items.values.toList.filter { i =>
        filter.categoryId.forall(_ == i.categoryId) &&
        filter.minPrice.forall(i.price >= _) &&
        filter.maxPrice.forall(i.price <= _) &&
        filter.location.forall(l => i.location.equalsIgnoreCase(l)) &&
        filter.query.forall(q => i.name.toLowerCase.contains(q.toLowerCase) || i.description.toLowerCase.contains(q.toLowerCase))
      }
      val sorted = filter.sort match {
        case SortOrder.Newest    => filtered.sortWith((a, b) => a.createdAt.isAfter(b.createdAt))
        case SortOrder.PriceAsc  => filtered.sortBy(_.price)
        case SortOrder.PriceDesc => filtered.sortBy(_.price).reverse
      }
      Page(sorted.drop(filter.offset).take(filter.limit), sorted.size.toLong, filter.limit, filter.offset)
    }

  override def create(item: Item): Task[Item] = ref.update(_ + (item.id -> item)).as(item)

  override def update(item: Item): Task[Option[Item]] =
    ref.modify(m => if (m.contains(item.id)) (Some(item), m + (item.id -> item)) else (None, m))

  override def delete(id: UUID): Task[Boolean] =
    ref.modify(m => (m.contains(id), m - id))
}

object InMemoryItemRepo {
  val layer: ZLayer[Any, Nothing, ItemRepo] =
    ZLayer.fromZIO(Ref.make(Map.empty[UUID, Item]).map(InMemoryItemRepo(_)))
}

object ItemServiceSpec extends ZIOSpecDefault {

  private val catId   = new UUID(1L, 1L)
  private val otherId = new UUID(2L, 2L)

  private def newItem(
      id: UUID,
      name: String,
      description: String,
      categoryId: UUID = catId,
      price: BigDecimal = BigDecimal(9.99)
  ): Item =
    Item(id, name, description, price, categoryId, "NY", "", Instant.EPOCH, None)

  def spec = suite("ItemServiceSpec")(
    test("create stores the item and it can be fetched") {
      val id = new UUID(10L, 0L)
      for {
        service <- ZIO.service[ItemService]
        created <- service.create(newItem(id, "Phone", "shiny"))
        fetched <- service.get(id)
      } yield assertTrue(created.id == id, fetched.contains(created))
    },
    test("get returns None for a missing item") {
      for {
        service <- ZIO.service[ItemService]
        result  <- service.get(new UUID(99L, 99L))
      } yield assertTrue(result.isEmpty)
    },
    test("search matches name and description case-insensitively") {
      for {
        service <- ZIO.service[ItemService]
        _       <- service.create(newItem(new UUID(11L, 0L), "Laptop", "fast machine"))
        _       <- service.create(newItem(new UUID(12L, 0L), "Desk", "wooden"))
        byName  <- service.search("laptop")
        byDesc  <- service.search("WOODEN")
        none    <- service.search("nonexistent")
      } yield assertTrue(byName.size == 1, byDesc.size == 1, none.isEmpty)
    },
    test("getItemsByCategory filters by category") {
      for {
        service <- ZIO.service[ItemService]
        _       <- service.create(newItem(new UUID(13L, 0L), "A", "a", catId))
        _       <- service.create(newItem(new UUID(14L, 0L), "B", "b", otherId))
        inCat   <- service.getItemsByCategory(catId)
      } yield assertTrue(inCat.forall(_.categoryId == catId), inCat.nonEmpty)
    },
    test("list sorts by price and paginates with a total count") {
      for {
        service <- ZIO.service[ItemService]
        _       <- service.create(newItem(new UUID(20L, 0L), "Cheap", "x", price = BigDecimal(5)))
        _       <- service.create(newItem(new UUID(21L, 0L), "Mid", "x", price = BigDecimal(10)))
        _       <- service.create(newItem(new UUID(22L, 0L), "Pricey", "x", price = BigDecimal(20)))
        page    <- service.list(ItemFilter(sort = SortOrder.PriceAsc, limit = 2, offset = 0))
      } yield assertTrue(page.total == 3L, page.items.map(_.price) == List(BigDecimal(5), BigDecimal(10)))
    },
    test("update changes fields and reports missing items") {
      val id = new UUID(30L, 0L)
      for {
        service <- ZIO.service[ItemService]
        _       <- service.create(newItem(id, "Old", "desc", price = BigDecimal(1)))
        updated <- service.update(newItem(id, "New", "desc", price = BigDecimal(2)))
        missing <- service.update(newItem(new UUID(31L, 0L), "Ghost", "desc"))
        fetched <- service.get(id)
      } yield assertTrue(updated.exists(_.name == "New"), missing.isEmpty, fetched.exists(_.price == BigDecimal(2)))
    },
    test("delete removes the item and reports whether it existed") {
      val id = new UUID(40L, 0L)
      for {
        service <- ZIO.service[ItemService]
        _       <- service.create(newItem(id, "Temp", "temp"))
        first   <- service.delete(id)
        second  <- service.delete(id)
        after   <- service.get(id)
      } yield assertTrue(first, !second, after.isEmpty)
    }
  ).provide(ItemServiceImpl.layer, InMemoryItemRepo.layer)
}
