package services

import javax.inject.{Inject, Singleton}
import models.Comment
import repositories.CommentRepository
import dtos.CommentWithReplies
import java.sql.Timestamp

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CommentService @Inject()(commentRepository: CommentRepository)(implicit ec: ExecutionContext) {

  def createComment(postId: Int, userId: Int, content: String, parentCommentId: Option[Int]): Future[CommentWithReplies] = {
    val comment = Comment(None, postId, userId, content, new Timestamp(System.currentTimeMillis()), parentCommentId)
    commentRepository.createComment(comment).flatMap { createdComment =>
      commentRepository.getCommentWithUserDetails(createdComment.id.get).map { commentDetails =>
        val (comment, username, profilePhoto) = commentDetails
        CommentWithReplies(comment, Seq.empty, username, profilePhoto)
      }
    }
  }

  def getCommentsForPost(postId: Int, page: Int, pageSize: Int): Future[Seq[CommentWithReplies]] = {
    commentRepository.getCommentsWithUserDetails(postId, page, pageSize).map { allComments =>
      def nestComments(comment: Comment, allComments: Seq[(Comment, String, Option[String])]): CommentWithReplies = {
        val replies = allComments.filter { case (c, _, _) => c.parentCommentId.contains(comment.id.get) }
        CommentWithReplies(
          comment,
          replies.map { case (reply, username, profilePhoto) => nestComments(reply, allComments) },
          allComments.find(_._1.id == comment.id).map(_._2).getOrElse(""),
          allComments.find(_._1.id == comment.id).flatMap(_._3)
        )
      }

      val topLevelComments = allComments.filter { case (c, _, _) => c.parentCommentId.isEmpty }
      topLevelComments.map { case (comment, username, profilePhoto) =>
        nestComments(comment, allComments)
      }
    }
  }

  def deleteComment(commentId: Int, userId: Int): Future[Either[String, Int]] = {
    commentRepository.getCommentById(commentId).flatMap {
      case Some(comment) if comment.userId == userId =>
        commentRepository.deleteComment(commentId).map(Right(_))
      case Some(_) => Future.successful(Left("Not authorized to delete this comment"))
      case None => Future.successful(Left("Comment not found"))
    }
  }
}
