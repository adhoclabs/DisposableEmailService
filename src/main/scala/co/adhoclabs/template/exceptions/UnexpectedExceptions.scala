package co.adhoclabs.template.exceptions

import co.adhoclabs.model.ErrorResponse
import co.adhoclabs.template.models.Album

abstract class UnexpectedException(val errorResponse: ErrorResponse) extends Exception(errorResponse.error) {}

case class AlbumNotCreatedException(album: Album) extends UnexpectedException(
  ErrorResponse(s"Unknown error creating album entitled ${album.title}.")
)
