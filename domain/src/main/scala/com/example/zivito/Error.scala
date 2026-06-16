package com.example.zivito

/** Typed application errors. They extend `Throwable` so they flow through the existing `Task` (error channel = `Throwable`) signatures unchanged, while the
  * route layer pattern-matches them to map onto the right HTTP status.
  */
sealed abstract class AppError(message: String) extends Throwable(message)

object AppError {

  /** The requested resource does not exist. Maps to `404`. */
  final case class NotFound(what: String) extends AppError(s"$what not found")

  /** A request field failed validation. Maps to `400`. */
  final case class Validation(field: String, reason: String) extends AppError(s"$field $reason")

  /** Authentication is required or the session is invalid. Maps to `401`. */
  final case class Unauthorized(reason: String = "Authentication required") extends AppError(reason)

  /** The caller is authenticated but not allowed to perform the action. Maps to `403`. */
  final case class Forbidden(reason: String = "You do not have permission to perform this action") extends AppError(reason)

  /** The request conflicts with current state (e.g. duplicate). Maps to `409`. */
  final case class Conflict(reason: String) extends AppError(reason)
}
