package co.adhoclabs.template.api

import akka.http.scaladsl.testkit.ScalatestRouteTest
import co.adhoclabs.template.business.{AlbumManager, SongManager}
import co.adhoclabs.template.models.JsonSupport
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.funspec.AsyncFunSpec

abstract class ApiTestBase extends AsyncFunSpec with Api with AsyncMockFactory with ScalatestRouteTest with JsonSupport {
  implicit val songManager: SongManager = mock[SongManager]
  implicit val albumManager: AlbumManager = mock[AlbumManager]
}
