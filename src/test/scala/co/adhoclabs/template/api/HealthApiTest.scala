package co.adhoclabs.template.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import co.adhoclabs.template.models.JsonSupport
import org.scalatest.flatspec.AnyFlatSpec

class HealthApiTest
    extends AnyFlatSpec
        with ScalatestRouteTest
        with HealthApi
        with JsonSupport {

  behavior of "GET /health"

  it should "return a 200 response with an empty body" in {
    Get(s"/health") ~> healthRoutes ~> check {
      assert(status == StatusCodes.OK)
    }
  }

}