package com.example.zivito

import com.example.zivito.Domain.{ChatMessage, Dialog}
import zio.{Task, ZLayer}
import java.util.UUID

final case class ChatServiceImpl(repo: ChatRepo) extends ChatService {
  override def createDialog(userAId: UUID, userBId: UUID): Task[Dialog] = repo.createDialog(userAId, userBId)
  override def dialogsFor(userId: UUID): Task[List[Dialog]] = repo.dialogsFor(userId).map(_.toList)
  override def messages(dialogId: UUID): Task[List[ChatMessage]] = repo.messages(dialogId).map(_.toList)
  override def send(dialogId: UUID, message: ChatMessage): Task[ChatMessage] = repo.send(dialogId, message)
}

object ChatServiceImpl {
  val layer: ZLayer[ChatRepo, Nothing, ChatService] = ZLayer.fromFunction(ChatServiceImpl(_))
}

