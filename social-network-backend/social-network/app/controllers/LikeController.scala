package controllers

import javax.inject._
import play.api.mvc._
import services.LikeService
import actions.AuthAction
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.Json

@Singleton
class LikeController @Inject()(cc: ControllerComponents, likeService: LikeService, authAction: AuthAction)
                              (implicit ec: ExecutionContext) extends AbstractController(cc) {

  def likePost(postId: Int): Action[AnyContent] = authAction.async { implicit request =>
    val userId = request.userId
    likeService.likePost(userId, postId).map {
      case Right(_) => Created(Json.obj("message" -> "Post liked successfully"))
      case Left(errorMessage) => BadRequest(Json.obj("message" -> errorMessage))
    }
  }

  def unlikePost(postId: Int): Action[AnyContent] = authAction.async { implicit request =>
    val userId = request.userId
    likeService.unlikePost(userId, postId).map {
      case Right(_) => NoContent
      case Left(errorMessage) => BadRequest(Json.obj("message" -> errorMessage))
    }
  }
}