package controllers

import javax.inject._
import play.api.mvc._
import repositories.UserRepository
import models.User
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import services.UserService

@Singleton
class UserController @Inject()(cc: ControllerComponents, userRepository: UserRepository, userService: UserService)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  implicit val userFormat: OFormat[User] = Json.format[User]

  // User response that omits sensitive fields like passwordHash
  case class UserResponse(id: Option[Int], username: String)
  implicit val userResponseFormat: OFormat[UserResponse] = Json.format[UserResponse]

  // Case classes for registration and login data
  case class RegistrationData(username: String, password: String)
  implicit val registrationFormat: OFormat[RegistrationData] = Json.format[RegistrationData]

  case class LoginData(username: String, password: String)
  implicit val loginFormat: OFormat[LoginData] = Json.format[LoginData]

  def register: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[RegistrationData].fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> "Invalid data"))),
      registrationData => {
        userService.registerUser(registrationData.username, registrationData.password).map { createdUser =>
          val response = UserResponse(createdUser.id, createdUser.username)
          Created(Json.toJson(response))
        }.recover {
          case ex: Exception => Conflict(Json.obj("message" -> ex.getMessage))
        }
      }
    )
  }

  def login: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[LoginData].fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> "Invalid data"))),
      loginData => {
        userService.authenticateUser(loginData.username, loginData.password).map {
          case Some(user) =>
            val response = UserResponse(user.id, user.username)
            Ok(Json.toJson(response))
          case None =>
            Unauthorized(Json.obj("message" -> "Invalid credentials"))
        }
      }
    )
  }

  def getAllUsers: Action[AnyContent] = Action.async { implicit request =>
    userRepository.getAllUsers.map { users =>
      val userResponses = users.map(user => UserResponse(user.id, user.username))
      Ok(Json.toJson(userResponses))
    }
  }

  def getUserById(id: Int): Action[AnyContent] = Action.async { implicit request =>
    userRepository.getUserById(id).map {
      case Some(user) => Ok(Json.toJson(UserResponse(user.id, user.username)))
      case None => NotFound(Json.obj("message" -> s"User with id $id not found"))
    }
  }

  def updateUser(id: Int): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[User].fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> "Invalid data"))),
      user => {
        userRepository.getUserById(id).flatMap {
          case Some(_) =>
            val updatedUser = user.copy(id = Some(id))
            userRepository.updateUser(id, updatedUser).map { _ =>
              Ok(Json.toJson(UserResponse(updatedUser.id, updatedUser.username)))
            }
          case None => Future.successful(NotFound(Json.obj("message" -> s"User with id $id not found")))
        }
      }
    )
  }

  def deleteUser(id: Int): Action[AnyContent] = Action.async { implicit request =>
    userRepository.getUserById(id).flatMap {
      case Some(_) =>
        userRepository.deleteUser(id).map { _ =>
          NoContent
        }
      case None => Future.successful(NotFound(Json.obj("message" -> s"User with id $id not found")))
    }
  }
}
