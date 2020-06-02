package co.adhoclabs.template.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

trait HealthApi extends ApiBase {
  // The health API is used by Kubernetes to determine whether a pod is ready to receive connections.
  // Every service should have this route.
  val healthRoutes: Route = path("health") {
    get {
      complete {
        StatusCodes.OK
      }
    }
  }
}
