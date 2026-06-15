package com.example.zivito

import com.example.zivito.Domain.Item
import zio.*
import zio.test.*

import java.util.UUID

final case class InMemoryItemRepo(ref: Ref[Map[UUID, Item]]) extends ItemRepo {
  override def get(id: UUID): Task[Option[Item]] = ref.get.map(_.get(id))
  override def getAll: Task[Seq[Item]]           = ref.get.map(_.values.toSeq)
  override def search(query: String): Task[Seq[Item]] =
    ref.get.map(_.values.filter(i => i.name.contains(query) || i.description.contains(query)).toSeq)
  override def getByCategoryID(categoryId: UUID): Task[Seq[Item]] =
    ref.get.map(_.values.filter(_.categoryId == categoryId).toSeq)
  override def create(item: Item): Task[Item] =
    for {
      id <- Random.nextUUID
      created = item.copy(id = id)
      _ <- ref.update(_ + (id -> created))
    } yield created
  override def delete(id: UUID): Task[Unit] = ref.update(_ - id)
}

object InMemoryItemRepo {
  val layer: ZLayer[Any, Nothing, ItemRepo] =
    ZLayer.fromZIO(Ref.make(Map.empty[UUID, Item]).map(InMemoryItemRepo(_)))
}

object ItemServiceSpec extends ZIOSpecDefault {

  private val catId   = new UUID(1L, 1L)
  private val otherId = new UUID(2L, 2L)

  private def newItem(name: String, description: String, categoryId: UUID = catId): Item =
    Item(new UUID(0L, 0L), name, description, BigDecimal(9.99), categoryId, "NY", "")

  def spec = suite("ItemServiceSpec")(
    test("create assigns a fresh id and stores the item") {
      for {
        service <- ZIO.service[ItemService]
        created <- service.create(newItem("Phone", "shiny"))
        fetched <- service.get(created.id)
      } yield assertTrue(created.id != new UUID(0L, 0L), fetched.contains(created))
    },
    test("get returns None for a missing item") {
      for {
        service <- ZIO.service[ItemService]
        result  <- service.get(new UUID(99L, 99L))
      } yield assertTrue(result.isEmpty)
    },
    test("search matches name and description") {
      for {
        service <- ZIO.service[ItemService]
        _       <- service.create(newItem("Laptop", "fast machine"))
        _       <- service.create(newItem("Desk", "wooden"))
        byName  <- service.search("Laptop")
        byDesc  <- service.search("wooden")
        none    <- service.search("nonexistent")
      } yield assertTrue(byName.size == 1, byDesc.size == 1, none.isEmpty)
    },
    test("getItemsByCategory filters by category") {
      for {
        service <- ZIO.service[ItemService]
        _       <- service.create(newItem("A", "a", catId))
        _       <- service.create(newItem("B", "b", otherId))
        inCat   <- service.getItemsByCategory(catId)
      } yield assertTrue(inCat.forall(_.categoryId == catId), inCat.nonEmpty)
    },
    test("delete removes the item") {
      for {
        service <- ZIO.service[ItemService]
        created <- service.create(newItem("Temp", "temp"))
        _       <- service.delete(created.id)
        after   <- service.get(created.id)
      } yield assertTrue(after.isEmpty)
    }
  ).provide(ItemServiceImpl.layer, InMemoryItemRepo.layer)
}
