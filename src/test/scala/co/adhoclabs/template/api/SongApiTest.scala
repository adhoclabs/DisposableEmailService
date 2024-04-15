package co.adhoclabs.template.api

import co.adhoclabs.template.exceptions.SongAlreadyExistsException
import co.adhoclabs.template.models.{CreateSongRequest, Song}
import zio.http.{Body, Request, Status}
import zio.schema.codec.JsonCodec.schemaBasedBinaryCodec

import scala.concurrent.Future

class SongApiTest extends ApiTestBase {
  val expectedSong: Song = generateSong(generateAlbum().id, 1)

  val createSongRequest: CreateSongRequest = CreateSongRequest(
    title         = expectedSong.title,
    albumId       = expectedSong.albumId,
    albumPosition = expectedSong.albumPosition
  )

  describe("GET /songs/:id") {
    it("should call SongManager.get and return a 200 response with a song body when song exists") {

      (songManager.get _)
        .expects(expectedSong.id)
        .returning(Future.successful(Some(expectedSong)))

      provokeServerSuccess[Song](
        app,
        Request.get(s"/songs/${expectedSong.id}"),
        expectedStatus   = Status.Ok,
        payloadAssertion = _ == expectedSong
      )
    }

    it("should call SongManager.get and return a 404 response song doesn't exist") {
      (songManager.get _)
        .expects(expectedSong.id)
        .returning(Future.successful(None))

      provokeServerFailure(
        app,
        Request.get(s"/songs/${expectedSong.id}"),
        expectedStatus = Status.NotFound,
      )
    }
  }

  describe("POST /songs") {

    it("should call SongManager.create and return created song with ID when successful") {
      (songManager.create _)
        .expects(createSongRequest)
        .returning(Future.successful(expectedSong))

      provokeServerSuccess[Song](
        app,
        Request.post(s"/songs", body = Body.from(createSongRequest)),
        expectedStatus   = Status.Created,
        payloadAssertion = _ == expectedSong
      )
    }

    it("should call SongManager.create and return a 400 response when creation is not successful") {
      (songManager.create _)
        .expects(createSongRequest)
        .throwing(SongAlreadyExistsException("Song not created"))

      provokeServerFailure(
        app,
        Request.post(s"/songs", body = Body.from(createSongRequest)),
        expectedStatus = Status.BadRequest,
        errorAssertion = _.error == s"Song not created"
      )
    }
  }

  describe("PUT /songs/:songId") {
    it("should call SongManager.update and return a 200 with an update song when it exists") {
      (songManager.update _)
        .expects(expectedSong)
        .returning(Future.successful(Some(expectedSong)))

      provokeServerSuccess[Song](
        app,
        Request.put(s"/songs/${expectedSong.id}", body = Body.from(expectedSong)),
        expectedStatus   = Status.Ok,
        payloadAssertion = _ == expectedSong
      )
    }

    it("should call SongManager.update and return a 404 when song doesn't exist") {
      (songManager.update _)
        .expects(expectedSong)
        .returning(Future.successful(None))

      provokeServerFailure(
        app,
        Request.put(s"/songs/${expectedSong.id}", body = Body.from(expectedSong)),
        expectedStatus = Status.NotFound
      )
    }
  }

  describe("DELETE /songs/:songId") {
    it("should call SongManager.delete and return empty 204") {
      (songManager.delete _)
        .expects(expectedSong.id)
        .returning(Future.successful(()))

      provokeServerSuccess[Unit](
        app,
        Request.delete(s"/songs/${expectedSong.id}"),
        expectedStatus = Status.NoContent,
      )
    }
  }
}
