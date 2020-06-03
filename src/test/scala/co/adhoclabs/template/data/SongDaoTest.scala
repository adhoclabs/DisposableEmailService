//package co.adhoclabs.template.data
//
//import co.adhoclabs.template.models.{Album, CreateAlbumRequest, Song}
//import java.util.UUID
//import org.scalatest.FutureOutcome
//import scala.concurrent.Await
//import scala.concurrent.duration._
//
//class SongDaoTest extends DataTestBase {
//  val songDao: SongDao = new SongDaoImpl
//  val albumDao: AlbumDao = new AlbumDaoImpl
//
//  override def withFixture(test: NoArgAsyncTest): FutureOutcome = {
//    // setup
//    val savedAlbum: Album = Await.result(albumDao.create(CreateAlbumRequest("album-title", None)), 2.second)
//
//    complete {
//      super.withFixture(test)
//    } lastly {
//      Await.result(albumDao.delete(savedAlbum.id), 2.second)
//    }
//  }
//
//  describe("get") {
//    it("should return a song with the supplied id") {
//      val albumId = UUID.randomUUID
//
//      val expectedSong: Song = Song(
//        id = UUID.randomUUID,
//        title = "Sunshine of Your Love",
//        album = albumId,
//        albumPosition = 1
//      )
//
//      songDao.get(expectedSong.id) flatMap {
//        case Some(song: Song) =>
//          assert(song.title == expectedSong.title)
//          assert(song.albumPosition == expectedSong.albumPosition)
//        case None => fail
//      }
//    }
//  }
//
//}
