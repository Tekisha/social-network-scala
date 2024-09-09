package services

import javax.inject.Inject
import models.FriendRequest
import repositories.{FriendRequestRepository, UserRepository, FriendshipRepository}
import scala.concurrent.{ExecutionContext, Future}
import java.sql.Timestamp
import enums.FriendRequestStatus
import dtos.FriendRequestDetails
import slick.dbio.DBIO
import slick.jdbc.JdbcProfile
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

class FriendRequestService @Inject()(
                                      friendRequestRepository: FriendRequestRepository,
                                      userRepository: UserRepository,
                                      friendshipRepository: FriendshipRepository,
                                      protected val dbConfigProvider: DatabaseConfigProvider
                                    )(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

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
          friendshipRepository.areFriends(request.requesterId, request.receiverId).flatMap {
            case true => Future.successful(Left("Users are already friends"))
            case false =>
              if (status == FriendRequestStatus.Accepted) {
                val addFriendAndUpdateStatus = for {
                  _ <- friendshipRepository.addFriend(request.requesterId, request.receiverId)
                  _ <- friendRequestRepository.updateStatus(requestId, status)
                } yield ()

                db.run(addFriendAndUpdateStatus.transactionally).map(_ => Right(1))
              } else {
                db.run(friendRequestRepository.updateStatus(requestId, status)).map(Right(_))
              }
          }
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

  def findById(requestId: Int): Future[Option[FriendRequestDetails]] = {
    friendRequestRepository.findById(requestId).flatMap {
      case Some(friendRequest) =>
        for {
          requester <- userRepository.getUserById(friendRequest.requesterId)
          receiver <- userRepository.getUserById(friendRequest.receiverId)
        } yield for {
          r <- requester
          v <- receiver
        } yield FriendRequestDetails(
          friendRequest.id.getOrElse(0),
          r.id.getOrElse(0),
          r.username,
          v.id.getOrElse(0),
          v.username,
          friendRequest.status.value,
          friendRequest.createdAt
        )
      case None => Future.successful(None)
    }
  }

  def findByUserId(userId: Int, page: Int, pageSize: Int): Future[Seq[FriendRequestDetails]] = {
    friendRequestRepository.findByUserId(userId, page, pageSize).flatMap { friendRequests =>
      Future.sequence(friendRequests.map { friendRequest =>
        for {
          requesterOpt <- userRepository.getUserById(friendRequest.requesterId)
          receiverOpt <- userRepository.getUserById(friendRequest.receiverId)
        } yield {
          (requesterOpt, receiverOpt) match {
            case (Some(requester), Some(receiver)) =>
              FriendRequestDetails(
                id = friendRequest.id.get,
                requesterId = requester.id.get,
                requesterUsername = requester.username,
                receiverId = receiver.id.get,
                receiverUsername = receiver.username,
                status = friendRequest.status.value,
                createdAt = friendRequest.createdAt
              )
            case _ => throw new Exception("User not found")
          }
        }
      })
    }
  }
}
