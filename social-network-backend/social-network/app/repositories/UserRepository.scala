package repositories

import models.User
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import utils.PasswordUtils

class UserRepository @Inject() (override protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val users = TableQuery[UserTable]

  def getAllUsers: Future[Seq[User]] = {
    db.run(users.result)
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

  private class UserTable(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
    def username = column[String]("username", O.Length(255), O.Unique)
    def password = column[String]("password")

    override def * = (id.?, username, password) <> (User.tupled, User.unapply)
  }
}
