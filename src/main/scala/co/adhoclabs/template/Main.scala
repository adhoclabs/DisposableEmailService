package co.adhoclabs.template

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import co.adhoclabs.analytics.AnalyticsManagerImpl
import co.adhoclabs.sqs_client.queue.SqsQueue
import co.adhoclabs.sqs_client.{SqsClient, SqsClientImpl}
import co.adhoclabs.template.api.{Api, ApiImpl}
import co.adhoclabs.template.business._
import co.adhoclabs.template.data.SlickPostgresProfile.backend.Database
import co.adhoclabs.template.data._
import co.adhoclabs.template.exceptions.AnalyticsSqsClientFailedToInitializeException
import com.typesafe.config.{Config, ConfigFactory}
import java.io.File
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Main extends App {

  implicit val system: ActorSystem = Dependencies.system
  implicit val executor: ExecutionContext = Dependencies.executor

  val config = Dependencies.config

  val host = config.getString("co.adhoclabs.template.host")
  val port = config.getInt("co.adhoclabs.template.port")

  val bindingFuture = Http().bindAndHandle(Dependencies.api.routes, host, port)
  bindingFuture.onComplete {
    case Success(serverBinding) => {
      println("Starting Template with:")
      println(s"- JAVA_OPTS: ${scala.util.Properties.envOrElse("JAVA_OPTS", "<EMPTY>")}")
      println(s"- CONF: ${scala.util.Properties.envOrElse("CONF", "<EMPTY>")} (file exists: ${Configuration.configFileExists})")

      println(s"Listening to ${serverBinding.localAddress}")
    }
    case Failure(error) => println(s"error: ${error.getMessage}")
  }
}

object Dependencies {

  implicit val config: Config = Configuration.config

  implicit val system: ActorSystem = ActorSystem("template")
  implicit val executor: ExecutionContext = system.dispatcher

  private val dbConfigReference: String = "co.adhoclabs.template.dbConfig"
  implicit val db: Database = SlickPostgresProfile.backend.Database.forConfig(dbConfigReference, config)

  implicit val songDao: SongDao = new SongDaoImpl
  implicit val albumDao: AlbumDao = new AlbumDaoImpl

  implicit val sqsClient: SqsClient = Analytics.sqsClient
  implicit val analyticsManager = new AnalyticsManagerImpl

  implicit val songManager: SongManager = new SongManagerImpl
  implicit val albumManager: AlbumManager = new AlbumManagerImpl

  val api: Api = new ApiImpl
}

object Configuration {
  private val configPath: String = scala.util.Properties.envOrElse("CONF", "")
  private val configFile: File = new File(configPath)
  val configFileExists: Boolean = configFile.exists
  val config: Config = {
    if(configFile.exists && configFile.isFile) {
      val parseFile = ConfigFactory.parseFile(configFile)
      ConfigFactory.load(parseFile)
    } else {
      ConfigFactory.load
    }
  }
}

object Analytics {
  val config = Configuration.config

  // These need to be made available separately via environment variable config
  private val awsAccessKeyO: Option[String] = sys.env.get("AWS_ACCESS_KEY_ID")
  private val awsSecretAccessKeyO: Option[String] = sys.env.get("AWS_SECRET_ACCESS_KEY")
  private val awsRegionO: Option[String] = sys.env.get("AWS_REGION")

  private val queueNames: List[String] = List(
    config.getString("co.adhoclabs.braze-sdk.queue_name"),
    config.getString("co.adhoclabs.braze-sdk.attributes_queue_name"),
    config.getString("co.adhoclabs.amplitude-sdk.queue_name")
  )

  implicit val sqsClient: SqsClientImpl = (awsAccessKeyO, awsSecretAccessKeyO, awsRegionO) match {
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

}