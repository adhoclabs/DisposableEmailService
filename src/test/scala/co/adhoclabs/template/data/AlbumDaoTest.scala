package co.adhoclabs.template.data

import co.adhoclabs.template.models.Album
import co.adhoclabs.template.models.Genre._
import java.util.UUID

class AlbumDaoTest extends DataTestBase {
  val albumDao: AlbumDao = new AlbumDaoImpl

//  describe("get") {
//    it("should return a album with the supplied id") {
//      val albumId = UUID.randomUUID
//      val expectedAlbum: Album = Album(
//        id = albumId,
//        title = "Remain in Light",
//        genre = Some(Rock)
//      )
//
//      albumDao.get(expectedAlbum.id) flatMap {
//        case Some(album: Album) => assert(album == expectedAlbum)
//        case None => fail
//      }
//    }
//  }

}
