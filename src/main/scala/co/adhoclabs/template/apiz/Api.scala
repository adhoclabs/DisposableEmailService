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

case class ApiZ(implicit albumApiZ: AlbumRoutes, songRoutes: SongRoutes, healthRoute: HealthRoutes) {
  val openApi = OpenAPIGen.fromEndpoints(
    title   = "BurnerAlbums",
    version = "1.0",
    SongApiEndpoints.endpoints ++ AlbumEndpoints.endpoints ++ HealthEndpoint.endpoints
  )

  val docsRoute =
    SwaggerUI.routes("docs", openApi)

  val zioRoutes = (docsRoute ++ healthRoute.routes ++ albumApiZ.routes ++ songRoutes.routes)
    .handleErrorCause { cause =>
      println("eh? defects galore?")
      Response(Status.Forbidden, body = Body.fromString(cause.prettyPrint))
    } @@ Middleware.requestLogging(statusCode => zio.LogLevel.Warning) @@ Middleware.debug
  //  @@ Middleware.intercept {
  //      (request, response) =>
  //        println("Should log stuffz")
  //        response
  //
  //    }

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
      logRequestResult(LoggingMagnet(_ => requestAndResponseLoggingHandler)) {
        handleExceptions(exceptionHandler) {
          songApi.routes ~ albumApi.routes
        }
      },
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

