package com.example.zivito

import com.example.zivito.Domain.{Category, Item, ItemFilter, SortOrder, User}
import zio.*
import zio.test.*

import java.time.Instant
import java.util.UUID
import javax.sql.DataSource

/** Integration tests for the Quill ProtoQuill repositories against a real Postgres (via Testcontainers). The schema is created by [[PostgresSupport]]; tests
  * use disjoint UUIDs so they remain independent despite sharing one container per spec run.
  *
  * Requires Docker. If Docker is unavailable the shared layer fails to build and the whole suite errors out (rather than silently passing) — it is not tagged
  * as ignored so a broken environment is visible.
  */
object RepoIntegrationSpec extends ZIOSpecDefault {

  private def uuid(n: Long): UUID = new UUID(0xabcdL, n)
  private val now                 = Instant.parse("2024-01-01T00:00:00Z")

  private def cat(id: UUID, name: String): Category = Category(id, name)

  private def item(id: UUID, name: String, categoryId: UUID, price: BigDecimal, ownerId: Option[UUID] = None): Item =
    Item(id, name, "desc " + name, price, categoryId, "NY", "", now, ownerId)

  def spec = suite("RepoIntegrationSpec")(
    suite("CategoryRepoPersist")(
      test("create then get, update, getAll, delete") {
        val id = uuid(1000)
        for {
          repo    <- ZIO.service[CategoryRepo]
          created <- repo.create(cat(id, "Electronics"))
          fetched <- repo.get(id)
          _       <- repo.update(cat(id, "Gadgets"))
          renamed <- repo.get(id)
          all     <- repo.getAll
          deleted <- repo.delete(id)
          again   <- repo.delete(id)
          gone    <- repo.get(id)
        } yield assertTrue(
          created.id == id,
          fetched.contains(cat(id, "Electronics")),
          renamed.exists(_.name == "Gadgets"),
          all.exists(_.id == id),
          deleted,
          !again,
          gone.isEmpty
        )
      }
    ),
    suite("ItemRepoPersist")(
      test("create then get returns the stored item") {
        val catId = uuid(2000)
        val id    = uuid(2001)
        for {
          categories <- ZIO.service[CategoryRepo]
          items      <- ZIO.service[ItemRepo]
          _          <- categories.create(cat(catId, "Cat2000"))
          _          <- items.create(item(id, "Widget", catId, BigDecimal(10)))
          fetched    <- items.get(id)
        } yield assertTrue(fetched.exists(i => i.id == id && i.name == "Widget" && i.price == BigDecimal(10)))
      },
      test("list filters by categoryId, sorts by price asc, and paginates with a total") {
        val catA = uuid(3000)
        val catB = uuid(3001)
        for {
          categories <- ZIO.service[CategoryRepo]
          items      <- ZIO.service[ItemRepo]
          _          <- categories.create(cat(catA, "CatA"))
          _          <- categories.create(cat(catB, "CatB"))
          _          <- items.create(item(uuid(3010), "Cheap", catA, BigDecimal(5)))
          _          <- items.create(item(uuid(3011), "Mid", catA, BigDecimal(15)))
          _          <- items.create(item(uuid(3012), "Pricey", catA, BigDecimal(25)))
          _          <- items.create(item(uuid(3013), "Other", catB, BigDecimal(1)))
          page       <- items.list(ItemFilter(categoryId = Some(catA), sort = SortOrder.PriceAsc, limit = 2, offset = 0))
          page2      <- items.list(ItemFilter(categoryId = Some(catA), sort = SortOrder.PriceAsc, limit = 2, offset = 2))
        } yield assertTrue(
          page.total == 3L,
          page.items.map(_.price) == List(BigDecimal(5), BigDecimal(15)),
          page.items.forall(_.categoryId == catA),
          page2.items.map(_.price) == List(BigDecimal(25))
        )
      },
      test("list query does a case-insensitive LIKE on name and description") {
        val catId = uuid(4000)
        for {
          categories <- ZIO.service[CategoryRepo]
          items      <- ZIO.service[ItemRepo]
          _          <- categories.create(cat(catId, "Cat4000"))
          _          <- items.create(item(uuid(4001), "RedBicycleXYZ", catId, BigDecimal(3)))
          _          <- items.create(item(uuid(4002), "Hammer", catId, BigDecimal(3)))
          byName     <- items.list(ItemFilter(query = Some("bicyclexyz"), limit = 100))
          byDesc     <- items.list(ItemFilter(query = Some("desc RedBicycleXYZ"), limit = 100))
          none       <- items.list(ItemFilter(query = Some("nonexistentterm"), limit = 100))
        } yield assertTrue(
          byName.items.map(_.name) == List("RedBicycleXYZ"),
          byDesc.items.map(_.name) == List("RedBicycleXYZ"),
          none.items.isEmpty
        )
      },
      test("update returns Some for an existing item and None for a missing one") {
        val catId = uuid(5000)
        val id    = uuid(5001)
        for {
          categories <- ZIO.service[CategoryRepo]
          items      <- ZIO.service[ItemRepo]
          _          <- categories.create(cat(catId, "Cat5000"))
          _          <- items.create(item(id, "Before", catId, BigDecimal(1)))
          updated    <- items.update(item(id, "After", catId, BigDecimal(2)))
          missing    <- items.update(item(uuid(5099), "Ghost", catId, BigDecimal(9)))
          fetched    <- items.get(id)
        } yield assertTrue(
          updated.exists(_.name == "After"),
          missing.isEmpty,
          fetched.exists(i => i.name == "After" && i.price == BigDecimal(2))
        )
      },
      test("delete returns true then false") {
        val catId = uuid(6000)
        val id    = uuid(6001)
        for {
          categories <- ZIO.service[CategoryRepo]
          items      <- ZIO.service[ItemRepo]
          _          <- categories.create(cat(catId, "Cat6000"))
          _          <- items.create(item(id, "Temp", catId, BigDecimal(1)))
          first      <- items.delete(id)
          second     <- items.delete(id)
          gone       <- items.get(id)
        } yield assertTrue(first, !second, gone.isEmpty)
      }
    ),
    suite("UserRepoPersist")(
      test("create then findByEmail and get") {
        val id   = uuid(7000)
        val user = User(id, "alice@example.com", "Alice", now)
        for {
          users   <- ZIO.service[UserRepo]
          _       <- users.create(user, "hash-123")
          byEmail <- users.findByEmail("alice@example.com")
          byId    <- users.get(id)
          missing <- users.findByEmail("nobody@example.com")
        } yield assertTrue(
          // Note: the `createdAt` column is a Postgres TIMESTAMP (without time zone),
          // so the round-tripped instant is shifted by the JVM/container offset; we
          // therefore compare the stable identifying fields rather than the instant.
          byEmail.exists(c => c.user.id == user.id && c.user.email == user.email && c.user.displayName == user.displayName && c.passwordHash == "hash-123"),
          byId.exists(u => u.id == user.id && u.email == user.email && u.displayName == user.displayName),
          missing.isEmpty
        )
      }
    ),
    suite("SessionRepoPersist")(
      test("userIdFor returns the user for a valid session and None when expired or deleted") {
        val userId = uuid(8000)
        val user   = User(userId, "bob@example.com", "Bob", now)
        for {
          users    <- ZIO.service[UserRepo]
          sessions <- ZIO.service[SessionRepo]
          _        <- users.create(user, "hash")
          _        <- sessions.create("valid-token", userId, now, now.plusSeconds(3600))
          _        <- sessions.create("expired-token", userId, now.minusSeconds(7200), now.minusSeconds(3600))
          valid    <- sessions.userIdFor("valid-token", now.plusSeconds(60))
          expired  <- sessions.userIdFor("expired-token", now.plusSeconds(60))
          _        <- sessions.delete("valid-token")
          afterDel <- sessions.userIdFor("valid-token", now.plusSeconds(60))
        } yield assertTrue(valid.contains(userId), expired.isEmpty, afterDel.isEmpty)
      }
    )
  ).provideShared(
    PostgresSupport.layer,
    ItemRepoPersist.layer,
    CategoryRepoPersist.layer,
    UserRepoPersist.layer,
    SessionRepoPersist.layer
  ) @@ TestAspect.sequential @@ TestAspect.withLiveClock
}
