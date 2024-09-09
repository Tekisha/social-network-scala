package repositories

import models.{Post, Tables}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import dtos.PostWithLikes


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
    val query = posts
      .filter(_.id === postId)
      .joinLeft(likes).on(_.id === _.postId)
      .groupBy(_._1)
      .map { case (post, group) =>
        val likeCount = group.map(_._2).length
        val likedByMe = group.map(_._2.map(_.userId === userId).getOrElse(false)).max
        (post, likedByMe, likeCount)
      }

    db.run(query.result.headOption).map {
      case Some((post, likedByMe, likeCount)) => Some(PostWithLikes(post, likedByMe.getOrElse(false), likeCount))
      case None => None
    }
  }

  def getAllPostsWithLikes(userId: Int, page: Int, pageSize: Int): Future[Seq[PostWithLikes]] = {
    val offset = (page - 1) * pageSize

    val query = posts
      .joinLeft(likes).on(_.id === _.postId)
      .groupBy(_._1)
      .map { case (post, group) =>
        val likeCount = group.map(_._2).length
        val likedByMe = group.map(_._2.map(_.userId === userId).getOrElse(false)).max
        (post, likedByMe, likeCount)
      }
      .drop(offset)
      .take(pageSize)

    db.run(query.result).map { results =>
      results.map { case (post, likedByMe, likeCount) =>
        PostWithLikes(post, likedByMe.getOrElse(false), likeCount)
      }
    }
  }

  def getUserPostsWithLikes(userId: Int, page: Int, pageSize: Int): Future[Seq[PostWithLikes]] = {
    val offset = (page - 1) * pageSize

    val query = posts
      .filter(_.userId === userId)
      .joinLeft(likes).on(_.id === _.postId)
      .groupBy(_._1)
      .map { case (post, group) =>
        val likeCount = group.map(_._2).length
        val likedByMe = group.map(_._2.map(_.userId === userId).getOrElse(false)).max
        (post, likedByMe, likeCount)
      }
      .drop(offset)
      .take(pageSize)

    db.run(query.result).map { results =>
      results.map { case (post, likedByMe, likeCount) =>
        PostWithLikes(post, likedByMe.getOrElse(false), likeCount)
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
