package co.adhoclabs.template.exceptions

import co.adhoclabs.model.ErrorResponse
import co.adhoclabs.template.models.CreateAlbumRequest

abstract class ValidationException(val errorResponse: ErrorResponse) extends Exception(errorResponse.error) {}

case class NoSongsInAlbumException(createAlbumRequest: CreateAlbumRequest) extends ValidationException(
  ErrorResponse(s"Not creating album entitled ${createAlbumRequest.title} because it had no songs.")
)

case class SongAlreadyExistsException(duplicateKeyMessage: String) extends ValidationException(
  ErrorResponse(duplicateKeyMessage)
)

case class AlbumAlreadyExistsException(duplicateKeyMessage: String) extends ValidationException(
  ErrorResponse(duplicateKeyMessage)
)

case class InvalidPatchException(invalidPatchMessage: String) extends ValidationException(
  ErrorResponse(invalidPatchMessage)
)
