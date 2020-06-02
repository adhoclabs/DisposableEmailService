package co.adhoclabs.template.api

import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import co.adhoclabs.template.models.{Album, CreateAlbumRequest, Song}
import co.adhoclabs.template.models.Genre._
import scala.concurrent.Future
import spray.json._

class AlbumApiTest extends ApiTestBase {

  describe("GET /albums/:id") {
    it("should call AlbumManager.get") {
      val expectedAlbum: Album = Album(
        id = "album-123",
        title = "Remain in Light",
        genre = Some(Rock)
      )

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
      val expectedAlbum = Album(
        id = "album-123",
        title = "Remain in Light",
        genre = Some(Rock)
      )

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
      val createAlbumRequest = CreateAlbumRequest(
        title = "Remain in Light",
        genre = Some(Rock)
      )

      val expectedAlbum: Album = Album(
        id = "album-123",
        title = "Remain in Light",
        genre = Some(Rock)
      )

      (albumManager.create _)
          .expects(createAlbumRequest)
          .returning(Future.successful(expectedAlbum))

      Post(s"/albums", HttpEntity(`application/json`, s"""${createAlbumRequest.toJson}""")) ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.Created)
        assert(responseAs[Album] == expectedAlbum)
      }
    }

    it("should call AlbumManager.create and return a 500 response when creation is not successful") {
      val createAlbumRequest = CreateAlbumRequest(
        title = "Remain in Light",
        genre = Some(Rock)
      )

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
      val albumId = "album-123"

      val expectedSongs: List[Song] = List(
        Song(
          id = "song-id-123",
          title = "Born Under Punches (the Heat Goes on)",
          album = albumId,
          albumPosition = 1
        ),
        Song(
          id = "song-id-124",
          title = "Once in a Lifetime",
          album = albumId,
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
