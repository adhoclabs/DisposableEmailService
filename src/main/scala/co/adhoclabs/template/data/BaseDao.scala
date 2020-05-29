package co.adhoclabs.template.data

import org.slf4j.Logger

trait BaseDao {
  protected val logger: Logger
  protected val db: data.SlickPostgresProfile.backend.Database
}
