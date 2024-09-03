package services

import javax.inject._
import models.User
import repositories.UserRepository
import scala.concurrent.{ExecutionContext, Future}
import utils.JwtUtils
import exceptions.UsernameAlreadyExistsException
import utils.PasswordUtils

@Singleton
class UserService @Inject() (userRepository: UserRepository)(implicit ec: ExecutionContext) {

  def registerUser(username: String, password: String): Future[User] = {
    userRepository.getUserByUsername(username).flatMap {
      case Some(_) => Future.failed(new UsernameAlreadyExistsException())
      case None =>
        val hashedPassword = PasswordUtils.hashPassword(password)
        val newUser = User(None, username, hashedPassword)
        userRepository.createNewUser(newUser)
    }
  }

  def authenticateUser(username: String, password: String): Future[Option[String]] = {
    validateUser(username, password).map {
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
    val updatedUser = prepareUpdatedUser(id, user, existingUser)

    userRepository.updateUser(id, updatedUser).map { updateCount =>
      if (updateCount > 0) {
        generateUpdatedToken(updatedUser)
      } else {
        Left("Failed to update user")
      }
    }
  }

  private def prepareUpdatedUser(id: Int, user: User, existingUser: User): User = {
    if (user.password != existingUser.password) {
      user.copy(id = Some(id), password = PasswordUtils.hashPassword(user.password))
    } else {
      user.copy(id = Some(id))
    }
  }

  private def generateUpdatedToken(user: User): Either[String, (User, String)] = {
    val newToken = JwtUtils.createToken(user.id.get, user.username, expirationPeriodInDays = 7)
    Right((user, newToken))
  }

  private def validateUser(username: String, password: String): Future[Option[User]] = {
    userRepository.getUserByUsername(username).map {
      case Some(user) if PasswordUtils.checkPassword(password, user.password) => Some(user)
      case _ => None
    }
  }
}
