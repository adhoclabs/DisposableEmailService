package co.adhoclabs.template.api

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.model.StatusCodes.{BadRequest, InternalServerError}
import akka.http.scaladsl.server.Directives.{complete, extractRequest}
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.unmarshalling.Unmarshal
import co.adhoclabs.model.exceptions.HttpException
import co.adhoclabs.template.models.JsonSupport
import org.slf4j.LoggerFactory
import co.adhoclabs.template.actorsystem._
import co.adhoclabs.template.exceptions.ValidationException

trait ApiBase extends JsonSupport {
  protected val logger = LoggerFactory.getLogger(this.getClass)

  private def logRequestException(request: HttpRequest, exception: Exception): Unit =
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
          complete(HttpResponse(BadRequest, entity = validationException.getMessage))
        }
      case exception: Exception =>
        extractRequest { request =>
          logRequestException(request, exception)
          complete(HttpResponse(InternalServerError, entity = exception.getMessage))
        }
    }
}
