package co.adhoclabs.template.data

import org.slf4j.Logger
import co.adhoclabs.template.data.SlickPostgresProfile.backend.Database
import org.postgresql.util.PSQLException

trait DaoBase {
  protected val logger: Logger
  protected val db: Database = databaseConnection.db

  protected def isDuplicateKeyException(e: PSQLException): Boolean =
    e.getServerErrorMessage.getMessage.contains("duplicate key value")
}
