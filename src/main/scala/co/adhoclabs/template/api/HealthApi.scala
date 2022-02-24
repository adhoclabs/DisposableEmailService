package co.adhoclabs.template.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import co.adhoclabs.template.business.HealthManager
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext

trait HealthApi extends ApiBase {
  val routes: Route
}

class HealthApiImpl(implicit healthManager: HealthManager, executionContext: ExecutionContext) extends HealthApi {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  // The health API is used by Kubernetes to determine whether a pod is ready to receive connections.
  // Every service should have this route.
  override val routes: Route = pathPrefix("health") {
    concat(
      path("api") {
        get {
          complete {
            StatusCodes.OK
          }
        }
      },
      path("db") {
        get {
          complete {
            healthManager.executeDbGet().map(_ => StatusCodes.OK)
          }
        }
      }
    )
  }
}
