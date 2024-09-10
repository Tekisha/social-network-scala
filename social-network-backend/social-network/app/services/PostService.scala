package services

import javax.inject.{Inject, Singleton}
import models.Post
import repositories.PostRepository
import dtos.PostWithLikes

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PostService @Inject()(postRepository: PostRepository)(implicit ec: ExecutionContext) {

  def createPost(userId: Int, content: String): Future[Post] = {
    val post = Post(None, userId, content, new java.sql.Timestamp(System.currentTimeMillis()), new java.sql.Timestamp(System.currentTimeMillis()))
    postRepository.createPost(post)
  }

  def getPostById(userId: Int, postId: Int): Future[Option[PostWithLikes]] = {
    postRepository.getPostWithLikes(userId, postId)
  }

  def getAllPosts(userId: Int, page: Int, pageSize: Int): Future[Seq[PostWithLikes]] = {
    postRepository.getAllPostsWithLikes(userId, page, pageSize)
  }

  def getUserPosts(userId: Int, page: Int, pageSize: Int): Future[Seq[PostWithLikes]] = {
    postRepository.getUserPostsWithLikes(userId, page, pageSize)
  }

  def getFriendsPosts(userId: Int, page: Int, pageSize: Int): Future[Seq[PostWithLikes]] = {
    postRepository.getFriendsPostsWithLikes(userId, page, pageSize)
  }

  def updatePost(postId: Int, userId: Int, content: String): Future[Either[String, Int]] = {
    postRepository.getPostById(postId).flatMap {
      case Some(post) if post.userId == userId =>
        val updatedPost = post.copy(content = content, updatedAt = new java.sql.Timestamp(System.currentTimeMillis()))
        postRepository.updatePost(postId, updatedPost).map(Right(_))
      case Some(_) => Future.successful(Left("Not authorized to update this post"))
      case None => Future.successful(Left("Post not found"))
    }
  }

  def deletePost(postId: Int, userId: Int): Future[Either[String, Int]] = {
    postRepository.getPostById(postId).flatMap {
      case Some(post) if post.userId == userId => postRepository.deletePost(postId).map(Right(_))
      case Some(_) => Future.successful(Left("Not authorized to delete this post"))
      case None => Future.successful(Left("Post not found"))
    }
  }
}
