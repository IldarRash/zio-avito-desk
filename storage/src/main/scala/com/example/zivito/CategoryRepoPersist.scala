package com.example.zivito

import java.util.UUID

import com.example.zivito.Domain.Category
import io.getquill.*
import javax.sql.DataSource
import zio.{Task, ZLayer}

case class CategoryTable(id: UUID, name: String)

case class CategoryRepoPersist(ds: DataSource) extends CategoryRepo {

  val ctx = new H2ZioJdbcContext(Escape)

  import ctx.*

  private val categoryToTable = (category: Category) => CategoryTable(category.id, category.name)
  private val tableToCategory = (table: CategoryTable) => Category(table.id, table.name)

  override def create(category: Category): Task[Category] =
    ctx
      .run {
        query[CategoryTable].insertValue(lift(categoryToTable(category)))
      }
      .as(category)
      .provide(ZLayer.succeed(ds))

  override def get(id: UUID): Task[Option[Category]] =
    ctx
      .run {
        query[CategoryTable].filter(_.id == lift(id))
      }
      .map(_.headOption.map(tableToCategory))
      .provide(ZLayer.succeed(ds))

  override def getAll: Task[Seq[Category]] =
    ctx
      .run {
        query[CategoryTable]
      }
      .map(_.map(tableToCategory))
      .provide(ZLayer.succeed(ds))

  override def update(category: Category): Task[Category] =
    ctx
      .run {
        query[CategoryTable].filter(_.id == lift(category.id)).updateValue(lift(categoryToTable(category)))
      }
      .as(category)
      .provide(ZLayer.succeed(ds))

  override def delete(id: UUID): Task[Boolean] =
    ctx
      .run {
        query[CategoryTable].filter(_.id == lift(id)).delete
      }
      .map(_ > 0)
      .provide(ZLayer.succeed(ds))
}

object CategoryRepoPersist {
  def layer: ZLayer[DataSource, Nothing, CategoryRepo] =
    ZLayer.fromFunction(CategoryRepoPersist(_))
}
