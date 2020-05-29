package co.adhoclabs.template.models

import spray.json.{DefaultJsonProtocol, DeserializationException, JsNumber, JsValue, RootJsonFormat}

object Genre extends Enumeration with DefaultJsonProtocol {
  val Rock = Value(0)
  val HipHop = Value(1)
  val Classical = Value(2)
  val Pop = Value(3)

  type Genre = Value

  // Manually defining the RootJsonFormat for this enum
  implicit object GenreFormat extends RootJsonFormat[Genre] {
    def write(genre: Genre): JsNumber = JsNumber(genre.id)
    def read(value: JsValue): Genre = {
      value match {
        case JsNumber(genre) => try { Genre(genre.toInt) } catch {
          case e: Throwable => throw DeserializationException(s"No corresponding Genre exists for id $genre.", e)
        }
        case jsValue => throw DeserializationException(s"Unable to parse value [$jsValue] supplied for Genre")
      }
    }
  }
}
