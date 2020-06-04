package co.adhoclabs.template.data

import co.adhoclabs.template.models.{Album, AlbumWithSongs, CreateAlbumRequest, Song}
import co.adhoclabs.template.models.Genre._
import java.util.UUID

import org.scalatest.FutureOutcome

import scala.concurrent.Await
import scala.concurrent.duration._

class AlbumDaoTest extends DataTestBase {
  val albumDao: AlbumDao = new AlbumDaoImpl

//  val existingAlbum = Album(
//    id = UUID.randomUUID,
//    title = "halo 3 soundtrack",
//    genre = Some(Classical)
//  )

  override def withFixture(test: NoArgAsyncTest): FutureOutcome = {

    complete {
      super.withFixture(test)
    } lastly {
    }
  }

  describe("create, get, delete") {
    it("should save and return an album") {
      val album = Album(
        id = UUID.randomUUID,
        title = "Remain in Light",
        //genre = Some(Rock)
      )
      val song = Song(
        id = UUID.randomUUID,
        title = "Forever Darkness",
        albumId = album.id,
        albumPosition = 1
      )

      albumDao.create(AlbumWithSongs(album, List(song))) flatMap { albumFromCreate: AlbumWithSongs =>
        assert(albumFromCreate.album.title == album.title)
        assert(albumFromCreate.album.genre == album.genre)

        albumDao.get(albumFromCreate.album.id) flatMap {
          case Some(albumFromGet: Album) =>
            assert(albumFromGet.title == albumFromCreate.album.title)
            assert(albumFromGet.genre == albumFromCreate.album.genre)


          case None => fail
        }
      }
    }
  }

}
