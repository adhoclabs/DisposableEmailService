package co.adhoclabs.template.data

import co.adhoclabs.template.data.SlickPostgresProfile.api._
import co.adhoclabs.template.models.{Album, AlbumWithSongs, CreateAlbumRequest, Song}
import co.adhoclabs.template.models.Genre._
import java.util.UUID

import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}
import slick.lifted.ProvenShape
import slick.dbio.Effect
import slick.sql.FixedSqlAction

trait AlbumDao {
  def get(id: UUID): Future[Option[Album]]
  def create(albumWithSongs: AlbumWithSongs): Future[AlbumWithSongs]
  def update(album: Album): Future[Option[Album]]
  def getAlbumSongs(id: UUID): Future[List[Song]]
  def delete(id: UUID): Future[Unit]
}

case class AlbumsTable(tag: Tag) extends Table[Album](tag, "albums") {
  def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
  def title: Rep[String] = column[String]("title")
  def genre: Rep[Option[Genre]] = column[Option[Genre]]("genre")

  // Provides a default projection that maps between columns in the table and instances of our case class.
  // mapTo creates a two-way mapping between the columns and fields.
  override def * : ProvenShape[Album] = (id, title, genre).mapTo[Album]
}

class AlbumDaoImpl(implicit databaseConnection: DatabaseConnection, executionContext: ExecutionContext) extends BaseDao with AlbumDao {
  lazy val albums = TableQuery[AlbumsTable]

  override protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  override def get(id: UUID): Future[Option[Album]] = db.run(
    albums.filter(_.id === id).result.headOption
  )

  override def create(albumWithSongs: AlbumWithSongs): Future[AlbumWithSongs] = {
    db.run(albums.returning(albums) += albumWithSongs.album).map(AlbumWithSongs(_, albumWithSongs.songs))
//    val createF = for {
//      album <- albums.returning(albums) += albumWithSongs.album
//      //songs <- songDao.create()
//    } yield ()
//    db.run(createF.transactionally)
  }

  override def update(album: Album): Future[Option[Album]] = Future.successful(Some(album))

  override def getAlbumSongs(id: UUID): Future[List[Song]] = ???

  override def delete(id: UUID): Future[Unit] = ???
}
