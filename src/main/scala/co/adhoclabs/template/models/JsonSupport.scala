package co.adhoclabs.template.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import java.util.UUID
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object UuidJsonFormat extends JsonFormat[UUID] {
    def write(x: UUID) = JsString(x toString ())
    def read(value: JsValue): UUID = value match {
      case JsString(x) => UUID.fromString(x)
      case x => throw DeserializationException("Expected UUID as JsString, but got " + x)
    }
  }

  implicit val songFormat: RootJsonFormat[Song] = jsonFormat4(Song.apply)
  implicit val createSongFormat: RootJsonFormat[CreateSongRequest] = jsonFormat3(CreateSongRequest.apply)
  implicit val albumFormat: RootJsonFormat[Album] = jsonFormat3(Album.apply)
  implicit val createAlbumFormat: RootJsonFormat[CreateAlbumRequest] = jsonFormat2(CreateAlbumRequest)

}
