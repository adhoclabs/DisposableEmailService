package co.adhoclabs.template.api

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._

trait Api extends HealthApi with SongApi {
  val routes: Route = healthRoutes ~ songRoutes
}
