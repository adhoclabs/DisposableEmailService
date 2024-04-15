package co.adhoclabs.email.api

import co.adhoclabs.email.TestBase
import co.adhoclabs.email.api.{AlbumRoutes, ApiZ, HealthRoutes, SongRoutes}
import co.adhoclabs.email.business.{AlbumManager, HealthManager, SongManager}

abstract class ApiTestBase extends TestBase with ZioHttpTestHelpers {

  implicit val healthManager: HealthManager = mock[HealthManager]
  implicit val songManager: SongManager     = mock[SongManager]
  implicit val albumManager: AlbumManager   = mock[AlbumManager]

  // ZIO-http bits
  implicit val albumbRoutes = AlbumRoutes()
  implicit val songRoutes   = SongRoutes()
  implicit val healthRoutes = HealthRoutes()

  val zioRoutes = ApiZ().zioRoutes
  val app       = zioRoutes.toHttpApp

}
