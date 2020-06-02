package co.adhoclabs.template.data

import org.slf4j.Logger
import co.adhoclabs.template.data.SlickPostgresProfile.backend.Database

trait BaseDao {
  protected val logger: Logger
  protected val db: Database = databaseConnection.db
}
