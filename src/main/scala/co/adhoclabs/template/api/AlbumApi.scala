package co.adhoclabs.template.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.http.scaladsl.server.Route
import co.adhoclabs.template.business.AlbumManager
import co.adhoclabs.template.models.{Album, CreateAlbumRequest}

trait AlbumApi extends ApiBase {

  implicit val albumManager: AlbumManager

  val albumRoutes: Route = pathPrefix("albums") {
    concat (
      pathEnd {
        post {
          postAlbumRoute
        }
      },
      pathPrefix(Segment) { id: String =>
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
          },
          path("songs") {
            getAlbumSongs(id)
          }
        )
      }
    )
  }

  def getAlbumRoute(id: String): Route =
    complete {
      albumManager.get(id)
    }


  def postAlbumRoute: Route =
    entity(as[CreateAlbumRequest]) { albumRequest: CreateAlbumRequest =>
      complete {
        StatusCodes.Created -> albumManager.create(albumRequest)
      }
    }

  def putAlbumRoute(id: String): Route =
    entity(as[Album]) { album: Album =>
      complete {
        albumManager.update(album)
      }
    }

  def getAlbumSongs(id: String): Route =
    complete {
      albumManager.getAlbumSongs(id)
    }
}
