package co.adhoclabs.template.api

import akka.http.scaladsl.server.PathMatchers.Segment
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import co.adhoclabs.template.business.SongManager

trait SongApi extends ApiBase {

  implicit val songManager: SongManager

  val songRoutes: Route = path("songs" / Segment) { id: String =>
    get {
      complete {
        songManager.get(id)
      }
    }
  }
}
