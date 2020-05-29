package co.adhoclabs.template.data

import co.adhoclabs.template.models.Song
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.Future

trait SongDao {
  def get(id: String): Future[Option[Song]]
}

class SongDaoImpl extends BaseDao with SongDao {
  override protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  override def get(id: String): Future[Option[Song]] = Future.successful(Some(Song(Some(id), "Sunshine of Your Love")))
}
