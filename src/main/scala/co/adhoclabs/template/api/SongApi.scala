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
