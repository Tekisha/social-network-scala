import base.TestBase
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting}
import play.api.mvc.{AnyContentAsEmpty, Headers}
import play.api.libs.json.{Json, JsObject}

class FriendRequestControllerSpec extends TestBase {

  "FriendRequestController POST sendFriendRequest" should {

    "successfully send a friend request" in {
      val sendFriendRequestData = Json.obj("receiverId" -> 3)
      val token = getTokenForTestUser("testuser1", "password123")

      val request: FakeRequest[JsObject] = FakeRequest(POST, "/friendRequests", Headers("Authorization" -> s"Bearer $token"), sendFriendRequestData)
      val result = route(app, request).get

      status(result) mustBe CREATED
      (contentAsJson(result) \ "receiverId").as[Int] mustBe 3
    }

    "fail to send a friend request when not authenticated" in {
      val sendFriendRequestData = Json.obj("receiverId" -> 2)

      val request: FakeRequest[JsObject] = FakeRequest(POST, "/friendRequests", Headers(), sendFriendRequestData)
      val result = route(app, request).get

      status(result) mustBe UNAUTHORIZED
    }

    "fail to send a friend request if there is already a pending request between users" in {
      val sendFriendRequestData = Json.obj("receiverId" -> 2)
      val token = getTokenForTestUser("testuser1", "password123")

      // Second request should fail because there's already a pending request
      val duplicateRequest: FakeRequest[JsObject] = FakeRequest(POST, "/friendRequests", Headers("Authorization" -> s"Bearer $token"), sendFriendRequestData)
      val duplicateResult = route(app, duplicateRequest).get

      status(duplicateResult) mustBe BAD_REQUEST
      (contentAsJson(duplicateResult) \ "message").as[String] mustBe "A pending friend request already exists between these users"
    }
  }

  "FriendRequestController PUT respondToRequest" should {

    "successfully respond to a friend request" in {
      val respondFriendRequestData = Json.obj("status" -> "accepted")
      val token = getTokenForTestUser("testuser2", "password456")

      val request: FakeRequest[JsObject] = FakeRequest(PUT, "/friendRequests/1/respond", Headers("Authorization" -> s"Bearer $token"), respondFriendRequestData)
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "message").as[String] mustBe "Friend request accepted successfully"
    }

    "return forbidden when trying to respond to a request not addressed to the user" in {
      val respondFriendRequestData = Json.obj("status" -> "accepted")
      val token = getTokenForTestUser("testuser1", "password123")

      val request: FakeRequest[JsObject] = FakeRequest(PUT, "/friendRequests/1/respond", Headers("Authorization" -> s"Bearer $token"), respondFriendRequestData)
      val result = route(app, request).get

      status(result) mustBe FORBIDDEN
      (contentAsJson(result) \ "message").as[String] mustBe "You are not authorized to respond to this request"
    }
  }

  "FriendRequestController DELETE deleteRequest" should {

    "successfully delete a friend request" in {
      val token = getTokenForTestUser("testuser1", "password123")

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(DELETE, "/friendRequests/1")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe NO_CONTENT
    }

    "return forbidden when trying to delete a request sent by another user" in {
      val token = getTokenForTestUser("testuser2", "password456")

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(DELETE, "/friendRequests/1")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe FORBIDDEN
      (contentAsJson(result) \ "message").as[String] mustBe "You are not authorized to delete this request"
    }
  }

  "FriendRequestController GET getFriendRequestById" should {

    "return a friend request by ID" in {
      val token = getTokenForTestUser("testuser1", "password123")

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/friendRequests/1")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "receiverId").as[Int] mustBe 2
    }

    "return 404 for non-existent friend request ID" in {
      val token = getTokenForTestUser("testuser1", "password123")

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/friendRequests/999")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe NOT_FOUND
      (contentAsJson(result) \ "message").as[String] mustBe "Friend request not found"
    }
  }

  "FriendRequestController GET getAllFriendRequests" should {

    "return a list of friend requests" in {
      val token = getTokenForTestUser("testuser1", "password123")

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/friendRequests")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe OK
      val requests = contentAsJson(result).as[Seq[JsObject]]

      requests.nonEmpty mustBe true
      val requesterIds = requests.map(request => (request \ "requesterId").as[Int])

      requesterIds must contain(1)
    }

    "return 401 Unauthorized when no token is provided" in {
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/friendRequests")
      val result = route(app, request).get

      status(result) mustBe UNAUTHORIZED
    }
  }
}
