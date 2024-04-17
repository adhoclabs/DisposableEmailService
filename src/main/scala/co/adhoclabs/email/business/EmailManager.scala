package co.adhoclabs.email.business

import co.adhoclabs.model.Voicemail.jsonFormat2
import co.adhoclabs.model.{Conversation, MessageId, Voicemail}
import co.adhoclabs.email.models.BurnerEmailAddress
import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat, RootJsonFormat}
import zio.schema.{DeriveSchema, Schema}

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

case class UserId(
  id: UUID
)

object UserId extends DefaultJsonProtocol {
  implicit val uuidFormat                         = BuiltInFormat.uuidJsonFormat
  implicit val jsonFormat: RootJsonFormat[UserId] = jsonFormat1(UserId.apply)

  implicit val schema: Schema[UserId] = DeriveSchema.gen
}

case class BurnerEmailMessageId(
  id: UUID
)

object BurnerEmailMessageId extends DefaultJsonProtocol {
  implicit val uuidFormat                                       = BuiltInFormat.uuidJsonFormat
  implicit val jsonFormat: RootJsonFormat[BurnerEmailMessageId] = jsonFormat1(BurnerEmailMessageId.apply)

  implicit val schema: Schema[BurnerEmailMessageId] = DeriveSchema.gen
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

  implicit val schema: Schema[BurnerEmailMessage] = DeriveSchema.gen
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
      messageId: BurnerEmailMessageId
  ): Future[Unit]

  def archiveMessage(
      messageId: BurnerEmailMessageId
  ): Future[Unit]
}

case class EmailManagerImpl() extends EmailManager {

  override def createBurnerEmailAddress(burnerEmail: BurnerEmailAddress): Either[String, BurnerEmailAddress] =
    ???

  override def getConversations(
      userId: String,
      burnerEmailO: Option[BurnerEmailAddress],
      pageO: Option[Int],
      pageSizeO: Option[Int]
  ): Future[List[BurnerEmailMessage]] = ???

  override def deleteMessage(messageId: BurnerEmailMessageId): Future[Unit] = ???

  override def archiveMessage(messageId: BurnerEmailMessageId): Future[Unit] = ???
}
