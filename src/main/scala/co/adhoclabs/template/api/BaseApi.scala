package co.adhoclabs.template.api


import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.server.Directives.{complete, extractRequest}
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.unmarshalling.Unmarshal
import co.adhoclabs.template.models.JsonSupport
import org.slf4j.LoggerFactory

trait BaseApi extends JsonSupport {
  protected val logger = LoggerFactory.getLogger(this.getClass)

  implicit val system: ActorSystem

  private def logRequestException(request: HttpRequest, exception: Exception): Unit =
    logger.error(s"${request.method} request to ${request.uri} with body ${Unmarshal(request.entity).to[String]} " +
        s"failed with exception: ${exception.toString}(message: ${exception.getMessage})", exception)

  protected val exceptionHandler: ExceptionHandler =
    ExceptionHandler {
//      case httpException: HttpException =>
//        extractRequest { request: HttpRequest =>
//          logRequestException(request, httpException)
//          complete(httpException.response)
//        }
      case exception: Exception =>
        extractRequest { request =>
          logRequestException(request, exception)
          complete(HttpResponse(InternalServerError, entity = exception.getMessage))
        }
    }

}
