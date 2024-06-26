package co.adhoclabs.email.business

import co.adhoclabs.email.models.BurnerEmailAddress
import zio.http.{Client, Request}
import zio.{Ref, Scope, ZIO, ZLayer}
import zio.schema.{DeriveSchema, Schema}

import java.time.Instant
import java.util.UUID
import scala.concurrent.Future
import scala.util.Random

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

case class BurnerEmailMessageOutput(
  id:                   BurnerEmailMessageId,
  source:               String,
  to:                   List[BurnerEmailAddress],
  from:                 List[String],
  //                  cc: List[String],
  //                  bcc: List[String],
  subject:              String,
  //                  attachments: List[Attachment],
  plainBodyDownloadUrl: Option[String],
  htmlBodyDownloadUrl:  Option[String],
  receivedAt:           Long,
  read:                 Boolean,
  preview:              Option[String]
)

case class BurnerEmailMessage(
  id:                   BurnerEmailMessageId,
  source:               String,
  to:                   List[BurnerEmailAddress],
  from:                 List[String],
//                  cc: List[String],
//                  bcc: List[String],
  subject:              String,
//                  attachments: List[Attachment],
  plainBodyDownloadUrl: Option[String],
  htmlBodyDownloadUrl:  Option[String],
  receivedAt:           Instant,
  read:                 Boolean = false,
  preview:              Option[String] = None
)

object BurnerEmailMessageOutput {
  implicit val schema: Schema[BurnerEmailMessageOutput]                              = DeriveSchema.gen
  def fromOriginal(burnerEmailMessage: BurnerEmailMessage): BurnerEmailMessageOutput =
    BurnerEmailMessageOutput(
      id = burnerEmailMessage.id,
      source = burnerEmailMessage.source,
      to = burnerEmailMessage.to,
      from = burnerEmailMessage.from,
      subject = burnerEmailMessage.subject,
      plainBodyDownloadUrl = burnerEmailMessage.plainBodyDownloadUrl,
      htmlBodyDownloadUrl = burnerEmailMessage.htmlBodyDownloadUrl,
      receivedAt = burnerEmailMessage.receivedAt.toEpochMilli,
      read = burnerEmailMessage.read,
      preview = burnerEmailMessage.preview
    )
}

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
object BurnerEmailMessage       {
  implicit val schema: Schema[BurnerEmailMessage] = DeriveSchema.gen

