package co.adhoclabs.template.analytics.events

import co.adhoclabs.analytics._
import co.adhoclabs.model.AnalyticsPlatformSettings
import java.util.UUID

case class AlbumCreatedAnalyticsEvent(albumId: UUID) extends Event {
  override def userId: String = "66505341-0e60-4650-b48c-e1c81947c6bb"
  override def name: String = "album_created"
  override def platformSettings: List[AnalyticsPlatformSettings] = AnalyticsPlatformSettings.AmplitudeOnly
  override def properties: Map[String, AxValue] = super.properties ++ Map(
    "album_id" -> AxString(albumId.toString)
  )
}
