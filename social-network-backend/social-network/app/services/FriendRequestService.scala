package services

import javax.inject.Inject
import models.FriendRequest
import repositories.FriendRequestRepository
import scala.concurrent.{ExecutionContext, Future}
import java.sql.Timestamp

class FriendRequestService @Inject()(friendRequestRepository: FriendRequestRepository)
                                    (implicit ec: ExecutionContext) {

  def sendRequest(requesterId: Int, receiverId: Int): Future[FriendRequest] = {
    val friendRequest = FriendRequest(
      None,
      requesterId,
      receiverId,
      "pending",
      new Timestamp(System.currentTimeMillis())
    )
    friendRequestRepository.create(friendRequest)
  }

  def respondToRequest(requestId: Int, userId: Int, status: String): Future[Either[String, Int]] = {
    if (status != "accepted" && status != "rejected") {
      Future.successful(Left("Invalid status"))
    } else {
      friendRequestRepository.findById(requestId).flatMap {
        case Some(request) if request.receiverId == userId =>
          friendRequestRepository.updateStatus(requestId, status).map(Right(_))
        case Some(_) => Future.successful(Left("Forbidden"))
        case None => Future.successful(Left("Request not found"))
      }
    }
  }

  def deleteRequest(requestId: Int, userId: Int): Future[Either[String, Int]] = {
    friendRequestRepository.findById(requestId).flatMap {
      case Some(request) if request.requesterId == userId =>
        friendRequestRepository.delete(requestId).map(Right(_))
      case Some(_) => Future.successful(Left("Forbidden"))
      case None => Future.successful(Left("Request not found"))
    }
  }

  def findById(requestId: Int): Future[Option[FriendRequest]] = {
    friendRequestRepository.findById(requestId)
  }

  def findByUserId(userId: Int): Future[Seq[FriendRequest]] = {
    friendRequestRepository.findByUserId(userId)
  }
}
