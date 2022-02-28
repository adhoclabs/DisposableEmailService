package co.adhoclabs.template.data

import co.adhoclabs.template.data.SlickPostgresProfile.api._
import co.adhoclabs.template.data.SlickPostgresProfile.backend.Database
import co.adhoclabs.template.models.SchemaHistory
import org.slf4j.{Logger, LoggerFactory}
import slick.lifted.ProvenShape

import scala.concurrent.Future

trait SchemaHistoryDao {
  def getLatest(): Future[SchemaHistory]
}

case class SchemaHistoryTable(tag: Tag) extends Table[SchemaHistory](tag, "flyway_schema_history") {
  def version: Rep[String] = column[String]("version")

  // Provides a default projection that maps between columns in the table and instances of our case class.
  // mapTo creates a two-way mapping between the columns and fields.
  override def * : ProvenShape[SchemaHistory] = (version).mapTo[SchemaHistory]
}

class SchemaHistoryDaoImpl(implicit db: Database) extends DaoBase with SchemaHistoryDao {

  override protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  lazy val schemaHistories = TableQuery[SchemaHistoryTable]

  override def getLatest(): Future[SchemaHistory] = {
    db.run(
      schemaHistories
        .sortBy(_.version.desc)
        .result
        .head
    )
  }
}
