package controllers

import javax.inject._
import play.api.mvc._
import services.FriendshipService
import actions.AuthAction
import scala.concurrent.{ExecutionContext, Future}
import models.Friendship
import play.api.libs.json.Json
import dtos.FriendDetails
import play.api.libs.json.{Json, OFormat}

@Singleton
class FriendshipController @Inject()(cc: ControllerComponents, friendshipService: FriendshipService, authAction: AuthAction)
                                    (implicit ec: ExecutionContext) extends AbstractController(cc) {

  implicit val friendshipFormat: OFormat[Friendship] = Json.format[Friendship]

  def getFriends: Action[AnyContent] = authAction.async { implicit request =>
    val pageParam = request.getQueryString("page").map(_.trim)
    val pageSizeParam = request.getQueryString("pageSize").map(_.trim)

    val pageNum = pageParam.flatMap(p => scala.util.Try(p.toInt).toOption).getOrElse(1)
    val pageSizeNum = pageSizeParam.flatMap(ps => scala.util.Try(ps.toInt).toOption).getOrElse(10)

    val userId = request.userId
    friendshipService.getFriends(userId, pageNum, pageSizeNum).map { friends =>
      Ok(Json.toJson(friends))
    }
  }

  def removeFriend(friendId: Int): Action[AnyContent] = authAction.async { implicit request =>
    val userId = request.userId
    friendshipService.removeFriend(userId, friendId).map {
      case Right(_) => NoContent
      case Left("Not found") => NotFound(Json.obj("message" -> "Friendship not found"))
      case Left(error) => BadRequest(Json.obj("message" -> error))
    }
  }
}
