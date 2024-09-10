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
           COUNT(l.id) AS like_count,
           MAX(l.user_id = $userId) AS liked_by_me
    FROM posts p
    LEFT JOIN likes l ON p.id = l.post_id
    WHERE p.id = $postId
    GROUP BY p.id, p.user_id, p.content, p.created_at, p.updated_at
  """.as[(Int, Int, String, Timestamp, Timestamp, Int, Boolean)]

    db.run(query.headOption).map {
      case Some((id, userId, content, createdAt, updatedAt, likeCount, likedByMe)) =>
        Some(PostWithLikes(Post(Some(id), userId, content, createdAt, updatedAt), likedByMe, likeCount))
      case None => None
    }
  }


  def getAllPostsWithLikes(userId: Int, page: Int, pageSize: Int): Future[Seq[PostWithLikes]] = {
    val offset = (page - 1) * pageSize

    val query = sql"""
    SELECT p.id, p.user_id, p.content, p.created_at, p.updated_at,
           COUNT(l.id) AS like_count,
           MAX(l.user_id = $userId) AS liked_by_me
    FROM posts p
    LEFT JOIN likes l ON p.id = l.post_id
    GROUP BY p.id, p.user_id, p.content, p.created_at, p.updated_at
    ORDER BY p.created_at DESC
    LIMIT $pageSize OFFSET $offset
  """.as[(Int, Int, String, Timestamp, Timestamp, Int, Boolean)]

    db.run(query).map { results =>
      results.map { case (id, userId, content, createdAt, updatedAt, likeCount, likedByMe) =>
        PostWithLikes(Post(Some(id), userId, content, createdAt, updatedAt), likedByMe, likeCount)
      }
    }
  }


  def getUserPostsWithLikes(userId: Int, page: Int, pageSize: Int): Future[Seq[PostWithLikes]] = {
    val offset = (page - 1) * pageSize

    val query = sql"""
    SELECT p.id, p.user_id, p.content, p.created_at, p.updated_at,
           COUNT(l.id) AS like_count,
           MAX(l.user_id = $userId) AS liked_by_me
    FROM posts p
    LEFT JOIN likes l ON p.id = l.post_id
    WHERE p.user_id = $userId
    GROUP BY p.id, p.user_id, p.content, p.created_at, p.updated_at
    ORDER BY p.created_at DESC
    LIMIT $pageSize OFFSET $offset
  """.as[(Int, Int, String, Timestamp, Timestamp, Int, Boolean)]

    db.run(query).map { results =>
      results.map { case (id, userId, content, createdAt, updatedAt, likeCount, likedByMe) =>
        PostWithLikes(Post(Some(id), userId, content, createdAt, updatedAt), likedByMe, likeCount)
      }
    }
  }

  def getFriendsPostsWithLikes(userId: Int, page: Int, pageSize: Int): Future[Seq[PostWithLikes]] = {
    val offset = (page - 1) * pageSize

    val query = sql"""
    SELECT p.id, p.user_id, p.content, p.created_at, p.updated_at,
           COUNT(l.id) AS like_count,
           MAX(l.user_id = $userId) AS liked_by_me
    FROM posts p
    LEFT JOIN likes l ON p.id = l.post_id
    WHERE p.user_id IN (
      SELECT f.user_id1 FROM friendships f WHERE f.user_id2 = $userId
      UNION
      SELECT f.user_id2 FROM friendships f WHERE f.user_id1 = $userId
    )
    GROUP BY p.id, p.user_id, p.content, p.created_at, p.updated_at
    ORDER BY p.created_at DESC
    LIMIT $pageSize OFFSET $offset
  """.as[(Int, Int, String, Timestamp, Timestamp, Int, Boolean)]

    db.run(query).map { results =>
      results.map { case (id, userId, content, createdAt, updatedAt, likeCount, likedByMe) =>
        PostWithLikes(Post(Some(id), userId, content, createdAt, updatedAt), likedByMe, likeCount)
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
