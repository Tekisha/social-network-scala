package services

import javax.inject.Inject
import repositories.{FriendshipRepository, UserRepository}
import scala.concurrent.{ExecutionContext, Future}
import dtos.FriendDetails

class FriendshipService @Inject()(friendshipRepository: FriendshipRepository, userRepository: UserRepository)
                                 (implicit ec: ExecutionContext) {

  def getFriends(userId: Int): Future[Seq[FriendDetails]] = {
    friendshipRepository.getFriends(userId).flatMap { friendships =>
      val friendDetails = friendships.map { friendship =>
        val friendId = if (friendship.userId1 == userId) friendship.userId2 else friendship.userId1
        userRepository.getUserById(friendId).map { userOpt =>
          userOpt.map { user =>
            FriendDetails(Some(friendship.id.get), user.id.get, user.username)
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
