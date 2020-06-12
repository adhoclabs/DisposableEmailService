package co.adhoclabs.template.business

import co.adhoclabs.analytics.AnalyticsManager
import co.adhoclabs.template.analytics.AlbumCreatedAnalyticsEvent
import co.adhoclabs.template.data.AlbumDao
import co.adhoclabs.template.exceptions.NoSongsInAlbumException
import co.adhoclabs.template.models.{Album, AlbumWithSongs, CreateAlbumRequest}
import java.util.UUID

import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}

trait AlbumManager extends BusinessBase {
  def get(id: UUID): Future[Option[AlbumWithSongs]]
  def create(createAlbumRequest: CreateAlbumRequest): Future[AlbumWithSongs]
  def update(album: Album): Future[Option[Album]]
  def delete(id: UUID): Future[Unit]
}

class AlbumManagerImpl (implicit albumDao: AlbumDao, executionContext: ExecutionContext, analyticsManager: AnalyticsManager) extends AlbumManager {
  override protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  // Using this slightly contrived logic where an album must come with songs
  // so we can demonstrate how to insert multiple rows in a transaction
  // and how to perform a join in Slick
  override def get(id: UUID): Future[Option[AlbumWithSongs]] = albumDao.get(id)

  override def create(createAlbumRequest: CreateAlbumRequest): Future[AlbumWithSongs] = {
    val albumWithSongs: AlbumWithSongs = AlbumWithSongs(createAlbumRequest)

    // This is here just to demonstrate exception handling and logging
    // You can trigger this exception to be thrown by attempting a POST request with an album with no songs
    if (createAlbumRequest.songs.isEmpty) return Future.failed(NoSongsInAlbumException(albumWithSongs.album))

    albumDao.create(AlbumWithSongs(createAlbumRequest)) map { albumWithSongs =>
      analyticsManager.trackEvent(AlbumCreatedAnalyticsEvent(albumWithSongs.album))
      albumWithSongs
    }
  }

  override def update(album: Album): Future[Option[Album]] = albumDao.update(album)

  override def delete(id: UUID): Future[Unit] = albumDao.delete(id).map(_ => ())
}
