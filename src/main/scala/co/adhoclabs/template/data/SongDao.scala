package co.adhoclabs.template.data

import co.adhoclabs.template.data.SlickPostgresProfile.api._
import co.adhoclabs.template.data.SlickPostgresProfile.backend.Database
import co.adhoclabs.template.exceptions.SongAlreadyExistsException
import co.adhoclabs.template.models.Song
import org.postgresql.util.PSQLException
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc.GetResult
import slick.lifted.ProvenShape

import java.time.{Clock, Instant}
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait SongDao {
  def get(id: UUID): Future[Option[Song]]
  def create(song: Song): Future[Song]
  def createMany(songs: List[Song]): Future[List[Song]]
  def update(song: Song): Future[Option[Song]]
  def delete(id: UUID): Future[Int]
}

case class SongsTable(tag: Tag) extends Table[Song](tag, "songs") {
  def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
  def title: Rep[String] = column[String]("title")
  def albumId: Rep[UUID] = column[UUID]("album_id")
  def albumPosition: Rep[Int] = column[Int]("album_position")
  def createdAt: Rep[Instant] = column[Instant]("created_at")
  def updatedAt: Rep[Instant] = column[Instant]("updated_at")

  // Provides a default projection that maps between columns in the table and instances of our case class.
  override def * : ProvenShape[Song] = (id, title, albumId, albumPosition, createdAt, updatedAt) <> ((Song.apply _).tupled, Song.unapply)
}

class SongDaoImpl(implicit db: Database, executionContext: ExecutionContext, clock: Clock) extends DaoBase with SongDao {

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
      (songs.returning(songs) += song).asTry
    ) map {
        case Success(s: Song) => s
        case Failure(e: PSQLException) =>
          if (DaoBase.isUniqueConstraintViolation(e))
            throw SongAlreadyExistsException(e.getServerErrorMessage.getMessage)
          else
            throw e
        case Failure(t: Throwable) =>
          throw t
      }
  }

  override def createMany(songsToAdd: List[Song]): Future[List[Song]] = {
    db.run(
      (songs.returning(songs) ++= songsToAdd).asTry
    ) map {
        case Success(s: Seq[Song]) => s.toList
        case Failure(e: PSQLException) =>
          if (DaoBase.isUniqueConstraintViolation(e))
            throw SongAlreadyExistsException(e.getServerErrorMessage.getMessage)
          else
            throw e
        case Failure(t: Throwable) =>
          throw t
      }
  }

  override def update(song: Song): Future[Option[Song]] = {
    db.run(
      songs
        .filterById(song.id)
        .update(song)
    ) flatMap { rowsAffected: Int =>
        if (rowsAffected == 1)
          get(song.id)
        else
          Future.successful(None)
      }
  }

  override def delete(id: UUID): Future[Int] = {
    db.run(
      songs
        .filterById(id)
        .delete
    )
  }

  implicit class SongsQueries(val query: SongsQuery) {
    def filterById(id: UUID): SongsQuery =
      query.filter(_.id === id)
  }

  implicit val getSongResult: GetResult[Song] = {
    GetResult { r =>
      DaoBase.constructSong(r.nextUuid, r)
    }
  }
}
