package com.example.zivito

import com.example.zivito.Domain.{ChatMessage, Dialog}
import zio.{Task, ZIO}
import java.util.UUID

trait ChatRepo {
  def dialogsFor(userId: UUID): Task[Seq[Dialog]]
  def messages(dialogId: UUID): Task[Seq[ChatMessage]]
  def send(dialogId: UUID, message: ChatMessage): Task[ChatMessage]
}

object ChatRepo {
  def dialogsFor(userId: UUID): ZIO[ChatRepo, Throwable, Seq[Dialog]] = ZIO.serviceWithZIO[ChatRepo](_.dialogsFor(userId))
  def messages(dialogId: UUID): ZIO[ChatRepo, Throwable, Seq[ChatMessage]] = ZIO.serviceWithZIO[ChatRepo](_.messages(dialogId))
  def send(dialogId: UUID, message: ChatMessage): ZIO[ChatRepo, Throwable, ChatMessage] = ZIO.serviceWithZIO[ChatRepo](_.send(dialogId, message))
}
