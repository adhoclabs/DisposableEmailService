package co.adhoclabs.email

import co.adhoclabs.email.api.{ApiZ, EmailRoutes, HealthRoutes}
import co.adhoclabs.email.business._
import com.typesafe.config.{Config, ConfigFactory}
import zio.{ZIO, ZIOAppDefault, ZLayer}
import zio.http.Server

import java.time.Clock
import scala.concurrent.ExecutionContext

object Main extends ZIOAppDefault {
  import Dependencies._
  implicit val healthRoutes = HealthRoutes()
//  implicit val emailRoutes  = EmailRoutes()

//  val app    = ApiZ().zioRoutes.toHttpApp
  val config = Dependencies.config
  val host   = config.getString("co.adhoclabs.template.host")
  val port   = config.getInt("co.adhoclabs.template.port")
  def run    =
    (for {
      _   <- ZIO.debug("Starting")
      api <- ZIO.service[ApiZ]
      _   <-
        Server
          .serve(api.zioRoutes.toHttpApp)
          .provide(
            Server.defaultWith(config => config.binding(hostname = host, port = port))
          )
          .exitCode

    } yield ()).provide(
      ApiZ.layer,
      EmailRoutes.layer,
      EmailManager.layer,
      ZLayer.succeed(HealthRoutes())
    )
}

object Dependencies {
  // config
  implicit val config: Config = Configuration.config
  implicit val clock: Clock   = Clock.systemUTC()

  // akka/concurrency
  implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  // aws
//  private val awsConfig: Config = config.getConfig("co.adhoclabs.template.aws")
//  private val awsRegion: String = awsConfig.getString("region")

  // sqs
//  private val queueNames: List[String]        =
//    List(
//      Configuration.sqsConfig.getString("fake_queue.queue_name")
//    )
//  private val queueMap: Map[String, SqsQueue] =
//    queueNames
//      .map(queueName =>
//        queueName -> SqsQueueWithInferredCredentials(
//          queueName = queueName,
//          regionName = awsRegion
//        )
//      )
//      .toMap
//  implicit val sqsClient: SqsClient           = new SqsClientImpl(queueMap)
//  implicit val sqsManager: SqsManager         = new SqsManagerImpl

  // secrets
//  private implicit val secretsClient: SecretsClient = new SecretsClientImpl(awsRegion)
//  implicit val secretsManager: SecretsManager       = new SecretsManagerImpl()

  // database
//  private val dbConfigReference: String           = "co.adhoclabs.template.dbConfig"
//  implicit val db: Database                       = SlickPostgresProfile.backend.Database.forConfig(dbConfigReference, config)
//  implicit val schemaHistoryDao: SchemaHistoryDao = new SchemaHistoryDaoImpl
//  implicit val songDao: SongDao                   = new SongDaoImpl
//  implicit val albumDao: AlbumDao                 = new AlbumDaoImpl

  // business
  implicit val healthManager: HealthManager = new HealthManagerImpl
//  implicit val songManager: SongManager     = new SongManagerImpl
//  implicit val emailManager: EmailManager   = new EmailManagerImpl

}

object Configuration {
  val config: Config    = ConfigFactory.load
  val sqsConfig: Config = config.getConfig("co.adhoclabs.template.sqs")
}
