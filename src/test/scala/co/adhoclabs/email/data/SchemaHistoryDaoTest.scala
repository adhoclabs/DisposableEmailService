package co.adhoclabs.email.data

import co.adhoclabs.email.models.SchemaHistory

class SchemaHistoryDaoTest extends DataTestBase {
  describe("getLatest") {
    it("should return the latest version") {
      schemaHistoryDao.getLatest() map { case SchemaHistory(version) =>
        assert(version >= "1")
      }
    }
  }
}
