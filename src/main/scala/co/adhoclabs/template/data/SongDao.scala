package co.adhoclabs.template.data

import co.adhoclabs.template.data.SlickPostgresProfile.api._
import co.adhoclabs.template.models.{CreateSongRequest, Song}
import java.util.UUID

import org.slf4j.{Logger, LoggerFactory}
import slick.lifted.ProvenShape

import scala.concurrent.Future

trait SongDao {
  def get(id: UUID): Future[Option[Song]]
  def create(song: Song): Future[Song]
  def createMany(songs: List[Song]): Future[List[Song]]
  def update(song: Song): Future[Option[Song]]
}

case class SongsTable(tag: Tag) extends Table[Song](tag, "songs") {
  def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
  def title: Rep[String] = column[String]("title")
  def albumId: Rep[UUID] = column[UUID]("album_id")
  def albumPosition: Rep[Int] = column[Int]("album_position")

  // Provides a default projection that maps between columns in the table and instances of our case class.
  // mapTo creates a two-way mapping between the columns and fields.
  override def * : ProvenShape[Song] = (id, title, albumId, albumPosition).mapTo[Song]
}

class SongDaoImpl(implicit databaseConnection: DatabaseConnection) extends BaseDao with SongDao {
  lazy val songs = TableQuery[SongsTable]

  override protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  override def get(id: UUID): Future[Option[Song]] = db.run(
    songs.filter(_.id === id).result.headOption
  )

  override def create(song: Song): Future[Song] = db.run(
    songs.returning(songs) += song
  )

  override def createMany(songsToAdd: List[Song]): Future[List[Song]] = ???
//    db.run(
//    songs.returning(songs) ++= songsToAdd
//  )

  override def update(song: Song): Future[Option[Song]] = ???
}
