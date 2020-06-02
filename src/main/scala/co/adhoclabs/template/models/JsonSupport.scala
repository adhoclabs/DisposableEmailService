package co.adhoclabs.template.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val songFormat: RootJsonFormat[Song] = jsonFormat4(Song)
  implicit val albumFormat: RootJsonFormat[Album] = jsonFormat3(Album.apply)
  implicit val createAlbumFormat: RootJsonFormat[CreateAlbumRequest] = jsonFormat2(CreateAlbumRequest)
}
