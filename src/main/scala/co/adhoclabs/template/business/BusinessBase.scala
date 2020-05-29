package co.adhoclabs.template.business

import org.slf4j.LoggerFactory

trait BusinessBase {
  val logger = LoggerFactory.getLogger(this.getClass)
}
