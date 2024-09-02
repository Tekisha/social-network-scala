import javax.inject._
import play.api.inject.ApplicationLifecycle
import scala.concurrent.{ExecutionContext, Future}
import repositories.UserRepository

@Singleton
class ApplicationStartUp @Inject()(
                                    userRepository: UserRepository,
                                    lifecycle: ApplicationLifecycle
                                  )(implicit ec: ExecutionContext) {

  userRepository.createSchemaIfNotExists().recover {
    case ex: Exception =>
      println(s"Error creating schema: ${ex.getMessage}")
  }

  lifecycle.addStopHook { () =>
    Future.successful(())
  }
}
