package com.example.zivito

import java.util.UUID

import com.example.zivito.Domain.Item
import io.getquill.context.ZioJdbc.DataSourceLayer
import io.getquill.{Escape, H2ZioJdbcContext}
import javax.sql.DataSource
import zio.{Random, Task, ZLayer}



case class ItemTable(id: UUID,
                     name: String,
                     categoryId: UUID
                    )
case class ItemRepoPersist(ds: DataSource) extends ItemRepo {

  val ctx = new H2ZioJdbcContext(Escape)

  import ctx._

  override def getByCategoryID(categoryId: UUID): Task[Seq[Item]] = ???

  override def create(item: Item): Task[Item] = {
    for {
      id <- Random.nextUUID
      _ <- ctx.run {
        quote {
          query[ItemTable].insertValue {
            lift(ItemTable(id, item.name, item.categoryId))
          }
        }
      }
    } yield Item(id, name = item.name, categoryId = item.categoryId)
  }.provide(ZLayer.succeed(ds))

  override def delete(id: UUID): Task[Unit] = ???
}

object ItemRepoPersist {
  def layer: ZLayer[DataSource, Throwable, ItemRepoPersist] =
    DataSourceLayer.fromPrefix("App") >>>
      ZLayer.fromFunction(ItemRepoPersist(_))
}

