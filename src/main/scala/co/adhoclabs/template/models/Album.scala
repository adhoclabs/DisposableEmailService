package co.adhoclabs.template.models

import co.adhoclabs.model.BaseJsonProtocol
import co.adhoclabs.template.models.Genre._
import zio.schema.annotation.description

import java.time.Instant
import java.util.UUID
import zio.schema.{DeriveSchema, Schema}

case class Album(
  id:                                                                        UUID,
  title:                                                                     String,
  @description("All Of the Artists that contributed to the album.") artists: List[String],
  genre:                                                                     Genre        = NoGenre,
  createdAt:                                                                 Instant,
  updatedAt:                                                                 Instant
)

object Album extends BaseJsonProtocol {
  implicit val schema: Schema[Album] = DeriveSchema.gen
}

case class AlbumWithSongs(
  album: Album,
  songs: List[Song]
)

object AlbumWithSongs extends BaseJsonProtocol {
  implicit val schema: Schema[AlbumWithSongs] = DeriveSchema.gen
}

case class CreateAlbumRequest(
  title:   String,
  artists: List[String],
  genre:   Genre,
  songs:   List[String]
)

object CreateAlbumRequest extends BaseJsonProtocol {
  implicit val schema: Schema[CreateAlbumRequest] = DeriveSchema.gen
}

case class PatchAlbumRequest(
  title:   Option[String]       = None,
  artists: Option[List[String]] = None,
  genre:   Option[Genre]        = None
)

object PatchAlbumRequest extends BaseJsonProtocol {
  implicit val schema: Schema[PatchAlbumRequest] = DeriveSchema.gen
}
