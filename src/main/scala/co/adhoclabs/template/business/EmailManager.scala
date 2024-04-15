package co.adhoclabs.template.business

import co.adhoclabs.model.Voicemail.jsonFormat2
import co.adhoclabs.model.{Conversation, MessageId, Voicemail}
import co.adhoclabs.template.models.BurnerEmailAddress
import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat, RootJsonFormat}

import java.time.Instant
import java.util.UUID
import scala.concurrent.Future

object BuiltInFormat {

  implicit val uuidJsonFormat =
    new JsonFormat[UUID] {
      def write(m: UUID)      = JsString(m.toString)
      def read(json: JsValue) =
        json match {
          case JsString(s) =>
            try {
              UUID.fromString(s)
            } catch {
              case ex: Exception =>
                throw new IllegalArgumentException("Invalid UUID: " + json.prettyPrint)
            }

          case _ => throw new IllegalArgumentException("Invalid UUID: " + json.prettyPrint)
        }
    }

  implicit val instantJsonFormat =
    new JsonFormat[Instant] {
      def write(m: Instant)   = JsString(m.toString)
      def read(json: JsValue) =
        json match {
          case JsString(s) =>
            try {
              Instant.parse(s)
            } catch {
              case ex: Exception =>
                throw new IllegalArgumentException("Invalid Instant: " + json.prettyPrint)
            }

          case _ => throw new IllegalArgumentException("Invalid Instant: " + json.prettyPrint)
        }
    }
}

case class BurnerEmailMessageId(
  id: UUID
)
object BurnerEmailMessageId extends DefaultJsonProtocol {
  implicit val uuidFormat                                       = BuiltInFormat.uuidJsonFormat
  implicit val jsonFormat: RootJsonFormat[BurnerEmailMessageId] = jsonFormat1(BurnerEmailMessageId.apply)
}

case class BurnerEmailMessage(
  id:           BurnerEmailMessageId,
  userId:       String,
  burnerId:     String,
  conversation: Conversation,
  dateCreated:  Instant,
  read:         Boolean,
  sid:          Option[String],
  content:      String,
  subject:      String
)
object BurnerEmailMessage   extends DefaultJsonProtocol {
  implicit val instantJsonFormat                              = BuiltInFormat.instantJsonFormat
  implicit val jsonFormat: RootJsonFormat[BurnerEmailMessage] = jsonFormat9(BurnerEmailMessage.apply)
}

trait EmailManager {
  def createBurnerEmailAddress(burnerEmail: BurnerEmailAddress): Either[String, BurnerEmailAddress]
  def getConversations(
      userId: String,
      burnerEmailO: Option[BurnerEmailAddress],
      pageO: Option[Int],
      pageSizeO: Option[Int]
  ): Future[List[BurnerEmailMessage]]

  def deleteMessage(
      messageId: MessageId
  ): Future[Unit]

  def archiveMessage(
      messageId: MessageId
  ): Future[Unit]
}
