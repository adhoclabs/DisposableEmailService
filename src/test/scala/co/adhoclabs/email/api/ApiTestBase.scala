package co.adhoclabs.email.api

import co.adhoclabs.email.TestBase
import co.adhoclabs.email.api.{ApiZ, HealthRoutes}
import co.adhoclabs.email.business.{EmailManager, HealthManager}

abstract class ApiTestBase extends TestBase with ZioHttpTestHelpers {

  implicit val healthManager: HealthManager = mock[HealthManager]
  implicit val emailManager: EmailManager   = mock[EmailManager]

  // ZIO-http bits
  implicit val albumbRoutes = EmailRoutes()
  implicit val healthRoutes = HealthRoutes()

  val zioRoutes = ApiZ().zioRoutes
  val app       = zioRoutes.toHttpApp

}
