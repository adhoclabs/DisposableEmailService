package co.adhoclabs.template

import co.adhoclabs.template.actorsystem.executor
import co.adhoclabs.template.analytics.analyticsManager

package object business {
  implicit val songManager: SongManager = new SongManagerImpl
  implicit val albumManager: AlbumManager = new AlbumManagerImpl
}
