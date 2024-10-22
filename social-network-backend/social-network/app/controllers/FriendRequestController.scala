package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import services.FriendRequestService
import actions.AuthAction
import scala.concurrent.{ExecutionContext, Future}
import models.FriendRequest
import utils.JsonFormatUtils._
import enums.FriendRequestStatus
import dtos.FriendRequestDetails

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

    friendRequestService.sendRequest(requesterId, receiverId).map {
      case Right(friendRequest) => Created(Json.toJson(friendRequest))
      case Left(errorMessage) => BadRequest(Json.obj("message" -> errorMessage))
    }
  }

  def respondToRequest(requestId: Int): Action[RespondFriendRequestData] = authAction.async(parse.json[RespondFriendRequestData]) { implicit request =>
    val userId = request.userId
    FriendRequestStatus.fromString(request.body.status) match {
      case Some(status) =>
        friendRequestService.respondToRequest(requestId, userId, status).map {
          case Right(_) => Ok(Json.obj("message" -> s"Friend request ${status.value.toLowerCase} successfully"))
          case Left("Forbidden") => Forbidden(Json.obj("message" -> "You are not authorized to respond to this request"))
          case Left(errorMessage) => BadRequest(Json.obj("message" -> errorMessage))
        }
      case None => Future.successful(BadRequest(Json.obj("message" -> "Invalid status")))
    }
  }

  def deleteRequest(requestId: Int): Action[AnyContent] = authAction.async { implicit request =>
    val userId = request.userId

    friendRequestService.deleteRequest(requestId, userId).map {
      case Right(_) => NoContent
      case Left("Forbidden") => Forbidden(Json.obj("message" -> "You are not authorized to delete this request"))
      case Left(errorMessage) => BadRequest(Json.obj("message" -> errorMessage))
    }
  }

  def getFriendRequestById(requestId: Int): Action[AnyContent] = authAction.async { implicit request =>
    friendRequestService.findById(requestId).map {
      case Some(friendRequestDetails) => Ok(Json.toJson(friendRequestDetails))
      case None => NotFound(Json.obj("message" -> "Friend request not found"))
    }
  }

  def getAllFriendRequests: Action[AnyContent] = authAction.async { implicit request =>
    val pageParam = request.getQueryString("page").map(_.trim)
    val pageSizeParam = request.getQueryString("pageSize").map(_.trim)

    val pageNum = pageParam.flatMap(p => scala.util.Try(p.toInt).toOption).getOrElse(1)
    val pageSizeNum = pageSizeParam.flatMap(ps => scala.util.Try(ps.toInt).toOption).getOrElse(10)

    val userId = request.userId
    friendRequestService.findByUserId(userId, pageNum, pageSizeNum).map { friendRequests =>
      Ok(Json.toJson(friendRequests))
    }
  }

  def deleteRequestByUserId(userId: Int): Action[AnyContent] = authAction.async { implicit request =>
    val requesterId = request.userId
    friendRequestService.deleteRequestByUserIds(requesterId, userId).map {
      case Right(_) => NoContent
      case Left("Forbidden") => Forbidden(Json.obj("message" -> "You are not authorized to delete this request"))
      case Left("Friend request not found") => NotFound(Json.obj("message" -> "Friend request not found"))
      case Left(errorMessage) => BadRequest(Json.obj("message" -> errorMessage))
    }
  }

  def getReceivedPendingRequests: Action[AnyContent] = authAction.async { implicit request =>
    val userId = request.userId
    val pageParam = request.getQueryString("page").map(_.trim)
    val pageSizeParam = request.getQueryString("pageSize").map(_.trim)

    val pageNum = pageParam.flatMap(p => scala.util.Try(p.toInt).toOption).getOrElse(1)
    val pageSizeNum = pageSizeParam.flatMap(ps => scala.util.Try(ps.toInt).toOption).getOrElse(10)

    friendRequestService.findPendingRequestsForReceiver(userId, pageNum, pageSizeNum).map { friendRequests =>
      Ok(Json.toJson(friendRequests))
    }
  }
}
