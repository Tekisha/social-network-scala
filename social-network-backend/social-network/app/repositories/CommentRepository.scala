package repositories

import models.{Comment, Tables}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import java.sql.Timestamp

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class CommentRepository @Inject()(override protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] with Tables {

  import profile.api._

  def createComment(comment: Comment): Future[Comment] = {
    val insertAction = (comments returning comments.map(_.id) into ((comment, id) => comment.copy(id = Some(id)))) += comment
    db.run(insertAction)
  }

  def getCommentWithReplies(commentId: Int): Future[Seq[Comment]] = {
    db.run(comments.filter(_.parentCommentId === commentId).result)
  }

  def getAllCommentsByPostId(postId: Int, page: Int, pageSize: Int): Future[Seq[Comment]] = {
    val offset = (page - 1) * pageSize
    db.run(comments.filter(_.postId === postId).drop(offset).take(pageSize).result)
  }

  def deleteComment(commentId: Int): Future[Int] = {
    db.run(comments.filter(_.id === commentId).delete)
  }

  def getCommentById(commentId: Int): Future[Option[Comment]] = {
    db.run(comments.filter(_.id === commentId).result.headOption)
  }

  def getCommentsWithUserDetails(postId: Int, page: Int, pageSize: Int): Future[Seq[(Comment, String, Option[String])]] = {
    val offset = (page - 1) * pageSize

    val query = sql"""
    SELECT c.id, c.post_id, c.user_id, c.content, c.created_at, c.parent_comment_id,
           u.username, u.profile_photo
    FROM comments c
    JOIN users u ON c.user_id = u.id
    WHERE c.post_id = $postId
    ORDER BY c.created_at ASC
    LIMIT $pageSize OFFSET $offset
  """.as[(Int, Int, Int, String, Timestamp, Option[Int], String, Option[String])]

    db.run(query).map { results =>
      results.map { case (id, postId, userId, content, createdAt, parentCommentId, username, profilePhoto) =>
        val comment = Comment(Some(id), postId, userId, content, createdAt, parentCommentId)
        (comment, username, profilePhoto)
      }
    }
  }

  def getCommentWithUserDetails(commentId: Int): Future[(Comment, String, Option[String])] = {
    val query = sql"""
    SELECT c.id, c.post_id, c.user_id, c.content, c.created_at, c.parent_comment_id,
           u.username, u.profile_photo
    FROM comments c
    JOIN users u ON c.user_id = u.id
    WHERE c.id = $commentId
  """.as[(Int, Int, Int, String, Timestamp, Option[Int], String, Option[String])].head

    db.run(query).map { case (id, postId, userId, content, createdAt, parentCommentId, username, profilePhoto) =>
      val comment = Comment(Some(id), postId, userId, content, createdAt, parentCommentId)
      (comment, username, profilePhoto)
    }
  }
}
