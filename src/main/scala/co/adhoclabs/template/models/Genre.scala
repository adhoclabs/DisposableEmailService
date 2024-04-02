package co.adhoclabs.template.models

import spray.json.{
  DefaultJsonProtocol,
  DeserializationException,
  JsNumber,
  JsObject,
  JsString,
  JsValue,
  RootJsonFormat
}

object Genre extends Enumeration with DefaultJsonProtocol {
  val NoGenre   = Value
  val Rock      = Value
  val HipHop    = Value
  val Classical = Value
  val Pop       = Value

  type Genre = Value

  // Manually defining the RootJsonFormat for this enum because since this isn't a case class, it doesn't have a
  // default json format

  implicit object GenreFormat extends RootJsonFormat[Genre] {
    def enumClassName: String = "Genre"

    def write(genre: Genre): JsObject =
      JsObject(("id", JsNumber(genre.id)), ("name", JsString(genre.toString)))

    def read(jsValue: JsValue): Genre = {

      def tryGetValueFromInt(id: Int): Value =
        try { Genre(id) }
        catch {
          case e: Throwable =>
            throw DeserializationException(s"No corresponding $enumClassName value exists for id $id.", e)
        }

      def tryGetValueFromString(name: String): Value =
        values.find(_.toString == name) match {
          case Some(genre: Genre) => genre
          case None               =>
            throw DeserializationException(s"No corresponding $enumClassName value exists for name $name.")
        }

      def tryGetValueFromObject(jsValue: JsValue): Value =
        jsValue.asJsObject.getFields("id") match {
          case Seq(JsNumber(id)) => tryGetValueFromInt(id.toIntExact)
          case _                 =>
            throw DeserializationException(s"Unable to parse value $jsValue supplied for $enumClassName")
        }

      // This implementation of `read` first attempts to read a plain numeric ID in the genre field,
      // then tries to find the genre via its string name, and finally attempts to read a numeric id
      // out of an object and find the genre using that.
      // This might be more generous than you want to be with how you expect clients to send you enum values.
      jsValue match {
        case JsNumber(id)   => tryGetValueFromInt(id.toIntExact)
        case JsString(name) => tryGetValueFromString(name)
        case _              => tryGetValueFromObject(jsValue)
      }
    }
  }
}
