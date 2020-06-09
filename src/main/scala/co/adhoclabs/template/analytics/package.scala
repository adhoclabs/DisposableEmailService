package co.adhoclabs.template

import co.adhoclabs.analytics.AnalyticsManagerImpl
import co.adhoclabs.sqs_client.SqsClientImpl
import co.adhoclabs.sqs_client.queue.SqsQueue
import co.adhoclabs.template.exceptions.AnalyticsSqsClientFailedToInitializeException
import co.adhoclabs.template.configuration.config

package object analytics {
  // These need to be made available separately via environment variable config
  private val awsAccessKeyO: Option[String] = sys.env.get("AWS_ACCESS_KEY_ID")
  private val awsSecretAccessKeyO: Option[String] = sys.env.get("AWS_SECRET_ACCESS_KEY")
  private val awsRegionO: Option[String] = sys.env.get("AWS_REGION")

  private val queueNames: List[String] = List(
    config.getString("co.adhoclabs.braze-sdk.queue_name"),
    config.getString("co.adhoclabs.braze-sdk.attributes_queue_name"),
    config.getString("co.adhoclabs.amplitude-sdk.queue_name")
  )

  implicit val sqsClient = (awsAccessKeyO, awsSecretAccessKeyO, awsRegionO) match {
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

  implicit val analyticsManager = new AnalyticsManagerImpl
}
