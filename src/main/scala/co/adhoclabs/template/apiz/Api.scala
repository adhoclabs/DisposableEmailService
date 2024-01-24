package co.adhoclabs.template.apiz

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RouteResult.{Complete, Rejected}
import akka.http.scaladsl.server.{ExceptionHandler, Rejection, RouteResult}
import akka.util.ByteString
import co.adhoclabs.model.ErrorResponse
import co.adhoclabs.template.api.ApiBase
import co.adhoclabs.template.business.{AlbumManager, HealthManager, SongManager}
import co.adhoclabs.template.exceptions.{UnexpectedException, ValidationException}
import org.slf4j.{Logger, LoggerFactory}
import zio.http.endpoint.Endpoint
import zio.{Cause, ZIO}
import zio.http.endpoint.openapi.{OpenAPIGen, SwaggerUI}
import zio.http.{Body, Handler, Middleware, Response, Route, RoutePattern, Routes, Status}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

case class ApiZ(implicit albumApiZ: AlbumRoutes, songRoutes: SongRoutes, healthRoute: HealthRoutes) {
  val openApi = OpenAPIGen.fromEndpoints(
    title   = "BurnerAlbums",
    version = "1.0",
    SongApiEndpoints.endpoints ++ AlbumEndpoints.endpoints ++ HealthEndpoint.endpoints
  )

  val docsRoute =
    SwaggerUI.routes("docs", openApi)

  val unhandled =
    Routes(
      Endpoint(RoutePattern.any)
        .out[String]
        .implement(
          Handler.fromFunctionZIO(
            _ =>
              ZIO.debug("Unhandled.").as("Unhandled")
          )
        )
    //      Route.route(RoutePattern.any).apply(Handler.fromZIO(ZIO.debug("Unhandled.").as(Response.text("Unhandled"))))
    )

  // TODO Where should this live?
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val zioRoutes = (docsRoute ++ healthRoute.routes ++ albumApiZ.routes ++ songRoutes.routes ++ unhandled)
    .handleErrorCause { cause =>
      import Schemas.errorResponseSchema
      import zio.schema.codec.JsonCodec.schemaBasedBinaryCodec
      println("In handleErrorCause")
      cause match {
        case Cause.Fail(value, trace) =>
          println("fail status: " + value.status)
          //          value.
          value
          value.copy(body = Body.from(ErrorResponse("Fail. " + value.body.toString)))
        //          Response(Status.InternalServerError, body = value.body)
        case Cause.Die(value, trace) =>
          value match {
            case validationException: ValidationException =>
              println("ValidationException: " + validationException)
              Response(Status.BadRequest, body = Body.from(validationException.errorResponse))
            case unexpectedException: UnexpectedException =>
              println("Unexpected: " + unexpectedException)
              Response(Status.InternalServerError, body = Body.from(unexpectedException.errorResponse))
            case exception: Exception =>
              logger.error("", exception)
              println("", exception)
              Response(Status.InternalServerError, body = Body.from(ErrorResponse("Exception. " + exception.getMessage)))
            case rawThrowable =>
              Response(Status.BadRequest, body = Body.from(ErrorResponse(rawThrowable.getMessage)))
          }
        case interrupt: Cause.Interrupt =>
          Response(Status.InternalServerError, body = Body.from(ErrorResponse("Process Interrupted. " + interrupt)))
        case other =>
          println("Other: " + other)
          Response(Status.Forbidden, body = Body.from(ErrorResponse(other.prettyPrint)))

      }
      //      Response(Status.Forbidden, body = Body.fromString(cause.prettyPrint))
    }
    .mapErrorZIO(errResponse =>
      ZIO.debug("ErrorResponse: " + errResponse).as(errResponse)) @@
    Middleware.requestLogging(statusCode => zio.LogLevel.Warning)
  //    Middleware.debug

  /*    Middleware.intercept {
        (request, response) =>
          println("Submit to datadog")
          response
      }
*/
}
trait Api extends ApiBase

class ApiImpl(
  implicit
  actorSystem:      ActorSystem,
  albumManager:     AlbumManager,
  executionContext: ExecutionContext,
  songManager:      SongManager,
  healthManager:    HealthManager,
  apiZ:             ApiZ
) {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private def logRequestResponse(request: HttpRequest, response: HttpResponse): Unit = {
    val timeout = 10.millis
    for {
      requestBodyAsBytes: ByteString <- request.entity.toStrict(timeout).map(_.data)
      responseBodyAsByes: ByteString <- response.entity.toStrict(timeout).map(_.data)
    } yield {
      val requestBodyString: String = requestBodyAsBytes.utf8String
      val responseBodyString: String = responseBodyAsByes.utf8String

      logger.info(
        (s"${response.status} " +
          s"${request.method.name} " +
          s"${request.uri} " +
          s"REQUEST BODY: $requestBodyString " +
          s"RESPONSE BODY: $responseBodyString").replace("\n", "")
      )
    }
  }

  private def logRequestRejection(request: HttpRequest, rejections: Seq[Rejection]): Unit = {
    val timeout = 10.millis
    request.entity.toStrict(timeout).map(_.data) map { requestBodyAsBytes: ByteString =>
      logger.info(
        (s"REJECTED: " +
          s"${request.method.name} " +
          s"${request.uri} " +
          s"REQUEST BODY: ${requestBodyAsBytes.utf8String} " +
          s"REJECTIONS: [${rejections.mkString(", ")}]").replace("\n", "")
      )
    }
  }

  protected def requestAndResponseLoggingHandler(request: HttpRequest): RouteResult => Unit = {
    case Complete(response)   => logRequestResponse(request, response)
    case Rejected(rejections) => logRequestRejection(request, rejections)
  }

  protected def logRequestException(exception: Exception): Unit =
    logger.error("", exception)

  protected val exceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case validationException: ValidationException =>
        logRequestException(validationException)
        complete(StatusCodes.BadRequest -> validationException.errorResponse)
      case unexpectedException: UnexpectedException =>
        logRequestException(unexpectedException)
        complete(StatusCodes.InternalServerError -> unexpectedException.errorResponse)
      case exception: Exception =>
        logRequestException(exception)
        complete(StatusCodes.InternalServerError -> exception.getMessage)
    }
}

