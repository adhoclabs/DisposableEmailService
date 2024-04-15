package co.adhoclabs.email.business

import co.adhoclabs.email.data.SchemaHistoryDao
import co.adhoclabs.email.models.SchemaHistory

import scala.concurrent.Future

class HealthManagerTest extends BusinessTestBase {
  implicit val schemaHistoryDao: SchemaHistoryDao = mock[SchemaHistoryDao]
  val healthManager: HealthManager                = new HealthManagerImpl

  val expectedSchemaHistory =
    SchemaHistory(
      version = "1"
    )

  describe("executeDbGet") {
    it("should return a Unit") {
      (schemaHistoryDao.getLatest _)
        .expects()
        .returning(Future.successful(expectedSchemaHistory))

      healthManager.executeDbGet() map { u =>
        assert(u == ())
      }
    }
  }
}
