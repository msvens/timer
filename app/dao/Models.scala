package dao

import java.sql.Timestamp

/**
  * @author msvens
  * @since 21/07/16
  */
case class Timer(id: Option[Int], title: String, start: Option[Timestamp], stop: Option[Timestamp], desc: Option[String])
