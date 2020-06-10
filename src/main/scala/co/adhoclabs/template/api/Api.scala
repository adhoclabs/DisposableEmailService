package co.adhoclabs.template.api

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._

trait Api extends HealthApi with SongApi with AlbumApi {
  val routes: Route = healthRoutes ~
    logRequestResult(requestAndResponseLoggingHandler _) {
      handleExceptions(exceptionHandler) {
        songRoutes ~ albumRoutes
      }
    }
}
