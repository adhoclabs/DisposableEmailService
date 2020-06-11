package co.adhoclabs.template.data

import co.adhoclabs.template.TestBase
import co.adhoclabs.template.data.SlickPostgresProfile.backend.Database
import com.typesafe.config.ConfigFactory
import scala.concurrent.ExecutionContext

abstract class DataTestBase extends TestBase {
  implicit protected val config = ConfigFactory.load()
  override implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.global

  private val dbConfigReference: String = "co.adhoclabs.template.dbConfig"
  implicit val db: Database = SlickPostgresProfile.backend.Database.forConfig(dbConfigReference, config)

  val albumDao: AlbumDao = new AlbumDaoImpl
  val songDao: SongDao = new SongDaoImpl
}
