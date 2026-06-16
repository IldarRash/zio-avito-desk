package com.example.zivito

import java.time.Instant
import java.util.UUID

import com.example.zivito.Domain.{Item, ItemFilter, Page, SortOrder}
import io.getquill.*
import javax.sql.DataSource
import zio.{Task, ZLayer}

case class ItemTable(
    id: UUID,
    name: String,
    description: String,
    price: BigDecimal,
    categoryId: UUID,
    location: String,
    imageUrl: String,
    createdAt: Instant,
    ownerId: Option[UUID]
)

case class ItemRepoPersist(ds: DataSource) extends ItemRepo {

  val ctx = new PostgresZioJdbcContext(Escape)

  import ctx.*

  private val tableToItem = (t: ItemTable) => Item(t.id, t.name, t.description, t.price, t.categoryId, t.location, t.imageUrl, t.createdAt, t.ownerId)
  private val itemToTable = (i: Item) => ItemTable(i.id, i.name, i.description, i.price, i.categoryId, i.location, i.imageUrl, i.createdAt, i.ownerId)

  override def get(id: UUID): Task[Option[Item]] =
    ctx
      .run(query[ItemTable].filter(_.id == lift(id)))
      .map(_.headOption.map(tableToItem))
      .provide(ZLayer.succeed(ds))

  override def list(filter: ItemFilter): Task[Page[Item]] = {
    val likeTerm = filter.query.map(q => s"%${q.toLowerCase}%")

    // Composed as runtime `Quoted` values, so ProtoQuill builds the SQL
    // dynamically from whichever filters are present.
    val base: Quoted[Query[ItemTable]] = quote(query[ItemTable])
    val byCategory                     = filter.categoryId.fold(base)(c => quote(base.filter(_.categoryId == lift(c))))
    val byMin                          = filter.minPrice.fold(byCategory)(p => quote(byCategory.filter(_.price >= lift(p))))
    val byMax                          = filter.maxPrice.fold(byMin)(p => quote(byMin.filter(_.price <= lift(p))))
    val byLocation                     = filter.location.fold(byMax)(loc => quote(byMax.filter(_.location.toLowerCase == lift(loc.toLowerCase))))
    val filtered =
      likeTerm.fold(byLocation)(term => quote(byLocation.filter(i => i.name.toLowerCase.like(lift(term)) || i.description.toLowerCase.like(lift(term)))))

    val sorted = filter.sort match {
      case SortOrder.PriceAsc  => quote(filtered.sortBy(_.price)(Ord.asc))
      case SortOrder.PriceDesc => quote(filtered.sortBy(_.price)(Ord.desc))
      case SortOrder.Newest    => quote(filtered.sortBy(_.createdAt)(Ord.desc))
    }

    val paged = quote(sorted.drop(lift(filter.offset)).take(lift(filter.limit)))

    (for {
      rows  <- ctx.run(paged)
      total <- ctx.run(quote(filtered.size))
    } yield Page(rows.map(tableToItem).toList, total, filter.limit, filter.offset))
      .provide(ZLayer.succeed(ds))
  }

  override def create(item: Item): Task[Item] =
    ctx
      .run(query[ItemTable].insertValue(lift(itemToTable(item))))
      .as(item)
      .provide(ZLayer.succeed(ds))

  override def update(item: Item): Task[Option[Item]] =
    ctx
      .run {
        query[ItemTable]
          .filter(_.id == lift(item.id))
          .update(
            _.name        -> lift(item.name),
            _.description -> lift(item.description),
            _.price       -> lift(item.price),
            _.categoryId  -> lift(item.categoryId),
            _.location    -> lift(item.location),
            _.imageUrl    -> lift(item.imageUrl)
          )
      }
      .map(updated => if (updated > 0) Some(item) else None)
      .provide(ZLayer.succeed(ds))

  override def delete(id: UUID): Task[Boolean] =
    ctx
      .run(query[ItemTable].filter(_.id == lift(id)).delete)
      .map(_ > 0)
      .provide(ZLayer.succeed(ds))
}

object ItemRepoPersist {
  def layer: ZLayer[DataSource, Nothing, ItemRepo] =
    ZLayer.fromFunction(ItemRepoPersist(_))
}
