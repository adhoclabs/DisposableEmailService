package co.adhoclabs.template.business

import co.adhoclabs.template.models.Song
import scala.concurrent.Future

trait SongManager extends BaseBusiness {
  def get(id: String): Future[Option[Song]]
}

class SongManagerImpl extends SongManager {
  override def get(id: String): Future[Option[Song]] = Future.successful(Some(Song(Some(id), "Sunshine of Your Love")))
}
