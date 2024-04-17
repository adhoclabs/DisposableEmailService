package co.adhoclabs.email.api

import java.util.UUID
import co.adhoclabs.model.{Burner, EmptyResponse, ErrorResponse}
import co.adhoclabs.email.models.{
  Album,
  AlbumWithSongs,
  BurnerEmailAddress,
  CreateAlbumRequest,
  Genre,
  PatchAlbumRequest
}
import zio.schema.{DeriveSchema, Schema}
import zio._
import zio.http._
import zio.http.endpoint.openapi.OpenAPIGen
import zio.http.endpoint.Endpoint
import Schemas._
import co.adhoclabs.email.business.{BurnerEmailMessage, BurnerEmailMessageId, EmailManager, UserId}
import co.adhoclabs.email.exceptions.{AlbumAlreadyExistsException, NoSongsInAlbumException}
import zio.http.codec.Doc

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
  implicit
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
      emailMessageId,
      userId,
      "content",
      "subject"
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
