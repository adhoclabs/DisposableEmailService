package co.adhoclabs.template.models

import co.adhoclabs.template.models.Genre.Genre
import java.util.UUID

case class Album(
  id: UUID,
  title: String,
  genre: Option[Genre]
)

object Album extends ((UUID, String, Option[Genre]) => Album) {
  def apply(createAlbumRequest: CreateAlbumRequest): Album = Album(
    id = UUID.randomUUID,
    title = createAlbumRequest.title,
    genre = createAlbumRequest.genre
  )
}

case class CreateAlbumRequest(
  title: String,
  genre: Option[Genre]
)