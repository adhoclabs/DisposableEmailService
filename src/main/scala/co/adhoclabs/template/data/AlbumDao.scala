package co.adhoclabs.template.data

import co.adhoclabs.template.data.SlickPostgresProfile.api._
import co.adhoclabs.template.data.SlickPostgresProfile.backend.Database
import co.adhoclabs.template.exceptions.{AlbumAlreadyExistsException, AlbumNotCreatedException}
import co.adhoclabs.template.models.Genre.Genre
import co.adhoclabs.template.models.{Album, AlbumWithSongs, Song}
import org.postgresql.util.PSQLException
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc.GetResult
import slick.lifted.ProvenShape

import java.time.{Clock, Instant}
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait AlbumDao {
  def get(id: UUID): Future[Option[Album]]
  def getWithSongs(id: UUID): Future[Option[AlbumWithSongs]]
  def create(albumWithSongs: AlbumWithSongs): Future[AlbumWithSongs]
  def update(album: Album): Future[Option[Album]]
  def delete(id: UUID): Future[Int]
}

case class AlbumsTable(tag: Tag) extends Table[Album](tag, "albums") {
  def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
  def title: Rep[String] = column[String]("title")
  def artists: Rep[List[String]] = column[List[String]]("artists")
  def genre: Rep[Genre] = column[Genre]("genre")
  def createdAt: Rep[Instant] = column[Instant]("created_at")
  def updatedAt: Rep[Instant] = column[Instant]("updated_at")

  // Provides a default projection that maps between columns in the table and instances of our case class.
  override def * : ProvenShape[Album] = (id, title, artists, genre, createdAt, updatedAt) <> ((Album.apply _).tupled, Album.unapply)
}

class AlbumDaoImpl(implicit db: Database, executionContext: ExecutionContext, clock: Clock) extends DaoBase with AlbumDao {

  override protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  lazy val albums = TableQuery[AlbumsTable]
  lazy val songs = TableQuery[SongsTable]

  private type AlbumsQuery = Query[AlbumsTable, Album, Seq]

  override def get(id: UUID): Future[Option[Album]] = {
    db.run(
      albums
        .filterById(id)
        .result
        .headOption
    )
  }

  override def getWithSongs(id: UUID): Future[Option[AlbumWithSongs]] = {
    // An example of how to write a join in plain SQL
    // You can also do joins in slick, but if the compiled query ends up being unnecessarily complex, this is preferred
    val queryAction =
      sql"""
        select
          a.id,
          a.title,
          a.artists,
          a.genre,
          a.created_at,
          a.updated_at,
          s.id,
          s.title,
          s.album_id,
          s.album_position,
          s.created_at,
          s.updated_at
        from albums a
        left join songs s on s.album_id = a.id
        where a.id = $id
        order by s.album_position
         """.as[(Album, Option[Song])]
    db.run(
      queryAction
    ) map { rows: Seq[(Album, Option[Song])] =>
      rows.headOption match {
        case Some((album, _)) =>
          val albumSongs: List[Song] = rows.flatMap(_._2).toList
          Some(AlbumWithSongs(album, albumSongs))
        case None =>
          None
      }
    }
  }

  override def create(albumToCreate: AlbumWithSongs): Future[AlbumWithSongs] = {

    val createAlbumAction = (albums.returning(albums) += albumToCreate.album).asTry map {
      case Success(a: Album) => a
      case Failure(p: PSQLException) =>
        if (DaoBase.isUniqueConstraintViolation(p))
          throw AlbumAlreadyExistsException(p.getServerErrorMessage.getMessage)
        else
          throw p
      case Failure(t: Throwable) =>
        throw t
    }

    val createSongsAction =
      if (albumToCreate.songs.nonEmpty)
        songs.returning(songs) ++= albumToCreate.songs
      else
        DBIO.successful(List.empty[Song])

    // A readable way to concatenate database actions
    val create = for {
      _ <- createAlbumAction
      _ <- createSongsAction
    } yield ()

    for {
      _ <- db.run(create.transactionally)
      albumWithSongsO <- getWithSongs(albumToCreate.album.id)
    } yield albumWithSongsO match {
      case Some(albumCreated) => albumCreated
      case None               => throw AlbumNotCreatedException(albumToCreate.album)
    }
  }

  override def update(album: Album): Future[Option[Album]] = {
    // we need to explicitly typecast genre here in plain sql because otherwise slick treats it as a string rather than
    // an enum
    val query =
      sql"""
        update albums
        set
          title = ${album.title},
          artists = ${album.artists},
          genre = ${album.genre.toString}::genre,
          updated_at = ${clock.instant()}
        where id = ${album.id}
        returning id, title, artists, genre, created_at, updated_at
         """.as[Album]
    db.run(
      query
        .headOption
    )
  }

  override def delete(id: UUID): Future[Int] = {
    // Song -> album reference has cascading delete so no need to explicitly delete songs
    db.run(
      albums
        .filterById(id)
        .delete
    )
  }

  // adding helper methods here with the return type of AlbumsQuery allows for readable chaining
  implicit class AlbumsQueries(val query: AlbumsQuery) {
    def filterById(id: UUID): AlbumsQuery =
      query.filter(_.id === id)
  }

  // when using plain SQL, we have to provide these `GetResult`s in order to convert between the result row
  // and the case class instance.
  // the `UuidSupport` trait in `SlickPostgresProfile` provides the `nextUuid` method, since there isn't out-of-the-box
  // conversion for postgres UUID type fields.
  // each field can also be written as `r.<<` for brevity, which will call the correct `.nextX` method -- here we used the
  // type-specific methods for compilation-time type safety.
  implicit val getAlbumResult: GetResult[Album] = {
    GetResult(DaoBase.constructAlbum)
  }

  implicit val getAlbumSongTupleResult: GetResult[(Album, Option[Song])] = {
    GetResult { r =>
      (
        DaoBase.constructAlbum(r),
        r.nextUuidOption match {
          case Some(uuid) => Some(DaoBase.constructSong(uuid, r))
          case None       => None
        }
      )
    }
  }
}
