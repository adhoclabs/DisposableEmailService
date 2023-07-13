package co.adhoclabs.template.business

import co.adhoclabs.template.data.AlbumDao
import co.adhoclabs.template.exceptions.{AlbumAlreadyExistsException, InvalidPatchException, NoSongsInAlbumException}
import co.adhoclabs.template.models._

import scala.concurrent.Future

class AlbumManagerTest extends BusinessTestBase {
  implicit val albumDao: AlbumDao = mock[AlbumDao]
  implicit val sqsManager: SqsManager = mock[SqsManager]
  val albumManager: AlbumManager = new AlbumManagerImpl

  val expectedAlbumWithSongs = generateAlbumWithSongs()

  describe("getWithSongs") {
    it("should return an album with the supplied id") {
      (albumDao.getWithSongs _)
        .expects(expectedAlbumWithSongs.album.id)
        .returning(Future.successful(Some(expectedAlbumWithSongs)))

      albumManager.getWithSongs(expectedAlbumWithSongs.album.id) map {
        case Some(albumWithSongs) => assert(albumWithSongs == expectedAlbumWithSongs)
        case None                 => fail
      }
    }

    it("should return None when the album doesn't exist") {
      (albumDao.getWithSongs _)
        .expects(expectedAlbumWithSongs.album.id)
        .returning(Future.successful(None))

      albumManager.getWithSongs(expectedAlbumWithSongs.album.id) flatMap {
        case None    => succeed
        case Some(_) => fail
      }
    }
  }

  describe("create") {
    val createAlbumRequest = CreateAlbumRequest(
      title   = expectedAlbumWithSongs.album.title,
      artists = expectedAlbumWithSongs.album.artists,
      genre   = expectedAlbumWithSongs.album.genre,
      songs   = expectedAlbumWithSongs.songs.map(_.title)
    )

    it("should call AlbumDao.create and return an album when successful") {
      (albumDao.create _)
        .expects(*)
        .returning(Future.successful(expectedAlbumWithSongs))

      (sqsManager.sendFakeSqsEvent _)
        .expects("fakeSqsPayload")
        .returning(Future.successful())

      albumManager.create(createAlbumRequest) map { albumWithSongs: AlbumWithSongs =>
        assert(albumWithSongs == expectedAlbumWithSongs)
      }
    }

    it("should call AlbumDao.create and throw an exception for an album with no songs") {
      recoverToSucceededIf[NoSongsInAlbumException] {
        albumManager.create(createAlbumRequest.copy(songs = List.empty[String]))
      }
    }

    it("should throw an exception from AlbumDao.create when attempting to create an album with an ID that already exists") {
      (albumDao.create _)
        .expects(*)
        .returning(Future.failed(AlbumAlreadyExistsException("album already exists")))

      recoverToSucceededIf[AlbumAlreadyExistsException] {
        albumManager.create(createAlbumRequest)
      }
    }
  }

  describe("patch") {
    val expectedUpdatedAlbum = expectedAlbumWithSongs.album.copy(
      title = "Updated Title",
      genre = Genre.HipHop
    )
    val patchRequest = PatchAlbumRequest(
      title = Some(expectedUpdatedAlbum.title),
      genre = Some(expectedUpdatedAlbum.genre)
    )

    it("should call AlbumDao.update and return updated album if it already exists") {
      (albumDao.get _)
        .expects(expectedAlbumWithSongs.album.id)
        .returning(Future.successful(Some(expectedAlbumWithSongs.album)))
      (albumDao.update _)
        .expects(expectedUpdatedAlbum)
        .returning(Future.successful(Some(expectedUpdatedAlbum)))

      albumManager.patch(expectedAlbumWithSongs.album.id, patchRequest) map {
        case Some(updatedAlbum: Album) => assert(updatedAlbum == expectedUpdatedAlbum)
        case None                      => fail
      }
    }

    it("should call albumDao.update and return None if the album doesn't exist") {
      (albumDao.get _)
        .expects(expectedAlbumWithSongs.album.id)
        .returning(Future.successful(None))

      albumManager.patch(expectedAlbumWithSongs.album.id, patchRequest) map {
        case None           => succeed
        case Some(_: Album) => fail
      }
    }

    it("should throw InvalidPatchException if patch is empty") {
      val emptyPatchRequest = PatchAlbumRequest()

      recoverToSucceededIf[InvalidPatchException] {
        albumManager.patch(expectedAlbumWithSongs.album.id, emptyPatchRequest)
      }
    }
  }

  describe("delete") {
    val expectedAlbumWithSongs = generateAlbumWithSongs()

    it("should return Unit if the album was deleted") {
      (albumDao.delete _)
        .expects(expectedAlbumWithSongs.album.id)
        .returning(Future.successful(1))

      albumManager.delete(expectedAlbumWithSongs.album.id) map { _ =>
        succeed
      }
    }

    it("should return Unit if the album doesn't exist") {
      (albumDao.delete _)
        .expects(expectedAlbumWithSongs.album.id)
        .returning(Future.successful(0))

      albumManager.delete(expectedAlbumWithSongs.album.id) map { _ =>
        succeed
      }
    }
  }
}
