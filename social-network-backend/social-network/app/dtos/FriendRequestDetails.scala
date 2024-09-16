package dtos

import play.api.libs.json.{Json, OFormat}

import java.sql.Timestamp
import utils.JsonFormatUtils._

case class FriendRequestDetails(
                                 id: Int,
                                 requesterId: Int,
                                 requesterUsername: String,
                                 requesterProfilePhoto: String,
                                 receiverId: Int,
                                 receiverUsername: String,
                                 status: String,
                                 createdAt: Timestamp
                               )

object FriendRequestDetails {
  implicit val friendRequestDetailsFormat: OFormat[FriendRequestDetails] = Json.format[FriendRequestDetails]
}
