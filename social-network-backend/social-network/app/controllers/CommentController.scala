package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import services.CommentService
import actions.AuthAction
import scala.concurrent.{ExecutionContext, Future}
import models.Comment
import utils.JsonFormatUtils._

class CommentController @Inject()(cc: ControllerComponents, commentService: CommentService, authAction: AuthAction)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  case class CreateCommentData(content: String, parentCommentId: Option[Int])
  implicit val createCommentFormat: OFormat[CreateCommentData] = Json.format[CreateCommentData]

  def createComment(postId: Int): Action[CreateCommentData] = authAction.async(parse.json[CreateCommentData]) { implicit request =>
    val userId = request.userId
    val commentData = request.body
    commentService.createComment(postId, userId, commentData.content, commentData.parentCommentId).map { commentWithReplies =>
      Created(Json.toJson(commentWithReplies))
    }
  }

  def getComments(postId: Int): Action[AnyContent] = authAction.async { implicit request =>
    val pageParam = request.getQueryString("page").map(_.trim)
    val pageSizeParam = request.getQueryString("pageSize").map(_.trim)

    val pageNum = pageParam.flatMap(p => scala.util.Try(p.toInt).toOption).getOrElse(1)
    val pageSizeNum = pageSizeParam.flatMap(ps => scala.util.Try(ps.toInt).toOption).getOrElse(10)

    commentService.getCommentsForPost(postId, pageNum, pageSizeNum).map { comments =>
        Ok(Json.toJson(comments))
    }
  }

  def deleteComment(commentId: Int): Action[AnyContent] = authAction.async { implicit request =>
    val userId = request.userId
    commentService.deleteComment(commentId, userId).map {
      case Right(_) => NoContent
      case Left(errorMessage) => Forbidden(Json.obj("message" -> errorMessage))
    }
  }
}
