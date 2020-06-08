package co.adhoclabs.template.data

import co.adhoclabs.template.models.AlbumWithSongs
import co.adhoclabs.template.models.Genre._

class AlbumDaoTest extends DataTestBase {
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

                  albumDao.get(expectedAlbumWithSongs.album.id) flatMap { a =>
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

              albumDao.get(expectedAlbumWithSongs.album.id) flatMap { a =>
                assert(a.isEmpty)
              }
            }
          case None => fail
        }
      }
    }
  }
}
