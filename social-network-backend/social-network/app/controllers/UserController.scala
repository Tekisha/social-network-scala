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

@Singleton
class UserController @Inject()(cc: ControllerComponents, userService: UserService, authAction: AuthAction)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  implicit val userFormat: OFormat[User] = Json.format[User]

  // User response that omits sensitive fields like passwordHash
  case class UserResponse(id: Option[Int], username: String)
  implicit val userResponseFormat: OFormat[UserResponse] = Json.format[UserResponse]

  // Case classes for registration and login data
  case class RegistrationData(username: String, password: String)
  implicit val registrationFormat: OFormat[RegistrationData] = Json.format[RegistrationData]

  case class LoginData(username: String, password: String)
  implicit val loginFormat: OFormat[LoginData] = Json.format[LoginData]

  def register: Action[RegistrationData] = Action.async(parse.json[RegistrationData]) { implicit request =>
    val registrationData = request.body
    userService.registerUser(registrationData.username, registrationData.password).map { createdUser =>
      val response = UserResponse(createdUser.id, createdUser.username)
      Created(Json.toJson(response))
    }.recover {
      case ex: UsernameAlreadyExistsException =>
        Conflict(Json.obj("message" -> ex.getMessage))
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
      val userResponses = users.map(user => UserResponse(user.id, user.username))
      Ok(Json.toJson(userResponses))
    }
  }

  def getUserById(id: Int): Action[AnyContent] = authAction.async { implicit request =>
    userService.getUserById(id).map {
      case Some(user) => Ok(Json.toJson(UserResponse(user.id, user.username)))
      case None => NotFound(Json.obj("message" -> s"User with id $id not found"))
    }
  }

  def updateUser: Action[User] = authAction.async(parse.json[User]) { implicit request =>
    val user = request.body
    val authenticatedUserId = request.userId

    userService.updateUser(authenticatedUserId, user).map {
      case Right((updatedUser, newToken)) =>
        Ok(Json.obj(
          "message" -> "User updated successfully",
          "token" -> newToken,
          "user" -> Json.toJson(UserResponse(updatedUser.id, updatedUser.username))
        ))
      case Left(errorMessage) =>
        Conflict(Json.obj("message" -> errorMessage))
    }
  }

  def deleteUser: Action[AnyContent] = authAction.async { implicit request =>
    val authenticatedUserId = request.userId

    userService.deleteUser(authenticatedUserId).map {
      case Right(_) => NoContent
      case Left(errorMessage) => Forbidden(Json.obj("message" -> errorMessage))
    }
  }

}
