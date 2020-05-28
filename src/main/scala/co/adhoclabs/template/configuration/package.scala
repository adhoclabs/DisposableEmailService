package co.adhoclabs.template

import com.typesafe.config.{Config, ConfigFactory}
import java.io.File

package object configuration {
  private val configPath: String = scala.util.Properties.envOrElse("CONF", "")
  implicit val configFile: File = new File(configPath)
  implicit val config: Config = {
    if(configFile.exists && configFile.isFile) {
      val parseFile = ConfigFactory.parseFile(configFile)
      ConfigFactory.load(parseFile)
    } else {
      ConfigFactory.load
    }
  }
}
