package co.adhoclabs.template.data

import co.adhoclabs.template.TestBase
import co.adhoclabs.template.data.SlickPostgresProfile.backend.Database
import com.typesafe.config.ConfigFactory
import scala.concurrent.ExecutionContext

abstract class DataTestBase extends TestBase {
  implicit protected val config = DataTestBase.config
  implicit protected val db: Database = DataTestBase.db
  implicit protected val daoExecutionContext: ExecutionContext = scala.concurrent.ExecutionContext.global

  protected val schemaHistoryDao: SchemaHistoryDao = new SchemaHistoryDaoImpl
  protected val albumDao: AlbumDao = new AlbumDaoImpl
  protected val songDao: SongDao = new SongDaoImpl
}

// For objects we only want to instantiate once per test run
object DataTestBase {
  val config = ConfigFactory.load()

  private val dbConfigReference: String = "co.adhoclabs.template.dbConfig"
  val db: Database = SlickPostgresProfile.backend.Database.forConfig(dbConfigReference, config)
}
