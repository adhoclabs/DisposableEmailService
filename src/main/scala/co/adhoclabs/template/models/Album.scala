package co.adhoclabs.template.models

import co.adhoclabs.model.BaseJsonProtocol
import co.adhoclabs.template.models.Genre._
import spray.json.RootJsonFormat

import java.time.Instant
import java.util.UUID

case class Album(
  id: UUID,
  title: String,
  artists: List[String],
  genre: Genre = NoGenre,
  createdAt: Instant,
  updatedAt: Instant
)

object Album extends BaseJsonProtocol {
  implicit val jsonFormat: RootJsonFormat[Album] = jsonFormat6(Album.apply)
}

case class AlbumWithSongs(
  album: Album,
  songs: List[Song]
)

object AlbumWithSongs extends BaseJsonProtocol {
  implicit val jsonFormat: RootJsonFormat[AlbumWithSongs] = jsonFormat2(AlbumWithSongs.apply)
}

case class CreateAlbumRequest(
  title: String,
  artists: List[String],
  genre: Genre,
  songs: List[String]
)

object CreateAlbumRequest extends BaseJsonProtocol {
  implicit val jsonFormat: RootJsonFormat[CreateAlbumRequest] = jsonFormat4(CreateAlbumRequest.apply)
}

case class PatchAlbumRequest(
  title: Option[String] = None,
  artists: Option[List[String]] = None,
  genre: Option[Genre] = None
)

object PatchAlbumRequest extends BaseJsonProtocol {
  implicit val jsonFormat: RootJsonFormat[PatchAlbumRequest] = jsonFormat3(PatchAlbumRequest.apply)
}
