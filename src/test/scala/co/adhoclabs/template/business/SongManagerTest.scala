package co.adhoclabs.template.business

import co.adhoclabs.template.models.Song


class OfferManagerTest extends BusinessTestBase {
  val songManager: SongManager = new SongManagerImpl

  behavior of "get"

  it should "return a song with the supplied ID" in {
    // given
    val expectedSong: Song = Song(
      id = Some("song-id-123"),
      title = "Sunshine of Your Love"
    )

    // when
    songManager.get(expectedSong.id.get) flatMap {
      // then
      case Some(song: Song) => assert(song == expectedSong)
      case None => fail
    }
  }
}
