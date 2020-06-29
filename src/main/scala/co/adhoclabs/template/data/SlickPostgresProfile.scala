package co.adhoclabs.template.data

import java.sql.JDBCType
import java.util.UUID

import co.adhoclabs.template.models.Genre
import com.github.tminglei.slickpg._
import slick.jdbc.{GetResult, PositionedParameters, PositionedResult, PostgresProfile, SetParameter}

// PgDate2Support/Date2DateTimePlainImplicits required for timestamp/Instant columns
// PgArraySupport/ArrayImplicits/SimpleArrayPlainImplicits required for array columns
// PgEnumSupport required for db enums
// UuidSupport required for uuid columns
trait SlickPostgresProfile extends PostgresProfile with PgDate2Support with PgArraySupport with PgEnumSupport {
  object PostgresAPI extends API with Date2DateTimePlainImplicits with ArrayImplicits with SimpleArrayPlainImplicits with UuidSupport {

    // custom mappers are required for enum types
    implicit val genreMapper = createEnumJdbcType("genre", Genre)
  }
  override val api = PostgresAPI
}

object SlickPostgresProfile extends SlickPostgresProfile

// Using solution posted at https://gist.github.com/drobert/9974355296c95029cce9528526e9a1ec
trait UuidSupport {
  // inlined from https://github.com/tminglei/slick-pg/blob/master/core/src/main/scala/com/github/tminglei/slickpg/utils/PlainSQLUtils.scala#L28
  def mkGetResult[T](next: (PositionedResult => T)): GetResult[T] =
    new GetResult[T] { def apply(rs: PositionedResult) = next(rs) }

  // UUIDs are not actually supported in plain SQL by slick still, only
  // using the 'natural' slick api
  // see: https://github.com/slick/slick/issues/161#issuecomment-169373730
  implicit class PgUuidPositionedResult(r: PositionedResult) {
    // postgres should natively support UUID types
    def nextUuid: UUID = r.nextObject().asInstanceOf[UUID]

    def nextUuidOption: Option[UUID] =
      r.nextObjectOption().map(_.asInstanceOf[UUID])
  }

  implicit val getResultUuid: GetResult[UUID] = mkGetResult(_.nextUuid)
  implicit val getResultUuidOption: GetResult[Option[UUID]] =
    mkGetResult(_.nextUuidOption)

  // approach relies on notes in: https://github.com/slick/slick/issues/161#issuecomment-329168647
  // primarily: postgres and its jdbc driver support UUID/java.util.UUID directly
  //
  // unlike the previously referenced code snippet https://github.com/slick/slick/issues/161#issuecomment-169373730
  // JDBCType.BINARY.getVendorTypeNumber does not work for postgres. The resulting errors
  // are like this one: https://stackoverflow.com/questions/4495233/postgresql-uuid-supported-by-hibernate
  //   column "SOME_COLUMN" is of type uuid but expression is of type bytea at character 149
  //
  // The fix is to use JDBCType.OTHER instead, as hibernate does in their own implementation:
  // https://github.com/hibernate/hibernate-orm/blob/master/hibernate-core/src/main/java/org/hibernate/type/PostgresUUIDType.java#L56
  implicit object SetUuid extends SetParameter[UUID] {
    override def apply(u: UUID, pp: PositionedParameters): Unit =
      pp.setObject(u, JDBCType.OTHER.getVendorTypeNumber)
  }

  implicit object SetUuidOption extends SetParameter[Option[UUID]] {
    override def apply(u: Option[UUID], pp: PositionedParameters): Unit =
      pp.setObjectOption(u, JDBCType.OTHER.getVendorTypeNumber)
  }
}
