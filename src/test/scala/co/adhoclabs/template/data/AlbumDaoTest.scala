package co.adhoclabs.template.data

import java.util.UUID

import co.adhoclabs.template.exceptions.AlbumAlreadyExistsException
import co.adhoclabs.template.models.Genre._
import co.adhoclabs.template.models.{Album, AlbumWithSongs}
import org.postgresql.util.PSQLException
import org.scalatest.FutureOutcome

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class AlbumDaoTest extends DataTestBase {
  // Since all songs need an album and songs cascade delete,
  // just need to delete any albums created to clean up the whole test
  val albumIdsToCleanUp = new java.util.concurrent.ConcurrentHashMap[UUID, String]()
  override def withFixture(test: NoArgAsyncTest): FutureOutcome = {
    complete {
      super.withFixture(test)
    } lastly {
      val cleanupFutures = albumIdsToCleanUp.asScala.toList.map {
        case (albumId, _) => albumDao.delete(albumId)
      }
      Await.result(Future.sequence(cleanupFutures), 5.second)
    }
  }

  describe("AlbumDao") {
    describe("create, get, update, delete") {
      it("should correctly execute the lifecycle of an album") {
        val expectedAlbumWithSongs = generateAlbumWithSongs()
        albumIdsToCleanUp.putIfAbsent(expectedAlbumWithSongs.album.id, "")

        albumDao.create(AlbumWithSongs(expectedAlbumWithSongs.album, expectedAlbumWithSongs.songs)) flatMap { createdAlbumWithSongs: AlbumWithSongs =>
          assert(createdAlbumWithSongs == expectedAlbumWithSongs)

          albumDao.get(expectedAlbumWithSongs.album.id) flatMap {
            case Some(gottenAlbumWithSongs) =>
              assert(gottenAlbumWithSongs == expectedAlbumWithSongs)

              val expectedUpdatedAlbum = expectedAlbumWithSongs.album.copy(title = "updated title", genre = Rock)
              albumDao.update(expectedUpdatedAlbum) flatMap {
                case Some(updatedAlbum) =>
                  assert(updatedAlbum == expectedUpdatedAlbum)

                  albumDao.delete(expectedAlbumWithSongs.album.id) flatMap { count =>
                    assert(count == 1)

                    albumDao.get(expectedAlbumWithSongs.album.id) map { a =>
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

    describe("create") {
      it("should correctly create with no songs when given an empty album") {
        val expectedAlbumWithSongs = generateAlbumWithSongs(songCount = 0)
        albumIdsToCleanUp.putIfAbsent(expectedAlbumWithSongs.album.id, "")

        albumDao.create(AlbumWithSongs(expectedAlbumWithSongs.album, expectedAlbumWithSongs.songs)) flatMap { createdAlbumWithSongs: AlbumWithSongs =>
          assert(createdAlbumWithSongs == expectedAlbumWithSongs)

          albumDao.get(expectedAlbumWithSongs.album.id) flatMap {
            case Some(gottenAlbumWithSongs) =>
              assert(gottenAlbumWithSongs == expectedAlbumWithSongs)

              albumDao.delete(expectedAlbumWithSongs.album.id) flatMap { count =>
                assert(count == 1)

                albumDao.get(expectedAlbumWithSongs.album.id) map { a =>
                  assert(a.isEmpty)
                }
              }
            case None => fail
          }
        }
      }

      it("should throw a validation exception when the primary key already exists") {
        val existingAlbumWithSongs = generateAlbumWithSongs()
        albumIdsToCleanUp.putIfAbsent(existingAlbumWithSongs.album.id, "")

        albumDao.create(existingAlbumWithSongs) flatMap { _ =>
          recoverToSucceededIf[AlbumAlreadyExistsException] {
            albumDao.create(existingAlbumWithSongs)
          }
        }
      }

      it("should throw a unique constraint violation exception when duplicate songs are added") {
        val album = generateAlbum()
        val songs = generateSongs(album.id, 3)
        val albumWithDuplicateSongs = AlbumWithSongs(album, songs ++ songs)
        albumIdsToCleanUp.putIfAbsent(albumWithDuplicateSongs.album.id, "")

        recoverToExceptionIf[PSQLException] {
          albumDao.create(albumWithDuplicateSongs)
        } map { e =>
          assert(DaoBase.isUniqueConstraintViolation(e))
        }
      }
    }

    describe("get") {
      it("should return None for an album that doesn't exist") {
        albumDao.get(UUID.randomUUID) map { albumWithSongsO: Option[AlbumWithSongs] =>
          assert(albumWithSongsO.isEmpty)
        }
      }
    }

    describe("update") {
      it("should return None for an album that doesn't exist") {
        val nonexistentAlbum = generateAlbum()
        albumIdsToCleanUp.putIfAbsent(nonexistentAlbum.id, "")

        albumDao.update(nonexistentAlbum) map { album: Option[Album] =>
          assert(album.isEmpty)
        }
      }
    }

    describe("delete") {
      it("should return 0 if the album doesn't exist when we attempt to delete it") {
        albumDao.delete(UUID.randomUUID) map { rowsAffected: Int =>
          assert(rowsAffected == 0)
        }
      }
    }
  }
}
