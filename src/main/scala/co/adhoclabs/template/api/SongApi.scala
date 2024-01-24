package co.adhoclabs.template.api

import java.util.UUID
import co.adhoclabs.model.{EmptyResponse, ErrorResponse}
import co.adhoclabs.template.business.SongManager
import co.adhoclabs.template.models.{CreateSongRequest, Song}
import zio.http.codec.Doc
import zio.schema.{DeriveSchema, Schema}
import zio._
import zio.http._
import zio.http.endpoint.Endpoint
import Schemas._
import co.adhoclabs.template.exceptions.{SongAlreadyExistsException, ValidationException}

object SongApiEndpoints {
  import zio.http.codec.PathCodec._

  implicit val schema: Schema[Song] = DeriveSchema.gen[Song]

  // TODO Report that url param description gets attached to the endpoint, not the param
  val getSong =
    Endpoint(Method.GET / "songs" / (uuid("songId") ?? Doc.p("The unique identifier of the song")))
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
      .in[CreateSongRequest]
      .out[Song](Status.Created)
      .outError[InternalErrorResponse](Status.InternalServerError)
      .outError[BadRequestResponse](Status.BadRequest)

  val updateSong =
    Endpoint(Method.PUT / "songs" / uuid("songId") ?? Doc.p("The unique identifier of the song"))
      .in[Song]
      .out[Song]
      .outError[NotFoundRequestResponse](Status.NotFound)
      .outError[InternalErrorResponse](Status.InternalServerError)
      .outError[BadRequestResponse](Status.BadRequest) ?? Doc.p("Update a song")

  val deleteSong =
    Endpoint(Method.DELETE / "songs" / uuid("songId") ?? Doc.p("The unique identifier of the song"))
      .out[Unit](Status.NoContent)
      .outError[InternalErrorResponse](Status.InternalServerError)
      .outError[BadRequestResponse](Status.BadRequest)

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

  val createSong = SongApiEndpoints.createSong.implement(
    Handler.fromFunctionZIO { (createSongRequest: CreateSongRequest) =>
      ZIO.fromFuture(implicit ec =>
        songManager.create(createSongRequest)).tapError(err => ZIO.debug("Err: " + err)).mapError {
        case ex: SongAlreadyExistsException =>
          Right(BadRequestResponse(ex.getMessage))
        case throwable: Throwable =>
          println("Basic throwable: " + throwable)
          Left(InternalErrorResponse(throwable.getMessage))
      }
    }
  )

  val updateSong = SongApiEndpoints.updateSong.implement(
    Handler.fromFunctionZIO {
      case (songId: UUID, song: Song) =>
        ZIO.fromFuture(implicit ec =>
          songManager.update(song)).mapError {
          case throwable: Throwable =>
            Left(Right(InternalErrorResponse(throwable.getMessage)))
        }.someOrFail {
          println("Song not found")
          Left(Left(NotFoundRequestResponse("Song not found: " + songId)))
        }
    }
  )

  val deleteSong = SongApiEndpoints.deleteSong.implement(
    Handler.fromFunctionZIO { (songId: UUID) =>
      ZIO.fromFuture(implicit ec =>
        songManager.delete(songId)).mapError {
        case ex: ValidationException =>
          Right(BadRequestResponse(ex.getMessage))
        case throwable: Throwable =>
          Left(InternalErrorResponse(throwable.getMessage))
      }
    }
  )

  val routes =
    Routes(
      getSong,
      createSong,
      updateSong,
      deleteSong

    )
}
