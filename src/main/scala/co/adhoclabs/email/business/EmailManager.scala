package co.adhoclabs.email.business

import co.adhoclabs.email.models.BurnerEmailAddress
import zio.{Ref, ZLayer}
import zio.schema.{DeriveSchema, Schema}

import java.time.Instant
import java.util.UUID
import scala.concurrent.Future

case class EmailFromLambda(
  burnerEmailAddress: BurnerEmailAddress
)

case class UserId(
  id: UUID
)

object UserId {
  implicit val schema: Schema[UserId] = Schema.primitive[UUID].transform(UserId(_), _.id)
}

case class BurnerEmailMessageId(
  id: UUID
)

object BurnerEmailMessageId {
  implicit val schema: Schema[BurnerEmailMessageId] = {
    Schema.primitive[UUID].transform(BurnerEmailMessageId(_), _.id)
  }
}

case class BurnerEmailMessage(
  id:                   BurnerEmailMessageId,
  source:               String,
  to:                   List[String],
  from:                 List[String],
//                  cc: List[String],
//                  bcc: List[String],
  subject:              String,
//                  attachments: List[Attachment],
  plainBodyDownloadUrl: Option[String],
  htmlBodyDownloadUrl:  Option[String],
  receivedAt:           Instant
)

case class BurnerEmailMessageOld(
  messageId: BurnerEmailMessageId,
  userId:    UserId,
  //  burnerId:     String,
  //  conversation: Conversation,
  //  dateCreated:  Instant,
  //  read:         Boolean,
  //  sid:          Option[String],
  content:   String,
  subject:   String
)
object BurnerEmailMessage   {
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

case class Inbox(burnerEmailAddress: BurnerEmailAddress, messages: List[BurnerEmailMessage])

case class EmailManagerImpl(
  emails: Ref[Map[UserId, List[Inbox]]]
) extends EmailManager {

  override def createBurnerEmailAddress(
      burnerEmail: BurnerEmailAddress
  ): Either[String, BurnerEmailAddress] = {
    Right(burnerEmail)
  }

  override def getConversations(
      userId: String,
      burnerEmailO: Option[BurnerEmailAddress],
      pageO: Option[Int],
      pageSizeO: Option[Int]
  ): Future[List[BurnerEmailMessage]] = ???

  override def deleteMessage(messageId: BurnerEmailMessageId): Future[Unit] = ???

  override def archiveMessage(messageId: BurnerEmailMessageId): Future[Unit] = ???
}

object EmailManager {
  val layer = {
    ZLayer.fromZIO(
      Ref.make(Map.empty[UserId, List[Inbox]]).map(EmailManagerImpl)
    )
  }
}
