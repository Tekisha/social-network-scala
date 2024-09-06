package models

import java.sql.Timestamp
import enums.FriendRequestStatus

case class FriendRequest(id: Option[Int], requesterId: Int, receiverId: Int, status: FriendRequestStatus, createdAt: Timestamp)
