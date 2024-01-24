package co.adhoclabs.template.api

import zio.http.{Request, Status}

import scala.concurrent.Future

class HealthApiTest extends ApiTestBase {

  describe("GET /health/api") {
    it("should return a 200 response with an empty body") {
      provokeServerSuccess[String](
        app,
        Request.get(s"/health/api"),
        expectedStatus   = Status.Ok,
        payloadAssertion = _ == "API is healthy!"
      )
    }
  }

  describe("GET /health/db") {
    it("should return a 200 response with an empty body") {
      (healthManager.executeDbGet _)
        .expects()
        .returning(Future.successful(()))

      provokeServerSuccess[String](
        app,
        Request.get(s"/health/db"),
        expectedStatus   = Status.Ok,
        payloadAssertion = _ == "DB is healthy!"
      )
    }
  }

}
