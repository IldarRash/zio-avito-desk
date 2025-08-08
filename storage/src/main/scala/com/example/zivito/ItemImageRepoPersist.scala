package com.example.zivito

import com.example.zivito.Domain.ItemImage
import io.getquill.{Escape, H2ZioJdbcContext}
import io.getquill.context.ZioJdbc.DataSourceLayer
import javax.sql.DataSource
import zio.{Random, Task, ZLayer}
import java.util.UUID

final case class ItemImageTable(id: UUID, itemId: UUID, url: String, isCover: Boolean, order: Int)

final case class ItemImageRepoPersist(ds: DataSource) extends ItemImageRepo {
  private val ctx = new H2ZioJdbcContext(Escape)
  import ctx._

  private def toDomain(t: ItemImageTable): ItemImage = ItemImage(t.id, t.itemId, t.url, t.isCover, t.order)

  override def listByItem(itemId: UUID): Task[Seq[ItemImage]] =
    ctx.run(query[ItemImageTable].filter(_.itemId == lift(itemId)).sortBy(_.order)(Ord.asc)).map(_.map(toDomain)).provide(ZLayer.succeed(ds))

  override def add(image: ItemImage): Task[ItemImage] =
    for {
      id <- Random.nextUUID
      saved = image.copy(id = id)
      _ <- ctx.run(query[ItemImageTable].insertValue(lift(ItemImageTable(saved.id, saved.itemId, saved.url, saved.isCover, saved.order))))
    } yield saved

  override def setCover(itemId: UUID, imageId: UUID): Task[Unit] =
    for {
      _ <- ctx.run(query[ItemImageTable].filter(_.itemId == lift(itemId)).update(_.isCover -> lift(false)))
      _ <- ctx.run(query[ItemImageTable].filter(r => r.itemId == lift(itemId) && r.id == lift(imageId)).update(_.isCover -> lift(true)))
    } yield ()

  override def delete(id: UUID): Task[Unit] =
    ctx.run(query[ItemImageTable].filter(_.id == lift(id)).delete).unit.provide(ZLayer.succeed(ds))
}

object ItemImageRepoPersist {
  def layer: ZLayer[DataSource, Throwable, ItemImageRepo] =
    DataSourceLayer.fromPrefix("App") >>> ZLayer.fromFunction(ItemImageRepoPersist(_))
}
