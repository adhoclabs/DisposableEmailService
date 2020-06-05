package co.adhoclabs.template.business

import co.adhoclabs.template.data.AlbumDao
import co.adhoclabs.template.models.{Album, AlbumWithSongs, CreateAlbumRequest, Song}
import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

trait AlbumManager extends BusinessBase {
  def get(id: UUID): Future[Option[AlbumWithSongs]]
  def create(createAlbumRequest: CreateAlbumRequest): Future[AlbumWithSongs]
  def update(album: Album): Future[Option[Album]]
}

class AlbumManagerImpl (implicit albumDao: AlbumDao, executionContext: ExecutionContext) extends AlbumManager {
  // Using this slightly contrived logic where an album must come with songs
  // so we can demonstrate how to insert multiple rows in a transaction
  // and how to perform a join in Slick
  override def get(id: UUID): Future[Option[AlbumWithSongs]] = albumDao.get(id)
  override def create(createAlbumRequest: CreateAlbumRequest): Future[AlbumWithSongs] =
    albumDao.create(AlbumWithSongs(createAlbumRequest))

  override def update(album: Album): Future[Option[Album]] = albumDao.update(album)
}
