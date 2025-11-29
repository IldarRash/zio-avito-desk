package com.example.zivito

import com.example.zivito.Domain.{Item, Order}
import zio.{Task, ZIO, ZLayer}
import java.util.UUID
import java.time.Instant

trait CartService {
  def addToCart(userId: UUID, itemId: UUID): Task[Unit]
  def removeFromCart(userId: UUID, itemId: UUID): Task[Unit]
  def getCartItems(userId: UUID): Task[List[Item]]
  def checkout(userId: UUID): Task[Order]
}

final case class CartServiceImpl(cartRepo: CartRepo, itemRepo: ItemRepo, orderRepo: OrderRepo) extends CartService {
  override def addToCart(userId: UUID, itemId: UUID): Task[Unit] =
    cartRepo.addItem(userId, itemId)

  override def removeFromCart(userId: UUID, itemId: UUID): Task[Unit] =
    cartRepo.removeItem(userId, itemId)

  override def getCartItems(userId: UUID): Task[List[Item]] =
    for {
      itemIds <- cartRepo.getItems(userId)
      items   <- ZIO.foreach(itemIds)(id => itemRepo.get(id)).map(_.flatten.toList)
    } yield items

  override def checkout(userId: UUID): Task[Order] =
    for {
      items <- getCartItems(userId)
      if items.nonEmpty
      totalPrice = items.map(_.price).sum
      order = Order(
        id = UUID.randomUUID(),
        userId = userId,
        itemIds = items.map(_.id),
        totalPrice = totalPrice,
        status = "CREATED",
        createdAt = Instant.now()
      )
      savedOrder <- orderRepo.create(order)
      _ <- cartRepo.clearCart(userId)
    } yield savedOrder
}

object CartServiceImpl {
  val layer: ZLayer[CartRepo with ItemRepo with OrderRepo, Nothing, CartService] =
    ZLayer.fromFunction(CartServiceImpl(_, _, _))
}

