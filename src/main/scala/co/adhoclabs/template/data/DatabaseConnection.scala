package co.adhoclabs.template.data

import co.adhoclabs.template.configuration.config
import co.adhoclabs.template.data.SlickPostgresProfile.backend.Database

trait DatabaseConnection {
  val db: Database
}

object DatabaseConnectionImpl extends DatabaseConnection {
  private val dbConfigReference: String = "co.adhoclabs.template.dbConfig"
  override val db: Database = SlickPostgresProfile.backend.Database.forConfig(dbConfigReference, config)
}
