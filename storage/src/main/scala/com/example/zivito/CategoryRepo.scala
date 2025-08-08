package com.example.zivito

import com.example.zivito.Domain.Category
import zio.{Task, ZIO}
import java.util.UUID

trait CategoryRepo {
  def getAll: Task[Seq[Category]]
  def get(id: UUID): Task[Option[Category]]
  def create(category: Category): Task[Category]
  def update(category: Category): Task[Category]
  def delete(id: UUID): Task[Unit]
}

object CategoryRepo {
  def getAll: ZIO[CategoryRepo, Throwable, Seq[Category]] = ZIO.serviceWithZIO[CategoryRepo](_.getAll)
  def get(id: UUID): ZIO[CategoryRepo, Throwable, Option[Category]] = ZIO.serviceWithZIO[CategoryRepo](_.get(id))
  def create(c: Category): ZIO[CategoryRepo, Throwable, Category] = ZIO.serviceWithZIO[CategoryRepo](_.create(c))
  def update(c: Category): ZIO[CategoryRepo, Throwable, Category] = ZIO.serviceWithZIO[CategoryRepo](_.update(c))
  def delete(id: UUID): ZIO[CategoryRepo, Throwable, Unit] = ZIO.serviceWithZIO[CategoryRepo](_.delete(id))
}
