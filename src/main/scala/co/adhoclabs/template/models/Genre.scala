package co.adhoclabs.template.models

import co.adhoclabs.template.models.Genre.Genre
import spray.json.{DefaultJsonProtocol, DeserializationException, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}

object Genre extends Enumeration with DefaultJsonProtocol {
  val Rock = Value(0)
  val HipHop = Value(1)
  val Classical = Value(2)
  val Pop = Value(3)

  type Genre = Value

  // Manually defining the RootJsonFormat for this enum because since this isn't a case class, it doesn't have a
  // default json format

  // TODO: can we genericize this?
  implicit object GenreFormat extends RootJsonFormat[Genre] {
    def enumClassName: String = "Genre"
    
    def write(genre: Genre): JsObject = JsObject(("id", JsNumber(genre.id)), ("name", JsString(genre.toString)))

    def read(jsValue: JsValue): Genre = {

      def tryGetValue(id: Int) = try { Genre(id) } catch {
        case e: Throwable => throw DeserializationException(s"No corresponding $enumClassName value exists for id $id.", e)
      }

      jsValue match {
        case JsNumber(id) => tryGetValue(id.toIntExact)
        case _ =>
          val jsObject = jsValue.asJsObject
          jsObject.getFields("id") match {
            case Seq(JsNumber(id)) => tryGetValue(id.toIntExact)
            case _ => throw DeserializationException(s"Unable to parse value $jsValue supplied for $enumClassName")
          }
      }
    }
  }
}