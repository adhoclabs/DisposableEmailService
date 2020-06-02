package co.adhoclabs.template.data

import co.adhoclabs.template.models.Song
import java.util.UUID

class SongDaoTest extends DataTestBase {
  val songDao: SongDao = new SongDaoImpl

  describe("get") {
//    it("should return a song with the supplied id") {
//      val albumId = UUID.randomUUID
//      val expectedSong: Song = Song(
//        id = "song-id-123",
//        title = "Sunshine of Your Love",
//        album = albumId,
//        albumPosition = 1
//      )
//
//      songDao.get(expectedSong.id) flatMap {
//        case Some(song: Song) =>
//          assert(!song.id.isEmpty)
//          assert(song.album == expectedSong.album)
//          assert(song.title == expectedSong.title)
//          assert(song.albumPosition == expectedSong.albumPosition)
//        case None => fail
//      }
//    }
  }

}
