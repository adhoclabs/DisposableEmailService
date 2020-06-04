package co.adhoclabs.template

import co.adhoclabs.template.actorsystem.executor

package object data {
  implicit val databaseConnection: DatabaseConnection = DatabaseConnectionImpl

  implicit val songDao: SongDao = new SongDaoImpl
  implicit val albumDao: AlbumDao = new AlbumDaoImpl
}
