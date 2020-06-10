package co.adhoclabs.template.business

import co.adhoclabs.template.data.SongDao
import co.adhoclabs.template.models.{CreateSongRequest, Song}
import java.util.UUID

import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.Future

trait SongManager extends BusinessBase {
  def get(id: UUID): Future[Option[Song]]
  def create(createSongRequest: CreateSongRequest): Future[Song]
  def update(song: Song): Future[Option[Song]]
}

class SongManagerImpl (implicit songDao: SongDao) extends SongManager {
  override protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  override def get(id: UUID): Future[Option[Song]] = songDao.get(id)

  override def create(createSongRequest: CreateSongRequest): Future[Song] = songDao.create(Song(createSongRequest))

  override def update(song: Song): Future[Option[Song]] = songDao.update(song)
}
