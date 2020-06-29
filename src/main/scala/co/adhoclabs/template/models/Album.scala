package co.adhoclabs.template.models

import java.time.Instant

import co.adhoclabs.template.models.Genre._
import java.util.UUID

case class Album(
  id: UUID,
  title: String,
  artists: List[String],
  genre: Genre = NoGenre,
  createdAt: Instant,
  updatedAt: Instant
)

case class AlbumWithSongs(
  album: Album,
  songs: List[Song]
)

case class CreateAlbumRequest(
  title: String,
  artists: List[String],
  genre: Genre,
  songs: List[String]
)

case class PatchAlbumRequest(
  title: Option[String] = None,
  artists: Option[List[String]] = None,
  genre: Option[Genre] = None
)
