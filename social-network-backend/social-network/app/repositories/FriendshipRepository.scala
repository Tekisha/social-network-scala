package repositories

import models.{Tables, Friendship}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FriendshipRepository @Inject()(override protected val dbConfigProvider: DatabaseConfigProvider)
                                    (implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] with Tables {

  import profile.api._

  def addFriend(userId1: Int, userId2: Int): DBIO[Friendship] = {
    val friendship = Friendship(None, userId1, userId2)
    friendships returning friendships.map(_.id) into ((friendship, id) => friendship.copy(id = Some(id))) += friendship
  }

  def removeFriend(userId1: Int, userId2: Int): Future[Int] = {
    db.run(friendships.filter(f => (f.userId1 === userId1 && f.userId2 === userId2) || (f.userId1 === userId2 && f.userId2 === userId1)).delete)
  }

  def getFriends(userId: Int): Future[Seq[Friendship]] = {
    db.run(friendships.filter(f => f.userId1 === userId || f.userId2 === userId).result)
  }

  def areFriends(userId1: Int, userId2: Int): Future[Boolean] = {
    db.run(friendships.filter(f => (f.userId1 === userId1 && f.userId2 === userId2) || (f.userId1 === userId2 && f.userId2 === userId1)).exists.result)
  }
}
