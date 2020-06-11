package co.adhoclabs.template.data

import org.slf4j.Logger
import co.adhoclabs.template.data.SlickPostgresProfile.backend.Database
import org.postgresql.util.PSQLException

abstract class DaoBase(implicit val db: Database) {
  protected val logger: Logger
}

object DaoBase {
  def isUniqueConstraintViolation(e: PSQLException): Boolean =
  //https://www.postgresql.org/docs/11/errcodes-appendix.html
    e.getSQLState == "23505"
}
