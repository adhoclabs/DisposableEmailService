package co.adhoclabs.template.business

import co.adhoclabs.template.data.SongDao
import co.adhoclabs.template.models.{CreateSongRequest, Song}
import java.util.UUID
import scala.concurrent.Future

trait SongManager extends BusinessBase {
  def get(id: UUID): Future[Option[Song]]
  def create(createSongRequest: CreateSongRequest): Future[Song]
  def update(song: Song): Future[Option[Song]]
}

class SongManagerImpl (implicit songDao: SongDao) extends SongManager {
  override def get(id: UUID): Future[Option[Song]] = songDao.get(id)

  override def create(createSongRequest: CreateSongRequest): Future[Song] = songDao.create(createSongRequest)

  override def update(song: Song): Future[Option[Song]] = songDao.update(song)
}
