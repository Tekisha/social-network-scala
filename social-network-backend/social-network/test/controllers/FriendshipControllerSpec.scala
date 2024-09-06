import base.TestBase
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting}
import play.api.mvc.{AnyContentAsEmpty, Headers}
import play.api.libs.json.{JsObject, Json}

class FriendshipControllerSpec extends TestBase {

  "FriendshipController GET getFriends" should {

    "successfully return a list of friends" in {
      val token = getTokenForTestUser("testuser1", "password123")

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/friendships")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe OK
      val friends = contentAsJson(result).as[Seq[JsObject]]

      friends.nonEmpty mustBe true

      val friendUsernames = friends.map(friend => (friend \ "username").as[String])
      friendUsernames must contain("testuser2")
    }

    "return 401 Unauthorized when no token is provided" in {
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/friendships")
      val result = route(app, request).get

      status(result) mustBe UNAUTHORIZED
    }

    "return empty list if user has no friends" in {
      val token = getTokenForTestUser("existinguser", "password789")

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/friendships")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe OK
      val friends = contentAsJson(result).as[Seq[JsObject]]
      friends.isEmpty mustBe true
    }
  }

  "FriendshipController DELETE removeFriend" should {

    "successfully remove a friend" in {
      val token = getTokenForTestUser("testuser1", "password123")

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(DELETE, "/friendships/2")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe NO_CONTENT
    }

    "return 404 Not Found when friendship doesn't exist" in {
      val token = getTokenForTestUser("testuser1", "password123")

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(DELETE, "/friendships/999")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe NOT_FOUND
      (contentAsJson(result) \ "message").as[String] mustBe "Friendship not found"
    }

    "return 401 Unauthorized when no token is provided" in {
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(DELETE, "/friendships/2")
      val result = route(app, request).get

      status(result) mustBe UNAUTHORIZED
    }
  }
}
