package co.adhoclabs.template.data

import co.adhoclabs.template.models.SchemaHistory

class SchemaHistoryDaoTest extends DataTestBase {
  describe("getLatest") {
    it("should return the latest version") {
      schemaHistoryDao.getLatest() map {
        case SchemaHistory(version) => assert(version >= "1")
      }
    }
  }
}
