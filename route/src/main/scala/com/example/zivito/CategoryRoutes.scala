package com.example.zivito

import zio._
import zio.json._
import zhttp.http._
import zhttp.http.Method._
import java.util.UUID

object CategoryRoutes {
  case class CreateCategoryRequest(name: String)
  case class UpdateCategoryRequest(id: UUID, name: String)
  
  object CreateCategoryRequest {
    implicit val decoder: JsonDecoder[CreateCategoryRequest] = DeriveJsonDecoder.gen[CreateCategoryRequest]
  }
  
  object UpdateCategoryRequest {
    implicit val decoder: JsonDecoder[UpdateCategoryRequest] = DeriveJsonDecoder.gen[UpdateCategoryRequest]
  }
  
  def routes: HttpApp[CategoryService, Throwable] =
    Http.collectZIO[Request] {
      case req @ GET -> Root / "categories" =>
        for {
          categories <- ZIO.serviceWithZIO[CategoryService](_.getAllCategories)
          response = Response.json(categories.toJson)
        } yield response
        
      case req @ GET -> Root / "categories" / id =>
        for {
          categoryId <- ZIO.attempt(UUID.fromString(id))
          category <- ZIO.serviceWithZIO[CategoryService](_.getCategory(categoryId))
          response = category match {
            case Some(category) => Response.json(category.toJson)
            case None => Response.status(Status.NotFound)
          }
        } yield response
        
      case req @ POST -> Root / "categories" =>
        for {
          body <- req.bodyAsString
          request <- ZIO.fromEither(body.fromJson[CreateCategoryRequest])
          category = Domain.Category(
            id = UUID.randomUUID(),
            name = request.name
          )
          createdCategory <- ZIO.serviceWithZIO[CategoryService](_.createCategory(category))
          response = Response.json(createdCategory.toJson)
        } yield response
        
      case req @ PUT -> Root / "categories" =>
        for {
          body <- req.bodyAsString
          request <- ZIO.fromEither(body.fromJson[UpdateCategoryRequest])
          category = Domain.Category(
            id = request.id,
            name = request.name
          )
          updatedCategory <- ZIO.serviceWithZIO[CategoryService](_.updateCategory(category))
          response = Response.json(updatedCategory.toJson)
        } yield response
        
      case req @ DELETE -> Root / "categories" / id =>
        for {
          categoryId <- ZIO.attempt(UUID.fromString(id))
          deleted <- ZIO.serviceWithZIO[CategoryService](_.deleteCategory(categoryId))
          response = if (deleted) Response.ok else Response.status(Status.NotFound)
        } yield response
    }
  }
}