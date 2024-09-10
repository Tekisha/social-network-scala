package models

import java.sql.Timestamp
import play.api.libs.json.{Json, OFormat}
import utils.JsonFormatUtils._

case class Post(id: Option[Int], userId: Int, content: String, createdAt: Timestamp, updatedAt: Timestamp)

object Post {
  implicit val postFormat: OFormat[Post] = Json.format[Post]
}