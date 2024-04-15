package co.adhoclabs.template.models

import co.adhoclabs.model.BaseJsonProtocol
import spray.json.RootJsonFormat
import zio.schema.{DeriveSchema, Schema}

import java.time.Instant
import java.util.UUID

case class Song(
  id:            UUID,
  title:         String,
  albumId:       UUID,
  albumPosition: Int,
  createdAt:     Instant,
  updatedAt:     Instant
)

object Song extends BaseJsonProtocol {
  implicit val jsonFormat: RootJsonFormat[Song] = jsonFormat6(Song.apply)
  implicit val schema: Schema[Song] = DeriveSchema.gen
}

/*
// While we ultimately felt that the apply method didn't add enough value, especially
// after adding date fields, we're leaving this example in case someone in the future
// feels a need to make a case class with an apply method also represent a DB table.
//
// If a case class is being used by Slick to represent a database row, and if that
// case class's helper function has custom apply methods like this one does,
// the helper object must must extend the Scala Function definition of the case class
// in order for the Slick mapTo method to identify the base apply method of the case class.

object Song extends ((UUID, String, UUID, Int) => Song) {
  def apply(createSongRequest: CreateSongRequest): Song = Song(
    id = UUID.randomUUID,
    title = createSongRequest.title,
    albumId = createSongRequest.albumId,
    albumPosition = createSongRequest.albumPosition
  )
}
*/

case class CreateSongRequest(
  title:         String,
  albumId:       UUID,
  albumPosition: Int
)

object CreateSongRequest extends BaseJsonProtocol {
  implicit val jsonFormat: RootJsonFormat[CreateSongRequest] = jsonFormat3(CreateSongRequest.apply)
  implicit val schema: Schema[CreateSongRequest] = DeriveSchema.gen
}
