package co.adhoclabs.template.data

import co.adhoclabs.template.models.Song

class SongDaoTest extends DataTestBase {
  val songDao: SongDao = new SongDaoImpl

  describe("get") {
    it("should return a song with the supplied id") {
      val expectedSong: Song = Song(
        id = Some("song-id-123"),
        title = "Sunshine of Your Love"
      )

      songDao.get(expectedSong.id.get) flatMap {
        case Some(song: Song) => assert(song == expectedSong)
        case None => fail
      }
    }
  }

}
