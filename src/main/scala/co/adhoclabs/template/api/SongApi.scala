package co.adhoclabs.template.api

import java.util.UUID
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ch.qos.logback.core.status.ErrorStatus
import co.adhoclabs.model.{EmptyResponse, ErrorResponse}
import co.adhoclabs.template.business.SongManager
import co.adhoclabs.template.models.{CreateSongRequest, Song}
import org.slf4j.{Logger, LoggerFactory}
import zio.http.codec.Doc

import scala.concurrent.ExecutionContext

trait SongApi extends ApiBase {
  val routes: Route
}

class SongApiImpl(implicit songManager: SongManager, executionContext: ExecutionContext) extends SongApi {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  override val routes: Route = {
    pathPrefix("songs") {
      concat(
        pathEnd {
          post {
            postSong
          }
        },
        path(JavaUUID) { id: UUID =>
          concat(
            get {
              getSong(id)
            },
            put {
              putSong(id)
            },
            delete {
              deleteSong(id)
            }
          )
        }
      )
    }
  }

  def getSong(id: UUID): Route = {
    return404IfFutureOptionIsEmpty {
      songManager.get(id)
    }
  }

  def postSong: Route = {
    entity(as[CreateSongRequest]) { songRequest: CreateSongRequest =>
      complete {
        StatusCodes.Created -> songManager.create(songRequest)
      }
    }
  }

  def putSong(id: UUID): Route = {
    entity(as[Song]) { song: Song =>
      return404IfFutureOptionIsEmpty {
        songManager.update(song)
      }
    }
  }

  def deleteSong(id: UUID): Route = {
    complete {
      StatusCodes.NoContent -> songManager.delete(id).map(_ => EmptyResponse())
    }
  }
}

import zio.schema.{DeriveSchema, Schema}
import zio._
import zio.http._
import zio.http.codec.PathCodec
import zio.http.endpoint.openapi.{OpenAPIGen, SwaggerUI}
import zio.http.endpoint.Endpoint
import Schemas._

object SongApiEndpoints {
  import zio.http.codec.PathCodec._

  //  import zio.http.codec.HttpCodec._
  implicit val schema: Schema[Song] = DeriveSchema.gen[Song]

  val getSong =
    Endpoint(Method.GET / "songs" / uuid("songId") ?? Doc.p("The unique identifier of the song"))
      .out[Song]
      .outError[ErrorResponse](Status.NotFound)
      .outError[ErrorResponse](Status.InternalServerError)
      .examplesIn(
        "Pre-existing Song1" -> UUID.fromString("e47ac10b-58cc-4372-a567-0e02b2c3d478"),
        "Pre-existing Song2" -> UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"),
      ) ?? Doc.p("Get a song by ID")

  val getSongs =
    Endpoint(Method.GET / "songs")
      .outError[ErrorResponse](Status.InternalServerError)
      .out[List[Song]] ?? Doc.p("Get all songs")

  val createSong =
    Endpoint(Method.POST / "songs")
      .in[CreateSongRequest] ?? Doc.p("Create a song")

  val updateSong =
    Endpoint(Method.PUT / "songs" / uuid("songId") ?? Doc.p("The unique identifier of the song"))
      .in[Song] ?? Doc.p("Update a song")

  val deleteSong =
    Endpoint(Method.DELETE / "songs" / uuid("songId") ?? Doc.p("The unique identifier of the song"))
      .out[Unit] ?? Doc.p("Delete a song")

  val endpoints =
    List(
      getSong,
      getSongs,
      createSong,
      updateSong,
      deleteSong
    )

}

case class SongRoutes(implicit songManager: SongManager) {
  import zio.http.codec.PathCodec._

  val getSong = SongApiEndpoints.getSong.implement(
    Handler.fromFunctionZIO { (songId: UUID) =>
      ZIO.fromFuture(implicit ec =>
        songManager.get(songId)).mapError {
        case throwable: Throwable =>
          ErrorResponse(throwable.getMessage)
      }.someOrFail(ErrorResponse("Song not found: " + songId))
    }

  )

  val routes =
    Routes(
      getSong

    )
}
