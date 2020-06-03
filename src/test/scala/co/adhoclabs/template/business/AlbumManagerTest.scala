package co.adhoclabs.template.business

import co.adhoclabs.template.data.AlbumDao
import co.adhoclabs.template.models.{Album, CreateAlbumRequest}
import co.adhoclabs.template.models.Genre._
import java.util.UUID
import scala.concurrent.Future

class AlbumManagerTest extends BusinessTestBase {
  implicit val albumDao: AlbumDao = mock[AlbumDao]
  val albumManager: AlbumManager = new AlbumManagerImpl

  describe("get") {
    it("should return a album with the supplied id") {
      val albumId = UUID.randomUUID
      val expectedAlbum: Album = Album(
        id = albumId,
        title = "Disraeli Gears",
        genre = Some(Rock)
      )
      (albumDao.get _)
        .expects(expectedAlbum.id)
        .returning(Future.successful(Some(expectedAlbum)))

      albumManager.get(expectedAlbum.id) flatMap {
        case Some(album: Album) => assert(album == expectedAlbum)
        case None => fail
      }
    }
  }

  describe("create") {
    val albumId = UUID.randomUUID
    val expectedAlbum: Album = Album(
      id = albumId,
      title = "Halo Reach Soundtrack",
      genre = Some(Classical)
    )

    val createAlbumRequest = CreateAlbumRequest(
      title = expectedAlbum.title,
      genre = expectedAlbum.genre
    )

    it("should call AlbumDao.create and return an album when successful"){
      (albumDao.create _)
          .expects(*)
          .returning(Future.successful(expectedAlbum))

      albumManager.create(createAlbumRequest) flatMap { album: Album =>
        assert(album == expectedAlbum)
      }
    }

//    it("should call AlbumDao.create and throw an exception when album is not created") {
//      (albumDao.create _)
//          .expects(createAlbumRequest)
//          .returning(Future.successful(None))
//
//      val f: Future[Album] = albumManager.create(createAlbumRequest)
//
//      recoverToExceptionIf(f) { ex =>
//        assert()
//      }
//    }
//  }
//
//  describe("update") {
//    it("should"){
//
//    }
//  }
//
//  describe("getAlbumSongs") {
//    it("should"){
//
//    }
  }

}
