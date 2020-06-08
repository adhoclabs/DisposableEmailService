package co.adhoclabs.template.exceptions

import java.util.UUID

import co.adhoclabs.model.ErrorResponse
import co.adhoclabs.template.models.Album

abstract class UnexpectedException(val errorResponse: ErrorResponse) extends Exception { }

case class AlbumNotCreatedException(album: Album) extends UnexpectedException(
    ErrorResponse(s"Unknown error creating album entitled ${album.title}.")
)

case class AlbumNotDeletedException(albumId: UUID) extends UnexpectedException(
    ErrorResponse(s"Unknown error deleting album entitled $albumId.")
)

case class SongNotDeletedException(songId: UUID) extends UnexpectedException(
    ErrorResponse(s"Unknown error deleting song entitled $songId.")
)
