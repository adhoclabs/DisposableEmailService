package co.adhoclabs.template.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.Future

class HealthApiTest extends ApiTestBase {

  describe("GET /health/api") {
    it("should return a 200 response with an empty body") {
      Get(s"/health/api") ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.OK)
      }
    }
  }

  describe("GET /health/db") {
    it("should return a 200 response with an empty body") {
      (healthManager.executeDbGet _)
        .expects()
        .returning(Future.successful(()))

      Get(s"/health/db") ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.OK)
      }
    }
  }
  
}
