package co.adhoclabs.template.data

import co.adhoclabs.template.data.SlickPostgresProfile.api._
import co.adhoclabs.template.models.{Album, CreateAlbumRequest, Song}
import co.adhoclabs.template.models.Genre._
import java.util.UUID
import org.slf4j.{Logger, LoggerFactory}
import scala.concurrent.{ExecutionContext, Future}
import slick.lifted.ProvenShape
import slick.dbio.Effect
import slick.sql.FixedSqlAction

trait AlbumDao {
  def get(id: UUID): Future[Option[Album]]
  def create(albumRequest: CreateAlbumRequest): Future[Album]
  def update(album: Album): Future[Option[Album]]
  def getAlbumSongs(id: UUID): Future[List[Song]]
}

case class AlbumTable(tag: Tag) extends Table[Album](tag, "albums") {
  def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
  def title: Rep[String] = column[String]("title")
  def genre: Rep[Option[Genre]] = column[Option[Genre]]("genre")

  //provides a default projection that maps between columns in the table and instances of our case class
  //mapTo creates a two-way mapping between the columns and fields
  override def * : ProvenShape[Album] = (id, title, genre).mapTo[Album]
}

class AlbumDaoImpl(implicit databaseConnection: DatabaseConnection) extends BaseDao with AlbumDao {
  lazy val albums = TableQuery[AlbumTable]

  override protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  override def get(id: UUID): Future[Option[Album]] = db.run(
    albums.filter(_.id === id).result.headOption
  )

  override def create(albumRequest: CreateAlbumRequest): Future[Album] = db.run(
    albums.returning(albums) += Album(albumRequest)
  )

  override def update(album: Album): Future[Option[Album]] = Future.successful(Some(album))

  override def getAlbumSongs(id: UUID): Future[List[Song]] = ???
}
