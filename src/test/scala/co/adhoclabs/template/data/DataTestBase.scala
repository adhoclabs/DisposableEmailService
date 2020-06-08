package co.adhoclabs.template.data

import co.adhoclabs.template.TestBase
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext

abstract class DataTestBase extends TestBase {
  implicit protected val config = ConfigFactory.load()
  override implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.global

  val albumDao: AlbumDao = new AlbumDaoImpl
  val songDao: SongDao = new SongDaoImpl
}
