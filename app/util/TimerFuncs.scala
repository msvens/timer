package util

import java.sql.Timestamp
import java.time.{Duration, OffsetDateTime}

import dao.Timer

/**
  * Created by msvens on 28/07/16.
  */
object TimerFuncs {

  implicit def toDT(t: Timestamp): OffsetDateTime = new OffsetDateTime(t.toInstant)
  implicit def toT(dt: OffsetDateTime): Timestamp = Timestamp.from(dt.toInstant)

  def secnodsToStop(t: Timer): Long = {
    val dt = OffsetDateTime.now()
    val dur = Duration.between(dt, t.stop.get)
    dur.getSeconds
  }

  def secondsFromStart(t: Timer): Long = {
    val dur = Duration.between(t.start.get, OffsetDateTime.now())
    dur.getSeconds
  }

}
