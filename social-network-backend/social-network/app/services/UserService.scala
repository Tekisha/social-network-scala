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
import dtos.UserResponse

@Singleton
class UserService @Inject() (userRepository: UserRepository)(implicit ec: ExecutionContext) {

  def registerUser(username: String, password: String): Future[User] = {
    userRepository.getUserByUsername(username).flatMap {
      case Some(_) => Future.failed(new UsernameAlreadyExistsException())
      case None =>
        val hashedPassword = PasswordUtils.hashPassword(password)
        val newUser = User(None, username, hashedPassword,  Some("/assets/images/default.png"))
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

  def getUserById(id: Int, authenticatedUserId: Int): Future[Option[UserResponse]] = {
    for {
      userOpt <- userRepository.getUserById(id)
      isFriend <- if (id == authenticatedUserId) Future.successful(false)
      else userRepository.areFriends(authenticatedUserId, id)
      pendingRequest <- if (id == authenticatedUserId) Future.successful(false)
      else userRepository.hasPendingFriendRequestFromAuthenticatedUser(authenticatedUserId, id)
    } yield {
      userOpt.map { user =>
        UserResponse(
          id = user.id,
          username = user.username,
          profilePhoto = user.profilePhoto,
          isFriend = isFriend,
          pendingRequest = pendingRequest
        )
      }
    }
  }

  def updateBasicInfo(authenticatedUserId: Int, newUsername: String): Future[Either[String, (User, String)]] = {
    userRepository.getUserById(authenticatedUserId).flatMap {
      case Some(existingUser) =>
        if (newUsername != existingUser.username) {
          userRepository.getUserByUsername(newUsername).flatMap {
            case Some(_) => Future.successful(Left("Username already exists"))
            case None =>
              val updatedUser = existingUser.copy(username = newUsername)
              userRepository.updateUser(authenticatedUserId, updatedUser).map { updateCount =>
                if (updateCount > 0) generateUpdatedToken(updatedUser)
                else Left("Failed to update user")
              }
          }
        } else {
          Future.successful(Left("Username is the same as the current one"))
        }
      case None => Future.successful(Left("User not found"))
    }
  }

  def updatePassword(authenticatedUserId: Int, oldPassword: String, newPassword: String): Future[Either[String, Unit]] = {
    userRepository.getUserById(authenticatedUserId).flatMap {
      case Some(existingUser) =>
        if (!PasswordUtils.checkPassword(oldPassword, existingUser.password)) {
          Future.successful(Left("Incorrect old password"))
        } else {
          val hashedNewPassword = PasswordUtils.hashPassword(newPassword)
          val updatedUser = existingUser.copy(password = hashedNewPassword)

          userRepository.updateUser(authenticatedUserId, updatedUser).map { updateCount =>
            if (updateCount > 0) {
              Right(())
            } else {
              Left("Failed to update password")
            }
          }
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

    userRepository.updateProfilePhoto(userId, s"/assets/images/users/$filename").map(_ => ())
  }

  def searchUsersByUsername(username: String, authenticatedUserId: Int, page: Int, pageSize: Int): Future[Seq[UserResponse]] = {
    userRepository.searchByUsername(authenticatedUserId, username, page, pageSize).map { usersWithFriendFlag =>
      usersWithFriendFlag.map { case (user, isFriend) =>
        UserResponse(user.id, user.username, user.profilePhoto, isFriend)
      }
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
