package co.adhoclabs.email.business

import co.adhoclabs.email.models.BurnerEmailAddress
import zio.schema.{DeriveSchema, Schema}

import java.util.UUID
import scala.concurrent.Future

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

case class EmailManagerImpl() extends EmailManager {

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
