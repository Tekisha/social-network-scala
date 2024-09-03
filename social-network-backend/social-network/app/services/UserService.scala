package services

import javax.inject._
import models.User
import repositories.UserRepository
import scala.concurrent.{ExecutionContext, Future}
import utils.JwtUtils

@Singleton
class UserService @Inject() (userRepository: UserRepository)(implicit ec: ExecutionContext) {

  def registerUser(username: String, password: String): Future[User] = {
    userRepository.getUserByUsername(username).flatMap {
      case Some(_) => Future.failed(new Exception("Username already exists"))
      case None =>
        userRepository.createNewUser(User(None, username, password))
    }
  }

  def authenticateUser(username: String, password: String): Future[Option[String]] = {
    userRepository.validateUser(username, password).map {
      case Some(user) =>
        val token = JwtUtils.createToken(user.username, expirationPeriodInDays = 7)
        Some(token)
      case None => None
    }
  }
}
