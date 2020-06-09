package co.adhoclabs.template

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import co.adhoclabs.template.api.Api
import co.adhoclabs.template.business._
import co.adhoclabs.template.configuration.{config, configFile}
import co.adhoclabs.template.actorsystem._
import scala.util.{Failure, Success}

object Main extends App with Api {
  val songManager: SongManager = implicitly
  val albumManager: AlbumManager = implicitly

  implicit val system: ActorSystem = actorsystem.system

  val host = config.getString("co.adhoclabs.template.host")
  val port = config.getInt("co.adhoclabs.template.port")

  val bindingFuture = Http().bindAndHandle(routes, host, port)
  bindingFuture.onComplete {
    case Success(serverBinding) => {
      println("Starting Template with:")
      println(s"- JAVA_OPTS: ${scala.util.Properties.envOrElse("JAVA_OPTS", "<EMPTY>")}")
      println(s"- CONF: ${scala.util.Properties.envOrElse("CONF", "<EMPTY>")} (file exists: ${configFile.exists})")

      println(s"Listening to ${serverBinding.localAddress}")
    }
    case Failure(error) => println(s"error: ${error.getMessage}")
  }
}
