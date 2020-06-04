package co.adhoclabs.template.models

import java.util.UUID

case class Song(
  id: UUID,
  title: String,
  albumId: UUID,
  albumPosition: Int
)

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
