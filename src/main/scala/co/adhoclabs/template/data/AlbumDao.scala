package co.adhoclabs.template.data

import co.adhoclabs.template.data.SlickPostgresProfile.api._
import co.adhoclabs.template.models.{Album, CreateAlbumRequest, Song}
import co.adhoclabs.template.models.Genre._
import java.util.UUID
import org.slf4j.{Logger, LoggerFactory}
import scala.concurrent.{ExecutionContext, Future}

trait AlbumDao {
  def get(id: String): Future[Option[Album]]
  def create(albumRequest: CreateAlbumRequest): Future[Option[Album]]
  def update(album: Album): Future[Option[Album]]
  def getAlbumSongs(id: String): Future[List[Song]]
}

class AlbumDaoImpl(implicit databaseConnection: DatabaseConnection) extends BaseDao with AlbumDao {

  override protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  override def get(id: String): Future[Option[Album]] = Future.successful(Some(Album(id, "Remain in Light", Some(Rock))))

  override def create(albumRequest: CreateAlbumRequest): Future[Option[Album]] =
    Future.successful(Some(Album("new-id", albumRequest.title, albumRequest.genre)))

  override def update(album: Album): Future[Option[Album]] = Future.successful(Some(album))

  override def getAlbumSongs(id: String): Future[List[Song]] = ???
}
