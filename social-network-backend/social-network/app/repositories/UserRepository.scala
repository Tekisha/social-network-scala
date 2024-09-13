package repositories

import models.{User, Tables}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserRepository @Inject() (override protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] with Tables {

  import profile.api._

  def getAllUsers(page: Int, pageSize: Int): Future[Seq[User]] = {
    val offset = (page - 1) * pageSize
    db.run(users.drop(offset).take(pageSize).result)
  }

  def getUserById(id: Int): Future[Option[User]] = {
    db.run(users.filter(_.id === id).result.headOption)
  }

  def getUserByUsername(username: String): Future[Option[User]] = {
    db.run(users.filter(_.username === username).result.headOption)
  }

  def createNewUser(user: User): Future[User] = {
    val insertAction = (users returning users.map(_.id) into ((user, id) => user.copy(id = Some(id)))) += user
    db.run(insertAction)
  }


  def retrievePasswordByUsername(username: String): Future[Option[String]] = {
    db.run(users.filter(user => user.username === username).map(_.password).result.headOption)
  }

  def deleteUser(id: Int): Future[Int] = {
    db.run(users.filter(_.id === id).delete)
  }

  def updateUser(id: Int, updatedUser: User): Future[Int] = {
    db.run(users.filter(_.id === id).update(updatedUser))
  }

  def deleteAllUsers(): Future[Int] = {
    db.run(users.delete)
  }

  def updateProfilePhoto(userId: Int, filePath: String): Future[Int] = {
    db.run(users.filter(_.id === userId).map(_.profilePhoto).update(Some(filePath)))
  }

  def searchByUsername(authenticatedUserId: Int, username: String, page: Int, pageSize: Int): Future[Seq[(User, Boolean)]] = {
    val offset = (page - 1) * pageSize

    val query = sql"""
    SELECT u.id, u.username, u.profile_photo,
           EXISTS (
               SELECT 1 FROM friendships f
               WHERE (f.user_id1 = u.id AND f.user_id2 = $authenticatedUserId)
               OR (f.user_id1 = $authenticatedUserId AND f.user_id2 = u.id)
           ) AS is_friend
    FROM users u
    WHERE LOWER(u.username) LIKE LOWER(${s"%$username%"})
    LIMIT $pageSize OFFSET $offset
  """.as[(Int, String, Option[String], Boolean)]

    db.run(query).map { result =>
      result.map { case (id, username, profilePhoto, isFriend) =>
        val user = User(Some(id), username, "", profilePhoto)
        (user, isFriend)
      }
    }
  }

  def areFriends(userId1: Int, userId2: Int): Future[Boolean] = {
    val query = friendships.filter { friendship =>
      (friendship.userId1 === userId1 && friendship.userId2 === userId2) ||
        (friendship.userId1 === userId2 && friendship.userId2 === userId1)
    }
    db.run(query.exists.result)
  }
}
