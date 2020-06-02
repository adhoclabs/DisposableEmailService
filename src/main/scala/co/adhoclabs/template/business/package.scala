package co.adhoclabs.template

import co.adhoclabs.template.actorsystem.executor

package object business {
  implicit val songManager: SongManager = new SongManagerImpl
  implicit val albumManager: AlbumManager = new AlbumManagerImpl
}
