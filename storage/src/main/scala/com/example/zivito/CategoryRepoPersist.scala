package com.example.zivito

import com.example.zivito.Domain.Category
import io.getquill.{Escape, H2ZioJdbcContext}
import io.getquill.context.ZioJdbc.DataSourceLayer
import javax.sql.DataSource
import zio.{Random, Task, ZLayer}
import java.util.UUID

final case class CategoryTable(id: UUID, name: String)

final case class CategoryRepoPersist(ds: DataSource) extends CategoryRepo {
  private val ctx = new H2ZioJdbcContext(Escape)
  import ctx._

  private def tableToDomain(t: CategoryTable): Category = Category(t.id, t.name)

  override def getAll: Task[Seq[Category]] =
    ctx.run(query[CategoryTable]).map(_.map(tableToDomain)).provide(ZLayer.succeed(ds))

  override def get(id: UUID): Task[Option[Category]] =
    ctx.run(query[CategoryTable].filter(_.id == lift(id))).map(_.headOption.map(tableToDomain)).provide(ZLayer.succeed(ds))

  override def create(category: Category): Task[Category] =
    for {
      id <- Random.nextUUID
      _  <- ctx.run(query[CategoryTable].insertValue(lift(CategoryTable(id, category.name))))
    } yield category.copy(id = id)

  override def update(category: Category): Task[Category] =
    ctx.run(query[CategoryTable].filter(_.id == lift(category.id)).updateValue(lift(CategoryTable(category.id, category.name))))
      .as(category).provide(ZLayer.succeed(ds))

  override def delete(id: UUID): Task[Unit] =
    ctx.run(query[CategoryTable].filter(_.id == lift(id)).delete).unit.provide(ZLayer.succeed(ds))
}

object CategoryRepoPersist {
  def layer: ZLayer[DataSource, Throwable, CategoryRepo] =
    DataSourceLayer.fromPrefix("App") >>> ZLayer.fromFunction(CategoryRepoPersist(_))
}
