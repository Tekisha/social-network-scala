package services

import javax.inject.Inject
import repositories.{FriendshipRepository, UserRepository}
import scala.concurrent.{ExecutionContext, Future}
import dtos.FriendDetails

class FriendshipService @Inject()(friendshipRepository: FriendshipRepository, userRepository: UserRepository)
                                 (implicit ec: ExecutionContext) {

  def getFriends(userId: Int, page: Int, pageSize: Int): Future[Seq[FriendDetails]] = {
    val offset = (page - 1) * pageSize
    friendshipRepository.getFriends(userId, offset, pageSize).flatMap { friendships =>
      val friendDetails = friendships.map { friendship =>
        val friendId = if (friendship.userId1 == userId) friendship.userId2 else friendship.userId1
        userRepository.getUserById(friendId).map { userOpt =>
          userOpt.map { user =>
            FriendDetails(Some(friendship.id.get), user.id.get, user.username, user.profilePhoto.get)
          }
        }
      }
      Future.sequence(friendDetails).map(_.flatten)
    }
  }


  def removeFriend(userId: Int, friendId: Int): Future[Either[String, Int]] = {
    friendshipRepository.removeFriend(userId, friendId).map { rows =>
      if (rows > 0) Right(rows) else Left("Not found")
    }
  }
}
