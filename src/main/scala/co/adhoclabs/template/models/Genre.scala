package co.adhoclabs.template.models

import zio.schema.{Schema, TypeId}

object Genre extends Enumeration {
  val NoGenre = Value
  val Rock = Value
  val HipHop = Value
  val Classical = Value
  val Pop = Value

  type Genre = Value

  // TODO Take a close look at this during PR. This is something we'll need to do for all of our custom Enums.
  implicit val genreSchema: Schema[Genre] =
    Schema.CaseClass1[String, Genre](
      TypeId.parse("co.adhoclabs.template.models.Genre"),
      field0            =
        Schema.Field[Genre, String](
          "name",
          Schema.primitive[String],
          get0 = _.toString,
          set0 = (_, v) => Genre.withName(v)
        ),
      defaultConstruct0 = (name) => Genre.withName(name)
    )

}
