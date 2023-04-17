package co.adhoclabs.template.business

import java.time.{Clock, Instant}
import co.adhoclabs.template.data.SongDao
import co.adhoclabs.template.models.{CreateSongRequest, Song}

import java.util.UUID
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}

trait SongManager extends BusinessBase {
  def get(id: UUID): Future[Option[Song]]
  def create(createSongRequest: CreateSongRequest): Future[Song]
  def update(song: Song): Future[Option[Song]]
  def delete(id: UUID): Future[Unit]
}

class SongManagerImpl(implicit songDao: SongDao, clock: Clock, executionContext: ExecutionContext) extends SongManager {
  override protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  override def get(id: UUID): Future[Option[Song]] = songDao.get(id)

  override def create(createSongRequest: CreateSongRequest): Future[Song] = {
    val now: Instant = clock.instant()
    val song = Song(
      id            = UUID.randomUUID,
      title         = createSongRequest.title,
      albumId       = createSongRequest.albumId,
      albumPosition = createSongRequest.albumPosition,
      createdAt     = now,
      updatedAt     = now
    )
    songDao.create(song)
  }

  override def update(song: Song): Future[Option[Song]] = songDao.update(song)

  override def delete(id: UUID): Future[Unit] = songDao.delete(id).map(_ => ())
}
