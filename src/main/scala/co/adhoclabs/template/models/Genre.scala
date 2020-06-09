package co.adhoclabs.template.models

import spray.json.{DefaultJsonProtocol, DeserializationException, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}

object Genre extends Enumeration with DefaultJsonProtocol {
  val NoGenre = Value
  val Rock = Value
  val HipHop = Value
  val Classical = Value
  val Pop = Value

  type Genre = Value

  // Manually defining the RootJsonFormat for this enum because since this isn't a case class, it doesn't have a
  // default json format

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