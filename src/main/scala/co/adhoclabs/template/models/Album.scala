package co.adhoclabs.template.models

import co.adhoclabs.template.models.Genre.Genre
import java.util.UUID

case class Album(
  id: UUID,
  title: String,
  genre: Option[Genre] = None
)

// This object isn't used, but is an example of how you would need to structure
// a helper object for a DB class with custom apply methods. In this case, the
// helper object must extend the Scala Function definition of the case class
// in order for the Slick mapTo method to identify the base apply method of the case class
object Album extends ((UUID, String, Option[Genre]) => Album) {
  def apply(createRequest: CreateAlbumRequest): Album = Album(
    id = UUID.randomUUID,
    title = createRequest.title,
    genre = createRequest.genre
  )
}

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