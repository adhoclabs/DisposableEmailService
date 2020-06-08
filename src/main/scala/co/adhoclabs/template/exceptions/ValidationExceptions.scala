package co.adhoclabs.template.exceptions

import co.adhoclabs.model.ErrorResponse
import co.adhoclabs.template.models.Album

abstract class ValidationException(val errorResponse: ErrorResponse) extends Exception {}

case class NoSongsInAlbumException(album: Album) extends ValidationException(
    ErrorResponse(s"Not creating album entitled ${album.title} because it had no songs.")
)
