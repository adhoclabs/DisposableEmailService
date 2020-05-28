package co.adhoclabs.template.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import co.adhoclabs.template.business.{SongManager, SongManagerImpl}
import co.adhoclabs.template.models.{JsonSupport, Song}
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import scala.concurrent.Future

class SongApiTest extends AnyFlatSpec with MockFactory with ScalatestRouteTest with JsonSupport with SongApi {

  val songManager: SongManager = mock[SongManagerImpl]

  behavior of "GET /song/:id"

  it should "call SongManager.get" in {
    // given
    val expectedSong: Song = Song(
      id = Some("song-id-123"),
      title = "Who Let the Dogs Out?"
    )

    // expectations
    (songManager.get _)
        .expects(expectedSong.id.get)
        .returning(Future.successful(Some(expectedSong)))

    // when
    Get(s"/song/${expectedSong.id.get}") ~> Route.seal(songRoutes) ~> check {
      // then
      assert(status == StatusCodes.OK)
      assert(responseAs[Song] == expectedSong)
    }
  }


}
