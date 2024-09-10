package repositories

import javax.inject.Inject
import models.{Like, Tables}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class LikeRepository @Inject()(override protected val dbConfigProvider: DatabaseConfigProvider)
                              (implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] with Tables {

  import profile.api._

  def addLike(like: Like): Future[Like] = {
    db.run(likes returning likes.map(_.id) into ((like, id) => like.copy(id = Some(id))) += like)
  }

  def removeLike(userId: Int, postId: Int): Future[Int] = {
    db.run(likes.filter(like => like.userId === userId && like.postId === postId).delete)
  }

  def checkIfLiked(userId: Int, postId: Int): Future[Option[Like]] = {
    db.run(likes.filter(like => like.userId === userId && like.postId === postId).result.headOption)
  }
}
