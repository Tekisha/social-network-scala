package services

import javax.inject._
import models.User
import repositories.UserRepository
import scala.concurrent.{ExecutionContext, Future}
import utils.JwtUtils
import exceptions.UsernameAlreadyExistsException
import utils.PasswordUtils
import play.api.libs.Files.TemporaryFile
import java.nio.file.{Files, Path, Paths}

@Singleton
class UserService @Inject() (userRepository: UserRepository)(implicit ec: ExecutionContext) {

  def registerUser(username: String, password: String): Future[User] = {
    userRepository.getUserByUsername(username).flatMap {
      case Some(_) => Future.failed(new UsernameAlreadyExistsException())
      case None =>
        val hashedPassword = PasswordUtils.hashPassword(password)
        val newUser = User(None, username, hashedPassword, None)
        userRepository.createNewUser(newUser)
    }
  }

  def authenticateUser(username: String, password: String): Future[Option[String]] = {
    userRepository.getUserByUsername(username).map {
      case Some(user) if PasswordUtils.checkPassword(password, user.password) =>
        val token = JwtUtils.createToken(user.id.get, user.username, expirationPeriodInDays = 7)
        Some(token)
      case _ => None
    }
  }

  def getAllUsers(page: Int, pageSize: Int): Future[Seq[User]] = {
    userRepository.getAllUsers(page, pageSize)
  }

  def getUserById(id: Int): Future[Option[User]] = {
    userRepository.getUserById(id)
  }

  def updateUser(authenticatedUserId: Int, user: User): Future[Either[String, (User, String)]] = {
    userRepository.getUserById(authenticatedUserId).flatMap {
      case Some(existingUser) =>
        if (user.username != existingUser.username) {
          userRepository.getUserByUsername(user.username).flatMap {
            case Some(_) => Future.successful(Left("Username already exists"))
            case None =>
              performUserUpdate(authenticatedUserId, user, existingUser)
          }
        } else {
          performUserUpdate(authenticatedUserId, user, existingUser)
        }
      case None => Future.successful(Left("User not found"))
    }
  }

  def deleteUser(authenticatedUserId: Int): Future[Either[String, Unit]] = {
    userRepository.getUserById(authenticatedUserId).flatMap {
      case Some(_) =>
        userRepository.deleteUser(authenticatedUserId).map { _ =>
          Right(())
        }
      case None => Future.successful(Left(s"User with id $authenticatedUserId not found"))
    }
  }

  def updateProfilePhoto(userId: Int, photo: TemporaryFile, filename: String): Future[Unit] = {
    val filePath = s"public/images/users/$filename"
    saveProfilePhoto(photo, filePath)

    userRepository.updateProfilePhoto(userId, s"/public/images/users/$filename").map(_ => ())
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

  private def saveProfilePhoto(photo: TemporaryFile, filePath: String): Unit = {
    val path: Path = Paths.get(filePath)
    Files.createDirectories(path.getParent)
    photo.moveTo(path, replace = true)
  }
}
