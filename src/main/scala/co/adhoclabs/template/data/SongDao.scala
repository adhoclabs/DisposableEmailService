package co.adhoclabs.template.data

import java.util.UUID

import co.adhoclabs.template.data.SlickPostgresProfile.api._
import co.adhoclabs.template.models.Song
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc.GetResult
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

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

class SongDaoImpl(implicit databaseConnection: DatabaseConnection, executionContext: ExecutionContext) extends BaseDao with SongDao {
  override protected val logger: Logger = LoggerFactory.getLogger(this.getClass)
  lazy val songs = TableQuery[SongsTable]
  private type SongsQuery = Query[SongsTable, Song, Seq]

  override def get(id: UUID): Future[Option[Song]] = {
    db.run(
      songs
        .filterById(id)
        .result
        .headOption
    )
  }

  override def create(song: Song): Future[Song] = {
    db.run(
      songs.returning(songs) += song
    )
  }

  override def createMany(songsToAdd: List[Song]): Future[List[Song]] = {
    db.run(
      songs.returning(songs) ++= songsToAdd
    ).map(_.toList)
  }

  override def update(song: Song): Future[Option[Song]] = {
    val query =
      sql"""
        update songs
        set
          title = ${song.title},
          album_id = ${song.albumId},
          album_position = ${song.albumPosition}
        where id = ${song.id}
        returning *
         """
    db.run(
      query
        .as[Song]
        .headOption
    )
  }

  implicit class SongsQueries(val query: SongsQuery) {
    def filterById(id: UUID): SongsQuery =
      query.filter(_.id === id)
  }
  implicit val getSongResult: GetResult[Song] = GetResult(r => Song(r.<<, r.<<, r.<<, r.<<))
}
