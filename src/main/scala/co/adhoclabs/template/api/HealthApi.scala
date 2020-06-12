package co.adhoclabs.template.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.slf4j.{Logger, LoggerFactory}

trait HealthApi extends ApiBase {
  val routes: Route
}

class HealthApiImpl extends HealthApi {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  // The health API is used by Kubernetes to determine whether a pod is ready to receive connections.
  // Every service should have this route.
  override val routes: Route = path("health") {
    get {
      complete {
        StatusCodes.OK
      }
    }
  }
}
