package co.adhoclabs.template.exceptions

import co.adhoclabs.template.models.Album

abstract class ValidationException(message: String) extends Exception { }

case class NoSongsInAlbumException(album: Album) extends ValidationException(
    s"Not creating album entitled ${album.title} because it had no songs."
)
