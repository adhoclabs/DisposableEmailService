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

  def create(userId: UserId, emailMessageId: BurnerEmailMessageId): BurnerEmailMessage = {
    BurnerEmailMessage(
      id = emailMessageId,
      source = "source",
      to =
        List(
          userId.id.toString
        ),
      from =
        List(
          "someExternalSender@mail.hardcoded"
        ),
      subject = "subject",
      plainBodyDownloadUrl =
        Some(
          "https://dev-burner-email-parsed-prototype.s3.us-west-2.amazonaws.com/ec324f4f-b4fb-4fde-98ec-80cb472121f3/body.txt?X-Amz-Security-Token=FwoGZXIvYXdzELD%2F%2F%2F%2F%2F%2F%2F%2F%2F%2FwEaDJhqGUy5G4n7%2BX4p0CKGAXPNIJAAB%2BVN5Vsq%2BVU5zbDyhHg1MsHI%2BYOigR%2Fh1Ai8wV%2BkwPYBktQsuBee8ADrO7zBb8pZt3dpe3gcqkcxarM%2Fx2zeYwMvRKgJYe1roSpKwW08Bbrq1ZC9ROaG%2BGK%2BqGbLWvoy%2FCQbLMgujgi8pW0ih6v3f%2FGG%2Bm3hiBVF5BgL755SHk%2BPKNPI%2F7AGMiiSLkjpBiEICkWQ4W%2BUOgGydI7yyjDKaUAGnjUROV9L8XwerJKFkftw&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20240417T150155Z&X-Amz-SignedHeaders=host&X-Amz-Expires=604799&X-Amz-Credential=ASIA2MPTIHUQHLXSJ7GG%2F20240417%2Fus-west-2%2Fs3%2Faws4_request&X-Amz-Signature=e22fa44b28ca7eb1a92e4a2030187419d600b52ee485e1a30528fbbc4e5ea2e9"
        ),
      htmlBodyDownloadUrl =
        Some(
          "https://dev-burner-email-parsed-prototype.s3.us-west-2.amazonaws.com/ec324f4f-b4fb-4fde-98ec-80cb472121f3/body.html?X-Amz-Security-Token=FwoGZXIvYXdzELD%2F%2F%2F%2F%2F%2F%2F%2F%2F%2FwEaDJhqGUy5G4n7%2BX4p0CKGAXPNIJAAB%2BVN5Vsq%2BVU5zbDyhHg1MsHI%2BYOigR%2Fh1Ai8wV%2BkwPYBktQsuBee8ADrO7zBb8pZt3dpe3gcqkcxarM%2Fx2zeYwMvRKgJYe1roSpKwW08Bbrq1ZC9ROaG%2BGK%2BqGbLWvoy%2FCQbLMgujgi8pW0ih6v3f%2FGG%2Bm3hiBVF5BgL755SHk%2BPKNPI%2F7AGMiiSLkjpBiEICkWQ4W%2BUOgGydI7yyjDKaUAGnjUROV9L8XwerJKFkftw&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20240417T150155Z&X-Amz-SignedHeaders=host&X-Amz-Expires=604799&X-Amz-Credential=ASIA2MPTIHUQHLXSJ7GG%2F20240417%2Fus-west-2%2Fs3%2Faws4_request&X-Amz-Signature=7b059ce0d1ceb6ba98db0560f9d70b631da64b327e18b5b79dc062d424518483"
        ),
      receivedAt = Instant.now()
    )
  }
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
