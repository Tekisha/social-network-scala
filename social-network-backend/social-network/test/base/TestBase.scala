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

abstract class TestBase extends PlaySpec with BeforeAndAfterEach with GuiceOneServerPerSuite {

  private lazy val db: Database = Database.forConfig("slick.dbs.default")
  lazy val dbApi: DBApi = app.injector.instanceOf[DBApi]

  override def fakeApplication() = new GuiceApplicationBuilder()
    .configure(
      "slick.dbs.default.profile" -> "slick.jdbc.MySQLProfile$",
      "slick.dbs.default.db.driver" -> "com.mysql.cj.jdbc.Driver",
      "slick.dbs.default.db.url" -> "jdbc:mysql://localhost:3307/test_db",  // Use Docker MySQL URL
      "slick.dbs.default.db.user" -> "test_user",
      "slick.dbs.default.db.password" -> "test_password",
      "play.evolutions.enabled" -> "true",
      "play.evolutions.autoApply" -> "true",
      "play.filters.enabled" -> Seq()   // Disable the AllowedHostsFilter
    )
    .build()

  val token: String = JwtUtils.createToken(1, "pera@email.com", 7)

  override def beforeEach(): Unit = {
    super.beforeEach()
    Evolutions.applyEvolutions(dbApi.database("default"))
  }

  override def afterEach(): Unit = {
    Evolutions.cleanupEvolutions(dbApi.database("default"))
    super.afterEach()
  }
}
