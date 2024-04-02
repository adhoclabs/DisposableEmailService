package co.adhoclabs.template.api

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import co.adhoclabs.template.TestBase
import co.adhoclabs.template.business.{AlbumManager, HealthManager, SongManager}

abstract class ApiTestBase extends TestBase with ScalatestRouteTest {

  implicit val healthManager: HealthManager = mock[HealthManager]
  implicit val songManager: SongManager = mock[SongManager]
  implicit val albumManager: AlbumManager = mock[AlbumManager]

  val api: Api = new ApiImpl
  val routes: Route = api.routes
}
