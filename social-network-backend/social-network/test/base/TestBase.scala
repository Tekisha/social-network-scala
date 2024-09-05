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
import play.api.test.{FakeRequest, Injecting}
import play.api.mvc.{AnyContentAsJson, Headers, AnyContentAsEmpty}

abstract class TestBase extends PlaySpec with BeforeAndAfterEach with GuiceOneServerPerSuite {

  private lazy val db: Database = Database.forConfig("slick.dbs.default")
  lazy val dbApi: DBApi = app.injector.instanceOf[DBApi]

  override def fakeApplication() = new GuiceApplicationBuilder()
    .configure(
      "slick.dbs.default.profile" -> "slick.jdbc.MySQLProfile$",
      "slick.dbs.default.db.driver" -> "com.mysql.cj.jdbc.Driver",
      "slick.dbs.default.db.url" -> "jdbc:mysql://localhost:3307/test_db",
      "slick.dbs.default.db.user" -> "test_user",
      "slick.dbs.default.db.password" -> "test_password",
      "play.evolutions.enabled" -> "true",
      "play.evolutions.autoApply" -> "true",
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
}
