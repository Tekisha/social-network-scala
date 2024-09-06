package services

import javax.inject.Inject
import models.FriendRequest
import repositories.FriendRequestRepository
import scala.concurrent.{ExecutionContext, Future}
import java.sql.Timestamp
import enums.FriendRequestStatus

class FriendRequestService @Inject()(friendRequestRepository: FriendRequestRepository)
                                    (implicit ec: ExecutionContext) {

  def sendRequest(requesterId: Int, receiverId: Int): Future[Either[String, FriendRequest]] = {
    friendRequestRepository.findPendingRequestBetweenUsers(requesterId, receiverId).flatMap {
      case Some(_) => Future.successful(Left("A pending friend request already exists between these users"))
      case None =>
        val friendRequest = FriendRequest(
          None,
          requesterId,
          receiverId,
          FriendRequestStatus.Pending,
          new Timestamp(System.currentTimeMillis())
        )
        friendRequestRepository.create(friendRequest).map(Right(_))
    }
  }

  def respondToRequest(requestId: Int, userId: Int, status: FriendRequestStatus): Future[Either[String, Int]] = {
    if (status != FriendRequestStatus.Accepted && status != FriendRequestStatus.Rejected) {
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
