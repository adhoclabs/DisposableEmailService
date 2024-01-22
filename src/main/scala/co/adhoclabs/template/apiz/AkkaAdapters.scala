package co.adhoclabs.template.apiz

import akka.actor.ActorSystem
import akka.event.Logging.LogLevel
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse, StatusCodes, UniversalEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RouteResult.{Complete, Rejected}
import akka.http.scaladsl.server.directives.LoggingMagnet
import akka.http.scaladsl.server.{ExceptionHandler, Rejection, RequestContext, Route, RouteResult}
import akka.util.ByteString
import co.adhoclabs.template.api.{AlbumApi, AlbumApiImpl, ApiBase, HealthApi, HealthApiImpl, SongApi, SongApiImpl}
import co.adhoclabs.template.business.{AlbumManager, HealthManager, SongManager}
import co.adhoclabs.template.exceptions.{UnexpectedException, ValidationException}
import org.slf4j.{Logger, LoggerFactory}
import zio.http.endpoint.openapi.{OpenAPIGen, SwaggerUI}
import zio.{Unsafe, ZIO, http}
import zio.http.{Body, Header, Headers, Method, Middleware, Request, Response, Status, URL}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

class AkkaAdapters(
  implicit
  actorSystem:      ActorSystem,
  albumManager:     AlbumManager,
  executionContext: ExecutionContext,
  songManager:      SongManager,
  healthManager:    HealthManager,
  apiZ:             ApiZ
) extends Api {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val healthApi: HealthApi = new HealthApiImpl
  val songApi: SongApi = new SongApiImpl
  val albumApi: AlbumApi = new AlbumApiImpl

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

  override val routes: Route = {
    concat(
      healthApi.routes,
      passUnhandledRequestsOverToZioHttp
    )
  }

  private def convertEntity(entity: HttpEntity): Future[Array[Byte]] = {
    import scala.concurrent.duration._
    import akka.http.scaladsl.model.HttpEntity
    import akka.util.ByteString
    import scala.concurrent.Future
    import akka.http.scaladsl.unmarshalling.Unmarshal
    import akka.stream.Materializer
    import scala.concurrent.ExecutionContext
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
