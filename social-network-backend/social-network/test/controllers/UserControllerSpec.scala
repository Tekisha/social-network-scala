import base.TestBase
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting}
import controllers.UserController
import play.api.mvc.{AnyContentAsEmpty, Headers}
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

  "UserController GET getAllUsers" should {

    "return a list of users when authenticated, including two registered users" in {
      //register the first user
      val registrationData1 = Json.obj("username" -> "testuser1", "password" -> "password123")
      val registrationRequest1: FakeRequest[JsObject] = FakeRequest(POST, "/register")
        .withBody(registrationData1)
      val registrationResult1 = route(app, registrationRequest1).get
      status(registrationResult1) mustBe CREATED

      // Register the second user
      val registrationData2 = Json.obj("username" -> "testuser2", "password" -> "password456")
      val registrationRequest2: FakeRequest[JsObject] = FakeRequest(POST, "/register")
        .withBody(registrationData2)
      val registrationResult2 = route(app, registrationRequest2).get
      status(registrationResult2) mustBe CREATED

      //log in as the first user to get the token
      val loginData = Json.obj("username" -> "testuser1", "password" -> "password123")
      val loginRequest: FakeRequest[JsObject] = FakeRequest(POST, "/login")
        .withBody(loginData)
      val loginResult = route(app, loginRequest).get
      status(loginResult) mustBe OK
      val token = (contentAsJson(loginResult) \ "token").as[String]

      //fetch the list of users
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/users")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe OK
      val users = contentAsJson(result).as[Seq[JsObject]]

      users.nonEmpty mustBe true
      users.map(user => (user \ "username").as[String]) must contain allOf ("testuser1", "testuser2")
    }

    "return 401 Unauthorized when no token is provided" in {
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/users")

      val result = route(app, request).get

      status(result) mustBe UNAUTHORIZED
    }
  }

  "UserController GET getUserById" should {

    "return the user details for a valid user ID" in {
      //register a user
      val registrationData = Json.obj("username" -> "testuser", "password" -> "password123")
      val registrationRequest: FakeRequest[JsObject] = FakeRequest(POST, "/register")
        .withBody(registrationData)
      val registrationResult = route(app, registrationRequest).get
      status(registrationResult) mustBe CREATED
      val userId = (contentAsJson(registrationResult) \ "id").as[Int]

      // Log in to get the token
      val loginData = Json.obj("username" -> "testuser", "password" -> "password123")
      val loginRequest: FakeRequest[JsObject] = FakeRequest(POST, "/login")
        .withBody(loginData)
      val loginResult = route(app, loginRequest).get
      status(loginResult) mustBe OK
      val token = (contentAsJson(loginResult) \ "token").as[String]

      // Fetch the user by ID using the token
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, s"/users/$userId")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "username").as[String] mustBe "testuser"
    }

    "return 404 for a non-existent user ID" in {
      // log in to get the token
      val registrationData = Json.obj("username" -> "testuser", "password" -> "password123")
      val registrationRequest: FakeRequest[JsObject] = FakeRequest(POST, "/register")
        .withBody(registrationData)
      val registrationResult = route(app, registrationRequest).get
      status(registrationResult) mustBe CREATED

      val loginData = Json.obj("username" -> "testuser", "password" -> "password123")
      val loginRequest: FakeRequest[JsObject] = FakeRequest(POST, "/login")
        .withBody(loginData)
      val loginResult = route(app, loginRequest).get
      status(loginResult) mustBe OK
      val token = (contentAsJson(loginResult) \ "token").as[String]

      // Attempt to fetch a non-existent user by ID
      val nonExistentUserId = 999
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, s"/users/$nonExistentUserId")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe NOT_FOUND
      (contentAsJson(result) \ "message").as[String] mustBe s"User with id $nonExistentUserId not found"
    }

    "return 401 Unauthorized when no token is provided" in {
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/users/1")
      val result = route(app, request).get

      status(result) mustBe UNAUTHORIZED
    }
  }

  "UserController PUT updateUser" should {

    "successfully update the user when authenticated" in {
      // register a user
      val registrationData = Json.obj("username" -> "testuser", "password" -> "password123")
      val registrationRequest: FakeRequest[JsObject] = FakeRequest(POST, "/register")
        .withBody(registrationData)
      val registrationResult = route(app, registrationRequest).get
      status(registrationResult) mustBe CREATED

      // Log in to get the token
      val loginData = Json.obj("username" -> "testuser", "password" -> "password123")
      val loginRequest: FakeRequest[JsObject] = FakeRequest(POST, "/login")
        .withBody(loginData)
      val loginResult = route(app, loginRequest).get
      status(loginResult) mustBe OK
      val token = (contentAsJson(loginResult) \ "token").as[String]

      val updatedUserData = Json.obj("username" -> "updateduser", "password" -> "newpassword123")

      // Send the update request with the token
      val request: FakeRequest[JsObject] = FakeRequest(PUT, "/users")
        .withHeaders("Authorization" -> s"Bearer $token")
        .withBody(updatedUserData)
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message").as[String] mustBe "User updated successfully"
      (contentAsJson(result) \ "user" \ "username").as[String] mustBe "updateduser"
      (contentAsJson(result) \ "token").as[String] must not be empty
    }

    "return 409 Conflict when the update fails" in {
      // Register a user
      val registrationData = Json.obj("username" -> "testuser", "password" -> "password123")
      val registrationRequest: FakeRequest[JsObject] = FakeRequest(POST, "/register")
        .withBody(registrationData)
      val registrationResult = route(app, registrationRequest).get
      status(registrationResult) mustBe CREATED

      val registrationData1 = Json.obj("username" -> "existinguser", "password" -> "newpassword123")
      val registrationRequest1: FakeRequest[JsObject] = FakeRequest(POST, "/register")
        .withBody(registrationData1)
      val registrationResult1 = route(app, registrationRequest1).get
      status(registrationResult1) mustBe CREATED

      // Log in to get the token
      val loginData = Json.obj("username" -> "testuser", "password" -> "password123")
      val loginRequest: FakeRequest[JsObject] = FakeRequest(POST, "/login")
        .withBody(loginData)
      val loginResult = route(app, loginRequest).get
      status(loginResult) mustBe OK
      val token = (contentAsJson(loginResult) \ "token").as[String]

      // Attempt to update with invalid data
      val invalidUpdateData = Json.obj("username" -> "existinguser", "password" -> "newpassword123")
      val request: FakeRequest[JsObject] = FakeRequest(PUT, "/users")
        .withHeaders("Authorization" -> s"Bearer $token")
        .withBody(invalidUpdateData)
      val result = route(app, request).get

      status(result) mustBe CONFLICT
      (contentAsJson(result) \ "message").as[String] mustBe "Username already exists"
    }

    "return 401 Unauthorized when no token is provided" in {
      val updateData = Json.obj("username" -> "updateduser", "password" -> "newpassword123")
      val request: FakeRequest[JsObject] = FakeRequest(PUT, "/users").withBody(updateData)

      val result = route(app, request).get

      status(result) mustBe UNAUTHORIZED
    }
  }
}
