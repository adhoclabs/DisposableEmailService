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
    println(s"""
        |file: + $file
        |filePath:  + $filePath
        |""".stripMargin)
    s"https://github.com/adhoclabs/${projectName}/blob/main/$filePath"
  }

  final def openApiSrcLink(line: sourcecode.Line) = {
    // This is the best way I've found to get a clickable link
    Doc.fromCommonMark(s"[Src]($githubLink#L${line.value})")
  }

  def emailMessageIdPathCodec(name: String) = uuid(name).transform(BurnerEmailMessageId.apply)(_.id)

  def userIdPathCodec(name: String) = uuid(name).transform(UserId.apply)(_.id)

  val goodUserId = UserId(UUID.fromString("d56ac10b-58cc-4372-a567-0e02b2c3d479"))

  val postMessage =
    Endpoint(
      Method.POST / "email"
    )
      .??(Doc.p("This just quietly accepts valid payloads. TODO save for relevant user, if exists."))
      .??(openApiSrcLink(implicitly[sourcecode.Line]))
      .in[BurnerEmailMessage]
      .out[BurnerEmailMessage](Status.Created)
      .outError[ErrorResponse](Status.NotFound)
      .examplesIn(
        "Pre-existing Record" ->
          BurnerEmailMessage
            .create(goodUserId, BurnerEmailMessageId(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479")))
      )

  val getMessage =
    Endpoint(
      Method.GET / "email" / "user" / userIdPathCodec("userId") / "emailMessages" / emailMessageIdPathCodec(
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
      .examplesIn(
        "New email for existing user" ->
          (goodUserId, BurnerEmailAddress("newFancyEmail@burnermail.me"))
      )

  // TODO Why do we need this to be in this file, rather than just one more entry in the Schemas object?
  implicit val schema: Schema[EmptyResponse] = DeriveSchema.gen

  val delete =
    // TODO Return 404 when album with id not found?
    Endpoint(Method.DELETE / "email" / "emailMessages" / emailMessageIdPathCodec("emailMessageId"))
      .??(openApiSrcLink(implicitly[sourcecode.Line]))
      .out[EmptyResponse](Status.NoContent) // TODO Why not AlbumWithSongs here?

  val endpoints =
    List(
      submit,
      postMessage,
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
            emailManager.createBurnerEmailAddress(createAlbumRequest, userId).mapError {
              case conflict: String =>
                Right(BadRequestResponse(conflict))
            }

        } yield futureRes
      }
    }

  val getMessage =
    EmailEndpoints.getMessage.implement {
      Handler.fromFunctionZIO { case (userId: UserId, emailMessageId: BurnerEmailMessageId) =>
        ZIO
          .fromOption[BurnerEmailMessage](
            Some(
              BurnerEmailMessage.create(userId, emailMessageId)
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
              BurnerEmailMessage.create(userId, BurnerEmailMessageId(UUID.randomUUID())),
              BurnerEmailMessage.create(userId, BurnerEmailMessageId(UUID.randomUUID())),
              BurnerEmailMessage.create(userId, BurnerEmailMessageId(UUID.randomUUID())),
              BurnerEmailMessage.create(userId, BurnerEmailMessageId(UUID.randomUUID())),
              BurnerEmailMessage.create(userId, BurnerEmailMessageId(UUID.randomUUID()))
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

  val postMessage =
    EmailEndpoints.postMessage.implement {
      Handler.fromFunctionZIO { (emailMessage: BurnerEmailMessage) =>
        ZIO.succeed(emailMessage)
      }

    }

  val routes =
    Routes(
      submit,
      getMessage,
      getInbox,
      delete,
      postMessage
    )
}

object EmailRoutes {
  val layer: ZLayer[EmailManager, Nothing, EmailRoutes] = ZLayer.fromFunction(EmailRoutes.apply _)
}
