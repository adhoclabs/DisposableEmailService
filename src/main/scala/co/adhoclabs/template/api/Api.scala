package co.adhoclabs.template.api

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._

trait Api extends HealthApi with SongApi with AlbumApi {
  val system: ActorSystem
  val routes: Route = healthRoutes ~ songRoutes ~ albumRoutes
}
