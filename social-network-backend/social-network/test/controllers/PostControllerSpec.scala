import base.TestBase
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Injecting}
import play.api.mvc.{AnyContentAsEmpty, Headers}
import play.api.libs.json.{Json, JsObject}

class PostControllerSpec extends TestBase {

  "PostController POST createPost" should {

    "successfully create a new post" in {
      val createPostData = Json.obj("content" -> "This is a test post")
      val token = getTokenForTestUser("testuser1", "password123")

      val request: FakeRequest[JsObject] = FakeRequest(POST, "/posts", Headers("Authorization" -> s"Bearer $token"), createPostData)
      val result = route(app, request).get

      status(result) mustBe CREATED
      (contentAsJson(result) \ "post" \ "content").as[String] mustBe "This is a test post"
      (contentAsJson(result) \ "likeCount").as[Int] mustBe 0
      (contentAsJson(result) \ "likedByMe").as[Boolean] mustBe false
    }

    "fail to create a post when not authenticated" in {
      val createPostData = Json.obj("content" -> "This is a test post")

      val request: FakeRequest[JsObject] = FakeRequest(POST, "/posts", Headers(), createPostData)
      val result = route(app, request).get

      status(result) mustBe UNAUTHORIZED
    }
  }

  "PostController PUT updatePost" should {

    "successfully update a post" in {
      val updatePostData = Json.obj("content" -> "This is an updated post")
      val token = getTokenForTestUser("testuser1", "password123")

      val request: FakeRequest[JsObject] = FakeRequest(PUT, "/posts/1", Headers("Authorization" -> s"Bearer $token"), updatePostData)
      val result = route(app, request).get

      status(result) mustBe OK
      val jsonResponse = contentAsJson(result)

      (jsonResponse \ "post" \ "content").as[String] mustBe "This is an updated post"
      (jsonResponse \ "likedByMe").as[Boolean] mustBe true
      (jsonResponse \ "likeCount").as[Int] mustBe 1
    }

    "return forbidden when trying to update a post of another user" in {
      val updatePostData = Json.obj("content" -> "This is an updated post")
      val token = getTokenForTestUser("testuser2", "password456")

      val request: FakeRequest[JsObject] = FakeRequest(PUT, "/posts/1", Headers("Authorization" -> s"Bearer $token"), updatePostData)
      val result = route(app, request).get

      status(result) mustBe FORBIDDEN
      (contentAsJson(result) \ "message").as[String] mustBe "Not authorized to update this post"
    }
  }

  "PostController DELETE deletePost" should {

    "successfully delete a post" in {
      val token = getTokenForTestUser("testuser1", "password123")

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(DELETE, "/posts/1")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe NO_CONTENT

      val getRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/posts/1")
        .withHeaders("Authorization" -> s"Bearer $token")
      val getResult = route(app, getRequest).get

      status(getResult) mustBe NOT_FOUND
      (contentAsJson(getResult) \ "message").as[String] mustBe "Post not found"
    }

    "return forbidden when trying to delete a post of another user" in {
      val token = getTokenForTestUser("testuser2", "password456")

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(DELETE, "/posts/1")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe FORBIDDEN
      (contentAsJson(result) \ "message").as[String] mustBe "Not authorized to delete this post"
    }
  }

  "PostController GET getPostById" should {

    "return a post by ID" in {
      val token = getTokenForTestUser("testuser1", "password123")

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/posts/1")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe OK
      (contentAsJson(result) \ "post" \ "content").as[String] mustBe "This is the first post by testuser1"
    }

    "return 404 for non-existent post ID" in {
      val token = getTokenForTestUser("testuser1", "password123")

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/posts/999")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe NOT_FOUND
      (contentAsJson(result) \ "message").as[String] mustBe "Post not found"
    }
  }

  "PostController GET getAllPosts" should {

    "return a list of posts" in {
      val token = getTokenForTestUser("testuser1", "password123")

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/posts")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe OK
      val posts = contentAsJson(result).as[Seq[JsObject]]

      posts.nonEmpty mustBe true
      val contents = posts.map(post => (post \ "post" \ "content").as[String])

      contents must contain allOf (
        "This is the first post by testuser1",
        "This is the first post by testuser2",
        "This is the first post by existinguser"
      )
    }

    "return 401 Unauthorized when no token is provided" in {
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/posts")
      val result = route(app, request).get

      status(result) mustBe UNAUTHORIZED
    }
  }

  "PostController GET getUserPosts" should {

    "return a list of posts for a specific user" in {
      val token = getTokenForTestUser("testuser1", "password123")

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/posts/user/1")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe OK
      val posts = contentAsJson(result).as[Seq[JsObject]]

      posts.nonEmpty mustBe true
      val contents = posts.map(post => (post \ "post" \ "content").as[String])

      contents must contain("This is the first post by testuser1")
      contents must not contain("This is the first post by testuser2")
    }

    "return 404 when user has no posts" in {
      val token = getTokenForTestUser("testuser1", "password123")

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/posts/user/999")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe OK
      val posts = contentAsJson(result).as[Seq[JsObject]]
      posts.isEmpty mustBe true
    }

    "return 401 Unauthorized when no token is provided" in {
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/posts/user/1")
      val result = route(app, request).get

      status(result) mustBe UNAUTHORIZED
    }
  }

  "PostController GET getFriendsPosts" should {

    "return a list of friends' posts with correct like count and likedByMe status" in {
      val token = getTokenForTestUser("testuser1", "password123")

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/posts/friends")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe OK
      val posts = contentAsJson(result).as[Seq[JsObject]]

      posts.nonEmpty mustBe true
      val contents = posts.map(post => (post \ "post" \ "content").as[String])
      val likeCounts = posts.map(post => (post \ "likeCount").as[Int])
      val likedByMeStatuses = posts.map(post => (post \ "likedByMe").as[Boolean])

      contents must contain("This is the first post by testuser2")

      likeCounts.foreach(_ must be >= 0)
      likedByMeStatuses.foreach(_ mustBe false)
    }

    "return empty list when the user has no friends' posts" in {
      val token = getTokenForTestUser("existingUser", "password789")

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/posts/friends")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe OK
      val posts = contentAsJson(result).as[Seq[JsObject]]
      posts.isEmpty mustBe true
    }

    "return 401 Unauthorized when no token is provided" in {
      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/posts/friends")
      val result = route(app, request).get

      status(result) mustBe UNAUTHORIZED
    }

    "return correct posts with like count and likedByMe status when a post is liked" in {
      val token = getTokenForTestUser("testuser1", "password123")

      val likeRequest = FakeRequest(POST, "/posts/2/like")
        .withHeaders("Authorization" -> s"Bearer $token")
      val likeResult = route(app, likeRequest).get

      status(likeResult) mustBe CREATED

      val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "/posts/friends")
        .withHeaders("Authorization" -> s"Bearer $token")
      val result = route(app, request).get

      status(result) mustBe OK
      val posts = contentAsJson(result).as[Seq[JsObject]]

      posts.nonEmpty mustBe true
      val contents = posts.map(post => (post \ "post" \ "content").as[String])
      val likeCounts = posts.map(post => (post \ "likeCount").as[Int])
      val likedByMeStatuses = posts.map(post => (post \ "likedByMe").as[Boolean])

      contents must contain("This is the first post by testuser2")
      likeCounts.head mustBe 2
      likedByMeStatuses.head mustBe true
    }
  }
}
