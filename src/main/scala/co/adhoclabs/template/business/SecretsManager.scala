package co.adhoclabs.template.business

import co.adhoclabs.secrets.{ApiKey, ApiKeyAndSecret, SecretsClient, UsernamePassword}
import co.adhoclabs.template.Configuration

trait SecretsManager {
  def getFakeApiKey(): ApiKey
  def getFakeApiKeyAndSecret(): ApiKeyAndSecret
  def getFakeAuth(): UsernamePassword
}

class SecretsManagerImpl(implicit secretsClient: SecretsClient) extends SecretsManager {
  private val secretsConfig = Configuration.config.getConfig("co.adhoclabs.template.secrets.secret_ids")

  private val fakeApiKeySecretId: String = secretsConfig.getString("fake_api_key")
  private val fakeApiKey: Option[ApiKey] = None

  override def getFakeApiKey(): ApiKey = {
    fakeApiKey match {
      case Some(auth) => auth
      case None       => secretsClient.getSecret[ApiKey](fakeApiKeySecretId)
    }
  }

  private val fakeApiKeyAndSecretSecretId: String = secretsConfig.getString("fake_api_key_and_secret")
  private val fakeApiKeyAndSecret: Option[ApiKeyAndSecret] = None

  override def getFakeApiKeyAndSecret(): ApiKeyAndSecret = {
    fakeApiKeyAndSecret match {
      case Some(auth) => auth
      case None       => secretsClient.getSecret[ApiKeyAndSecret](fakeApiKeyAndSecretSecretId)
    }
  }

  private val fakeAuthSecretId: String = secretsConfig.getString("fake_auth")
  private val fakeAuth: Option[UsernamePassword] = None

  override def getFakeAuth(): UsernamePassword = {
    fakeAuth match {
      case Some(auth) => auth
      case None       => secretsClient.getSecret[UsernamePassword](fakeAuthSecretId)
    }
  }
}
