package co.adhoclabs.template.api

import co.adhoclabs.model.ErrorResponse
import zio.{Cause, Exit, Unsafe, ZIO}
import zio.http.{HttpApp, Request, Status}
import zio.schema.Schema
import zio.schema.codec.JsonCodec.schemaBasedBinaryCodec

trait ZioHttpTestHelpers {
  def provokeServerFailure(app: HttpApp[Any], request: Request, expectedStatus: Status, errorAssertion: ErrorResponse => Boolean = _ => true): (Status, ErrorResponse) = {
    import Schemas.errorResponseSchema

    val (status, errorResponse) =
      invokeZioRequest[ErrorResponse](app, request)
        .left
        .getOrElse(throw new Exception("Broken failure test!"))

    assert(status == expectedStatus)
    assert(errorAssertion(errorResponse))
    (status, errorResponse)
  }

  def provokeServerSuccess[T: Schema](app: HttpApp[Any], request: Request): (Status, T) = {
    invokeZioRequest(app, request).right.getOrElse(throw new Exception("Broken successful test!"))
  }

  def invokeZioRequest[T: Schema](app: HttpApp[Any], request: Request): Either[(Status, ErrorResponse), (Status, T)] = {
    import Schemas.errorResponseSchema
    val runtime = zio.Runtime.default
    import zio.schema._
    //    import zio.schema.derivation._
    import zio.json._
    import spray.json._
    import DefaultJsonProtocol._
    Unsafe.unsafe { implicit unsafe =>
      runtime.unsafe.run {
        (for {
          response <- app.apply(request)
          _ <- ZIO.when(response.status.isError)(
            for {
              errorResponse <- response.body.to[ErrorResponse]
            } yield ZIO.fail((response.status, errorResponse))
          )
          res <- response.body.to[T]
        } yield (response.status, res))
          .mapError {
            case (errorStatus: Status, er: ErrorResponse) => (errorStatus, er)
            case other                                    => (Status.InternalServerError, ErrorResponse(other.toString))
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
      case Exit.Failure(cause) =>
        cause match {
          case Cause.Empty => ???
          case Cause.Fail(value, trace) =>
            Left(value)
          case Cause.Die(value, trace) =>
            ???
          case Cause.Interrupt(fiberId, trace)   => ???
          case Cause.Stackless(cause, stackless) => ???
          case Cause.Then(left, right)           => ???
          case Cause.Both(left, right)           => ???
        }

      //        Left(cause.failureOrCause.left.get)
      //        Left(cause.failureOption.get)

    }
  }

}
