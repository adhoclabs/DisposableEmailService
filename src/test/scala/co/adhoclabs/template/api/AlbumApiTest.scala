package co.adhoclabs.template.api

import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import co.adhoclabs.template.models.{Album, AlbumWithSongs, CreateAlbumRequest, CreateSongRequest, Song}
import co.adhoclabs.template.models.Genre._
import java.util.UUID

import scala.concurrent.Future
import spray.json._

class AlbumApiTest extends ApiTestBase {

  val albumId = UUID.randomUUID
  val songId = UUID.randomUUID

  val expectedAlbum = Album(
    id = albumId,
    title = "Remain in Light",
    genre = Some(Rock)
  )
  val expectedSong = Song(
    id = songId,
    title = "Forever Drakeness",
    albumId = expectedAlbum.id,
    albumPosition = 1
  )

  val createAlbumRequest = CreateAlbumRequest(
    title = expectedAlbum.title,
    genre = expectedAlbum.genre,
    songs = List.empty[CreateSongRequest]
  )

  describe("GET /albums/:id") {
    it("should call AlbumManager.get") {
      (albumManager.get _)
        .expects(expectedAlbum.id)
        .returning(Future.successful(Some(expectedAlbum)))

      Get(s"/albums/${expectedAlbum.id}") ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.OK)
        assert(responseAs[Album] == expectedAlbum)
      }
    }
  }

  describe("PUT /albums/:id") {
    it("should call AlbumManager.update") {
      (albumManager.update _)
          .expects(expectedAlbum)
          .returning(Future.successful(Some(expectedAlbum)))

      Put(s"/albums/${expectedAlbum.id}", HttpEntity(`application/json`, s"""${expectedAlbum.toJson}""")) ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.OK)
        assert(responseAs[Album] == expectedAlbum)
      }
    }
  }

  describe("POST /albums") {
    it("should call AlbumManager.create and return a 201 Created response when creation is successful") {
      (albumManager.create _)
          .expects(createAlbumRequest)
          .returning(Future.successful(AlbumWithSongs(expectedAlbum, List(expectedSong))))

      Post(s"/albums", HttpEntity(`application/json`, s"""${createAlbumRequest.toJson}""")) ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.Created)
        assert(responseAs[Album] == expectedAlbum)
      }
    }

    it("should call AlbumManager.create and return a 500 response when creation is not successful") {
      (albumManager.create _)
          .expects(createAlbumRequest)
          .throwing(new Exception("Album not created")) // todo: use HttpExceptions from model here

      Post(s"/albums", HttpEntity(`application/json`, s"""${createAlbumRequest.toJson}""")) ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.InternalServerError)
      }
    }
  }

  describe("GET /albums/:id/songs") {
    it("should call AlbumManager.getAlbumSongs and return a 200 with a list of songs") {

      val expectedSongs: List[Song] = List(
        Song(
          id = UUID.randomUUID,
          title = "Born Under Punches (the Heat Goes on)",
          albumId = albumId,
          albumPosition = 1
        ),
        Song(
          id = UUID.randomUUID,
          title = "Once in a Lifetime",
          albumId = albumId,
          albumPosition = 2
        )
      )

      (albumManager.getAlbumSongs _)
          .expects(albumId)
          .returning(Future.successful(expectedSongs))

      Get(s"/albums/$albumId/songs") ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.OK)
        assert(responseAs[List[Song]] == expectedSongs)
      }
    }
  }
}
