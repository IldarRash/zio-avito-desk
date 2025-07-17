package com.example.zivito

import zio._
import zio.json._
import zhttp.http._
import zhttp.http.Method._
import java.util.UUID

object ItemRoutes {
  case class CreateItemRequest(name: String, categoryId: UUID)
  case class UpdateItemRequest(id: UUID, name: String, categoryId: UUID)
  
  object CreateItemRequest {
    implicit val decoder: JsonDecoder[CreateItemRequest] = DeriveJsonDecoder.gen[CreateItemRequest]
  }
  
  object UpdateItemRequest {
    implicit val decoder: JsonDecoder[UpdateItemRequest] = DeriveJsonDecoder.gen[UpdateItemRequest]
  }
  
  def routes: HttpApp[ItemService, Throwable] =
    Http.collectZIO[Request] {
      case req @ GET -> Root / "items" =>
        for {
          items <- ZIO.serviceWithZIO[ItemService](_.getAllItems)
          response = Response.json(items.toJson)
        } yield response
        
      case req @ GET -> Root / "items" / id =>
        for {
          itemId <- ZIO.attempt(UUID.fromString(id))
          item <- ZIO.serviceWithZIO[ItemService](_.getItem(itemId))
          response = item match {
            case Some(item) => Response.json(item.toJson)
            case None => Response.status(Status.NotFound)
          }
        } yield response
        
      case req @ POST -> Root / "items" =>
        for {
          body <- req.bodyAsString
          request <- ZIO.fromEither(body.fromJson[CreateItemRequest])
          item = Domain.Item(
            id = UUID.randomUUID(),
            name = request.name,
            categoryId = request.categoryId
          )
          createdItem <- ZIO.serviceWithZIO[ItemService](_.createItem(item))
          response = Response.json(createdItem.toJson)
        } yield response
        
      case req @ PUT -> Root / "items" =>
        for {
          body <- req.bodyAsString
          request <- ZIO.fromEither(body.fromJson[UpdateItemRequest])
          item = Domain.Item(
            id = request.id,
            name = request.name,
            categoryId = request.categoryId
          )
          updatedItem <- ZIO.serviceWithZIO[ItemService](_.updateItem(item))
          response = Response.json(updatedItem.toJson)
        } yield response
        
      case req @ DELETE -> Root / "items" / id =>
        for {
          itemId <- ZIO.attempt(UUID.fromString(id))
          deleted <- ZIO.serviceWithZIO[ItemService](_.deleteItem(itemId))
          response = if (deleted) Response.ok else Response.status(Status.NotFound)
        } yield response
        
      case req @ GET -> Root / "items" / "category" / categoryId =>
        for {
          catId <- ZIO.attempt(UUID.fromString(categoryId))
          items <- ZIO.serviceWithZIO[ItemService](_.getItemsByCategory(catId))
          response = Response.json(items.toJson)
        } yield response
    }
  }
}