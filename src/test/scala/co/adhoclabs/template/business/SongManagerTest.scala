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

  val song: Song = Song(
    id = songId,
    title = "Sunshine of Your Love",
    albumId = albumId,
    albumPosition = 1
  )

  val createSongRequest = CreateSongRequest(
    title = song.title,
    albumId = song.albumId,
    albumPosition = 1
  )

  describe("get") {
    it("should return a song with the supplied id") {
      (songDao.get _)
        .expects(song.id)
        .returning(Future.successful(Some(song)))

      songManager.get(song.id) flatMap {
        case Some(song: Song) => assert(song == song)
        case None => fail
      }
    }
  }

  describe("create") {
    it("should call SongDao.create and return a newly saved song") {
      (songDao.create _)
          .expects(song)
          .returning(Future.successful(song))

      songManager.create(createSongRequest) flatMap { createdSong: Song =>
        assert(createdSong == song)

      }
    }
  }

  describe("update") {
    it("should call SongDao.update and return updated song if it already exists") {
      (songDao.update _)
          .expects(song)
          .returning(Future.successful(Some(song)))

      songManager.update(song) flatMap {
        case Some(updatedSong: Song) => assert(updatedSong == song)
        case None => fail
      }
    }

//    it("should call SongDao.update and return error if song does not exist") {
//
//    }
  }

}
