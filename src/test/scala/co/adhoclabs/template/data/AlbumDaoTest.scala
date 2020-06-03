package co.adhoclabs.template.data

import co.adhoclabs.template.models.{Album, CreateAlbumRequest}
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
        genre = Some(Rock)
      )

      albumDao.create(album) flatMap { albumFromCreate: Album =>
        assert(albumFromCreate.title == album.title)
        assert(albumFromCreate.genre == album.genre)

        albumDao.get(albumFromCreate.id) flatMap {
          case Some(albumFromGet: Album) =>
            assert(albumFromGet.title == albumFromCreate.title)
            assert(albumFromGet.genre == albumFromCreate.genre)


          case None => fail
        }
      }
    }
  }

}
