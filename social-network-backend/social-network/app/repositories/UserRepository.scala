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

  def createSchemaIfNotExists(): Future[Unit] = {
    val schema = users.schema
    db.run(DBIO.seq(
      schema.createIfNotExists
    )).map(_=>())
  }

  def getAllUsers: Future[Seq[User]] = {
    db.run(users.result)
  }

  def getUserById(id: Int): Future[Option[User]] = {
    db.run(users.filter(_.id === id).result.headOption)
  }

  def getUserByUsername(username: String): Future[Option[User]] = {
    db.run(users.filter(_.username === username).result.headOption)
  }

  def validateUser(username: String, password: String): Future[Option[User]] = {
    getUserByUsername(username).map {
      case Some(user) if PasswordUtils.checkPassword(password, user.password) => Some(user)
      case _ => None
    }
  }

  def createNewUser(user: User): Future[User] = {
    val hashedPassword = PasswordUtils.hashPassword(user.password)
    val userWithHashedPassword = user.copy(password = hashedPassword)
    val insertAction = (users returning users.map(_.id) into ((user, id) => userWithHashedPassword.copy(id = Some(id)))) += userWithHashedPassword
    db.run(insertAction)
  }

  def retrievePasswordByUsername(username: String): Future[Option[String]] = {
    db.run(users.filter(user => user.username === username).map(_.password).result.headOption)
  }

  def deleteUser(id: Int): Future[Int] = {
    db.run(users.filter(_.id === id).delete)
  }

  def updateUser(id: Int, user: User): Future[Int] = {
    getUserById(id).flatMap {
      case Some(existingUser) =>
        val updatedUser = if (user.password != existingUser.password) {
          user.copy(password = PasswordUtils.hashPassword(user.password))
        } else {
          user
        }

        db.run(users.filter(_.id === id).update(updatedUser))

      case None =>
        Future.successful(0)
    }
  }

  private class UserTable(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Int]("UserID", O.AutoInc, O.PrimaryKey)
    def username = column[String]("Username")
    def password = column[String]("Password")

    override def * = (id.?, username, password) <> (User.tupled, User.unapply)
  }
}
