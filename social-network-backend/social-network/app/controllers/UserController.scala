package controllers

import javax.inject._
import play.api.mvc._
import repositories.UserRepository
import models.User
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import services.UserService
import actions.AuthAction
import java.sql.SQLIntegrityConstraintViolationException
import exceptions.UsernameAlreadyExistsException
import play.api.libs.Files.TemporaryFile
import java.nio.file.{Files, Path, Paths}
import utils.FileUtils
import dtos.UpdateBasicInfo

@Singleton
class UserController @Inject()(cc: ControllerComponents, userService: UserService, authAction: AuthAction)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  implicit val userFormat: OFormat[User] = Json.format[User]

  // User response that omits sensitive fields like passwordHash
  case class UserResponse(id: Option[Int], username: String, profilePhoto: Option[String])
  implicit val userResponseFormat: OFormat[UserResponse] = Json.format[UserResponse]

  // Case classes for registration and login data
  case class RegistrationData(username: String, password: String)
  implicit val registrationFormat: OFormat[RegistrationData] = Json.format[RegistrationData]

  case class LoginData(username: String, password: String)
  implicit val loginFormat: OFormat[LoginData] = Json.format[LoginData]

  def register: Action[RegistrationData] = Action.async(parse.json[RegistrationData]) { implicit request =>
    val registrationData = request.body
    validateUsername(registrationData.username) match {
      case Left(error) => Future.successful(BadRequest(Json.obj("message" -> error)))
      case Right(username) if registrationData.password.length < 8 =>
        Future.successful(BadRequest(Json.obj("message" -> "Password must be at least 8 characters long")))
      case Right(username) =>
        userService.registerUser(username, registrationData.password).map { createdUser =>
          Created(Json.toJson(UserResponse(createdUser.id, createdUser.username, createdUser.profilePhoto)))
        }.recover {
          case ex: UsernameAlreadyExistsException => Conflict(Json.obj("message" -> ex.getMessage))
        }
    }
  }

  def updateBasicInfo: Action[UpdateBasicInfo] = authAction.async(parse.json[UpdateBasicInfo]) { implicit request =>
    val user = request.body
    val authenticatedUserId = request.userId

    validateUsername(user.username) match {
      case Left(error) => Future.successful(BadRequest(Json.obj("message" -> error)))
      case Right(username) =>
        userService.updateBasicInfo(authenticatedUserId, username).map {
          case Right((updatedUser, newToken)) =>
            Ok(Json.obj(
              "message" -> "User info updated successfully",
              "token" -> newToken,
              "user" -> Json.toJson(UserResponse(updatedUser.id, updatedUser.username, updatedUser.profilePhoto))
            ))
          case Left(errorMessage) =>
            Conflict(Json.obj("message" -> errorMessage))
        }
    }
  }

  def updatePassword: Action[JsValue] = authAction.async(parse.json) { implicit request =>
    val authenticatedUserId = request.userId
    val oldPassword = (request.body \ "oldPassword").as[String]
    val newPassword = (request.body \ "newPassword").as[String]

    if (newPassword.length < 8) {
      Future.successful(BadRequest(Json.obj("message" -> "New password must be at least 8 characters long")))
    } else {
      userService.updatePassword(authenticatedUserId, oldPassword, newPassword).flatMap { result =>
        result match {
          case Right(_) => Future.successful(Ok(Json.obj("message" -> "Password updated successfully")))
          case Left("Incorrect old password") => Future.successful(Unauthorized(Json.obj("message" -> "Incorrect old password")))
          case Left("User not found") => Future.successful(NotFound(Json.obj("message" -> "User not found")))
          case Left(errorMessage) => Future.successful(Conflict(Json.obj("message" -> errorMessage)))
        }
      }
    }
  }

  def login: Action[LoginData] = Action.async(parse.json[LoginData]) { implicit request =>
    val loginData = request.body
    userService.authenticateUser(loginData.username, loginData.password).map {
      case Some(token) => Ok(Json.obj("token" -> token))
      case None => Unauthorized(Json.obj("message" -> "Invalid credentials"))
    }
  }

  def getAllUsers: Action[AnyContent] = authAction.async { implicit request =>
    val pageParam = request.getQueryString("page").map(_.trim)
    val pageSizeParam = request.getQueryString("pageSize").map(_.trim)

    val pageNum = pageParam.flatMap(p => scala.util.Try(p.toInt).toOption).getOrElse(1)
    val pageSizeNum = pageSizeParam.flatMap(ps => scala.util.Try(ps.toInt).toOption).getOrElse(10)

    userService.getAllUsers(pageNum, pageSizeNum).map { users =>
      val userResponses = users.map(user => UserResponse(user.id, user.username, user.profilePhoto))
      Ok(Json.toJson(userResponses))
    }
  }

  def getUserById(id: Int): Action[AnyContent] = authAction.async { implicit request =>
    userService.getUserById(id).map {
      case Some(user) => Ok(Json.toJson(UserResponse(user.id, user.username, user.profilePhoto)))
      case None => NotFound(Json.obj("message" -> s"User with id $id not found"))
    }
  }

  def deleteUser: Action[AnyContent] = authAction.async { implicit request =>
    val authenticatedUserId = request.userId

    userService.deleteUser(authenticatedUserId).map {
      case Right(_) => NoContent
      case Left(errorMessage) => Forbidden(Json.obj("message" -> errorMessage))
    }
  }

  def changeProfilePhoto: Action[MultipartFormData[TemporaryFile]] = authAction.async(parse.multipartFormData) { implicit request =>
    val userId = request.userId
    request.body.file("profile_photo").map { photo =>
      val extension = Paths.get(photo.filename).getFileName.toString.split("\\.").lastOption.getOrElse("jpg")
      val fileSize = photo.fileSize

      if (!FileUtils.isValidExtension(photo.filename)) {
        Future.successful(BadRequest(Json.obj("message" -> "Invalid file type. Only jpg and png are allowed.")))
      } else if (!FileUtils.isValidSize(fileSize)) {
        Future.successful(BadRequest(Json.obj("message" -> "File size exceeds the 5MB limit.")))
      } else {
        val filename = s"$userId.$extension"
        userService.updateProfilePhoto(userId, photo.ref, filename).map { _ =>
          Ok(Json.obj("message" -> "Profile photo updated successfully"))
        }
      }
    }.getOrElse {
      Future.successful(BadRequest(Json.obj("message" -> "Missing file")))
    }
  }

  def searchUsers: Action[AnyContent] = authAction.async { implicit request =>
    val username = request.getQueryString("username").map(_.trim).getOrElse("")
    val page = request.getQueryString("page").flatMap(p => scala.util.Try(p.toInt).toOption).getOrElse(1)
    val pageSize = request.getQueryString("pageSize").flatMap(ps => scala.util.Try(ps.toInt).toOption).getOrElse(10)

    userService.searchUsersByUsername(username, page, pageSize).map { paginatedUsers =>
      if (paginatedUsers.isEmpty) {
        NotFound(Json.obj("message" -> "No users found"))
      } else {
        val userResponses = paginatedUsers.map(user => UserResponse(user.id, user.username, user.profilePhoto))
        Ok(Json.toJson(userResponses))
      }
    }
  }

  private def validateUsername(username: String): Either[String, String] = {
    if (username.trim.isEmpty) {
      Left("Username cannot be empty or just whitespace")
    } else {
      Right(username)
    }
  }
}
