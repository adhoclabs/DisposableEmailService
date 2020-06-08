package co.adhoclabs.template.models

import co.adhoclabs.template.models.Genre._
import java.util.UUID

case class Album(
  id: UUID,
  title: String,
  genre: Genre = NoGenre
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
  genre: Genre,
  songs: List[CreateSongRequest]
)