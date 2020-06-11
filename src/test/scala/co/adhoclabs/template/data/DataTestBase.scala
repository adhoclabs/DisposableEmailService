package co.adhoclabs.template.data

import co.adhoclabs.template.TestBase
import co.adhoclabs.template.data.SlickPostgresProfile.backend.Database
import com.typesafe.config.ConfigFactory
import scala.concurrent.ExecutionContext

abstract class DataTestBase extends TestBase {
  implicit protected val config = ConfigFactory.load()

  private val dbConfigReference: String = "co.adhoclabs.template.dbConfig"
  implicit val db: Database = SlickPostgresProfile.backend.Database.forConfig(dbConfigReference, config)

  implicit val daoExecutionContext: ExecutionContext = scala.concurrent.ExecutionContext.global

  val albumDao: AlbumDao = new AlbumDaoImpl
  val songDao: SongDao = new SongDaoImpl
}
