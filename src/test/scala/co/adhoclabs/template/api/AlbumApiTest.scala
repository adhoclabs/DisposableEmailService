package co.adhoclabs.template.api

import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import co.adhoclabs.template.exceptions.{AlbumAlreadyExistsException, AlbumNotCreatedException}
import co.adhoclabs.template.models.{Album, AlbumWithSongs, CreateAlbumRequest, CreateSongRequest}
import scala.concurrent.Future
import spray.json._

class AlbumApiTest extends ApiTestBase {

  val expectedAlbumWithSongs = generateAlbumWithSongs()

  val createAlbumRequest = CreateAlbumRequest(
    title = expectedAlbumWithSongs.album.title,
    genre = expectedAlbumWithSongs.album.genre,
    songs = expectedAlbumWithSongs.songs.map(song =>
      CreateSongRequest(
      title = song.title,
      albumId = song.albumId,
      albumPosition = song.albumPosition
    ))
  )

  describe("GET /albums/:id") {
    it("should call AlbumManager.get and return a 200 with an album with songs body when album exists") {
      (albumManager.get _)
        .expects(expectedAlbumWithSongs.album.id)
        .returning(Future.successful(Some(expectedAlbumWithSongs)))

      Get(s"/albums/${expectedAlbumWithSongs.album.id}") ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.OK)
        assert(responseAs[AlbumWithSongs] == expectedAlbumWithSongs)
      }
    }

    it("should call AlbumManager.get and return a 404 when album doesn't exist") {
      (albumManager.get _)
          .expects(expectedAlbumWithSongs.album.id)
          .returning(Future.successful(None))

      Get(s"/albums/${expectedAlbumWithSongs.album.id}") ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.NotFound)
      }
    }
  }

  describe("PUT /albums/:id") {
    it("should call AlbumManager.update and return updated song when it exists") {
      (albumManager.update _)
          .expects(expectedAlbumWithSongs.album)
          .returning(Future.successful(Some(expectedAlbumWithSongs.album)))

      val requestEntity = HttpEntity(`application/json`, s"""${expectedAlbumWithSongs.album.toJson}""")

      Put(s"/albums/${expectedAlbumWithSongs.album.id}", requestEntity) ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.OK)
        assert(responseAs[Album] == expectedAlbumWithSongs.album)
      }
    }

    it("should call AlbumManager.update and return a 404 when album doesn't exist") {
      (albumManager.update _)
          .expects(expectedAlbumWithSongs.album)
          .returning(Future.successful(None))

      val requestEntity = HttpEntity(`application/json`, s"""${expectedAlbumWithSongs.album.toJson}""")

      Put(s"/albums/${expectedAlbumWithSongs.album.id}", requestEntity) ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.NotFound)
      }
    }
  }

  describe("POST /albums") {
    it("should call AlbumManager.create and return a 201 Created response when creation is successful") {
      (albumManager.create _)
          .expects(createAlbumRequest)
          .returning(Future.successful(expectedAlbumWithSongs))

      val requestEntity = HttpEntity(`application/json`, s"""${createAlbumRequest.toJson}""")

      Post(s"/albums", requestEntity) ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.Created)
        assert(responseAs[AlbumWithSongs] == expectedAlbumWithSongs)
      }
    }

    it("should call AlbumManager.create and return a 500 response when creation is not successful") {
      (albumManager.create _)
          .expects(createAlbumRequest)
          .throwing(AlbumNotCreatedException(expectedAlbumWithSongs.album))

      val requestEntity = HttpEntity(`application/json`, s"""${createAlbumRequest.toJson}""")

      Post(s"/albums", requestEntity) ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.InternalServerError)
      }
    }

    it("should call AlbumManager.create and return a 400 response when album already exists") {
      (albumManager.create _)
          .expects(createAlbumRequest)
          .throwing(AlbumAlreadyExistsException("album already exists"))

      val requestEntity = HttpEntity(`application/json`, s"""${createAlbumRequest.toJson}""")

      Post(s"/albums", requestEntity) ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.BadRequest)
      }
    }
  }
}
