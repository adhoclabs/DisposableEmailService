package co.adhoclabs.email.api

import co.adhoclabs.model.ErrorResponse
import co.adhoclabs.email.exceptions.{UnexpectedException, ValidationException}
import org.slf4j.{Logger, LoggerFactory}
import zio.http.endpoint.Endpoint
import zio.{Cause, ZIO, ZLayer}
import zio.http.endpoint.openapi.{OpenAPIGen, SwaggerUI}
import zio.http.{Body, Handler, Middleware, Response, RoutePattern, Routes, Status}

case class ApiZ(
  emailApiZ:   EmailRoutes,
  healthRoute: HealthRoutes
) {
  val openApi =
    OpenAPIGen.fromEndpoints(
      title = "Burner Emails",
      version = "1.0",
      EmailEndpoints.endpoints ++ HealthEndpoint.endpoints
    )

  val docsRoute = SwaggerUI.routes("email/docs", openApi)

  val unhandled =
    Routes(
      Endpoint(RoutePattern.any)
        .out[String]
        .implement(
          Handler.fromFunctionZIO(_ => ZIO.debug("Unhandled.").as("Unhandled"))
        )
    )

  // TODO Where should this live?
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val zioRoutes =
    (docsRoute ++ healthRoute.routes ++ emailApiZ.routes ++ unhandled)
      .handleErrorCause { cause =>
        import Schemas.errorResponseSchema
        import zio.schema.codec.JsonCodec.schemaBasedBinaryCodec
        println("In handleErrorCause")
        cause match {
          case Cause.Fail(value, trace)   =>
            println("fail status: " + value.status)
            value.copy(body =
              Body.from(
                ErrorResponse("Did you pass the wrong URL into the mocked call?" + value.body.toString)
              )
            )
          case Cause.Die(value, trace)    =>
            value match {
              case validationException: ValidationException =>
                println("ValidationException: " + validationException)
                Response(Status.BadRequest, body = Body.from(validationException.errorResponse))
              case unexpectedException: UnexpectedException =>
                println("Unexpected: " + unexpectedException)
                Response(Status.InternalServerError, body = Body.from(unexpectedException.errorResponse))
              case exception: Exception                     =>
                logger.error("", exception)
                println("", exception)
                Response(
                  Status.InternalServerError,
                  body = Body.from(ErrorResponse("Exception. " + exception.getMessage))
                )
              case rawThrowable                             =>
                Response(Status.BadRequest, body = Body.from(ErrorResponse(rawThrowable.getMessage)))
            }
          case interrupt: Cause.Interrupt =>
            Response(
              Status.InternalServerError,
              body = Body.from(ErrorResponse("Process Interrupted. " + interrupt))
            )
          case other                      =>
            println("Other: " + other)
            Response(Status.Forbidden, body = Body.from(ErrorResponse(other.prettyPrint)))

        }

      }
      .mapErrorZIO(errResponse => ZIO.debug("ErrorResponse: " + errResponse).as(errResponse)) @@
      Middleware.requestLogging(statusCode => zio.LogLevel.Warning)
}

object ApiZ {
  val layer: ZLayer[EmailRoutes with HealthRoutes, Nothing, ApiZ] = ZLayer.fromFunction(ApiZ.apply _)
}
