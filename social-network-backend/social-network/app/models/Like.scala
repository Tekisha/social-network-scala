package models

import java.sql.Timestamp

case class Like(id: Option[Int], userId: Int, postId: Int, createdAt: Timestamp)
