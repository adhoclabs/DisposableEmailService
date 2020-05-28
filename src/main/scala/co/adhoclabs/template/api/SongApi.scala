package co.adhoclabs.template.api

import akka.http.scaladsl.server.PathMatchers.Segment
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import co.adhoclabs.template.business.SongManager

trait SongApi extends BaseApi {

  implicit def songManager: SongManager

  val songRoutes: Route = path("song" / Segment) { id: String =>
    get {
      complete {
        songManager.get(id)
      }
    }
  }
}
