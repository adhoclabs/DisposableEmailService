package co.adhoclabs.template.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import co.adhoclabs.model.ErrorResponse
import co.adhoclabs.template.MainZio
import co.adhoclabs.template.apiz.{AlbumEndpoints, AlbumRoutes, ApiZ, HealthEndpoint, HealthRoutes, SongRoutes}
import co.adhoclabs.template.exceptions.{AlbumAlreadyExistsException, AlbumNotCreatedException, NoSongsInAlbumException}
import co.adhoclabs.template.models.{Album, AlbumWithSongs, CreateAlbumRequest, PatchAlbumRequest}
import spray.json._
import zio.{Cause, Exit, Scope, Unsafe, ZIO, ZLayer}
import zio.http.Header.Authorization
import zio.http.Server.Config
import zio.http.{Client, Driver, Request, Status, TestServer, URL}
import zio.http.endpoint.{EndpointExecutor, EndpointLocator, EndpointMiddleware, Invocation}
import zio.schema.codec.JsonCodec.schemaBasedBinaryCodec
import zio.schema.{DynamicValue, Schema}

import java.io
import java.util.UUID
import scala.concurrent.Future

class AlbumApiTest extends ApiTestBase {

  val expectedAlbumWithSongs = generateAlbumWithSongs()

  val createAlbumRequest = CreateAlbumRequest(
    title   = expectedAlbumWithSongs.album.title,
    artists = expectedAlbumWithSongs.album.artists,
    genre   = expectedAlbumWithSongs.album.genre,
    songs   = expectedAlbumWithSongs.songs.map(_.title)
  )

  implicit val albumbRoutes = AlbumRoutes()
  implicit val songRoutes = SongRoutes()
  implicit val healthRoutes = HealthRoutes()

  val zioRoutes = ApiZ().zioRoutes
  val app = zioRoutes.toHttpApp

  def invokeZioRequest[T: Schema](request: Request): Either[ErrorResponse, (Status, T)] = {
    val runtime = zio.Runtime.default
    Unsafe.unsafe { implicit unsafe =>
      runtime.unsafe.run {
        (for {
          _ <- ZIO.debug("going to make request")
          response <- app.apply(request)
          _ <- ZIO.when(response.status == Status.NotFound)(
            ZIO.fail(ErrorResponse("Not found!"))
          )
          res <- response.body.to[T]
        } yield (response.status, res))
          .mapError {
            case er: ErrorResponse => er
            case other             => ErrorResponse(other.toString)
          }
      }
    } match {
      case Exit.Success(value) => Right(value)
      case Exit.Failure(cause) =>
        println("Exit failure: " + cause)
        cause match {
          case Cause.Empty => ???
          case Cause.Fail(value, trace) =>
            println("fail")
            Left(value)
          case Cause.Die(value, trace) =>
            println("die")
            ???
          case Cause.Interrupt(fiberId, trace)   => ???
          case Cause.Stackless(cause, stackless) => ???
          case Cause.Then(left, right)           => ???
          case Cause.Both(left, right)           => ???
        }

      //        Left(cause.failureOrCause.left.get)
      //        Left(cause.failureOption.get)

    }
  }

  describe("GET /albums/:id") {
    it("should call AlbumManager.get and return a 200 with an album with songs body when album exists") {
      (albumManager.getWithSongs _)
        .expects(expectedAlbumWithSongs.album.id)
        .returning(Future.successful(Some(expectedAlbumWithSongs)))

      val zioResSimple =
        invokeZioRequest[AlbumWithSongs](Request.get(s"albums/${expectedAlbumWithSongs.album.id}"))

      zioResSimple match {
        case Right((statusCode, body)) =>
          assert(statusCode == Status.Created)
          assert(body == expectedAlbumWithSongs)
        case Left(cause) => ???
      }

    }

    it("should call AlbumManager.get and return a 404 when album doesn't exist") {
      (albumManager.getWithSongs _)
        .expects(expectedAlbumWithSongs.album.id)
        .returning(Future.successful(None))

      val zioResSimple =
        invokeZioRequest[AlbumWithSongs](Request.get(s"albums/${expectedAlbumWithSongs.album.id}"))

      zioResSimple match {
        case Right((statusCode, body)) =>
          println("Right")
          assert(statusCode == Status.Created)
          assert(body == expectedAlbumWithSongs)
        case Left(cause) =>
          println("Left: " + cause)
          succeed
      }
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

      val requestEntity = HttpEntity(`application/json`, s"""${expectedAlbumWithSongs.album.toJson}""")

      Patch(s"/albums/${expectedAlbumWithSongs.album.id}", requestEntity) ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.OK)
        assert(responseAs[Album] == expectedAlbumWithSongs.album)
      }
    }

    it("should call AlbumManager.update and return a 404 when album doesn't exist") {
      (albumManager.patch _)
        .expects(expectedAlbumWithSongs.album.id, patchRequest)
        .returning(Future.successful(None))

      val requestEntity = HttpEntity(`application/json`, s"""${expectedAlbumWithSongs.album.toJson}""")

      Patch(s"/albums/${expectedAlbumWithSongs.album.id}", requestEntity) ~> Route.seal(routes) ~> check {
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
        assert(responseAs[ErrorResponse].error == s"album already exists")
      }
    }

    it("should return a 400 response when the album has no songs") {
      val createAlbumRequestNoSongs = createAlbumRequest.copy(songs = List.empty[String])

      (albumManager.create _)
        .expects(createAlbumRequestNoSongs)
        .throwing(NoSongsInAlbumException(createAlbumRequestNoSongs))

      val requestEntity = HttpEntity(`application/json`, s"""${createAlbumRequestNoSongs.toJson}""")

      Post(s"/albums", requestEntity) ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.BadRequest)
        assert(responseAs[ErrorResponse].error == s"Not creating album entitled ${createAlbumRequest.title} because it had no songs.")
      }
    }

    describe("DELETE /albums/:id") {
      it("should call AlbumManager.delete and return an empty 204") {
        (albumManager.delete _)
          .expects(expectedAlbumWithSongs.album.id)
          .returning(Future.successful(()))

        Delete(s"/albums/${expectedAlbumWithSongs.album.id}") ~> Route.seal(routes) ~> check {
          assert(status == StatusCodes.NoContent)
        }
      }
    }
  }
}
