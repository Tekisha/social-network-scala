package enums

import play.api.libs.json._

sealed trait FriendRequestStatus {
  def value: String
}

object FriendRequestStatus {
  case object Pending extends FriendRequestStatus { val value = "pending" }
  case object Accepted extends FriendRequestStatus { val value = "accepted" }
  case object Rejected extends FriendRequestStatus { val value = "rejected" }

  def fromString(status: String): Option[FriendRequestStatus] = status match {
    case "pending"  => Some(Pending)
    case "accepted" => Some(Accepted)
    case "rejected" => Some(Rejected)
    case _ => None
  }

  implicit val friendRequestStatusFormat: Format[FriendRequestStatus] = new Format[FriendRequestStatus] {
    def writes(status: FriendRequestStatus): JsValue = JsString(status.value)

    def reads(json: JsValue): JsResult[FriendRequestStatus] = json match {
      case JsString(s) => fromString(s).map(JsSuccess(_)).getOrElse(JsError("Invalid friend request status"))
      case _ => JsError("Expected a string for friend request status")
    }
  }
}
