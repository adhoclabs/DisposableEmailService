package co.adhoclabs.email.business

import co.adhoclabs.email.TestBase
import com.typesafe.config.ConfigFactory

abstract class BusinessTestBase extends TestBase {
  implicit protected val config = ConfigFactory.load()
}
