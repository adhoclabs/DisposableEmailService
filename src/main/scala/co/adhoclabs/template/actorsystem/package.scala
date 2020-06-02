package co.adhoclabs.template

import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext

package object actorsystem {
  implicit val system: ActorSystem = ActorSystem("template")
  implicit val executor: ExecutionContext = system.dispatcher
}
