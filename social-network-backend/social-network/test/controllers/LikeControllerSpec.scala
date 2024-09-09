import base.TestBase
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting}
import play.api.mvc.{AnyContentAsEmpty, Headers}
import play.api.libs.json.{JsObject, Json}

class LikeControllerSpec extends TestBase {

  "LikeController POST likePost" should {

    "successfully like a post" in {
      val token = getTokenForTestUser("testuser1", "password123")

      val request = FakeRequest(POST, "/posts/2/like")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe CREATED
      (contentAsJson(result) \ "message").as[String] mustBe "Post liked successfully"
    }

    "return bad request if already liked" in {
      val token = getTokenForTestUser("testuser1", "password123")

      val request = FakeRequest(POST, "/posts/1/like")
        .withHeaders("Authorization" -> s"Bearer $token")
      route(app, request).get

      val result = route(app, request).get

      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] mustBe "Already liked"
    }
  }

  "LikeController DELETE unlikePost" should {

    "successfully unlike a post" in {
      val token = getTokenForTestUser("testuser1", "password123")

      val request = FakeRequest(DELETE, "/posts/1/unlike")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe NO_CONTENT
    }

    "return bad request if not liked" in {
      val token = getTokenForTestUser("testuser2", "password456")

      val request = FakeRequest(DELETE, "/posts/1/unlike")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] mustBe "Not liked yet"
    }
  }

  "LikeController GET post with like count and liked by me status" should {

    "return correct like count and liked by me status" in {
      val token = getTokenForTestUser("testuser1", "password123")

      val likeRequest = FakeRequest(POST, "/posts/2/like")
        .withHeaders("Authorization" -> s"Bearer $token")
      route(app, likeRequest).get

      val getRequest = FakeRequest(GET, "/posts/2")
        .withHeaders("Authorization" -> s"Bearer $token")
      val getResult = route(app, getRequest).get

      status(getResult) mustBe OK
      (contentAsJson(getResult) \ "likeCount").as[Int] mustBe 2
      (contentAsJson(getResult) \ "likedByMe").as[Boolean] mustBe true
    }

    "return correct like count and liked by me status after unlike" in {
      val token = getTokenForTestUser("testuser1", "password123")

      val unlikeRequest = FakeRequest(DELETE, "/posts/1/unlike")
        .withHeaders("Authorization" -> s"Bearer $token")
      val unlikeResult = route(app, unlikeRequest).get

      status(unlikeResult) mustBe NO_CONTENT

      val getRequest = FakeRequest(GET, "/posts/1")
        .withHeaders("Authorization" -> s"Bearer $token")
      val getResult = route(app, getRequest).get

      println(contentAsJson(getResult))

      status(getResult) mustBe OK
      (contentAsJson(getResult) \ "likeCount").as[Int] mustBe 0
      (contentAsJson(getResult) \ "likedByMe").as[Boolean] mustBe false
    }
  }
}
