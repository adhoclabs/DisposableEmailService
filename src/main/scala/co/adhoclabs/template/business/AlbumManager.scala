package co.adhoclabs.template.business

import co.adhoclabs.template.data.AlbumDao
import co.adhoclabs.template.models.{Album, CreateAlbumRequest, Song}
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

trait AlbumManager extends BusinessBase {
  def get(id: UUID): Future[Option[Album]]
  def create(createAlbumRequest: CreateAlbumRequest): Future[Album]
  def update(album: Album): Future[Option[Album]]
  def getAlbumSongs(id: UUID): Future[List[Song]]
}

class AlbumManagerImpl (implicit albumDao: AlbumDao, executionContext: ExecutionContext) extends AlbumManager {
  override def get(id: UUID): Future[Option[Album]] = albumDao.get(id)

  override def create(createAlbumRequest: CreateAlbumRequest): Future[Album] =
    albumDao.create(Album(createAlbumRequest))

  override def update(album: Album): Future[Option[Album]] = albumDao.update(album)

  override def getAlbumSongs(id: UUID): Future[List[Song]] = albumDao.getAlbumSongs(id)
}
