package co.adhoclabs.template.api

import java.time.Instant

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import co.adhoclabs.template.models._
import java.util.UUID

import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object UuidJsonFormat extends JsonFormat[UUID] {
    def write(uuid: UUID) = JsString(uuid.toString)
    def read(value: JsValue): UUID = value match {
      case JsString(s) => UUID.fromString(s)
      case _ => throw DeserializationException("Expected UUID as JsString, but got " + value)
    }
  }

  implicit object InstantJsonFormat extends JsonFormat[Instant] {
    def write(instant: Instant) = JsString(instant.toString)
    def read(value: JsValue): Instant = value match {
      case JsString(s) => Instant.parse(s)
      case _ => throw DeserializationException("Expected Instant as JsString, but got " + value)
    }
  }

  implicit val songFormat: RootJsonFormat[Song] = jsonFormat6(Song.apply)
  implicit val createSongFormat: RootJsonFormat[CreateSongRequest] = jsonFormat3(CreateSongRequest.apply)
  implicit val albumFormat: RootJsonFormat[Album] = jsonFormat5(Album.apply)
  implicit val createAlbumRequestFormat: RootJsonFormat[CreateAlbumRequest] = jsonFormat3(CreateAlbumRequest.apply)
  implicit val patchAlbumRequestFormat: RootJsonFormat[PatchAlbumRequest] = jsonFormat2(PatchAlbumRequest.apply)
  implicit val albumWithSongsFormat: RootJsonFormat[AlbumWithSongs] = jsonFormat2(AlbumWithSongs.apply)
}
