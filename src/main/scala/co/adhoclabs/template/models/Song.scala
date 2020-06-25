package co.adhoclabs.template.models

import java.time.Instant
import java.util.UUID

case class Song(
  id: UUID,
  title: String,
  albumId: UUID,
  albumPosition: Int,
  createdAt: Instant,
  updatedAt: Instant
)

case class CreateSongRequest(
  title: String,
  albumId: UUID,
  albumPosition: Int
)
