package co.adhoclabs.template.data

import java.util.UUID

import co.adhoclabs.template.data.SlickPostgresProfile.api._
import co.adhoclabs.template.exceptions.AlbumNotCreatedException
import co.adhoclabs.template.models.Genre._
import co.adhoclabs.template.models.{Album, AlbumWithSongs}
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc.GetResult
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

trait AlbumDao {
  def get(id: UUID): Future[Option[AlbumWithSongs]]
  def create(albumWithSongs: AlbumWithSongs): Future[AlbumWithSongs]
  def update(album: Album): Future[Option[Album]]
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
  override protected val logger: Logger = LoggerFactory.getLogger(this.getClass)
  lazy val albums = TableQuery[AlbumsTable]
  lazy val songs = TableQuery[SongsTable]
  private type AlbumsQuery = Query[AlbumsTable, Album, Seq]

  override def get(id: UUID): Future[Option[AlbumWithSongs]] = {
    db.run(
      albums
        .filterById(id)
        .joinLeft(songs).on(_.id === _.albumId)
        .map { case (albums, songs) => (albums, songs) }
        .result
    ).map { rows =>
      rows.headOption match {
        case Some((album, _)) =>
          val albumSongs = rows.flatMap(_._2).toList
          Some(AlbumWithSongs(album, albumSongs))
        case None =>
          None
      }
    }
  }

  override def create(albumToCreate: AlbumWithSongs): Future[AlbumWithSongs] = {
    db.run(albums.returning(albums) += albumToCreate.album).map(AlbumWithSongs(_, albumToCreate.songs))
    val create = for {
      album <- albums.returning(albums) += albumToCreate.album
      songs <- songs.returning(songs) ++= albumToCreate.songs
    } yield (album, songs)
    for {
      _ <- db.run(create.transactionally)
      albumWithSongsO <- get(albumToCreate.album.id)
    } yield (albumWithSongsO match {
      case Some(albumCreated) => albumCreated
      case None => throw AlbumNotCreatedException(albumToCreate.album)
    })

  }

  override def update(album: Album): Future[Option[Album]] = {
    val query =
      sql"""
        update albums
        set
          title = ${album.title},
          genre = ${album.id}
        where id = ${album.id}
        returning *
         """
    db.run(
      query
        .as[Album]
        .headOption
    )
  }

  override def delete(id: UUID): Future[Unit] = ???

  implicit class AlbumsQueries(val query: AlbumsQuery) {
    def filterById(id: UUID): AlbumsQuery =
      query.filter(_.id === id)
  }
  implicit val getAlbumResult: GetResult[Album] = GetResult(r => Album(r.<<, r.<<, r.<<))
}
