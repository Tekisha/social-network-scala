package repositories

import models.{Post, Tables}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PostRepository @Inject()(override protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] with Tables {

  import profile.api._

  def createPost(post: Post): Future[Post] = {
    val insertAction = (posts returning posts.map(_.id) into ((post, id) => post.copy(id = Some(id)))) += post
    db.run(insertAction)
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
