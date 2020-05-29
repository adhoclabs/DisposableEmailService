package co.adhoclabs.template.business

import co.adhoclabs.template.data.SongDao
import co.adhoclabs.template.models.Song

import scala.concurrent.Future

trait SongManager extends BusinessBase {
  def get(id: String): Future[Option[Song]]
}

class SongManagerImpl (implicit songDao: SongDao) extends SongManager {
  override def get(id: String): Future[Option[Song]] = songDao.get(id)
}
