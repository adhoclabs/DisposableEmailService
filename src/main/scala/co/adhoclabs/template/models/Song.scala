package co.adhoclabs.template.models

import java.util.UUID

case class Song(
  id: UUID,
  title: String,
  albumId: UUID,
  albumPosition: Int
)

// If a case class is being used by Slick to represent a database row, and if that
// case class's helper function has custom apply methods like this one does,
// the helper object must must extend the Scala Function definition of the case class
// in order for the Slick mapTo method to identify the base apply method of the case class.
object Song extends ((UUID, String, UUID, Int) => Song) {
  def apply(createSongRequest: CreateSongRequest): Song = Song(
    id = UUID.randomUUID,
    title = createSongRequest.title,
    albumId = createSongRequest.albumId,
    albumPosition = createSongRequest.albumPosition
  )
}

case class CreateSongRequest(
  title: String,
  albumId: UUID,
  albumPosition: Int
)
