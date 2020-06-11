package co.adhoclabs.template.api

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{ExceptionHandler, Rejection, Route, RouteResult}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RouteResult.Rejected
import akka.http.scaladsl.server.directives.LogEntry
import akka.http.scaladsl.unmarshalling.Unmarshal
import co.adhoclabs.template.business.{AlbumManager, SongManager}
import co.adhoclabs.template.exceptions.{UnexpectedException, ValidationException}
import org.slf4j.{Logger, LoggerFactory}

trait Api extends ApiBase

class ApiImpl(implicit albumManager: AlbumManager, songManager: SongManager, actorSystem: ActorSystem) extends Api {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val healthApi: HealthApi = new HealthApiImpl
  val songApi: SongApi = new SongApiImpl
  val albumApi: AlbumApi = new AlbumApiImpl

  override val routes: Route = healthApi.routes ~
      logRequestResult(requestAndResponseLoggingHandler _) {
        handleExceptions(exceptionHandler) {
          songApi.routes ~ albumApi.routes
        }
      }

  private def logRequestResponse(request: HttpRequest, response: HttpResponse): Option[LogEntry] = {
    logger.info(
      s"${response.status} " +
          s"${request.method.name} " +
          s"${request.uri} " +
          // we are logging request bodies here; if request bodies in your app contain sensitive information, consider changing this
          s"${if (!request.entity.httpEntity.isKnownEmpty) "request body: " + Unmarshal(request.entity).to[String] else ""} " +
          s"response body: ${Unmarshal(response.entity).to[String]}"
    )
    // This ensures that these logs are the same format as logs elsewhere in the service
    None
  }

  private def logRequestRejection(request: HttpRequest, rejections: Seq[Rejection]): Option[LogEntry] = {
    logger.error(
      s"REJECTED: " +
          s"${request.method.name} " +
          s"${request.uri} " +
          s"${if (!request.entity.httpEntity.isKnownEmpty) "request body: " + Unmarshal(request.entity).to[String] else ""} " +
          s"rejections: ${rejections.mkString(", ")}"
    )
    // This ensures that these logs are the same format as logs elsewhere in the service
    None
  }

  protected def requestAndResponseLoggingHandler(request: HttpRequest): RouteResult => Option[LogEntry] = {
    case RouteResult.Complete(response) => logRequestResponse(request, response)
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
