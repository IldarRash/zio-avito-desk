package com.example.zivito

import caliban.GraphQL
import caliban.RootResolver
import caliban.schema.Annotations.GQLDescription
import zio._
import java.util.UUID
import com.example.zivito.Domain.ItemSearchFilters

object GraphQLApi {

  @GQLDescription("Input for creating an item")
  final case class CreateItemInput(
    name: String,
    description: String,
    price: BigDecimal,
    categoryId: UUID,
    location: String
  )

  case class Queries(
    @GQLDescription("List all items")
    items: ZIO[ItemService, Throwable, List[Domain.Item]],
    @GQLDescription("Get item by id")
    item: UUID => ZIO[ItemService, Throwable, Option[Domain.Item]],
    @GQLDescription("Search items by query")
    search: String => ZIO[ItemService, Throwable, List[Domain.Item]],
    @GQLDescription("Advanced search by filters")
    searchByFilters: ItemSearchFilters => ZIO[ItemService, Throwable, List[Domain.Item]],
    @GQLDescription("List all categories")
    categories: ZIO[CategoryService, Throwable, List[Domain.Category]]
  )

  case class Mutations(
    @GQLDescription("Create a new item")
    createItem: CreateItemInput => ZIO[ItemService, Throwable, Domain.Item],
    @GQLDescription("Delete item by id")
    deleteItem: UUID => ZIO[ItemService, Throwable, Boolean],
    @GQLDescription("Register a new user")
    register: (String, String, String) => ZIO[AuthService, Throwable, AuthDomain.User],
    @GQLDescription("Login and obtain JWT token")
    login: (String, String) => ZIO[AuthService, Throwable, AuthDomain.JwtToken]
  )

  val api: GraphQL[ItemService with CategoryService] = {
    val queries = Queries(
      items = ZIO.serviceWithZIO[ItemService](_.getAll),
      item = (id: UUID) => ZIO.serviceWithZIO[ItemService](_.get(id)),
      search = (q: String) => ZIO.serviceWithZIO[ItemService](_.search(q)),
      searchByFilters = (f: ItemSearchFilters) => ZIO.serviceWithZIO[ItemService](_.search(f)),
      categories = ZIO.serviceWithZIO[CategoryService](_.getAllCategories)
    )

    val mutations = Mutations(
      createItem = (in: CreateItemInput) =>
        ZIO.serviceWithZIO[ItemService](_.create(Domain.Item(UUID.randomUUID(), in.name, in.description, in.price, in.categoryId, in.location))),
      deleteItem = (id: UUID) =>
        ZIO.serviceWithZIO[ItemService](_.delete(id)).as(true),
      register = (name: String, email: String, password: String) =>
        ZIO.serviceWithZIO[AuthService](_.register(name, email, password)),
      login = (email: String, password: String) =>
        ZIO.serviceWithZIO[AuthService](_.login(email, password))
    )

    GraphQL.graphQL(RootResolver(queries, mutations))
  }
}
