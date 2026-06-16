package com.example.zivito

import zio.*

/** Small reusable field validators. Each fails with [[AppError.Validation]] so the route layer maps them to `400 Bad Request`.
  */
object Validate {

  /** Trims and requires a non-empty, not-too-long string. */
  def text(field: String, value: String, maxLen: Int): IO[AppError, String] = {
    val v = value.trim
    if (v.isEmpty) ZIO.fail(AppError.Validation(field, "must not be empty"))
    else if (v.length > maxLen) ZIO.fail(AppError.Validation(field, s"must be at most $maxLen characters"))
    else ZIO.succeed(v)
  }

  /** Optional string with a max length; normalizes blank/empty to "". */
  def optionalText(field: String, value: String, maxLen: Int): IO[AppError, String] = {
    val v = value.trim
    if (v.length > maxLen) ZIO.fail(AppError.Validation(field, s"must be at most $maxLen characters"))
    else ZIO.succeed(v)
  }

  def nonNegative(field: String, value: BigDecimal): IO[AppError, BigDecimal] =
    if (value < 0) ZIO.fail(AppError.Validation(field, "must not be negative"))
    else if (value.scale > 2) ZIO.fail(AppError.Validation(field, "must have at most 2 decimal places"))
    else ZIO.succeed(value)
}
