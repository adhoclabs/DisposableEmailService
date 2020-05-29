package co.adhoclabs.template.data

import com.typesafe.config.ConfigFactory
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.funspec.AsyncFunSpec

abstract class DataTestBase extends AsyncFunSpec with AsyncMockFactory {
  implicit protected val config = ConfigFactory.load()
}
