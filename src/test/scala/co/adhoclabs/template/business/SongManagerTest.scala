package co.adhoclabs.template.business

import co.adhoclabs.template.data.SongDao
import co.adhoclabs.template.exceptions.SongAlreadyExistsException
import co.adhoclabs.template.models.{CreateSongRequest, Song}

import scala.concurrent.Future

class SongManagerTest extends BusinessTestBase {
  implicit val songDao: SongDao = mock[SongDao]
  val songManager: SongManager = new SongManagerImpl

  val expectedSong: Song = generateSong(generateAlbum().id, 1)

  val createSongRequest = CreateSongRequest(
    title         = expectedSong.title,
    albumId       = expectedSong.albumId,
    albumPosition = 1
  )

  describe("get") {
    it("should return a song with the supplied id when it exists") {
      (songDao.get _)
        .expects(expectedSong.id)
        .returning(Future.successful(Some(expectedSong)))

      songManager.get(expectedSong.id) flatMap {
        case Some(song: Song) => assert(song == song)
        case None             => fail
      }
    }

    it("should return None when the song doesn't exist") {
      (songDao.get _)
        .expects(expectedSong.id)
        .returning(Future.successful(None))

      songManager.get(expectedSong.id) flatMap {
        case None          => succeed
        case Some(_: Song) => fail
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

      songManager.create(createSongRequest) map { createdSong: Song =>
        assert(createdSong == expectedSong)
      }
    }

    it("should throw an exception from SongDao.create when attempting to create a song with an ID that already exists") {
      (songDao.create _)
        .expects(*)
        .returning(Future.failed(SongAlreadyExistsException("song already exists")))

      recoverToSucceededIf[SongAlreadyExistsException] {
        songManager.create(createSongRequest)
      }
    }
  }

  describe("update") {
    it("should call SongDao.update and return updated song if it already exists") {
      (songDao.update _)
        .expects(expectedSong)
        .returning(Future.successful(Some(expectedSong)))

      songManager.update(expectedSong) map {
        case Some(updatedSong: Song) => assert(updatedSong == expectedSong)
        case None                    => fail
      }
    }

    it("should call songDao.update and return None if the song doesn't exist") {
      (songDao.update _)
        .expects(*)
        .returning(Future.successful(None))

      songManager.update(expectedSong) map {
        case None          => succeed
        case Some(_: Song) => fail
      }
    }
  }

  describe("delete") {
    it("should return unit if song was deleted") {
      (songDao.delete _)
        .expects(expectedSong.id)
        .returning(Future.successful(0))

      songManager.delete(expectedSong.id) map { _ =>
        succeed
      }
    }
  }
}
