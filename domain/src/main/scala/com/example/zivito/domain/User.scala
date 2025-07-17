package com.example.zivito.domain

import zio.json._

case class User(
  id: Option[Long],
  name: String,
  email: String,
  age: Int
)

object User {
  implicit val encoder: JsonEncoder[User] = DeriveJsonEncoder.gen[User]
  implicit val decoder: JsonDecoder[User] = DeriveJsonDecoder.gen[User]
}