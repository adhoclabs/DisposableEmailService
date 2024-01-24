package co.adhoclabs.template.api

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{RequestContext, Route, RouteResult}
import org.slf4j.{Logger, LoggerFactory}
import zio.Unsafe
import zio.http.{Body, Header, Headers, Method, Request, URL}

import scala.concurrent.{ExecutionContext, Future}

class AkkaAdapters(
  implicit
  actorSystem:      ActorSystem,
  executionContext: ExecutionContext,
  apiZ:             ApiZ
) {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private def passUnhandledRequestsOverToZioHttp(req: RequestContext)(implicit apiZ: ApiZ): Future[RouteResult] = {

    val runtime = zio.Runtime.default
    for {
      zioRequest <- {
        println("\nUnrecognized akka request: " + req.request + "\n")
        akkaToZio(req.request)
      }
      response <- {
        println("\nconverted zio     request: " + zioRequest + "\n")

        val routeZio = apiZ.zioRoutes.apply(zioRequest)
        Unsafe.unsafe { implicit unsafe =>
          runtime.unsafe.runToFuture(routeZio.mapError(errorResponse => throw new Exception(errorResponse.body.toString)))
        }
        //        Future.successful()
      }
      body <- Unsafe.unsafe { implicit unsafe =>
        runtime.unsafe.runToFuture(response.body.asString.orDie)
      }

    } yield {
      RouteResult.Complete(HttpResponse.apply(StatusCodes.OK, entity = body))
    }
  }

  // This field was originally defined in the Api trait.
  val routes: Route = {
    concat(
      path("api") {
        get {
          complete {
            StatusCodes.OK
          }
        }
      },
      passUnhandledRequestsOverToZioHttp
    )
  }

  private def convertEntity(entity: HttpEntity): Future[Array[Byte]] = {
    import akka.http.scaladsl.unmarshalling.Unmarshal
    import akka.stream.Materializer
    import akka.util.ByteString

    import scala.concurrent.Future
    //      import scala.concurrent.ExecutionContext.Implicits.global

    def entityToBytes()(implicit materializer: Materializer): Future[Array[Byte]] = {
      Unmarshal(entity).to[ByteString].map(_.toArray)
    }
    entityToBytes()
  }

  private def akkaToZio(akkaRequest: HttpRequest): Future[Request] = {
    val headers = {
      Headers.apply(
        akkaRequest.headers.map { header =>
          Header.Custom(header.name(), header.value())
        }
      )
    }

    convertEntity(akkaRequest.entity) map { entityBytes =>
      Request(
        method  = Method.fromString(akkaRequest.method.name),
        url     = URL.decode(akkaRequest.uri.toString()).getOrElse(???),
        headers = headers,
        body    = Body.fromArray(entityBytes)
      )
    }

  }

}
