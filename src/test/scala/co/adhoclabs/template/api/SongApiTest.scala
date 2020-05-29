package co.adhoclabs.template.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import co.adhoclabs.template.business.SongManager
import co.adhoclabs.template.models.{JsonSupport, Song}
import org.scalamock.scalatest.MockFactory
import org.scalatest.funspec.AnyFunSpec
import scala.concurrent.Future

class SongApiTest extends AnyFunSpec with MockFactory with ScalatestRouteTest with JsonSupport with Api {

  val songManager: SongManager = mock[SongManager]

  describe("GET /song/:id") {
    it("should call SongManager.get") {
      val expectedSong: Song = Song(
        id = Some("song-id-123"),
        title = "Who Let the Dogs Out?"
      )

      (songManager.get _)
        .expects(expectedSong.id.get)
        .returning(Future.successful(Some(expectedSong)))

      Get(s"/song/${expectedSong.id.get}") ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.OK)
        assert(responseAs[Song] == expectedSong)
      }
    }
  }
}
