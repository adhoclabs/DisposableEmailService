package co.adhoclabs.template

import java.time.Clock

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import co.adhoclabs.analytics.{AnalyticsManager, AnalyticsManagerImpl}
import co.adhoclabs.sqs_client.SqsClientImpl
import co.adhoclabs.sqs_client.queue.SqsQueue
import co.adhoclabs.template.api.{Api, ApiImpl}
import co.adhoclabs.template.business._
import co.adhoclabs.template.data.SlickPostgresProfile.backend.Database
import co.adhoclabs.template.data._
import co.adhoclabs.template.exceptions.AnalyticsSqsClientFailedToInitializeException
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Main extends App {

  implicit val system: ActorSystem = Dependencies.actorSystem
  implicit val executor: ExecutionContext = Dependencies.executionContext

  val config = Dependencies.config

  val host = config.getString("co.adhoclabs.template.host")
  val port = config.getInt("co.adhoclabs.template.port")

  val bindingFuture = Http().bindAndHandle(Dependencies.api.routes, host, port)
  bindingFuture.onComplete {
    case Success(serverBinding) =>
      println("Starting Template with:")
      println(s"- JAVA_OPTS: ${scala.util.Properties.envOrElse("JAVA_OPTS", "<EMPTY>")}")
      println(s"Listening to ${serverBinding.localAddress}")
    case Failure(error) =>
      println(s"error: ${error.getMessage}")
  }
}

object Dependencies {
  // config
  implicit val config: Config = Configuration.config
  implicit val clock: Clock = Clock.systemUTC()

  // akka/concurrency
  implicit val actorSystem: ActorSystem = ActorSystem("template")
  implicit val executionContext: ExecutionContext = actorSystem.dispatcher

  // database
  private val dbConfigReference: String = "co.adhoclabs.template.dbConfig"
  implicit val db: Database = SlickPostgresProfile.backend.Database.forConfig(dbConfigReference, config)
  implicit val schemaHistoryDao: SchemaHistoryDao = new SchemaHistoryDaoImpl
  implicit val songDao: SongDao = new SongDaoImpl
  implicit val albumDao: AlbumDao = new AlbumDaoImpl

  // business
  implicit val analyticsManager: AnalyticsManager = Analytics.analyticsManager
  implicit val healthManager: HealthManager = new HealthManagerImpl
  implicit val songManager: SongManager = new SongManagerImpl
  implicit val albumManager: AlbumManager = new AlbumManagerImpl

  // api
  val api: Api = new ApiImpl
}

object Configuration {
  val config: Config = ConfigFactory.load
}

object Analytics {
  val config: Config = Configuration.config

  // These need to be made available separately via environment variable config
  private val awsAccessKeyO: Option[String] = sys.env.get("AWS_ACCESS_KEY_ID")
  private val awsSecretAccessKeyO: Option[String] = sys.env.get("AWS_SECRET_ACCESS_KEY")
  private val awsRegionO: Option[String] = sys.env.get("AWS_REGION")

  private val queueNames: List[String] = List(
    config.getString("co.adhoclabs.braze-sdk.queue_name"),
    config.getString("co.adhoclabs.braze-sdk.attributes_queue_name"),
    config.getString("co.adhoclabs.amplitude-sdk.queue_name")
  )

  private implicit val sqsClient: SqsClientImpl = (awsAccessKeyO, awsSecretAccessKeyO, awsRegionO) match {
    case (Some(accessKey: String), Some(secretAccessKey: String), Some(region: String)) =>
      val queues: List[SqsQueue] = queueNames.map(queueName => SqsQueue(
        queueName = queueName,
        accessKeyId = accessKey,
        secretAccessKey = secretAccessKey,
        regionName = region
      ))
      new SqsClientImpl((queueNames zip queues).toMap)
    case _ => throw new AnalyticsSqsClientFailedToInitializeException
  }
  val analyticsManager: AnalyticsManager = new AnalyticsManagerImpl
}