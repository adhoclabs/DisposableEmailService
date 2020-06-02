package co.adhoclabs.template.models

import java.util.UUID

case class Song(
  id: String,
  title: String,
  album: UUID,
  albumPosition: Int
)
