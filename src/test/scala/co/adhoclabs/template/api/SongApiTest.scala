package co.adhoclabs.template.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import co.adhoclabs.template.models.Song
import scala.concurrent.Future

class SongApiTest extends ApiTestBase with Api {

  describe("GET /songs/:id") {
    it("should call SongManager.get") {
      val expectedSong: Song = Song(
        id = Some("song-id-123"),
        title = "Who Let the Dogs Out?"
      )

      (songManager.get _)
        .expects(expectedSong.id.get)
        .returning(Future.successful(Some(expectedSong)))

      Get(s"/songs/${expectedSong.id.get}") ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.OK)
        assert(responseAs[Song] == expectedSong)
      }
    }
  }

}
