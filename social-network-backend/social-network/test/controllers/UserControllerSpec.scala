import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.Json
import controllers.UserController

class UserControllerSpec extends PlaySpec with GuiceOneAppPerTest {

  lazy val controller = app.injector.instanceOf[UserController]

  "UserController POST register" should {

    "successfully register a new user" in {
      val registrationData = Json.obj("username" -> "testuser", "password" -> "password123")

      val request = FakeRequest(POST, "/register").withJsonBody(registrationData)
      val result = controller.register().apply(request)

      status(result) mustBe CREATED
      contentAsJson(result) mustBe Json.obj("id" -> 1, "username" -> "testuser")
    }

    "fail to register a user with an existing username" in {
      val registrationData = Json.obj("username" -> "existinguser", "password" -> "password123")

      // First request to register the user
      val firstRequest = FakeRequest(POST, "/register").withJsonBody(registrationData)
      val firstResult = controller.register().apply(firstRequest)
      status(firstResult) mustBe CREATED

      // Second request with the same username
      val secondResult = controller.register().apply(firstRequest)
      status(secondResult) mustBe CONFLICT
      contentAsJson(secondResult) mustBe Json.obj("message" -> "Username already exists")
    }
  }
}
