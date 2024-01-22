package co.adhoclabs.template.apiz

import co.adhoclabs.model.{EmptyResponse, ErrorResponse}

object Schemas {
  import zio.schema.{DeriveSchema, Schema}
  // TODO better spot for this. Ideally it would live in the upstream lib
  implicit val schema: Schema[EmptyResponse] = DeriveSchema.gen
  implicit val errorResponseSchema: Schema[ErrorResponse] = DeriveSchema.gen

}
