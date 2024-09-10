package models

import slick.jdbc.JdbcProfile
import play.api.db.slick.HasDatabaseConfigProvider
import java.sql.Timestamp
import enums.FriendRequestStatus

trait Tables { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  implicit val friendRequestStatusMapper: BaseColumnType[FriendRequestStatus] =
    MappedColumnType.base[FriendRequestStatus, String](_.value, FriendRequestStatus.fromString(_).get)

  class UserTable(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def username = column[String]("username", O.Length(255), O.Unique)
    def password = column[String]("password")

    override def * = (id.?, username, password) <> (User.tupled, User.unapply)
  }

  class PostTable(tag: Tag) extends Table[Post](tag, "posts") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Int]("user_id")
    def content = column[String]("content", O.Length(255))
    def createdAt = column[Timestamp]("created_at")
    def updatedAt = column[Timestamp]("updated_at")

    def * = (id.?, userId, content, createdAt, updatedAt) <> ((Post.apply _).tupled, Post.unapply)

    def user = foreignKey("user_fk", userId, TableQuery[UserTable])(_.id)
  }

  class FriendRequestTable(tag: Tag) extends Table[FriendRequest](tag, "friend_requests") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def requesterId = column[Int]("requester_id")
    def receiverId = column[Int]("receiver_id")
    def status = column[FriendRequestStatus]("status")
    def createdAt = column[Timestamp]("created_at")

    def * = (id.?, requesterId, receiverId, status, createdAt) <> ((FriendRequest.apply _).tupled, FriendRequest.unapply)

    def requester = foreignKey("requester_fk", requesterId, TableQuery[UserTable])(_.id)
    def receiver = foreignKey("receiver_fk", receiverId, TableQuery[UserTable])(_.id)
  }

  class FriendshipTable(tag: Tag) extends Table[Friendship](tag, "friendships") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def userId1 = column[Int]("user_id1")
    def userId2 = column[Int]("user_id2")

    def * = (id.?, userId1, userId2) <> ((Friendship.apply _).tupled, Friendship.unapply)

    def user1 = foreignKey("user1_fk", userId1, TableQuery[UserTable])(_.id)
    def user2 = foreignKey("user2_fk", userId2, TableQuery[UserTable])(_.id)
  }

  class LikeTable(tag: Tag) extends Table[Like](tag, "likes") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Int]("user_id")
    def postId = column[Int]("post_id")
    def createdAt = column[Timestamp]("created_at", O.Default(new Timestamp(System.currentTimeMillis())))

    def * = (id.?, userId, postId, createdAt) <> (Like.tupled, Like.unapply)

    def user = foreignKey("user_fk", userId, TableQuery[UserTable])(_.id, onDelete = ForeignKeyAction.Cascade)
    def post = foreignKey("post_fk", postId, TableQuery[PostTable])(_.id, onDelete = ForeignKeyAction.Cascade)

    def uniqueUserPost = index("unique_user_post", (userId, postId), unique = true)
  }

  protected val users = TableQuery[UserTable]
  protected val posts = TableQuery[PostTable]
  protected val friendRequests = TableQuery[FriendRequestTable]
  protected val friendships = TableQuery[FriendshipTable]
  protected val likes = TableQuery[LikeTable]
}
