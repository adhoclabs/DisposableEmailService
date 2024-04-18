package co.adhoclabs.email.api

import co.adhoclabs.email.business.HealthManager
import zio._
import zio.http._
import zio.http.endpoint.Endpoint

import scala.concurrent.Future

object HealthEndpoint {
  val api =
    Endpoint(Method.GET / "email" / "health" / "api")
      .out[String]

  val db =
    Endpoint(Method.GET / "email" / "health" / "db")
      .out[String]

  val endpoints =
    List(
//      api,
//      db
    )
}

case class HealthRoutes(
  implicit
  healthManager: HealthManager
) {
  val api =
    HealthEndpoint.api.implement {
      Handler.fromZIO {
        ZIO.succeed("API is healthy!")
      }
    }

  val db =
    HealthEndpoint.db.implement {
      Handler.fromZIO {
        ZIO.fromFuture(implicit ec => Future.successful("**HARDCODED*** DB is healthy!")).orDie
      }
    }

  val routes = Routes(api, db)
}
