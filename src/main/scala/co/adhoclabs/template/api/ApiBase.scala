package co.adhoclabs.template.api

import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.RouteResult.Rejected
import akka.http.scaladsl.server.directives.LogEntry
import akka.http.scaladsl.server.{ExceptionHandler, Rejection, RouteResult}
import akka.http.scaladsl.unmarshalling.Unmarshal
import co.adhoclabs.template.actorsystem._
import co.adhoclabs.template.exceptions.{UnexpectedException, ValidationException}
import co.adhoclabs.template.models.JsonSupport
import org.slf4j.LoggerFactory

trait ApiBase extends JsonSupport {
  protected val logger = LoggerFactory.getLogger(this.getClass)

  private def logRequestResponse(request: HttpRequest, response: HttpResponse): Option[LogEntry] = {
    logger.info(
      s"${response.status} " +
          s"${request.method.name} " +
          s"${request.uri} " +
          // we are logging request bodies here; if request bodies in your app contain sensitive information, consider changing this
          s"${if (!request.entity.httpEntity.isKnownEmpty) "request body: " + Unmarshal(request.entity).to[String] else ""} " +
          s"response body: ${Unmarshal(response.entity).to[String]}"
    )
    None
  }

  private def logRequestRejection(request: HttpRequest, rejections: Seq[Rejection]): Option[LogEntry] = {
    logger.info(
      s"REJECTED: " +
          s"${request.method.name} " +
          s"${request.uri} " +
          s"${if (!request.entity.httpEntity.isKnownEmpty) "request body: " + Unmarshal(request.entity).to[String] else ""} " +
          s"rejections: ${rejections.mkString(", ")}"
    )
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
