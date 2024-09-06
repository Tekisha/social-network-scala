package repositories

import javax.inject.Inject
import models.FriendRequest
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.JdbcProfile
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import models.{FriendRequest, Tables}

class FriendRequestRepository @Inject()(override protected val dbConfigProvider: DatabaseConfigProvider)
                                       (implicit executionContext: ExecutionContext)
                                        extends HasDatabaseConfigProvider[JdbcProfile] with Tables {
  import profile.api._

  def create(friendRequest: FriendRequest): Future[FriendRequest] = {
    db.run(friendRequests returning friendRequests.map(_.id) into ((req, id) => req.copy(id = Some(id))) += friendRequest)
  }

  def findById(id: Int): Future[Option[FriendRequest]] = db.run(friendRequests.filter(_.id === id).result.headOption)

  def updateStatus(id: Int, status: String): Future[Int] = db.run(friendRequests.filter(_.id === id).map(_.status).update(status))

  def delete(id: Int): Future[Int] = db.run(friendRequests.filter(_.id === id).delete)

  def findByUserId(userId: Int): Future[Seq[FriendRequest]] = {
    db.run(friendRequests.filter(req => req.requesterId === userId || req.receiverId === userId).result)
  }

  def findPendingRequestBetweenUsers(requesterId: Int, receiverId: Int): Future[Option[FriendRequest]] = {
    db.run(friendRequests.filter(req =>
      (req.requesterId === requesterId && req.receiverId === receiverId || req.requesterId === receiverId && req.receiverId === requesterId) &&
        req.status === "pending"
    ).result.headOption)
  }
}