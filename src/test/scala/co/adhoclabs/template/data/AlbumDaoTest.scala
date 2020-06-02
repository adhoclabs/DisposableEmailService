package co.adhoclabs.template.data

import co.adhoclabs.template.models.Album
import co.adhoclabs.template.models.Genre._

class AlbumDaoTest extends DataTestBase {
  val albumDao: AlbumDao = new AlbumDaoImpl

  describe("get") {
    it("should return a album with the supplied id") {
      val expectedAlbum: Album = Album(
        id = "album-id-123",
        title = "Remain in Light",
        genre = Some(Rock)
      )

      albumDao.get(expectedAlbum.id) flatMap {
        case Some(album: Album) => assert(album == expectedAlbum)
        case None => fail
      }
    }
  }

}
