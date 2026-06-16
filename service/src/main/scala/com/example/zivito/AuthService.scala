package com.example.zivito

import zio._

import com.example.zivito.Domain.User

trait AuthService {

  /** Registers a new account and opens a session. Returns the user and the new session token. Fails with [[AppError.Conflict]] if the email is taken.
    */
  def register(email: String, password: String, displayName: String): Task[(User, String)]

  /** Authenticates by email/password and opens a session. Returns the user and the new session token. Fails with [[AppError.Unauthorized]] on bad creds.
    */
  def login(email: String, password: String): Task[(User, String)]

  /** Ends the session for the given token (no-op if unknown). */
  def logout(token: String): Task[Unit]

  /** Resolves the current user from a session token, or `None` if the session is missing/expired.
    */
  def authenticate(token: String): Task[Option[User]]
}
