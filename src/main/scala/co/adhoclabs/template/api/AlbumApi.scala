package co.adhoclabs.template.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import co.adhoclabs.template.business.AlbumManager
import co.adhoclabs.template.models.{Album, CreateAlbumRequest}
import java.util.UUID
import org.slf4j.{Logger, LoggerFactory}

trait AlbumApi extends ApiBase {
  val routes: Route
}

class AlbumApiImpl(implicit albumManager: AlbumManager) extends AlbumApi {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  override val routes: Route = pathPrefix("albums") {
    concat (
      pathEnd {
        post {
          postAlbumRoute
        }
      },
      // Be aware that legacy burner users may have ids that are not valid UUIDs,
      // so we shouldn't make user id fields UUIDs
      pathPrefix(JavaUUID) { id: UUID =>
        concat (
          pathEnd {
            concat(
              get {
                getAlbumRoute(id)
              },
              put {
                putAlbumRoute(id)
              }
            )
          }
        )
      }
    )
  }

  def getAlbumRoute(id: UUID): Route =
    rejectEmptyResponse {
      complete {
        albumManager.get(id)
      }
    }


  def postAlbumRoute: Route =
    entity(as[CreateAlbumRequest]) { albumRequest: CreateAlbumRequest =>
      complete {
        StatusCodes.Created -> albumManager.create(albumRequest)
      }
    }

  def putAlbumRoute(id: UUID): Route =
    entity(as[Album]) { album: Album =>
      rejectEmptyResponse {
        complete {
          albumManager.update(album)
        }
      }
    }
}
