package co.adhoclabs.template.api

import java.util.UUID
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import co.adhoclabs.model.EmptyResponse
import co.adhoclabs.template.business.AlbumManager
import co.adhoclabs.template.models.{CreateAlbumRequest, PatchAlbumRequest}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext

trait AlbumApi extends ApiBase {
  val routes: Route
}

class AlbumApiImpl(
  implicit
  albumManager:     AlbumManager,
  executionContext: ExecutionContext
) extends AlbumApi {

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
