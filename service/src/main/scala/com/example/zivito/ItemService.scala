package com.example.zivito

import zio._
import java.util.UUID

trait ItemService {

  /**
   * Creates a new item.
   *
   * @param item The item to create.
   * @return The created item.
   */
  def create(item: Domain.Item): Task[Domain.Item]

  /**
   * Retrieves an item by its ID.
   *
   * @param id The ID of the item to retrieve.
   * @return An optional item.
   */
  def get(id: UUID): Task[Option[Domain.Item]]

  /**
   * Retrieves all items.
   *
   * @return A list of all items.
   */
  def getAll: Task[List[Domain.Item]]

  /**
    * Searches for items based on a query.
    *
    * @param query The search query.
    * @return A list of items matching the query.
    */
  def search(query: String): Task[List[Domain.Item]]

  /**
   * Deletes an item by its ID.
   *
   * @param id The ID of the item to delete.
   * @return A boolean indicating whether the item was deleted.
   */
  def delete(id: UUID): Task[Unit]

  /**
   * Retrieves all items in a given category.
   *
   * @param categoryId The ID of the category.
   * @return A list of items in the category.
   */
  def getItemsByCategory(categoryId: UUID): Task[List[Domain.Item]]
}