package co.adhoclabs.template

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import co.adhoclabs.secrets.{SecretsClient, SecretsClientImpl}
import co.adhoclabs.sqs_client.{SqsClient, SqsClientImpl}
import co.adhoclabs.sqs_client.queue.{SqsQueue, SqsQueueWithInferredCredentials}
import co.adhoclabs.template.Main.config
import co.adhoclabs.template.api.{Api, ApiImpl}
import co.adhoclabs.template.apiz.{AlbumRoutes, ApiZ, HealthRoutes, SongRoutes}
import co.adhoclabs.template.business._
import co.adhoclabs.template.data.SlickPostgresProfile.backend.Database
import co.adhoclabs.template.data._
import com.typesafe.config.{Config, ConfigFactory}
import zio.{ZIO, ZIOAppDefault}
import zio.http.Server

import java.time.Clock
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object MainZio extends ZIOAppDefault {
  import Dependencies._
  implicit val albumbRoutes = AlbumRoutes()
  implicit val songRoutes = SongRoutes()
  implicit val healthRoutes = HealthRoutes()

  val app = ApiZ().zioRoutes.toHttpApp
  val config = Dependencies.config
  val host = config.getString("co.adhoclabs.template.host")
  val port = config.getInt("co.adhoclabs.template.port")
  def run = {
    ZIO.debug("Starting") *>
      Server.serve(app).provide(Server.defaultWith(config => config.binding(hostname = host, port = port))).exitCode
    /*
    TODO Can we replicate this exactly?
    Akka startup output:

      bindingFuture.onComplete {
        case Success(serverBinding) =>
          println("Starting Template with:")
          println(s"- JAVA_OPTS: ${scala.util.Properties.envOrElse("JAVA_OPTS", "<EMPTY>")}")
          println(s"Listening to ${serverBinding.localAddress}")
        case Failure(error) =>
          println(s"error: ${error.getMessage}")
      }
       */
    }
}
object Main {

  implicit val system: ActorSystem = Dependencies.actorSystem
  implicit val executor: ExecutionContext = Dependencies.executionContext

  val config = Dependencies.config

  val host = config.getString("co.adhoclabs.template.host")
  val port = config.getInt("co.adhoclabs.template.port")

  val bindingFuture = Http.get(system).newServerAt(interface = host, port = port).bind(Dependencies.api.routes)
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

  // aws
  private val awsConfig: Config = config.getConfig("co.adhoclabs.template.aws")
  private val awsRegion: String = awsConfig.getString("region")

  // sqs
  private val queueNames: List[String] = List(
    Configuration.sqsConfig.getString("fake_queue.queue_name")
  )
  private val queueMap: Map[String, SqsQueue] = queueNames.map(queueName =>
    queueName -> SqsQueueWithInferredCredentials(
      queueName  = queueName,
      regionName = awsRegion
    )).toMap
  implicit val sqsClient: SqsClient = new SqsClientImpl(queueMap)
  implicit val sqsManager: SqsManager = new SqsManagerImpl

  // secrets
  private implicit val secretsClient: SecretsClient = new SecretsClientImpl(awsRegion)
  implicit val secretsManager: SecretsManager = new SecretsManagerImpl()

  // database
  private val dbConfigReference: String = "co.adhoclabs.template.dbConfig"
  implicit val db: Database = SlickPostgresProfile.backend.Database.forConfig(dbConfigReference, config)
  implicit val schemaHistoryDao: SchemaHistoryDao = new SchemaHistoryDaoImpl
  implicit val songDao: SongDao = new SongDaoImpl
  implicit val albumDao: AlbumDao = new AlbumDaoImpl

  // business
  implicit val healthManager: HealthManager = new HealthManagerImpl
  implicit val songManager: SongManager = new SongManagerImpl
  implicit val albumManager: AlbumManager = new AlbumManagerImpl

  // api
  val api: Api = new ApiImpl

}

object Configuration {
  val config: Config = ConfigFactory.load
  val sqsConfig: Config = config.getConfig("co.adhoclabs.template.sqs")
}
