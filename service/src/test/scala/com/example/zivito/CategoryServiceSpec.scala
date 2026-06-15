package com.example.zivito

import com.example.zivito.Domain.Category
import zio.*
import zio.test.*

import java.util.UUID

final case class InMemoryCategoryRepo(ref: Ref[Map[UUID, Category]]) extends CategoryRepo {
  override def create(category: Category): Task[Category] =
    ref.update(_ + (category.id -> category)).as(category)
  override def get(id: UUID): Task[Option[Category]] = ref.get.map(_.get(id))
  override def getAll: Task[Seq[Category]]           = ref.get.map(_.values.toSeq)
  override def update(category: Category): Task[Category] =
    ref.update(_ + (category.id -> category)).as(category)
  override def delete(id: UUID): Task[Boolean] =
    ref.modify(m => (m.contains(id), m - id))
}

object InMemoryCategoryRepo {
  val layer: ZLayer[Any, Nothing, CategoryRepo] =
    ZLayer.fromZIO(Ref.make(Map.empty[UUID, Category]).map(InMemoryCategoryRepo(_)))
}

object CategoryServiceSpec extends ZIOSpecDefault {

  private def cat(name: String): UIO[Category] = Random.nextUUID.map(Category(_, name))

  def spec = suite("CategoryServiceSpec")(
    test("create then get returns the category") {
      for {
        service <- ZIO.service[CategoryService]
        c       <- cat("Electronics")
        created <- service.createCategory(c)
        fetched <- service.getCategory(created.id)
      } yield assertTrue(fetched.contains(created))
    },
    test("get returns None for a missing category") {
      for {
        service <- ZIO.service[CategoryService]
        result  <- service.getCategory(new UUID(123L, 456L))
      } yield assertTrue(result.isEmpty)
    },
    test("getAll returns created categories") {
      for {
        service <- ZIO.service[CategoryService]
        c       <- cat("Furniture")
        _       <- service.createCategory(c)
        all     <- service.getAllCategories
      } yield assertTrue(all.exists(_.name == "Furniture"))
    },
    test("update changes the name") {
      for {
        service <- ZIO.service[CategoryService]
        c       <- cat("Old")
        _       <- service.createCategory(c)
        _       <- service.updateCategory(c.copy(name = "New"))
        fetched <- service.getCategory(c.id)
      } yield assertTrue(fetched.exists(_.name == "New"))
    },
    test("delete removes the category and reports its prior presence") {
      for {
        service <- ZIO.service[CategoryService]
        c       <- cat("Vehicles")
        _       <- service.createCategory(c)
        deleted <- service.deleteCategory(c.id)
        again   <- service.deleteCategory(c.id)
        fetched <- service.getCategory(c.id)
      } yield assertTrue(deleted, !again, fetched.isEmpty)
    }
  ).provide(CategoryServiceImpl.layer, InMemoryCategoryRepo.layer)
}
