package co.adhoclabs.email.api

import co.adhoclabs.email.api.Schemas._
import co.adhoclabs.email.business.{BurnerEmailMessage, BurnerEmailMessageId, EmailManager, UserId}
import co.adhoclabs.email.models.BurnerEmailAddress
import co.adhoclabs.model.{EmptyResponse, ErrorResponse}
import zio._
import zio.http._
import zio.http.codec.Doc
import zio.http.endpoint.Endpoint
import zio.schema.{DeriveSchema, Schema}

import java.time.Instant
import java.util.UUID

object EmailEndpoints {
  val file                                = implicitly[sourcecode.File]
  val projectName                         = "disposable-email-service"
  def keepFilePath(file: sourcecode.File) = {
    val index = file.value.indexOf(projectName)
    file.value.drop(index + projectName.length + 1)
  }

  val githubLink = {
    val filePath = keepFilePath(file)
    s"https://github.com/adhoclabs/${projectName}/blob/main/$filePath"
  }

  final def openApiSrcLink(line: sourcecode.Line) = {
    // This is the best way I've found to get a clickable link
    Doc.fromCommonMark(s"[Src]($githubLink#L${line.value})")
  }

  def emailMessageidPathCodec(name: String) = uuid(name).transform(BurnerEmailMessageId.apply)(_.id)

  def userIdPathCodec(name: String) = uuid(name).transform(UserId.apply)(_.id)

  val getMessage =
    Endpoint(
      Method.GET / "email" / "user" / userIdPathCodec("userId") / "emailMessages" / emailMessageidPathCodec(
        "emailMessageId"
      )
    )
      .??(openApiSrcLink(implicitly[sourcecode.Line]))
      .out[BurnerEmailMessage](Status.Created)
      .outError[ErrorResponse](Status.NotFound)
      .examplesIn(
        "Pre-existing Record" ->
          (UserId(UUID.fromString("d56ac10b-58cc-4372-a567-0e02b2c3d479")),
          BurnerEmailMessageId(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479")))
      )

  val getInbox =
    Endpoint(
      Method.GET / "email" / "user" / userIdPathCodec("userId") / "emailMessages"
    )
      .??(Doc.p("Get all email messages for a user"))
      .??(openApiSrcLink(implicitly[sourcecode.Line]))
      .out[List[BurnerEmailMessage]](Status.Ok)
      .outError[ErrorResponse](Status.NotFound)
      .examplesIn(
        "Pre-existing Record" ->
          UserId(UUID.fromString("d56ac10b-58cc-4372-a567-0e02b2c3d479"))
      )

  val submit =
    Endpoint(Method.POST / "email" / "user" / userIdPathCodec("userId") / "emailAddress")
      .??(openApiSrcLink(implicitly[sourcecode.Line]))
      .in[BurnerEmailAddress]
      .out[BurnerEmailAddress](Status.Created)
      .outError[InternalErrorResponse](Status.InternalServerError)
      .outError[BadRequestResponse](Status.BadRequest)

  // TODO Why do we need this to be in this file, rather than just one more entry in the Schemas object?
  implicit val schema: Schema[EmptyResponse] = DeriveSchema.gen

  val delete =
    // TODO Return 404 when album with id not found?
    Endpoint(Method.DELETE / "email" / "emailMessages" / emailMessageidPathCodec("emailMessageId"))
      .??(openApiSrcLink(implicitly[sourcecode.Line]))
      .out[EmptyResponse](Status.NoContent) // TODO Why not AlbumWithSongs here?

  val endpoints =
    List(
      submit,
      getMessage,
      getInbox,
      delete
    )
}

case class EmailRoutes(
  emailManager: EmailManager
) {
  val submit =
    EmailEndpoints.submit.implement {
      Handler.fromFunctionZIO { case (userId: UserId, createAlbumRequest: BurnerEmailAddress) =>
        for {
          futureRes <-
            ZIO.fromEither(emailManager.createBurnerEmailAddress(createAlbumRequest)).mapError {
              case conflict: String =>
                Right(BadRequestResponse(conflict))
            }

        } yield futureRes
      }
    }

  def createEmailMessage(userId: UserId, emailMessageId: BurnerEmailMessageId): BurnerEmailMessage = {
    BurnerEmailMessage(
      id = emailMessageId,
      source = "source",
      to =
        List(
          userId.toString
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

  val getMessage =
    EmailEndpoints.getMessage.implement {
      Handler.fromFunctionZIO { case (userId: UserId, emailMessageId: BurnerEmailMessageId) =>
        ZIO
          .fromOption[BurnerEmailMessage](
            Some(
              createEmailMessage(userId, emailMessageId)
            )
          )
          .mapError(_ => new Exception("No email message with id: " + emailMessageId))
          .orDie
      }
    }

  val getInbox =
    EmailEndpoints.getInbox.implement {
      Handler.fromFunctionZIO { case (userId: UserId) =>
        ZIO
          .succeed(
            List(
              createEmailMessage(userId, BurnerEmailMessageId(UUID.randomUUID())),
              createEmailMessage(userId, BurnerEmailMessageId(UUID.randomUUID()))
            )
          )
//          .mapError(_ => new Exception("No user with id: " + userId))
//          .orDie
      }
    }

  val delete =
    EmailEndpoints.delete.implement {
      Handler.fromFunctionZIO { (albumId: BurnerEmailMessageId) =>
        ZIO
          .fromFuture(implicit ec => emailManager.deleteMessage(albumId))
          .orDie
          .as(EmptyResponse())
      }
    }

  val routes =
    Routes(
      submit,
      getMessage,
      getInbox,
      delete
    )
}

object EmailRoutes {
  val layer: ZLayer[EmailManager, Nothing, EmailRoutes] = ZLayer.fromFunction(EmailRoutes.apply _)
}
