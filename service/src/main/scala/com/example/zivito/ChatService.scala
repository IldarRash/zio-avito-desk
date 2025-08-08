package com.example.zivito

import com.example.zivito.Domain.{ChatMessage, Dialog}
import zio.Task
import java.util.UUID

trait ChatService {
  def createDialog(userAId: UUID, userBId: UUID): Task[Dialog]
  def dialogsFor(userId: UUID): Task[List[Dialog]]
  def messages(dialogId: UUID): Task[List[ChatMessage]]
  def send(dialogId: UUID, message: ChatMessage): Task[ChatMessage]
}
