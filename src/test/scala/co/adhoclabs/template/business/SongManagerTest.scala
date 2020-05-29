package co.adhoclabs.template.business

import co.adhoclabs.template.data.SongDao
import co.adhoclabs.template.models.Song
import scala.concurrent.Future

class SongManagerTest extends BusinessTestBase {
  implicit val songDao: SongDao = mock[SongDao]
  val songManager: SongManager = new SongManagerImpl

  describe("get") {
    it("should return a song with the supplied id") {
      val expectedSong: Song = Song(
        id = Some("song-id-123"),
        title = "Sunshine of Your Love"
      )
      (songDao.get _)
        .expects(expectedSong.id.get)
        .returning(Future.successful(Some(expectedSong)))

      songManager.get(expectedSong.id.get) flatMap {
        case Some(song: Song) => assert(song == expectedSong)
        case None => fail
      }
    }
  }

}
