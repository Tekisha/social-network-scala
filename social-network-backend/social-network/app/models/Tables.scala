package models

import slick.jdbc.JdbcProfile
import play.api.db.slick.HasDatabaseConfigProvider
import java.sql.Timestamp

trait Tables { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

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

    def * = (id.?, userId, content, createdAt, updatedAt) <> (Post.tupled, Post.unapply)

    def user = foreignKey("user_fk", userId, TableQuery[UserTable])(_.id)
  }

  protected val users = TableQuery[UserTable]
  protected val posts = TableQuery[PostTable]
}
