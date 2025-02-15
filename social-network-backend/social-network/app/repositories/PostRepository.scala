package repositories

import models.{Post, Tables}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import dtos.PostWithLikes
import java.sql.Timestamp

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PostRepository @Inject()(override protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] with Tables {

  import profile.api._

  def createPost(post: Post): Future[Post] = {
    val insertAction = (posts returning posts.map(_.id) into ((post, id) => post.copy(id = Some(id)))) += post
    db.run(insertAction)
  }

  def getPostWithLikes(userId: Int, postId: Int): Future[Option[PostWithLikes]] = {
    val query = sql"""
    SELECT p.id, p.user_id, p.content, p.created_at, p.updated_at,
           COUNT(DISTINCT l.id) AS like_count,
           MAX(l.user_id = $userId) AS liked_by_me,
           COUNT(DISTINCT c.id) AS comment_count,
           u.username, u.profile_photo
    FROM posts p
    LEFT JOIN likes l ON p.id = l.post_id
    LEFT JOIN comments c ON p.id = c.post_id
    JOIN users u ON p.user_id = u.id
    WHERE p.id = $postId
    GROUP BY p.id, p.user_id, p.content, p.created_at, p.updated_at, u.username, u.profile_photo
  """.as[(Int, Int, String, Timestamp, Timestamp, Int, Boolean, Int, String, Option[String])]

    db.run(query.headOption).map {
      case Some((id, userId, content, createdAt, updatedAt, likeCount, likedByMe, commentCount, username, profilePhoto)) =>
        Some(PostWithLikes(Post(Some(id), userId, content, createdAt, updatedAt), likedByMe, likeCount, commentCount, username, profilePhoto))
      case None => None
    }
  }

  def getAllPostsWithLikes(userId: Int, page: Int, pageSize: Int): Future[Seq[PostWithLikes]] = {
    val offset = (page - 1) * pageSize

    val query = sql"""
    SELECT p.id, p.user_id, p.content, p.created_at, p.updated_at,
           COUNT(DISTINCT l.id) AS like_count,
           MAX(l.user_id = $userId) AS liked_by_me,
           COUNT(DISTINCT c.id) AS comment_count,
           u.username, u.profile_photo
    FROM posts p
    LEFT JOIN likes l ON p.id = l.post_id
    LEFT JOIN comments c ON p.id = c.post_id
    JOIN users u ON p.user_id = u.id
    GROUP BY p.id, p.user_id, p.content, p.created_at, p.updated_at, u.username, u.profile_photo
    ORDER BY p.created_at DESC
    LIMIT $pageSize OFFSET $offset
  """.as[(Int, Int, String, Timestamp, Timestamp, Int, Boolean, Int, String, Option[String])]

    db.run(query).map { results =>
      results.map { case (id, userId, content, createdAt, updatedAt, likeCount, likedByMe, commentCount, username, profilePhoto) =>
        PostWithLikes(Post(Some(id), userId, content, createdAt, updatedAt), likedByMe, likeCount, commentCount, username, profilePhoto)
      }
    }
  }

  def getUserPostsWithLikes(userId: Int, page: Int, pageSize: Int, currentUserId: Int): Future[Seq[PostWithLikes]] = {
    val offset = (page - 1) * pageSize

    val query = sql"""
    SELECT p.id, p.user_id, p.content, p.created_at, p.updated_at,
           COUNT(DISTINCT l.id) AS like_count,
           MAX(l.user_id = $currentUserId) AS liked_by_me,
           COUNT(DISTINCT c.id) AS comment_count,
           u.username, u.profile_photo
    FROM posts p
    LEFT JOIN likes l ON p.id = l.post_id
    LEFT JOIN comments c ON p.id = c.post_id
    JOIN users u ON p.user_id = u.id
    WHERE p.user_id = $userId
    GROUP BY p.id, p.user_id, p.content, p.created_at, p.updated_at, u.username, u.profile_photo
    ORDER BY p.created_at DESC
    LIMIT $pageSize OFFSET $offset
  """.as[(Int, Int, String, Timestamp, Timestamp, Int, Boolean, Int, String, Option[String])]

    db.run(query).map { results =>
      results.map { case (id, postUserId, content, createdAt, updatedAt, likeCount, likedByMe, commentCount, username, profilePhoto) =>
        PostWithLikes(Post(Some(id), postUserId, content, createdAt, updatedAt), likedByMe, likeCount, commentCount, username, profilePhoto)
      }
    }
  }

  def getFriendsPostsWithLikes(userId: Int, page: Int, pageSize: Int): Future[Seq[PostWithLikes]] = {
    val offset = (page - 1) * pageSize

    val query = sql"""
    SELECT p.id, p.user_id, p.content, p.created_at, p.updated_at,
           COUNT(DISTINCT l.id) AS like_count,
           MAX(l.user_id = $userId) AS liked_by_me,
           COUNT(DISTINCT c.id) AS comment_count,
           u.username, u.profile_photo
    FROM posts p
    LEFT JOIN likes l ON p.id = l.post_id
    LEFT JOIN comments c ON p.id = c.post_id
    JOIN users u ON p.user_id = u.id
    WHERE p.user_id IN (
      SELECT f.user_id1 FROM friendships f WHERE f.user_id2 = $userId
      UNION
      SELECT f.user_id2 FROM friendships f WHERE f.user_id1 = $userId
    )
    GROUP BY p.id, p.user_id, p.content, p.created_at, p.updated_at, u.username, u.profile_photo
    ORDER BY p.created_at DESC
    LIMIT $pageSize OFFSET $offset
  """.as[(Int, Int, String, Timestamp, Timestamp, Int, Boolean, Int, String, Option[String])]

    db.run(query).map { results =>
      results.map { case (id, userId, content, createdAt, updatedAt, likeCount, likedByMe, commentCount, username, profilePhoto) =>
        PostWithLikes(Post(Some(id), userId, content, createdAt, updatedAt), likedByMe, likeCount, commentCount, username, profilePhoto)
      }
    }
  }

  def getPostById(id: Int): Future[Option[Post]] = {
    db.run(posts.filter(_.id === id).result.headOption)
  }

  def getAllPosts(page: Int, pageSize: Int): Future[Seq[Post]] = {
    val offset = (page - 1) * pageSize
    db.run(posts.drop(offset).take(pageSize).result)
  }

  def getUserPosts(userId: Int, page: Int, pageSize: Int): Future[Seq[Post]] = {
    val offset = (page - 1) * pageSize
    db.run(posts.filter(_.userId === userId).drop(offset).take(pageSize).result)
  }

  def updatePost(id: Int, updatedPost: Post): Future[Int] = {
    db.run(posts.filter(_.id === id).update(updatedPost))
  }

  def deletePost(id: Int): Future[Int] = {
    db.run(posts.filter(_.id === id).delete)
  }

  def getPostsByUserIds(userIds: Seq[Int]): Future[Seq[Post]] = {
    db.run(posts.filter(_.userId inSet userIds).result)
  }
}
