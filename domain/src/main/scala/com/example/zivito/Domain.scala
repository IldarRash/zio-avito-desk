package com.example.zivito

import java.util.UUID


object Domain {

  case class Item(
                   id: UUID,
                   name: String,
                   categoryId: UUID,
                 )

  case class Category(
                       id: UUID,
                       name: String,
                     )

  case class User(
                   id: UUID,
                   name: String,
                   email: String
                 )
}