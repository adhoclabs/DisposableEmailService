package co.adhoclabs.template.business

import com.typesafe.config.ConfigFactory
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.flatspec.AsyncFlatSpec

class BusinessTestBase extends AsyncFlatSpec with AsyncMockFactory {
  // CONFIG
  implicit protected val config = ConfigFactory.load()
}
