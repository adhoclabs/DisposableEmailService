package co.adhoclabs.template.apiz

import co.adhoclabs.model.ErrorResponse
import co.adhoclabs.template.business.HealthManager
import zio._
import zio.http._
import zio.http.endpoint.Endpoint

object HealthEndpoint {
  val okBoomer =
    Endpoint(Method.GET / "health" / "boom")
      .out[String]

  val api =
    Endpoint(Method.GET / "health" / "api")
      .out[String]

  val db =
    Endpoint(Method.GET / "health" / "db")
      .out[String]

  val endpoints =
    List(
      api,
      db,
      okBoomer
    )
}

case class HealthRoutes(implicit healthManager: HealthManager) {
  val api =
    HealthEndpoint.api.implement {
      Handler.fromZIO {
        ZIO.succeed("API is healthy!")
      }
    }

  val db =
    HealthEndpoint.db.implement {
      Handler.fromZIO {
        ZIO.fromFuture(
          implicit ec =>
            healthManager.executeDbGet()
        ).map(_ => "DB is healthy!")
          .orDie
      }
    }

  val okBoomer =
    HealthEndpoint.okBoomer.implement {
      Handler.fromZIO {
        ZIO.succeed(???)
      } // .mapError(throwable => ErrorResponse(throwable.getMessage))
    }

  val routes = Routes(api, db, okBoomer)
}