  def create(
      userId: UserId,
      burnerEmailAddress: BurnerEmailAddress,
      emailMessageId: BurnerEmailMessageId
  ): BurnerEmailMessage = {
    BurnerEmailMessage(
      id = emailMessageId,
      source = "source",
      to =
        List(
          burnerEmailAddress
        ),
      from =
        List(
          "someExternalSender@mail.hardcoded"
        ),
      subject = "subject",
      plainBodyDownloadUrl =
        Some(
          "https://dev-burner-email-parsed-prototype.s3.us-west-2.amazonaws.com/326dc511-87b2-422e-9f9a-35370a595403/body.txt?response-content-disposition=inline&X-Amz-Security-Token=IQoJb3JpZ2luX2VjELr%2F%2F%2F%2F%2F%2F%2F%2F%2F%2FwEaCXVzLXdlc3QtMiJGMEQCIHDApivadnBbxgKe%2BGki%2FIQUKmdlFURBoPS16i7SQP54AiAQ1HvzlQfLnmq01y3hNNdQrqgg7E5ILmRv6t9RU9EcUiqIAwjz%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F8BEAAaDDcxNDAxMTU4OTkyMCIMeK4mxtNyikcbTXmFKtwCVEADvAfk51axaXLbZaRMaY6GNsSnnz9uKbLg4oVxuOIXB8QmRwznVKG9DHLWf226AIwnNhpxhh6GY6SK0jWrY2ouwjygBpq5LI9%2Be61kXxAQru0loeZMp4j4%2FU2rmORvnt8Lzuul99XEhePyvEwhvGZUj5eGHsmZ6SuggcZR%2B2zlae6GUcTqB2GTXWOqEAJnlTjpdgJYGW%2FaoFjg6DXzvNahaJMmXFInNsGnfiGpotmUHygRuk40EUaATJ%2BxzqD2T4EccCsZtu1Pqcg%2BD%2BjZZAufVBpMOsNlZ9mp8auV7mkYkY9EZNw82EsoJNLWIE4UqsoxWOJDw0fwMH4yyzTGfgTJ%2F3g4hkhbEosRjbjGQDrbrdxEUF9IEdrciNk82CTRrHGlKC59WK0xaugMQFgJ5QJTiY7noS1hUVt%2Bj2Sts%2Byu68xgJc%2FfzmB8lUGoJqe3f6%2FeyZb2Ep2gfVfCMMqPhbEGOrQChgJDotcTj%2B21UeLr80Tlfza7zeqPNYgT4mOeVvBWjGs6mCby%2FeVEu5XLEIlg1qMOV%2BGDD5C7gGZ%2B4xLMfrP95svMYaStmvpmWZsmm08yqaFVVN2QMCJfo8fr%2FzpK1OUQn1hixJeuFMh0wtrYbodT5Zqph1elZ62WCN112rPSX%2BHgBtzjkwoqRv4sdDfHN85olqEgReUqvIbHAaTRjmEJySRp9I3BuDfSbGHX23xEknpTFeAReGsdgHzdEHnG5uKYX2iCOVquSqhnbG1jfdfKQ5pAiMBg8eMxTuUER%2BfQnsn5XUb7e80Ub4PYy5XB4jY0X3arqFXmbwN8spKaOuMn9J3Y8uzl4ZrRtYR8LIGjzAGKBVkumLrWB65OZe%2BkLsnfaZCoWmjdYgjMCgp3waaNXtCVMXs%3D&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20240418T174913Z&X-Amz-SignedHeaders=host&X-Amz-Expires=43200&X-Amz-Credential=ASIA2MPTIHUQHLU5QM33%2F20240418%2Fus-west-2%2Fs3%2Faws4_request&X-Amz-Signature=173d9982fefc09badbb18eeb498deb708adbb4dc0b85445a5d5a9a88278d97b4"
        ),
      htmlBodyDownloadUrl =
        Some(
          "https://dev-burner-email-parsed-prototype.s3.us-west-2.amazonaws.com/ec324f4f-b4fb-4fde-98ec-80cb472121f3/body.html?X-Amz-Security-Token=FwoGZXIvYXdzELD%2F%2F%2F%2F%2F%2F%2F%2F%2F%2FwEaDJhqGUy5G4n7%2BX4p0CKGAXPNIJAAB%2BVN5Vsq%2BVU5zbDyhHg1MsHI%2BYOigR%2Fh1Ai8wV%2BkwPYBktQsuBee8ADrO7zBb8pZt3dpe3gcqkcxarM%2Fx2zeYwMvRKgJYe1roSpKwW08Bbrq1ZC9ROaG%2BGK%2BqGbLWvoy%2FCQbLMgujgi8pW0ih6v3f%2FGG%2Bm3hiBVF5BgL755SHk%2BPKNPI%2F7AGMiiSLkjpBiEICkWQ4W%2BUOgGydI7yyjDKaUAGnjUROV9L8XwerJKFkftw&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20240417T150155Z&X-Amz-SignedHeaders=host&X-Amz-Expires=604799&X-Amz-Credential=ASIA2MPTIHUQHLXSJ7GG%2F20240417%2Fus-west-2%2Fs3%2Faws4_request&X-Amz-Signature=7b059ce0d1ceb6ba98db0560f9d70b631da64b327e18b5b79dc062d424518483"
        ),
      receivedAt = Instant.now(),
      read = Random.nextBoolean(),
      preview = None
    )
  }
}

trait EmailManager {
  def getEmailAddresses(
      userId: UserId
  ): ZIO[Any, String, List[BurnerEmailAddress]]
  def getInbox(
      userId: UserId,
      burnerEmailAddress: BurnerEmailAddress
  ): ZIO[Any, String, List[BurnerEmailMessageOutput]]
  def getInbox(
      userId: UserId
  ): ZIO[Any, String, List[BurnerEmailMessageOutput]]
  def createBurnerEmailAddress(
      burnerEmail: BurnerEmailAddress,
      userId: UserId
  ): ZIO[Any, String, BurnerEmailAddress]

  def deleteMessage(
      messageId: BurnerEmailMessageId
  ): Future[Unit]

  def archiveMessage(
      messageId: BurnerEmailMessageId
  ): Future[Unit]

  def createNewEmailMessage(
      burnerEmailMessage: BurnerEmailMessage
  ): ZIO[Any, String, Unit]
}

case class Inbox(emails: List[BurnerEmailMessage]) {
  def userIsUsingThisEmailAddress(burnerEmailAddress: BurnerEmailAddress) =
    emails.contains(burnerEmailAddress)
}

