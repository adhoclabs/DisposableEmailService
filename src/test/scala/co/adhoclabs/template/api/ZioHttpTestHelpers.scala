package co.adhoclabs.template.api

import co.adhoclabs.model.ErrorResponse
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.OneInstancePerTest
import org.scalatest.funspec.AsyncFunSpec
import zio.{Exit, Unsafe, ZIO}
import zio.http.{HttpApp, Request, Status}
import zio.schema.Schema
import zio.schema.codec.JsonCodec.schemaBasedBinaryCodec

trait ZioHttpTestHelpers extends AsyncFunSpec with AsyncMockFactory with OneInstancePerTest {
  def provokeServerFailure(
    app:            HttpApp[Any],
    request:        Request,
    expectedStatus: Status,
    errorAssertion: ErrorResponse => Boolean = _ => true
  ) = {
    import co.adhoclabs.template.api.Schemas.errorResponseSchema

    val (status, errorResponse) =
      invokeZioRequest[ErrorResponse](app, request)
        .left
        .getOrElse(throw new Exception("Broken failure test!"))

    println("TODO Better assertion reporting. errorResponse: " + errorResponse)
    println("TODO Better assertion reporting. errorStatus  : " + status)
    assert(status == expectedStatus)
    assert(errorAssertion(errorResponse))
  }

  def provokeServerSuccess[T: Schema](
    app:              HttpApp[Any],
    request:          Request,
    expectedStatus:   Status,
    payloadAssertion: T => Boolean = (_: T) => true
  ) = {
    val (status, errorResponse) =
      invokeZioRequest(app, request)
        .right
        .getOrElse(throw new Exception("Broken successful test!"))
    assert(status == expectedStatus)
    assert(payloadAssertion(errorResponse))
  }

  private def invokeZioRequest[T: Schema](app: HttpApp[Any], request: Request): Either[(Status, ErrorResponse), (Status, T)] = {
    import co.adhoclabs.template.api.Schemas.errorResponseSchema
    val runtime = zio.Runtime.default
    Unsafe.unsafe { implicit unsafe =>
      runtime.unsafe.run {
        (for {
          response <- app.apply(request)
          _ <- ZIO.when(response.status.isError)(
            for {
              _ <- response.body.asString.debug("Error response body: ")
              errorResponse <- response.body.to[ErrorResponse]
            } yield ZIO.fail((response.status, errorResponse))
          )
          res <- response.body.to[T]
        } yield (response.status, res))
          .mapError {
            case (errorStatus: Status, er: ErrorResponse) =>
              println("A")
              (errorStatus, er)
            case other =>
              println("B")
              (Status.InternalServerError, ErrorResponse(other.toString))
          }
      }
    } match {
      case Exit.Success((status, value)) =>
        value match {
          case er: ErrorResponse =>
            Left((status, er))
          case other =>
            Right((status, value))
        }
      case other =>
        println("Other: " + other)
        other match {
          case Exit.Success(value) => Right(value)
          case Exit.Failure(cause) =>
            Left(cause.failureOrCause.left.getOrElse(???))
          // Left(cause.g)
        }
      //        throw new Exception("Unexpected exit: " + other)
    }
  }

}
