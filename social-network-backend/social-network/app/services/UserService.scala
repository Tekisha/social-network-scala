package services

import javax.inject._
import models.User
import repositories.UserRepository
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserService @Inject() (userRepository: UserRepository)(implicit ec: ExecutionContext) {

  def registerUser(username: String, password: String): Future[User] = {
    userRepository.getUserByUsername(username).flatMap {
      case Some(_) => Future.failed(new Exception("Username already exists"))
      case None =>
        userRepository.createNewUser(User(None, username, password))
    }
  }

  def authenticateUser(username: String, password: String): Future[Option[User]] = {
    userRepository.validateUser(username, password)
  }
}
