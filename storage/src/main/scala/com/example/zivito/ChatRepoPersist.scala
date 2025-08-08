package com.example.zivito

import com.example.zivito.Domain.{ChatMessage, Dialog}
import io.getquill.{Escape, H2ZioJdbcContext}
import io.getquill.context.ZioJdbc.DataSourceLayer
import javax.sql.DataSource
import zio.{Random, Task, ZLayer}
import java.util.UUID
import java.time.Instant

final case class DialogTable(id: UUID, userAId: UUID, userBId: UUID, lastMessageAt: Instant)
final case class MessageTable(id: UUID, dialogId: UUID, senderId: UUID, text: String, createdAt: Instant)

final case class ChatRepoPersist(ds: DataSource) extends ChatRepo {
  private val ctx = new H2ZioJdbcContext(Escape)
  import ctx._

  private def toDialog(t: DialogTable): Dialog = Dialog(t.id, t.userAId, t.userBId, t.lastMessageAt)
  private def toMessage(t: MessageTable): ChatMessage = ChatMessage(t.id, t.dialogId, t.senderId, t.text, t.createdAt)

  override def createDialog(userAId: UUID, userBId: UUID): Task[Dialog] =
    for {
      id <- Random.nextUUID
      now = Instant.now()
      _ <- ctx.run(query[DialogTable].insertValue(lift(DialogTable(id, userAId, userBId, now))))
    } yield Dialog(id, userAId, userBId, now)

  override def dialogsFor(userId: UUID): Task[Seq[Dialog]] =
    ctx.run(query[DialogTable].filter(d => d.userAId == lift(userId) || d.userBId == lift(userId))).map(_.map(toDialog)).provide(ZLayer.succeed(ds))

  override def messages(dialogId: UUID): Task[Seq[ChatMessage]] =
    ctx.run(query[MessageTable].filter(_.dialogId == lift(dialogId)).sortBy(_.createdAt)(Ord.asc)).map(_.map(toMessage)).provide(ZLayer.succeed(ds))

  override def send(dialogId: UUID, message: ChatMessage): Task[ChatMessage] =
    for {
      id <- Random.nextUUID
      now = Instant.now()
      saved = message.copy(id = id, dialogId = dialogId, createdAt = now)
      _ <- ctx.run(query[MessageTable].insertValue(lift(MessageTable(saved.id, saved.dialogId, saved.senderId, saved.text, saved.createdAt))))
      _ <- ctx.run(query[DialogTable].filter(_.id == lift(dialogId)).update(_.lastMessageAt -> lift(now)))
    } yield saved
}

object ChatRepoPersist {
  def layer: ZLayer[DataSource, Throwable, ChatRepo] =
    DataSourceLayer.fromPrefix("App") >>> ZLayer.fromFunction(ChatRepoPersist(_))
}
