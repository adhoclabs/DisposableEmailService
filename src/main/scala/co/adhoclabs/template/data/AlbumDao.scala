package co.adhoclabs.template.data

import java.util.UUID

import co.adhoclabs.template.data.SlickPostgresProfile.api._
import co.adhoclabs.template.exceptions.{AlbumNotCreatedException, AlbumNotDeletedException}
import co.adhoclabs.template.models.Genre._
import co.adhoclabs.template.models.{Album, AlbumWithSongs, Genre, Song}
import org.slf4j.{Logger, LoggerFactory}
import slick.dbio.Effect
import slick.jdbc.{GetResult, SetParameter}
import slick.lifted.ProvenShape
import slick.sql.FixedSqlAction

import scala.concurrent.{ExecutionContext, Future}

trait AlbumDao {
  def get(id: UUID): Future[Option[AlbumWithSongs]]
  def create(albumWithSongs: AlbumWithSongs): Future[AlbumWithSongs]
  def update(album: Album): Future[Option[Album]]
  def delete(id: UUID): Future[Int]
}

case class AlbumsTable(tag: Tag) extends Table[Album](tag, "albums") {
  def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
  def title: Rep[String] = column[String]("title")
  def genre: Rep[Genre] = column[Genre]("genre")

  // Provides a default projection that maps between columns in the table and instances of our case class.
  // mapTo creates a two-way mapping between the columns and fields.
  override def * : ProvenShape[Album] = (id, title, genre).mapTo[Album]
}

class AlbumDaoImpl(implicit databaseConnection: DatabaseConnection, executionContext: ExecutionContext) extends BaseDao with AlbumDao {
  override protected val logger: Logger = LoggerFactory.getLogger(this.getClass)
  lazy val albums = TableQuery[AlbumsTable]
  lazy val songs = TableQuery[SongsTable]
  private type AlbumsQuery = Query[AlbumsTable, Album, Seq]

  override def get(id: UUID): Future[Option[AlbumWithSongs]] = {
    val queryAction =
      sql"""
        select a.id, a.title, a.genre, s.id, s.title, s.album_id, s.album_position
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
    val create = for {
      _ <- albums.returning(albums) += albumToCreate.album
      _ <- if (albumToCreate.songs.nonEmpty) songs.returning(songs) ++= albumToCreate.songs else DBIO.successful(List.empty[Song])
    } yield ()
    for {
      _ <- db.run(create.transactionally)
      albumWithSongsO <- get(albumToCreate.album.id)
    } yield albumWithSongsO match {
      case Some(albumCreated) => albumCreated
      case None => throw AlbumNotCreatedException(albumToCreate.album)
    }
  }

  override def update(album: Album): Future[Option[Album]] = {
    val query =
      sql"""
        update albums
        set
          title = ${album.title},
          genre = ${album.genre.toString}::genre
        where id = ${album.id}
        returning id, title, genre
         """
    db.run(
      query
        .as[Album]
        .headOption
    )
  }

  override def delete(id: UUID): Future[Int] = {
    // Song -> album reference has cascading delete so no need to explicitly delete songs
    db.run(
      albums
        .filterById(id)
        .delete
    ) map {
      case count if count != 1 =>
        throw AlbumNotDeletedException(id)
      case count =>
        count
    }
  }

  implicit class AlbumsQueries(val query: AlbumsQuery) {
    def filterById(id: UUID): AlbumsQuery =
      query.filter(_.id === id)
  }

  implicit val getAlbumResult: GetResult[Album] =
    GetResult(r =>  Album(r.nextUuid, r.nextString, Genre.withName(r.nextString)))

  implicit val getAlbumSongTupleResult: GetResult[(Album, Option[Song])] =
    GetResult { r => (
      Album(r.nextUuid, r.nextString, Genre.withName(r.nextString)),
      r.nextUuidOption match {
        case Some(uuid) => Some(Song(uuid, r.nextString, r.nextUuid, r.nextInt))
        case None => None
      }
    )
  }
}
