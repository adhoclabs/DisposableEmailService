package co.adhoclabs.template.data

import co.adhoclabs.template.models.Genre
import com.github.tminglei.slickpg._
import slick.jdbc.PostgresProfile

trait SlickPostgresProfile extends PostgresProfile with PgArraySupport with PgEnumSupport {
  object PostgresAPI extends API with ArrayImplicits  {

    // custom mappers are required for enum types
    implicit val appliedOfferStatusMapper = createEnumJdbcType("genre", Genre)
  }
  override val api = PostgresAPI
}

object SlickPostgresProfile extends SlickPostgresProfile
