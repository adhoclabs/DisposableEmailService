package co.adhoclabs.template.data

import co.adhoclabs.template.models.{AlbumWithSongs, Song}
import org.scalatest.FutureOutcome

import scala.concurrent.Await
import scala.concurrent.duration._

class SongDaoTest extends DataTestBase {

  val existingAlbum = generateAlbum()
  val existingAlbum2 = generateAlbum()

  override def withFixture(test: NoArgAsyncTest): FutureOutcome = {
    val createF1 = albumDao.create(AlbumWithSongs(existingAlbum, List.empty[Song]))
    val createF2 = albumDao.create(AlbumWithSongs(existingAlbum2, List.empty[Song]))
    Await.result(
      for {
        _ <- createF1
        _ <- createF2
      } yield (),
      2.second)

    complete {
      super.withFixture(test)
    } lastly {
      Await.result(
        for {
          _ <- albumDao.delete(existingAlbum.id)
          _ <- albumDao.delete(existingAlbum2.id)
        } yield (),
        2.second)
    }
  }

  describe("create, get, update, delete") {
    it("should correctly execute the lifecycle of a song") {
      val expectedSong = generateSong(existingAlbum.id, albumPosition = 1)

      songDao.create(expectedSong) flatMap { createdSong: Song =>
        assert(createdSong == expectedSong)

        songDao.get(expectedSong.id) flatMap {
          case Some(gottenSong) =>
            assert(gottenSong == expectedSong)

            val expectedUpdatedSong = expectedSong.copy(title = "updated title", albumId = existingAlbum2.id, albumPosition = 145)
            songDao.update(expectedUpdatedSong) flatMap {
              case Some(updatedSong) =>
                assert(updatedSong == expectedUpdatedSong)

                songDao.delete(expectedSong.id) flatMap { count =>
                  assert(count == 1)

                  songDao.get(expectedSong.id) flatMap { a =>
                    assert(a.isEmpty)
                  }
                }
              case None => fail
            }
          case None => fail
        }
      }
    }
  }
//
//  describe("createMany") {
//    it("should ")
//  }

}
