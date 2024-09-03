package services

import javax.inject._
import models.User
import repositories.UserRepository
import scala.concurrent.{ExecutionContext, Future}
import utils.JwtUtils
import exceptions.UsernameAlreadyExistsException

@Singleton
class UserService @Inject() (userRepository: UserRepository)(implicit ec: ExecutionContext) {

  def registerUser(username: String, password: String): Future[User] = {
    userRepository.getUserByUsername(username).flatMap {
      case Some(_) => Future.failed(new UsernameAlreadyExistsException())
      case None =>
        userRepository.createNewUser(User(None, username, password))
    }
  }

  def authenticateUser(username: String, password: String): Future[Option[String]] = {
    userRepository.validateUser(username, password).map {
      case Some(user) =>
        val token = JwtUtils.createToken(user.id.get, user.username, expirationPeriodInDays = 7)
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

  def updateUser(id: Int, user: User, authenticatedUserId: Int): Future[Either[String, (User, String)]] = {
    if (id != authenticatedUserId) {
      Future.successful(Left("You can only update your own account"))
    } else {
      userRepository.getUserById(id).flatMap {
        case Some(existingUser) =>
          if (user.username != existingUser.username) {
            userRepository.getUserByUsername(user.username).flatMap {
              case Some(_) => Future.successful(Left("Username already exists"))
              case None =>
                performUserUpdate(id, user, existingUser)
            }
          } else {
            performUserUpdate(id, user, existingUser)
          }
        case None => Future.successful(Left("User not found"))
      }
    }
  }

  def deleteUser(id: Int, authenticatedUserId: Int): Future[Either[String, Unit]] = {
    if (id != authenticatedUserId) {
      Future.successful(Left("You can only delete your own account"))
    } else {
      userRepository.getUserById(id).flatMap {
        case Some(_) =>
          userRepository.deleteUser(id).map { _ =>
            Right(())
          }
        case None => Future.successful(Left(s"User with id $id not found"))
      }
    }
  }

  private def performUserUpdate(id: Int, user: User, existingUser: User): Future[Either[String, (User, String)]] = {
    userRepository.updateUser(id, user).map { _ =>
      val newToken = JwtUtils.createToken(id, user.username, expirationPeriodInDays = 7)
      Right((user.copy(id = Some(id)), newToken))
    }
  }
}
