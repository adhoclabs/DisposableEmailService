package co.adhoclabs.template.api

import akka.http.scaladsl.testkit.ScalatestRouteTest
import co.adhoclabs.template.TestBase
import co.adhoclabs.template.business.{AlbumManager, SongManager}
import co.adhoclabs.template.models.JsonSupport

abstract class ApiTestBase extends TestBase with Api with ScalatestRouteTest with JsonSupport {
  implicit val songManager: SongManager = mock[SongManager]
  implicit val albumManager: AlbumManager = mock[AlbumManager]
}
