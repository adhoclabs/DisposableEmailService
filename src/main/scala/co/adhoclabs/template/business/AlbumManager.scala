package co.adhoclabs.template.business

import java.time.{Clock, Instant}

import co.adhoclabs.analytics.AnalyticsManager
import co.adhoclabs.template.analytics.AlbumCreatedAnalyticsEvent
import co.adhoclabs.template.data.AlbumDao
import co.adhoclabs.template.exceptions.{InvalidPatchException, NoSongsInAlbumException}
import co.adhoclabs.template.models.{Album, AlbumWithSongs, CreateAlbumRequest, PatchAlbumRequest, Song}
import java.util.UUID

import co.adhoclabs.template.models.Genre.Genre
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}

trait AlbumManager extends BusinessBase {
  def getWithSongs(id: UUID): Future[Option[AlbumWithSongs]]
  def create(createRequest: CreateAlbumRequest): Future[AlbumWithSongs]
  def patch(id: UUID, patchRequest: PatchAlbumRequest): Future[Option[Album]]
  def delete(id: UUID): Future[Unit]
}

class AlbumManagerImpl(implicit albumDao: AlbumDao, analyticsManager: AnalyticsManager, clock: Clock, executionContext: ExecutionContext) extends AlbumManager {
  override protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  override def getWithSongs(id: UUID): Future[Option[AlbumWithSongs]] = albumDao.getWithSongs(id)

  // Using this slightly contrived logic where an album must come with songs
  // so we can demonstrate how to insert multiple rows in a transaction
  // and how to perform a join in Slick
  override def create(createAlbumRequest: CreateAlbumRequest): Future[AlbumWithSongs] = {
    // This is here just to demonstrate exception handling and logging
    // You can trigger this exception to be thrown by attempting a POST request with an album with no songs
    if (createAlbumRequest.songs.isEmpty) return Future.failed(NoSongsInAlbumException(createAlbumRequest))

    val now: Instant = clock.instant()
    val album = Album(
      id        = UUID.randomUUID,
      title     = createAlbumRequest.title,
      artists   = createAlbumRequest.artists,
      genre     = createAlbumRequest.genre,
      createdAt = now,
      updatedAt = now
    )
    val albumToCreate = AlbumWithSongs(
      album = album,
      songs = createAlbumRequest.songs
        .zipWithIndex
        .map {
          case (title, index) => Song(
            id            = UUID.randomUUID,
            title         = title,
            albumId       = album.id,
            albumPosition = index + 1,
            createdAt     = now,
            updatedAt     = now
          )
        }
    )

    albumDao.create(albumToCreate) map { albumWithSongs =>
      analyticsManager.trackEvent(AlbumCreatedAnalyticsEvent(albumWithSongs.album))
      albumWithSongs
    }
  }

  override def patch(id: UUID, patchRequest: PatchAlbumRequest): Future[Option[Album]] = {
    if (patchRequest.title.isEmpty && patchRequest.genre.isEmpty) {
      Future.failed(InvalidPatchException("Requested album patch does not modify any fields."))
    } else {
      albumDao.get(id) flatMap {
        case Some(albumWithSongs) =>
          val patchedAlbum = albumWithSongs.album.patch(patchRequest)
          albumDao.update(patchedAlbum)
        case None =>
          Future.successful(None)
      }
    }
  }

  override def delete(id: UUID): Future[Unit] = albumDao.delete(id).map(_ => ())

  implicit class AlbumPatches(val album: Album) {
    def patch(patchRequest: PatchAlbumRequest): Album = {
      album
        .patchTitle(patchRequest.title)
        .patchGenre(patchRequest.genre)
        .patchArtists(patchRequest.artists)
    }

    def patchTitle(titleO: Option[String]): Album = {
      titleO match {
        case Some(title) => album.copy(title = title)
        case None        => album
      }
    }

    def patchGenre(genreO: Option[Genre]): Album = {
      genreO match {
        case Some(genre) => album.copy(genre = genre)
        case None        => album
      }
    }

    def patchArtists(artistsO: Option[List[String]]): Album = {
      artistsO match {
        case Some(artists) => album.copy(artists = artists)
        case None          => album
      }
    }
  }
}