case class EmailManagerImpl(
  emails:         Ref[Map[BurnerEmailAddress, Inbox]],
  emailAddresses: Ref[Map[UserId, List[BurnerEmailAddress]]]
) extends EmailManager {

  def getInbox(
      userId: UserId,
      burnerEmailAddress: BurnerEmailAddress
  ): ZIO[Any, String, List[BurnerEmailMessageOutput]] =
    for {
      _                     <- ZIO.debug("Decoded email: " + burnerEmailAddress)
      emailsWithoutPreviews <-
        emails.get
          .map(_.getOrElse(burnerEmailAddress, Inbox(List.empty)).emails)
          .map(messages => messages.map(BurnerEmailMessageOutput.fromOriginal))
      emailsWithPreviews    <-
        ZIO.foreach(emailsWithoutPreviews) { message =>
          (for {
            preview <-
              message.plainBodyDownloadUrl match {
                case Some(url) =>
                  getPreview(url).debug("Preview") // HttpClient
                case None      => ZIO.succeed("No preview available")
              }

          } yield message.copy(preview = Some(preview)))
            .provide(Client.default, Scope.default)
            .tapError(ex =>
              ZIO.debug("Error getting preview for messageId: " + message.id + " Error: " + ex.getMessage)
            )
            .orDie // TODO Better error handling here
        }

    } yield emailsWithPreviews

  def getInbox(userId: UserId): ZIO[Any, String, List[BurnerEmailMessageOutput]] = {
    for {
      userEmailAddress <- emailAddresses.get.map(_.getOrElse(userId, List.empty))
      currentEmails    <-
        ZIO.foreach(userEmailAddress) { burnerEmailAddress =>
          getInbox(userId, burnerEmailAddress)
        }
    } yield currentEmails.flatten
  }

  def getPreview(url: String) =
    (for {
      plainText <- Client.request(Request.get(url))
      body      <- plainText.body.asString
    } yield body.take(100)).catchAll(ex => ZIO.succeed("Error downloading preview: " + ex.getMessage))

  def getEmailAddresses(
      userId: UserId
  ): ZIO[Any, String, List[BurnerEmailAddress]] = emailAddresses.get.map(_.getOrElse(userId, List.empty))

  override def createBurnerEmailAddress(
      burnerEmail: BurnerEmailAddress,
      userId: UserId
  ): ZIO[Any, String, BurnerEmailAddress] =
    for {
      existingEmailAddresses <-
        emailAddresses.getAndUpdate(addresses =>
          addresses.get(userId) match {
            case Some(userEmails) =>
              if (userEmails.contains(burnerEmail)) {
                addresses
//                ZIO.fail("User this email.")
              } else
                addresses + (userId -> (List(burnerEmail) ++ userEmails))
            //              ZIO.fail("User already has an email address. Must burn it before making a new one.")
            case None             =>
              addresses + (userId -> List(burnerEmail))
          }
        )
      _                      <-
        existingEmailAddresses.get(userId) match {
          case Some(value) =>
            if (value.contains(burnerEmail))
              ZIO.fail(s"User already has this burner email: $burnerEmail")
            else
              ZIO.unit
          case None        => ZIO.unit
        }
    } yield burnerEmail

  override def deleteMessage(messageId: BurnerEmailMessageId): Future[Unit] = ???

  override def archiveMessage(messageId: BurnerEmailMessageId): Future[Unit] = ???

  override def createNewEmailMessage(burnerEmailMessage: BurnerEmailMessage): ZIO[Any, String, Unit] = {
    for {
      _         <- ZIO.unit
      recipient <-
        burnerEmailMessage.to match {
          case List(oneRecipient) => ZIO.succeed(oneRecipient)
          case other              => ZIO.fail("Only one recipient allowed.")
        }
      _         <- ZIO.debug("Recipient: " + recipient)
      _         <-
        emails.update(emails =>
          emails.get(recipient) match {
            case Some(inbox) =>
              println("Should be updating inbox and adding to: " + inbox)
              emails + (recipient -> inbox.copy(emails = inbox.emails :+ burnerEmailMessage))
            case None        =>
              println("updating inbox. No existing messages. ")
              emails + (recipient -> Inbox(List(burnerEmailMessage)))
          }
        )
//          emails + (recipient -> Inbox(List(burnerEmailMessage))
    } yield ()
  }
}

object EmailManager {
  def layer(
      originalUserData: Map[BurnerEmailAddress, Inbox],
      originalUserEmails: Map[UserId, List[BurnerEmailAddress]]
  ) = {
    ZLayer.fromZIO(
      for {
        originalUserData <- Ref.make(originalUserData)
        emailAddress     <- Ref.make(originalUserEmails)

      } yield EmailManagerImpl(originalUserData, emailAddress)
    )
  }
}
