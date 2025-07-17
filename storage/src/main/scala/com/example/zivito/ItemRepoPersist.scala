package com.example.zivito

import java.util.UUID

import com.example.zivito.Domain.Item
import io.getquill.context.ZioJdbc.DataSourceLayer
import io.getquill.{Escape, H2ZioJdbcContext}
import javax.sql.DataSource
import zio.{Random, Task, ZLayer}



case class ItemTable(id: UUID,
                     name: String,
                     description: String,
                     price: BigDecimal,
                     categoryId: UUID,
                     location: String
                    )
case class ItemRepoPersist(ds: DataSource) extends ItemRepo {

  val ctx = new H2ZioJdbcContext(Escape)

  import ctx._

  private val itemToTable = (item: Item) => ItemTable(item.id, item.name, item.description, item.price, item.categoryId, item.location)
  private val tableToItem = (itemTable: ItemTable) => Item(itemTable.id, itemTable.name, itemTable.description, itemTable.price, itemTable.categoryId, itemTable.location)


  override def get(id: UUID): Task[Option[Item]] =
    ctx.run {
      query[ItemTable].filter(_.id == lift(id))
    }.map(_.headOption.map(tableToItem)).provide(ZLayer.succeed(ds))

  override def getAll: Task[Seq[Item]] =
    ctx.run {
      query[ItemTable]
    }.map(_.map(tableToItem)).provide(ZLayer.succeed(ds))

  override def search(searchQuery: String): Task[Seq[Item]] =
    ctx.run {
      query[ItemTable].filter(p => p.name.like(lift(s"%$searchQuery%")) || p.description.like(lift(s"%$searchQuery%")))
    }.map(_.map(tableToItem)).provide(ZLayer.succeed(ds))


  override def getByCategoryID(categoryId: UUID): Task[Seq[Item]] =
    ctx.run {
      query[ItemTable].filter(_.categoryId == lift(categoryId))
    }.map(_.map(tableToItem)).provide(ZLayer.succeed(ds))

  override def create(item: Item): Task[Item] = {
    for {
      id <- Random.nextUUID
      newItem = item.copy(id = id)
      _ <- ctx.run {
        query[ItemTable].insertValue {
          lift(itemToTable(newItem))
        }
      }
    } yield newItem
  }.provide(ZLayer.succeed(ds))

  override def delete(id: UUID): Task[Unit] =
    ctx.run {
      query[ItemTable].filter(_.id == lift(id)).delete
    }.unit.provide(ZLayer.succeed(ds))
}

object ItemRepoPersist {
  def layer: ZLayer[DataSource, Throwable, ItemRepoPersist] =
    DataSourceLayer.fromPrefix("App") >>>
      ZLayer.fromFunction(ItemRepoPersist(_))
}

