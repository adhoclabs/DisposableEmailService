package co.adhoclabs.template.api

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import co.adhoclabs.template.business.{SongManager, SongManagerImpl}
import scala.concurrent.ExecutionContext

trait TemplateApi extends HealthApi with SongApi {
  implicit val system: ActorSystem = ActorSystem("template")
  implicit val executor: ExecutionContext = system.dispatcher

  implicit def songManager: SongManager = new SongManagerImpl

  val routes: Route = healthRoutes ~ songRoutes
}
