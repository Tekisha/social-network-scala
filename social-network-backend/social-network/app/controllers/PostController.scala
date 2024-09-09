package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import services.PostService
import actions.AuthAction
import scala.concurrent.{ExecutionContext, Future}
import models.Post
import utils.JsonFormatUtils._

@Singleton
class PostController @Inject()(cc: ControllerComponents, postService: PostService, authAction: AuthAction)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  implicit val postFormat: OFormat[Post] = Json.format[Post]

  case class CreatePostData(content: String)
  implicit val createPostFormat: OFormat[CreatePostData] = Json.format[CreatePostData]

  case class UpdatePostData(content: String)
  implicit val updatePostFormat: OFormat[UpdatePostData] = Json.format[UpdatePostData]

  def createPost: Action[CreatePostData] = authAction.async(parse.json[CreatePostData]) { implicit request =>
    val userId = request.userId
    val postData = request.body
    postService.createPost(userId, postData.content).map { post =>
      Created(Json.toJson(post))
    }
  }

  def updatePost(postId: Int): Action[UpdatePostData] = authAction.async(parse.json[UpdatePostData]) { implicit request =>
    val userId = request.userId
    val postData = request.body
    postService.updatePost(postId, userId, postData.content).map {
      case Right(_) => Ok(Json.obj("message" -> "Post updated successfully"))
      case Left(errorMessage) => Forbidden(Json.obj("message" -> errorMessage))
    }
  }

  def deletePost(postId: Int): Action[AnyContent] = authAction.async { implicit request =>
    val userId = request.userId
    postService.deletePost(postId, userId).map {
      case Right(_) => NoContent
      case Left(errorMessage) => Forbidden(Json.obj("message" -> errorMessage))
    }
  }

  def getUserPosts(userId: Int): Action[AnyContent] = authAction.async { implicit request =>
    val pageParam = request.getQueryString("page").map(_.trim)
    val pageSizeParam = request.getQueryString("pageSize").map(_.trim)

    val pageNum = pageParam.flatMap(p => scala.util.Try(p.toInt).toOption).getOrElse(1)
    val pageSizeNum = pageSizeParam.flatMap(ps => scala.util.Try(ps.toInt).toOption).getOrElse(10)

    postService.getUserPosts(userId, pageNum, pageSizeNum).map { posts =>
      Ok(Json.toJson(posts))
    }
  }

  def getAllPosts: Action[AnyContent] = authAction.async { implicit request =>
    val pageParam = request.getQueryString("page").map(_.trim)
    val pageSizeParam = request.getQueryString("pageSize").map(_.trim)

    val pageNum = pageParam.flatMap(p => scala.util.Try(p.toInt).toOption).getOrElse(1)
    val pageSizeNum = pageSizeParam.flatMap(ps => scala.util.Try(ps.toInt).toOption).getOrElse(10)

    postService.getAllPosts(pageNum, pageSizeNum).map { posts =>
      Ok(Json.toJson(posts))
    }
  }

  def getPostById(postId: Int): Action[AnyContent] = authAction.async { implicit request =>
    postService.getPostById(postId).map {
      case Some(post) => Ok(Json.toJson(post))
      case None => NotFound(Json.obj("message" -> "Post not found"))
    }
  }
}
