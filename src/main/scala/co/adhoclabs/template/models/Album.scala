package co.adhoclabs.template.models

import co.adhoclabs.template.models.Genre.Genre
import java.util.UUID

case class Album(
  id: UUID,
  title: String,
  genre: Option[Genre] = None
)

case class AlbumWithSongs(
  album: Album,
  songs: List[Song]
)

object AlbumWithSongs {
  def apply(createRequest: CreateAlbumRequest): AlbumWithSongs = {
    val album = Album(
      id = UUID.randomUUID,
      title = createRequest.title,
      genre = createRequest.genre,
    )
    AlbumWithSongs(
      album = album,
      songs = createRequest.songs.map(Song(_))
    )
  }
}

case class CreateAlbumRequest(
  title: String,
  genre: Option[Genre],
  songs: List[CreateSongRequest]
)