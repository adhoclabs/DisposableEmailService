package co.adhoclabs.template.business

import co.adhoclabs.sqs_client.SqsClient
import co.adhoclabs.template.Configuration
import org.slf4j.{Logger, LoggerFactory}

trait SqsManager extends BusinessBase {
  def sendFakeSqsEvent(payload: String): Unit
}

class SqsManagerImpl(implicit sqsClient: SqsClient) extends SqsManager {
  override protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  private val fakeQueueName: String = Configuration.sqsConfig.getString("fake_queue.queue_name")

  override def sendFakeSqsEvent(payload: String): Unit = {
    sqsClient.sendMessage(payload, fakeQueueName)
  }
}

