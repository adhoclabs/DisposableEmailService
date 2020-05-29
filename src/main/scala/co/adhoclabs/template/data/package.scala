package co.adhoclabs.template

package object data {
  implicit val songDao: SongDao = new SongDaoImpl
}
