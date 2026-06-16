package com.example.zivito

import java.util.UUID

import com.example.zivito.Domain.User
import zio.Task

trait UserRepo {

  /** Inserts a new user with the given (already-hashed) password. */
  def create(user: User, passwordHash: String): Task[User]

  /** Looks up a user by id. */
  def get(id: UUID): Task[Option[User]]

  /** Looks up a user and their password hash by email, for login. */
  def findByEmail(email: String): Task[Option[UserRepo.Credentials]]
}

object UserRepo {

  /** A user paired with their stored password hash (never serialized). */
  final case class Credentials(user: User, passwordHash: String)
}
