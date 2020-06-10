package co.adhoclabs.template.data

import org.slf4j.Logger
import co.adhoclabs.template.data.SlickPostgresProfile.backend.Database
import org.postgresql.util.PSQLException

trait DaoBase {
  protected val logger: Logger
  protected val db: Database = databaseConnection.db
}

object DaoBase {
  def isUniqueConstraintViolation(e: PSQLException): Boolean =
  //https://www.postgresql.org/docs/11/errcodes-appendix.html
    e.getSQLState == "23505"
}
