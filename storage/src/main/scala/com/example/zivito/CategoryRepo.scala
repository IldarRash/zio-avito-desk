package com.example.zivito

import java.util.UUID

import com.example.zivito.Domain.Category
import zio.{Task, ZIO}

trait CategoryRepo {

  def create(category: Category): Task[Category]

  def get(id: UUID): Task[Option[Category]]

  def getAll: Task[Seq[Category]]

  def update(category: Category): Task[Category]

  def delete(id: UUID): Task[Boolean]
}

object CategoryRepo {

  def create(category: Category): ZIO[CategoryRepo, Throwable, Category] =
    ZIO.serviceWithZIO[CategoryRepo](_.create(category))

  def get(id: UUID): ZIO[CategoryRepo, Throwable, Option[Category]] =
    ZIO.serviceWithZIO[CategoryRepo](_.get(id))

  def getAll: ZIO[CategoryRepo, Throwable, Seq[Category]] =
    ZIO.serviceWithZIO[CategoryRepo](_.getAll)

  def update(category: Category): ZIO[CategoryRepo, Throwable, Category] =
    ZIO.serviceWithZIO[CategoryRepo](_.update(category))

  def delete(id: UUID): ZIO[CategoryRepo, Throwable, Boolean] =
    ZIO.serviceWithZIO[CategoryRepo](_.delete(id))
}
