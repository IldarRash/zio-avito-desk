package com.example.zivito

import caliban.GraphQL
import caliban.RootResolver
import caliban.schema.Annotations.GQLDescription
import zio._
import java.util.UUID
import com.example.zivito.Domain. {ItemSearchFilters, ChatMessage, Dialog}

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
    categories: ZIO[CategoryService, Throwable, List[Domain.Category]],
    @GQLDescription("Chat dialogs for a user")
    dialogsFor: UUID => ZIO[ChatService, Throwable, List[Dialog]],
    @GQLDescription("Messages for a dialog")
    messages: UUID => ZIO[ChatService, Throwable, List[ChatMessage]],
    @GQLDescription("Get my cart items")
    myCart: UUID => ZIO[CartService, Throwable, List[Domain.Item]],
    @GQLDescription("Get my orders")
    myOrders: UUID => ZIO[OrderService, Throwable, List[Domain.Order]]
  )

  case class Mutations(
    @GQLDescription("Create a new item")
    createItem: CreateItemInput => ZIO[ItemService, Throwable, Domain.Item],
    @GQLDescription("Delete item by id")
    deleteItem: UUID => ZIO[ItemService, Throwable, Boolean],
    @GQLDescription("Register a new user")
    register: (String, String, String) => ZIO[AuthService, Throwable, AuthDomain.User],
    @GQLDescription("Login and obtain JWT token")
    login: (String, String) => ZIO[AuthService, Throwable, AuthDomain.JwtToken],
    @GQLDescription("Create a dialog between two users")
    createDialog: (UUID, UUID) => ZIO[ChatService, Throwable, Dialog],
    @GQLDescription("Send a chat message")
    sendMessage: (UUID, UUID, String) => ZIO[ChatService, Throwable, ChatMessage],
    @GQLDescription("Add item to cart")
    addToCart: (UUID, UUID) => ZIO[CartService, Throwable, Boolean],
    @GQLDescription("Remove item from cart")
    removeFromCart: (UUID, UUID) => ZIO[CartService, Throwable, Boolean],
    @GQLDescription("Checkout cart to order")
    checkout: UUID => ZIO[CartService, Throwable, Domain.Order]
  )

  val api: GraphQL[ItemService with CategoryService with ChatService with AuthService with CartService with OrderService] = {
    val queries = Queries(
      items = ZIO.serviceWithZIO[ItemService](_.getAll),
      item = (id: UUID) => ZIO.serviceWithZIO[ItemService](_.get(id)),
      search = (q: String) => ZIO.serviceWithZIO[ItemService](_.search(q)),
      searchByFilters = (f: ItemSearchFilters) => ZIO.serviceWithZIO[ItemService](_.search(f)),
      categories = ZIO.serviceWithZIO[CategoryService](_.getAllCategories),
      dialogsFor = (userId: UUID) => ZIO.serviceWithZIO[ChatService](_.dialogsFor(userId)),
      messages = (dialogId: UUID) => ZIO.serviceWithZIO[ChatService](_.messages(dialogId)),
      myCart = (userId: UUID) => ZIO.serviceWithZIO[CartService](_.getCartItems(userId)),
      myOrders = (userId: UUID) => ZIO.serviceWithZIO[OrderService](_.getOrders(userId))
    )

    val mutations = Mutations(
      createItem = (in: CreateItemInput) =>
        ZIO.serviceWithZIO[ItemService](_.create(Domain.Item(UUID.randomUUID(), in.name, in.description, in.price, in.categoryId, in.location))),
      deleteItem = (id: UUID) =>
        ZIO.serviceWithZIO[ItemService](_.delete(id)).as(true),
      register = (name: String, email: String, password: String) =>
        ZIO.serviceWithZIO[AuthService](_.register(name, email, password)),
      login = (email: String, password: String) =>
        ZIO.serviceWithZIO[AuthService](_.login(email, password)),
      createDialog = (userA: UUID, userB: UUID) =>
        ZIO.serviceWithZIO[ChatService](_.createDialog(userA, userB)),
      sendMessage = (dialogId: UUID, senderId: UUID, text: String) =>
        ZIO.succeed(ChatMessage(UUID.randomUUID(), dialogId, senderId, text, java.time.Instant.EPOCH)).flatMap { msg =>
          ZIO.serviceWithZIO[ChatService](_.send(dialogId, msg))
        },
      addToCart = (userId: UUID, itemId: UUID) =>
        ZIO.serviceWithZIO[CartService](_.addToCart(userId, itemId)).as(true),
      removeFromCart = (userId: UUID, itemId: UUID) =>
        ZIO.serviceWithZIO[CartService](_.removeFromCart(userId, itemId)).as(true),
      checkout = (userId: UUID) =>
        ZIO.serviceWithZIO[CartService](_.checkout(userId))
    )

    GraphQL.graphQL(RootResolver(queries, mutations))
  }
}
