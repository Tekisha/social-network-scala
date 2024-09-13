package dtos

import play.api.libs.json.{Json, OFormat}

case class UserResponse(id: Option[Int], username: String, profilePhoto: Option[String], isFriend: Boolean = false)

object UserResponse {
  implicit val userResponseFormat: OFormat[UserResponse] = Json.format[UserResponse]
}
