import base.TestBase
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting}
import controllers.UserController
import play.api.mvc.Headers
import play.api.libs.json.{Json, JsObject}

class UserControllerSpec extends TestBase {

  "UserController POST register" should {

    "successfully register a new user" in {
      val registrationData = Json.obj("username" -> "testuser", "password" -> "password123")

      val request: FakeRequest[JsObject] = FakeRequest(POST, "/register", Headers(), registrationData)
      val result = route(app, request).get

      status(result) mustBe CREATED
      contentAsJson(result) mustBe Json.obj("id" -> 1, "username" -> "testuser")
    }

    "fail to register a user with an existing username" in {
      val registrationData = Json.obj("username" -> "existinguser", "password" -> "password123")

      // First request to register the user
      val firstRequest: FakeRequest[JsObject] = FakeRequest(POST, "/register", Headers(), registrationData)
      val firstResult = route(app, firstRequest).get
      status(firstResult) mustBe CREATED

      // Second request with the same username
      val secondResult = route(app, firstRequest).get
      status(secondResult) mustBe CONFLICT
      contentAsJson(secondResult) mustBe Json.obj("message" -> "Username already exists")
    }
  }

  "UserController POST login" should {

    "successfully login with correct credentials" in {
      val registrationData = Json.obj("username" -> "testuser", "password" -> "password123")
      val registrationRequest: FakeRequest[JsObject] = FakeRequest(POST, "/register", Headers(), registrationData)
      val registrationResult = route(app, registrationRequest).get
      status(registrationResult) mustBe CREATED

      val loginData = Json.obj("username" -> "testuser", "password" -> "password123")
      val loginRequest: FakeRequest[JsObject] = FakeRequest(POST, "/login", Headers(), loginData)
      val loginResult = route(app, loginRequest).get

      status(loginResult) mustBe OK
      (contentAsJson(loginResult) \ "token").as[String] must not be empty
    }

    "fail to login with invalid credentials" in {
      val registrationData = Json.obj("username" -> "testuser", "password" -> "password123")
      val registrationRequest: FakeRequest[JsObject] = FakeRequest(POST, "/register", Headers(), registrationData)
      val registrationResult = route(app, registrationRequest).get
      status(registrationResult) mustBe CREATED

      val loginData = Json.obj("username" -> "testuser", "password" -> "wrongpassword")
      val loginRequest: FakeRequest[JsObject] = FakeRequest(POST, "/login", Headers(), loginData)
      val loginResult = route(app, loginRequest).get

      status(loginResult) mustBe UNAUTHORIZED
      contentAsJson(loginResult) mustBe Json.obj("message" -> "Invalid credentials")
    }
  }
}
