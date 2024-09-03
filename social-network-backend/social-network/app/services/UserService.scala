package services

import javax.inject._
import models.User
import repositories.UserRepository
import scala.concurrent.{ExecutionContext, Future}
import utils.JwtUtils

@Singleton
class UserService @Inject() (userRepository: UserRepository)(implicit ec: ExecutionContext) {

  def registerUser(username: String, password: String): Future[User] = {
    userRepository.getUserByUsername(username).flatMap {
      case Some(_) => Future.failed(new Exception("Username already exists"))
      case None =>
        userRepository.createNewUser(User(None, username, password))
    }
  }

  def authenticateUser(username: String, password: String): Future[Option[String]] = {
    userRepository.validateUser(username, password).map {
      case Some(user) =>
        val token = JwtUtils.createToken(user.username, expirationPeriodInDays = 7)
        Some(token)
      case None => None
    }
  }

  def getAllUsers: Future[Seq[User]] = {
    userRepository.getAllUsers
  }

  def getUserById(id: Int): Future[Option[User]] = {
    userRepository.getUserById(id)
  }

  def updateUser(id: Int, user: User, authenticatedUsername: String): Future[Either[String, (User, String)]] = {
    userRepository.getUserById(id).flatMap {
      case Some(existingUser) if existingUser.username == authenticatedUsername =>
        userRepository.getUserByUsername(user.username).flatMap {
          case Some(_) if user.username != existingUser.username =>
            Future.successful(Left("Username already exists"))
          case _ =>
            userRepository.updateUser(id, user).map { affectedRows =>
              if (affectedRows > 0) {
                val newToken = JwtUtils.createToken(user.username, expirationPeriodInDays = 7)
                Right((user.copy(id = Some(id)), newToken))
              } else {
                Left("User update failed")
              }
            }
        }
      case Some(_) => Future.successful(Left("You can only update your own account"))
      case None => Future.successful(Left(s"User with id $id not found"))
    }
  }


  def deleteUser(id: Int, authenticatedUsername: String): Future[Either[String, Unit]] = {
    userRepository.getUserById(id).flatMap {
      case Some(existingUser) if existingUser.username == authenticatedUsername =>
        userRepository.deleteUser(id).map { _ =>
          Right(())
        }
      case Some(_) => Future.successful(Left("You can only delete your own account"))
      case None => Future.successful(Left(s"User with id $id not found"))
    }
  }
}
