package com.example.zivito

import java.time.Instant
import java.util.UUID

import zio.Task

trait SessionRepo {

  /** Persists a session token for a user. */
  def create(token: String, userId: UUID, createdAt: Instant, expiresAt: Instant): Task[Unit]

  /** Returns the user id for a token if the session exists and has not expired. */
  def userIdFor(token: String, now: Instant): Task[Option[UUID]]

  /** Removes a session (logout). */
  def delete(token: String): Task[Unit]
}
