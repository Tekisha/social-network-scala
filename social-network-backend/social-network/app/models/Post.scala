package models

import java.sql.Timestamp

case class Post(id: Option[Int], userId: Int, content: String, createdAt: Timestamp, updatedAt: Timestamp)