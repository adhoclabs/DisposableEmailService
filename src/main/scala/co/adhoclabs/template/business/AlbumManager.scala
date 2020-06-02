package co.adhoclabs.template.business

import co.adhoclabs.template.data.AlbumDao
import co.adhoclabs.template.models.{Album, CreateAlbumRequest, Song}
import scala.concurrent.{ExecutionContext, Future}

trait AlbumManager extends BusinessBase {
  def get(id: String): Future[Option[Album]]
  def create(createAlbumRequest: CreateAlbumRequest): Future[Album]
  def update(album: Album): Future[Option[Album]]
  def getAlbumSongs(id: String): Future[List[Song]]
}

class AlbumManagerImpl (implicit albumDao: AlbumDao, executionContext: ExecutionContext) extends AlbumManager {
  override def get(id: String): Future[Option[Album]] = albumDao.get(id)

  override def create(createAlbumRequest: CreateAlbumRequest): Future[Album] =
    albumDao.create(createAlbumRequest) flatMap {
      case Some(album: Album) => Future.successful(album)
      case None => Future.failed(new Exception("")) // todo: use httpexceptions from model
    }

  override def update(album: Album): Future[Option[Album]] = albumDao.update(album)

  override def getAlbumSongs(id: String): Future[List[Song]] = albumDao.getAlbumSongs(id)
}
