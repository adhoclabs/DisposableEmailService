package co.adhoclabs.template.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import co.adhoclabs.model.ErrorResponse
import co.adhoclabs.template.MainZio
import co.adhoclabs.template.apiz.{AlbumRoutes, ApiZ, HealthEndpoint, HealthRoutes, SongRoutes}
import co.adhoclabs.template.exceptions.{AlbumAlreadyExistsException, AlbumNotCreatedException, NoSongsInAlbumException}
import co.adhoclabs.template.models.{Album, AlbumWithSongs, CreateAlbumRequest, PatchAlbumRequest}
import spray.json._
import zio.{Scope, ZLayer}
import zio.http.Header.Authorization
import zio.http.Server.Config
import zio.http.{Client, Driver, Request, TestServer, URL}
import zio.http.endpoint.{EndpointExecutor, EndpointLocator}

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

      import zio.{Unsafe, ZIO, http}
      import zio.{Unsafe, ZIO, http}

      implicit val albumbRoutes = AlbumRoutes()
      implicit val songRoutes = SongRoutes()
      implicit val healthRoutes = HealthRoutes()

      val zioRoutes = ApiZ().zioRoutes
      val app = zioRoutes.toHttpApp

      val locator =
        EndpointLocator.fromURL(URL.decode("http://localhost:8080").toOption.get)

      val runtime = zio.Runtime.default
      val zioRes =
        Unsafe.unsafe { implicit unsafe =>
          runtime.unsafe.run {
            (for {
              client <- ZIO.service[Client]
              _ <- {
                TestServer.addRoutes(zioRoutes)
              }
              res <- {
                val executor: EndpointExecutor[Unit] =
                  EndpointExecutor(client, locator, ZIO.succeed(()))

                executor(HealthEndpoint.api())
                //                              MainZio.app()

              }
            } yield {
              res
            }).provide(Client.default, Scope.default, TestServer.layer, Driver.default, ZLayer.succeed(Config.default))
          }
        }

      println("zioRes: " + zioRes)

      Get(s"/albums/${expectedAlbumWithSongs.album.id}") ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.OK)
        assert(responseAs[AlbumWithSongs] == expectedAlbumWithSongs)
      }
    }

    it("should call AlbumManager.get and return a 404 when album doesn't exist") {
      (albumManager.getWithSongs _)
        .expects(expectedAlbumWithSongs.album.id)
        .returning(Future.successful(None))

      Get(s"/albums/${expectedAlbumWithSongs.album.id}") ~> Route.seal(routes) ~> check {
        assert(status == StatusCodes.NotFound)
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
