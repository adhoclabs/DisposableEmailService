package co.adhoclabs.template.data

import java.util.UUID

import co.adhoclabs.template.data.SlickPostgresProfile.api._
import co.adhoclabs.template.data.SlickPostgresProfile.backend.Database
import co.adhoclabs.template.models.{Album, Genre, Song}
import org.postgresql.util.PSQLException
import org.slf4j.Logger
import slick.jdbc.PositionedResult

abstract class DaoBase(implicit val db: Database) {
  protected val logger: Logger
}

object DaoBase {
  def isUniqueConstraintViolation(e: PSQLException): Boolean = {
    //https://www.postgresql.org/docs/11/errcodes-appendix.html
    e.getSQLState == "23505"
  }

  def constructAlbum(r: PositionedResult): Album = {
    Album(
      id        = r.nextUuid,
      title     = r.nextString,
      artists   = r.nextArray[String].toList,
      genre     = Genre.withName(r.nextString),
      createdAt = r.nextInstant,
      updatedAt = r.nextInstant
    )
  }

  def constructSong(uuid: UUID, r: PositionedResult): Song = {
    Song(
      id            = uuid,
      title         = r.nextString,
      albumId       = r.nextUuid,
      albumPosition = r.nextInt,
      createdAt     = r.nextInstant,
      updatedAt     = r.nextInstant
    )
  }
}
