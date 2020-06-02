package co.adhoclabs.template

package object data {
  implicit val databaseConnection: DatabaseConnection = DatabaseConnectionImpl

  implicit val songDao: SongDao = new SongDaoImpl
  implicit val albumDao: AlbumDao = new AlbumDaoImpl
}
