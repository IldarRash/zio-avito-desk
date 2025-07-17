package com.example.zivito.domain

import zio._

trait UserRepository {
  def findAll: Task[List[User]]
  def findById(id: Long): Task[Option[User]]
  def create(user: User): Task[User]
  def update(user: User): Task[User]
  def delete(id: Long): Task[Boolean]
}