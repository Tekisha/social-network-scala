package dtos

import play.api.libs.json.{Json, OFormat}

case class FriendDetails(id: Option[Int], friendId: Int, username: String)

object FriendDetails {
  implicit val format: OFormat[FriendDetails] = Json.format[FriendDetails]
}
