package co.adhoclabs.template.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val song: RootJsonFormat[Song] = jsonFormat2(Song)
}
