package co.adhoclabs.template.api

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._

trait Api extends HealthApi with SongApi with AlbumApi {
  val routes: Route = logRequestResult(requestAndResponseLoggingHandler _) {
    handleExceptions(exceptionHandler) {
      healthRoutes ~ songRoutes ~ albumRoutes
    }
  }
}
