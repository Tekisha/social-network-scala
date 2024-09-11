package dtos

import play.api.libs.json.{Json, OFormat}

case class UpdateBasicInfo(username: String)

object UpdateBasicInfo {
  implicit val format: OFormat[UpdateBasicInfo] = Json.format[UpdateBasicInfo]
}

