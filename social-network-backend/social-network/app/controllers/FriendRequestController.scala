package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import services.FriendRequestService
import actions.AuthAction
import scala.concurrent.{ExecutionContext, Future}
import models.FriendRequest
import utils.JsonFormatUtils._

@Singleton
class FriendRequestController @Inject()(cc: ControllerComponents, friendRequestService: FriendRequestService, authAction: AuthAction)
                                       (implicit ec: ExecutionContext) extends AbstractController(cc) {

  implicit val friendRequestFormat: OFormat[FriendRequest] = Json.format[FriendRequest]

  case class SendFriendRequestData(receiverId: Int)
  implicit val sendFriendRequestFormat: OFormat[SendFriendRequestData] = Json.format[SendFriendRequestData]

  case class RespondFriendRequestData(status: String)
  implicit val respondFriendRequestFormat: OFormat[RespondFriendRequestData] = Json.format[RespondFriendRequestData]

  def sendFriendRequest: Action[JsValue] = authAction.async(parse.json) { implicit request =>
    val requesterId = request.userId
    val receiverId = (request.body \ "receiverId").as[Int]

    friendRequestService.sendRequest(requesterId, receiverId).map { friendRequest =>
      Created(Json.toJson(friendRequest))
    }
  }

  def respondToRequest(requestId: Int): Action[RespondFriendRequestData] = authAction.async(parse.json[RespondFriendRequestData]) { implicit request =>
    val userId = request.userId
    val status = request.body.status

    friendRequestService.respondToRequest(requestId, userId, status).map {
      case Right(_) => Ok(Json.obj("message" -> s"Friend request $status successfully"))
      case Left(errorMessage) => BadRequest(Json.obj("message" -> errorMessage))
    }
  }

  def deleteRequest(requestId: Int): Action[AnyContent] = authAction.async { implicit request =>
    val userId = request.userId

    friendRequestService.deleteRequest(requestId, userId).map {
      case Right(_) => NoContent
      case Left(errorMessage) => NotFound(Json.obj("message" -> errorMessage))
    }
  }

  def getFriendRequestById(requestId: Int): Action[AnyContent] = authAction.async { implicit request =>
    friendRequestService.findById(requestId).map {
      case Some(friendRequest) => Ok(Json.toJson(friendRequest))
      case None => NotFound(Json.obj("message" -> "Friend request not found"))
    }
  }

  def getAllFriendRequests: Action[AnyContent] = authAction.async { implicit request =>
    val userId = request.userId
    friendRequestService.findByUserId(userId).map { friendRequests =>
      Ok(Json.toJson(friendRequests))
    }
  }
}
