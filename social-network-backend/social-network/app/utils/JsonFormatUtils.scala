package utils

import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import play.api.libs.json._

object JsonFormatUtils {
  implicit val timestampFormat: Format[Timestamp] = new Format[Timestamp] {
    def reads(json: JsValue): JsResult[Timestamp] = json.validate[String].map { str =>
      Timestamp.valueOf(LocalDateTime.parse(str, DateTimeFormatter.ISO_DATE_TIME))
    }

    def writes(ts: Timestamp): JsValue = JsString(ts.toLocalDateTime.format(DateTimeFormatter.ISO_DATE_TIME))
  }
}
