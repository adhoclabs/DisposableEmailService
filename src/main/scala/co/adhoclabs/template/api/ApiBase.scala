package co.adhoclabs.template.api

import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, extractRequest}
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.unmarshalling.Unmarshal
import co.adhoclabs.model.exceptions.HttpException
import co.adhoclabs.template.actorsystem._
import co.adhoclabs.template.exceptions.{UnexpectedException, ValidationException}
import co.adhoclabs.template.models.JsonSupport
import org.slf4j.LoggerFactory

trait ApiBase extends JsonSupport {
  protected val logger = LoggerFactory.getLogger(this.getClass)

  protected def logRequestException(request: HttpRequest, exception: Exception): Unit =
    logger.error(s"${request.method} request to ${request.uri} with body ${Unmarshal(request.entity).to[String]} " +
        s"failed with exception message: ${exception.getMessage})", exception)

  protected implicit val exceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case httpException: HttpException =>
        extractRequest { request: HttpRequest =>
          logRequestException(request, httpException)
          complete(httpException.response)
        }
      case validationException: ValidationException =>
        extractRequest { request =>
          logRequestException(request, validationException)
          complete(StatusCodes.BadRequest -> validationException.errorResponse)
        }
      case unexpectedException: UnexpectedException =>
        extractRequest { request =>
          logRequestException(request, unexpectedException)
          complete(StatusCodes.InternalServerError -> unexpectedException.errorResponse)
        }
      case exception: Exception =>
        extractRequest { request =>
          logRequestException(request, exception)
          complete(StatusCodes.InternalServerError -> exception.getMessage)
        }
    }
}
