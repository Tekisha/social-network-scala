package base

import org.scalatestplus.play._
import play.api.db.evolutions.Evolutions
import slick.jdbc.JdbcBackend.Database
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import models.User
import utils.JwtUtils
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.db.{DBApi}
import play.api.libs.json.{Json, JsObject}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting, FakeHeaders}
import play.api.mvc.{AnyContentAsJson, Headers, AnyContentAsEmpty}
import java.nio.file.Files
import java.nio.file.Paths
import play.api.libs.Files.TemporaryFile
import play.api.libs.Files.TemporaryFileCreator
import play.api.mvc.MultipartFormData
import org.apache.pekko.util.ByteString
import play.api.http.Writeable
import play.api.mvc.MultipartFormData.FilePart

abstract class TestBase extends PlaySpec with BeforeAndAfterEach with GuiceOneServerPerSuite with Injecting {

  private lazy val db: Database = Database.forConfig("slick.dbs.default")
  lazy val dbApi: DBApi = app.injector.instanceOf[DBApi]
  lazy val tempFileCreator: TemporaryFileCreator = inject[TemporaryFileCreator]

  override def fakeApplication() = new GuiceApplicationBuilder()
    .configure(
      "slick.dbs.default.profile" -> "slick.jdbc.MySQLProfile$",
      "slick.dbs.default.db.driver" -> "com.mysql.cj.jdbc.Driver",
      "slick.dbs.default.db.url" -> "jdbc:mysql://localhost:3307/test_db",
      "slick.dbs.default.db.user" -> "test_user",
      "slick.dbs.default.db.password" -> "test_password",
      "play.evolutions.enabled" -> "true",
      "play.evolutions.autoApply" -> "true",
      "play.evolutions.autoApplyDowns" -> "true",
      "play.filters.enabled" -> Seq()   // Disable the AllowedHostsFilter
    )
    .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    Evolutions.applyEvolutions(dbApi.database("default"))
  }

  override def afterEach(): Unit = {
    Evolutions.cleanupEvolutions(dbApi.database("default"))
    super.afterEach()
  }

  def getTokenForTestUser(username: String, password: String): String = {
    val loginData = Json.obj("username" -> username, "password" -> password)

    val loginRequest: FakeRequest[AnyContentAsJson] =
      FakeRequest(POST, "/login").withHeaders(Headers("Content-Type" -> "application/json")).withBody(AnyContentAsJson(loginData))

    val loginResult = route(app, loginRequest).get

    status(loginResult) mustBe OK
    (contentAsJson(loginResult) \ "token").as[String]
  }

  implicit def writeableOfMultipartFormData: Writeable[MultipartFormData[TemporaryFile]] = {
    Writeable(data => {
      val boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW"
      val body = new StringBuilder()

      data.dataParts.foreach {
        case (key, values) =>
          values.foreach { value =>
            body.append(s"--$boundary\r\n")
            body.append(s"Content-Disposition: form-data; name=\"$key\"\r\n\r\n")
            body.append(s"$value\r\n")
          }
      }

      data.files.foreach { file =>
        body.append(s"--$boundary\r\n")
        body.append(s"Content-Disposition: form-data; name=\"${file.key}\"; filename=\"${file.filename}\"\r\n")
        body.append(s"Content-Type: ${file.contentType.getOrElse("application/octet-stream")}\r\n\r\n")
        body.append(file.ref.path.toFile)
        body.append("\r\n")
      }

      body.append(s"--$boundary--\r\n")
      ByteString(body.toString())
    }, Some("multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW"))
  }

  def createMultipartFormData(fileName: String, fileType: String): MultipartFormData[TemporaryFile] = {
    val tempFile = tempFileCreator.create(fileName, fileType)
    val part = FilePart[TemporaryFile](
      key = "profile_photo",
      filename = s"$fileName.$fileType",
      contentType = Some(s"image/$fileType"),
      ref = tempFile
    )
    MultipartFormData(
      dataParts = Map.empty,
      files = Seq(part),
      badParts = Seq.empty
    )
  }

  def createPhotoUploadRequest(token: Option[String], formData: MultipartFormData[TemporaryFile]): FakeRequest[MultipartFormData[TemporaryFile]] = {
    val request = FakeRequest(POST, "/users/profile-photo", FakeHeaders(), formData)

    token match {
      case Some(t) => request.withHeaders("Authorization" -> s"Bearer $t")
      case None => request
    }
  }
}
