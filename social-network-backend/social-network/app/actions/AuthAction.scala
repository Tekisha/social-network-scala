package actions

import javax.inject._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import utils.JwtUtils
import play.api.libs.json.Json

class AuthenticatedRequest[A](val userId: Int, request: Request[A]) extends WrappedRequest[A](request)

class AuthAction @Inject()(val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext) extends ActionBuilder[AuthenticatedRequest, AnyContent] with ActionRefiner[Request, AuthenticatedRequest] {

  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = Future.successful {
    request.headers.get("Authorization").flatMap { token =>
      JwtUtils.validateToken(token.replace("Bearer ", "")).map { claim =>
        new AuthenticatedRequest(claim.userId, request)
      }
    } match {
      case Some(authRequest) => Right(authRequest)
      case None => Left(Results.Unauthorized(Json.obj("message" -> "Invalid or missing token")))
    }
  }
}
