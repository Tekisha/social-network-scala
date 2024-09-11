import base.TestBase
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting, FakeHeaders}
import play.api.mvc.{AnyContentAsEmpty, Headers}
import play.api.libs.json.{Json, JsObject}
import play.api.libs.Files.TemporaryFile
import play.api.libs.Files.TemporaryFileCreator
import play.api.mvc.MultipartFormData
import play.api.http.Writeable

class UserControllerSpec extends TestBase {

  "UserController POST register" should {

    "successfully register a new user" in {
      val registrationData = Json.obj("username" -> "testuser", "password" -> "password123")

      val request: FakeRequest[JsObject] = FakeRequest(POST, "/register", Headers(), registrationData)
      val result = route(app, request).get

      status(result) mustBe CREATED
      contentAsJson(result) mustBe Json.obj("id" -> 4, "username" -> "testuser")
    }

    "fail to register a user with an existing username" in {
      val registrationData = Json.obj("username" -> "existinguser", "password" -> "password123")

      // First request to register the user
      val request: FakeRequest[JsObject] = FakeRequest(POST, "/register", Headers(), registrationData)
      val result = route(app, request).get

      status(result) mustBe CONFLICT
      contentAsJson(result) mustBe Json.obj("message" -> "Username already exists")
    }
  }

  "UserController POST login" should {

    "successfully login with correct credentials" in {
      val token = getTokenForTestUser("testuser1", "password123")

      token must not be empty
    }

    "fail to login with invalid credentials" in {
      val loginData = Json.obj("username" -> "testuser1", "password" -> "wrongpassword")
      val loginRequest: FakeRequest[JsObject] = FakeRequest(POST, "/login")
        .withBody(loginData)
      val loginResult = route(app, loginRequest).get

      status(loginResult) mustBe UNAUTHORIZED
      contentAsJson(loginResult) mustBe Json.obj("message" -> "Invalid credentials")
    }
  }

  "UserController GET getAllUsers" should {

    "return a list of users when authenticated" in {
      val token = getTokenForTestUser("testuser1", "password123")

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
      val token = getTokenForTestUser("testuser1", "password123")

      // Fetch the user by ID using the token
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/users/1")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "username").as[String] mustBe "testuser1"
    }

    "return 404 for a non-existent user ID" in {
      val token = getTokenForTestUser("testuser1", "password123")

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
      val token = getTokenForTestUser("testuser1", "password123")

      // Prepare the updated user data
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
      val token = getTokenForTestUser("testuser1", "password123")

      // Attempt to update with an existing username
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

  "UserController DELETE deleteUser" should {

    "successfully delete the authenticated user" in {
      val token = getTokenForTestUser("testuser1", "password123")

      // Send the delete request
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(DELETE, "/users")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe NO_CONTENT

      val getRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/users/1")
        .withHeaders("Authorization" -> s"Bearer $token")
      val getResult = route(app, getRequest).get

      status(getResult) mustBe NOT_FOUND
      (contentAsJson(getResult) \ "message").as[String] mustBe "User with id 1 not found"
    }

    "return 401 Unauthorized when no token is provided" in {
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(DELETE, "/users")
      val result = route(app, request).get

      status(result) mustBe UNAUTHORIZED
    }
  }

  "UserController POST changeProfilePhoto" should {

    "successfully change profile photo when authenticated and a valid file is provided" in {
      val token = getTokenForTestUser("testuser1", "password123")
      val formData = createMultipartFormData("profile", "jpg")
      val request = createPhotoUploadRequest(Some(token), formData)

      val result = route(app, request).get
      status(result) mustBe OK
      (contentAsJson(result) \ "message").as[String] mustBe "Profile photo updated successfully"
    }

    "return 400 BadRequest when no file is provided" in {
      val token = getTokenForTestUser("testuser1", "password123")
      val formData = MultipartFormData[TemporaryFile](dataParts = Map.empty, files = Seq.empty, badParts = Seq.empty)
      val request = createPhotoUploadRequest(Some(token), formData)

      val result = route(app, request).get
      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] mustBe "Missing file"
    }

    "return 400 BadRequest when the file is of unsupported type" in {
      val token = getTokenForTestUser("testuser1", "password123")
      val formData = createMultipartFormData("profile", "txt")
      val request = createPhotoUploadRequest(Some(token), formData)

      val result = route(app, request).get
      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] mustBe "Invalid file type. Only jpg and png are allowed."
    }

    "return 401 Unauthorized when no token is provided" in {
      val formData = createMultipartFormData("profile", "jpg")
      val request = createPhotoUploadRequest(None, formData)

      val result = route(app, request).get
      status(result) mustBe UNAUTHORIZED
      (contentAsJson(result) \ "message").as[String] mustBe "Invalid or missing token"
    }
  }
}
