package controllers

import javax.inject._
import play.api.mvc._
import repositories.UserRepository
import models.User
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

@Singleton
class UserController @Inject()(cc: ControllerComponents, userRepository: UserRepository)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  implicit val userFormat: OFormat[User] = Json.format[User]

  def getAllUsers: Action[AnyContent] = Action.async { implicit request =>
    userRepository.getAllUsers.map { users =>
      Ok(Json.toJson(users))
    }
  }

  def getUserById(id: Int): Action[AnyContent] = Action.async { implicit request =>
    userRepository.getUserById(id).map {
      case Some(user) => Ok(Json.toJson(user))
      case None => NotFound(Json.obj("message" -> s"User with id $id not found"))
    }
  }

  def createUser: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[User].fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> "Invalid data"))),
      user => {
        userRepository.createNewUser(user).map { createdUser =>
          Created(Json.toJson(createdUser))
        }
      }
    )
  }

  def updateUser(id: Int): Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[User].fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> "Invalid data"))),
      user => {
        userRepository.getUserById(id).flatMap {
          case Some(_) =>
            val updatedUser = user.copy(id = Some(id))
            userRepository.updateUser(id, updatedUser).map { _ =>
              Ok(Json.toJson(updatedUser))
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
