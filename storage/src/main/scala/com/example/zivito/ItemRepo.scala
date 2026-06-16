package com.example.zivito

import java.util.UUID

import com.example.zivito.Domain.{Item, ItemFilter, Page}
import zio.Task

trait ItemRepo {

  /** Retrieves an item by its ID. */
  def get(id: UUID): Task[Option[Item]]

  /** Lists items matching the filter, sorted and paginated, plus the total count matching the filter (for pagination controls).
    */
  def list(filter: ItemFilter): Task[Page[Item]]

  /** Inserts a new item (the item carries its already-generated id). */
  def create(item: Item): Task[Item]

  /** Updates the mutable fields of an existing item. Returns the updated item, or `None` if no row matched the id.
    */
  def update(item: Item): Task[Option[Item]]

  /** Deletes an item, returning whether a row was removed. */
  def delete(id: UUID): Task[Boolean]
}
