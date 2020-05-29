package co.adhoclabs.template

package object business {
  implicit val songManager: SongManager = new SongManagerImpl
}
