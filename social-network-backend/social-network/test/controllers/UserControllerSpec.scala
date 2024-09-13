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
      val jsonResponse = contentAsJson(result)
      (jsonResponse \ "username").as[String] mustBe "testuser"
      (jsonResponse \ "profilePhoto").asOpt[String].get mustBe "/assets/images/default.png"
    }

    "fail to register a user with an existing username" in {
      val registrationData = Json.obj("username" -> "existinguser", "password" -> "password123")

      val request: FakeRequest[JsObject] = FakeRequest(POST, "/register", Headers(), registrationData)
      val result = route(app, request).get

      status(result) mustBe CONFLICT
      (contentAsJson(result) \ "message").as[String] mustBe "Username already exists"
    }

    "fail to register a user with a short password" in {
      val registrationData = Json.obj("username" -> "newuser", "password" -> "short")

      val request: FakeRequest[JsObject] = FakeRequest(POST, "/register", Headers(), registrationData)
      val result = route(app, request).get

      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] mustBe "Password must be at least 8 characters long"
    }

    "fail to register a user with an empty or whitespace username" in {
      val registrationData = Json.obj("username" -> "   ", "password" -> "password123")

      val request: FakeRequest[JsObject] = FakeRequest(POST, "/register", Headers(), registrationData)
      val result = route(app, request).get

      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] mustBe "Username cannot be empty or just whitespace"
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

  "UserController PATCH updateBasicInfo" should {

    "successfully update user basic info" in {
      val token = getTokenForTestUser("testuser1", "password123")
      val updateData = Json.obj("username" -> "newuser")

      val request = FakeRequest(PATCH, "/users/me")
        .withHeaders("Authorization" -> s"Bearer $token")
        .withJsonBody(updateData)

      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message").as[String] mustBe "User info updated successfully"
      (contentAsJson(result) \ "token").as[String] must not be empty
    }

    "fail to update with an empty username" in {
      val token = getTokenForTestUser("testuser1", "password123")
      val updateData = Json.obj("username" -> "")

      val request = FakeRequest(PATCH, "/users/me")
        .withHeaders("Authorization" -> s"Bearer $token")
        .withJsonBody(updateData)

      val result = route(app, request).get

      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] mustBe "Username cannot be empty or just whitespace"
    }

    "fail to update with a whitespace username" in {
      val token = getTokenForTestUser("testuser1", "password123")
      val updateData = Json.obj("username" -> "   ")

      val request = FakeRequest(PATCH, "/users/me")
        .withHeaders("Authorization" -> s"Bearer $token")
        .withJsonBody(updateData)

      val result = route(app, request).get

      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] mustBe "Username cannot be empty or just whitespace"
    }

    "fail to update with an existing username" in {
      val token = getTokenForTestUser("testuser1", "password123")
      val updateData = Json.obj("username" -> "existinguser")

      val request = FakeRequest(PATCH, "/users/me")
        .withHeaders("Authorization" -> s"Bearer $token")
        .withJsonBody(updateData)

      val result = route(app, request).get

      status(result) mustBe CONFLICT
      (contentAsJson(result) \ "message").as[String] mustBe "Username already exists"
    }
  }

  "UserController PUT updatePassword" should {

    "successfully update password" in {
      val token = getTokenForTestUser("testuser1", "password123")
      val updateData = Json.obj("oldPassword" -> "password123", "newPassword" -> "newpassword123")

      val request = FakeRequest(PUT, "/users/me/password")
        .withHeaders("Authorization" -> s"Bearer $token")
        .withJsonBody(updateData)

      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message").as[String] mustBe "Password updated successfully"
    }

    "fail to update password with a short new password" in {
      val token = getTokenForTestUser("testuser1", "password123")
      val updateData = Json.obj("oldPassword" -> "password123", "newPassword" -> "short")

      val request = FakeRequest(PUT, "/users/me/password")
        .withHeaders("Authorization" -> s"Bearer $token")
        .withJsonBody(updateData)

      val result = route(app, request).get

      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] mustBe "New password must be at least 8 characters long"
    }

    "fail to update password with incorrect old password" in {
      val token = getTokenForTestUser("testuser1", "password123")
      val updateData = Json.obj("oldPassword" -> "wrongpassword", "newPassword" -> "newpassword123")

      val request = FakeRequest(PUT, "/users/me/password")
        .withHeaders("Authorization" -> s"Bearer $token")
        .withJsonBody(updateData)

      val result = route(app, request).get

      status(result) mustBe UNAUTHORIZED
      (contentAsJson(result) \ "message").as[String] mustBe "Incorrect old password"
    }

    "fail to update password when user not found because of invalid token" in {
      val token = "invalidToken"
      val updateData = Json.obj("oldPassword" -> "password123", "newPassword" -> "newpassword123")

      val request = FakeRequest(PUT, "/users/me/password")
        .withHeaders("Authorization" -> s"Bearer $token")
        .withJsonBody(updateData)

      val result = route(app, request).get

      status(result) mustBe UNAUTHORIZED
      (contentAsJson(result) \ "message").as[String] mustBe "Invalid or missing token"
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

  "UserController GET searchUsers" should {

    "successfully return matching users" in {
      val token = getTokenForTestUser("testuser1", "password123")

      val request = FakeRequest(GET, "/users/search?username=test")
        .withHeaders("Authorization" -> s"Bearer $token")

      val result = route(app, request).get

      status(result) mustBe OK
      val users = contentAsJson(result).as[Seq[JsObject]]
      users.nonEmpty mustBe true
    }

    "return 404 when no users are found" in {
      val token = getTokenForTestUser("testuser1", "password123")

      val request = FakeRequest(GET, "/users/search?username=nonexistent")
        .withHeaders("Authorization" -> s"Bearer $token")

      val result = route(app, request).get

      status(result) mustBe OK
      val jsonResponse = contentAsJson(result)
      jsonResponse.as[Seq[JsObject]].isEmpty mustBe true
    }

    "return 401 Unauthorized when no token is provided" in {
      val request = FakeRequest(GET, "/users/search?username=test")

      val result = route(app, request).get

      status(result) mustBe UNAUTHORIZED
      (contentAsJson(result) \ "message").as[String] mustBe "Invalid or missing token"
    }
  }
}
