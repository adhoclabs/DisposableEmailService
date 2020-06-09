package co.adhoclabs.template.analytics

import co.adhoclabs.analytics.{AxString, AxValue, Event}
import co.adhoclabs.model.AnalyticsPlatformSettings
import co.adhoclabs.template.models.Album

case class AlbumCreatedAnalyticsEvent(album: Album) extends Event {
  override def userId: String = "66505341-0e60-4650-b48c-e1c81947c6bb"
  override def name: String = "album_created"
  override def platformSettings: List[AnalyticsPlatformSettings] = AnalyticsPlatformSettings.AmplitudeAndBraze
  override def properties: Map[String, AxValue] = super.properties ++ Map(
    "album_id" -> AxString(album.id.toString)
  )

  override def userProperties: Map[String, AxValue] = super.userProperties ++ Map(
    "favorite_album_id" -> AxString(album.title)
  )
}
