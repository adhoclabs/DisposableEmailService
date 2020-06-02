package co.adhoclabs.template.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

class HealthApiTest extends ApiTestBase {

  describe("GET /health") {
    it("should return a 200 response with an empty body") {
      Get(s"/health") ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.OK)
      }
    }
  }

}