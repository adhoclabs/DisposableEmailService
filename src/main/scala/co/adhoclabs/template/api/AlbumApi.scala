package co.adhoclabs.template.api

import java.util.UUID
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import co.adhoclabs.model.{EmptyResponse, ErrorResponse}
import co.adhoclabs.template.business.AlbumManager
import co.adhoclabs.template.models.{Album, AlbumWithSongs, CreateAlbumRequest, Genre, PatchAlbumRequest}
import org.slf4j.{Logger, LoggerFactory}
import zio.schema.{DeriveSchema, Schema}

import scala.concurrent.ExecutionContext

trait AlbumApi extends ApiBase {
  val routes: Route
}

import zio._
import zio.http._
import zio.http.codec.PathCodec
import zio.http.endpoint.openapi.{OpenAPIGen, SwaggerUI}
import zio.http.endpoint.Endpoint

import Schemas._

object AlbumEndpoints {
  val submit =
    Endpoint(Method.POST / "albums")
      .in[CreateAlbumRequest]
      .out[AlbumWithSongs]
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
    // TODO Return 404 when album with id not found?
    Endpoint(Method.PATCH / "albums" / uuid("albumId"))
      .in[PatchAlbumRequest]
      .out[Album] // TODO Why not AlbumWithSongs here?
      .outError[ErrorResponse](Status.NotFound)
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
      .out[EmptyResponse] // TODO Why not AlbumWithSongs here?

  val openAPI =
    OpenAPIGen.fromEndpoints(
      title   = "Burner",
      version = "1.0",
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
        ZIO.fromFuture(
          implicit ec =>
            albumManager.create(createAlbumRequest)
        ).orDie

    }
  }

  val get = AlbumEndpoints.get.implement {
    Handler.fromFunctionZIO {
      (albumId: UUID) =>
        ZIO.fromFuture(
          implicit ec =>
            albumManager.getWithSongs(albumId)
        ).orDie
          .someOrFail("Could not find album!")
    }.mapError(ex => ErrorResponse(ex))
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

class AlbumApiImpl(implicit albumManager: AlbumManager, executionContext: ExecutionContext) extends AlbumApi {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  override val routes: Route = {
    pathPrefix("albums") {
      concat(
        pathEnd {
          post {
            postAlbumRoute
          }
        },
        // Be aware that legacy burner users may have ids that are not valid UUIDs,
        // so we shouldn't make user id fields UUIDs
        pathPrefix(JavaUUID) { id: UUID =>
          concat(
            pathEnd {
              concat(
                get {
                  getAlbumRoute(id)
                },
                patch {
                  patchAlbumRoute(id)
                },
                delete {
                  deleteAlbumRoute(id)
                }
              )
            }
          )
        }
      )
    }
  }

  def getAlbumRoute(id: UUID): Route = {
    return404IfFutureOptionIsEmpty {
      albumManager.getWithSongs(id)
    }
  }

  def postAlbumRoute: Route = {
    entity(as[CreateAlbumRequest]) { albumRequest: CreateAlbumRequest =>
      complete {
        StatusCodes.Created -> albumManager.create(albumRequest)
      }
    }
  }

  def patchAlbumRoute(id: UUID): Route = {
    entity(as[PatchAlbumRequest]) { patchRequest: PatchAlbumRequest =>
      return404IfFutureOptionIsEmpty {
        albumManager.patch(id, patchRequest)
      }
    }
  }

  def deleteAlbumRoute(id: UUID): Route = {
    complete {
      StatusCodes.NoContent -> albumManager.delete(id).map(_ => EmptyResponse())
    }
  }
}
