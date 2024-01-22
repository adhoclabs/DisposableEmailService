package co.adhoclabs.template.apiz

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

object SongApiEndpoints {
  import zio.http.codec.PathCodec._

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
