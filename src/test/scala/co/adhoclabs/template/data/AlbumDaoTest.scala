package co.adhoclabs.template.data

import co.adhoclabs.template.exceptions.AlbumAlreadyExistsException
import co.adhoclabs.template.models.{Album, AlbumWithSongs}
import co.adhoclabs.template.models.Genre._
import java.util.UUID

class AlbumDaoTest extends DataTestBase {
  describe("AlbumDao") {
    describe("create, get, update, delete") {
      it("should correctly execute the lifecycle of an album") {
        val expectedAlbumWithSongs = generateAlbumWithSongs()

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

    describe("creating an empty album") {
      it("should correctly create with no songs") {
        val expectedAlbumWithSongs = generateAlbumWithSongs(songCount = 0)

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
        albumDao.update(generateAlbum()) map { album: Option[Album] =>
          assert(album.isEmpty)
        }
      }
    }

    describe("create") {
      it("should throw a validation exception when the primary key already exists") {
        val existingAlbumWithSongs = generateAlbumWithSongs()
        albumDao.create(existingAlbumWithSongs) flatMap { _ =>
          recoverToSucceededIf[AlbumAlreadyExistsException] {
            albumDao.create(existingAlbumWithSongs)
          }
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
