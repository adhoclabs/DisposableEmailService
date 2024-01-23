package co.adhoclabs.template.api

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import co.adhoclabs.template.TestBase
import co.adhoclabs.template.apiz.{AlbumRoutes, ApiZ, HealthRoutes, SongRoutes}
import co.adhoclabs.template.business.{AlbumManager, HealthManager, SongManager}

abstract class ApiTestBase extends TestBase with ScalatestRouteTest with ZioHttpTestHelpers {

  implicit val healthManager: HealthManager = mock[HealthManager]
  implicit val songManager: SongManager = mock[SongManager]
  implicit val albumManager: AlbumManager = mock[AlbumManager]

  val api: Api = new ApiImpl
  val routes: Route = api.routes

  // ZIO-http bits
  implicit val albumbRoutes = AlbumRoutes()
  implicit val songRoutes = SongRoutes()
  implicit val healthRoutes = HealthRoutes()

  val zioRoutes = ApiZ().zioRoutes
  val app = zioRoutes.toHttpApp

}
