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
        id = "song-id-123",
        title = "Sunshine of Your Love",
        album = "album-123",
        albumPosition = 1
      )
      (songDao.get _)
        .expects(expectedSong.id)
        .returning(Future.successful(Some(expectedSong)))

      songManager.get(expectedSong.id) flatMap {
        case Some(song: Song) => assert(song == expectedSong)
        case None => fail
      }
    }
  }

}
