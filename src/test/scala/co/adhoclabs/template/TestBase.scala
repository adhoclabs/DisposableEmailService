package co.adhoclabs.template

import java.util.UUID

import co.adhoclabs.template.models.Genre._
import co.adhoclabs.template.models.{Album, AlbumWithSongs, Song}
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.OneInstancePerTest
import org.scalatest.funspec.AsyncFunSpec

abstract class TestBase extends AsyncFunSpec with AsyncMockFactory with OneInstancePerTest {
  protected def generateAlbumWithSongs(songCount: Int = 3, genre: Genre = Classical): AlbumWithSongs = {
    val album = generateAlbum(genre)
    val songs = generateSongs(album.id, songCount)
    AlbumWithSongs(album, songs)
  }

  protected def generateAlbum(genre: Genre = Classical): Album = {
    Album(
      id = UUID.randomUUID,
      title = "Album Title",
      genre = genre
    )
  }

  protected def generateSongs(albumId: UUID, count: Int): List[Song] = {
    val songs = for (i <- 1 to count) yield generateSong(albumId, i)
    songs.toList
  }

  protected def generateSong(albumId: UUID, albumPosition: Int): Song = {
    Song(
      UUID.randomUUID,
      s"Song $albumPosition",
      albumId = albumId,
      albumPosition = albumPosition
    )
  }
}
