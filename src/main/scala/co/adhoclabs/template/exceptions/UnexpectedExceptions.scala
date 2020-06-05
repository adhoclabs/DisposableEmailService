package co.adhoclabs.template.exceptions

import co.adhoclabs.template.models.Album

abstract class UnexpectedException(message: String) extends Exception { }

case class AlbumNotCreatedException(album: Album) extends UnexpectedException(
    s"Unknown error creating album entitled ${album.title}."
)
