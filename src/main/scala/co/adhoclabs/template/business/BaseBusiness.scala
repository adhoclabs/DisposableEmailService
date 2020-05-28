package co.adhoclabs.template.business

import org.slf4j.LoggerFactory

trait BaseBusiness {
  val logger = LoggerFactory.getLogger(this.getClass)
}
