package co.adhoclabs.template.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

trait HealthApi extends ApiBase {

  val healthRoutes: Route = path("health") {
    get {
      complete {
        StatusCodes.OK
      }
    }
  }
}
