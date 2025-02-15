package dtos

import play.api.libs.json.{Json, OFormat}
import models.Post
import utils.JsonFormatUtils._

case class PostWithLikes(
                          post: Post,
                          likedByMe: Boolean,
                          likeCount: Int,
                          commentCount: Int,
                          username: String,
                          profilePhoto: Option[String]
                        )

object PostWithLikes {
  implicit val format: OFormat[PostWithLikes] = Json.format[PostWithLikes]
}

