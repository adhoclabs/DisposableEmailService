package co.adhoclabs.template

import java.time.{Clock, Instant, ZoneId}

class TestClock extends Clock {
  val nowClock: Clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
  val earlierClock: Clock = Clock.fixed(nowClock.instant().minusSeconds(10), ZoneId.systemDefault())
  val laterClock: Clock = Clock.fixed(nowClock.instant().plusSeconds(10), ZoneId.systemDefault())

  var clock: Clock = nowClock

  override def getZone: ZoneId = clock.getZone

  override def withZone(zone: ZoneId): Clock = clock.withZone(zone)

  override def instant(): Instant = clock.instant()

  def setClockToNow() = clock = nowClock
  def setClockToEarlier() = clock = earlierClock
  def setClockToLater() = clock = laterClock
}
