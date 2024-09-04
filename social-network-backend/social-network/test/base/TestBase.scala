package base

import org.scalatestplus.play._
import play.api.db.evolutions.Evolutions
import play.api.db.Database
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import models.User
import utils.JwtUtils

abstract class TestBase extends PlaySpec with BeforeAndAfterEach with GuiceOneServerPerSuite {

  private lazy val db: Database = app.injector.instanceOf[Database]
  val token: String = JwtUtils.createToken(1, "pera@email.com", 7)

  override def beforeEach(): Unit = {
    super.beforeEach()
    Evolutions.applyEvolutions(db)
  }

  override def afterEach(): Unit = {
    Evolutions.cleanupEvolutions(db)
    super.afterEach()
  }
}
