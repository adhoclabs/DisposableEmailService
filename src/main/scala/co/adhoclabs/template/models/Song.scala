package co.adhoclabs.template.models

import java.util.UUID

case class Song(
  id: UUID,
  title: String,
  album: UUID,
  albumPosition: Int
)

object Song extends ((UUID, String, UUID, Int) => Song) {
  def apply(createSongRequest: CreateSongRequest): Song = Song(
    id = UUID.randomUUID,
    title = createSongRequest.title,
    album = createSongRequest.album,
    albumPosition = createSongRequest.albumPosition
  )
}

case class CreateSongRequest(
  title: String,
  album: UUID,
  albumPosition: Int
)
