package co.adhoclabs.template.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import co.adhoclabs.model.{EmptyResponse, ErrorResponse}
import co.adhoclabs.template.exceptions.{AlbumAlreadyExistsException, AlbumNotCreatedException, NoSongsInAlbumException}
import co.adhoclabs.template.models.{Album, AlbumWithSongs, CreateAlbumRequest, PatchAlbumRequest}
import spray.json._
import zio.http.{Body, Request, Status}
import zio.schema.codec.JsonCodec.schemaBasedBinaryCodec

import scala.concurrent.Future

class AlbumApiTest extends ApiTestBase {

  val expectedAlbumWithSongs = generateAlbumWithSongs()

  val createAlbumRequest = CreateAlbumRequest(
    title   = expectedAlbumWithSongs.album.title,
    artists = expectedAlbumWithSongs.album.artists,
    genre   = expectedAlbumWithSongs.album.genre,
    songs   = expectedAlbumWithSongs.songs.map(_.title)
  )

  describe("GET /albums/:id") {
    it("should call AlbumManager.get and return a 200 with an album with songs body when album exists") {
      (albumManager.getWithSongs _)
        .expects(expectedAlbumWithSongs.album.id)
        .returning(Future.successful(Some(expectedAlbumWithSongs)))

      provokeServerSuccess[AlbumWithSongs](
        app,
        Request.get(s"albums/${expectedAlbumWithSongs.album.id}"),
        Status.Created,
        _ == expectedAlbumWithSongs
      )

    }

    it("should call AlbumManager.get and return a 404 when album doesn't exist") {
      (albumManager.getWithSongs _)
        .expects(expectedAlbumWithSongs.album.id)
        .returning(Future.successful(None))

      provokeServerFailure(
        app,
        Request.get(s"albums/${expectedAlbumWithSongs.album.id}"),
        expectedStatus = Status.NotFound,
        _ == ErrorResponse("Could not find album!")
      )
    }
  }

  describe("PATCH /albums/:id") {
    val patchRequest = PatchAlbumRequest(
      title   = Some(expectedAlbumWithSongs.album.title),
      artists = Some(expectedAlbumWithSongs.album.artists),
      genre   = Some(expectedAlbumWithSongs.album.genre)
    )

    it("should call AlbumManager.update and return updated song when it exists") {
      (albumManager.patch _)
        .expects(expectedAlbumWithSongs.album.id, patchRequest)
        .returning(Future.successful(Some(expectedAlbumWithSongs.album)))

      provokeServerSuccess[Album](
        app,
        Request.patch(s"albums/${expectedAlbumWithSongs.album.id}", body = Body.from(expectedAlbumWithSongs.album)),
        Status.Ok,
        _ == expectedAlbumWithSongs.album
      )

    }

    it("should call AlbumManager.update and return a 404 when album doesn't exist") {
      (albumManager.patch _)
        .expects(expectedAlbumWithSongs.album.id, patchRequest)
        .returning(Future.successful(None))

      provokeServerFailure(
        app,
        Request.patch(s"albums/${expectedAlbumWithSongs.album.id}", body = Body.from(expectedAlbumWithSongs.album)),
        expectedStatus = Status.NotFound
      )

    }
  }

  describe("POST /albums") {
    it("should call AlbumManager.create and return a 201 Created response when creation is successful") {
      (albumManager.create _)
        .expects(createAlbumRequest)
        .returning(Future.successful(expectedAlbumWithSongs))

      provokeServerSuccess[AlbumWithSongs](
        app,
        Request.post(s"/albums", body = Body.from(createAlbumRequest)),
        expectedStatus   = Status.Created,
        payloadAssertion = _ == expectedAlbumWithSongs
      )

    }

    it("should call AlbumManager.create and return a 500 response when creation is not successful") {
      (albumManager.create _)
        .expects(createAlbumRequest)
        .throwing(AlbumNotCreatedException(expectedAlbumWithSongs.album))

      provokeServerFailure(
        app,
        Request.post(s"albums", body = Body.from(createAlbumRequest)),
        expectedStatus = Status.InternalServerError
      )
    }

    it("should call AlbumManager.create and return a 400 response when album already exists") {
      (albumManager.create _)
        .expects(createAlbumRequest)
        .throwing(AlbumAlreadyExistsException("album already exists"))

      val requestEntity = HttpEntity(`application/json`, s"""${createAlbumRequest.toJson}""")

      provokeServerFailure(
        app,
        Request.post(s"albums", body = Body.from(createAlbumRequest)),
        expectedStatus = Status.BadRequest,
        _.error == s"album already exists"
      )

      //      Post(s"/albums", requestEntity) ~> Route.seal(routes) ~> check {
      //        assert(status == StatusCodes.BadRequest)
      //        assert(responseAs[ErrorResponse].error == s"album already exists")
      //      }
    }

    it("should return a 400 response when the album has no songs") {
      val createAlbumRequestNoSongs = createAlbumRequest.copy(songs = List.empty[String])

      (albumManager.create _)
        .expects(createAlbumRequestNoSongs)
        .throwing(NoSongsInAlbumException(createAlbumRequestNoSongs))

      val requestEntity = HttpEntity(`application/json`, s"""${createAlbumRequestNoSongs.toJson}""")

      provokeServerFailure(
        app,
        Request.post(s"albums", body = Body.from(createAlbumRequest)),
        expectedStatus = Status.InternalServerError,
        error => error.error == s"Not creating album entitled ${createAlbumRequest.title} because it had no songs."
      )

      //      Post(s"/albums", requestEntity) ~> Route.seal(routes) ~> check {
      //        assert(status == StatusCodes.BadRequest)
      //        assert(responseAs[ErrorResponse].error == s"Not creating album entitled ${createAlbumRequest.title} because it had no songs.")
      //      }
    }

    describe("DELETE /albums/:id") {
      it("should call AlbumManager.delete and return an empty 204") {
        (albumManager.delete _)
          .expects(expectedAlbumWithSongs.album.id)
          .returning(Future.successful(()))

        import Schemas.schema
        provokeServerSuccess[EmptyResponse](
          app,
          Request.delete(s"/albums/${expectedAlbumWithSongs.album.id}"),
          expectedStatus = Status.NoContent
        )

        //        Delete(s"/albums/${expectedAlbumWithSongs.album.id}") ~> Route.seal(routes) ~> check {
        //          assert(status == StatusCodes.NoContent)
        //        }
      }
    }
  }
}
