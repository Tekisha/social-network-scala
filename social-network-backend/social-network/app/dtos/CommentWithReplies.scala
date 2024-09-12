package dtos

import play.api.libs.json.{Json, OFormat}
import models.Comment

case class CommentWithReplies(
                               comment: Comment,
                               replies: Seq[CommentWithReplies],
                               username: String,
                               profilePhoto: Option[String]
                             )

object CommentWithReplies {
  implicit val format: OFormat[CommentWithReplies] = Json.format[CommentWithReplies]
}

