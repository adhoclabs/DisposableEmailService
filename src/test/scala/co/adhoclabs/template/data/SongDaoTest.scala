package co.adhoclabs.template.data

import co.adhoclabs.template.exceptions.SongAlreadyExistsException
import co.adhoclabs.template.models.{AlbumWithSongs, Song}
import java.util.UUID
import org.scalatest.FutureOutcome
import scala.concurrent.Await
import scala.concurrent.duration._

class SongDaoTest extends DataTestBase {

  val existingAlbum = generateAlbum()
  val existingAlbum2 = generateAlbum()

  override def withFixture(test: NoArgAsyncTest): FutureOutcome = {
    val createF1 = albumDao.create(AlbumWithSongs(existingAlbum, List.empty[Song]))
    val createF2 = albumDao.create(AlbumWithSongs(existingAlbum2, List.empty[Song]))
    Await.result(
      for {
        _ <- createF1
        _ <- createF2
      } yield (),
      2.second)

    complete {
      super.withFixture(test)
    } lastly {
      // Since all songs need an album and songs cascade delete,
      // just need to delete these two to clean up the whole test
      val deleteF1 = albumDao.delete(existingAlbum.id)
      val deleteF2 = albumDao.delete(existingAlbum2.id)
      Await.result(
        for {
          _ <- deleteF1
          _ <- deleteF2
        } yield (),
        2.second)
    }
  }

  describe("create, get, update, delete") {
    it("should correctly execute the lifecycle of a song") {
      val expectedSong = generateSong(existingAlbum.id, albumPosition = 1)

      songDao.create(expectedSong) flatMap { createdSong: Song =>
        assert(createdSong == expectedSong)

        songDao.get(expectedSong.id) flatMap {
          case Some(gottenSong) =>
            assert(gottenSong == expectedSong)

            val expectedUpdatedSong = expectedSong.copy(title = "updated title", albumId = existingAlbum2.id, albumPosition = 145)
            songDao.update(expectedUpdatedSong) flatMap {
              case Some(updatedSong) =>
                assert(updatedSong == expectedUpdatedSong)

                songDao.delete(expectedSong.id) flatMap { count =>
                  assert(count == 1)

                  songDao.get(expectedSong.id) map { a =>
                    assert(a.isEmpty)
                  }
                }
              case None => fail
            }
          case None => fail
        }
      }
    }
  }

  describe("createMany") {
    it("should create multiple songs") {
      val expectedSongs: List[Song] = generateSongs(existingAlbum.id, 3) ++ generateSongs(existingAlbum2.id, 3)
      songDao.createMany(expectedSongs) map { songs: List[Song] =>
        assert(songs.sortBy(_.id) == expectedSongs.sortBy(_.id))
      }
    }
  }

  describe("get") {
    it("should return None for a song that doesn't exist") {
      songDao.get(UUID.randomUUID) map { songO: Option[Song] =>
        assert(songO.isEmpty)
      }
    }
  }

  describe("update") {
    it("should return None for a song that doesn't exist") {
      songDao.update(generateSong(existingAlbum.id, 1)) map { songO: Option[Song] =>
        assert(songO.isEmpty)
      }
    }
  }

  describe("create") {
    it("should throw a validation exception when the primary key already exists") {
      val existingSong = generateSong(existingAlbum.id, 1)
      songDao.create(existingSong) flatMap { _ =>
        recoverToSucceededIf[SongAlreadyExistsException] {
          songDao.create(existingSong)
        }
      }
    }
  }

  describe("delete") {
    it("should return 0 if the song doesn't exist when we attempt to delete it") {
      songDao.delete(UUID.randomUUID) map { rowsAffected: Int =>
        assert(rowsAffected == 0)
      }
    }
  }

  describe("createMany") {
    it("should throw a validation exception when the primary key for one of the songs already exists") {
      val existingSong = generateSong(existingAlbum.id, 1)
      songDao.create(existingSong) flatMap { _ =>
        val expectedSongs: List[Song] = generateSongs(existingAlbum.id, 3) ++ List(existingSong)
        recoverToSucceededIf[SongAlreadyExistsException] {
          songDao.createMany(expectedSongs)
        }
      }
    }
  }
}
