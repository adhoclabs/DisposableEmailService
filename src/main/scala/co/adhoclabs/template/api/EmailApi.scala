package co.adhoclabs.template.api

import java.util.UUID
import co.adhoclabs.model.{Burner, EmptyResponse, ErrorResponse}
import co.adhoclabs.template.models.{
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
import co.adhoclabs.template.business.{BurnerEmailMessage, BurnerEmailMessageId, EmailManager}
import co.adhoclabs.template.exceptions.{AlbumAlreadyExistsException, NoSongsInAlbumException}
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

  val get =
    // TODO Return 404 when album with id not found
    Endpoint(Method.GET / "emailMessage" / emailMessageidPathCodec("emailMessageId"))
      .??(openApiSrcLink(implicitly[sourcecode.Line]))
      .out[BurnerEmailMessage](Status.Created)
      .outError[ErrorResponse](Status.NotFound)
      .examplesIn(
        "Pre-existing Record" -> BurnerEmailMessageId(UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479"))
      )

  val submit =
    Endpoint(Method.POST / "emailMessages")
      .??(openApiSrcLink(implicitly[sourcecode.Line]))
      .in[BurnerEmailAddress]
      .out[BurnerEmailAddress](Status.Created)
      .outError[InternalErrorResponse](Status.InternalServerError)
      .outError[BadRequestResponse](Status.BadRequest)

  // TODO Why do we need this to be in this file, rather than just one more entry in the Schemas object?
  implicit val schema: Schema[EmptyResponse] = DeriveSchema.gen

  val delete =
    // TODO Return 404 when album with id not found?
    Endpoint(Method.DELETE / "emailMessages" / emailMessageidPathCodec("emailMessageId"))
      .??(openApiSrcLink(implicitly[sourcecode.Line]))
      .out[EmptyResponse](Status.NoContent) // TODO Why not AlbumWithSongs here?

  val openAPI =
    OpenAPIGen.fromEndpoints(
      title = "BurnerAlbums",
      version = "1.0",
      submit,
      get,
      delete
    )

  val endpoints =
    List(
      submit,
      get,
      delete
    )
}

case class EmailRoutes(
  implicit
  emailManager: EmailManager
) {
  val submit =
    EmailEndpoints.submit.implement {
      Handler.fromFunctionZIO { (createAlbumRequest: BurnerEmailAddress) =>
        for {
          futureRes <-
            ZIO.fromEither(emailManager.createBurnerEmailAddress(createAlbumRequest)).mapError {
              case conflict: String =>
                Right(BadRequestResponse(conflict))
            }

        } yield futureRes
      }
    }

  val get =
    EmailEndpoints.get.implement {
      Handler.fromFunctionZIO { (emailMessageId: BurnerEmailMessageId) =>
        ZIO
          .fromOption[BurnerEmailMessage](None)
          .mapError(_ => new Exception("No email message with id: " + emailMessageId))
          .orDie
//          .someOrFail(ErrorResponse("Could not find album!"))
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
      get,
      delete
    )
}
