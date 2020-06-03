package co.adhoclabs.template.data

import co.adhoclabs.template.data.SlickPostgresProfile.api
import co.adhoclabs.template.models.{CreateSongRequest, Song}
import java.util.UUID
import org.slf4j.{Logger, LoggerFactory}
import scala.concurrent.Future

trait SongDao {
  def get(id: UUID): Future[Option[Song]]
  def create(createSongRequest: CreateSongRequest): Future[Song]
  def update(song: Song): Future[Option[Song]]
}

class SongDaoImpl(implicit databaseConnection: DatabaseConnection) extends BaseDao with SongDao {
  override protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  override def get(id: UUID): Future[Option[Song]] = Future.successful(Some(Song(id, "Sunshine of Your Love", UUID.randomUUID, 1)))

  override def create(createSongRequest: CreateSongRequest): Future[Song] = ???

  override def update(song: Song): Future[Option[Song]] = ???
}
