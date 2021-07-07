package co.adhoclabs.template.api

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RouteResult.{Complete, Rejected}
import akka.http.scaladsl.server.directives.LoggingMagnet
import akka.http.scaladsl.server.{ExceptionHandler, Rejection, Route, RouteResult}
import akka.util.ByteString
import co.adhoclabs.template.business.{AlbumManager, HealthManager, SongManager}
import co.adhoclabs.template.exceptions.{UnexpectedException, ValidationException}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

trait Api extends ApiBase

class ApiImpl(implicit albumManager: AlbumManager, songManager: SongManager, healthManager: HealthManager, actorSystem: ActorSystem, executionContext: ExecutionContext) extends Api {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val healthApi: HealthApi = new HealthApiImpl
  val songApi: SongApi = new SongApiImpl
  val albumApi: AlbumApi = new AlbumApiImpl

  override val routes: Route = healthApi.routes ~
      logRequestResult(LoggingMagnet(_ =>requestAndResponseLoggingHandler)) {
        handleExceptions(exceptionHandler) {
          songApi.routes ~ albumApi.routes
        }
      }

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
    case Complete(response) => logRequestResponse(request, response)
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
