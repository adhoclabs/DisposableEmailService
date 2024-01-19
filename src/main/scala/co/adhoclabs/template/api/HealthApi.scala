package co.adhoclabs.template.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import co.adhoclabs.template.business.HealthManager
import co.adhoclabs.template.models.BasicPayload
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext

trait HealthApi extends ApiBase {
  val routes: Route
}

class HealthApiImpl(implicit healthManager: HealthManager, executionContext: ExecutionContext) extends HealthApi {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  // The health API is used by Kubernetes to determine whether a pod is ready to receive connections.
  // Every service should have this route.
  override val routes: Route = {
    pathPrefix("health") {
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
}

import zio._
import zio.http._
import zio.http.codec.PathCodec
import zio.http.endpoint.openapi.{OpenAPIGen, SwaggerUI}
import zio.http.endpoint.Endpoint

object HealthEndpoint {
  val endpoint =
    Endpoint(Method.GET / "healthZ")
      .out[String]

  val payloadReceiver =
    Endpoint(Method.POST / "payload")
      .in[BasicPayload]
      .out[String]
}

object HealthRoute {
  val route =
    HealthEndpoint.endpoint.implement {
      Handler.fromZIO {
        ZIO.succeed("You're so healthy!").debug("Reached ZIO health check!")
      }

    }

  val payloadRoute =
    HealthEndpoint.payloadReceiver.implement {
      Handler.fromFunctionZIO {
        (payload: BasicPayload) =>
          ZIO.debug(payload) *>
            ZIO.succeed("You're so healthy!").debug("Reached ZIO health check!")
      }

    }

  val routes = Routes(route, payloadRoute)
}
