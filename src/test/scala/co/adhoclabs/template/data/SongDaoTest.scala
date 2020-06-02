package co.adhoclabs.template.data

import co.adhoclabs.template.models.Song

class SongDaoTest extends DataTestBase {
  val songDao: SongDao = new SongDaoImpl

  describe("get") {
    it("should return a song with the supplied id") {
      val expectedSong: Song = Song(
        id = "song-id-123",
        title = "Sunshine of Your Love",
        album = "album-123",
        albumPosition = 1
      )

      songDao.get(expectedSong.id) flatMap {
        case Some(song: Song) => assert(song == expectedSong)
        case None => fail
      }
    }
  }

}
