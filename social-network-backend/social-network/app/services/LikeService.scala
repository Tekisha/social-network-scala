package services

import javax.inject.Inject
import repositories.LikeRepository
import scala.concurrent.{ExecutionContext, Future}
import models.Like
import java.sql.Timestamp

class LikeService @Inject()(likeRepository: LikeRepository)(implicit ec: ExecutionContext) {

  def likePost(userId: Int, postId: Int): Future[Either[String, Unit]] = {
    likeRepository.checkIfLiked(userId, postId).flatMap {
      case Some(_) => Future.successful(Left("Already liked"))
      case None =>
        val like = Like(None, userId, postId, new Timestamp(System.currentTimeMillis()))
        likeRepository.addLike(like).map(_ => Right(()))
    }
  }

  def unlikePost(userId: Int, postId: Int): Future[Either[String, Unit]] = {
    likeRepository.checkIfLiked(userId, postId).flatMap {
      case Some(_) => likeRepository.removeLike(userId, postId).map(_ => Right(()))
      case None => Future.successful(Left("Not liked yet"))
    }
  }
}
