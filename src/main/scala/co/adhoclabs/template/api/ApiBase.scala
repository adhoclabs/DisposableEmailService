package co.adhoclabs.template.api

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, onSuccess}
import akka.http.scaladsl.server.Route
import org.slf4j.Logger

import scala.concurrent.Future

trait ApiBase {
  val routes: Route
  protected val logger: Logger

  protected def return404IfFutureOptionIsEmpty[T](
    action:            Future[Option[T]],
    successStatusCode: StatusCodes.Success = StatusCodes.OK
  )(implicit m: ToEntityMarshaller[T]): Route = {
    onSuccess(action)(return404IfOptionIsEmpty(_, successStatusCode))
  }

  protected def return404IfOptionIsEmpty[T](
    actionResult:      Option[T],
    successStatusCode: StatusCodes.Success = StatusCodes.OK
  )(implicit m: ToEntityMarshaller[T]): Route = {
    actionResult match {
      case Some(t) => complete(successStatusCode, t)
      case None    => complete(StatusCodes.NotFound, "")
    }
  }
}
