package co.adhoclabs.template.api

import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import co.adhoclabs.template.exceptions.SongAlreadyExistsException
import co.adhoclabs.template.models.{CreateSongRequest, Song}
import java.util.UUID
import scala.concurrent.Future
import spray.json._

class SongApiTest extends ApiTestBase {

  val albumId = UUID.randomUUID
  val songId = UUID.randomUUID

  val expectedSong: Song = Song(
    id = songId,
    title = "Once in a Lifetime",
    albumId = albumId,
    albumPosition = 1
  )

  val createSongRequest: CreateSongRequest = CreateSongRequest(
    title = expectedSong.title,
    albumId = expectedSong.albumId,
    albumPosition = expectedSong.albumPosition
  )

  describe("GET /songs/:id") {
    it("should call SongManager.get and return a 200 response with a song body when song exists") {

      (songManager.get _)
        .expects(expectedSong.id)
        .returning(Future.successful(Some(expectedSong)))

      Get(s"/songs/${expectedSong.id}") ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.OK)
        assert(responseAs[Song] == expectedSong)
      }
    }

    it("should call SongManager.get and return a 404 response song doesn't exist") {
      (songManager.get _)
          .expects(expectedSong.id)
          .returning(Future.successful(None))

      Get(s"/songs/${expectedSong.id}") ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.NotFound)
      }
    }
  }

  describe("POST /songs") {
    it("should call SongManager.create and return created song with ID when successful") {
      (songManager.create _)
          .expects(createSongRequest)
          .returning(Future.successful(expectedSong))

      Post("/songs", HttpEntity(`application/json`, s"""${createSongRequest.toJson}""")) ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.Created)
        assert(responseAs[Song] == expectedSong)
      }
    }

    it("should call SongManager.create and return a 400 response when creation is not successful") {
      (songManager.create _)
          .expects(createSongRequest)
          .throwing(SongAlreadyExistsException("Song not created"))

      Post(s"/songs", HttpEntity(`application/json`, s"""${createSongRequest.toJson}""")) ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.BadRequest)
      }
    }
  }

  describe("PUT /songs/:songId") {
    it("should call SongManager.update and return a 200 with an update song when it exists") {
      (songManager.update _)
          .expects(expectedSong)
          .returning(Future.successful(Some(expectedSong)))

      Put(s"/songs/${expectedSong.id}", HttpEntity(`application/json`, s"""${expectedSong.toJson}""")) ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.OK)
        assert(responseAs[Song] == expectedSong)
      }
    }

    it("should call SongManager.update and return a 404 when song doesn't exist") {
      (songManager.update _)
          .expects(expectedSong)
          .returning(Future.successful(None))

      Put(s"/songs/${expectedSong.id}", HttpEntity(`application/json`, s"""${expectedSong.toJson}""")) ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.NotFound)
      }
    }
  }

}
