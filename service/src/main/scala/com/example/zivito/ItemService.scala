package com.example.zivito

import zio._
import java.util.UUID

import com.example.zivito.Domain.{Item, ItemFilter, Page}

trait ItemService {

  /** Creates a new item. */
  def create(item: Item): Task[Item]

  /** Updates an existing item; `None` if no item has that id. */
  def update(item: Item): Task[Option[Item]]

  /** Retrieves an item by its ID. */
  def get(id: UUID): Task[Option[Item]]

  /** Lists items matching the filter, sorted and paginated, with a total count. */
  def list(filter: ItemFilter): Task[Page[Item]]

  /** Retrieves all items (unpaginated). */
  def getAll: Task[List[Item]]

  /** Searches items by name/description. */
  def search(query: String): Task[List[Item]]

  /** Retrieves all items in a given category. */
  def getItemsByCategory(categoryId: UUID): Task[List[Item]]

  /** Deletes an item, returning whether it existed. */
  def delete(id: UUID): Task[Boolean]
}
