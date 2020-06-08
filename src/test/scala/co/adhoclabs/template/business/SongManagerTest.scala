package co.adhoclabs.template.business

import co.adhoclabs.template.data.SongDao
import co.adhoclabs.template.models.{CreateSongRequest, Genre, Song}
import java.util.UUID
import scala.concurrent.Future

class SongManagerTest extends BusinessTestBase {
  implicit val songDao: SongDao = mock[SongDao]
  val songManager: SongManager = new SongManagerImpl

  val albumId = UUID.randomUUID
  val songId = UUID.randomUUID

  val expectedSong: Song = Song(
    id = songId,
    title = "Sunshine of Your Love",
    albumId = albumId,
    albumPosition = 1
  )

  val createSongRequest = CreateSongRequest(
    title = expectedSong.title,
    albumId = expectedSong.albumId,
    albumPosition = 1
  )

  describe("get") {
    it("should return a song with the supplied id") {
      (songDao.get _)
        .expects(expectedSong.id)
        .returning(Future.successful(Some(expectedSong)))

      songManager.get(expectedSong.id) flatMap {
        case Some(song: Song) => assert(song == song)
        case None => fail
      }
    }
  }

  describe("create") {
    it("should call SongDao.create and return a newly saved song") {
      (songDao.create _)
          .expects(where { song: Song =>
            // Since the song id is generated in the create method, we want to match on everything else
            song.title == expectedSong.title && song.albumId == expectedSong.albumId && song.albumPosition == expectedSong.albumPosition
          })
          .returning(Future.successful(expectedSong))

      songManager.create(createSongRequest) flatMap { createdSong: Song =>
        assert(createdSong == expectedSong)

      }
    }
  }

  describe("update") {
    it("should call SongDao.update and return updated song if it already exists") {
      (songDao.update _)
          .expects(expectedSong)
          .returning(Future.successful(Some(expectedSong)))

      songManager.update(expectedSong) flatMap {
        case Some(updatedSong: Song) => assert(updatedSong == expectedSong)
        case None => fail
      }
    }

//    it("should call SongDao.update and return error if song does not exist") {
//
//    }
  }

}
