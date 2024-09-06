package models

import java.sql.Timestamp

case class FriendRequest(id: Option[Int], requesterId: Int, receiverId: Int, status: String, createdAt: Timestamp)
