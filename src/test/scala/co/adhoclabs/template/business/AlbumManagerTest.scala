package co.adhoclabs.template.business

import co.adhoclabs.analytics.AnalyticsManager
import co.adhoclabs.template.analytics.events.AlbumCreatedAnalyticsEvent
import co.adhoclabs.template.data.AlbumDao
import co.adhoclabs.template.models.{AlbumWithSongs, CreateAlbumRequest, CreateSongRequest}
import scala.concurrent.Future

class AlbumManagerTest extends BusinessTestBase {
  implicit val albumDao: AlbumDao = mock[AlbumDao]
  implicit val analyticsManager: AnalyticsManager = mock[AnalyticsManager]

  val albumManager: AlbumManager = new AlbumManagerImpl

  describe("get") {
    it("should return a album with the supplied id") {
      val expectedAlbumWithSongs = generateAlbumWithSongs()
      (albumDao.get _)
        .expects(expectedAlbumWithSongs.album.id)
        .returning(Future.successful(Some(expectedAlbumWithSongs)))

      albumManager.get(expectedAlbumWithSongs.album.id) flatMap {
        case Some(albumWithSongs) => assert(albumWithSongs == expectedAlbumWithSongs)
        case None => fail
      }
    }
  }

  describe("create") {
    val expectedAlbumWithSongs = generateAlbumWithSongs()

    val createAlbumRequest = CreateAlbumRequest(
      title = expectedAlbumWithSongs.album.title,
      genre = expectedAlbumWithSongs.album.genre,
      songs = expectedAlbumWithSongs.songs.map(song => CreateSongRequest(
        title = song.title,
        albumId = song.albumId,
        albumPosition = song.albumPosition
      )),
    )

    it("should call AlbumDao.create and return an album when successful"){
      (albumDao.create _)
          .expects(*)
          .returning(Future.successful(expectedAlbumWithSongs))

      (analyticsManager.trackEvent _)
        .expects(AlbumCreatedAnalyticsEvent(expectedAlbumWithSongs.album.id))
        .returning(Future.successful())

      albumManager.create(createAlbumRequest) flatMap { albumWithSongs: AlbumWithSongs =>
        assert(albumWithSongs == expectedAlbumWithSongs)
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
