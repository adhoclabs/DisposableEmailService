package co.adhoclabs.template.api

import java.util.UUID
import co.adhoclabs.model.{EmptyResponse, ErrorResponse}
import co.adhoclabs.template.business.AlbumManager
import co.adhoclabs.template.models.{Album, AlbumWithSongs, CreateAlbumRequest, Genre, PatchAlbumRequest}
import zio.schema.{DeriveSchema, Schema}

import zio._
import zio.http._
import zio.http.endpoint.openapi.OpenAPIGen
import zio.http.endpoint.Endpoint
import Schemas._
import co.adhoclabs.template.exceptions.{AlbumAlreadyExistsException, NoSongsInAlbumException}

object AlbumEndpoints {
  val submit =
    Endpoint(Method.POST / "albums")
      .in[CreateAlbumRequest]
      .out[AlbumWithSongs](Status.Created)
      .outError[InternalErrorResponse](Status.InternalServerError)
      .outError[BadRequestResponse](Status.BadRequest)
      .examplesIn(
        "simple" ->
          CreateAlbumRequest(
            title   = "SuperAlbum",
            artists = List("Muse", "Robyn", "TaylorSwift"),
            genre   = Genre.Rock,
            songs   = List("KnightsOfCydonia", "Starlight", "Delicate", "WithMyFriends")
          )
      )

  val get =
    // TODO Return 404 when album with id not found
    Endpoint(Method.GET / "albums" / uuid("albumId"))
      .out[AlbumWithSongs](Status.Created)
      .outError[ErrorResponse](Status.NotFound)
      .examplesIn(
        "Pre-existing Record" -> UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479")
      )

  val patch =
    Endpoint(Method.PATCH / "albums" / uuid("albumId"))
      .in[PatchAlbumRequest]
      .out[Album] // TODO Why not AlbumWithSongs here?
      .outError[ErrorResponse](Status.NotFound)
      .outError[ErrorResponse](Status.BadRequest)
      .examplesIn(
        "Change Title and Artists" ->
          (
            UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"),
            PatchAlbumRequest(
              title   = Some("Rock Album 1 - Director's Cut"),
              artists = Some(List("Artist1", "Artist2")),
              genre   = Some(Genre.Rock)
            )
          ),
        "Restore original" -> (
          UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"),
          PatchAlbumRequest(
            title   = Some("Rock Album 1"),
            artists = Some(List("Artist1")),
            genre   = Some(Genre.Rock)
          )
        )
      )

  // TODO Why do we need this to be in this file, rather than just one more entry in the Schemas object?
  implicit val schema: Schema[EmptyResponse] = DeriveSchema.gen
  val delete =
    // TODO Return 404 when album with id not found?
    Endpoint(Method.DELETE / "albums" / uuid("albumId"))
      .out[EmptyResponse](Status.NoContent) // TODO Why not AlbumWithSongs here?

  val openAPI =
    OpenAPIGen.fromEndpoints(
      title   = "BurnerAlbums",
      version = "1.0",
      submit,
      get,
      patch,
      delete
    )

  val endpoints =
    List(
      submit,
      get,
      patch,
      delete
    )
}

case class AlbumRoutes(implicit albumManager: AlbumManager) {
  val submit = AlbumEndpoints.submit.implement {
    Handler.fromFunctionZIO {
      (createAlbumRequest: CreateAlbumRequest) =>
        for {
          futureRes <- ZIO.fromFuture(
            implicit ec =>
              albumManager.create(createAlbumRequest)
          ).mapError {
              case conflict: AlbumAlreadyExistsException =>
                println("Right")
                Right(BadRequestResponse(conflict.errorResponse))

              case ex: NoSongsInAlbumException =>
                println("Right")
                Right(BadRequestResponse(ex.errorResponse))
              case other =>
                println("Left")
                Left(InternalErrorResponse(ErrorResponse(other.getMessage)))
            }

        } yield futureRes
    }
  }

  val get = AlbumEndpoints.get.implement {
    Handler.fromFunctionZIO {
      (albumId: UUID) =>
        ZIO.fromFuture(
          implicit ec =>
            albumManager.getWithSongs(albumId)
        ).orDie
          .someOrFail(ErrorResponse("Could not find album!"))
    }
  }

  val patch = AlbumEndpoints.patch.implement {
    Handler.fromFunctionZIO {
      case (albumId: UUID, patchAlbumRequest: PatchAlbumRequest) =>
        ZIO.fromFuture(
          implicit ec =>
            albumManager.patch(albumId, patchAlbumRequest)
        ).orDie
          .someOrFail(ErrorResponse("Could not find album!"))
    }
  }

  val delete = AlbumEndpoints.delete.implement {
    Handler.fromFunctionZIO {
      (albumId: UUID) =>
        ZIO.fromFuture(
          implicit ec =>
            albumManager.delete(albumId)
        ).orDie
          .as(EmptyResponse())
    }
  }

  val routes = Routes(
    submit,
    get,
    patch,
    delete
  )
}
