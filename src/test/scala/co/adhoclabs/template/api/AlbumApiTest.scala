package co.adhoclabs.template.api

import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import co.adhoclabs.template.models.{Album, AlbumWithSongs, CreateAlbumRequest, CreateSongRequest, Song}
import co.adhoclabs.template.models.Genre._
import java.util.UUID

import co.adhoclabs.model.ErrorResponse
import co.adhoclabs.template.exceptions.AlbumNotCreatedException

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
    it("should call AlbumManager.get") {
      (albumManager.get _)
        .expects(expectedAlbumWithSongs.album.id)
        .returning(Future.successful(Some(expectedAlbumWithSongs)))

      Get(s"/albums/${expectedAlbumWithSongs.album.id}") ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.OK)
        assert(responseAs[AlbumWithSongs] == expectedAlbumWithSongs)
      }
    }
  }

  describe("PUT /albums/:id") {
    it("should call AlbumManager.update") {
      (albumManager.update _)
          .expects(expectedAlbumWithSongs.album)
          .returning(Future.successful(Some(expectedAlbumWithSongs.album)))

      Put(s"/albums/${expectedAlbumWithSongs.album.id}", HttpEntity(`application/json`, s"""${expectedAlbumWithSongs.album.toJson}""")) ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.OK)
        assert(responseAs[Album] == expectedAlbumWithSongs.album)
      }
    }
  }

  describe("POST /albums") {
    it("should call AlbumManager.create and return a 201 Created response when creation is successful") {
      (albumManager.create _)
          .expects(createAlbumRequest)
          .returning(Future.successful(expectedAlbumWithSongs))

      Post(s"/albums", HttpEntity(`application/json`, s"""${createAlbumRequest.toJson}""")) ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.Created)
        assert(responseAs[AlbumWithSongs] == expectedAlbumWithSongs)
      }
    }

    it("should call AlbumManager.create and return a 500 response when creation is not successful") {
      (albumManager.create _)
          .expects(createAlbumRequest)
          .throwing(AlbumNotCreatedException(expectedAlbumWithSongs.album))

      Post(s"/albums", HttpEntity(`application/json`, s"""${createAlbumRequest.toJson}""")) ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.InternalServerError)
      }
    }
  }
}
