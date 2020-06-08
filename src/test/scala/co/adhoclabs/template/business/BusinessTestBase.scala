package co.adhoclabs.template.business

import co.adhoclabs.template.TestBase
import com.typesafe.config.ConfigFactory

abstract class BusinessTestBase extends TestBase {
  implicit protected val config = ConfigFactory.load()
}
