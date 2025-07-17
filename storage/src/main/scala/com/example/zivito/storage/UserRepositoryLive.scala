package com.example.zivito.storage

import com.example.zivito.domain.{User, UserRepository}
import io.getquill._
import io.getquill.jdbczio.QuillDataSource
import zio._

import java.sql.Types
import javax.sql.DataSource

class UserRepositoryLive(dataSource: DataSource) extends UserRepository[object Object]
  val ctx = new H2cContext(SnakeCase, dataSource)
  import ctx._

  private val users = quote(querySchema[User]("users"))

  override def findAll: Task[List[User]] = {
    run(quote(users))
  }

  override def findById(id: Long): Task[Option[User]] = {
    run(quote(users.filter(_.id.contains(lift(id)))).map(_.headOption)
  }

  override def create(user: User): TaskUser] = {
    run(quote(users.insertValue(lift(user)).returning(_.id)))
      .map(id => user.copy(id = Some(id)))
  }

  override def update(user: User): Task[User] = {
    user.id match[object Object]      case Some(id) =>
        run(quote(users.filter(_.id.contains(lift(id))).updateValue(lift(user))))
          .map(_ => user)
      case None => ZIO.fail(new IllegalArgumentException(User must have an ID to update"))
    }
  }

  override def delete(id: Long): Task[Boolean] = {
    run(quote(users.filter(_.id.contains(lift(id))).delete))
      .map(_ > 0)
  }
}

object UserRepositoryLive {
  val layer: ZLayer[DataSource, Nothing, UserRepository] =
    ZLayer {
      for {
        dataSource <- ZIO.service[DataSource]
      } yield new UserRepositoryLive(dataSource)
    }
}