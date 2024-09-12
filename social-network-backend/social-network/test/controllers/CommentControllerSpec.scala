import base.TestBase
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting}
import play.api.mvc.{AnyContentAsEmpty, Headers}
import play.api.libs.json.{Json, JsObject}

class CommentControllerSpec extends TestBase {

  "CommentController POST createComment" should {

    "successfully create a new comment" in {
      val createCommentData = Json.obj("content" -> "This is a test comment")
      val token = getTokenForTestUser("testuser1", "password123")

      val request: FakeRequest[JsObject] = FakeRequest(POST, "/posts/1/comments", Headers("Authorization" -> s"Bearer $token"), createCommentData)
      val result = route(app, request).get

      status(result) mustBe CREATED
      (contentAsJson(result) \ "comment" \ "content").as[String] mustBe "This is a test comment"
    }

    "fail to create a comment when not authenticated" in {
      val createCommentData = Json.obj("content" -> "This is a test comment")

      val request: FakeRequest[JsObject] = FakeRequest(POST, "/posts/1/comments", Headers(), createCommentData)
      val result = route(app, request).get

      status(result) mustBe UNAUTHORIZED
    }
  }

  "CommentController DELETE deleteComment" should {

    "successfully delete a comment" in {
      val token = getTokenForTestUser("testuser1", "password123")

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(DELETE, "/comments/1")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe NO_CONTENT

      val getRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/posts/1/comments")
        .withHeaders("Authorization" -> s"Bearer $token")
      val getResult = route(app, getRequest).get

      status(getResult) mustBe OK
      val jsonResponse = contentAsJson(getResult)
      jsonResponse.as[Seq[JsObject]].isEmpty mustBe true
    }

    "return forbidden when trying to delete a comment of another user" in {
      val token = getTokenForTestUser("testuser2", "password456")

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(DELETE, "/comments/1")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe FORBIDDEN
      (contentAsJson(result) \ "message").as[String] mustBe "Not authorized to delete this comment"
    }
  }

  "CommentController GET getCommentsForPost" should {

    "return a list of comments for a specific post" in {
      val token = getTokenForTestUser("testuser1", "password123")

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/posts/1/comments")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe OK
      val comments = contentAsJson(result).as[Seq[JsObject]]

      comments.nonEmpty mustBe true
      val contents = comments.map(comment => (comment \ "comment" \ "content").as[String])

      contents must contain("This is a comment by testuser1 on post 1")
    }
  }
}
