package models

import play.api.libs.json.{Json, OFormat}
import utils.JsonFormatUtils._
import java.sql.Timestamp

case class Comment(
                    id: Option[Int],
                    postId: Int,
                    userId: Int,
                    content: String,
                    createdAt: Timestamp,
                    parentCommentId: Option[Int] = None
                  )

object Comment {
  implicit val commentFormat: OFormat[Comment] = Json.format[Comment]
}

