package co.adhoclabs.template
import akka.http.scaladsl.Http
import co.adhoclabs.template.api.TemplateApi
import co.adhoclabs.template.configuration.{config, configFile}
import scala.util.{Failure, Success}

object Main extends App with TemplateApi {
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
